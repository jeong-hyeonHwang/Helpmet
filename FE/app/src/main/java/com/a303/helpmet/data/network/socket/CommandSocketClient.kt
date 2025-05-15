package com.a303.helpmet.data.network.socket

import com.a303.helpmet.domain.model.command.DetectionCommand
import com.a303.helpmet.domain.model.command.DirectionCommand
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CommandSocketClient : BaseSocketClient() {
    private val json = Json { ignoreUnknownKeys = true }

    fun sendDirectionCommand(command: DirectionCommand) {
        val message = json.encodeToString(command)
        webSocket?.send(message)
    }

    fun sendDetectionCommand(command: DetectionCommand) {
        val message = json.encodeToString(command)
        webSocket?.send(message)
    }

    override fun onMessage(text: String) { }

}