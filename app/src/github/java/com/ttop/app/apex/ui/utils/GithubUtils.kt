package com.ttop.app.apex.ui.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.ttop.app.apex.R
import com.ttop.app.apex.extensions.showToast

object GithubUtils {

    fun checkFilesPermission(): Boolean {
        return Environment.isExternalStorageManager()
    }

    fun enableAllFiles(context: Context) {
        if (!Environment.isExternalStorageManager()) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            val uri = Uri.fromParts("package", context.packageName, null)
            intent.data = uri
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } else {
            context.showToast(ContextCompat.getString(context, R.string.all_files_granted))
        }
    }

    fun manageAllFiles(context: Context) {
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        val uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
}