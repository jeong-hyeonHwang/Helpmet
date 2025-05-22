from gtts import gTTS
import os

class SpeakerModule:
    def __init__(self):
        print("USB 스피커 모듈 실행")
        self.tts = None

    def generate(self, message):
        self.tts = gTTS("안녕하세요 김민서입니다", lang='ko')
        self.tts.save("info_voice.mp3")
        os.system("mpg123 -a plughw:1,0 info_voice.mp3")

speaker_module = SpeakerModule()

speaker_module.generate("USB 스피커 모듈이 실행되었습니다")
