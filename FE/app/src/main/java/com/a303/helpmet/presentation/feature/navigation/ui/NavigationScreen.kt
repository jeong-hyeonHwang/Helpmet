package com.a303.helpmet.presentation.feature.navigation.ui

import android.Manifest
import android.content.pm.PackageManager
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
import com.a303.helpmet.R
import com.a303.helpmet.domain.model.DirectionState
import com.a303.helpmet.presentation.feature.navigation.viewmodel.NavigationViewModel
import org.koin.androidx.compose.koinViewModel
import com.a303.helpmet.presentation.feature.navigation.component.StreamingNoticeView
import com.a303.helpmet.presentation.feature.navigation.component.StreamingView
import com.a303.helpmet.presentation.feature.navigation.viewmodel.DetectionViewModel
import com.a303.helpmet.presentation.feature.navigation.viewmodel.RouteViewModel
import com.a303.helpmet.presentation.feature.preride.UserPositionViewModel
import com.a303.helpmet.presentation.feature.voiceinteraction.VoiceInteractViewModel
import com.a303.helpmet.ui.theme.HelpmetTheme
import com.a303.helpmet.util.cache.RouteCache

@Composable
fun NavigationScreen(
    onFinish: () -> Unit,
    navigationViewModel: NavigationViewModel = koinViewModel(),
    userPositionViewModel: UserPositionViewModel = viewModel(),
    routeViewModel: RouteViewModel = koinViewModel(),
    detectionViewModel: DetectionViewModel = koinViewModel(),
    voiceViewModel: VoiceInteractViewModel = koinViewModel(),
    navController: NavController
) {
    val context = LocalContext.current

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
        if (isGranted) {
            voiceViewModel.startListening()
        } else {
            voiceViewModel.notifyPermissionMissing()
        }
    }

    // 최초 실행 시 권한 요청
    LaunchedEffect(Unit) {
        if (!hasRecordPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            voiceViewModel.startListening()
        }

        navigationViewModel.connectToSocket(detectionViewModel.onFrameReceived())

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
//        Log.d("VoiceHandler", "뒤로감")
        voiceViewModel.stopListening() // 강제로 STT/TTS 종료
        navController.popBackStack()
    }


    // 2) 내 위치 자동 추적 플래그
    var followUser by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        StreamingView()
    }

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
//            .weight(1f)
    ) {
        MapScreen(
            followUser = followUser,
            onFollowHandled = { followUser = false },
            routeViewModel = routeViewModel,
            userPositionViewModel = userPositionViewModel,
            voiceViewModel = voiceViewModel
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            DirectionIcons()
        }
    }

    // 안내 멘트
    StreamingNoticeView(
        onFinish,
        navigationViewModel = navigationViewModel,
        routeViewModel = routeViewModel
    )

}

@Composable
fun DirectionIcons(
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
        modifier = Modifier
            .fillMaxWidth(),
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