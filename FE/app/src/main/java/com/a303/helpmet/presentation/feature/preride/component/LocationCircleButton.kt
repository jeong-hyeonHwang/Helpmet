package com.a303.helpmet.presentation.feature.preride.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.a303.helpmet.R
import com.a303.helpmet.ui.theme.HelpmetTheme

@Composable
fun LocationCircleButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .background(color = HelpmetTheme.colors.white1, shape = CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_util_location_circle),
            contentDescription = null,
            tint = HelpmetTheme.colors.gray1,
            modifier = Modifier.size(32.dp)
        )
    }
}
