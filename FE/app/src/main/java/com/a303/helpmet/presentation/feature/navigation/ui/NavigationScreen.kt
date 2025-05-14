package com.a303.helpmet.presentation.feature.navigation.ui

import android.Manifest
import android.content.pm.PackageManager
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
import com.a303.helpmet.R
import com.a303.helpmet.domain.model.DirectionState
import com.a303.helpmet.presentation.feature.navigation.viewmodel.NavigationViewModel
import org.koin.androidx.compose.koinViewModel
import com.a303.helpmet.presentation.feature.navigation.component.StreamingNoticeView
import com.a303.helpmet.presentation.feature.navigation.component.StreamingView
import com.a303.helpmet.presentation.feature.voiceinteraction.VoiceInteractViewModel

@Composable
fun NavigationScreen(
    onFinish: () -> Unit,
    viewModel: NavigationViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val voiceViewModel: VoiceInteractViewModel = koinViewModel()

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
        viewModel.connectToDirectionSocket()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.disconnectFromDirectionSocket()
        }
    }

    val isActiveStreamingView by viewModel.isActiveStreamingView.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        if (isActiveStreamingView) {
            StreamingView()
        }

        // 카메라 뷰 토글 버튼
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable { viewModel.toggleStreaming() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.Gray)
            )
        }

        // 지도
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            MapScreen(
                defaultZoom = 15
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
        StreamingNoticeView(onFinish)

    }
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