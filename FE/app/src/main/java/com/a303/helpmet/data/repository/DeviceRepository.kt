package com.a303.helpmet.data.repository

import com.a303.helpmet.data.dto.response.DeviceInfoResponse
import com.a303.helpmet.data.service.DeviceService
import retrofit2.Response

class DeviceRepository(private val service: DeviceService) {
    suspend fun getServiceInfo(): Response<DeviceInfoResponse> {
        return service.getDeviceInfo()
    }
}

