package com.a303.helpmet.data.service

import com.a303.helpmet.data.dto.response.NavigationResponseDto
import retrofit2.Response
import retrofit2.http.GET

interface NavigationService {
    @GET("/routes/bike")
    suspend fun getBikeNavigationRouteList(): Response<List<NavigationResponseDto>>

}