package com.a303.helpmet.data.repository

import android.content.Context
import com.a303.helpmet.BuildConfig
import com.a303.helpmet.data.network.socket.CommandSocketClient
import com.a303.helpmet.domain.model.command.DetectionCommand
import com.a303.helpmet.domain.model.command.DirectionCommand
import com.a303.helpmet.util.handler.getGatewayIp


class WebsocketRepository(
    private val socketClient: CommandSocketClient,
    private val context: Context
) {
    fun connect() {
        val ip = getGatewayIp(context) ?: return
        val url = "ws://$ip:${BuildConfig.SOCKET_PORT}/ws"
        socketClient.connect(url, ip)
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