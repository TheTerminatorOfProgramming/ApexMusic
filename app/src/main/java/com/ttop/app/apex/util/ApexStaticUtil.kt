package com.ttop.app.apex.util

import android.app.Activity
import android.media.MediaScannerConnection
import com.ttop.app.apex.R
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.misc.UpdateToastMediaScannerCompletionListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileFilter

object ApexStaticUtil {
    suspend fun listPaths(
        file: File,
        fileFilter: FileFilter,
        doOnPathListed: (paths: Array<String?>) -> Unit,
    ) {
        val paths = try {
            val paths: Array<String?>
            if (file.isDirectory) {
                val files = FileUtil.listFilesDeep(file, fileFilter)
                paths = arrayOfNulls(files.size)
                for (i in files.indices) {
                    val f = files[i]
                    paths[i] = FileUtil.safeGetCanonicalPath(f)
                }
            } else {
                paths = arrayOfNulls(1)
                paths[0] = file.path
            }
            paths
        } catch (e: Exception) {
            e.printStackTrace()
            arrayOf()
        }
        withContext(Dispatchers.Main) {
            doOnPathListed(paths)
        }
    }

    fun scanPaths(activity: Activity, toBeScanned: Array<String?>) {
        if (toBeScanned.isEmpty()) {
            activity.showToast(R.string.nothing_to_scan)
        } else {
            MediaScannerConnection.scanFile(
                activity,
                toBeScanned,
                null,
                UpdateToastMediaScannerCompletionListener(activity, listOf(*toBeScanned))
            )
            activity.recreate()
        }
    }
}