import asyncio
import json
import cv2
import traceback
import numpy as np
import time
from av import VideoFrame
from aiohttp import web
from aiortc import RTCPeerConnection, RTCSessionDescription, VideoStreamTrack
import base64
from gtts import gTTS
import os
from arrow import arrow_loop, turn_on_arrow, turn_off_arrow
from contextlib import asynccontextmanager

# WebSocket 클라이언트 저장소
websockets = set()

# WebRTC 연결 저장소
pcs = set()

# arrow loop task 시작
@asynccontextmanager
async def lifespan(app):
    asyncio.create_task(arrow_loop())
    yield

# 프레임 브로드캐스트 함수
async def broadcast_frame_to_websockets(frame, websockets):
    if not websockets:
        return

    try:
        encode_param = [int(cv2.IMWRITE_JPEG_QUALITY), 70]
        _, buffer = cv2.imencode('.jpg', frame, encode_param)
        jpg_as_text = base64.b64encode(buffer).decode('utf-8')

        message = {
            "type": "frame",
            "data": jpg_as_text,
            "timestamp": round(time.time() * 1000)
        }

        data = json.dumps(message)
        await asyncio.gather(*[ws.send_str(data) for ws in websockets])

    except Exception as e:
        print(f"[오류] 프레임 전송 중 오류 발생: {str(e)}")

# 카메라 트랙 클래스
class CameraStreamTrack(VideoStreamTrack):
    def __init__(self):
        super().__init__()
        self.frame_id = 0
        self.fps_counter = 0
        self.fps_timer = time.time()
        self.fps = 0

        retries = 5
        for i in range(retries):
            self.cap = cv2.VideoCapture(
                "v4l2src device=/dev/video10 ! video/x-raw,width=640,height=480,framerate=15/1 ! "
                "videoconvert ! video/x-raw,format=BGR ! appsink",
                cv2.CAP_GSTREAMER
            )
            if self.cap.isOpened():
                break
            print(f"[카메라] 열기 실패! 재시도: {i+1}/{retries}")
            time.sleep(0.3)

        if not self.cap.isOpened():
            print("[카메라] GStreamer 실패. 일반 모드 시도.")
            self.cap = cv2.VideoCapture(0)
            if not self.cap.isOpened():
                raise RuntimeError("카메라를 열 수 없습니다.")

        print("[카메라] 초기화 완료")

    async def recv(self):
        pts, time_base = await self.next_timestamp()
        ret, frame = self.cap.read()
        if not ret:
            print("[카메라] 프레임 읽기 실패")
            frame = np.zeros((480, 640, 3), dtype=np.uint8)

        self.frame_id += 1
        current_time = time.time()

        if self.frame_id % 10 == 1 and websockets:
            await broadcast_frame_to_websockets(frame, websockets)

        self.fps_counter += 1
        if current_time - self.fps_timer > 1.0:
            self.fps = self.fps_counter / (current_time - self.fps_timer)
            self.fps_counter = 0
            self.fps_timer = current_time

        video_frame = VideoFrame.from_ndarray(frame, format="bgr24")
        video_frame.pts = pts
        video_frame.time_base = time_base

        return video_frame

    async def stop(self):
        if self.cap and self.cap.isOpened():
            self.cap.release()
        await super().stop()

# 라우트 설정
routes = web.RouteTableDef()

@routes.get("/")
async def index(request):
    with open("templates/index.html", "r", encoding="utf-8") as f:
        content = f.read()
    return web.Response(text=content, content_type="text/html")

@routes.get("/ws")
async def websocket_handler(request):
    print("[WS] 연결 요청")
    ws = web.WebSocketResponse()
    await ws.prepare(request)

    websockets.add(ws)
    print(f"[WS] 연결됨 (총 {len(websockets)})")

    try:
        async for msg in ws:
            if msg.type == web.WSMsgType.TEXT:
                try:
                    data = json.loads(msg.data)
                    msg_type = data.get("type")

                    if msg_type == "turn_left":
                        if data.get("command") == "start":
                            turn_on_arrow(0)
                        elif data.get("command") == "stop":
                            turn_off_arrow(0)

                    elif msg_type == "turn_right":
                        if data.get("command") == "start":
                            turn_on_arrow(1)
                        elif data.get("command") == "stop":
                            turn_off_arrow(1)

                    elif msg_type == "turn_off":
                        cmd = data.get("command")
                        if cmd == "left" or cmd == "both":
                            turn_off_arrow(0)
                        if cmd == "right" or cmd == "both":
                            turn_off_arrow(1)

                except Exception as e:
                    print(f"[WS] 메시지 처리 오류: {e}")
    finally:
        websockets.remove(ws)
        print(f"[WS] 연결 해제됨 (총 {len(websockets)})")

    return ws

@routes.post("/offer")
async def offer(request):
    params = await request.json()
    offer = RTCSessionDescription(sdp=params["sdp"], type=params["type"])

    pc = RTCPeerConnection()
    pcs.add(pc)
    print(f"[RTC] 연결 생성 (총 {len(pcs)})")

    try:
        video = CameraStreamTrack()
        pc.addTrack(video)
    except RuntimeError as e:
        print(f"[에러] 카메라 실패: {e}")
        await pc.close()
        pcs.discard(pc)
        return web.Response(status=500, text="카메라 열기 실패")

    @pc.on("connectionstatechange")
    async def on_connectionstatechange():
        print(f"[RTC] 상태: {pc.connectionState}")
        if pc.connectionState in ["closed", "failed", "disconnected"]:
            await video.stop()
            await pc.close()
            pcs.discard(pc)
            print("[RTC] 연결 종료 및 리소스 정리")

    await pc.setRemoteDescription(offer)
    answer = await pc.createAnswer()
    await pc.setLocalDescription(answer)

    return web.json_response({
        "sdp": pc.localDescription.sdp,
        "type": pc.localDescription.type
    })

@routes.post("/disconnect")
async def disconnect(request):
    print("[클라이언트] 연결 끊기 요청")
    for pc in list(pcs):
        await pc.close()
        pcs.discard(pc)
    return web.Response(text="Disconnected")

@routes.get("/info")
async def get_info(request):
    return web.json_response({"serviceName": "HELPMET"})

def cleanup():
    print("[메인] 리소스 정리 중...")
    tasks = [pc.close() for pc in pcs]
    if tasks:
        loop = asyncio.get_event_loop()
        loop.run_until_complete(asyncio.gather(*tasks))
    print("[메인] 리소스 정리 완료")

def create_app():
    app = web.Application()
    app.add_routes(routes)
    app.on_shutdown.append(lambda app: cleanup())
    return app

if __name__ == "__main__":
    try:
        print("==== HELPMET 스트리밍 서버 시작 ====")
        web.run_app(create_app(), host="0.0.0.0", port=8081)
    except KeyboardInterrupt:
        print("종료 요청 수신됨")
    except Exception as e:
        print(f"서버 오류: {e}")
        traceback.print_exc()
    finally:
        cleanup()
        print("서버가 종료되었습니다.")
