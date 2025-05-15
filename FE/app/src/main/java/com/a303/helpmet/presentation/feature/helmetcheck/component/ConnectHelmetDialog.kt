package com.a303.helpmet.presentation.feature.helmetcheck.component

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.a303.helpmet.R
import com.a303.helpmet.presentation.feature.helmetcheck.HelmetCheckViewModel
import com.a303.helpmet.ui.theme.HelpmetTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun ConnectHelmetDialog(
    viewModel: HelmetCheckViewModel = koinViewModel(),
) {
    val helmetName by viewModel.helmetName.collectAsState()
    val context = LocalContext.current

    Dialog(onDismissRequest = { viewModel.cancelDialog() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = HelpmetTheme.colors.white1
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 헤더 영역
                Text(
                    text = stringResource(R.string.helmet_found, helmetName),
                    style = HelpmetTheme.typography.bodyLarge,
                    color = HelpmetTheme.colors.black1
                )
                Text(
                    text = stringResource(R.string.confirm_connect_helmet),
                    style = HelpmetTheme.typography.bodySmall,
                    color = HelpmetTheme.colors.black1
                )
                Text(
                    text = stringResource(R.string.info_connect_helmet),
                    style = HelpmetTheme.typography.caption.copy(
                        lineHeight = 14.sp
                    ),
                    color = HelpmetTheme.colors.black1
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.connectToHelmetAp(context) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HelpmetTheme.colors.black1,
                            contentColor = HelpmetTheme.colors.primaryNeon
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.dialog_confirm)
                        )
                    }
                    OutlinedButton(
                        onClick = { viewModel.cancelDialog() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, HelpmetTheme.colors.gray2),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = HelpmetTheme.colors.black1
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.dialog_cancel)
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ConnectHelmetDialogPreview() {
    ConnectHelmetDialog(
        viewModel = HelmetCheckViewModel()
    )
}