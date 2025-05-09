package com.a303.helpmet.presentation.feature.preride

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
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
import com.kakao.vectormap.LatLngBounds
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
    followUser: Boolean,
    onFollowHandled: () -> Unit,
    defaultZoom: Int = 17,
    updateMapShapes: UpdateMapShapesUseCase = UpdateMapShapesUseCase(),
    routePreviewViewModel: RoutePreviewViewModel = viewModel()
) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 1) 현재 위치·헤딩 관찰
    val position by routePreviewViewModel.position.collectAsState()
    val heading  by routePreviewViewModel.heading.collectAsState()

    // 2) 퍼미션 체크 및 요청
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
        if (!hasLocPerm) permLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    LaunchedEffect(hasLocPerm) {
        if (hasLocPerm) routePreviewViewModel.startTracking(context) { _, _ -> }
    }

    // 3) MapView·KakaoMap·레이어 참조
    var mapViewRef     by remember { mutableStateOf<MapView?>(null) }
    var kakaoMap       by remember { mutableStateOf<KakaoMap?>(null) }
    var routeLineLayer by remember { mutableStateOf<RouteLineLayer?>(null) }
    var shapeLayer     by remember { mutableStateOf<ShapeLayer?>(null) }

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
                            kakaoMap       = map
                            shapeLayer     = map.shapeManager?.getLayer()
                            routeLineLayer = map.routeLineManager?.getLayer()

                            // 초기 경로 그리기 + 전체 fit
                            routeOption?.let { opts ->
                                routeLineLayer!!.addRouteLine(opts)
                                val allPoints = opts.segments.flatMap { it.points }
                                if (allPoints.isNotEmpty()) {
                                    val lats = allPoints.map { it.latitude }
                                    val lons = allPoints.map { it.longitude }
                                    val bounds = LatLngBounds(
                                        LatLng.from(lats.minOrNull()!!, lons.minOrNull()!!),
                                        LatLng.from(lats.maxOrNull()!!, lons.maxOrNull()!!)
                                    )
                                    val update = CameraUpdateFactory.fitMapPoints(bounds, 100)
                                    map.moveCamera(update, CameraAnimation.from(500))
                                }
                            }
                        }
                        override fun getPosition(): LatLng = LatLng.from(0.0, 0.0)
                        override fun getZoomLevel(): Int   = defaultZoom
                    }
                )
            }
        },
        update = { _ ->
            // 옵션 변경 시 다시 그리기
            routeLineLayer?.apply {
                removeAll()
                routeOption?.let { addRouteLine(it) }
            }

            routeOption?.let { opts ->
                val allPoints = opts.segments.flatMap { it.points }
                if (allPoints.isNotEmpty()) {
                    val lats = allPoints.map { it.latitude }
                    val lons = allPoints.map { it.longitude }
                    val south = lats.minOrNull()!!
                    val north = lats.maxOrNull()!!
                    val west  = lons.minOrNull()!!
                    val east  = lons.maxOrNull()!!

                    val bounds = LatLngBounds(
                        LatLng.from(south, west),
                        LatLng.from(north, east)
                    )
                    val paddingPx = 100
                    val update = CameraUpdateFactory.fitMapPoints(bounds, paddingPx)

                    kakaoMap?.moveCamera(update, CameraAnimation.from(500))
                }
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .pointerInteropFilter { ev ->
                if (ev.action == MotionEvent.ACTION_DOWN) {
                    routePreviewViewModel.setUserInteracting(true)
                }
                mapViewRef?.dispatchTouchEvent(ev)
                true
            }
    )

    // 4) followUser 플래그 켜지면 현재 위치로 카메라 이동
    LaunchedEffect(followUser, position) {
        if (followUser) {
            kakaoMap?.moveCamera(
                CameraUpdateFactory.newCenterPosition(position, defaultZoom),
                CameraAnimation.from(300)
            )
            onFollowHandled()
        }
    }

    // 5) 위치·헤딩 변경 때마다 트레이스 도형 업데이트
    LaunchedEffect(shapeLayer) {
        snapshotFlow { position to heading }
            .collect { (pos, hd) ->
                shapeLayer?.let { updateMapShapes(it, pos, hd) }
            }
    }
}
