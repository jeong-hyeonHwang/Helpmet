package com.a303.helpmet.presentation.feature.preride

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a303.helpmet.data.dto.response.NavigationResponseDto
import com.a303.helpmet.data.network.api_services.ApiResult
import com.a303.helpmet.data.service.NavigationService
import com.a303.helpmet.domain.mapper.toDomain
import com.a303.helpmet.presentation.mapper.toRouteInfo
import com.a303.helpmet.presentation.mapper.toRouteLineOptions
import com.a303.helpmet.presentation.model.RouteInfo
import com.kakao.vectormap.route.RouteLineOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import kotlin.math.log

class PreRideViewModel (
    private val service: NavigationService
): ViewModel() {
    private val _selectedCourseIndex = MutableStateFlow(0)
    val selectedCourseIndex: StateFlow<Int> = _selectedCourseIndex

    private val _routeOptions = MutableStateFlow<List<RouteLineOptions>>(emptyList())
    val routeOptions: StateFlow<List<RouteLineOptions>> = _routeOptions

    private val _routeInfoList = MutableStateFlow<List<RouteInfo>>(emptyList())
    val routeInfoList: StateFlow<List<RouteInfo>> = _routeInfoList;

    fun onCourseSelected(index: Int) {
        _selectedCourseIndex.value = index
    }

    fun loadRoutes(context: Context) {
        viewModelScope.launch {
            when (val result = service.getBikeNavigationRouteList()) {
                is ApiResult.Success -> {
                    val dtoList    = result.data
                    val domainList = dtoList.map { it.toDomain() }
                    val options    = domainList.map { it.toRouteLineOptions(context) }
                    val infoList   = domainList.mapIndexed { index, domain ->
                        domain.toRouteInfo(index)
                    }

                    _routeOptions.value = options
                    _routeInfoList.value = infoList
                }

                is ApiResult.Error -> {
                    _routeOptions.value = emptyList()
                    _routeInfoList.value = emptyList()
                }
            }
        }
    }
}