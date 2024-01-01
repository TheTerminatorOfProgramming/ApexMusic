package com.ttop.app.apex.util

import com.ttop.app.appthemehelper.util.VersionUtils

object AppIntroPermissions {

    fun notificationPermission(): Int {
        return 3
    }

    fun bluetoothPermission(): Int {
        return if(VersionUtils.hasT()) {
            5
        }else {
            4
        }
    }

    fun storagePermission(): Int {
        return if(VersionUtils.hasT()) {
            4
        }else {
            3
        }
    }
}