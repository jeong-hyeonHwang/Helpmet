package com.a303.helpmet.presentation.feature.navigation.ui

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.view.MotionEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.a303.helpmet.domain.extension.isApproaching
import com.a303.helpmet.domain.extension.isNear
import com.a303.helpmet.domain.model.Action
import com.a303.helpmet.domain.model.DirectionState
import com.a303.helpmet.presentation.feature.navigation.test.SimulatedPathTest
import com.a303.helpmet.presentation.feature.navigation.usecase.AdjustCameraUseCase
import com.a303.helpmet.presentation.feature.navigation.usecase.UpdateUserPositionShapesUseCase
import com.a303.helpmet.presentation.feature.navigation.viewmodel.RouteViewModel
import com.a303.helpmet.presentation.feature.preride.UserPositionViewModel
import com.a303.helpmet.presentation.feature.preride.component.RouteMapLoadingView
import com.a303.helpmet.presentation.feature.voiceinteraction.VoiceInteractViewModel
import com.a303.helpmet.presentation.model.LatLngUi
import com.a303.helpmet.presentation.model.TurnState
import com.a303.helpmet.util.GeoUtils
import com.kakao.vectormap.*
import com.kakao.vectormap.camera.CameraAnimation
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.route.RouteLineLayer
import com.kakao.vectormap.shape.ShapeLayer
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import java.lang.Exception

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MapScreen(
    followUser: Boolean,
    onFollowHandled: () -> Unit,
    defaultZoom: Int = 17,
    routeViewModel: RouteViewModel,
    userPositionViewModel: UserPositionViewModel,
    voiceViewModel: VoiceInteractViewModel,
    updateMapShapes: UpdateUserPositionShapesUseCase = UpdateUserPositionShapesUseCase(),
    adjustCameraUseCase: AdjustCameraUseCase = AdjustCameraUseCase()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val position by userPositionViewModel.position.collectAsState()
    val heading by userPositionViewModel.heading.collectAsState()
    val routeOption by routeViewModel.routeLineOptions.collectAsState()
    val destination by routeViewModel.destination.collectAsState()

    val isVoiceReady by voiceViewModel.isVoiceReady.collectAsState()

    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }
    var routeLineLayer by remember { mutableStateOf<RouteLineLayer?>(null) }
    var shapeLayer by remember { mutableStateOf<ShapeLayer?>(null) }
    var hasEnded by remember { mutableStateOf(false) }

    // 새로운 경로를 탐색하는 중을 나타내는 상태 변수
    val isLoading by voiceViewModel.isLoading.collectAsState()

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

    var turnState by remember { mutableStateOf(TurnState.IDLE) }
    var expectedHeading by remember { mutableStateOf<Float?>(null) }
    var listenerRegistered by remember { mutableStateOf(false) }

    fun handleGuideEnd(){
        routeLineLayer?.removeAll()
        routeViewModel.routeLine = null
        turnState = TurnState.IDLE
        expectedHeading = null
    }

    // TEST: 테스트용 시뮬레이션 위치 이동

