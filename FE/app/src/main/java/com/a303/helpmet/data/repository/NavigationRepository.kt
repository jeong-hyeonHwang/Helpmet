package com.a303.helpmet.data.repository

import com.a303.helpmet.data.dto.response.NavigationResponseDto
import com.a303.helpmet.data.network.api_services.BaseResponse
import com.a303.helpmet.data.service.NavigationService
import retrofit2.Response

class NavigationRepository(private val service: NavigationService) {
    suspend fun getBikeNavigationRouteList(
        lat: Double,
        lng: Double,
        maxMinutes: Int
    ): BaseResponse<List<NavigationResponseDto>>{
        return service.getBikeNavigationRouteList(
            lat = lat,
            lng = lng,
            maxMinutes = maxMinutes
        )
    }

}