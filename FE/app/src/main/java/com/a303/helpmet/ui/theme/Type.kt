package com.a303.helpmet.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.a303.helpmet.R


// Pretendard 폰트 정의 (여러 굵기 지원)
val Pretendard = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_bold, FontWeight.Bold)
)

// HelpmetTypography 데이터 클래스 정의
@Immutable
data class HelpmetTypography(
    val display: TextStyle,
    val appTitle: TextStyle,
    val headline: TextStyle,
    val title: TextStyle,
    val subtitle: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val bodySmall: TextStyle,
    val caption: TextStyle
)


// Material3 Typography 커스터마이징 (Pretendard 적용)
val defaultHelpmetTypography = HelpmetTypography(
    // Display: Pretendard Bold 54
    display = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Bold,
        fontSize = 54.sp
    ),
    // App Title: Pretendard Bold 46
    appTitle = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Bold,
        fontSize = 46.sp
    ),
    // Headline: Pretendard Bold 28
    headline = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp
    ),
    // Title: Pretendard Bold 24
    title = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    // Subtitle: Pretendard Bold 16
    subtitle = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    ),
    // Body Large: Pretendard Bold 18
    bodyLarge = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    ),
    // Body Medium: Pretendard Regular 16
    bodyMedium = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    // Body Small: Pretendard Regular 14
    bodySmall = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    // Caption: Pretendard Regular 10
    caption = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp
    )
)

val LocalHelpmetTypography = staticCompositionLocalOf { defaultHelpmetTypography }