package com.a303.helpmet.domain.mapper

import com.a303.helpmet.data.dto.response.InstructionDto
import com.a303.helpmet.data.dto.response.LatLngDto
import com.a303.helpmet.data.dto.response.NavigationResponseDto
import com.a303.helpmet.data.dto.response.RouteSegmentDto
import com.a303.helpmet.domain.model.Action
import com.a303.helpmet.domain.model.Instruction
import com.a303.helpmet.domain.model.LatLng
import com.a303.helpmet.domain.model.NavigationRoute
import com.a303.helpmet.domain.model.RouteSegment

fun LatLngDto.toDomain() = LatLng(lat, lng)

fun RouteSegmentDto.toDomain() = RouteSegment(
    from    = from.toDomain(),
    to      = to.toDomain(),
    isCycle = isCycle,
    distance = distance
)

fun InstructionDto.toDomain() = Instruction(
    index           = index,
    location        = location.toDomain(),
    distance       = distance,
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
    distance        = distance,
    estimatedTimeSec = estimatedTimeSec,
    startAddress = startAddr,
    endAddress = endAddr,
    segments         = route.map { it.toDomain() },
    instructions     = instructions.map { it.toDomain() }
)
