package com.a303.helpmet.presentation.feature.preride

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a303.helpmet.data.dto.response.NavigationResponseDto
import com.a303.helpmet.data.service.NavigationService
import com.a303.helpmet.domain.mapper.toDomain
import com.a303.helpmet.presentation.mapper.toRouteLineOptions
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

    fun onCourseSelected(index: Int) {
        _selectedCourseIndex.value = index
    }

    fun loadRoutes(context: Context) {
        viewModelScope.launch {
            val resp: Response<List<NavigationResponseDto>> =
                service.getBikeNavigationRouteList()

            if (resp.isSuccessful) {
                val dtoList    = resp.body().orEmpty()
                val domainList = dtoList.map { it.toDomain() }
                val options    = domainList.map { it.toRouteLineOptions(context) }
                _routeOptions.value = options
            } else {
                _routeOptions.value = emptyList()
            }

            Log.d("????????", "loadRoutes: " + routeOptions.value.size)
        }
    }
}