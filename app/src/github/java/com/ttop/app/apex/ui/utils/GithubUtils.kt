package com.ttop.app.apex.ui.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
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

    fun checkForUpdateWebsite(context: Context, showFailMessage: Boolean) {
        val updater = AppUpdaterUtils(context)
            .setUpdateFrom(UpdateFrom.XML)
            .setUpdateXML("https://raw.githubusercontent.com/TheTerminatorOfProgramming/TheTerminatorOfProgramming.github.io/main/download/apex_latest_version.xml")
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

                    val updateVersion = update.latestVersion.toDouble()

                    if (updateVersion > finalVersionNumber) {
                        val builder = AlertDialog.Builder(context)
                            .setTitle("Update Available!")
                            .setMessage("Update " + update.latestVersion + " is available to download!\n\n" + "Do you want to download and install this update now?")
                            .setCancelable(false)
                            .setPositiveButton(R.string.yes) { _, _ ->
                                val downloadApk = DownloadApk(context)
                                // With standard fileName 'App Update.apk'
                                //downloadApk.startDownloadingApk(update.urlToDownload.toString())
                                //With Custom fileName
                                downloadApk.startDownloadingApk(update.urlToDownload.toString(),"Apex Music " + update.latestVersion + ".apk")
                            }

                            .setNegativeButton(R.string.no) { _, _ ->
                            }
                        builder.show().withCenteredButtons()
                    }else {
                        if (showFailMessage) {
                            context.showToast("No Updates Available!")
                        }
                    }
                }

                override fun onFailed(error: AppUpdaterError) {
                    Log.d("AppUpdater Error", "Something went wrong")
                }
            })
        updater.start()
    }

    fun checkForUpdateWebsitePreview(context: Context, showFailMessage: Boolean) {
        val updater = AppUpdaterUtils(context)
            .setUpdateFrom(UpdateFrom.XML)
            .setUpdateXML("https://raw.githubusercontent.com/TheTerminatorOfProgramming/TheTerminatorOfProgramming.github.io/main/download/apex_latest_version_preview.xml")
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

                    val updateVersion = update.latestVersion.toDouble()

                    if (updateVersion > finalVersionNumber) {
                        val builder = AlertDialog.Builder(context)
                            .setTitle("Update Available!")
                            .setMessage("Update " + update.latestVersion + " is available to download!\n\n" + "Do you want to download and install this update now?")
                            .setCancelable(false)
                            .setPositiveButton(R.string.yes) { _, _ ->
                                val downloadApk = DownloadApk(context)
                                // With standard fileName 'App Update.apk'
                                //downloadApk.startDownloadingApk(update.urlToDownload.toString())
                                //With Custom fileName
                                downloadApk.startDownloadingApk(update.urlToDownload.toString(),"Apex Music " + update.latestVersion + ".apk")
                            }

                            .setNegativeButton(R.string.no) { _, _ ->
                            }
                        builder.show().withCenteredButtons()
                    }else {
                        if (showFailMessage){
                            context.showToast("No Updates Available!")
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
                    if (PreferenceUtil.updateSource == "github") {
                        checkForUpdateGithub(context, false)
                    }else {
                        if (PreferenceUtil.isPreviewChannel) {
                            checkForUpdateWebsitePreview(context, false)
                        }else {
                            checkForUpdateWebsite(context, false)
                        }
                    }

                    PreferenceUtil.updateChecked = true
                }
            }

            "daily" -> {
                val cal = Calendar.getInstance()
                val currentDayOfYear = cal[Calendar.DAY_OF_YEAR]

                if (PreferenceUtil.dayOfYear != currentDayOfYear) {
                    PreferenceUtil.dayOfYear = currentDayOfYear

                    if (!PreferenceUtil.updateChecked) {
                        if (PreferenceUtil.updateSource == "github") {
                            checkForUpdateGithub(context, false)
                        }else {
                            if (PreferenceUtil.isPreviewChannel) {
                                checkForUpdateWebsitePreview(context, false)
                            }else {
                                checkForUpdateWebsite(context, false)
                            }
                        }
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
                        if (PreferenceUtil.updateSource == "github") {
                            checkForUpdateGithub(context, false)
                        }else {
                            if (PreferenceUtil.isPreviewChannel) {
                                checkForUpdateWebsitePreview(context, false)
                            }else {
                                checkForUpdateWebsite(context, false)
                            }
                        }
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
                        if (PreferenceUtil.updateSource == "github") {
                            checkForUpdateGithub(context, false)
                        }else {
                            if (PreferenceUtil.isPreviewChannel) {
                                checkForUpdateWebsitePreview(context, false)
                            }else {
                                checkForUpdateWebsite(context, false)
                            }
                        }
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
}