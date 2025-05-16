package com.a303.helpmet.presentation.feature.helmetcheck.component

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.a303.helpmet.R
import com.a303.helpmet.ui.theme.HelpmetTheme
import com.a303.helpmet.util.cache.PermissionPrefs

@Composable
fun KakaoNotificationDialog(
    onDismiss: () -> Unit
){
    val context = LocalContext.current
    Dialog (onDismissRequest = onDismiss) {
        Surface (
            shape = RoundedCornerShape(16.dp),
            color = HelpmetTheme.colors.white1
        ) {
            Column (
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 헤더 영역
                Text(
                    text = stringResource(R.string.noti_permission_title),
                    style = HelpmetTheme.typography.bodyLarge,
                    color = HelpmetTheme.colors.black1
                )
                Text(
                    text = stringResource(R.string.noti_permission_content),
                    style = HelpmetTheme.typography.bodySmall,
                    color = HelpmetTheme.colors.black1
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            PermissionPrefs.setNotificationAsked(context, true)
                            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HelpmetTheme.colors.black1,
                            contentColor = HelpmetTheme.colors.primaryNeon
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.noti_permission_button_setting),
                            style = HelpmetTheme.typography.caption
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            PermissionPrefs.setNotificationAsked(context, true)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, HelpmetTheme.colors.gray2),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = HelpmetTheme.colors.black1
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.noti_permission_button_cancel),
                            style = HelpmetTheme.typography.caption
                        )
                    }
                }
            }
        }
    }
}