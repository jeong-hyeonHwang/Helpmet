package com.a303.helpmet.domain.usecase

import com.a303.helpmet.data.repository.WebsocketRepository
import com.a303.helpmet.domain.model.command.DetectionCommand


class SendDetectionCommandUseCase(
    private val repository: WebsocketRepository
) {
    operator fun invoke(command: DetectionCommand) {
        repository.sendDetectionCommand(command)
    }
}
