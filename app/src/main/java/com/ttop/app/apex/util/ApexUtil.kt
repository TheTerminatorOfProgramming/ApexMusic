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

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.res.Configuration
import android.graphics.Point
import android.os.Build
import android.os.Environment
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.getSystemService
import com.ttop.app.apex.App.Companion.getContext
import com.ttop.app.apex.R
import java.io.File
import java.net.InetAddress
import java.net.NetworkInterface
import java.text.DecimalFormat
import java.util.*


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

    fun createFolderStructure() {
        val backupRoot = getBackupRoot()
        val lrcRoot = getLrcRoot()
        if (!backupRoot.exists()) {
            backupRoot.mkdirs()
        }

        if (!lrcRoot.exists()) {
            lrcRoot.mkdirs()
        }
    }

    private fun getBackupRoot(): File {
        return File(
            getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "Apex/Backups"
        )
    }

    private fun getLrcRoot(): File {
        return File(
            getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "Apex/LRC"
        )
    }

    fun createNotification(ID: Int, ChannelID: String) : Notification{
        //CREATE NOTIFICATION
        val builder = NotificationCompat.Builder(getContext(), ChannelID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Foreground Notification")
            .setContentText("This Notification keeps the Service Alive for the Bluetooth AutoPlay Feature")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)

        //CREATE CHANNEL
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Bluetooth Foreground Notification"
            val descriptionText = "Foreground Notification"
            val importance = NotificationManager.IMPORTANCE_LOW
            val mChannel = NotificationChannel(ChannelID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getContext().getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }

        return builder.build()
    }
}