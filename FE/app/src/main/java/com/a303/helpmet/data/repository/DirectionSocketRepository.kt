package com.a303.helpmet.data.repository

import android.content.Context
import com.a303.helpmet.BuildConfig
import com.a303.helpmet.data.network.socket.DirectionSocketClient
import com.a303.helpmet.domain.model.DirectionCommand
import com.a303.helpmet.util.handler.getGatewayIp

class DirectionSocketRepository(
    private val socketClient: DirectionSocketClient,
    private val context: Context
) {
    fun connect() {
        val ip = getGatewayIp(context) ?: return
        val url = "ws://$ip:${BuildConfig.SOCKET_PORT}/ws"
        socketClient.connect(url, ip)
    }

    fun send(command: DirectionCommand) {
        socketClient.sendCommand(command)
    }

    fun disconnect() {
        socketClient.disconnect()
    }
}