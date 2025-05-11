package com.a303.helpmet.data.repository

import com.a303.helpmet.data.service.DeviceService

class DeviceRepository(private val service: DeviceService) {
    suspend fun isHelpmetDevice(): Boolean {
        return try {
            val response = service.getDeviceInfo()
            response.isSuccessful && response.body()?.serviceName == "helpmet"
        } catch (e: Exception) {
            false
        }
    }
}
