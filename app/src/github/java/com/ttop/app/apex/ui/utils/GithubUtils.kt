package com.ttop.app.apex.ui.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dcastalia.localappupdate.DownloadApk
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.AppUpdaterUtils
import com.github.javiersantos.appupdater.enums.AppUpdaterError
import com.github.javiersantos.appupdater.enums.Display
import com.github.javiersantos.appupdater.enums.UpdateFrom
import com.github.javiersantos.appupdater.objects.Update
import com.ttop.app.apex.BuildConfig
import com.ttop.app.apex.R
import com.ttop.app.apex.extensions.appendChar
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.extensions.withCenteredButtons
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.appthemehelper.util.VersionUtils
import java.util.Calendar

object GithubUtils {
    fun enableUnknownSources(context: Context) {
        context.startActivity(
            Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:" + context.packageName)
            )
        )
    }
    fun checkUnknownSources(context: Context): Boolean {
        return context.packageManager.canRequestPackageInstalls()
    }
    fun checkForUpdateGithub(context: Context, showFailMessage: Boolean) {
        val updater = AppUpdaterUtils(context)
            .setUpdateFrom(UpdateFrom.GITHUB)
            .setGitHubUserAndRepo("TheTerminatorOfProgramming", "ApexMusic")
            .withListener(object : AppUpdaterUtils.UpdateListener {
                override fun onSuccess(update: Update, isUpdateAvailable: Boolean?) {
                    Log.d("Latest Version", update.latestVersion)
                    Log.d("Latest Version Code", update.latestVersionCode.toString())
                    Log.d("URL", update.urlToDownload.toString())
                    Log.d(
                        "Is update available?",
                        java.lang.Boolean.toString(isUpdateAvailable!!)
                    )

                    val finalVersionNumberString = BuildConfig.VERSION_CODE.toString().appendChar(0, '.')
                    val finalVersionNumber = finalVersionNumberString.toDouble()
                    val isPreview = update.latestVersion.contains("-preview")

                    if (isPreview) {
                        val updateVersionPreview = update.latestVersion.dropLast(8).toDouble()
                        if (updateVersionPreview > finalVersionNumber) {
                            if (PreferenceUtil.isPreviewChannel) {
                                val appUpdater = AppUpdater(context)
                                    .setUpdateFrom(UpdateFrom.GITHUB)
                                    .setGitHubUserAndRepo("TheTerminatorOfProgramming", "ApexMusic")
                                    .setDisplay(Display.DIALOG)
                                    .setContentOnUpdateAvailable("Update " + update.latestVersion.replace('-', ' ') + " is available to download!\n\n" + "By downloading this update you will get the latest features, improvements and bug fixes!")
                                    .showAppUpdated(true)
                                appUpdater.start()
                            }else {
                                if (showFailMessage){
                                    context.showToast("No Updates Available!")
                                }
                            }
                        }else {
                            if (showFailMessage){
                                context.showToast("No Updates Available!")
                            }
                        }
                    }else {
                        val updateVersion = update.latestVersion.toDouble()
                        if (updateVersion > finalVersionNumber) {
                            val appUpdater = AppUpdater(context)
                                .setUpdateFrom(UpdateFrom.GITHUB)
                                .setGitHubUserAndRepo("TheTerminatorOfProgramming", "ApexMusic")
                                .setDisplay(Display.DIALOG)
                                .setContentOnUpdateAvailable("Update " + update.latestVersion + " is available to download!\n\n" + "By downloading this update you will get the latest features, improvements and bug fixes!")
                                .showAppUpdated(true)
                            appUpdater.start()
                        } else if (BuildConfig.BUILD_TYPE.equals("preview")) {
                            val appUpdater = AppUpdater(context)
                                .setUpdateFrom(UpdateFrom.GITHUB)
                                .setGitHubUserAndRepo("TheTerminatorOfProgramming", "ApexMusic")
                                .setDisplay(Display.DIALOG)
                                .setContentOnUpdateAvailable("Update " + update.latestVersion + " stable is available to download!\n\n" + "By downloading this update you will get the latest features, improvements and bug fixes!")
                                .showAppUpdated(true)
                            appUpdater.start()
                        }else {
                            if (showFailMessage){
                                context.showToast("No Updates Available!")
                            }
                        }
                    }
                }

                override fun onFailed(error: AppUpdaterError) {
                    Log.d("AppUpdater Error", "Something went wrong")
                }
            })
        updater.start()
    }

    fun updateApex(context: Context) {
        when (PreferenceUtil.updateFrequency) {
            "startup" -> {
                if (!PreferenceUtil.updateChecked) {
                    checkForUpdateGithub(context, false)

                    PreferenceUtil.updateChecked = true
                }
            }

            "daily" -> {
                val cal = Calendar.getInstance()
                val currentDayOfYear = cal[Calendar.DAY_OF_YEAR]

                if (PreferenceUtil.dayOfYear != currentDayOfYear) {
                    PreferenceUtil.dayOfYear = currentDayOfYear

                    if (!PreferenceUtil.updateChecked) {
                        checkForUpdateGithub(context, false)

                        PreferenceUtil.updateChecked = true
                    }
                }
            }

            "weekly" -> {
                val cal = Calendar.getInstance()
                val currentWeekOfYear = cal[Calendar.WEEK_OF_YEAR]

                if (PreferenceUtil.weekOfYear != currentWeekOfYear) {
                    PreferenceUtil.weekOfYear = currentWeekOfYear

                    if (!PreferenceUtil.updateChecked) {
                        checkForUpdateGithub(context, false)

                        PreferenceUtil.updateChecked = true
                    }
                }
            }

            "monthly" -> {
                val cal = Calendar.getInstance()
                val currentMonthOfYear = cal[Calendar.MONTH]

                if (PreferenceUtil.monthOfYear != currentMonthOfYear) {
                    PreferenceUtil.monthOfYear = currentMonthOfYear

                    if (!PreferenceUtil.updateChecked) {
                        checkForUpdateGithub(context, false)

                        PreferenceUtil.updateChecked = true
                    }
                }
            }
        }
    }

    fun initUpdateSettings() {
        val cal = Calendar.getInstance()
        val currentMonthOfYear = cal[Calendar.MONTH]
        val currentWeekOfYear = cal[Calendar.WEEK_OF_YEAR]
        val currentDayOfYear = cal[Calendar.DAY_OF_YEAR]

        PreferenceUtil.monthOfYear = currentMonthOfYear
        PreferenceUtil.weekOfYear = currentWeekOfYear
        PreferenceUtil.dayOfYear = currentDayOfYear
    }

    fun checkFilesPermission(context: Context): Boolean {
        return if (VersionUtils.hasR()) {
            Environment.isExternalStorageManager()
        }else if (VersionUtils.hasQ()){
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun enableManageAllFiles(context: Context, activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri = Uri.fromParts("package", context.packageName, null)
                intent.data = uri
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }else {
                context.showToast("MANAGE_ALL_FILES Permission Already Granted!")
            }
        } else {
            //below android 11=======
            activity.let { it1 ->
                ActivityCompat.requestPermissions(
                    it1,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 100)
            }
        }
    }
}