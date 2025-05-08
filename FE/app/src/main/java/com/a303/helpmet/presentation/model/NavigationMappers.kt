package com.a303.helpmet.presentation.model

import android.annotation.SuppressLint
import com.a303.helpmet.domain.model.Instruction
import com.a303.helpmet.domain.model.LatLng
import com.a303.helpmet.domain.model.NavigationRoute
import com.a303.helpmet.domain.model.RouteSegment

fun LatLng.toUi() = LatLngUi(
    latitude = lat,
    longitude = lon
)

fun RouteSegment.toUi() = SegmentUi(
    from    = from.toUi(),
    to      = to.toUi(),
    isCycle = isCycle
)

fun Instruction.toUi() = InstructionUi(
    index    = index,
    location = location.toUi(),
    message  = message
)

@SuppressLint("DefaultLocale")
fun NavigationRoute.toUiState(): NavigationUiState = NavigationUiState(
    startLocation = segments.first().from.toUi(),
    endLocation   = segments.last().to.toUi(),
    distanceText  = String.format("%.1fkm", distance / 1000),
    timeText      = "${estimatedTimeSec / 60}분",
    segments      = segments.map { it.toUi() },
    instructions  = instructions.map { it.toUi() }
)
