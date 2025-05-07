package com.a303.helpmet.presentation.feature.navigation.ui

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
import com.a303.helpmet.presentation.feature.navigation.viewmodel.MapViewModel
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraAnimation
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.shape.ShapeLayer

@OptIn(ExperimentalComposeUiApi::class)
@SuppressLint("RememberReturnType")
@Composable
fun MapScreen(
    defaultZoom: Int = 17,
    mapViewModel: MapViewModel = viewModel(),
    updateMapShapes: UpdateMapShapesUseCase = UpdateMapShapesUseCase()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 1) 권한 요청
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
    LaunchedEffect(Unit) { if (!hasLocPerm) permLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }

    // 2) 뷰모델 상태 구독
    val position by mapViewModel.position.collectAsState()
    val heading  by mapViewModel.heading.collectAsState()

    // 3) 권한 생기면 트래킹 시작
    LaunchedEffect(hasLocPerm) {
        if (hasLocPerm) mapViewModel.startTracking(context) { _, _ -> }
    }

    // 4) 맵 초기화
    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }
    var shapeLayer by remember { mutableStateOf<ShapeLayer?>(null) }
    val mapView = remember {
        MapView(context).apply {
            lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) = resume()
                override fun onPause(owner: LifecycleOwner)  = pause()
                override fun onDestroy(owner: LifecycleOwner) = finish()
            })
            start(
                object : MapLifeCycleCallback() {
                    override fun onMapDestroy(){}
                    override fun onMapError(e: Exception){}
                },
                object : KakaoMapReadyCallback() {
                    override fun onMapReady(map: KakaoMap) {
                        kakaoMap = map
                        shapeLayer = map.shapeManager?.getLayer()
                        // 초기 도형
                        updateMapShapes(shapeLayer!!, position, 0f)
                    }
                    override fun getPosition(): LatLng = position
                    override fun getZoomLevel(): Int = defaultZoom
                }
            )
        }
    }

    // 5) 위치/헤딩 업데이트 시 UseCase 호출 및 카메라 이동
    LaunchedEffect(kakaoMap) {
        snapshotFlow { position to heading }
            .collect { (pos, hd) ->
                shapeLayer?.let { updateMapShapes(it, pos, hd) }
                // 사용자가 상호작용 중이 아니라면 카메라 자동 이동
                if (!mapViewModel.isUserInteracting.value) {
                    kakaoMap?.moveCamera(
                        CameraUpdateFactory.newCenterPosition(pos, defaultZoom),
                        CameraAnimation.from(300)
                    )
                }
            }
    }

    // 6) MapView 표시 및 터치 이벤트
    AndroidView(
        factory = { mapView },
        modifier = Modifier
            .fillMaxSize()
            .pointerInteropFilter { motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                    // 한 번이라도 터치가 감지되면 true (이후엔 유지)
                    mapViewModel.setUserInteracting(true)
                }
                mapView.dispatchTouchEvent(motionEvent)
                true
            }
    )
}
