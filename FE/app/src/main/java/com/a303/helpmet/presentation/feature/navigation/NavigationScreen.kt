package com.a303.helpmet.presentation.feature.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@Composable
fun NavigationScreen(
    onFinish: () -> Unit,
    viewModel: NavigationViewModel = koinViewModel()
) {
    val progress by viewModel.progress.collectAsState()
    LaunchedEffect(Unit) { viewModel.startNavigation() }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("내비게이션 진행 중: $progress%")
        Spacer(Modifier.height(16.dp))
        if (progress >= 100) {
            Button(onClick = onFinish) {
                Text("안내 종료")
            }
        }
    }
}