package com.a303.helpmet.presentation.feature.preride.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.a303.helpmet.R
import com.a303.helpmet.ui.theme.HelpmetTheme

@Composable
fun RouteMapLoadingView(
    message: String = stringResource(R.string.dialog_connecting_map)
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HelpmetTheme.colors.black1.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier.size(50.dp),
                color = HelpmetTheme.colors.primary,
                strokeWidth = 5.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = HelpmetTheme.typography.bodyMedium,
                color = HelpmetTheme.colors.white1
            )
        }
    }
}
