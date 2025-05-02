package com.a303.helpmet.presentation.feature.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import com.a303.helpmet.R
import com.a303.helpmet.ui.theme.HelpmetTheme
import com.a303.helpmet.util.postPosition.appendObjectPostposition
import com.a303.helpmet.util.postPosition.appendSubjectPostposition


@Composable
fun NavigationScreen(
    onFinish: () -> Unit,
    viewModel: NavigationViewModel = koinViewModel()
) {
    val isActiveStreamingView by viewModel.isActiveStreamingView.collectAsState()



    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        val screenWidth = LocalConfiguration.current.screenWidthDp.dp
        val streamingViewHeight = screenWidth * 3 / 4

        if (isActiveStreamingView) {
            Box(
                modifier = Modifier.fillMaxWidth().height(streamingViewHeight).background(Color.Black)
            )
        }

        // 카메라 뷰 토클
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable { viewModel.toggleStreaming() }, contentAlignment = Alignment.Center
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
                .background(Color.Yellow)
        )

        // 안내 멘트
        StreamingNoticeView()

    }
}


@Composable
fun StreamingNoticeView(
    viewModel: NavigationViewModel = koinViewModel()
) {
    val state by viewModel.noticeState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.updateNoticeState(StreamingNoticeState.Danger)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(HelpmetTheme.colors.white1),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            StreamingNoticeState.Default -> DefaultNotice()
            StreamingNoticeState.Caution -> CautionNotice()
            StreamingNoticeState.Danger -> DangerNotice()
        }
    }
}

@Composable
fun DefaultNotice() {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ){
        Text(text="도착시간")
        Spacer(modifier = Modifier.width(50.dp))
        Text(text="남은 거리")
    }
}

@Composable
fun CautionNotice() {
    val alpha by rememberInfiniteTransition().animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Yellow.copy(alpha = alpha)),
        contentAlignment = Alignment.Center
    ) {
        Text(text=stringResource(R.string.rear_caution, appendSubjectPostposition("자동차")), color = Color.Black)
    }
}

@Composable
fun DangerNotice() {
    val alpha by rememberInfiniteTransition().animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFC0CB).copy(alpha = alpha)),
        contentAlignment = Alignment.Center
    ) {
        Text(text=stringResource(R.string.rear_danger, appendObjectPostposition("사람")), color = Color.Black)
    }

}

