package com.a303.helpmet.data.network.socket

import com.a303.helpmet.domain.model.DirectionCommand
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DirectionSocketClient : BaseSocketClient() {
    private val json = Json { ignoreUnknownKeys = true }

    fun sendCommand(command: DirectionCommand) {
        val message = json.encodeToString(command)
        webSocket?.send(message)
    }

    override fun onMessage(text: String) { }
}
