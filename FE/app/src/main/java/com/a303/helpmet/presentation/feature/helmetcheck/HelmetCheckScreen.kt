package com.a303.helpmet.presentation.feature.helmetcheck

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@Composable
fun HelmetCheckScreen(
    viewModel: HelmetCheckViewModel = koinViewModel(),
    onSuccess: () -> Unit
) {
    val isConnected by viewModel.isConnected.collectAsState()
    LaunchedEffect(isConnected) {
        if (isConnected) onSuccess()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(if (isConnected) "헬멧이 연결되었습니다" else "헬멧을 연결하세요")
        Spacer(Modifier.height(16.dp))
        Button(onClick = { viewModel.checkConnection() }) {
            Text("연결 확인")
        }
    }
}