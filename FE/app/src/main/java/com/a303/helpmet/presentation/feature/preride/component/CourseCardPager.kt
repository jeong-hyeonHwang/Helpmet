package com.a303.helpmet.presentation.feature.preride.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import androidx.compose.ui.unit.dp
import com.a303.helpmet.presentation.model.RouteInfo
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.absoluteValue

@Composable
fun CourseCardPager(
    modifier: Modifier,
    courses: List<RouteInfo>,
    onSelectCourse: (Int) -> Unit,
    onStartRide: (Int) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { courses.size })

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                onSelectCourse(page)
            }
    }



    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 8.dp,
        ) { page ->
            CourseCardView(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                        scaleX = lerp(0.9f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                        scaleY = scaleX
                        alpha = lerp(0.85f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                    },
                course = courses[page],
                onSelectCourse = onSelectCourse,
                onStartRide = onStartRide
            )
        }

        CoursesCardPagerIndicator(
            pageCount = courses.size,
            currentPage = pagerState.currentPage
        )
    }
}
