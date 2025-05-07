package com.a303.helpmet.presentation.feature.preride.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.util.lerp
import androidx.compose.ui.unit.dp
import com.a303.helpmet.presentation.model.CourseInfo
import kotlin.math.absoluteValue

@Composable
fun CourseCardPager(
    modifier: Modifier,
    courses: List<CourseInfo>,
    onStartRide: (Int) -> Unit
){
    val pagerState = rememberPagerState(pageCount = { courses.size })
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val cardWidth = screenWidth * 0.85f
    val sidePadding = (screenWidth - cardWidth) / 2

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = sidePadding), // 정확히 가운데 맞춤
            pageSpacing = 8.dp,
        ) { page ->

            val pageOffset = (
                    (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                    ).absoluteValue

            val scale = lerp(start = 0.9f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f))
            val alpha = lerp(start = 0.88f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f))
            CourseCardView(
                modifier = Modifier.width(cardWidth)
                    .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                },
                course = courses[page],
                onStartRide = onStartRide)
        }

        CoursesCardPagerIndicator(
            pageCount = courses.size,
            currentPage = pagerState.currentPage
        )
    }
}
