package com.a303.helpmet.presentation.feature.helmetcheck.component

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.a303.helpmet.presentation.feature.helmetcheck.HelmetCheckViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun RecommendRouteView(
    viewModel: HelmetCheckViewModel = koinViewModel(),
    onSuccess: () -> Unit
) {
    val isConnected by viewModel.isConnected.collectAsState()
    Column(
        modifier = Modifier.fillMaxWidth(), //.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("추천 코스")
        Button(onClick = {
            if (isConnected) {
                onSuccess()
            }
        }) {
            Text("추천 받기")
        }
    }
}