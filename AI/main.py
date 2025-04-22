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

# FastAPI 앱 초기화
app = FastAPI(title="YOLO Object Approach Detection Server")

# 정적 파일 및 템플릿 설정
templates = Jinja2Templates(directory="templates")
app.mount("/static", StaticFiles(directory="static"), name="static")

# YOLO 모델 및 설정
MODEL = 'yolov8n.pt'
INTEREST_CLASSES = [0, 1, 2, 3, 5, 7]  # person, bicycle, car, motorcycle, bus, truck
HISTORY_FRAMES = 10
AREA_THRESHOLD = 0.10
MIN_CONFIDENCE = 0.40

# 모델 로드
model = YOLO(MODEL)
print(f"YOLO 모델 '{MODEL}' 로드 완료!")

# 클라이언트 관리 클래스
class ConnectionManager:
    def __init__(self):
        self.active_connections: Dict[str, WebSocket] = {}
        self.clients_data: Dict[str, Dict] = {}

    async def connect(self, websocket: WebSocket, client_id: str):
        await websocket.accept()
        self.active_connections[client_id] = websocket
        self.clients_data[client_id] = {
            "object_history": {},
            "analysis_active": False
        }
        print(f"클라이언트 {client_id} 연결됨. 현재 {len(self.active_connections)}개 연결")

    def disconnect(self, client_id: str):
        self.active_connections.pop(client_id, None)
        self.clients_data.pop(client_id, None)
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
                    img_data = base64.b64decode(message.get("image"))
                    nparr = np.frombuffer(img_data, np.uint8)
                    frame = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
                    if frame is None:
                        continue
                    results = model(frame, conf=MIN_CONFIDENCE, classes=INTEREST_CLASSES)
                    approaching_objects = []
                    current_objects = set()
                    object_history = manager.get_client_history(client_id)
                    frame_height, frame_width = frame.shape[:2]

                    if len(results) > 0:
                        boxes = results[0].boxes
                        for i, box in enumerate(boxes):
                            x1, y1, x2, y2 = map(int, box.xyxy[0])
                            conf = float(box.conf[0])
                            cls_id = int(box.cls[0])
                            cls_name = model.names[cls_id]
                            w = x2 - x1
                            h = y2 - y1
                            cx = x1 + w // 2
                            cy = y1 + h // 2
                            obj_id = f"{cls_id}_{i}"
                            current_objects.add(obj_id)
                            if obj_id not in object_history:
                                object_history[obj_id] = []
                            curr_box = (cx, cy, w, h)
                            object_history[obj_id].append(curr_box)
                            if len(object_history[obj_id]) > HISTORY_FRAMES:
                                object_history[obj_id].pop(0)
                            is_approaching, approach_speed = calculate_approaching(curr_box, object_history[obj_id])
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

                            print(alert_reasons)
                            is_alert = len(alert_reasons) > 1 or distance <= ALERT_DISTANCE_THRESHOLD

                            approaching_objects.append({
                                "class": cls_name,
                                "confidence": float(conf),
                                "distance": float(distance),
                                "speed": float(approach_speed),
                                "box": [int(x1), int(y1), int(x2), int(y2)],
                                "alert": is_alert,
                                "alert_reason": ", ".join(alert_reasons) if is_alert else ""
                            })

                    old_objects = set(object_history.keys()) - current_objects
                    for obj_id in old_objects:
                        object_history.pop(obj_id, None)

                    result_data = {
                        "type": "analysis_result",
                        "approaching_objects": approaching_objects,
                        "total_objects": len(current_objects),
                        "timestamp": time.time()
                    }
                    await manager.send_message(json.dumps(result_data), client_id)

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
        "connected_clients": len(manager.active_connections)
    }

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)