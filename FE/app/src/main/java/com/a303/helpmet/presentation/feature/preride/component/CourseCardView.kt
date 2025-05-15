package com.a303.helpmet.presentation.feature.preride.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import com.a303.helpmet.R
import com.a303.helpmet.presentation.model.RouteInfo
import com.a303.helpmet.ui.theme.HelpmetTheme
import com.kakao.vectormap.route.RouteLineOptions

@Composable
fun CourseCardView(
    modifier: Modifier = Modifier,
    course: RouteInfo,
    onStartRide: () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = HelpmetTheme.colors.white1),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val courseName = if (course.routeId == -1) {
                stringResource(R.string.loading_recommended_courses)
            } else {
                stringResource(R.string.course_number, course.routeId + 1)
            }
            Text(
                text = courseName,
                style = HelpmetTheme.typography.subtitle,
                color = HelpmetTheme.colors.black1
            )
            // 출발 / 도착 지점
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.course_duration, course.duration),
                    style = HelpmetTheme.typography.title,
                    color = HelpmetTheme.colors.black1
                )
                Text(
                    text = stringResource(R.string.course_distance, course.distanceKm),
                    style = HelpmetTheme.typography.bodySmall,
                    color = HelpmetTheme.colors.black1
                )

                Spacer(Modifier.padding(4.dp))

                Text(
                    text = stringResource(R.string.start_location, course.startLocationName),
                    style = HelpmetTheme.typography.bodyMedium,
                    color = HelpmetTheme.colors.black1
                )
                Text(
                    text = stringResource(R.string.end_location, course.endLocationName),
                    style = HelpmetTheme.typography.bodyMedium,
                    color = HelpmetTheme.colors.black1
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 시작 버튼
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HelpmetTheme.colors.black1),
                onClick = {
                    onStartRide()
                }
            ) {
                Text(
                    text = stringResource(R.string.start_navigation),
                    style = HelpmetTheme.typography.subtitle,
                    color = HelpmetTheme.colors.primaryNeon
                )
            }
        }
    }
}
