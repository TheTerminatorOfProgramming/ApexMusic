package com.ttop.app.apex.libraries.appthemehelper.util

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

/**
 * @author Hemanth S (h4h13).
 */

object VersionUtils {
    /**
     * @return true if device is running API >= 31
     */
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
    @JvmStatic
    fun hasS(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }

    /**
     * @return true if device is running API >= 32
     */
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S_V2)
    @JvmStatic
    fun hasSv2(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2
    }

    /**
     * @return true if device is running API >= 33
     */
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
    @JvmStatic
    fun hasT(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }

    /**
     * @return true if device is running API >= 34
     */
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @JvmStatic
    fun hasUpsideDownCake(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
    }

    /**
     * @return true if device is running API >= 35
     */
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.VANILLA_ICE_CREAM)
    @JvmStatic
    fun hasVanillaIceCream(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM
    }
}
