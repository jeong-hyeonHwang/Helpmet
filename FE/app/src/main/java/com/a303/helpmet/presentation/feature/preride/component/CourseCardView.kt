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
import com.a303.helpmet.presentation.model.CourseInfo
import com.a303.helpmet.ui.theme.HelpmetTheme

@Composable
fun CourseCardView(
    modifier: Modifier = Modifier,
    course: CourseInfo,
    onSelectCourse: (Int) -> Unit,
    onStartRide: (Int) -> Unit
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
            // 코스 이름
            Text(
                text = course.courseName,
                style = HelpmetTheme.typography.subtitle,
                color = HelpmetTheme.colors.black1
            )

            // 시간 · 거리
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = course.duration,
                    style = HelpmetTheme.typography.bodyMedium,
                    color = HelpmetTheme.colors.black1
                )
                Text(
                    text = course.distanceKm,
                    style = HelpmetTheme.typography.bodyMedium,
                    color = HelpmetTheme.colors.black1
                )
            }

            // 출발 / 도착 스테이션
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = course.startStation,
                    style = HelpmetTheme.typography.bodySmall,
                    color = HelpmetTheme.colors.black1
                )
                Text(
                    text = course.endStation,
                    style = HelpmetTheme.typography.bodySmall,
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
                    onSelectCourse(course.navId)
                    onStartRide(course.navId)
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
