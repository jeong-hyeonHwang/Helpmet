package com.a303.helpmet.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

@Composable
fun HelpmetTheme(
    content: @Composable () -> Unit
) {
    val helpmetColors = defaultHelpmetColors
    val helpmetTypography = defaultHelpmetTypography

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            background = Color.White
        ),
        typography = MaterialTheme.typography,
        content = {
            CompositionLocalProvider(
                LocalHelpmetColors provides helpmetColors,
                LocalHelpmetTypography provides helpmetTypography
            ) {
                content()
            }
        }
    )
}