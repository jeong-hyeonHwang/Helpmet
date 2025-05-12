package com.a303.helpmet.presentation.feature.preride.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.a303.helpmet.R
import com.a303.helpmet.presentation.feature.preride.PreRideViewModel
import com.a303.helpmet.presentation.model.CourseInfo
import com.a303.helpmet.ui.theme.HelpmetTheme

@Composable
fun CourseCardView(
    modifier: Modifier,
    course: CourseInfo,
    onStartRide: (Int) -> Unit
){
    val viewModel = PreRideViewModel()
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = HelpmetTheme.colors.white1 // ← 원하는 배경색으로 바꾸기
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = course.courseName,
                style = HelpmetTheme.typography.subtitle,
                color = HelpmetTheme.colors.black1
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = course.duration,
                    style = HelpmetTheme.typography.title,
                    color = HelpmetTheme.colors.black1
                )
                Text(
                    text = course.distanceKm,
                    style = HelpmetTheme.typography.bodySmall,
                    color = HelpmetTheme.colors.black1
                )
                Spacer(Modifier.padding(4.dp))
                Text(
                    text = course.startStation,
                    style = HelpmetTheme.typography.bodyMedium,
                    color = HelpmetTheme.colors.black1
                )
                Text(
                    text = course.endStation,
                    style = HelpmetTheme.typography.bodyMedium,
                    color = HelpmetTheme.colors.black1
                )
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .width(333.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HelpmetTheme.colors.black1
                ),
                onClick = {
                    viewModel.selectCourse(course.navId.toString())
                    onStartRide(course.navId) }
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
