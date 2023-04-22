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

import android.content.SharedPreferences
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SeekBarPreference
import androidx.preference.TwoStatePreference
import com.ttop.app.apex.*
import com.ttop.app.apex.appshortcuts.DynamicShortcutManager
import com.ttop.app.apex.appwidgets.AppWidgetCircle
import com.ttop.app.apex.appwidgets.AppWidgetClassic
import com.ttop.app.apex.appwidgets.AppWidgetFull
import com.ttop.app.apex.appwidgets.AppWidgetFullCircle
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.service.MusicService
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.appthemehelper.ThemeStore
import com.ttop.app.appthemehelper.util.VersionUtils

/**
 * @author Hemanth S (h4h13).
 */

class NotificationSettingsFragment : AbsSettingsFragment(),
    SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            CLASSIC_NOTIFICATION -> {
                findPreference<Preference>(COLORED_NOTIFICATION)?.isEnabled =
                    sharedPreferences?.getBoolean(key, false)!!
            }
        }
    }

    override fun invalidateSettings() {
        val musicService: MusicService? = MusicPlayerRemote.musicService
        val appWidgetClassic: AppWidgetClassic = AppWidgetClassic.instance
        val appWidgetFull: AppWidgetFull = AppWidgetFull.instance

        val classicNotification: TwoStatePreference? = findPreference(CLASSIC_NOTIFICATION)
        classicNotification?.apply {
            isChecked = PreferenceUtil.isClassicNotification
            setOnPreferenceChangeListener { _, newValue ->
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                // Save preference
                PreferenceUtil.isClassicNotification = newValue as Boolean
                invalidateSettings()
                true
            }
        }

        val coloredNotification: TwoStatePreference? = findPreference(COLORED_NOTIFICATION)
        coloredNotification?.apply {
            isChecked = PreferenceUtil.isColoredNotification
            setOnPreferenceChangeListener { _, newValue ->
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                PreferenceUtil.isColoredNotification = newValue as Boolean
                true
            }
        }

        val update: TwoStatePreference? = findPreference(SHOW_UPDATE)
        /*if (!PreferenceUtil.isAlbumArtOnLockScreen) {
            update?.isChecked = false
            update?.isEnabled = false
            MusicPlayerRemote.updateNotification()
        }*/

        update?.setOnPreferenceChangeListener { _, newValue ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            update.isChecked = newValue as Boolean
            MusicPlayerRemote.updateNotification()
            false
        }

        val colors: TwoStatePreference? = findPreference(WIDGET_COLORS)
        colors?.setOnPreferenceChangeListener { _, newValue ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            colors.isChecked = newValue as Boolean
            appWidgetClassic.notifyThemeChange(musicService)
            appWidgetFull.notifyThemeChange(musicService)
            false
        }

        val transparent: TwoStatePreference? = findPreference(WIDGET_TRANSPERENCY)
        transparent?.setOnPreferenceChangeListener { _, newValue ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            transparent.isChecked = newValue as Boolean
            appWidgetClassic.notifyThemeChange(musicService)
            appWidgetFull.notifyThemeChange(musicService)
            false
        }

        val image: SeekBarPreference? = findPreference(WIDGET_IMAGE)
        image?.setOnPreferenceChangeListener { _, newValue ->
            image.value = newValue as Int
            appWidgetClassic.notifyThemeChange(musicService)
            false
        }

        val imageFull: SeekBarPreference? = findPreference(WIDGET_IMAGE_FULL)
        imageFull?.setOnPreferenceChangeListener { _, newValue ->
            imageFull.value = newValue as Int
            appWidgetFull.notifyThemeChange(musicService)
            false
        }

        val classicShape: TwoStatePreference? = findPreference(CLASSIC_SHAPE)
        classicShape?.setOnPreferenceChangeListener { _, newValue ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            classicShape.isChecked = newValue as Boolean
            appWidgetClassic.notifyThemeChange(musicService)
            false
        }

        val fullShape: TwoStatePreference? = findPreference(FULL_SHAPE)
        fullShape?.setOnPreferenceChangeListener { _, newValue ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            fullShape.isChecked = newValue as Boolean
            appWidgetFull.notifyThemeChange(musicService)
            false
        }

        val textAlignment: Preference? = findPreference(TEXT_ALIGNMENT)
        textAlignment?.setOnPreferenceChangeListener { _, newValue ->
            PreferenceUtil.textAlignment = newValue as String
            appWidgetFull.notifyThemeChange(musicService)
            true
        }

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

        val fullBlur: TwoStatePreference? = findPreference(FULL_BLUR)
        fullBlur?.setOnPreferenceChangeListener { _, newValue ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            fullBlur.isChecked = newValue as Boolean
            appWidgetFull.notifyThemeChange(musicService)
            false
        }

        val classicBlur: TwoStatePreference? = findPreference(CLASSIC_BLUR)
        classicBlur?.setOnPreferenceChangeListener { _, newValue ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            classicBlur.isChecked = newValue as Boolean
            appWidgetClassic.notifyThemeChange(musicService)
            false
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
}
