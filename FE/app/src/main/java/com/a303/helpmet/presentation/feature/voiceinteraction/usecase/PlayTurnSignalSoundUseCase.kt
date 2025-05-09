package com.a303.helpmet.presentation.feature.voiceinteraction.usecase

import com.a303.helpmet.domain.model.DirectionState
import com.a303.helpmet.presentation.feature.voiceinteraction.sound.TickSoundManager

class PlayTurnSignalSoundUseCase(
    private val updateDirectionState: (DirectionState) -> Unit,
    private val tickSoundManager: TickSoundManager
) {
    operator fun invoke(state: DirectionState) {
        updateDirectionState(state)
        tickSoundManager.start()
    }
}