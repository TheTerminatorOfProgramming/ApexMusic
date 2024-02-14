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

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.preference.TwoStatePreference
import com.ttop.app.apex.APPBAR_MODE
import com.ttop.app.apex.HOME_ALBUM_GRID_STYLE
import com.ttop.app.apex.HOME_ARTIST_GRID_STYLE
import com.ttop.app.apex.PAUSE_HISTORY
import com.ttop.app.apex.R
import com.ttop.app.apex.REMEMBER_LAST_TAB
import com.ttop.app.apex.SHOW_SCROLLBAR
import com.ttop.app.apex.TAB_TEXT_MODE
import com.ttop.app.apex.TOGGLE_SUGGESTIONS
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.appthemehelper.common.prefs.supportv7.ATEListPreference

class PersonalizeSettingsFragment : AbsSettingsFragment() {

    override fun invalidateSettings() {

        val suggestions: TwoStatePreference? = findPreference(TOGGLE_SUGGESTIONS)
        suggestions?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            true
        }

        val pauseHistory: TwoStatePreference? = findPreference(PAUSE_HISTORY)
        pauseHistory?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            true
        }

        val lastTab: TwoStatePreference? = findPreference(REMEMBER_LAST_TAB)
        if (PreferenceUtil.tempValue == 1){
            lastTab?.isChecked = false
            lastTab?.isEnabled = false
        }else{
            lastTab?.isEnabled = true
        }
        lastTab?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            true
        }

        val showScrollbar: TwoStatePreference? = findPreference(SHOW_SCROLLBAR)
        showScrollbar?.isChecked = PreferenceUtil.isShowScrollbar
        showScrollbar?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            true
        }

        val appBarMode: TwoStatePreference? = findPreference(APPBAR_MODE)
        appBarMode?.isChecked = PreferenceUtil.appBarMode
        appBarMode?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            true
        }

    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_ui)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val homeArtistStyle: ATEListPreference? = findPreference(HOME_ARTIST_GRID_STYLE)
        homeArtistStyle?.setOnPreferenceChangeListener { preference, newValue ->
            setSummary(preference, newValue)
            true
        }
        val homeAlbumStyle: ATEListPreference? = findPreference(HOME_ALBUM_GRID_STYLE)
        homeAlbumStyle?.setOnPreferenceChangeListener { preference, newValue ->
            setSummary(preference, newValue)
            true
        }
        val tabTextMode: ATEListPreference? = findPreference(TAB_TEXT_MODE)
        tabTextMode?.setOnPreferenceChangeListener { prefs, newValue ->
            setSummary(prefs, newValue)
            true
        }
    }
}