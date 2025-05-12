import fastapi
from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from fastapi.responses import HTMLResponse
from fastapi.templating import Jinja2Templates
from fastapi.staticfiles import StaticFiles
import uvicorn
import cv2
import numpy as np
import base64
import json
import time
from ultralytics import YOLO
import asyncio
from typing import List, Dict
from collections import defaultdict
from arrow import arrow_loop, turn_on_arrow, turn_off_arrow
from contextlib import asynccontextmanager

# 확장된 타입 힌트
from typing import Dict, List, Optional, Tuple, Union

@asynccontextmanager
async def lifespan(app:FastAPI):
    asyncio.create_task(arrow_loop())
    yield

# FastAPI 앱 초기화
app = FastAPI(title="YOLO Object Approach Detection Server with ByteTrack", lifespan=lifespan)

# 정적 파일 및 템플릿 설정
templates = Jinja2Templates(directory="templates")
app.mount("/static", StaticFiles(directory="static"), name="static")

# YOLO 모델 및 설정
MODEL = 'yolov8n.pt'
INTEREST_CLASSES = [0, 1, 2, 3, 5, 7]  # person, bicycle, car, motorcycle, bus, truck
HISTORY_FRAMES = 5
AREA_THRESHOLD = 0.10
MIN_CONFIDENCE = 0.40

# 모델 로드
model = YOLO(MODEL)
print(f"YOLO 모델 '{MODEL}' 로드 완료!")

# YOLO 모델을 추적 모드로 설정
print("YOLO 모델을 추적 모드로 설정합니다...")

# 클라이언트 관리 클래스
class ConnectionManager:
    def __init__(self):
        self.active_connections: Dict[str, WebSocket] = {}
        self.clients_data: Dict[str, Dict] = {}
        # 클라이언트별 트래커 관리
        self.trackers: Dict[str, YOLO] = {}

    async def connect(self, websocket: WebSocket, client_id: str):
        await websocket.accept()
        self.active_connections[client_id] = websocket
        self.clients_data[client_id] = {
            "object_history": {},
            "analysis_active": False
        }
        # 클라이언트별 트래커 생성 (YOLO 모델의 추적 인스턴스)
        self.trackers[client_id] = YOLO(MODEL)
        print(f"클라이언트 {client_id} 연결됨. 현재 {len(self.active_connections)}개 연결")

    def disconnect(self, client_id: str):
        self.active_connections.pop(client_id, None)
        self.clients_data.pop(client_id, None)
        self.trackers.pop(client_id, None)
        print(f"클라이언트 {client_id} 연결 해제. 현재 {len(self.active_connections)}개 연결")

    async def send_message(self, message: str, client_id: str):
        if client_id in self.active_connections:
            await self.active_connections[client_id].send_text(message)

    def set_analysis_state(self, client_id: str, state: bool):
        if client_id in self.clients_data:
            self.clients_data[client_id]["analysis_active"] = state

    def get_analysis_state(self, client_id: str) -> bool:
        return self.clients_data.get(client_id, {}).get("analysis_active", False)

    def get_client_history(self, client_id: str) -> Dict:
        return self.clients_data.get(client_id, {}).get("object_history", {})

    def get_tracker(self, client_id: str) -> YOLO:
        return self.trackers.get(client_id)

manager = ConnectionManager()

# 객체 접근 계산 함수
def calculate_approaching(curr_box, history, frames=5):
    if len(history) < frames:
        return False, 0.0
    current_area = curr_box[2] * curr_box[3]
    past_areas = [box[2] * box[3] for box in history[-frames:]]
    oldest_area = past_areas[0]
    if oldest_area > 0:
        area_change_rate = (current_area - oldest_area) / oldest_area
    else:
        return False, 0.0
    approach_speed = area_change_rate / frames
    is_approaching = area_change_rate > AREA_THRESHOLD
    return is_approaching, approach_speed

# 메인 페이지
@app.get("/", response_class=HTMLResponse)
async def get_index(request: fastapi.Request):
    return templates.TemplateResponse("index.html", {"request": request})

