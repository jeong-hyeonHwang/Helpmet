package com.a303.helpmet.util.handler

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.core.content.ContextCompat
import com.a303.helpmet.R
import java.util.*

class VoiceInteractionHandler(private val context: Context){

    private var speechRecognizer: SpeechRecognizer? = null
    private var recognitionCallback: ((String) -> Unit)? = null
    private var tts: TextToSpeech? = null
    var isTtsReady = false

    private var isListening = false
    private var isTtsSpeaking = false

    private var onTtsComplete: (() -> Unit)? = null

    init{
        initSpeechRecognizer()
        initTextToSpeech()
    }

    // STT 초기화
    private fun initSpeechRecognizer(){
        if(!SpeechRecognizer.isRecognitionAvailable(context)){
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(object: RecognitionListener {
            override fun onResults(results: Bundle?) {
                isListening = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.firstOrNull()?.let { result ->
                    Log.d("VoiceHandler", "STT Result: $result")
                    recognitionCallback?.invoke(result)
                }
            }
            override fun onReadyForSpeech(params: Bundle?) {} // 마이크 준비 완료
            override fun onBeginningOfSpeech() {}             // 사용자가 말하기 시작
            override fun onRmsChanged(rmsdB: Float) {}        // 말하는 소리의 데시벨 수치
            override fun onBufferReceived(buffer: ByteArray?) {} // 음성 데이터 수신
            override fun onEndOfSpeech() {
                // 사용자가 말하기 끝냄
                isListening = false
            }
            override fun onError(error: Int) {
                // 오류 발생
                isListening = false
                val retryableErrors = listOf(
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT, // 1번
                    SpeechRecognizer.ERROR_NETWORK,         // 2번
                    SpeechRecognizer.ERROR_SERVER,          // 4번
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT,  // 6번
                    SpeechRecognizer.ERROR_NO_MATCH,         // 7번
                    11
                    // 3: 오디오 오류, 5: 클라이언트 오류, 8: 인식기 바쁨, 9: 권한 없음
                )
                if (error in retryableErrors) {
                    restartListeningWithDelay()
                } else {
                    // 재시도 불가 오류: 권한 안내, 상태 정리 등 추가 처리
                    Log.e("VoiceHandler", "STT 중단 - 재시도 불가 코드: $error")
//                    speak(context.getString(R.string.voice_restart_failed))
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {} // 중간 인식 결과
            override fun onEvent(eventType: Int, params: Bundle?) {}  // 기타 Event
        })
    }

    private fun restartListeningWithDelay(){
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isListening && !isTtsSpeaking) {
                startListening()
            }
        }, 400)
    }

    private fun createRecognizerIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
    }

    private fun hasRecordAudioPermission(): Boolean {
        val permissionCheck = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        )
        return permissionCheck == PackageManager.PERMISSION_GRANTED
    }

    fun startListening(){
        if (!hasRecordAudioPermission()) {
            return
        }

        if (isListening || isTtsSpeaking) {
            return
        }

        isListening = true
        speechRecognizer?.cancel()
        speechRecognizer?.startListening(createRecognizerIntent())
    }

    fun stopListening(){
        isListening = false
        speechRecognizer?.stopListening()
    }

    fun updateRecognitionCallback(callback: (String) -> Unit){
        recognitionCallback = callback
    }

    private fun initTextToSpeech(){
        tts = TextToSpeech(context){status ->
            if(status == TextToSpeech.SUCCESS){
                tts?.language = Locale.KOREAN
                isTtsReady = true

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onDone(utteranceId: String?) {
                        isTtsSpeaking = false
                        Handler(Looper.getMainLooper()).post {
                            onTtsComplete?.invoke()
                            onTtsComplete = null
                        }
                    }

                    override fun onError(utteranceId: String?) {
                        isTtsSpeaking = false
                    }

                    override fun onStart(utteranceId: String?) {
                        isTtsSpeaking = true
                    }
                })
            }else{
                isTtsReady = false
            }
        }
    }

    fun speak(text: String, autoStartListening: Boolean = true) {
        if (!isTtsReady) return
        stopListening()
        isTtsSpeaking = true
        onTtsComplete = {
            isTtsSpeaking = false
            if (autoStartListening) startListening()
        }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_id")
    }


    fun destroy(){
        stopListening()
        speechRecognizer?.destroy()
        tts?.stop()
        tts?.shutdown()
    }

}