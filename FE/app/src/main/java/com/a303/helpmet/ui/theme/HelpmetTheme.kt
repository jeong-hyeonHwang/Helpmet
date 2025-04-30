package com.a303.helpmet.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

object HelpmetTheme {
    val colors: HelpmetColors
        @Composable
        @ReadOnlyComposable
        get() = LocalHelpmetColors.current

    val typography: HelpmetTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalHelpmetTypography.current
}
