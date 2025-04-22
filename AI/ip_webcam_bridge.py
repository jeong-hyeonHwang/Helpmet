import cv2
import asyncio
import websockets
import json
import base64
import time
import argparse
import threading

# 설정
WEBCAM_URL = "http://192.168.0.10:8080/video"
SERVER_WS_URL = "ws://192.168.0.100:8000/ws/test-client-2"

# 프레임 스트림 클래스 (지연 최소화)
class VideoStream:
    def __init__(self, src):
        self.cap = cv2.VideoCapture(src)
        self.ret, self.frame = self.cap.read()
        self.running = True
        self.lock = threading.Lock()
        self.thread = threading.Thread(target=self.update, daemon=True)
        self.thread.start()

    def update(self):
        while self.running:
            ret, frame = self.cap.read()
            if ret:
                with self.lock:
                    self.ret = ret
                    self.frame = frame

    def read(self):
        with self.lock:
            return self.ret, self.frame.copy() if self.frame is not None else (False, None)

    def release(self):
        self.running = False
        self.thread.join()
        self.cap.release()

# OpenCV 창 설정
def setup_window():
    cv2.namedWindow("IP Webcam Stream", cv2.WINDOW_NORMAL)
    # cv2.resizeWindow("IP Webcam Stream", 320, 240)

async def connect_and_stream():
    setup_window()

    print(f"IP Webcam 스트림에 연결 시도: {WEBCAM_URL}")
    stream = VideoStream(WEBCAM_URL)
    await asyncio.sleep(1)  # 연결 안정화 대기

    print("IP Webcam 스트림 연결 성공!")
    print(f"GPU 서버 WebSocket에 연결 시도: {SERVER_WS_URL}")
    
    try:
        async with websockets.connect(SERVER_WS_URL) as websocket:
            print("GPU 서버 WebSocket 연결 성공!")

            # 분석 시작 명령 전송
            start_command = {
                "type": "command", 
                "command": "start_analysis"
            }
            await websocket.send(json.dumps(start_command))
            
            response_task = asyncio.create_task(receive_responses(websocket))

            frame_count = 0
            skip_factor = 8  # 분석용 프레임 스킵 비율

            try:
                while True:
                    ret, frame = stream.read()
                    if not ret or frame is None:
                        print("프레임을 가져오지 못했습니다. 재시도 중...")
                        await asyncio.sleep(1)
                        continue

                    frame_count += 1

                    # 표시용 프레임 (항상 표시)
                    display_frame = cv2.resize(frame.copy(), (320, 240))
                    cv2.imshow("IP Webcam Stream", display_frame)

                    # 분석 프레임 전송
                    if frame_count % skip_factor == 0:
                        resized = cv2.resize(frame, (320, 240))
                        _, buffer = cv2.imencode('.jpg', resized, [cv2.IMWRITE_JPEG_QUALITY, 30])
                        img_str = base64.b64encode(buffer).decode('utf-8')
                        frame_data = {
                            "type": "frame",
                            "image": img_str
                        }
                        await websocket.send(json.dumps(frame_data))

                    if cv2.waitKey(1) == ord('q'):
                        break

                    await asyncio.sleep(0.025)  # 속도 제한

            finally:
                stop_command = {
                    "type": "command", 
                    "command": "stop_analysis"
                }
                await websocket.send(json.dumps(stop_command))
                response_task.cancel()
                stream.release()
                cv2.destroyAllWindows()
    
    except Exception as e:
        print(f"WebSocket 연결 오류: {e}")
        stream.release()
        cv2.destroyAllWindows()

async def receive_responses(websocket):
    try:
        while True:
            response = await websocket.recv()
            data = json.loads(response)
            
            if data.get("type") == "analysis_result" and data.get("approaching_objects"):
                approaching = data.get("approaching_objects")
                print("\n====== 접근 중인 객체 감지! ======")
                for obj in approaching:
                    class_name = obj.get("class")
                    distance = obj.get("distance", 0.0)
                    speed = obj.get("speed", 0.0)
                    is_alert = obj.get("alert", False)
                    alert_reason = obj.get('alert_reason')

                    print(is_alert)
                    if is_alert:
                        print(f"🚨 [경고] 객체: {class_name}, 거리: {distance:.2f}, 속도: {speed:.4f}, 이유: {alert_reason}")
                    else:
                        print(f"객체: {class_name}, 거리: {distance:.2f}, 속도: {speed:.4f}")
                print("===============================\n")

            elif data.get("type") == "status":
                print(f"서버 상태 메시지: {data.get('status')}")

            elif data.get("type") == "error":
                print(f"서버 오류 메시지: {data.get('message')}")

    except asyncio.CancelledError:
        print("응답 수신 태스크 종료")
    except Exception as e:
        print(f"응답 수신 오류: {e}")


def parse_arguments():
    parser = argparse.ArgumentParser(description='IP Webcam to GPU Server Bridge')
    parser.add_argument('--webcam', type=str, default=WEBCAM_URL,
                        help='IP Webcam 스트림 URL (예: http://192.168.0.10:8080/video)')
    parser.add_argument('--server', type=str, default=SERVER_WS_URL,
                        help='GPU 서버 WebSocket URL (예: ws://192.168.0.100:8000/ws/client1)')
    return parser.parse_args()

if __name__ == "__main__":
    args = parse_arguments()
    WEBCAM_URL = args.webcam
    SERVER_WS_URL = args.server
    asyncio.run(connect_and_stream())
