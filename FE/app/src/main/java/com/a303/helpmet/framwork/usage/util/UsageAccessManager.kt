package com.a303.helpmet.framwork.usage.util

import android.app.AlertDialog
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.ui.res.stringResource
import com.a303.helpmet.R
import androidx.compose.ui.res.stringResource


object UsageAccessManager {

    fun hasUsageAccess(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun requestUsageAccessPermission(context: Context) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    fun showPermissionDialog(context: Context, onAccepted: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle(
                context.getString(R.string.permission_title))
            .setMessage(context.getString(R.string.permission_content))
            .setPositiveButton(context.getString(R.string.permission_button_setting)) { _, _ -> onAccepted() }
            .setNegativeButton(context.getString(R.string.permission_button_cancel), null)
            .show()
    }
}

