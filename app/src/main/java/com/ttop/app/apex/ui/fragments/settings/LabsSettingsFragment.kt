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
import androidx.preference.SeekBarPreference
import androidx.preference.TwoStatePreference
import com.ttop.app.apex.*
import com.ttop.app.apex.appwidgets.AppWidgetCircle
import com.ttop.app.apex.appwidgets.AppWidgetClassic
import com.ttop.app.apex.appwidgets.AppWidgetFullCircle
import com.ttop.app.apex.extensions.hide
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.service.MusicService
import com.ttop.app.apex.util.PreferenceUtil

/**
 * @author Hemanth S (h4h13).
 */

class LabsSettingsFragment : AbsSettingsFragment() {

    override fun invalidateSettings() {





    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_labs)
    }
}
