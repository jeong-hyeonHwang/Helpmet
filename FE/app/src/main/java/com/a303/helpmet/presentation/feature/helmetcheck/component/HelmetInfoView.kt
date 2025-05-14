package com.a303.helpmet.presentation.feature.helmetcheck.component

import android.Manifest
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.a303.helpmet.R
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.a303.helpmet.presentation.model.HelmetConnectionState
import com.a303.helpmet.presentation.feature.helmetcheck.HelmetCheckViewModel
import com.a303.helpmet.ui.theme.HelpmetTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun HelmetInfoView(
    viewModel: HelmetCheckViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val isConnected by viewModel.isConnected.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val toastShown by viewModel.toastShown.collectAsState()

    val wifiPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 권한이 허용되면 ViewModel에서 스캔 시작
            viewModel.startSearchAndScan(context) { scanResults ->
                val helmetResult = scanResults.firstOrNull { it.SSID.startsWith("HELPMET") }
                if (helmetResult != null) {
                    viewModel.setHelmetName(helmetResult.SSID)
                } else {
                    Toast.makeText(context, context.getString(R.string.error_helmet_not_found), Toast.LENGTH_SHORT).show()
                    viewModel.cancelDialog()
                }
            }
        } else {
            Toast.makeText(context, context.getString(R.string.permission_location_required), Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(connectionState) {
        if (connectionState == HelmetConnectionState.Success && !toastShown) {
            Toast.makeText(context, context.getString(R.string.dialog_connect_helmet_success), Toast.LENGTH_SHORT).show()
            viewModel.markToastShown()
        }
    }

    when(connectionState){
        HelmetConnectionState.Searching -> {
            LoadingDialog(viewModel, stringResource(R.string.dialog_searching_helmet))
        }
        HelmetConnectionState.Found -> {
            ConnectHelmetDialog(viewModel)
        }
        HelmetConnectionState.Connecting -> {
            LoadingDialog(viewModel, stringResource(R.string.dialog_connecting_helmet))
        }
        HelmetConnectionState.Disconnecting -> {
            LoadingDialog(viewModel, stringResource(R.string.dialog_disconnecting_helmet))
        }
        HelmetConnectionState.Success,
        HelmetConnectionState.Idle -> Unit
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
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

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Card(
                shape = RoundedCornerShape(10.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = HelpmetTheme.colors.white1,
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_util_helmet),
                        contentDescription = "헬멧",
                        tint = HelpmetTheme.colors.black1,
                        modifier = Modifier.size(200.dp),
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(if (isConnected) R.string.connect_helmet_success else R.string.connect_helmet_prompt),
                            style = HelpmetTheme.typography.bodySmall
                        )
                        Button(
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isConnected) HelpmetTheme.colors.gray2 else HelpmetTheme.colors.primaryLight,
                                contentColor = if (isConnected) HelpmetTheme.colors.white1 else HelpmetTheme.colors.black1
                            ),
                            modifier = Modifier
                                .height(30.dp),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 21.dp, vertical = 9.dp),
                            onClick = {
                                if (isConnected) {
                                    viewModel.disconnectFromHelmetAp(context)
                                }
                                else {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                        wifiPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                        viewModel.markToastUnShown()
                                    } else {
                                        viewModel.startSearchAndScan(context) { scanResults ->
                                            val helmetResult = scanResults.firstOrNull { it.SSID.startsWith("HELPMET") }
                                            if (helmetResult != null) {
                                                viewModel.setHelmetName(helmetResult.SSID)
                                            } else {
                                                Toast.makeText(context, context.getString(R.string.error_helmet_not_found), Toast.LENGTH_SHORT).show()
                                                viewModel.cancelDialog()
                                            }
                                        }
                                    }
                                }
                            }
                        ) {
                            Text(
                                text = if (isConnected) stringResource(R.string.end_connection) else stringResource(R.string.begin_connection),
                                style = HelpmetTheme.typography.caption
                            )
                        }
                    }

                }
            }

        }
    }


}

@Preview
@Composable
fun HelmetInfoViewPreview() {
    HelmetInfoView(
        viewModel = HelmetCheckViewModel(),
    )
}