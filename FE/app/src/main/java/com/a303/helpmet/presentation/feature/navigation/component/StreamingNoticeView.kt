package com.a303.helpmet.presentation.feature.navigation.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.a303.helpmet.R
import com.a303.helpmet.domain.model.StreamingNoticeState
import com.a303.helpmet.presentation.feature.navigation.NavigationViewModel
import com.a303.helpmet.ui.theme.HelpmetTheme
import com.a303.helpmet.util.postPosition.appendObjectPostposition
import com.a303.helpmet.util.postPosition.appendSubjectPostposition
import org.koin.androidx.compose.koinViewModel


@Composable
fun StreamingNoticeView(
    viewModel: NavigationViewModel = koinViewModel()
) {
    val state by viewModel.noticeState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(HelpmetTheme.colors.white1),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            StreamingNoticeState.Default -> DefaultNotice()
            StreamingNoticeState.Caution -> CautionNotice()
            StreamingNoticeState.Danger -> DangerNotice()
        }
    }
}

@Composable
fun DefaultNotice() {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ){
        Text(text="도착시간")
        Spacer(modifier = Modifier.width(50.dp))
        Text(text="남은 거리")
    }
}

@Composable
fun CautionNotice() {
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
        Text(text= stringResource(R.string.rear_caution, appendSubjectPostposition("자동차")), color = Color.Black)
    }
}

@Composable
fun DangerNotice() {
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
        Text(text= stringResource(R.string.rear_danger, appendObjectPostposition("사람")), color = Color.Black)
    }

}
