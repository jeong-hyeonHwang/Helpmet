package com.a303.helpmet.data.repository

import com.a303.helpmet.data.dto.response.DeviceInfo
import com.a303.helpmet.data.service.DeviceService

class DeviceRepository(private val service: DeviceService) {
    suspend fun isHelpmetDevice(): DeviceInfo {
        return try {
            val response = service.getDeviceInfo()
            if (response.isSuccessful) {
                val body = response.body()
                val isValidPi = body?.serviceName.equals("helpmet", ignoreCase = true)
                val isAccess = body?.isAccess == true
                DeviceInfo(isValidPi = isValidPi, isAccess = isAccess)
            } else {
                DeviceInfo(isValidPi = false, isAccess = false)
            }
        } catch (e: Exception) {
            DeviceInfo(isValidPi = false, isAccess = false)
        }
    }
}