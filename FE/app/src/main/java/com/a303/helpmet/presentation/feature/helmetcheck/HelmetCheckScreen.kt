package com.a303.helpmet.presentation.feature.helmetcheck

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.a303.helpmet.presentation.feature.helmetcheck.component.HelmetInfoView
import com.a303.helpmet.presentation.feature.helmetcheck.component.HelmetServiceInfoView
import com.a303.helpmet.presentation.feature.helmetcheck.component.KakaoNotificationDialog
import com.a303.helpmet.presentation.feature.helmetcheck.component.RecommendRouteView
import com.a303.helpmet.util.cache.PermissionPrefs
import com.a303.helpmet.util.extension.hasNotificationListenerPermission
import com.kakao.sdk.story.model.Story
import org.koin.androidx.compose.koinViewModel

@Composable
fun HelmetCheckScreen(
    viewModel: HelmetCheckViewModel = koinViewModel(),
    onHelmetChecked: () -> Unit,
    onStartVoiceGuide: () -> Unit
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val alreadyAsked = PermissionPrefs.wasNotificationAsked(context)
        if(!context.hasNotificationListenerPermission() && !alreadyAsked){
            showDialog = true
        }
    }

    if(showDialog){
        KakaoNotificationDialog (onDismiss = { showDialog = false })
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        HelmetInfoView(viewModel)
        RecommendRouteView(viewModel, onHelmetChecked)
        HelmetServiceInfoView(onStartVoiceGuide)
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