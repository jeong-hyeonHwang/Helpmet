package com.a303.helpmet.presentation.feature.helmetcheck.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.material3.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.a303.helpmet.R
import com.a303.helpmet.presentation.feature.helmetcheck.HelmetCheckScreen
import com.a303.helpmet.presentation.feature.helmetcheck.HelmetCheckViewModel
import com.a303.helpmet.ui.theme.HelpmetTheme

@Composable
fun HelmetServiceInfoView(
    onStartVoiceGuide: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = HelpmetTheme.colors.black1,
        ),
        modifier = Modifier.fillMaxWidth().clickable { onStartVoiceGuide() }
    ){
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_util_info_circle),
                    contentDescription = "정보",
                    tint = HelpmetTheme.colors.primaryNeon,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.voice_guide),
                    style = HelpmetTheme.typography.bodyMedium,
                    color = HelpmetTheme.colors.primaryNeon,
                )
            }
        }
    }
}

@Preview
@Composable
fun HelmetServiceInfoViewPreview() {
    HelmetServiceInfoView(onStartVoiceGuide={})
}