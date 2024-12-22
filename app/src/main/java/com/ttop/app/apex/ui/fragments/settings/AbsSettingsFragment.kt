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

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.ttop.app.apex.libraries.appthemehelper.common.prefs.supportv7.ATEPreferenceFragmentCompat
import com.ttop.app.apex.preferences.AlbumCoverStylePreference
import com.ttop.app.apex.preferences.AlbumCoverStylePreferenceDialog
import com.ttop.app.apex.preferences.BlacklistPreference
import com.ttop.app.apex.preferences.BlacklistPreferenceDialog
import com.ttop.app.apex.preferences.BluetoothDelayPreference
import com.ttop.app.apex.preferences.BluetoothDelayPreferenceDialog
import com.ttop.app.apex.preferences.BluetoothDevicePreference
import com.ttop.app.apex.preferences.BluetoothDevicePreferenceDialog
import com.ttop.app.apex.preferences.BlurPreference
import com.ttop.app.apex.preferences.BlurPreferenceDialog
import com.ttop.app.apex.preferences.CrossFadePreference
import com.ttop.app.apex.preferences.CrossFadePreferenceDialog
import com.ttop.app.apex.preferences.DurationPreference
import com.ttop.app.apex.preferences.DurationPreferenceDialog
import com.ttop.app.apex.preferences.FilterMaxPreference
import com.ttop.app.apex.preferences.FilterMaxPreferenceDialog
import com.ttop.app.apex.preferences.FilterMinPreference
import com.ttop.app.apex.preferences.FilterMinPreferenceDialog
import com.ttop.app.apex.preferences.LibraryPreference
import com.ttop.app.apex.preferences.LibraryPreferenceDialog
import com.ttop.app.apex.preferences.NowPlayingScreenPreference
import com.ttop.app.apex.preferences.NowPlayingScreenPreferenceDialog
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.PreferenceUtil
import dev.chrisbanes.insetter.applyInsetter

/**
 * @author Hemanth S (h4h13).
 */

abstract class AbsSettingsFragment : ATEPreferenceFragmentCompat() {

    internal fun setSummary(preference: Preference, value: Any?) {
        val stringValue = value.toString()
        if (preference is ListPreference) {
            val index = preference.findIndexOfValue(stringValue)
            preference.setSummary(if (index >= 0) preference.entries[index] else null)
        } else {
            preference.summary = stringValue
        }
    }

    abstract fun invalidateSettings()

    protected fun setSummary(preference: Preference?) {
        preference?.let {
            setSummary(
                it, PreferenceManager
                    .getDefaultSharedPreferences(it.context)
                    .getString(it.key, "")
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDivider(ColorDrawable(Color.BLACK))
        listView.overScrollMode = View.OVER_SCROLL_NEVER

        listView.applyInsetter {
            type(navigationBars = true) {
                padding(vertical = true)
            }
        }
        invalidateSettings()
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        when (preference) {
            is LibraryPreference -> {
                val fragment = LibraryPreferenceDialog.newInstance()
                fragment.show(childFragmentManager, preference.key)
            }

            is NowPlayingScreenPreference -> {
                val fragment = NowPlayingScreenPreferenceDialog.newInstance()
                fragment.show(childFragmentManager, preference.key)
            }

            is AlbumCoverStylePreference -> {
                val fragment = AlbumCoverStylePreferenceDialog.newInstance()
                fragment.show(childFragmentManager, preference.key)
            }

            is BlacklistPreference -> {
                val fragment = BlacklistPreferenceDialog.newInstance()
                fragment.show(childFragmentManager, preference.key)
            }

            is CrossFadePreference -> {
                val fragment = CrossFadePreferenceDialog.newInstance()
                fragment.show(childFragmentManager, preference.key)
            }

            is FilterMinPreference -> {
                val fragment = FilterMinPreferenceDialog.newInstance()
                fragment.show(childFragmentManager, preference.key)
            }

            is FilterMaxPreference -> {
                val fragment = FilterMaxPreferenceDialog.newInstance()
                fragment.show(childFragmentManager, preference.key)
            }

            is BlurPreference -> {
                val fragment = BlurPreferenceDialog.newInstance()
                fragment.show(childFragmentManager, preference.key)
            }

            is BluetoothDelayPreference -> {
                val fragment = BluetoothDelayPreferenceDialog.newInstance()
                fragment.show(childFragmentManager, preference.key)
            }

            is DurationPreference -> {
                val fragment = DurationPreferenceDialog.newInstance()
                fragment.show(childFragmentManager, preference.key)
            }

            is BluetoothDevicePreference -> {
                val fragment = BluetoothDevicePreferenceDialog.newInstance()
                fragment.show(childFragmentManager, preference.key)
            }

            else -> super.onDisplayPreferenceDialog(preference)
        }
    }

    fun restartActivity() {
        activity?.recreate()
    }

    fun autoRotate() {
        activity?.requestedOrientation = if (ApexUtil.isTablet) {
            if (PreferenceUtil.isAutoRotate) {
                ActivityInfo.SCREEN_ORIENTATION_SENSOR
            } else {
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }
}
