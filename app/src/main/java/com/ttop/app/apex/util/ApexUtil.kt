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
import android.content.res.Configuration
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.ttop.app.apex.App.Companion.getContext
import com.ttop.app.appthemehelper.common.ATHToolbarActivity
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

    fun disableBatteryOptimization() {
        val intent = Intent()
        val packageName: String = getContext().packageName

        intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
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
        val articles = java.util.List.of(
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
}