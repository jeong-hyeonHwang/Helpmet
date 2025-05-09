package com.a303.helpmet.presentation.feature.preride

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.a303.helpmet.data.service.FakeNavigationService
import com.a303.helpmet.presentation.feature.preride.component.CourseCardPager
import com.a303.helpmet.presentation.feature.preride.component.CourseInfoBubbleView
import com.a303.helpmet.presentation.feature.preride.component.LocationCircleButton

@Composable
fun PreRideScreen(
    preRideViewModel: PreRideViewModel = remember {
        PreRideViewModel(FakeNavigationService())
    },
    onStartRide: (Int) -> Unit
) {
    // 1) ViewModel 상태 구독
    val routeOptions  by preRideViewModel.routeOptions.collectAsState()
    val selectedIndex by preRideViewModel.selectedCourseIndex.collectAsState()
    val routeInfoList by preRideViewModel.routeInfoList.collectAsState()

    // 2) 내 위치 자동 추적 플래그
    var followUser by remember { mutableStateOf(false) }

    // 4) 최초 한 번만 API 호출
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        preRideViewModel.loadRoutes(context)
    }

    Box(Modifier.fillMaxSize()) {
        // 5) 선택된 경로가 있으면 MapView 백그라운드로 그리기
        if (routeOptions.isNotEmpty()) {
            val option = routeOptions[selectedIndex]
            RoutePreviewMapView(
                routeOption     = option,
                followUser      = followUser,
                onFollowHandled = { followUser = false }
            )
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
                courses        = routeInfoList,
                onSelectCourse = { idx -> preRideViewModel.onCourseSelected(idx) },
                onStartRide    = onStartRide
            )
        }
    }
}

@Preview
@Composable
fun PreRidePreview() {
    PreRideScreen(onStartRide = {})
}
