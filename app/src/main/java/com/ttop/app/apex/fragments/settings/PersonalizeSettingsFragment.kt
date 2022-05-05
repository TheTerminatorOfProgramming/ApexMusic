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
package com.ttop.app.apex.fragments.settings

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.preference.TwoStatePreference
import com.ttop.app.appthemehelper.common.prefs.supportv7.ATEListPreference
import com.ttop.app.appthemehelper.common.prefs.supportv7.ATESwitchPreference
import com.ttop.app.apex.*

class PersonalizeSettingsFragment : AbsSettingsFragment() {

    override fun invalidateSettings() {
        val toggleFullScreen: TwoStatePreference? = findPreference(TOGGLE_FULL_SCREEN)
        toggleFullScreen?.setOnPreferenceChangeListener { _, _ ->
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

        val albumArtOnLockScreen: ATESwitchPreference? = findPreference(ALBUM_ART_ON_LOCK_SCREEN)
        albumArtOnLockScreen?.setOnPreferenceChangeListener { _, newValue ->
            if (!albumArtOnLockScreen.isChecked){
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Notification Bug")
                builder.setMessage("Enabling this option produces a bug with the playback notification")

                builder.setPositiveButton(android.R.string.yes) { _, _ ->
                    albumArtOnLockScreen.isChecked = newValue as Boolean
                }

                builder.setNegativeButton(android.R.string.no) { _, _ ->
                    albumArtOnLockScreen.isChecked != newValue as Boolean
                }
                builder.show()
            }else{
                albumArtOnLockScreen.isChecked = false
            }
            false
        }
    }
}
