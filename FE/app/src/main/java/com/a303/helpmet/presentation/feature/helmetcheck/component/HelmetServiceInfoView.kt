package com.a303.helpmet.presentation.feature.helmetcheck.component

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp

@Composable
fun HelmetServiceInfoView() {
    Column(
        modifier = Modifier.fillMaxWidth(), //.padding(16.dp)
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("음성 정보")
    }
}