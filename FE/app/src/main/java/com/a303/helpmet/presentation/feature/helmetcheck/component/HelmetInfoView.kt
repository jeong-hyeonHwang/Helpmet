package com.a303.helpmet.presentation.feature.helmetcheck.component

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.material3.*
import androidx.compose.ui.res.stringResource
import com.a303.helpmet.R
import androidx.compose.ui.unit.dp
import com.a303.helpmet.presentation.feature.helmetcheck.HelmetCheckViewModel
import com.a303.helpmet.ui.theme.HelpmetTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun HelmetInfoView(
    viewModel: HelmetCheckViewModel = koinViewModel(),
) {
    val isConnected by viewModel.isConnected.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Row {
            Text(
                text = "헬프",
                style = HelpmetTheme.typography.appTitle,
                color = HelpmetTheme.colors.primary
            )
            Text(
                text = "멧",
                style = HelpmetTheme.typography.appTitle,
                color = HelpmetTheme.colors.black1
            )
        }
        Text(
            text = stringResource(R.string.safety_info),
            style = HelpmetTheme.typography.bodySmall,
            color = HelpmetTheme.colors.gray2
        )
    }

    // 두 Column 사이 간격
    Spacer(modifier = Modifier.height(32.dp))

    Column {
        Text("헬멧 연결 정보")
        Button(onClick = { viewModel.checkConnection() }) {
            Text(if (isConnected) "연결됨" else "연결하기")
        }
    }
}