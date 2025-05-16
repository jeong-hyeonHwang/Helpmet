package com.a303.helpmet.data.network.socket

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.a303.helpmet.domain.model.command.DetectionCommand
import com.a303.helpmet.domain.model.command.DirectionCommand
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONObject

class CommandSocketClient : BaseSocketClient() {
    private val json = Json { ignoreUnknownKeys = true }
    var onFrameReceived: ((Bitmap) -> Unit)? = null

    fun connect(url: String, ip: String, onFrameReceived: (Bitmap) -> Unit) {
        this.onFrameReceived = onFrameReceived
        super.connect(url, ip)
    }

    fun sendDirectionCommand(command: DirectionCommand) {
        val message = json.encodeToString(command)
        webSocket?.send(message)
    }

    fun sendDetectionCommand(command: DetectionCommand) {
        val message = json.encodeToString(command)
        webSocket?.send(message)
    }

    override fun onMessage(text: String) {
        try {
            val jsonObject = JSONObject(text)
            val type = jsonObject.optString("type")
            if (type == "frame") {
                val base64 = jsonObject.optString("data")
                val bytes = Base64.decode(base64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                onFrameReceived?.invoke(bitmap)
            }
        } catch (e: Exception) {
            Log.e("WebSocket", "메시지 파싱 오류", e)
        }
    }
}
