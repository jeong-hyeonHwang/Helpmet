package com.a303.helpmet.presentation.feature.voiceinteraction.usecase

import android.content.Context
import android.net.Network
import android.util.Log
import com.a303.helpmet.data.network.RetrofitProvider
import com.a303.helpmet.data.service.NavigationService
import com.a303.helpmet.di.GetCellularNetworkUseCase
import com.a303.helpmet.domain.mapper.toDomain
import com.a303.helpmet.presentation.mapper.toInstructionList
import com.a303.helpmet.presentation.mapper.toRouteLineOptions
import com.a303.helpmet.presentation.model.InstructionUi
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.route.RouteLineOptions

data class NavigateRouteResult(
    val routeOptions: RouteLineOptions,
    val instructionList: List<InstructionUi>,
)

class NavigateToPlaceUseCase(
    private val retrofitProvider: RetrofitProvider,
    private val navigationService: NavigationService
) {
    suspend fun invoke(
        context: Context,
        currentPosition: LatLng,
        placeType: String
    ): NavigateRouteResult? {
        return try {

            val getCellularNetworkUseCase = GetCellularNetworkUseCase(context)
            val network: Network? = getCellularNetworkUseCase()
            val service = if (network != null) {
                Log.d("PreRideViewModel", "📡 셀룰러 네트워크로 요청")
                retrofitProvider.createNavigationServiceForNetwork(network)
            } else {
                Log.w("PreRideViewModel", "⚠️ 셀룰러 없음 → 기본 네트워크로 요청")
                retrofitProvider.navigationService
            }

            val response = service.getBikeNavigationNearBy(
                lat = currentPosition.latitude,
                lng = currentPosition.longitude,
                placeType = placeType
            )
            if(response.status==200){
                val dto = response.data
                val domain = dto.toDomain()
                NavigateRouteResult(
                    routeOptions = domain.toRouteLineOptions(context),
                    instructionList = domain.toInstructionList()
                )
            }else{
                Log.e("NavigationResponseError", "HTTP 상태 코드 오류: ${response.status} in UC")
                null
            }
        } catch (e: Exception) {
            Log.e("Navigate", "경로 요청 실패: ${e.message}")
            null
        }
    }
}