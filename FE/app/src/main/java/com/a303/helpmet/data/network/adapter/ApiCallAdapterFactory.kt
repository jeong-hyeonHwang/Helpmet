package com.a303.helpmet.data.network.adapter

import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class ApiCallAdapterFactory : CallAdapter.Factory() {
    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != Call::class.java) {
            return null
        }

        if (returnType !is ParameterizedType) {
            throw IllegalStateException("ApiResult must be parameterized")
        }

        val responseType = getParameterUpperBound(0, returnType)
        return ApiCallAdapter<Any>(responseType)
    }
}
