package com.a303.helpmet.data.service

import com.a303.helpmet.data.dto.response.DeviceInfoResponse
import retrofit2.Response
import retrofit2.http.GET

interface DeviceService {
    @GET("/info")
    suspend fun getDeviceInfo(): Response<DeviceInfoResponse>
}

