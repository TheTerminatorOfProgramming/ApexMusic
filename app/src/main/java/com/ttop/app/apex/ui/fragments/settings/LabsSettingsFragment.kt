/*
 * Copyright (c) 2020 Hemanth Savarla.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 */
package com.ttop.app.apex.ui.fragments.settings

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.preference.TwoStatePreference
import com.ttop.app.apex.*
import com.ttop.app.apex.appwidgets.*
import com.ttop.app.apex.util.PreferenceUtil


/**
 * @author Hemanth S (h4h13).
 */

class LabsSettingsFragment : AbsSettingsFragment() {

    override fun invalidateSettings() {
        val disableWidgets: TwoStatePreference? = findPreference(DISABLE_WIDGETS)
        disableWidgets?.isChecked = PreferenceUtil.isDisableWidgets
        disableWidgets?.setOnPreferenceChangeListener { _, newValue ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

            val value = newValue as Boolean

            bigWidgetState(requireContext(), value)

            classicWidgetState(requireContext(), value)

            circleWidgetState(requireContext(), value)

            fullWidgetState(requireContext(), value)

            true
        }

        val transparentMiniPlayer: TwoStatePreference? = findPreference(TRANSPARENT_MINI_PLAYER)
        transparentMiniPlayer?.isChecked = PreferenceUtil.isMiniPlayerTransparent
        transparentMiniPlayer?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            restartActivity()
            true
        }

        val lyricsMessages: TwoStatePreference? = findPreference(DISABLE_MESSAGE_LYRICS)
        lyricsMessages?.isChecked = PreferenceUtil.isLyricsMessageDisabled
        lyricsMessages?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            true
        }

        val simpleMode: TwoStatePreference? = findPreference(SIMPLE_MODE)
        simpleMode?.isChecked = PreferenceUtil.isSimpleMode
        simpleMode?.setOnPreferenceChangeListener { _, newValue ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            val value = newValue as Boolean

            if (value) {
                //General
                PreferenceUtil.isCustomFont = "default"
                PreferenceUtil.isSwipe = "off"
                PreferenceUtil.isExtraControls = false
                //Now Playing
                PreferenceUtil.isPlayerBackgroundType = false
                PreferenceUtil.isColorAnimate = false
                PreferenceUtil.swipeAnywhereToChangeSong = "off"
                PreferenceUtil.isExpandPanel = "disabled"
                PreferenceUtil.isCarouselEffect = false
                PreferenceUtil.lyricsScreenOn = false
                PreferenceUtil.lyricsMode = "disabled"
                //Personalize
                PreferenceUtil.rememberLastTab = false
                PreferenceUtil.tabTitleMode = 1
                PreferenceUtil.scrollbarStyle = "auto_hide"
                //Audio
                PreferenceUtil.isAutoplay = false
                PreferenceUtil.isBluetoothSpeaker = false
                PreferenceUtil.specificDevice = false
                PreferenceUtil.bluetoothDevice = ""
                //Advanced
                PreferenceUtil.isNotificationActionsOnAuto = true
                PreferenceUtil.searchActionShuffle = false
                PreferenceUtil.isVoiceSearch = false
                PreferenceUtil.isScreenOnEnabled = false
                PreferenceUtil.isAutoRotate = false
            }
            true
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_labs)
    }

    private fun bigWidgetState(context: Context, state: Boolean) {
        val pm: PackageManager = context.packageManager

        val newState = if (state) {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }else {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        }

        pm.setComponentEnabledSetting(
            ComponentName(
                context,
                AppWidgetBig::class.java
            ), newState, PackageManager.DONT_KILL_APP
        )
    }

    private fun classicWidgetState(context: Context, state: Boolean) {
        val pm: PackageManager = context.packageManager

        val newState = if (state) {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }else {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        }

        pm.setComponentEnabledSetting(
            ComponentName(
                context,
                AppWidgetClassic::class.java
            ), newState, PackageManager.DONT_KILL_APP
        )
    }

    private fun circleWidgetState(context: Context, state: Boolean) {
        val pm: PackageManager = context.packageManager

        val newState = if (state) {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }else {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        }

        pm.setComponentEnabledSetting(
            ComponentName(
                context,
                AppWidgetCircle::class.java
            ), newState, PackageManager.DONT_KILL_APP
        )
    }

    private fun fullWidgetState(context: Context, state: Boolean) {
        val pm: PackageManager = context.packageManager

        val newState = if (state) {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }else {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED

        }

        pm.setComponentEnabledSetting(
            ComponentName(
                context,
                AppWidgetFull::class.java
            ), newState, PackageManager.DONT_KILL_APP
        )
    }
}
