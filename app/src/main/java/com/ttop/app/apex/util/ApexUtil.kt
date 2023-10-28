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

import android.Manifest
import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Context.KEYGUARD_SERVICE
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Point
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import com.dcastalia.localappupdate.DownloadApk
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.AppUpdaterUtils
import com.ttop.app.apex.BuildConfig
import com.github.javiersantos.appupdater.enums.AppUpdaterError
import com.github.javiersantos.appupdater.enums.Display
import com.github.javiersantos.appupdater.enums.UpdateFrom
import com.github.javiersantos.appupdater.objects.Update
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.ttop.app.apex.App.Companion.getContext
import com.ttop.app.apex.R
import com.ttop.app.apex.extensions.appendChar
import com.ttop.app.apex.extensions.backgroundTintList
import com.ttop.app.apex.extensions.darkAccentColor
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

    @RequiresApi(Build.VERSION_CODES.R)
    fun isFoldable(context: Context): Boolean {
        val pm = context.packageManager

        return pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_HINGE_ANGLE)
    }

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

    fun updateSimpleAppBarTitleTextAppearance(context: Context, simpleToolbarLayout: MaterialToolbar){
        if (PreferenceUtil.isCustomFont == "sans") {
            if (PreferenceUtil.isCustomFontBold) {
               simpleToolbarLayout.setTitleTextAppearance(context, R.style.SansBoldThemeOverlay)
            } else {
               simpleToolbarLayout.setTitleTextAppearance(context, R.style.SansThemeOverlay)
            }
        }

        if (PreferenceUtil.isCustomFont == "barlow") {
            if (PreferenceUtil.isCustomFontBold) {
               simpleToolbarLayout.setTitleTextAppearance(context, R.style.BarlowBoldThemeOverlay)
            } else {
               simpleToolbarLayout.setTitleTextAppearance(context, R.style.BarlowThemeOverlay)
            }
        }

        if (PreferenceUtil.isCustomFont == "jura") {
            if (PreferenceUtil.isCustomFontBold) {
               simpleToolbarLayout.setTitleTextAppearance(context, R.style.JuraBoldThemeOverlay)
            } else {
               simpleToolbarLayout.setTitleTextAppearance(context, R.style.JuraThemeOverlay)
            }
        }

        if (PreferenceUtil.isCustomFont == "pencil") {
           simpleToolbarLayout.setTitleTextAppearance(context, R.style.PencilThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "samsung") {
            if (PreferenceUtil.isCustomFontBold) {
               simpleToolbarLayout.setTitleTextAppearance(context, R.style.SamsungBoldThemeOverlay)
            } else {
               simpleToolbarLayout.setTitleTextAppearance(context, R.style.SamsungThemeOverlay)
            }
        }

        if (PreferenceUtil.isCustomFont == "drexs") {
           simpleToolbarLayout.setTitleTextAppearance(context, R.style.DrexsThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "hermanoalto") {
           simpleToolbarLayout.setTitleTextAppearance(context, R.style.HermanoaltoThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "oneui") {
            if (PreferenceUtil.isCustomFontBold) {
               simpleToolbarLayout.setTitleTextAppearance(context, R.style.OneUiBoldThemeOverlay)
            } else {
               simpleToolbarLayout.setTitleTextAppearance(context, R.style.OneUiThemeOverlay)
            }
        }

        if (PreferenceUtil.isCustomFont == "nothing") {
           simpleToolbarLayout.setTitleTextAppearance(context, R.style.NothingThemeOverlay)
        }
    }

    fun updateCollapsableAppBarTitleTextAppearance(collapsingToolbarLayout: CollapsingToolbarLayout){
        //Expanded
        if (PreferenceUtil.isCustomFont == "sans") {
            if (PreferenceUtil.isCustomFontBold) {
                collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.SansBoldThemeOverlay)
            } else {
                collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.SansThemeOverlay)
            }
        }

        if (PreferenceUtil.isCustomFont == "barlow") {
            if (PreferenceUtil.isCustomFontBold) {
                collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.BarlowBoldThemeOverlay)
            } else {
                collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.BarlowThemeOverlay)
            }
        }

        if (PreferenceUtil.isCustomFont == "jura") {
            if (PreferenceUtil.isCustomFontBold) {
                collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.JuraBoldThemeOverlay)
            } else {
                collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.JuraThemeOverlay)
            }
        }

        if (PreferenceUtil.isCustomFont == "pencil") {
            collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.PencilThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "samsung") {
            if (PreferenceUtil.isCustomFontBold) {
                collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.SamsungBoldThemeOverlay)
            } else {
                collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.SamsungThemeOverlay)
            }
        }

        if (PreferenceUtil.isCustomFont == "drexs") {
            collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.DrexsThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "hermanoalto") {
            collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.HermanoaltoThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "oneui") {
            if (PreferenceUtil.isCustomFontBold) {
                collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.OneUiBoldThemeOverlay)
            } else {
                collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.OneUiThemeOverlay)
            }
        }

        if (PreferenceUtil.isCustomFont == "nothing") {
            collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.NothingThemeOverlay)
        }
        //Collapsed
        if (PreferenceUtil.isCustomFont == "sans") {
            if (PreferenceUtil.isCustomFontBold) {
                collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.SansBoldThemeOverlay)
            } else {
                collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.SansThemeOverlay)
            }
        }

        if (PreferenceUtil.isCustomFont == "barlow") {
            if (PreferenceUtil.isCustomFontBold) {
                collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.BarlowBoldThemeOverlay)
            } else {
                collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.BarlowThemeOverlay)
            }
        }

        if (PreferenceUtil.isCustomFont == "jura") {
            if (PreferenceUtil.isCustomFontBold) {
                collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.JuraBoldThemeOverlay)
            } else {
                collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.JuraThemeOverlay)
            }
        }

        if (PreferenceUtil.isCustomFont == "pencil") {
            collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.PencilThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "samsung") {
            if (PreferenceUtil.isCustomFontBold) {
                collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.SamsungBoldThemeOverlay)
            } else {
                collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.SamsungThemeOverlay)
            }
        }

        if (PreferenceUtil.isCustomFont == "drexs") {
            collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.DrexsThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "hermanoalto") {
            collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.HermanoaltoThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "oneui") {
            if (PreferenceUtil.isCustomFontBold) {
                collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.OneUiBoldThemeOverlay)
            } else {
                collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.OneUiThemeOverlay)
            }
        }

        if (PreferenceUtil.isCustomFont == "nothing") {
            collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.NothingThemeOverlay)
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

    fun Context.getCatchyUsername(): String {
        // This will only work for English locales, I don't want to localize this
        return if (Locale.getDefault().toLanguageTag().contains("en")) {
            arrayOf(
                "The Unnamed",
                "The Unknown",
                "The Mysterious",
                "The Unrevealed",
                "The Nameless",
                "The Unspeakable",
                "The Unmentionable",
            ).random().apply { logD("Username $this") }
        } else {
            logD("username here")
            getString(R.string.user_name)
        }
    }

    fun checkForBiometrics(context: Context) : Boolean{
        var canAuthenticate = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT < 29) {
                val keyguardManager : KeyguardManager = context.getSystemService(KEYGUARD_SERVICE) as KeyguardManager
                val packageManager : PackageManager = context.packageManager
                if (!packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
                    canAuthenticate = false
                }
                if (!keyguardManager.isKeyguardSecure) {
                    canAuthenticate = false
                }
            } else if (Build.VERSION.SDK_INT == 29){
                val biometricManager = BiometricManager.from(context)
                if (biometricManager.canAuthenticate() != BIOMETRIC_SUCCESS){
                    canAuthenticate = false
                }
            }
            else if (Build.VERSION.SDK_INT >= 30){
                val biometricManager = BiometricManager.from(context)
                val authenticationTypes = BIOMETRIC_WEAK or DEVICE_CREDENTIAL
                val authenticate = biometricManager.canAuthenticate(authenticationTypes)
                if (authenticate != BIOMETRIC_SUCCESS) {
                    canAuthenticate = false
                }
            }
        }else{
            canAuthenticate = false
        }
        return canAuthenticate
    }

    fun enableManageAllFiles(context: Context, activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri = Uri.fromParts("package", context.packageName, null)
                intent.data = uri
                context.startActivity(intent)
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