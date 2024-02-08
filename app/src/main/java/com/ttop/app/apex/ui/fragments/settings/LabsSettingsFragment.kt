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

            classicWidgetState(requireContext(), value)

            circleWidgetState(requireContext(), value)

            fullWidgetState(requireContext(), value)

            true
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_labs)
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
