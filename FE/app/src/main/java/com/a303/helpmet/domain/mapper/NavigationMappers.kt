package com.a303.helpmet.domain.mapper

import com.a303.helpmet.data.dto.InstructionDto
import com.a303.helpmet.data.dto.LatLngDto
import com.a303.helpmet.data.dto.NavigationResponseDto
import com.a303.helpmet.data.dto.RouteSegmentDto
import com.a303.helpmet.domain.model.Action
import com.a303.helpmet.domain.model.Instruction
import com.a303.helpmet.domain.model.LatLng
import com.a303.helpmet.domain.model.NavigationRoute
import com.a303.helpmet.domain.model.RouteSegment

fun LatLngDto.toDomain() = LatLng(lat, lng)

fun RouteSegmentDto.toDomain() = RouteSegment(
    from    = from.toDomain(),
    to      = to.toDomain(),
    isCycle = isCycle
)

fun InstructionDto.toDomain() = Instruction(
    index           = index,
    location        = location.toDomain(),
    distanceM       = distanceM,
    distanceToHereM = distanceToHereM,
    action          = when(action) {
        "직진" -> Action.STRAIGHT
        "좌회전" -> Action.LEFT
        "우회전" -> Action.RIGHT
        "횡단보도"-> Action.CROSS
        else     -> Action.STRAIGHT
    },
    message = message
)

fun NavigationResponseDto.toDomain() = NavigationRoute(
    distanceM        = distanceM,
    estimatedTimeSec = estimatedTimeSec,
    segments         = route.map { it.toDomain() },
    instructions     = instructions.map { it.toDomain() }
)