//    LaunchedEffect(routeOption) {
//        if (routeOption != null) {
//            val simulatedPath = SimulatedPathTest.simulatedPath
//            routeViewModel.simulateMovementWithProgressUpdate(simulatedPath) {
//                userPositionViewModel.setMockPosition(it)
//            }
//        }
//    }

    LaunchedEffect(Unit) {
        routeViewModel.loadFromCache()
        if (!hasLocPerm) permLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)

        if (!listenerRegistered) {
            voiceViewModel.setOnRouteUpdateListener { result ->
                routeViewModel.setRouteOption(result.routeOptions)
                routeViewModel.setInstructionList(result.instructionList)
                hasEnded = false
            }
            listenerRegistered = true
        }
    }

    LaunchedEffect(hasLocPerm) {
        if (hasLocPerm) {
            userPositionViewModel.startTracking(context) { pos, _ ->
                // 위치가 바뀔 때마다 호출됨
                voiceViewModel.updatePosition(pos)
            }
        }
    }

    LaunchedEffect(routeOption) {
        hasEnded = false
        val layer = routeLineLayer
        val map = kakaoMap
        val option = routeOption

        if (option != null && layer != null && map != null) {
            layer.removeAll()

            val routeLine = layer.addRouteLine(option)
            routeViewModel.routeLine = routeLine

            val allPoints = option.segments.flatMap { it.points }
            val lats = allPoints.map { it.latitude }
            val lons = allPoints.map { it.longitude }

            val bounds = LatLngBounds(
                LatLng.from(lats.minOrNull()!!, lons.minOrNull()!!),
                LatLng.from(lats.maxOrNull()!!, lons.maxOrNull()!!)
            )

            map.moveCamera(CameraUpdateFactory.fitMapPoints(bounds, 100), CameraAnimation.from(500))
        } else {
            Log.w("NavigationUpdateError", "❗ 조건 불충분: option=$option, layer=$layer, map=$map")
        }
    }

    // TEST: 테스트용 시뮬레이션 위치 이동
    /*
    LaunchedEffect(routeOption) {
        if (routeOption != null) {
            routeViewModel.simulateMovementWithProgressUpdate(SimulatedPathTest.simulatedPath) {
                userPositionViewModel.setMockPosition(it)
            }
        }
    }
    */
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                        override fun onResume(owner: LifecycleOwner) = resume()
                        override fun onPause(owner: LifecycleOwner) = pause()
                        override fun onDestroy(owner: LifecycleOwner) = finish()
                    })
                    mapViewRef = this

                    start(
                        object : MapLifeCycleCallback() {
                            override fun onMapDestroy() {}

                            override fun onMapError(p0: Exception?) {}
                        },
                        object : KakaoMapReadyCallback() {
                            override fun onMapReady(map: KakaoMap) {
                                kakaoMap = map
                                shapeLayer = map.shapeManager?.getLayer()
                                routeLineLayer = map.routeLineManager?.getLayer()

                                routeOption?.let { opts ->
                                    val routeLine = routeLineLayer!!.addRouteLine(opts)
                                    routeViewModel.routeLine = routeLine

                                    val allPoints = opts.segments.flatMap { it.points }
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

                            override fun getZoomLevel(): Int = defaultZoom
                            override fun getPosition(): LatLng = LatLng.from(0.0, 0.0)
                        }
                    )
                }
            },
            update = {
                routeLineLayer?.apply {
                    removeAll()
                    routeOption?.let { addRouteLine(it) }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .pointerInteropFilter {
                    if (it.action == MotionEvent.ACTION_DOWN) {
                        userPositionViewModel.setUserInteracting(true)
                    }
                    mapViewRef?.dispatchTouchEvent(it)
                    true
                }
        )

        if(isLoading){
            RouteMapLoadingView(
                message = "경로를 탐색중입니다.",
                isBackgroundBlack = false
            )
        }
    }
    // 4) 사용자 따라가기
    LaunchedEffect(followUser, position, heading) {
        if (!isVoiceReady) return@LaunchedEffect
        if (position.latitude == 0.0 && position.longitude == 0.0) return@LaunchedEffect
        if (followUser && kakaoMap != null) {

            val firstSegment = routeOption?.segments?.firstOrNull()
            val from = firstSegment?.points?.getOrNull(0)
            val to = firstSegment?.points?.getOrNull(1)

            if (from != null && to != null) {
                adjustCameraUseCase(
                    kakaoMap = kakaoMap!!,
                    currentPosition = position,
                    from = from,
                    to = LatLngUi(to.latitude, to.longitude),
                    zoom = defaultZoom
                )

                onFollowHandled()
            }
        }

        routeViewModel.instructionList.value
            ?.firstOrNull { !it.isSpoken }
            ?.let { instruction ->

                val isTurningInstruction = instruction.action in listOf(Action.LEFT, Action.RIGHT)

                if (turnState == TurnState.IDLE) {
                    if (position.isApproaching(instruction.action, instruction.location)) {
                        if (isTurningInstruction) {
                            // 회전 안내 → 안내는 하지만 다음 안내는 보류
                            voiceViewModel.speak(instruction.message)
                            routeViewModel.markInstructionAsSpoken(instruction)

                            // 회전 감지를 위해 초기 heading 저장
                            expectedHeading = heading

                            if (instruction.action == Action.LEFT) {
                                turnState = TurnState.WAITING_FOR_LEFT_TURN
                            } else {
                                turnState = TurnState.WAITING_FOR_RIGHT_TURN
                            }
                        } else {
                            // 일반 안내는 바로
                            voiceViewModel.speak(instruction.message)
                            routeViewModel.markInstructionAsSpoken(instruction)
                        }
                    }
                } else if ((turnState == TurnState.WAITING_FOR_RIGHT_TURN ||
                    turnState == TurnState.WAITING_FOR_LEFT_TURN)
                    && expectedHeading != null) {
                    val delta = GeoUtils.normalizeAngle(heading.toDouble() - expectedHeading!!)
                    val passed = when (turnState) {
                        TurnState.WAITING_FOR_LEFT_TURN  -> delta < -45f
                        TurnState.WAITING_FOR_RIGHT_TURN -> delta > 45f
                        else -> false
                    }

                    Log.d("ANGLE_CHECK", "CURRENT ANGLE: ${delta}")
                    if (passed) {
                        // 회전 감지 완료
                        turnState = TurnState.IDLE
                        expectedHeading = null

                        // 다음 안내 지점으로 방향 벡터 구해 회전
                        val nextInstruction = routeViewModel.instructionList.value
                            ?.firstOrNull { !it.isSpoken }

                        if (nextInstruction != null) {
                            val to = LatLngUi(nextInstruction.location.latitude, nextInstruction.location.longitude)
                            adjustCameraUseCase(
                                kakaoMap = kakaoMap!!,
                                currentPosition = position,
                                from = position,
                                to = to,
                                zoom = defaultZoom
                            )
                            voiceViewModel.turnOnOffSignal(DirectionState.None)
                        }
                    }
                }

                destination?.location?.let{ loc ->
                    if(!hasEnded && position.isNear(loc, threshold = 5.0)){
                        hasEnded = true
                        voiceViewModel.setEndGuideContext()
                        handleGuideEnd()
                    }
                }
            }
    }

    // 5) 위치 변화 → 도형 업데이트
    LaunchedEffect(shapeLayer) {
        snapshotFlow { position to heading }
            .collect { (pos, hd) ->
                shapeLayer?.let { updateMapShapes(it, pos, hd) }
                routeViewModel.setUserPositionAndUpdateProgress(pos)
            }
    }

    LaunchedEffect(true) {
        voiceViewModel.onGuideEnd.collect {
            if (!hasEnded) {
                hasEnded = true
                handleGuideEnd()
            }
        }
    }


}