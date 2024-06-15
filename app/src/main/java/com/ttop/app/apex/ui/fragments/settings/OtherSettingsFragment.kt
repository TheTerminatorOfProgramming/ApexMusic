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
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.TwoStatePreference
import com.ttop.app.apex.AUTO_ROTATE
import com.ttop.app.apex.HAPTIC_FEEDBACK
import com.ttop.app.apex.KEEP_SCREEN_ON
import com.ttop.app.apex.LAST_ADDED_CUTOFF
import com.ttop.app.apex.R
import com.ttop.app.apex.SEARCH_ACTION
import com.ttop.app.apex.SEARCH_ICON_NAVIGATION
import com.ttop.app.apex.USE_NOTIFY_ACTIONS_AUTO
import com.ttop.app.apex.ui.fragments.LibraryViewModel
import com.ttop.app.apex.ui.fragments.ReloadType.HomeSections
import com.ttop.app.apex.util.PreferenceUtil
import org.koin.androidx.viewmodel.ext.android.activityViewModel

/**
 * @author Hemanth S (h4h13).
 */

class OtherSettingsFragment : AbsSettingsFragment(),
    SharedPreferences.OnSharedPreferenceChangeListener {
    private val libraryViewModel by activityViewModel<LibraryViewModel>()

    override fun invalidateSettings() {
        val autoRotate: TwoStatePreference? = findPreference(AUTO_ROTATE)
        autoRotate?.setOnPreferenceChangeListener { _, newValue ->
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }

            PreferenceUtil.isAutoRotate = newValue as Boolean
            true
        }

        val keepScreenOn: TwoStatePreference? = findPreference(KEEP_SCREEN_ON)
        keepScreenOn?.setOnPreferenceChangeListener { _, _ ->
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            true
        }

        val useNotiActionsAuto: TwoStatePreference? = findPreference(USE_NOTIFY_ACTIONS_AUTO)
        useNotiActionsAuto?.setOnPreferenceChangeListener { _, _ ->
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            true
        }

        val searchAction: TwoStatePreference? = findPreference(SEARCH_ACTION)
        searchAction?.setOnPreferenceChangeListener { _, _ ->
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            true
        }

        val searchIconNavigation: TwoStatePreference? = findPreference(SEARCH_ICON_NAVIGATION)
        searchIconNavigation?.setOnPreferenceChangeListener { _, _ ->
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            true
        }

        val hapticFeedback: TwoStatePreference? = findPreference(HAPTIC_FEEDBACK)
        hapticFeedback?.setOnPreferenceChangeListener { _, _ ->
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            true
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_advanced)

        val search: PreferenceCategory? = findPreference("search")
        val advanced: PreferenceCategory? = findPreference("advanced")
        val auto: PreferenceCategory? = findPreference("android_auto")

        search?.isVisible = !PreferenceUtil.isSimpleMode
        advanced?.isVisible = !PreferenceUtil.isSimpleMode
        auto?.isVisible = !PreferenceUtil.isSimpleMode
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

    override fun onSharedPreferenceChanged(p0: SharedPreferences?, key: String?) {
        when (key) {
            AUTO_ROTATE -> {
                autoRotate()
            }
        }
    }
}