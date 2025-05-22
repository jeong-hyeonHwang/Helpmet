## 실행방법

1. 가상환경 생성
   `python -m venv .venv`

2. 가상환경 활성화
   `./.venv/Scripts/activate`

3. 의존성 다운로드
   `pip install -r requirements.txt`

4. main 파일 실행
   `python main.py`

5. 웹캠 서버 실행
   `python ip_webcam_bridge.py --webcam http://[웹캠 IPv4]:8080/video --server ws://[내 서버 IPv4]:8000/ws/test-client-1`
