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
import com.a303.helpmet.presentation.feature.navigation.usecase.UpdateMapShapesUseCase
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraAnimation
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.route.RouteLineLayer
import com.kakao.vectormap.route.RouteLineOptions
import com.kakao.vectormap.shape.ShapeLayer

@OptIn(ExperimentalComposeUiApi::class)
@SuppressLint("RememberReturnType")
@Composable
fun RoutePreviewMapView(
    routeOption: RouteLineOptions?,
    routePreviewViewModel: RoutePreviewViewModel = viewModel(),
    defaultZoom: Int = 17,
    updateMapShapes: UpdateMapShapesUseCase = UpdateMapShapesUseCase()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 1) 위치·헤딩 Flows 구독
    val position by routePreviewViewModel.position.collectAsState()
    val heading  by routePreviewViewModel.heading.collectAsState()

    // 2) 권한 요청
    var hasLocPerm by remember {
        mutableStateOf(
            ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasLocPerm = granted }

    LaunchedEffect(Unit) {
        Log.d("PreRideScreen", "RoutePreviewMapView: ${routeOption.toString()}")
        if (!hasLocPerm) permLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    // 3) 권한 생기면 트래킹 시작
    LaunchedEffect(hasLocPerm) {
        if (hasLocPerm) {
            routePreviewViewModel.startTracking(context) { _, _ -> /* no-op */ }
        }
    }

    // 4) MapView 참조용
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var kakaoMap   by remember { mutableStateOf<KakaoMap?>(null) }
    var routeLineLayer by remember { mutableStateOf<RouteLineLayer?>(null) }
    var shapeLayer by remember { mutableStateOf<ShapeLayer?>(null) }

    // 5) MapView + RouteLine 그리기
    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                    override fun onResume(owner: LifecycleOwner) = resume()
                    override fun onPause(owner: LifecycleOwner)  = pause()
                    override fun onDestroy(owner: LifecycleOwner) = finish()
                })
                mapViewRef = this

                start(
                    object : MapLifeCycleCallback() {
                        override fun onMapDestroy() {}
                        override fun onMapError(e: Exception) {}
                    },
                    object : KakaoMapReadyCallback() {
                        override fun onMapReady(map: KakaoMap) {
                            kakaoMap = map
                            // ShapeLayer for location traces
                            shapeLayer = map.shapeManager?.getLayer()
                            // RouteLineLayer for routeOption
                            routeLineLayer = map.routeLineManager?.getLayer()

                            // 초기 경로 그리기
                            routeOption?.let { opts ->
                                routeLineLayer!!.addRouteLine(opts)
                                // 카메라 출발점 이동
                                opts.segments
                                    .firstOrNull()
                                    ?.points
                                    ?.firstOrNull()
                                    ?.let { start ->
                                        map.moveCamera(
                                            CameraUpdateFactory.newCenterPosition(start, defaultZoom),
                                            CameraAnimation.from(500)
                                        )
                                    }
                            }
                        }
                        override fun getPosition(): LatLng = LatLng.from(0.0, 0.0)
                        override fun getZoomLevel(): Int = defaultZoom
                    }
                )
            }
        },
        update = { _ ->
            // routeOption 이 바뀔 때마다 다시 그리기
            routeLineLayer?.apply {
                removeAll()
                routeOption?.let { addRouteLine(it) }
            }

            val firstPoint = routeOption
                ?.segments?.firstOrNull()
                ?.points?.firstOrNull()
            firstPoint?.let {
                kakaoMap?.moveCamera(
                    CameraUpdateFactory.newCenterPosition(it, defaultZoom),
                    CameraAnimation.from(300)
                )
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .pointerInteropFilter { ev ->
                // 사용자 터치 감지 → 자동 카메라 추적 중지
                if (ev.action == MotionEvent.ACTION_DOWN) {
                    routePreviewViewModel.setUserInteracting(true)
                }
                mapViewRef?.dispatchTouchEvent(ev)
                true
            }
    )

    // 6) 위치/헤딩 변화시 도형 갱신 & 자동 카메라 추적
    LaunchedEffect(shapeLayer, kakaoMap) {
        snapshotFlow { position to heading }
            .collect { (pos, hd) ->
                shapeLayer?.let { updateMapShapes(it, pos, hd) }
                if (!routePreviewViewModel.isUserInteracting.value) {
                    kakaoMap?.moveCamera(
                        CameraUpdateFactory.newCenterPosition(pos, defaultZoom),
                        CameraAnimation.from(300)
                    )
                }
            }
    }
}
