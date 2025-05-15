package com.a303.helpmet.presentation.feature.preride.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.a303.helpmet.ui.theme.HelpmetTheme

@Composable
fun CoursesCardPagerIndicator(
    pageCount: Int = 1,
    currentPage: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = HelpmetTheme.colors.primary,
    inactiveColor: Color = HelpmetTheme.colors.white1,
    indicatorSize: Dp = 8.dp,
    spacing: Dp = 8.dp
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(spacing),
        modifier = modifier.padding(top = 16.dp)
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier
                    .size(indicatorSize)
                    .clip(CircleShape)
                    .background(if (index == currentPage) activeColor else inactiveColor)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CoursesCardPagerIndicatorPreview() {
    CoursesCardPagerIndicator(
        pageCount = 3,
        currentPage = 0 // 가운데 인디케이터 활성화
    )
}

