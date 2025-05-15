package com.a303.helpmet.data.network.api_services

import kotlinx.serialization.Serializable

@Serializable
sealed class ApiResult<out T> {
    data class Success<out T>(val data: T): ApiResult<T>()
    data class Error(
        val code: Int,
        val message: String,
        val throwable: Throwable? = null
    ): ApiResult<Nothing>()
}