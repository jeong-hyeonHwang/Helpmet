package com.a303.helpmet.domain.usecase

import com.a303.helpmet.data.repository.DirectionSocketRepository
import com.a303.helpmet.domain.model.DirectionCommand

class SendDirectionCommandUseCase(
    private val repository: DirectionSocketRepository
) {
    operator fun invoke(command: DirectionCommand) {
        repository.send(command)
    }
}
