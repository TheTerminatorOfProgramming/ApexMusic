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

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.ttop.app.apex.R
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.helper.MusicPlayerRemote.audioSessionId
import com.ttop.app.apex.ui.activities.DriveModeActivity
import com.ttop.app.apex.ui.activities.LicenseActivity
import com.ttop.app.apex.ui.activities.WhatsNewFragment
import com.ttop.app.equalizer.DialogEqualizerFragment

object NavigationUtil {
    fun goToOpenSource(activity: Activity) {
        activity.startActivity(
            Intent(activity, LicenseActivity::class.java), null
        )
    }

    fun gotoDriveMode(activity: Activity) {
        activity.startActivity(
            Intent(activity, DriveModeActivity::class.java), null
        )
    }

    fun gotoWhatNews(activity: FragmentActivity) {
        val changelogBottomSheet = WhatsNewFragment()
        changelogBottomSheet.show(activity.supportFragmentManager, WhatsNewFragment.TAG)
    }

    fun openEqualizer(activity: Activity, fragmentManager: FragmentManager, title: String) {
        if (PreferenceUtil.isStockEqualizer) {
            stockEqualizer(activity)
        }else {
            integratedEqualizer(activity, fragmentManager, title)
        }
    }
    private fun integratedEqualizer(activity: Activity, fragmentManager: FragmentManager, title: String) {
        val fragment = DialogEqualizerFragment.newBuilder()
            .setAudioSessionId(MusicPlayerRemote.audioSessionId)
            .themeColor(ContextCompat.getColor(activity, R.color.md_blue_500))
            .textColor(ContextCompat.getColor(activity, R.color.md_white_1000))
            .title(title)
            .setAccentColor(activity.accentColor())
            .build()
        fragment.show(fragmentManager, "eq")
    }

    private fun stockEqualizer(activity: Activity) {
        val sessionId = audioSessionId
        if (sessionId == AudioEffect.ERROR_BAD_VALUE) {
            activity.showToast(R.string.no_audio_ID, Toast.LENGTH_LONG)
        } else {
            try {
                val effects = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                effects.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, sessionId)
                effects.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                activity.startActivityForResult(effects, 0)
            } catch (notFound: ActivityNotFoundException) {
                return
            }
        }
    }
}