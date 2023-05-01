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
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.TwoStatePreference
import com.ttop.app.apex.*
import com.ttop.app.apex.appwidgets.AppWidgetFull
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.service.MusicService
import com.ttop.app.apex.ui.fragments.LibraryViewModel
import com.ttop.app.apex.ui.fragments.ReloadType.HomeSections
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.PreferenceUtil
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

/**
 * @author Hemanth S (h4h13).
 */

class OtherSettingsFragment : AbsSettingsFragment(),
    SharedPreferences.OnSharedPreferenceChangeListener {
    private val libraryViewModel by activityViewModel<LibraryViewModel>()

    override fun invalidateSettings() {
        val whitelist: TwoStatePreference? = findPreference(WHITELIST_MUSIC)
        whitelist?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            true
        }

        val auto_rotate: TwoStatePreference? = findPreference(AUTO_ROTATE)
        auto_rotate?.setOnPreferenceChangeListener { _, newValue ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

            PreferenceUtil.isAutoRotate = newValue as Boolean

            PreferenceUtil.shouldRecreate = true
            restartActivity()
            true
        }

        val keepScreenOn: TwoStatePreference? = findPreference(KEEP_SCREEN_ON)
        keepScreenOn?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            true
        }

        val youtubeSearch: TwoStatePreference? = findPreference(YOUTUBE_SEARCH)
        youtubeSearch?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            true
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        PreferenceUtil.languageCode =
            AppCompatDelegate.getApplicationLocales().toLanguageTags().ifEmpty { "auto" }
        addPreferencesFromResource(R.xml.pref_advanced)

        val youtubeSearch: TwoStatePreference? = findPreference(YOUTUBE_SEARCH)
        youtubeSearch?.isVisible = ApexUtil.checkYoutubeMusic(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val preference: Preference? = findPreference(LAST_ADDED_CUTOFF)
        preference?.setOnPreferenceChangeListener { lastAdded, newValue ->
            setSummary(lastAdded, newValue)
            libraryViewModel.forceReload(HomeSections)
            true
        }
    }

    override fun onResume() {
        super.onResume()
        val youtubeSearch: TwoStatePreference? = findPreference(YOUTUBE_SEARCH)
        youtubeSearch?.isVisible = ApexUtil.checkYoutubeMusic(requireContext())
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            AUTO_ROTATE -> {
                autoRotate()
            }
            YOUTUBE_SEARCH -> {
                val appWidgetFull: AppWidgetFull = AppWidgetFull.instance
                val musicService = MusicService()
                appWidgetFull.notifyThemeChange(musicService)
            }
        }
    }
}