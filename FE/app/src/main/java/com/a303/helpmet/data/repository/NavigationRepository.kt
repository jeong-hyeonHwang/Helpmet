package com.a303.helpmet.data.repository

import com.a303.helpmet.data.dto.response.DeviceInfoResponse
import com.a303.helpmet.data.dto.response.NavigationResponseDto
import com.a303.helpmet.data.service.DeviceService
import com.a303.helpmet.data.service.NavigationService
import retrofit2.Response

class NavigationRepository(private val service: NavigationService) {
    suspend fun getBikeNavigationRouteList(): Response<List<NavigationResponseDto>> {
        return service.getBikeNavigationRouteList()
    }
}