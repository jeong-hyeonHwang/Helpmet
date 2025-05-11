package com.a303.helpmet.presentation.feature.voiceinteraction.usecase

import com.a303.helpmet.domain.model.DirectionState
import com.a303.helpmet.presentation.feature.voiceinteraction.sound.TickSoundManager
import com.a303.helpmet.presentation.model.DirectionStateManager

class PlayTurnSignalSoundUseCase(
    private val updateDirectionState: (DirectionState) -> Unit,
    private val tickSoundManager: TickSoundManager
) {
    operator fun invoke(state: DirectionState) {
        if (DirectionStateManager.directionState.value == state) return
        DirectionStateManager.update(state)
        if(DirectionStateManager.directionState.value == DirectionState.None) return
        tickSoundManager.start()
    }
}