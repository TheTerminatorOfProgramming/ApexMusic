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
import androidx.preference.TwoStatePreference
import com.ttop.app.apex.*
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.ui.fragments.NowPlayingScreen
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.appthemehelper.common.prefs.supportv7.ATESwitchPreference

/**
 * @author Hemanth S (h4h13).
 */

class NowPlayingSettingsFragment : AbsSettingsFragment(),
    SharedPreferences.OnSharedPreferenceChangeListener {
    override fun invalidateSettings() {
        updateNowPlayingScreenSummary()
        updateAlbumCoverStyleSummary()

        val carouselEffect: TwoStatePreference? = findPreference(CAROUSEL_EFFECT)
        carouselEffect?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            return@setOnPreferenceChangeListener true
        }

        val snow: TwoStatePreference? = findPreference(SNOWFALL)
        snow?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            true
        }

        val lyrics: TwoStatePreference? = findPreference(SYNCED_LYRICS)
        lyrics?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

            PreferenceUtil.shouldRecreate = true
            restartActivity()
            true
        }

        val lyricsScreenOn: TwoStatePreference? = findPreference(SCREEN_ON_LYRICS)
        lyricsScreenOn?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            true
        }

        val circlePlayButton: TwoStatePreference? = findPreference(CIRCLE_PLAY_BUTTON)
        circlePlayButton?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            true
        }

        val swipeAnywhere: TwoStatePreference? = findPreference(SWIPE_ANYWHERE_NOW_PLAYING)
        swipeAnywhere?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            true
        }

        val showLyrics: TwoStatePreference? = findPreference(LYRICS)
        showLyrics?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            true
        }

        val expand: TwoStatePreference? = findPreference(EXPAND_NOW_PLAYING_PANEL)
        expand?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            true
        }

        val songInfo: TwoStatePreference? = findPreference(EXTRA_SONG_INFO)
        songInfo?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            true
        }

        val autoplay: TwoStatePreference? = findPreference(TOGGLE_AUTOPLAY)
        autoplay?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            true
        }

        val volume: TwoStatePreference? = findPreference(TOGGLE_VOLUME)
        volume?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            true
        }

        val adaptiveColor: ATESwitchPreference? = findPreference(ADAPTIVE_COLOR_APP)
        adaptiveColor?.isEnabled =
            PreferenceUtil.nowPlayingScreen in listOf(
                NowPlayingScreen.Adaptive,
                NowPlayingScreen.Card,
                NowPlayingScreen.Classic,
                NowPlayingScreen.Color,
                NowPlayingScreen.Fit,
                NowPlayingScreen.Flat,
                NowPlayingScreen.Full,
                NowPlayingScreen.Gradient,
                NowPlayingScreen.Normal,
                NowPlayingScreen.MD3,
                NowPlayingScreen.Material,
                NowPlayingScreen.Peek,
                NowPlayingScreen.Plain,
                NowPlayingScreen.Simple,
                NowPlayingScreen.Swipe
            )

        if (adaptiveColor?.isEnabled == false) {
            adaptiveColor.isChecked = false
        }

        adaptiveColor?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            true
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_now_playing_screen)
    }

    private fun updateAlbumCoverStyleSummary() {
        val preference: Preference? = findPreference(ALBUM_COVER_STYLE)
        preference?.setSummary(PreferenceUtil.albumCoverStyle.titleRes)
    }

    private fun updateNowPlayingScreenSummary() {
        val preference: Preference? = findPreference(NOW_PLAYING_SCREEN_ID)
        preference?.setSummary(PreferenceUtil.nowPlayingScreen.titleRes)

        val adaptiveColor: ATESwitchPreference? = findPreference(ADAPTIVE_COLOR_APP)
        adaptiveColor?.isEnabled =
            PreferenceUtil.nowPlayingScreen in listOf(
                NowPlayingScreen.Adaptive,
                NowPlayingScreen.Card,
                NowPlayingScreen.Classic,
                NowPlayingScreen.Color,
                NowPlayingScreen.Fit,
                NowPlayingScreen.Flat,
                NowPlayingScreen.Full,
                NowPlayingScreen.Gradient,
                NowPlayingScreen.Normal,
                NowPlayingScreen.MD3,
                NowPlayingScreen.Material,
                NowPlayingScreen.Peek,
                NowPlayingScreen.Plain,
                NowPlayingScreen.Simple,
                NowPlayingScreen.Swipe
            )

        if (adaptiveColor?.isEnabled == false) {
            adaptiveColor.isChecked = false
        }
    }

    private fun updateAlbumCoverStyle() {
        val preference: Preference? = findPreference(ALBUM_COVER_STYLE)
        when (PreferenceUtil.nowPlayingScreen) {
            NowPlayingScreen.Card,   NowPlayingScreen.Fit,   NowPlayingScreen.Tiny,   NowPlayingScreen.Classic,  NowPlayingScreen.Gradient,   NowPlayingScreen.Full -> {
                preference?.isEnabled = false
            }
            else -> {
                preference?.isEnabled = true
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        PreferenceUtil.registerOnSharedPreferenceChangedListener(this)

        val preference: Preference? = findPreference(ALBUM_COVER_TRANSFORM)
        preference?.setOnPreferenceChangeListener { albumPrefs, newValue ->
            setSummary(albumPrefs, newValue)
            true
        }
        updateAlbumCoverStyle()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        PreferenceUtil.unregisterOnSharedPreferenceChangedListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        when (key) {
            NOW_PLAYING_SCREEN_ID -> {
                updateNowPlayingScreenSummary()
                updateAlbumCoverStyle()
            }
            ALBUM_COVER_STYLE -> updateAlbumCoverStyleSummary()
            CIRCULAR_ALBUM_ART, CAROUSEL_EFFECT -> invalidateSettings()
            LYRICS -> {
                val lyrics: TwoStatePreference? = findPreference(SYNCED_LYRICS)
                if (!PreferenceUtil.showLyrics){
                    lyrics?.isChecked = false
                }
            }
        }
    }
}
