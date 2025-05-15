package com.a303.helpmet.presentation.feature.preride

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import android.view.MotionEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.a303.helpmet.presentation.feature.navigation.usecase.UpdateUserPositionShapesUseCase
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.LatLngBounds
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraAnimation
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.route.RouteLineLayer
import com.kakao.vectormap.route.RouteLineOptions
import com.kakao.vectormap.shape.ShapeLayer
import java.lang.Exception

@OptIn(ExperimentalComposeUiApi::class)
@SuppressLint("RememberReturnType")
@Composable
fun RoutePreviewMapView(
    routeOption: RouteLineOptions?,
    followUser: Boolean,
    onFollowHandled: () -> Unit,
    defaultZoom: Int = 17,
    updateMapShapes: UpdateUserPositionShapesUseCase = UpdateUserPositionShapesUseCase(),
    userPositionViewModel: UserPositionViewModel
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val position by userPositionViewModel.position.collectAsState()
    val heading by userPositionViewModel.heading.collectAsState()

    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }
    var routeLineLayer by remember { mutableStateOf<RouteLineLayer?>(null) }
    var shapeLayer by remember { mutableStateOf<ShapeLayer?>(null) }

    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                mapViewRef = this

                start(
                    object : MapLifeCycleCallback() {
                        override fun onMapDestroy() {}
                        override fun onMapError(p0: Exception?) {
                            Log.e("MapError", "Map Error: $p0")
                        }
                    },
                    object : KakaoMapReadyCallback() {
                        override fun onMapReady(map: KakaoMap) {
                            kakaoMap = map
                            shapeLayer = map.shapeManager?.getLayer()
                            routeLineLayer = map.routeLineManager?.getLayer()

                            // 안전한 시점에서 resume 호출
                            this@apply.resume()
                        }

                        override fun getPosition(): LatLng = LatLng.from(0.0, 0.0)
                        override fun getZoomLevel(): Int = defaultZoom
                    }
                )
            }
                  },
        modifier = Modifier
            .fillMaxSize()
            .pointerInteropFilter { ev ->
                if (ev.action == MotionEvent.ACTION_DOWN) {
                    userPositionViewModel.setUserInteracting(true)
                }
                mapViewRef?.dispatchTouchEvent(ev)
                true
            }
    )

    // routeOption 변경 시 지도에 반응하여 경로 다시 그리기
    LaunchedEffect(routeOption, routeLineLayer) {
        if (routeLineLayer != null && routeOption != null) {
            routeLineLayer!!.removeAll()
            routeLineLayer!!.addRouteLine(routeOption)

            val allPoints = routeOption.segments.flatMap { it.points }
            if (allPoints.isNotEmpty()) {
                val lats = allPoints.map { it.latitude }
                val lons = allPoints.map { it.longitude }
                val minLat = lats.minOrNull()!!
                val maxLat = lats.maxOrNull()!!
                val minLon = lons.minOrNull()!!
                val maxLon = lons.maxOrNull()!!

                val adjustedMinLat = minLat - 0.04

                val bounds = LatLngBounds(
                    LatLng.from(adjustedMinLat, minLon),
                    LatLng.from(maxLat, maxLon)
                )
                val update = CameraUpdateFactory.fitMapPoints(bounds, 200)
                kakaoMap?.moveCamera(update, CameraAnimation.from(500))
            }
        }
    }

    // 사용자가 followUser를 눌렀을 때 카메라 이동
    LaunchedEffect(followUser, position) {
        if (followUser) {
            kakaoMap?.moveCamera(
                CameraUpdateFactory.newCenterPosition(position, defaultZoom),
                CameraAnimation.from(300)
            )
            onFollowHandled()
        }
    }

    // 위치/헤딩 변경 시 트레이스 도형 그리기
    LaunchedEffect(shapeLayer) {
        snapshotFlow { position to heading }
            .collect { (pos, hd) ->
                shapeLayer?.let { updateMapShapes(it, pos, hd) }
            }
    }

    DisposableEffect(Unit) {
        onDispose {
            mapViewRef?.pause()
            mapViewRef?.finish()
        }
    }
}

