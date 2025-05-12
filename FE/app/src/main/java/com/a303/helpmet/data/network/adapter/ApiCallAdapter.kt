package com.a303.helpmet.data.network.adapter

import com.a303.helpmet.data.network.api_services.ApiResult
import com.a303.helpmet.data.network.api_services.BaseResponse
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type

class ApiCallAdapter<T>(
    private val responseType: Type
) : CallAdapter<T, suspend () -> ApiResult<T>> {

    override fun responseType(): Type = responseType

    override fun adapt(call: Call<T>): suspend () -> ApiResult<T> = {
        try {
            val response = call.execute()
            val body = response.body()

            if (response.isSuccessful && body is BaseResponse<*>) {
                if (body.status == 200) {
                    @Suppress("UNCHECKED_CAST")
                    ApiResult.Success(body.data as T)
                } else {
                    ApiResult.Error(body.status, body.message)
                }
            } else {
                ApiResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            ApiResult.Error(-1, "네트워크 오류", e)
        }
    }
}
