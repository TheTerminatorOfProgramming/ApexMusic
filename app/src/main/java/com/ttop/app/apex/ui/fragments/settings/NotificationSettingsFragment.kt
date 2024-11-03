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
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.preference.Preference
import androidx.preference.TwoStatePreference
import com.ttop.app.apex.DISABLE_UPDATE
import com.ttop.app.apex.DISABLE_WIDGETS
import com.ttop.app.apex.NOTIFICATION_ACTION_1
import com.ttop.app.apex.NOTIFICATION_ACTION_2
import com.ttop.app.apex.R
import com.ttop.app.apex.SQUIRCLE_ART
import com.ttop.app.apex.WIDGET_STYLE
import com.ttop.app.apex.appwidgets.AppWidgetBig
import com.ttop.app.apex.appwidgets.AppWidgetCircle
import com.ttop.app.apex.appwidgets.AppWidgetClassic
import com.ttop.app.apex.appwidgets.AppWidgetFull
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.service.MusicService
import com.ttop.app.apex.util.PreferenceUtil

/**
 * @author Hemanth S (h4h13).
 */

class NotificationSettingsFragment : AbsSettingsFragment(),
    SharedPreferences.OnSharedPreferenceChangeListener {
    private val musicService: MusicService? = MusicPlayerRemote.musicService
    private val appWidgetBig: AppWidgetBig = AppWidgetBig.instance
    private val appWidgetClassic: AppWidgetClassic = AppWidgetClassic.instance
    private val appWidgetFull: AppWidgetFull = AppWidgetFull.instance
    private val appWidgetCircle: AppWidgetCircle = AppWidgetCircle.instance

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            WIDGET_STYLE, SQUIRCLE_ART -> {
                appWidgetBig.notifyThemeChange(musicService)
                appWidgetClassic.notifyThemeChange(musicService)
                appWidgetFull.notifyThemeChange(musicService)
                appWidgetCircle.notifyThemeChange(musicService)
            }
        }
    }

    override fun invalidateSettings() {
        val action1: Preference? = findPreference(NOTIFICATION_ACTION_1)
        action1?.let {
            setSummary(it)
            it.setOnPreferenceChangeListener { _, newValue ->
                setSummary(it, newValue)
                true
            }
        }

        val action2: Preference? = findPreference(NOTIFICATION_ACTION_2)
        action2?.let {
            setSummary(it)
            it.setOnPreferenceChangeListener { _, newValue ->
                setSummary(it, newValue)
                true
            }
        }

        val disableUpdate: TwoStatePreference? = findPreference(DISABLE_UPDATE)
        disableUpdate?.setOnPreferenceChangeListener { _, _ ->
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }

            appWidgetClassic.notifyThemeChange(musicService)
            appWidgetBig.notifyThemeChange(musicService)
            appWidgetFull.notifyThemeChange(musicService)
            true
        }

        val widgetBackground: TwoStatePreference? = findPreference(WIDGET_STYLE)
        widgetBackground?.setOnPreferenceChangeListener { _, _ ->
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            true
        }

        val disableWidgets: TwoStatePreference? = findPreference(DISABLE_WIDGETS)
        disableWidgets?.isChecked = PreferenceUtil.isDisableWidgets
        disableWidgets?.setOnPreferenceChangeListener { _, newValue ->
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }

            bigWidgetState(requireContext(), newValue as Boolean)
            classicWidgetState(requireContext(), newValue)
            circleWidgetState(requireContext(), newValue)
            fullWidgetState(requireContext(), newValue)

            true
        }

        val squircleAlbumArt: TwoStatePreference? = findPreference(SQUIRCLE_ART)
        squircleAlbumArt?.isChecked = PreferenceUtil.isAlbumArtSquircle
        squircleAlbumArt?.setOnPreferenceChangeListener { _, _ ->
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            true
        }
    }

    override fun onResume() {
        super.onResume()
        PreferenceUtil.registerOnSharedPreferenceChangedListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        PreferenceUtil.unregisterOnSharedPreferenceChangedListener(this)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_notification)
    }

    private fun bigWidgetState(context: Context, state: Boolean) {
        val pm: PackageManager = context.packageManager

        val newState = if (state) {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        } else {
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
        } else {
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
        } else {
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
        } else {
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
