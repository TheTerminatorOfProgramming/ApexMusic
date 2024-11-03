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

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Point
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.net.toUri
import com.google.android.material.appbar.MaterialToolbar
import com.ttop.app.apex.App.Companion.getContext
import com.ttop.app.apex.R
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.libraries.appthemehelper.common.ATHToolbarActivity
import java.net.InetAddress
import java.net.NetworkInterface
import java.text.DecimalFormat
import java.util.Collections

object ApexUtil {
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

    fun dpToPixel(dp: Float, context: Context?): Float {
        return if (context != null) {
            val resources = context.resources
            val metrics = resources.displayMetrics
            dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        } else {
            val metrics = Resources.getSystem().displayMetrics
            dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        }
    }

    fun updateSimpleAppBarTitleTextAppearance(
        context: Context,
        simpleToolbarLayout: MaterialToolbar
    ) {
        if (PreferenceUtil.isApexFont) {
            simpleToolbarLayout.setTitleTextAppearance(context, R.style.ApexThemeOverlay)
        }

        when (PreferenceUtil.fontSize) {
            "12" -> {
                simpleToolbarLayout.setTitleTextAppearance(context, R.style.FontSize18)
            }

            "13" -> {
                simpleToolbarLayout.setTitleTextAppearance(context, R.style.FontSize19)
            }

            "14" -> {
                simpleToolbarLayout.setTitleTextAppearance(context, R.style.FontSize20)
            }

            "15" -> {
                simpleToolbarLayout.setTitleTextAppearance(context, R.style.FontSize21)
            }

            "16" -> {
                simpleToolbarLayout.setTitleTextAppearance(context, R.style.FontSize22)
            }

            "17" -> {
                simpleToolbarLayout.setTitleTextAppearance(context, R.style.FontSize23)
            }

            "18" -> {
                simpleToolbarLayout.setTitleTextAppearance(context, R.style.FontSize24)
            }

            "19" -> {
                simpleToolbarLayout.setTitleTextAppearance(context, R.style.FontSize25)
            }

            "20" -> {
                simpleToolbarLayout.setTitleTextAppearance(context, R.style.FontSize26)
            }

            "21" -> {
                simpleToolbarLayout.setTitleTextAppearance(context, R.style.FontSize27)
            }

            "22" -> {
                simpleToolbarLayout.setTitleTextAppearance(context, R.style.FontSize28)
            }

            "23" -> {
                simpleToolbarLayout.setTitleTextAppearance(context, R.style.FontSize29)
            }

            "24" -> {
                simpleToolbarLayout.setTitleTextAppearance(context, R.style.FontSize30)
            }
        }

    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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

    private fun buildBiometricPrompt(context: Context, title: Int): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(title))
            .setSubtitle(context.getString(R.string.biometric_authenticate_subtitle))
            .setDescription(context.getString(R.string.biometric_authenticate_description))
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .build()
    }

    fun checkAndAuthenticate(context: Context, biometricPrompt: BiometricPrompt, title: Int) {
        val biometricManager: BiometricManager = BiometricManager.from(context)
        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS,
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED,
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                val promptInfo = buildBiometricPrompt(context, title)
                biometricPrompt.authenticate(promptInfo)
            }

            else -> context.showToast(context.getString(R.string.md_error_label))
        }
    }
}