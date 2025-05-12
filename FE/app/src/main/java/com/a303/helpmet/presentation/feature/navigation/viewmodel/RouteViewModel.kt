package com.a303.helpmet.presentation.feature.navigation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a303.helpmet.util.RouteProgressCalculator
import com.a303.helpmet.util.cache.RouteCache
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.route.RouteLine
import com.kakao.vectormap.route.RouteLineOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RouteViewModel : ViewModel() {

    // 전체 경로 데이터
    private val _routeLineOptions = MutableStateFlow<RouteLineOptions?>(null)
    val routeLineOptions: StateFlow<RouteLineOptions?> get() = _routeLineOptions

    // 스냅된 사용자 위치 (진행률에 가장 가까운 점)
    private val _snappedPosition = MutableStateFlow<LatLng?>(null)
    val snappedPosition: StateFlow<LatLng?> get() = _snappedPosition

    // 지도 위에 실제 표시되는 RouteLine 객체
    var routeLine: RouteLine? = null

    // 캐시된 경로 불러오기
    fun loadFromCache() {
        RouteCache.get()?.let {
            _routeLineOptions.value = it
        }
    }

    // 수동으로 경로 지정
    fun setRouteOption(option: RouteLineOptions) {
        _routeLineOptions.value = option
    }

    // 외부에서 직접 진행률 업데이트
    fun updateProgress(progress: Float) {
        Log.d("RouteViewModel", "updateProgress: $progress")
        routeLine?.progressTo(progress, 0)
    }

    // 사용자 위치 입력 → 진행률 및 스냅 위치 계산
    fun setUserPositionAndUpdateProgress(user: LatLng) {
        val route = _routeLineOptions.value?.segments?.flatMap { it.points } ?: return

        val (progress, snapped) = RouteProgressCalculator.calculateProgressAndSnappedPoint(user, route)

        if (RouteProgressCalculator.distance(user, snapped) <= 50.0) {
            routeLine?.progressTo(progress, 0)
            _snappedPosition.value = snapped
        }
    }

    // 테스트용 사용자 이동 시뮬레이션
    fun simulateMovementWithProgressUpdate(
        path: List<LatLng>,
        interval: Long = 1000L,
        onPositionUpdate: (LatLng) -> Unit
    ) {
        viewModelScope.launch {
            val route = _routeLineOptions.value?.segments?.flatMap { it.points } ?: return@launch

            for (point in path) {
                val (progress, snapped) = RouteProgressCalculator.calculateProgressAndSnappedPoint(point, route)
                routeLine?.progressTo(progress, 0)
                _snappedPosition.value = snapped
                onPositionUpdate(point)
                delay(interval)
            }
        }
    }
}
