package com.a303.helpmet.presentation.feature.preride

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.a303.helpmet.data.dto.response.CourseResponse
import com.a303.helpmet.data.service.FakeNavigationService
import com.a303.helpmet.domain.mapper.toCourseInfo
import com.a303.helpmet.presentation.feature.preride.component.*

@Composable
fun PreRideScreen(
//    preRideViewModel: PreRideViewModel = koinViewModel(),
    preRideViewModel: PreRideViewModel = remember {
        PreRideViewModel(FakeNavigationService())
    },
    onStartRide: (Int) -> Unit
) {
    // 1) ViewModel의 Flows 를 collectAsState
    val routeOptions   by preRideViewModel.routeOptions.collectAsState()
    val selectedIndex  by preRideViewModel.selectedCourseIndex.collectAsState()

    val selectedOption = routeOptions.getOrNull(selectedIndex)

    val dummyCourses = listOf(
        CourseResponse(
            courseNumber = 1,
            duration = 15,
            distanceKm = 8.5f,
            startStation = "연제구 꽃잎길 32",
            endStation = "성동구 와우로길 32길",
            navId = 1
        ),
        CourseResponse(
            courseNumber = 2,
            duration = 30,
            distanceKm = 10.5f,
            startStation = "연제구 꽃잎길 32",
            endStation = "성동구 와우로길 32길",
            navId = 2
        ),
        CourseResponse(
            courseNumber = 3,
            duration = 10,
            distanceKm = 6f,
            startStation = "연제구 꽃잎길 32",
            endStation = "성동구 와우로길 32길",
            navId = 3
        )
    ).map { it.toCourseInfo(LocalContext.current) }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        preRideViewModel.loadRoutes(context)
    }

    LaunchedEffect(routeOptions) {
        Log.d("PreRideScreen", "routeOptions size = ${routeOptions.size}")
        Log.d("PreRideScreen", "selectedOption size = ${selectedOption.toString()}")


    }

    // 1) 전체를 채우는 Box
    Box(modifier = Modifier.fillMaxSize()) {
        // 2) 맵을 배경으로 깔기
        if (routeOptions.isNotEmpty()) {
            val option = routeOptions[selectedIndex]
            RoutePreviewMapView(
                routeOption = option,
            )
        }

        // 3) 상단에 CourseInfoBubbleView 오버레이
        CourseInfoBubbleView(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 32.dp)
        )

        // 4) 하단에 CourseCardPager 오버레이
        CourseCardPager(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            courses = dummyCourses,
            onSelectCourse = { idx ->
                Log.d("PreRideScreen", "PreRideScreen: ${idx}")
                preRideViewModel.onCourseSelected(idx)
            },
            onStartRide = onStartRide
        )
    }
}


@Preview
@Composable
fun PreRidePreview(){
    PreRideScreen(
        onStartRide={}
    )
}