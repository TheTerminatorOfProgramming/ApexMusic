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
import android.annotation.SuppressLint
import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Context.KEYGUARD_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Point
import android.media.audiofx.AudioEffect
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.provider.Settings
import android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.google.android.datatransport.runtime.scheduling.jobscheduling.SchedulerConfig.Flag
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.ttop.app.apex.App.Companion.getContext
import com.ttop.app.apex.R
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.appthemehelper.common.ATHToolbarActivity
import com.ttop.app.appthemehelper.util.VersionUtils
import java.net.InetAddress
import java.net.NetworkInterface
import java.text.Collator
import java.text.DecimalFormat
import java.util.*


object ApexUtil {
    private val collator = Collator.getInstance()
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

    val navigationBarHeight: Int
        @SuppressLint("InternalInsetResource", "DiscouragedApi")
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

    @SuppressLint("BatteryLife")
    fun disableBatteryOptimization() {
        val intent = Intent()
        val packageName: String = getContext().packageName

        intent.action = ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        intent.data = Uri.parse("package:$packageName")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        getContext().startActivity(intent)
    }

    fun setMargins(view: View, left: Int, top: Int, right: Int, bottom: Int) {
        if (view.layoutParams is ViewGroup.MarginLayoutParams) {
            val p = view.layoutParams as ViewGroup.MarginLayoutParams
            p.setMargins(left, top, right, bottom)
            view.requestLayout()
        }
    }

    fun dpToMargin(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), getContext().resources
                .displayMetrics
        ).toInt()
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

        if (PreferenceUtil.isCustomFont == "caviar") {
            if (PreferenceUtil.isCustomFontBold) {
                simpleToolbarLayout.setTitleTextAppearance(context, R.style.CaviarDreamsBoldThemeOverlay)
            } else {
                simpleToolbarLayout.setTitleTextAppearance(context, R.style.CaviarDreamsThemeOverlay)
            }
        }

        if (PreferenceUtil.isCustomFont == "pencil") {
           simpleToolbarLayout.setTitleTextAppearance(context, R.style.PencilThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "drexs") {
           simpleToolbarLayout.setTitleTextAppearance(context, R.style.DrexsThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "hermanoalto") {
           simpleToolbarLayout.setTitleTextAppearance(context, R.style.HermanoaltoThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "nothing") {
           simpleToolbarLayout.setTitleTextAppearance(context, R.style.NothingThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "binjay") {
            simpleToolbarLayout.setTitleTextAppearance(context, R.style.BinjayTitleThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "hiatus") {
            simpleToolbarLayout.setTitleTextAppearance(context, R.style.HiatusTitleThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "apex") {
            simpleToolbarLayout.setTitleTextAppearance(context, R.style.ApexThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "neue") {
            simpleToolbarLayout.setTitleTextAppearance(context, R.style.NeueBureauThemeOverlay)
        }
    }

    fun updateCollapsableAppBarTitleTextAppearance(collapsingToolbarLayout: CollapsingToolbarLayout){
        //Expanded
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

        if (PreferenceUtil.isCustomFont == "caviar") {
            if (PreferenceUtil.isCustomFontBold) {
                collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.CaviarDreamsBoldThemeOverlay)
            } else {
                collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.CaviarDreamsThemeOverlay)
            }
        }

        if (PreferenceUtil.isCustomFont == "pencil") {
            collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.PencilThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "drexs") {
            collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.DrexsThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "hermanoalto") {
            collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.HermanoaltoThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "nothing") {
            collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.NothingThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "binjay") {
            collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.BinjayTitleThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "hiatus") {
            collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.HiatusTitleThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "apex") {
            collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ApexThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "neue") {
            collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.NeueBureauThemeOverlay)
        }
        //Collapsed
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

        if (PreferenceUtil.isCustomFont == "caviar") {
            if (PreferenceUtil.isCustomFontBold) {
                collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CaviarDreamsBoldThemeOverlay)
            } else {
                collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CaviarDreamsThemeOverlay)
            }
        }

        if (PreferenceUtil.isCustomFont == "pencil") {
            collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.PencilThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "drexs") {
            collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.DrexsThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "hermanoalto") {
            collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.HermanoaltoThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "nothing") {
            collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.NothingThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "binjay") {
             collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.BinjayTitleThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "hiatus") {
             collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.HiatusTitleThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "apex") {
            collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.ApexThemeOverlay)
        }

        if (PreferenceUtil.isCustomFont == "neue") {
            collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.NeueBureauThemeOverlay)
        }
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
        return canAuthenticate
    }

    fun hasEqualizer(context: Context): Boolean {
        val effects = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)

        val pm = context.packageManager
        val ri = pm.resolveActivity(effects, 0)
        return ri != null
    }
}