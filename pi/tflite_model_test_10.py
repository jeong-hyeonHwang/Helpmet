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

# 활성 WebSocket 저장소
websockets = set()

async def broadcast_frame_to_websockets(frame, websockets):
    """모든 WebSocket 클라이언트에 이미지 프레임을 전송합니다."""
    if not websockets:
        return  # 연결된 클라이언트가 없으면 처리하지 않음

    try:
        # JPEG 형식으로 이미지 인코딩 (품질과 크기의 균형)
        # 품질을 높이려면 [1, 100] 범위 내에서 높은 값 사용
        encode_param = [int(cv2.IMWRITE_JPEG_QUALITY), 70]
        _, buffer = cv2.imencode('.jpg', frame, encode_param)

        # Base64로 인코딩
        jpg_as_text = base64.b64encode(buffer).decode('utf-8')

        # 메시지 구성
        message = {
            "type": "frame",
            "data": jpg_as_text,
            "timestamp": round(time.time() * 1000)  # 밀리초 단위 타임스탬프
        }

        # JSON 직렬화
        data = json.dumps(message)

        # 모든 WebSocket 클라이언트에 전송
        await asyncio.gather(*[ws.send_str(data) for ws in websockets])

    except Exception as e:
        print(f"[오류] 프레임 전송 중 오류 발생: {str(e)}")

# WebRTC 관련 설정
pcs = set()

class CameraStreamTrack(VideoStreamTrack):
    """카메라에서 비디오 프레임을 캡처하는 WebRTC 트랙입니다."""

    def __init__(self):
        super().__init__()
        self.frame_id = 0
        self.fps_counter = 0
        self.fps_timer = time.time()
        self.fps = 0

        # 카메라 열기 (재시도 포함)
        retries = 5
        for i in range(retries):
            self.cap = cv2.VideoCapture(
                "v4l2src device=/dev/video0 ! video/x-raw,width=640,height=480,framerate=15/1 ! "
                "videoconvert ! video/x-raw,format=BGR ! appsink",
                cv2.CAP_GSTREAMER
            )
            if self.cap.isOpened():
                break
            print(f"[카메라] 열기 실패! 재시도: {i+1}/{retries}")
            time.sleep(0.3)

        if not self.cap.isOpened():
            print("[카메라] GStreamer로 열기 실패, 일반 모드로 시도합니다.")
            self.cap = cv2.VideoCapture(0)
            if not self.cap.isOpened():
                raise RuntimeError("카메라를 열 수 없습니다.")

        print("[카메라] 초기화 완료")

    async def recv(self):
        # 타임스탬프 획득
        pts, time_base = await self.next_timestamp()

        # 카메라에서 프레임 읽기
        ret, frame = self.cap.read()
        if not ret:
            print("[카메라] 프레임 읽기 실패")
            frame = np.zeros((480, 640, 3), dtype=np.uint8)

        self.frame_id += 1
        current_time = time.time()

        # 30프레임마다 WebSocket으로 프레임 전송
        if self.frame_id % 10 == 1 and websockets:
            await broadcast_frame_to_websockets(frame, websockets)

        # FPS 계산
        self.fps_counter += 1
        if current_time - self.fps_timer > 1.0:  # 1초마다 FPS 업데이트
            self.fps = self.fps_counter / (current_time - self.fps_timer)
            self.fps_counter = 0
            self.fps_timer = current_time

        # 비디오 프레임 반환
        video_frame = VideoFrame.from_ndarray(frame, format="bgr24")
        video_frame.pts = pts
        video_frame.time_base = time_base

        return video_frame

    async def stop(self):
        if self.cap and self.cap.isOpened():
            self.cap.release()
        await super().stop()

# 웹 서버 라우트 설정
routes = web.RouteTableDef()

