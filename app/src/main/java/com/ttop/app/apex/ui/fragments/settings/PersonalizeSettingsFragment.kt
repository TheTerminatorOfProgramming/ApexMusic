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
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.preference.TwoStatePreference
import com.ttop.app.apex.APP_BAR_COLOR
import com.ttop.app.apex.DISABLE_APP_BAR_SCROLL
import com.ttop.app.apex.HOME_ALBUM_GRID_STYLE
import com.ttop.app.apex.HOME_ARTIST_GRID_STYLE
import com.ttop.app.apex.PAUSE_HISTORY
import com.ttop.app.apex.R
import com.ttop.app.apex.REMEMBER_LAST_TAB
import com.ttop.app.apex.TAB_TEXT_MODE
import com.ttop.app.apex.TOGGLE_SUGGESTIONS
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.appthemehelper.common.prefs.supportv7.ATEListPreference

class PersonalizeSettingsFragment : AbsSettingsFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun invalidateSettings() {
        val suggestions: TwoStatePreference? = findPreference(TOGGLE_SUGGESTIONS)
        suggestions?.setOnPreferenceChangeListener { _, _ ->
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            true
        }

        val pauseHistory: TwoStatePreference? = findPreference(PAUSE_HISTORY)
        pauseHistory?.setOnPreferenceChangeListener { _, _ ->
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
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
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            true
        }

        val disableAppBarScroll: TwoStatePreference? = findPreference(DISABLE_APP_BAR_SCROLL)
        disableAppBarScroll?.setOnPreferenceChangeListener { _, _ ->
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            restartActivity()
            true
        }

        val appBarColor: TwoStatePreference? = findPreference(APP_BAR_COLOR)
        appBarColor?.setOnPreferenceChangeListener { _, _ ->
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            restartActivity()
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            TAB_TEXT_MODE -> {
                val tabTextMode: ATEListPreference? = findPreference(TAB_TEXT_MODE)
                if (tabTextMode != null) {
                    tabTextMode.summary = PreferenceUtil.tabTitleMode.toString()
                }
            }
        }
    }
}