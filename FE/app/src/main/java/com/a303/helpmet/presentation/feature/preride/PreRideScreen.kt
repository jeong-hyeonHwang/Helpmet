package com.a303.helpmet.presentation.feature.preride

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.a303.helpmet.data.service.FakeNavigationService
import com.a303.helpmet.presentation.feature.preride.component.CourseCardPager
import com.a303.helpmet.presentation.feature.preride.component.CourseInfoBubbleView
import com.a303.helpmet.presentation.feature.preride.component.LocationCircleButton
import com.a303.helpmet.presentation.feature.preride.component.NoiseCancelingWarningDialog
import com.a303.helpmet.presentation.feature.preride.component.RouteMapLoadingView
import com.a303.helpmet.util.cache.RouteCache
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import org.koin.androidx.compose.koinViewModel

@Composable
fun PreRideScreen(
    rideTime: Int,
    userPositionViewModel: UserPositionViewModel = koinViewModel(),
    preRideViewModel: PreRideViewModel = koinViewModel(),
    onStartRide: () -> Unit
) {
    // 1) ViewModel 상태 구독
    val routeOptions by preRideViewModel.routeOptions.collectAsState()
    val instructionList by preRideViewModel.instructionList.collectAsState()
    val selectedIndex by preRideViewModel.selectedCourseIndex.collectAsState()
    val routeInfoList by preRideViewModel.routeInfoList.collectAsState()
    val isRouteLoaded by preRideViewModel.isRouteLoaded.collectAsState()

    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var selectedCourseId by remember { mutableStateOf<Int?>(null) }

    // 위치 권한 체크
    var hasLocPerm by remember {
        mutableStateOf(
            ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocPerm = granted }

    LaunchedEffect(Unit) {
        if (!hasLocPerm) permLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    LaunchedEffect(hasLocPerm) {
        if (hasLocPerm) userPositionViewModel.startTracking(context) { _, _ -> }
    }

    val position by userPositionViewModel.position.collectAsState()

    // 이어폰 연결 확인 함수
    fun isHeadsetConnected(): Boolean {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val devices: Array<AudioDeviceInfo> =
            audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)

        return devices.any { device ->
            when (device.type) {
                AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                AudioDeviceInfo.TYPE_WIRED_HEADSET,
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> true

                else -> false
            }
        }
    }

    val onStartRideClicked: (Int) -> Unit = { courseId ->
        val isConnected = isHeadsetConnected()
        if (isConnected) {
            selectedCourseId = courseId
            showDialog = true
        } else {
            onStartRide()
        }
    }

    if (showDialog && selectedCourseId != null) {
        NoiseCancelingWarningDialog(
            onDismiss = { showDialog = false },
            onConfirm = {
                selectedCourseId?.let { courseId ->
                    showDialog = false
                    onStartRideClicked(courseId)
                    onStartRide()
                }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 2) 내 위치 자동 추적 플래그
        var followUser by remember { mutableStateOf(false) }
        val context = LocalContext.current

        // 4) 최초 한 번만 API 호출
        LaunchedEffect(Unit) {
            userPositionViewModel.startTracking(context) { _, _ -> }

            userPositionViewModel.position
                .filter { it.latitude != 0.0 && it.longitude != 0.0 }
                .take(1)
                .collectLatest { pos ->
                    preRideViewModel.loadRoutes(context, pos, rideTime)
                }
        }

        Box(Modifier.fillMaxSize()) {
            // 5) 선택된 경로가 있으면 MapView 백그라운드로 그리기
            if (isRouteLoaded) {
                routeOptions.getOrNull(selectedIndex)?.let { option ->
                    RoutePreviewMapView(
                        routeOption = option,
                        followUser = followUser,
                        onFollowHandled = { followUser = false },
                        userPositionViewModel = userPositionViewModel
                    )
                }
            } else {
                RouteMapLoadingView()
            }

            // 6) 상단 버블 (남은 거리 등)
            CourseInfoBubbleView(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 32.dp)
            )

            // 7) 하단 카드 페이저
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)  // Box 안에서 아래 중앙
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),       // 화면 아래 여유
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                LocationCircleButton(
                    modifier = Modifier
                        .padding(horizontal = 16.dp),
                    onClick = { followUser = true }
                )

                Spacer(Modifier.height(8.dp))

                CourseCardPager(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    courses = routeInfoList,
                    onSelectCourse = { idx -> preRideViewModel.onCourseSelected(idx) },
                    onStartRide = {
                        RouteCache.set(routeOptions[selectedIndex], instructionList[selectedIndex], routeInfoList[selectedIndex])
                        onStartRide()
                    }
                )
            }
        }
    }
}