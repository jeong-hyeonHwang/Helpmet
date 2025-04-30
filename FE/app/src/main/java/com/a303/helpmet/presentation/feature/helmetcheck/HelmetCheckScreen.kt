package com.a303.helpmet.presentation.feature.helmetcheck

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
-import com.a303.helpmet.presentation.feature.helmetcheck.component.HelmetInfoView
import com.a303.helpmet.presentation.feature.helmetcheck.component.HelmetServiceInfoView
import com.a303.helpmet.presentation.feature.helmetcheck.component.RecommendRouteView
import com.a303.helpmet.R
import com.a303.helpmet.ui.theme.HelpmetTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun HelmetCheckScreen(
    viewModel: HelmetCheckViewModel = koinViewModel(),
    onSuccess: () -> Unit
) {
//    val isConnected by viewModel.isConnected.collectAsState()
//    LaunchedEffect(isConnected) {
//        if (isConnected) onSuccess()
//    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {       
        // Text(if (isConnected) "헬멧이 연결되었습니다" else "헬멧을 연결하세요")
        // Spacer(Modifier.height(16.dp))
        // Button(
        //     colors = ButtonDefaults.buttonColors(
        //         containerColor = HelpmetTheme.colors.primary,
        //         contentColor = HelpmetTheme.colors.white1
        //     ),
        //     onClick = { viewModel.checkConnection() }) {
        //     Text(
        //         text = stringResource(R.string.begin_connection),
        //         style = HelpmetTheme.typography.bodyLarge)
        // }
        HelmetInfoView(viewModel)
        RecommendRouteView(viewModel, onSuccess)
        HelmetServiceInfoView()
    }
}

@Preview
@Composable
fun HelpmetCheckScreenPreview() {
    HelmetCheckScreen(
        viewModel = HelmetCheckViewModel(),
        onSuccess = {}
    )
}