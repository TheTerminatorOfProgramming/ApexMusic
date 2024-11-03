package com.ttop.app.apex.util

import com.ttop.app.apex.libraries.appintro.AppIntroPageTransformerType
import com.ttop.app.apex.libraries.appthemehelper.util.VersionUtils

object AppIntroUtil {

    fun notificationPermission(): Int {
        return 3
    }

    fun bluetoothPermission(): Int {
        return if (VersionUtils.hasT()) {
            5
        } else {
            4
        }
    }

    fun storagePermission(): Int {
        return if (VersionUtils.hasT()) {
            4
        } else {
            3
        }
    }

    fun transformerType(): AppIntroPageTransformerType {
        return AppIntroPageTransformerType.Parallax()
    }
}