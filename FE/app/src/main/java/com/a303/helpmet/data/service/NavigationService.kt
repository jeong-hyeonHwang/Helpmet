package com.a303.helpmet.data.service

import com.a303.helpmet.data.dto.response.NavigationResponseDto
import com.a303.helpmet.data.network.api_services.BaseResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NavigationService {
    @GET("/route/bike")
    suspend fun getBikeNavigationRouteList(
        @Query("lat") lat: Double,
        @Query("lon") lng: Double,
        @Query("max_minutes") maxMinutes: Int
        ): BaseResponse<List<NavigationResponseDto>>
}
