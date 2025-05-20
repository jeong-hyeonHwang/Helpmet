package com.a303.helpmet.presentation.feature.helmetcheck.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.material3.*
import com.a303.helpmet.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.a303.helpmet.presentation.feature.helmetcheck.HelmetCheckScreen
import com.a303.helpmet.presentation.feature.helmetcheck.HelmetCheckViewModel
import com.a303.helpmet.ui.theme.HelpmetTheme
import org.koin.androidx.compose.koinViewModel


@Composable
fun RecommendRouteView(
    viewModel: HelmetCheckViewModel = koinViewModel(),
    onHelmetChecked: () -> Unit
) {
    val isConnected by viewModel.isConnected.collectAsState()
    Card(
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = HelpmetTheme.colors.black1,
        ),
        modifier = Modifier.fillMaxWidth()
    ){
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = stringResource(R.string.recommend_course),
                    style = HelpmetTheme.typography.title,
                    color = HelpmetTheme.colors.white1,
                )
                Text(
                    text = stringResource(R.string.recommend_course_with_ddareungi),
                    color = HelpmetTheme.colors.primaryNeon,
                    style = HelpmetTheme.typography.bodySmall
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().clickable {
                    onHelmetChecked()
                },
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    text = stringResource(R.string.start),
                    style = HelpmetTheme.typography.subtitle,
                    color = HelpmetTheme.colors.white1,
                )
            }

        }
    }
}

@Preview
@Composable
fun HelpmetCheckScreenPreview() {
    HelmetCheckScreen(
        viewModel = HelmetCheckViewModel(),
        onHelmetChecked = {},
        onStartVoiceGuide = {}
    )
}