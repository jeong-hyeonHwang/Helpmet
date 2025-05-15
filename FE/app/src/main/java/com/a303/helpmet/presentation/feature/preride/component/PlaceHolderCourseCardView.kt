package com.a303.helpmet.presentation.feature.preride.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.a303.helpmet.presentation.model.RouteInfo

@Composable
fun PlaceholderCourseCard() {
    CourseCardView(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        course = RouteInfo(),
        onStartRide = { }
    )
}