@routes.get("/")
async def index(request):
    """메인 페이지를 제공합니다."""
    content = """
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>라즈베리파이 WebRTC 스트리밍</title>
        <style>
            html, body {
                margin: 0;
                padding: 0;
                background: #FFF;
                width: 100vw;
                height: 100vh;
                font-family: Arial, sans-serif;
                overflow: hidden;
            }

            .container {
                display: flex;
                height: 100vh;
                flex-direction: column;
            }

            .video-container {
                flex: 1;
                position: relative;
                background-color: #000;
                overflow: hidden;
            }

            video {
                width: 100%;
                height: 100%;
                object-fit: contain;
            }

            .status-bar {
                height: 30px;
                background-color: #333;
                color: white;
                display: flex;
                align-items: center;
                padding: 0 10px;
            }

            .status-indicator {
                width: 10px;
                height: 10px;
                border-radius: 50%;
                background-color: red;
                margin-right: 10px;
            }

            .status-indicator.connected {
                background-color: green;
            }
        </style>
    </head>
    <body>
        <div class="container">
            <div class="video-container">
                <video id="video" autoplay playsinline muted></video>
            </div>
            <div class="status-bar">
                <div id="statusIndicator" class="status-indicator"></div>
                <span id="statusPanel">연결 중...</span>
            </div>
        </div>

        <script>
            const video = document.getElementById('video');
            const statusPanel = document.getElementById('statusPanel');
            const statusIndicator = document.getElementById('statusIndicator');

            let ws = null;
            let pc = null;

            // WebSocket 연결 설정
            function setupWebSocket() {
                const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
                const wsUrl = `${protocol}//${window.location.host}/ws`;

                ws = new WebSocket(wsUrl);

                ws.onopen = () => {
                    console.log('WebSocket 연결 성공');
                    statusPanel.textContent = 'WebSocket 연결됨';
                };

                ws.onmessage = (event) => {
                    try {
                        const data = JSON.parse(event.data);
                        console.log('WebSocket 메시지 수신:', data);
                    } catch (error) {
                        console.error('메시지 처리 오류:', error);
                    }
                };

                ws.onclose = () => {
                    console.log('WebSocket 연결 종료됨');
                    statusPanel.textContent = 'WebSocket 연결 끊김';
                    statusIndicator.classList.remove('connected');
                    // 재연결 시도
                    setTimeout(setupWebSocket, 5000);
                };

                ws.onerror = (error) => {
                    console.error('WebSocket 오류:', error);
                    statusPanel.textContent = 'WebSocket 오류 발생';
                    statusIndicator.classList.remove('connected');
                };
            }

            // WebRTC 연결 설정
            async function setupWebRTC() {
                try {
                    console.log("WebRTC 연결 시작");
                    pc = new RTCPeerConnection();
                    pc.addTransceiver("video", { direction: "recvonly" });
                    console.log("RTCPeerConnection() 생성 완료");

                    pc.ontrack = (event) => {
                        console.log("비디오 트랙 수신");
                        video.srcObject = event.streams[0];
                        video.play().catch(e => console.error("비디오 재생 오류:", e));
                        statusPanel.textContent = '비디오 스트림 연결됨';
                        statusIndicator.classList.add('connected');
                    };

                    pc.onconnectionstatechange = () => {
                        console.log(`연결 상태 변경: ${pc.connectionState}`);
                        if (pc.connectionState === 'connected') {
                            statusPanel.textContent = '비디오 스트림 연결됨';
                            statusIndicator.classList.add('connected');
                        } else if (pc.connectionState === 'disconnected' ||
                                  pc.connectionState === 'failed') {
                            statusPanel.textContent = '비디오 스트림 끊김';
                            statusIndicator.classList.remove('connected');
                            // 재연결 시도
                            setTimeout(() => {
                                if (pc) {
                                    pc.close();
                                }
                                setupWebRTC();
                            }, 5000);
                        }
                    };

                    const offer = await pc.createOffer();
                    await pc.setLocalDescription(offer);

                    const response = await fetch("/offer", {
                        method: "POST",
                        body: JSON.stringify({
                            sdp: pc.localDescription.sdp,
                            type: pc.localDescription.type,
                        }),
                        headers: { "Content-Type": "application/json" },
                    });

                    const answer = await response.json();
                    await pc.setRemoteDescription(answer);
                    console.log("WebRTC 연결 설정 완료");

                } catch (e) {
                    console.error("WebRTC 연결 오류:", e);
                    statusPanel.textContent = 'WebRTC 연결 오류';
                    statusIndicator.classList.remove('connected');
                    // 재연결 시도
                    setTimeout(setupWebRTC, 5000);
                }
            }

            // 페이지 종료 처리
            window.addEventListener("beforeunload", async () => {
                try {
                    console.log("연결 종료");
                    if (pc) {
                        pc.close();
                    }
                    await navigator.sendBeacon("/disconnect");
                } catch (e) {
                    console.warn("disconnect 전송 실패:", e);
                }
            });

            // 초기화
            setupWebSocket();
            setupWebRTC();
        </script>
    </body>
    </html>
    """
    return web.Response(text=content, content_type="text/html")

