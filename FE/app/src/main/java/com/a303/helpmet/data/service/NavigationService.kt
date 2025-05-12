package com.a303.helpmet.data.service

import com.a303.helpmet.data.dto.response.NavigationResponseDto
import com.a303.helpmet.data.network.api_services.ApiResult
import retrofit2.http.GET
import retrofit2.http.Query

interface NavigationService {
    @GET("/routes/bike")
    suspend fun getBikeNavigationRouteList(
        @Query("from_lat") fromLat: Double,
        @Query("to_lat") fromLng: Double,
        @Query("to_lat") toLat: Double,
        @Query("to_lon") toLng: Double,
        ): ApiResult<List<NavigationResponseDto>>

}