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

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Point
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Environment
import android.os.PowerManager
import android.os.VibratorManager
import android.provider.Settings
import android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.Display
import com.github.javiersantos.appupdater.enums.UpdateFrom
import com.github.javiersantos.appupdater.objects.GitHub
import com.google.android.material.appbar.MaterialToolbar
import com.ttop.app.apex.App.Companion.getContext
import com.ttop.app.apex.R
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.libraries.appthemehelper.common.ATHToolbarActivity
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.MalformedURLException
import java.net.NetworkInterface
import java.net.URL
import java.text.DecimalFormat
import java.util.Collections
import java.util.Locale


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

    fun pixelToDp(dp: Int, context: Context?): Float {
        return if (context != null) {
            val resources = context.resources
            val metrics = resources.displayMetrics

            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp.toFloat(),
                metrics)
        } else {
            val metrics = Resources.getSystem().displayMetrics

            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp.toFloat(),
                metrics)
        }
    }

    fun updateSimpleAppBarTitleTextAppearance(
        context: Context,
        simpleToolbarLayout: MaterialToolbar
    ) {
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

    fun isDeviceSecured(context: Context): Boolean {
        val manager: KeyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return manager.isDeviceSecure
    }

    fun fadeAnimator(toBeShown: View, toBeHidden: View, animationDuration: Int, tobeShownVisibleCode: Boolean, showListener: Boolean) {
        toBeShown.apply {
            // Set the content view to 0% opacity but visible, so that it is
            // visible but fully transparent during the animation.
            alpha = 0f
            if (tobeShownVisibleCode) {
                visibility = View.VISIBLE
            }

            // Animate the content view to 100% opacity and clear any animation
            // listener set on the view.
            animate()
                .alpha(1f)
                .setDuration(animationDuration.toLong())
                .setListener(null)
        }
        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step so it doesn't
        // participate in layout passes.

        if (showListener) {
            toBeHidden.animate()
                .alpha(0f)
                .setDuration(animationDuration.toLong())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        toBeHidden.visibility = View.GONE
                    }
                })
        }else {
            toBeHidden.animate()
                .alpha(0f)
                .setDuration(animationDuration.toLong())
        }
    }

    fun canVibrate(context: Context): Boolean {
        val mVibrator = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager?

        return mVibrator!!.defaultVibrator.hasVibrator()
    }

    private fun getUpdateURL(): URL {
        try {
            return URL("https://github.com/" + "TheTerminatorOfProgramming" + "/" + "ApexMusic" + "/releases/latest")
        } catch (e: MalformedURLException) {
            throw RuntimeException(e)
        }
    }

    private fun getLatestAppVersion(
    ): String {
        var isAvailable = false
        var source = ""
        val client = OkHttpClient()
        val url = getUpdateURL()
        val request: Request = Request.Builder()
            .url(url)
            .build()
        var body: ResponseBody? = null

        try {
            val response = client.newCall(request).execute()
            body = response.body
            val reader = BufferedReader(InputStreamReader(body.byteStream(), "UTF-8"))
            val str = StringBuilder()

            var line: String
            while ((reader.readLine().also { line = it }) != null) {
                if (line.contains("/tree/")) {
                    str.append(line)
                    isAvailable = true
                }
            }

            if (str.isEmpty()) {
                Log.e("AppUpdater", "Cannot retrieve latest version. Is it configured properly?")
            }

            response.body.close()
            source = str.toString()
        } catch (e: FileNotFoundException) {
            Log.e("AppUpdater", "App wasn't found in the provided source. Is it published?")
        } catch (ignore: IOException) {
        } finally {
            body?.close()
        }

        var version = "0.0.0.0"
        var splitGitHub: Array<String> =
            source.split("/tree/".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
        if (splitGitHub.size > 1) {
            splitGitHub =
                splitGitHub[1].split("(\")".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            version = splitGitHub[0].trim { it <= ' ' }
            if (version.startsWith("v")) { // Some repo uses vX.X.X
                splitGitHub = version.split("(v)".toRegex(), limit = 2).toTypedArray()
                version = splitGitHub[1].trim { it <= ' ' }
            }
        }

        return version
    }

    fun checkForUpdates(context: Context, update: Boolean, autoCheck: Boolean) {
        val appUpdater = AppUpdater(context)

        if (update) {
            if (autoCheck) {
                appUpdater
                    .setUpdateFrom(UpdateFrom.GITHUB)
                    .setGitHubUserAndRepo("TheTerminatorOfProgramming", "ApexMusic")
                    .setDisplay(Display.DIALOG)
                    .setContentOnUpdateAvailable(String.format(context.getString(R.string.appupdater_update_available_description), getLatestAppVersion() ,context.getString(R.string.name)) + "\n\n" + context.getString(R.string.appupdater_update_disable))
                    .setContentOnUpdateNotAvailable(String.format(context.getString(R.string.appupdater_update_not_available_description), context.getString(R.string.name)) + "\n\n" + context.getString(R.string.appupdater_update_disable))
                    .showAppUpdated(true)
                    .start()
            }else {
                appUpdater
                    .setUpdateFrom(UpdateFrom.GITHUB)
                    .setGitHubUserAndRepo("TheTerminatorOfProgramming", "ApexMusic")
                    .setDisplay(Display.DIALOG)
                    .showAppUpdated(true)
                    .start()
            }
        }
    }
}