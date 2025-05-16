package com.a303.helpmet.presentation.feature.navigation.ui

import android.Manifest
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
import com.a303.helpmet.domain.extension.isApproaching
import com.a303.helpmet.domain.model.Action
import com.a303.helpmet.presentation.feature.navigation.usecase.UpdateUserPositionShapesUseCase
import com.a303.helpmet.presentation.feature.navigation.viewmodel.RouteViewModel
import com.a303.helpmet.presentation.feature.preride.UserPositionViewModel
import com.a303.helpmet.presentation.feature.voiceinteraction.VoiceInteractViewModel
import com.kakao.vectormap.*
import com.kakao.vectormap.camera.CameraAnimation
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.route.RouteLineLayer
import com.kakao.vectormap.shape.ShapeLayer
import org.koin.androidx.compose.koinViewModel
import java.lang.Exception

enum class TurnState { IDLE, WAITING_FOR_LEFT_TURN, WAITING_FOR_RIGHT_TURN }

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MapScreen(
    followUser: Boolean,
    onFollowHandled: () -> Unit,
    defaultZoom: Int = 17,
    routeViewModel: RouteViewModel,
    userPositionViewModel: UserPositionViewModel,
    voiceViewModel: VoiceInteractViewModel,
    updateMapShapes: UpdateUserPositionShapesUseCase = UpdateUserPositionShapesUseCase()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val position by userPositionViewModel.position.collectAsState()
    val heading by userPositionViewModel.heading.collectAsState()
    val routeOption by routeViewModel.routeLineOptions.collectAsState()

    val isVoiceReady by voiceViewModel.isVoiceReady.collectAsState()

    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }
    var routeLineLayer by remember { mutableStateOf<RouteLineLayer?>(null) }
    var shapeLayer by remember { mutableStateOf<ShapeLayer?>(null) }

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

    LaunchedEffect(Unit) {
        routeViewModel.loadFromCache()
        if (!hasLocPerm) permLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    LaunchedEffect(hasLocPerm) {
        if (hasLocPerm) userPositionViewModel.startTracking(context) { _, _ -> }
    }

    // TEST: 테스트용 시뮬레이션 위치 이동

    /*
    LaunchedEffect(routeOption) {
        if (routeOption != null) {
            val simulatedPath = listOf(
                LatLng.from(37.5016331, 127.0399224),
                LatLng.from(37.5015831, 127.0399224),
                LatLng.from(37.5015331, 127.0399224),
                LatLng.from(37.5015331, 127.0399824),
                LatLng.from(37.5014500, 127.0399800),
                LatLng.from(37.5012806, 127.0400408),
                LatLng.from(37.5009753, 127.0401896),
                LatLng.from(37.5009408, 127.0402094),
                LatLng.from(37.5008408, 127.0402094),
                LatLng.from(37.5007408, 127.0402094),
                LatLng.from(37.5006408, 127.0402094),
                LatLng.from(37.5005832, 127.0403949),
                LatLng.from(37.5005832, 127.0404949),
                LatLng.from(37.5005832, 127.0405949),
                LatLng.from(37.5005832, 127.0408949),
                LatLng.from(37.5005832, 127.0409949),
                LatLng.from(37.5005832, 127.0411949),
                LatLng.from(37.5008822, 127.0413418),
                LatLng.from(37.5010206, 127.0417805),
                LatLng.from(37.5010206, 127.0417905),
                LatLng.from(37.5010206, 127.0418005),
                LatLng.from(37.5010206, 127.0419005),
                LatLng.from(37.5010206, 127.0420005),
                LatLng.from(37.5010206, 127.0421005),
                LatLng.from(37.5010206, 127.0422005),
                LatLng.from(37.5010206, 127.0423005),
                LatLng.from(37.5010909, 127.0424105),
                LatLng.from(37.5012198, 127.042809)
            )
            routeViewModel.simulateMovementWithProgressUpdate(simulatedPath) {
                userPositionViewModel.setMockPosition(it)
            }
        }
    }
    */

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
                        override fun onMapDestroy() { }

                        override fun onMapError(p0: Exception?) { }
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

    // 4) 사용자 따라가기
    LaunchedEffect(followUser, position) {
        if (!isVoiceReady) return@LaunchedEffect

        if (followUser) {
            kakaoMap?.moveCamera(
                CameraUpdateFactory.newCenterPosition(position, defaultZoom),
                CameraAnimation.from(300)
            )
            onFollowHandled()
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
                    val delta = normalizeAngle(heading - expectedHeading!!)
                    val passed = when (turnState) {
                        TurnState.WAITING_FOR_LEFT_TURN  -> delta < -60f
                        TurnState.WAITING_FOR_RIGHT_TURN -> delta > 60f
                        else -> false
                    }

                    if (passed) {
                        // 회전 감지 완료
                        turnState = TurnState.IDLE
                        expectedHeading = null
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
}

fun normalizeAngle(deg: Float): Float {
    var angle = deg % 360f
    if (angle < -180f) angle += 360f
    if (angle > 180f) angle -= 360f
    return angle
}
