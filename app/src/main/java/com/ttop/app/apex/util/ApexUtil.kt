/*
 * Copyright (c) 2019 Hemanth Savarala.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by
 *  the Free Software Foundation either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */
package com.ttop.app.apex.util

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Point
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import com.dcastalia.localappupdate.DownloadApk
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.AppUpdaterUtils
import com.github.javiersantos.appupdater.enums.AppUpdaterError
import com.github.javiersantos.appupdater.enums.Display
import com.github.javiersantos.appupdater.enums.UpdateFrom
import com.github.javiersantos.appupdater.objects.Update
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.ttop.app.apex.App.Companion.getContext
import com.ttop.app.apex.BuildConfig
import com.ttop.app.apex.R
import com.ttop.app.apex.extensions.appendChar
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.extensions.withCenteredButtons
import com.ttop.app.appthemehelper.common.ATHToolbarActivity
import java.net.InetAddress
import java.net.NetworkInterface
import java.text.Collator
import java.text.DecimalFormat
import java.util.*


object ApexUtil {
    private val collator = Collator.getInstance()
    private var a = ""
    fun formatValue(numValue: Float): String {
        var value = numValue
        val arr = arrayOf("", "K", "M", "B", "T", "P", "E")
        var index = 0
        while (value / 1000 >= 1) {
            value /= 1000
            index++
        }
        val decimalFormat = DecimalFormat("#.##")
        return String.format("%s %s", decimalFormat.format(value.toDouble()), arr[index])
    }

    fun frequencyCount(frequency: Int): Float {
        return (frequency / 1000.0).toFloat()
    }

    fun getScreenSize(context: Context): Point {
        val x: Int = context.resources.displayMetrics.widthPixels
        val y: Int = context.resources.displayMetrics.heightPixels
        return Point(x, y)
    }

