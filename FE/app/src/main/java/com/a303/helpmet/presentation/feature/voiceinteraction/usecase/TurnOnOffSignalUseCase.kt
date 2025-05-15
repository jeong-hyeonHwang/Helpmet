package com.a303.helpmet.presentation.feature.voiceinteraction.usecase

import android.os.Handler
import android.os.Looper
import com.a303.helpmet.domain.model.DirectionCommand
import com.a303.helpmet.domain.model.DirectionState

class TurnOnOffSignalUseCase(
    private val speak: (String) -> Unit,
    private val playTurnSignal: (DirectionState) -> Unit,
    private val sendCommand: (DirectionCommand) -> Unit
) {
    operator fun invoke(direction: DirectionState) {
        val (message, socketCommand) = when (direction) {
            DirectionState.Left -> "좌회전입니다." to DirectionCommand("turn_left", "start")
            DirectionState.Right -> "우회전입니다." to DirectionCommand("turn_right", "start")
            DirectionState.None -> null to DirectionCommand("turn_off", "both")
        }

        // 메시지가 있을 경우에만 음성 안내
        message?.let { speak(it) }

        Handler(Looper.getMainLooper()).postDelayed({
            playTurnSignal(direction)
            sendCommand(socketCommand)
        }, 1000)
    }
}
