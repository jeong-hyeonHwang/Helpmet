package com.a303.helpmet.presentation.feature.navigation.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.a303.helpmet.R
import com.a303.helpmet.domain.model.DetectedObjectState
import com.a303.helpmet.domain.model.StreamingNoticeState
import com.a303.helpmet.presentation.feature.navigation.viewmodel.NavigationViewModel
import com.a303.helpmet.presentation.feature.navigation.viewmodel.RouteViewModel
import com.a303.helpmet.presentation.state.DetectionStateManager
import com.a303.helpmet.ui.theme.HelpmetTheme
import com.a303.helpmet.util.extension.trimLocationName
import com.a303.helpmet.util.postPosition.appendObjectPostposition
import com.a303.helpmet.util.postPosition.appendSubjectPostposition
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Composable
fun StreamingNoticeView(
    onFinish: () -> Unit,
    navigationViewModel: NavigationViewModel,
    routeViewModel: RouteViewModel
) {
    val noticeState by DetectionStateManager.noticeState.collectAsState()
    val detectedObjectState by DetectionStateManager.detectedObjectState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(HelpmetTheme.colors.white1),
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
            ,
            contentAlignment = Alignment.CenterEnd
        ){
            NavigationMoreButton(onFinish)
        }


        when (noticeState) {
            StreamingNoticeState.Default -> DefaultNotice(routeViewModel)
            StreamingNoticeState.Caution -> CautionNotice(detectedObjectState)
            StreamingNoticeState.Danger -> DangerNotice(detectedObjectState)
        }


    }
}

@Composable
fun DefaultNotice(
    routeViewModel: RouteViewModel
) {

    val routeInfo by routeViewModel.routeInfo.collectAsState()
    val durationMin = routeInfo?.duration

    val estimatedArrivalTime = remember(durationMin) {
        val now = LocalDateTime.now()
        val arrival = durationMin?.toLong()?.let { now.plusMinutes(it) }
        val formatter = DateTimeFormatter.ofPattern("HH시 mm분")
        arrival?.format(formatter) ?: now.format(formatter)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            ){
            Icon(
                painter = painterResource(id = R.drawable.ic_util_direction_arrow),
                contentDescription = "시간 감소",
                tint = HelpmetTheme.colors.black1,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            routeInfo?.endLocationName?.let { Text(text= it.trimLocationName()) }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ){
            Text(text= estimatedArrivalTime)

            Spacer(modifier = Modifier.width(50.dp))

            routeInfo?.distanceKm?.let {
                Text(text= stringResource(R.string.course_distance, it))
            }

        }
    }


}

@Composable
fun CautionNotice(
    detectedObjectState: DetectedObjectState,
) {
    val alpha by rememberInfiniteTransition().animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Yellow.copy(alpha = alpha)),
        contentAlignment = Alignment.Center
    ) {
        Text(text= stringResource(R.string.rear_caution, appendSubjectPostposition(detectedObjectState.toKorean())), color = Color.Black)
    }
}

@Composable
fun DangerNotice(
    detectedObjectState: DetectedObjectState,
) {
    val alpha by rememberInfiniteTransition().animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFC0CB).copy(alpha = alpha)),
        contentAlignment = Alignment.Center
    ) {
        Text(text= stringResource(R.string.rear_danger, appendObjectPostposition(detectedObjectState.toKorean())), color = Color.Black)
    }

}
