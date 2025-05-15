package com.a303.helpmet.data.network.socket

import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.serialization.json.JsonObject
import okhttp3.*

abstract class BaseSocketClient {
    private val client = OkHttpClient()
    protected var webSocket: WebSocket? = null
    private var isConnected: Boolean = false
    private var isConnecting = false
    private var retryCount = 0
    private val maxRetries = 3
    private val retryDelay = 2000L

    private var lastUrl = ""
    private var lastIp = ""


    open fun connect(url: String, ip: String) {
        if (isConnected || isConnecting) {
//            Log.d("WebSocket", "이미 연결되어 있음: 중복 연결 방지 in BSC")
            return
        }
        isConnecting = true;
        lastUrl = url
        lastIp = ip

        webSocket?.cancel()
        webSocket = null

        val request = Request.Builder()
            .url(url)
            .build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
//                Log.d("WebSocket", "✅ 연결됨: $url  in BSC")
                isConnected = true;
                isConnecting = false
                retryCount = 0
                onOpen(ws)
            }

            override fun onMessage(ws: WebSocket, text: String) {
//                Log.d("WebSocket", "📩 수신 메시지: $text  in BSC")
                onMessage(text)
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
//                Log.e("WebSocket", "❌ 연결 실패: ${t}  in BSC")
                isConnected = false
                isConnecting = false
                if(retryCount < maxRetries){
                    retryCount++
                    Handler(Looper.getMainLooper()).postDelayed({
                        connect(lastUrl, lastIp)
                    }, 2000)
                }
                onFailure(t)
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
//                Log.d("WebSocket", "🔌 연결 종료: $reason in BSC")
                isConnected = false
                isConnecting = false
                onClosed()
            }
        })
    }

    fun disconnect() {
        if (!isConnected) {
//            Log.w("WebSocket", "연결되어 있지 않아 메시지 전송 불가  in BSC")
            return
        }
        retryCount = 0
        isConnected = false
        webSocket?.close(1000, "종료 요청  in BSC")
        webSocket = null
    }

    fun sendJson(json: JsonObject) {
        val message = json.toString()
        val success = webSocket?.send(message) ?: false
        if (success) {
//            Log.d("WebSocket", "✅ 메시지 전송됨: $message  in BSC")
        } else {
//            Log.e("WebSocket", "❌ 메시지 전송 실패  in BSC")
        }
    }

    // 서브클래스가 구현해야 할 콜백들
    protected open fun onOpen(ws: WebSocket) {}
    protected abstract fun onMessage(text: String)
    protected open fun onFailure(t: Throwable) {}
    protected open fun onClosed() {}
}
