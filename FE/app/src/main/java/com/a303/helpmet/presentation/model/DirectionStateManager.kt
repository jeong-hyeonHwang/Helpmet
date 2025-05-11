package com.a303.helpmet.presentation.model

import com.a303.helpmet.domain.model.DirectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object DirectionStateManager {
    private val _directionState = MutableStateFlow(DirectionState.None)
    val directionState: StateFlow<DirectionState> = _directionState

    fun update(state: DirectionState?) {
        _directionState.value = state ?: DirectionState.None
    }
}
