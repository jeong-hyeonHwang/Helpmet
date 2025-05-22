package com.a303.helpmet.presentation.feature.preride.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.a303.helpmet.R
import com.a303.helpmet.ui.theme.HelpmetTheme

@Composable
fun CourseInfoBubbleView(
    modifier: Modifier = Modifier
){
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val cardWidth = screenWidth * 0.88f

    Card(
        modifier = modifier.width(cardWidth),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = HelpmetTheme.colors.white1.copy(alpha = 0.85f),
            contentColor = HelpmetTheme.colors.black1)
    ) {
        Text(
            text = stringResource(R.string.nearby_course_info),
            style = HelpmetTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth()
                . padding(horizontal = 16.dp, vertical = 12.dp),
            textAlign = TextAlign.Center
        )
    }
}