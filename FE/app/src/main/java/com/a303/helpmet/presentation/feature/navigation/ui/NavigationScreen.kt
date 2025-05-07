package com.a303.helpmet.presentation.feature.navigation.ui

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
import com.a303.helpmet.presentation.feature.navigation.viewmodel.NavigationViewModel
import org.koin.androidx.compose.koinViewModel
import com.a303.helpmet.presentation.feature.navigation.component.StreamingNoticeView
import com.a303.helpmet.presentation.feature.navigation.component.StreamingView
import com.a303.helpmet.presentation.feature.navigation.ui.MapScreen

@Composable
fun NavigationScreen(
    onFinish: () -> Unit,
    viewModel: NavigationViewModel = koinViewModel()
) {
    val isActiveStreamingView by viewModel.isActiveStreamingView.collectAsState()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val streamingViewHeight = screenWidth * 3 / 4

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
        }

        // 안내 멘트
        StreamingNoticeView(onFinish)

    }
}