@routes.get("/ws")
async def websocket_handler(request):
    """WebSocket 연결을 처리합니다."""
    print("[WS] 클라이언트 WebSocket 연결 요청")
    ws = web.WebSocketResponse()
    await ws.prepare(request)

    # WebSocket 컬렉션에 추가
    websockets.add(ws)
    print(f"[WS] 클라이언트 WebSocket 연결됨 (현재 {len(websockets)}개 연결)")

    try:
        async for msg in ws:
            print(f"[WS] raw msg 수신: {msg}")
            if msg.type == web.WSMsgType.TEXT:
                print(f"[WS] 메시지 수신: {msg.data}")
                # 메시지 처리 (필요한 경우)
                await ws.send_str(f"수신 확인: {msg.data}")
            elif msg.type == "CAR_DETECTED":
                print("[DANGER] 자동차가 감지되었습니다: {msg.data}")
            elif msg.type == web.WSMsgType.ERROR:
                print(f"[WS] 오류: {ws.exception()}")
    finally:
        # WebSocket 연결 종료 시 컬렉션에서 제거
        websockets.remove(ws)
        print(f"[WS] 연결 종료 (현재 {len(websockets)}개 연결)")

    return ws

@routes.post("/offer")
async def offer(request):
    """WebRTC offer를 처리합니다."""
    params = await request.json()
    offer = RTCSessionDescription(sdp=params["sdp"], type=params["type"])

    pc = RTCPeerConnection()
    pcs.add(pc)

    print(f"[RTC] 새 연결 생성 (현재 {len(pcs)}개 연결)")

    try:
        video = CameraStreamTrack()
        pc.addTrack(video)
    except RuntimeError as e:
        print(f"[에러] 카메라 열기 실패: {e}")
        await pc.close()
        pcs.discard(pc)
        return web.Response(status=500, text="카메라 열기 실패")

    # 연결 상태 변경 이벤트 핸들러
    @pc.on("connectionstatechange")
    async def on_connectionstatechange():
        print(f"[RTC] 연결 상태: {pc.connectionState}")
        if pc.connectionState in ["closed", "failed", "disconnected"]:
            await video.stop()
            await pc.close()
            pcs.discard(pc)
            print(f"[RTC] 연결 해제 및 리소스 정리 완료 (현재 {len(pcs)}개 연결)")

    # offer 처리 및 answer 생성
    await pc.setRemoteDescription(offer)
    answer = await pc.createAnswer()
    await pc.setLocalDescription(answer)

    return web.json_response({
        "sdp": pc.localDescription.sdp,
        "type": pc.localDescription.type
    })

@routes.post("/disconnect")
async def disconnect(request):
    """연결 종료 요청을 처리합니다."""
    print("[클라이언트] 연결 끊음 신호 수신")
    for pc in list(pcs):
        await pc.close()
        pcs.discard(pc)
    return web.Response(text="Disconnected")

@routes.get("/info")
async def get_info(request):
    """서비스 정보를 제공합니다."""
    print("[API] get_info 실행")

    return web.json_response({
        "serviceName": "HELPMET"
    })


def cleanup():
    """리소스를 정리합니다."""
    print("[메인] 리소스 정리 중...")

    # WebRTC 연결 정리
    tasks = []
    for pc in list(pcs):
        tasks.append(pc.close())

    if tasks:
        loop = asyncio.get_event_loop()
        loop.run_until_complete(asyncio.gather(*tasks))

    print("[메인] 리소스 정리 완료")

# 애플리케이션 생성
def create_app():
    """웹 애플리케이션을 생성합니다."""
    app = web.Application()
    app.add_routes(routes)

    # 정리 콜백 등록
    app.on_shutdown.append(lambda app: cleanup())

    return app

# 서버 시작
if __name__ == "__main__":
    try:
        print("==== WebRTC 비디오 스트리밍 서버 시작됨 ====")
        print("- 브라우저에서 http://192.168.4.1:8080 에 접속하세요.")

        # 웹 서버 시작
        web.run_app(create_app(), host="0.0.0.0", port=8080)

    except KeyboardInterrupt:
        print("서버 종료 요청 받음...")
    except Exception as e:
        print(f"서버 오류 발생: {str(e)}")
        traceback.print_exc()
    finally:
        cleanup()
        print("서버가 종료되었습니다.")