    val statusBarHeight: Int
        get() {
            var result = 0
            val resourceId = getContext()
                .resources
                .getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = getContext().resources.getDimensionPixelSize(resourceId)
            }
            return result
        }

    val navigationBarHeight: Int
        get() {
            var result = 0
            val resourceId = getContext()
                .resources
                .getIdentifier("navigation_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = getContext().resources.getDimensionPixelSize(resourceId)
            }
            return result
        }

    val isLandscape: Boolean
        get() = (getContext().resources.configuration.orientation
                == Configuration.ORIENTATION_LANDSCAPE)
    val isTablet: Boolean
        get() = (getContext().resources.configuration.smallestScreenWidthDp
                >= 600)

    fun getIpAddress(useIPv4: Boolean): String? {
        try {
            val interfaces: List<NetworkInterface> =
                Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs: List<InetAddress> = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        val sAddr = addr.hostAddress

                        if (sAddr != null) {
                            val isIPv4 = sAddr.indexOf(':') < 0
                            if (useIPv4) {
                                if (isIPv4) return sAddr
                            } else {
                                if (!isIPv4) {
                                    val delim = sAddr.indexOf('%') // drop ip6 zone suffix
                                    return if (delim < 0) {
                                        sAddr.uppercase()
                                    } else {
                                        sAddr.substring(
                                            0,
                                            delim
                                        ).uppercase()
                                    }
                                }
                            }
                        } else {
                            return null
                        }

                    }
                }
            }
        } catch (ignored: Exception) {
        }
        return ""
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun hasBatteryPermission(): Boolean {
        val packageName = getContext().packageName
        val pm = getContext().getSystemService(ATHToolbarActivity.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(packageName)
    }

    fun hasAudioPermission(): Boolean {
        return Settings.System.canWrite(getContext())
    }

    fun enableAudioPermission(context: Context) {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
        intent.data = ("package:" + context.packageName).toUri()
        context.startActivity(intent)
    }

    fun disableBatteryOptimization() {
        val intent = Intent()
        val packageName: String = getContext().packageName

        intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        intent.data = Uri.parse("package:$packageName")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        getContext().startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun enableUnknownSources(context: Context) {
        context.startActivity(
            Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:" + context.packageName)
            )
        )
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun checkUnknownSources(context: Context): Boolean {
        return context.packageManager.canRequestPackageInstalls()
    }

    fun setMargins(view: View, left: Int, top: Int, right: Int, bottom: Int) {
        if (view.layoutParams is ViewGroup.MarginLayoutParams) {
            val p = view.layoutParams as ViewGroup.MarginLayoutParams
            p.setMargins(left, top, right, bottom)
            view.requestLayout()
        }
    }

    fun DpToMargin(dp: Int): Int {
        val marginInDp = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), getContext().resources
                .displayMetrics
        ).toInt()

        return marginInDp
    }

    fun compareIgnoreAccent(s1: String?, s2: String?): Int {
        // Null-proof comparison
        if (s1 == null) {
            return if (s2 == null) 0 else -1
        } else if (s2 == null) {
            return 1
        }
        return collator.compare(s1, s2)
    }

    fun getNameWithoutArticle(title: String?): String {
        if (TextUtils.isEmpty(title)) {
            return ""
        }
        var strippedTitle = title!!.trim { it <= ' ' }
        val articles = listOf(
            "a ", "an ", "the ",  // English ones
            "l'", "le ", "la ", "les " // French ones
        )
        val lowerCaseTitle = strippedTitle.lowercase(Locale.getDefault())
        for (article in articles) {
            if (lowerCaseTitle.startsWith(article)) {
                strippedTitle = strippedTitle.substring(article.length)
                break
            }
        }
        return strippedTitle
    }

    fun updateCollapsableAppBarTitleTextAppearance(collapsingToolbarLayout: CollapsingToolbarLayout){
        //Expanded
        if (PreferenceUtil.isCustomFont == "sans") {
            collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.SansThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "barlow") {
            collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.BarlowThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "jura") {
            collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.JuraThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "pencil") {
            collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.PencilThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "samsung") {
            collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.SamsungThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "ostrich") {
            collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.OstrichThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "capture") {
            collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.CaptureThemeOverlay)
        }

        //Collapsed
        if (PreferenceUtil.isCustomFont == "sans") {
            collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.SansThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "barlow") {
            collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.BarlowThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "jura") {
            collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.JuraThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "pencil") {
            collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.PencilThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "samsung") {
            collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.SamsungThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "ostrich") {
            collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.OstrichThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "capture") {
            collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CaptureThemeOverlay)
        }
    }

    fun checkForUpdateGithub(context: Context,showFailMessage: Boolean) {
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
                    val isPreview = update.latestVersion.endsWith("-preview")

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

    fun checkForUpdateWebsite(context: Context,showFailMessage: Boolean) {
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

    fun checkForUpdateWebsitePreview(context: Context,showFailMessage: Boolean) {
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

    fun searchYoutube(context:Context, title: String, artist: String) {
        if (isNetworkAvailable(context)) {
            if (checkYoutubeMusic(context)) {
                if (PreferenceUtil.isYoutubeMusicSearch) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://music.youtube.com/search?q=$title+$artist"))
                    context.startActivity(intent)
                }else {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=$title+$artist"))
                    context.startActivity(intent)
                }
            }else {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=$title+$artist"))
                context.startActivity(intent)
            }
        }else {
            context.showToast("Internet Disconnected: Unable to Perform Request!")
        }
    }

    fun checkYoutubeMusic(context: Context): Boolean {
        val packages: List<ApplicationInfo>
        val pm: PackageManager = context.packageManager
        packages = pm.getInstalledApplications(0)
        for (packageInfo in packages) {
            if (packageInfo.packageName == "com.google.android.apps.youtube.music")
                return true
        }
        return false
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nw = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            //for other device how are able to connect with Ethernet
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            //for check internet over Bluetooth
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            else -> false
        }
    }
}