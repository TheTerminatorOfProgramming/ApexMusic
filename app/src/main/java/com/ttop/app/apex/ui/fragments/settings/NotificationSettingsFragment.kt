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
import com.afollestad.materialdialogs.color.colorChooser
import com.ttop.app.apex.*
import com.ttop.app.apex.appshortcuts.DynamicShortcutManager
import com.ttop.app.apex.appwidgets.AppWidgetCircle
import com.ttop.app.apex.appwidgets.AppWidgetClassic
import com.ttop.app.apex.appwidgets.AppWidgetFull
import com.ttop.app.apex.extensions.materialDialog
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.service.MusicService
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.appthemehelper.ACCENT_COLORS
import com.ttop.app.appthemehelper.ACCENT_COLORS_SUB
import com.ttop.app.appthemehelper.common.prefs.supportv7.ATEColorPreference
import com.ttop.app.appthemehelper.common.prefs.supportv7.ATEListPreference
import com.ttop.app.appthemehelper.util.ColorUtil
import com.ttop.app.appthemehelper.util.VersionUtils

/**
 * @author Hemanth S (h4h13).
 */

class NotificationSettingsFragment : AbsSettingsFragment(),
    SharedPreferences.OnSharedPreferenceChangeListener {
    val musicService: MusicService? = MusicPlayerRemote.musicService
    private val appWidgetClassic: AppWidgetClassic = AppWidgetClassic.instance
    private val appWidgetFull: AppWidgetFull = AppWidgetFull.instance
    private val appWidgetCircle: AppWidgetCircle = AppWidgetCircle.instance

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            CLASSIC_NOTIFICATION -> {
                findPreference<Preference>(COLORED_NOTIFICATION)?.isEnabled =
                    sharedPreferences?.getBoolean(key, false)!!
            }
            WIDGET_BUTTON_COLOR -> {
                val customWidgetColor: ATEColorPreference? = findPreference(WIDGET_CUSTOM_COLOR)
                customWidgetColor?.isEnabled = PreferenceUtil.buttonColorOnWidgets == "custom"

                appWidgetClassic.notifyThemeChange(musicService)
                appWidgetFull.notifyThemeChange(musicService)
                appWidgetCircle.notifyThemeChange(musicService)
            }
            WIDGET_CUSTOM_COLOR -> {
                appWidgetClassic.notifyThemeChange(musicService)
                appWidgetFull.notifyThemeChange(musicService)
                appWidgetCircle.notifyThemeChange(musicService)
            }
            PROGRESSBAR_COLOR-> {
                appWidgetClassic.notifyThemeChange(musicService)
                appWidgetFull.notifyThemeChange(musicService)
            }
            WIDGET_BACKGROUND -> {
                appWidgetClassic.notifyThemeChange(musicService)
                appWidgetFull.notifyThemeChange(musicService)
                appWidgetCircle.notifyThemeChange(musicService)
            }
        }
    }

    override fun invalidateSettings() {
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

        val buttonColor: Preference? = findPreference(WIDGET_BUTTON_COLOR)
        buttonColor?.let {
            setSummary(it)
            it.setOnPreferenceChangeListener { _, newValue ->
                setSummary(it, newValue)
                true
            }
        }

        val customWidgetColor: ATEColorPreference? = findPreference(WIDGET_CUSTOM_COLOR)
        val accentColor = PreferenceUtil.customWidgetColor
        customWidgetColor?.setColor(accentColor, ColorUtil.darkenColor(accentColor))
        customWidgetColor?.setOnPreferenceClickListener {
            materialDialog().show {
                colorChooser(
                    initialSelection = accentColor,
                    showAlphaSelector = false,
                    colors = ACCENT_COLORS,
                    subColors = ACCENT_COLORS_SUB, allowCustomArgb = true
                ) { _, color ->
                    PreferenceUtil.customWidgetColor = color
                    appWidgetClassic.notifyThemeChange(musicService)
                    appWidgetFull.notifyThemeChange(musicService)
                    appWidgetCircle.notifyThemeChange(musicService)
                    restartActivity()
                }
            }
            return@setOnPreferenceClickListener true
        }

        val progressBarColor: Preference? = findPreference(PROGRESSBAR_COLOR)
        progressBarColor?.let {
            setSummary(it)
            it.setOnPreferenceChangeListener { _, newValue ->
                setSummary(it, newValue)
                appWidgetClassic.notifyThemeChange(musicService)
                appWidgetFull.notifyThemeChange(musicService)
                true
            }
        }

        val showProgressBar: TwoStatePreference? = findPreference(SHOW_WIDGET_PROGRESSBAR)
        showProgressBar?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            true
        }

        val disableUpdate: TwoStatePreference? = findPreference(DISABLE_UPDATE)
        disableUpdate?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

            appWidgetClassic.notifyThemeChange(musicService)
            appWidgetFull.notifyThemeChange(musicService)
            true
        }

        val widgetBackground: ATEListPreference? = findPreference(WIDGET_BACKGROUND)
        widgetBackground?.setOnPreferenceChangeListener { _, newValue ->

            val value = newValue as String

            buttonColor?.isEnabled = value != "day_night"
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

        val customWidgetColor: ATEColorPreference? = findPreference(WIDGET_CUSTOM_COLOR)
        customWidgetColor?.isEnabled = PreferenceUtil.buttonColorOnWidgets == "custom"

        val buttonColor: Preference? = findPreference(WIDGET_BUTTON_COLOR)
        buttonColor?.isEnabled = PreferenceUtil.widgetBackground != "day_night"
    }
}
