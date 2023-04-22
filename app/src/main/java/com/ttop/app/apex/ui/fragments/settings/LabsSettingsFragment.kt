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
import com.ttop.app.apex.appwidgets.AppWidgetBig
import com.ttop.app.apex.appwidgets.AppWidgetFullCircle
import com.ttop.app.apex.appwidgets.AppWidgetSquare
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.service.MusicService
import com.ttop.app.apex.util.PreferenceUtil


/**
 * @author Hemanth S (h4h13).
 */

class LabsSettingsFragment : AbsSettingsFragment() {

    override fun invalidateSettings() {
        val legacyWidgets: TwoStatePreference? = findPreference(RESTORE_LEGACY_WIDGETS)
        legacyWidgets?.isChecked = PreferenceUtil.isLegacyWidgets
        legacyWidgets?.setOnPreferenceChangeListener { _, newValue ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

            val value = newValue as Boolean

            if (PreferenceUtil.isBigWidget) {
                bigWidgetState(requireContext(), value)
            }

            if (PreferenceUtil.isFullCircleWidget) {
                fullCircleWidgetState(requireContext(), value)
            }

            true
        }

        val bigWidget: TwoStatePreference? = findPreference(RESTORE_BIG_WIDGET)
        bigWidget?.isChecked = PreferenceUtil.isLegacyWidgets
        bigWidget?.setOnPreferenceChangeListener { _, newValue ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

            val value = newValue as Boolean

            bigWidgetState(requireContext(), value)
            true
        }

        val fullCircleWidget: TwoStatePreference? = findPreference(RESTORE_FULL_CIRCLE_WIDGET)
        fullCircleWidget?.isChecked = PreferenceUtil.isLegacyWidgets
        fullCircleWidget?.setOnPreferenceChangeListener { _, newValue ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

            val value = newValue as Boolean

            fullCircleWidgetState(requireContext(), value)
            true
        }

        val squareWidget: TwoStatePreference? = findPreference(USE_NEW_WIDGET)
        squareWidget?.isChecked = PreferenceUtil.isSquareWidgetAvailable
        squareWidget?.setOnPreferenceChangeListener { _, newValue ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

            val value = newValue as Boolean

            squareWidgetState(requireContext(), value)
            true
        }

        val squareWidgetText: TwoStatePreference? = findPreference(SQUARE_WIDGET_TEXT)
        squareWidgetText?.isChecked = PreferenceUtil.hideSquareWidgetText
        squareWidgetText?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            val appWidgetSquare = AppWidgetSquare.instance
            val musicService: MusicService? = MusicPlayerRemote.musicService
            appWidgetSquare.notifyThemeChange(musicService)
            true
        }

        val squareWidgetColored: TwoStatePreference? = findPreference(SQUARE_WIDGET_COLORED)
        squareWidgetColored?.isChecked = PreferenceUtil.coloredSquareWidget
        squareWidgetColored?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            true
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_labs)
    }

    private fun bigWidgetState(context: Context, state: Boolean) {
        val pm: PackageManager = context.packageManager

        val newState = if (state) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        }else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }

        pm.setComponentEnabledSetting(
            ComponentName(
                context,
                AppWidgetBig::class.java
            ), newState, PackageManager.DONT_KILL_APP
        )
    }

    private fun fullCircleWidgetState(context: Context, state: Boolean) {
        val pm: PackageManager = context.packageManager

        val newState = if (state) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        }else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }


        pm.setComponentEnabledSetting(
            ComponentName(
                context,
                AppWidgetFullCircle::class.java
            ), newState, PackageManager.DONT_KILL_APP
        )
    }

    private fun squareWidgetState(context: Context, state: Boolean) {
        val pm: PackageManager = context.packageManager

        val newState = if (state) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        }else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }

        pm.setComponentEnabledSetting(
            ComponentName(
                context,
                AppWidgetSquare::class.java
            ), newState, PackageManager.DONT_KILL_APP
        )
    }
}
