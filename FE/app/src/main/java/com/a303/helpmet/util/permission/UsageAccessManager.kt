package com.a303.helpmet.util.permission

import android.app.AlertDialog
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.provider.Settings

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
            .setTitle("따릉이를 빌리면 자동으로 알림을 드려요!")
            .setMessage("서비스 제공을 위해 사용 정보 접근 권한이 필요해요.")
            .setPositiveButton("설정으로 이동") { _, _ -> onAccepted() }
            .setNegativeButton("취소", null)
            .show()
    }
}

