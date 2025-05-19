package com.a303.helpmet.data.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class DeviceInfoResponse(
    val serviceName: String,
    val isAccess: Boolean
)

@Serializable
data class DeviceInfo(
    val isValidPi: Boolean,
    val isAccess: Boolean
)