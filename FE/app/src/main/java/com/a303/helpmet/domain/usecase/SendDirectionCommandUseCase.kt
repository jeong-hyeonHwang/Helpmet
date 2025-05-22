package com.a303.helpmet.domain.usecase

import com.a303.helpmet.data.repository.WebsocketRepository
import com.a303.helpmet.domain.model.command.DirectionCommand

class SendDirectionCommandUseCase(
    private val repository: WebsocketRepository
) {
    operator fun invoke(command: DirectionCommand) {
        repository.sendDirectionCommand(command)
    }
}
