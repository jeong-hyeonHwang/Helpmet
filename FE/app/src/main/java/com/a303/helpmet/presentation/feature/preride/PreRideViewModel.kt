package com.a303.helpmet.presentation.feature.preride

import android.content.Context
import android.net.Network
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a303.helpmet.data.network.RetrofitProvider
import com.a303.helpmet.di.GetCellularNetworkUseCase
import com.a303.helpmet.domain.mapper.toDomain
import com.a303.helpmet.presentation.mapper.toInstructionList
import com.a303.helpmet.presentation.mapper.toRouteInfo
import com.a303.helpmet.presentation.mapper.toRouteLineOptions
import com.a303.helpmet.presentation.model.InstructionUi
import com.a303.helpmet.presentation.model.RouteInfo
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.route.RouteLineOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PreRideViewModel(
    private val retrofitProvider: RetrofitProvider,
    private val getCellularNetworkUseCase: GetCellularNetworkUseCase
) : ViewModel() {

    private val _selectedCourseIndex = MutableStateFlow(0)
    val selectedCourseIndex: StateFlow<Int> = _selectedCourseIndex

    private val _routeOptions = MutableStateFlow<List<RouteLineOptions>>(emptyList())
    val routeOptions: StateFlow<List<RouteLineOptions>> = _routeOptions

    private val _instructionList = MutableStateFlow<List<List<InstructionUi>>>(emptyList())
    val instructionList: StateFlow<List<List<InstructionUi>>> = _instructionList

    private val _routeInfoList = MutableStateFlow<List<RouteInfo>>(emptyList())
    val routeInfoList: StateFlow<List<RouteInfo>> = _routeInfoList

    private val _isRouteLoaded = MutableStateFlow(false)
    val isRouteLoaded: StateFlow<Boolean> = _isRouteLoaded

    fun onCourseSelected(index: Int) {
        _selectedCourseIndex.value = index
    }

    fun loadRoutes(context: Context, position: LatLng, minute: Int) {
        _isRouteLoaded.value = false

        viewModelScope.launch(Dispatchers.IO) {
            val network: Network? = getCellularNetworkUseCase()
            val service = if (network != null) {
                Log.d("PreRideViewModel", "📡 셀룰러 네트워크로 요청")
                retrofitProvider.createNavigationServiceForNetwork(network)
            } else {
                Log.w("PreRideViewModel", "⚠️ 셀룰러 없음 → 기본 네트워크로 요청")
                retrofitProvider.navigationService
            }

            val response = service.getBikeNavigationRouteList(
                lat = position.latitude,
                lng = position.longitude,
                maxMinutes = minute
            )

            if (response.status == 200) {
                val domainList = response.data.map { it.toDomain() }

                val routeOptions = domainList.map { it.toRouteLineOptions(context) }
                val instructionList = domainList.map { it.toInstructionList() }
                val infoList = domainList.mapIndexed { index, domain -> domain.toRouteInfo(index) }

                withContext(Dispatchers.Main) {
                    _routeOptions.value = routeOptions
                    _instructionList.value = instructionList
                    _routeInfoList.value = infoList
                    _isRouteLoaded.value = true
                }
            } else {
                Log.e("PreRideViewModel", "❌ HTTP 오류: ${response.status}")
            }
        }
    }
}
