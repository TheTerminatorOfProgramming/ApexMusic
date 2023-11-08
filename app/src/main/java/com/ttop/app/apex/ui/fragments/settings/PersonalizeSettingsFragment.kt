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

import android.os.Build
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.preference.Preference
import androidx.preference.TwoStatePreference
import com.ttop.app.apex.*
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.appthemehelper.common.prefs.supportv7.ATEListPreference
import com.ttop.app.appthemehelper.common.prefs.supportv7.ATESwitchPreference
import com.ttop.app.appthemehelper.util.VersionUtils

class PersonalizeSettingsFragment : AbsSettingsFragment() {

    override fun invalidateSettings() {
        val user: TwoStatePreference? = findPreference(TOGGLE_USER_NAME)
        user?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            true
        }

        val banner: TwoStatePreference? = findPreference(TOGGLE_HOME_BANNER)
        banner?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            true
        }

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
        val albumArt: TwoStatePreference? = findPreference(ALBUM_ART_ON_LOCK_SCREEN)
        albumArt?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            true
        }

        val blur: TwoStatePreference? = findPreference(BLURRED_ALBUM_ART)
        blur?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            true
        }

        val lockScreen: TwoStatePreference? = findPreference(LOCK_SCREEN)
        lockScreen?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            true
        }

        val appBarMode: ATEListPreference? = findPreference(APPBAR_MODE)
        appBarMode?.setOnPreferenceChangeListener { _, _ ->
            restartActivity()
            true
        }

        val showScrollbar: TwoStatePreference? = findPreference(SHOW_SCROLLBAR)
        showScrollbar?.isChecked = PreferenceUtil.isShowScrollbar
        showScrollbar?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            true
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q){
            addPreferencesFromResource(R.xml.pref_ui_a10)
        }else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R){
            addPreferencesFromResource(R.xml.pref_ui_a11)
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            addPreferencesFromResource(R.xml.pref_ui)
        }
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