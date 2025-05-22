package com.a303.helpmet.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class HelpmetColors(
    val primary: Color,
    val primaryNeon: Color,
    val primaryLight: Color,
    val red1: Color,
    val red2: Color,
    val yellow1: Color,
    val black1: Color,
    val gray1: Color,
    val gray2: Color,
    val white1: Color
)

// 3) defaultHelpmetColors 에 매핑
val defaultHelpmetColors = HelpmetColors(
    primary       = Primary,
    primaryNeon   = PrimaryNeon,
    primaryLight  = PrimaryLight,
    red1          = Red1,
    red2          = Red2,
    yellow1       = Yellow1,
    black1        = Black1,
    gray1         = Gray1,
    gray2         = Gray2,
    white1        = White1
)

// 4) CompositionLocal 정의
val LocalHelpmetColors = staticCompositionLocalOf { defaultHelpmetColors }