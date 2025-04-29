package com.a303.helpmet.presentation.feature.preride

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@Composable
fun PreRideScreen(
    viewModel: PreRideViewModel = koinViewModel(),
    onStartRide: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("따릉이 코스 안내 전 화면")
        Spacer(Modifier.height(16.dp))
        Button(onClick = { onStartRide() }) {
            Text("내비게이션 시작")
        }
    }
}