import asyncio
import json
import cv2
from aiohttp import web
from aiortc import RTCPeerConnection, RTCSessionDescription, VideoStreamTrack
from aiortc.contrib.signaling import BYE
import numpy as np
from av import VideoFrame

pcs = set()

class CameraStreamTrack(VideoStreamTrack):
    def __init__(self):
        super().__init__()
        self.cap = cv2.VideoCapture(
            "v4l2src device=/dev/video0 ! video/x-raw,width=640,height=480,framerate=15/1 ! "
            "videoconvert ! video/x-raw,format=BGR ! appsink",
            cv2.CAP_GSTREAMER
        )
        if not self.cap.isOpened():
            raise RuntimeError("Failed to open camera with GStreamer")

    async def recv(self):
        pts, time_base = await self.next_timestamp()
        ret, frame = self.cap.read()
        if not ret:
            frame = np.zeros((480, 640, 3), dtype=np.uint8)

        video_frame = VideoFrame.from_ndarray(frame, format="bgr24")
        video_frame.pts = pts
        video_frame.time_base = time_base
        return video_frame

    async def stop(self):
        if self.cap.isOpened():
            self.cap.release()
        await super().stop()

routes = web.RouteTableDef()

@routes.get("/")
async def index(request):
    return web.FileResponse("index.html")

@routes.post("/offer")
async def offer(request):
    params = await request.json()
    offer = RTCSessionDescription(sdp=params["sdp"], type=params["type"])

    pc = RTCPeerConnection()
    pcs.add(pc)

    video = CameraStreamTrack()
    pc.addTrack(video)

    @pc.on("connectionstatechange")
    async def on_connectionstatechange():
        print(f"[RTC] 연결 상태: {pc.connectionState}")
        if pc.connectionState in ["closed", "failed", "disconnected"]:
            await video.stop()
            await pc.close()
            pcs.discard(pc)
            print("[RTC] 연결 해제 및 리소스 정리 완료")

    await pc.setRemoteDescription(offer)
    answer = await pc.createAnswer()
    await pc.setLocalDescription(answer)

    return web.json_response(
        {"sdp": pc.localDescription.sdp, "type": pc.localDescription.type}
    )


@routes.post("/disconnect")
async def disconnect(request):
    print("[클라이언트] 연결 끊음 신호 수신")
    for pc in list(pcs):
        await pc.close()
        pcs.discard(pc)
    return web.Response(text="Disconnected")

@routes.get("/ws")
async def websocket_handler(request):
    print("[WS] 클라이언트 WebSocket 연결 요청")

    ws = web.WebSocketResponse()
    await ws.prepare(request)

    print("[WS] 클라이언트 WebSocket 연결됨")

    async for msg in ws:
        if msg.type == web.WSMsgType.TEXT:
            print(f"[WS] 메시지 수신: {msg.data}")
            await ws.send_str(f"수신 확인: {msg.data}")
        elif msg.type == web.WSMsgType.ERROR:
            print(f"[WS] 오류: {ws.exception()}")

    print("[WS] 연결 종료")
    return ws

app = web.Application()
app.add_routes(routes)

web.run_app(app, host="0.0.0.0", port=8080)
