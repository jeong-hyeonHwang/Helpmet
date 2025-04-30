package com.a303.helpmet.presentation.feature.design_test

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.a303.helpmet.R
import com.a303.helpmet.ui.theme.HelpmetTheme

@Composable
fun DesignTestScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HelpmetTheme.colors.white1),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .wrapContentSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_util_minus_circle),
                contentDescription = "시간 감소",
                tint = HelpmetTheme.colors.black1,
                modifier = Modifier.size(48.dp)
            )
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = HelpmetTheme.colors.primary,
                    contentColor = HelpmetTheme.colors.white1
                ),
                onClick = { Log.d("TEST", "DesignTestScreen: Button Clicked!") }
            ) {
                Text(
                    text = stringResource(R.string.begin_connection),
                    style = HelpmetTheme.typography.bodyLarge
                )
            }
            Icon(
                painter = painterResource(id = R.drawable.ic_util_plus_circle),
                contentDescription = "시간 증가",
                tint = HelpmetTheme.colors.black1,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Preview
@Composable
fun DesignTestScreenPreview() {
    DesignTestScreen()
}