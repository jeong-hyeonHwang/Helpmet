# run_tflite.py
import tflite_runtime.interpreter as tflite
import numpy as np
import cv2
import os

# 모델 경로
MODEL_PATH = 'yolov8n.tflite'  # 복사한 tflite 모델명에 따라 변경
USE_DISPLAY = os.environ.get("DISPLAY", "") != ""

# TFLite 인터프리터 로드
interpreter = tflite.Interpreter(model_path=MODEL_PATH)
interpreter.allocate_tensors()

input_details = interpreter.get_input_details()
output_details = interpreter.get_output_details()

# 입력 텐서 사이즈 확인
height, width = input_details[0]['shape'][1:3]

# 카메라 열기
cap = cv2.VideoCapture(0)
assert cap.isOpened(), "카메라를 열 수 없습니다."

while True:
    ret, frame = cap.read()
    if not ret or frame is None:
        print("프레임을 읽지 못했습니다.")
        continue
    # 확인용 출력
    print("Raw frame shape:", frame.shape)

    # frame을 RGB로 변환 (만약 BGR로 들어오는 경우)
    if len(frame.shape) == 2:  # 흑백
        frame = cv2.cvtColor(frame, cv2.COLOR_GRAY2RGB)
    elif frame.shape[2] == 1:  # 채널이 1개
        frame = cv2.cvtColor(frame, cv2.COLOR_GRAY2RGB)
    elif frame.shape[2] == 3:  # 일반적인 경우
        frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)

    # 모델 입력 크기로 리사이즈
    img = cv2.resize(frame, (width, height))

    # 차원 및 dtype 맞추기
    input_data = np.expand_dims(img, axis=0).astype(input_details[0]['dtype'])
    print("Final input_data shape:", input_data.shape)

    # 추론
    interpreter.set_tensor(input_details[0]['index'], input_data)
    interpreter.invoke()
    output_data = interpreter.get_tensor(output_details[0]['index'])
    print(output_data)

    if USE_DISPLAY:
        cv2.imshow('frame', frame)
        if cv2.waitKey(1) == ord('q'):
            break
    else:
        # AP 모드에서는 sleep 추가해서 CPU 과점유 방지
        import time
        time.sleep(0.1)

cap.release()
cv2.destroyAllWindows()

