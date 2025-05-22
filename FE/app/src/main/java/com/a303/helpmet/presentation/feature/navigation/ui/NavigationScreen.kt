package com.a303.helpmet.presentation.feature.navigation.ui

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.a303.helpmet.BuildConfig
import com.a303.helpmet.R
import com.a303.helpmet.domain.model.DirectionState
import com.a303.helpmet.presentation.feature.navigation.viewmodel.NavigationViewModel
import org.koin.androidx.compose.koinViewModel
import com.a303.helpmet.presentation.feature.navigation.component.StreamingNoticeView
import com.a303.helpmet.presentation.feature.navigation.component.StreamingView
import com.a303.helpmet.presentation.feature.navigation.component.getCellularNetwork
import com.a303.helpmet.presentation.feature.navigation.component.getWifiNetwork
import com.a303.helpmet.presentation.feature.navigation.viewmodel.DetectionViewModel
import com.a303.helpmet.presentation.feature.navigation.viewmodel.RouteViewModel
import com.a303.helpmet.presentation.feature.preride.UserPositionViewModel
import com.a303.helpmet.presentation.feature.voiceinteraction.VoiceInteractViewModel
import com.a303.helpmet.ui.theme.HelpmetTheme
import com.a303.helpmet.util.cache.RouteCache
import com.a303.helpmet.util.handler.getGatewayIp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

@Composable
fun NavigationScreen(
    onFinish: () -> Unit,
    navigationViewModel: NavigationViewModel = koinViewModel(),
    userPositionViewModel: UserPositionViewModel = viewModel(),
    routeViewModel: RouteViewModel,
    detectionViewModel: DetectionViewModel = koinViewModel(),
    navController: NavController
) {
    val context = LocalContext.current
    val gatewayIp = getGatewayIp(context)
    val webPageUrl = "http://$gatewayIp:${BuildConfig.SOCKET_PORT}"
    val voiceViewModel : VoiceInteractViewModel = koinViewModel()

    // 권한 상태 관리
    var hasRecordPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // 권한 요청 런처
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasRecordPermission = isGranted
        if(!isGranted){
            voiceViewModel.notifyPermissionMissing()
        }
    }

    DisposableEffect(Unit) {
        val appContext = context.applicationContext
        val filter = IntentFilter("com.a303.helpmet.RETURN_ALERT_DETECTED")
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d("NotificationReceiver", "NavigationScreen에서 수신")
                voiceViewModel.onReturnAlertReceived()
            }
        }

        ContextCompat.registerReceiver(
            appContext,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        onDispose {
            try {
                appContext.unregisterReceiver(receiver)
            } catch (e: IllegalArgumentException) {
                Log.w("Receiver", "이미 해제된 리시버입니다: ${e.message}")
            }
        }
    }


    LaunchedEffect(Unit) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifiNetwork = getWifiNetwork(context)
        val cellularNetwork = getCellularNetwork(context)

        if (wifiNetwork != null) {
            cm.bindProcessToNetwork(wifiNetwork)
            Log.d("WebRTC", "✅ Wi-Fi 네트워크로 바인딩 완료")
        }

        // ✅ WebRTC 연결 이후 → 셀룰러로 복원
        delay(8000L)

        if (cellularNetwork != null) {
            cm.bindProcessToNetwork(cellularNetwork)
            Log.d("WebRTC", "🔁 셀룰러 네트워크로 복원 완료")
            delay(1000L)
        } else {
            cm.bindProcessToNetwork(null)
            Log.w("WebRTC", "⚠️ 셀룰러 네트워크를 찾지 못해 기본으로 복원")
        }

        // 셀룰러 복원 후, 권한이 있다면 STT 시작
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            voiceViewModel.startListening()
        } else {
            Log.w("VoiceHandler", "❌ RECORD_AUDIO 권한이 없어 STT 시작 안함")
        }
    }

    // 최초 실행 시 권한 요청
    LaunchedEffect(Unit) {
        if (!hasRecordPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

    }

    LaunchedEffect(webPageUrl) {
        navigationViewModel.connectToSocket(webPageUrl,context, detectionViewModel.onFrameReceived())
        detectionViewModel.startDetectionLoop()
    }

    DisposableEffect(Unit) {
        onDispose {
            navigationViewModel.disconnectFromSocket()
            RouteCache.clear()
            voiceViewModel.stopListening()
        }
    }

    BackHandler {
        voiceViewModel.stopListening() // 강제로 STT/TTS 종료
        navController.popBackStack()
    }


    // 2) 내 위치 자동 추적 플래그
    var followUser by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        StreamingView(
            detectionViewModel = detectionViewModel,
            webPageUrl = webPageUrl
        )

        // 카메라 뷰 토글 버튼
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(HelpmetTheme.colors.white1)
                .padding(vertical = 8.dp)
                .clickable { navigationViewModel.toggleStreaming() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(HelpmetTheme.colors.gray1)
            )
        }

    // 지도
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
    ) {
        MapScreen(
            followUser = followUser,
            onFollowHandled = { followUser = false },
            routeViewModel = routeViewModel,
            userPositionViewModel = userPositionViewModel,
            voiceViewModel = voiceViewModel
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            DirectionIcons(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            // 안내 멘트
            StreamingNoticeView(
                modifier = Modifier,
                onFinish,
                routeViewModel = routeViewModel
            )
        }
    }
}
}

@Composable
fun DirectionIcons(
    modifier: Modifier = Modifier,
    viewModel: NavigationViewModel = koinViewModel()
) {
    val direction: DirectionState by viewModel.directionState.collectAsState()

    val infiniteTransition = rememberInfiniteTransition()

    val blinkingAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_util_left_direction_arrow),
            contentDescription = "좌회전",
            tint = Color.Unspecified,
            modifier = Modifier
                .size(80.dp)
                .alpha(if (direction == DirectionState.Left) blinkingAlpha else 0f)
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_util_right_direction_arrow),
            contentDescription = "우회전",
            tint = Color.Unspecified,
            modifier = Modifier
                .size(80.dp)
                .alpha(if (direction == DirectionState.Right) blinkingAlpha else 0f)
        )
    }
}