# 웹소켓 연결 엔드포인트
@app.websocket("/ws/{client_id}")
async def websocket_endpoint(websocket: WebSocket, client_id: str):
    await manager.connect(websocket, client_id)
    try:
        frame_id = 0
        while True:
            data = await websocket.receive_text()
            try:
                message = json.loads(data)
                if message.get("type") == "command":
                    command = message.get("command")
                    if command == "start_analysis":
                        manager.set_analysis_state(client_id, True)
                        await manager.send_message(json.dumps({"type": "status", "status": "analysis_started"}), client_id)
                    elif command == "stop_analysis":
                        manager.set_analysis_state(client_id, False)
                        await manager.send_message(json.dumps({"type": "status", "status": "analysis_stopped"}), client_id)
                elif message.get("type") == "frame":
                    if not manager.get_analysis_state(client_id):
                        continue

                    frame_id += 1
                    img_data = base64.b64decode(message.get("image"))
                    nparr = np.frombuffer(img_data, np.uint8)
                    frame = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
                    if frame is None:
                        continue

                    client_tracker = manager.get_tracker(client_id)
                    if client_tracker:
                        results = client_tracker.track(
                            frame, conf=MIN_CONFIDENCE, classes=INTEREST_CLASSES,
                            persist=True, tracker="bytetrack.yaml")
                    else:
                        results = model(frame, conf=MIN_CONFIDENCE, classes=INTEREST_CLASSES)

                    online_targets = []
                    if results and len(results) > 0 and hasattr(results[0], 'boxes') and hasattr(results[0].boxes, 'id') and results[0].boxes.id is not None:
                        online_targets = results[0].boxes

                    approaching_objects = []
                    object_history = manager.get_client_history(client_id)
                    frame_height, frame_width = frame.shape[:2]

                    if len(online_targets) > 0:
                        for i, det in enumerate(online_targets):
                            if hasattr(det, 'id') and det.id is not None:
                                track_id = int(det.id[0])
                                obj_id = f"track_{track_id}"
                            else:
                                obj_id = f"temp_{frame_id}_{i}"
                                track_id = i

                            # 경계 상자
                            x1, y1, x2, y2 = map(int, det.xyxy[0])
                            w, h = x2 - x1, y2 - y1
                            cx = x1 + w // 2
                            cy = y1 + h // 2

                            # 클래스 ID 및 이름
                            cls_id = int(det.cls[0])
                            cls_conf = float(det.conf[0])
                            cls_name = model.names.get(cls_id, "unknown")

                            # 디버깅 로그 추가
                            print(f"[Frame {frame_id}] Track ID: {track_id} | Class: {cls_name} | Confidence: {cls_conf:.2f} | Box: ({x1}, {y1}, {x2}, {y2})")

                            # 객체 히스토리 관리
                            if obj_id not in object_history:
                                object_history[obj_id] = {"positions": []}

                            curr_box = (cx, cy, w, h)
                            object_history[obj_id]["positions"].append(curr_box)
                            if len(object_history[obj_id]["positions"]) > HISTORY_FRAMES:
                                object_history[obj_id]["positions"].pop(0)

                            # 접근 계산
                            is_approaching, approach_speed = calculate_approaching(
                                curr_box,
                                object_history[obj_id]["positions"]
                            )

                            relative_size = h / frame_height
                            distance = 1.0 - relative_size

                            bottom_cx = cx
                            bottom_cy = y2
                            center_x = frame_width // 2
                            center_y = frame_height
                            dx = bottom_cx - center_x
                            dy = bottom_cy - center_y
                            pixel_dist = (dx ** 2 + dy ** 2) ** 0.5
                            diag_len = (frame_width ** 2 + frame_height ** 2) ** 0.5
                            norm_dist = pixel_dist / diag_len

                            ALERT_HEIGHT_THRESHOLD = 0.7
                            ALERT_DIST_THRESHOLD = 0.1
                            ALERT_DISTANCE_THRESHOLD = 0.7

                            alert_reasons = []
                            if relative_size >= ALERT_HEIGHT_THRESHOLD:
                                alert_reasons.append("bounding box height ≥ 50%")
                            if norm_dist <= ALERT_DIST_THRESHOLD:
                                alert_reasons.append("bottom center close to screen center")

                            is_alert = len(alert_reasons) > 1 or distance <= ALERT_DISTANCE_THRESHOLD
                            cls_name = model.names.get(cls_id, "unknown")

                            # 결과 저장
                            approaching_objects.append({
                                "track_id": int(track_id),
                                "class": cls_name,
                                "confidence": float(cls_conf),
                                "distance": float(distance),
                                "speed": float(approach_speed),
                                "box": [int(x1), int(y1), int(x2), int(y2)],
                                "alert": is_alert,
                                "alert_reason": ", ".join(alert_reasons) if is_alert else ""
                            })

                    result_data = {
                        "type": "analysis_result",
                        "approaching_objects": approaching_objects,
                        "total_objects": len(approaching_objects),
                        "timestamp": time.time()
                    }
                    await manager.send_message(json.dumps(result_data), client_id)

                elif message.get("type") == "turn_left":
                    command = message.get("command")

                    if command == "start":
                        turn_on_arrow(0)

                    elif command == "stop":
                        turn_off_arrow(0)

                elif message.get("type") == "turn_right":
                    command = message.get("command")

                    if command == "start":
                        turn_on_arrow(1)
                    elif command == "stop":
                        turn_off_arrow(1)

                elif message.get("type") == "turn_off":
                    command = message.get("command")

                    if command == "left" or command == "both":
                        turn_off_arrow(0)

                    if command == "right" or command == "both":
                        turn_off_arrow(1)

            except Exception as e:
                print(f"메시지 처리 오류: {str(e)}")
                error_data = {"type": "error", "message": str(e)}
                await manager.send_message(json.dumps(error_data), client_id)



    except WebSocketDisconnect:
        manager.disconnect(client_id)

@app.get("/status")
async def check_status():
    return {
        "status": "online",
        "model": MODEL,
        "connected_clients": len(manager.active_connections),
        "tracking": "ByteTrack"
    }

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)