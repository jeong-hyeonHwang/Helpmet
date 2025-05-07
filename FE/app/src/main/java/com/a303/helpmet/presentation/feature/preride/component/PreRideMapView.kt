package com.a303.helpmet.presentation.feature.preride.component

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import com.a303.helpmet.ui.theme.HelpmetTheme

@Composable
fun PreRideMapView(){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HelpmetTheme.colors.red1) // 초록색 배경
    )
}