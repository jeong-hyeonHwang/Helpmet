package com.a303.helpmet.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.a303.helpmet.BuildConfig
import com.a303.helpmet.data.network.socket.CommandSocketClient
import com.a303.helpmet.domain.model.command.DetectionCommand
import com.a303.helpmet.domain.model.command.DirectionCommand
import com.a303.helpmet.util.handler.getGatewayIp
import okhttp3.OkHttpClient


class WebsocketRepository(
    private val socketClient: CommandSocketClient,
    private val context: Context
) {
    fun setClient(client: OkHttpClient) {
        socketClient.setClient(client)
    }

    fun connect(ip: String) {
        val url = "ws://$ip:${BuildConfig.SOCKET_PORT}/ws"
        socketClient.connect(url, ip)
    }

    fun connect(onFrameReceived: (Bitmap) -> Unit) {
        val ip = getGatewayIp(context) ?: return
        val url = "ws://$ip:${BuildConfig.SOCKET_PORT}/ws"
        socketClient.connect(url, ip, onFrameReceived)
    }

    fun sendDirectionCommand(command: DirectionCommand) {
        socketClient.sendDirectionCommand(command)
    }

    fun sendDetectionCommand(command: DetectionCommand) {
        socketClient.sendDetectionCommand(command)
    }

    fun disconnect() {
        socketClient.disconnect()
    }
}