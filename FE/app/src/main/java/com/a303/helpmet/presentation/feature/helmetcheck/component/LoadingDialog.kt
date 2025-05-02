package com.a303.helpmet.presentation.feature.helmetcheck.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.a303.helpmet.presentation.feature.helmetcheck.HelmetCheckViewModel
import com.a303.helpmet.ui.theme.HelpmetTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoadingDialog(
    viewModel: HelmetCheckViewModel = koinViewModel(),
    message: String
){
    Dialog(onDismissRequest = {viewModel.cancelDialog()}) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = HelpmetTheme.colors.white1
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.height(50.dp).width(50.dp),
                    color = HelpmetTheme.colors.primary,
                    strokeWidth = 5.dp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    style = HelpmetTheme.typography.bodyMedium,
                    color = HelpmetTheme.colors.black1
                )
            }
        }
    }
}

@Preview
@Composable
fun LoadingDialogPreview(){
    LoadingDialog(
        viewModel = HelmetCheckViewModel(),
        message="헬멧을 검색하는 중입니다."
    )
}
