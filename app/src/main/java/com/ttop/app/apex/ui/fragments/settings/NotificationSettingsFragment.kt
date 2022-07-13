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
import androidx.preference.Preference
import androidx.preference.TwoStatePreference
import com.ttop.app.apex.CLASSIC_NOTIFICATION
import com.ttop.app.apex.COLORED_NOTIFICATION
import com.ttop.app.apex.R
import com.ttop.app.apex.util.PreferenceUtil

/**
 * @author Hemanth S (h4h13).
 */

class NotificationSettingsFragment : AbsSettingsFragment(),
    SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == CLASSIC_NOTIFICATION) {
            if (VERSION.SDK_INT >= VERSION_CODES.O) {
                findPreference<Preference>(COLORED_NOTIFICATION)?.isEnabled =
                    sharedPreferences?.getBoolean(key, false)!!
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
