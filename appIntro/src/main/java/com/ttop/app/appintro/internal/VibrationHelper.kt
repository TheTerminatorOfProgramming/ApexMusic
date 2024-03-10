package com.ttop.app.appintro.internal

import android.annotation.SuppressLint
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object VibrationHelper {

    private var vibrator: Vibrator? = null

    private fun initializeVibrator(context: Context) {
        val vibratorManager =
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager

        vibrator = vibratorManager.defaultVibrator
    }

    // You must grant vibration permissions on your AndroidManifest.xml file
    @SuppressLint("MissingPermission")
    fun vibrate(context: Context, vibrateDuration: Long) {
        if (vibrator == null) {
            initializeVibrator(context)
        }
        vibrator?.vibrate(VibrationEffect.createOneShot(vibrateDuration, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}
