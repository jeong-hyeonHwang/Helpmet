package com.a303.helpmet.data.network.api_services

import kotlinx.serialization.Serializable

@Serializable
data class BaseResponse<T> (
    val status: Int,
    val message: String,
    val data: T
)