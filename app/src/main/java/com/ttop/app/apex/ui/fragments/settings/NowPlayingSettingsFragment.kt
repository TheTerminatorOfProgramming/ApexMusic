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
import com.ttop.app.apex.ADAPTIVE_COLOR_APP
import com.ttop.app.apex.ALBUM_COVER_STYLE
import com.ttop.app.apex.ALBUM_COVER_TRANSFORM
import com.ttop.app.apex.CAROUSEL_EFFECT
import com.ttop.app.apex.CIRCULAR_ALBUM_ART
import com.ttop.app.apex.COLOR_ANIMATE
import com.ttop.app.apex.DURATION_SAME
import com.ttop.app.apex.EXPAND_NOW_PLAYING_PANEL
import com.ttop.app.apex.FAST_FORWARD_DURATION
import com.ttop.app.apex.LYRICS_MODE
import com.ttop.app.apex.LYRICS_TYPE
import com.ttop.app.apex.NEW_BLUR_AMOUNT
import com.ttop.app.apex.NOW_PLAYING_SCREEN_ID
import com.ttop.app.apex.PLAYER_BACKGROUND
import com.ttop.app.apex.QUEUE_STYLE
import com.ttop.app.apex.QUEUE_STYLE_LAND
import com.ttop.app.apex.R
import com.ttop.app.apex.REWIND_DURATION
import com.ttop.app.apex.SCREEN_ON_LYRICS
import com.ttop.app.apex.SHUFFLE_STATE
import com.ttop.app.apex.SWIPE_ANYWHERE_NOW_PLAYING
import com.ttop.app.apex.TOGGLE_AUTOPLAY
import com.ttop.app.apex.ui.fragments.NowPlayingScreen
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.appthemehelper.common.prefs.supportv7.ATEListPreference
import com.ttop.app.appthemehelper.common.prefs.supportv7.ATESeekBarPreference
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

        val lyricsScreenOn: TwoStatePreference? = findPreference(SCREEN_ON_LYRICS)
        lyricsScreenOn?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            true
        }

        val autoplay: TwoStatePreference? = findPreference(TOGGLE_AUTOPLAY)
        autoplay?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            true
        }

        val colorAnimate: TwoStatePreference? = findPreference(COLOR_ANIMATE)
        colorAnimate?.isChecked = PreferenceUtil.isColorAnimate
        colorAnimate?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            restartActivity()
            true
        }

        val adaptiveColor: ATESwitchPreference? = findPreference(ADAPTIVE_COLOR_APP)
        adaptiveColor?.isEnabled =
            PreferenceUtil.nowPlayingScreen in listOf(
                NowPlayingScreen.Adaptive,
                NowPlayingScreen.Card,
                NowPlayingScreen.Classic,
                NowPlayingScreen.Gradient,
                NowPlayingScreen.Peek,
            )

        adaptiveColor?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            restartActivity()
            true
        }

        val playerBG: ATESwitchPreference? = findPreference(PLAYER_BACKGROUND)
        playerBG?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            restartActivity()
            true
        }

        val shuffleState: ATESwitchPreference? = findPreference(SHUFFLE_STATE)
        shuffleState?.setOnPreferenceChangeListener { _, _ ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            true
        }

        val rwdDuration: ATESeekBarPreference? = findPreference(REWIND_DURATION)
        rwdDuration?.min = 5

        val ffDuration: ATESeekBarPreference? = findPreference(FAST_FORWARD_DURATION)
        ffDuration?.min = 5
        ffDuration?.setOnPreferenceChangeListener { _, newValue ->
            val duration = newValue as Int

            if (PreferenceUtil.isDurationSame) {
                rwdDuration!!.value = duration
            }
            true
        }

        val durationSame: ATESwitchPreference? = findPreference(DURATION_SAME)
        if (PreferenceUtil.isDurationSame) {
            rwdDuration!!.value = ffDuration!!.value
        }
        rwdDuration?.isEnabled = !PreferenceUtil.isDurationSame

        durationSame?.setOnPreferenceChangeListener { _, newValue ->
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

            val enabled = newValue as Boolean

            if (enabled) {
                rwdDuration!!.value = ffDuration!!.value
            }

            rwdDuration?.isEnabled = !enabled

            true
        }

        val queueStyle: ATEListPreference? = findPreference(QUEUE_STYLE)
        queueStyle?.setOnPreferenceChangeListener { _, _ ->
            restartActivity()
            true
        }

        val queueStyleLand: ATEListPreference? = findPreference(QUEUE_STYLE_LAND)
        queueStyleLand?.setOnPreferenceChangeListener { _, _ ->
            restartActivity()
            true
        }

        val lyricsType: ATEListPreference? = findPreference(LYRICS_MODE)
        val coverLyricsType: ATEListPreference? = findPreference(LYRICS_TYPE)
        lyricsType?.setOnPreferenceChangeListener { _, _ ->
            when (PreferenceUtil.lyricsMode) {
                "id3" -> {
                    coverLyricsType?.isVisible = false
                }
                "synced" -> {
                    coverLyricsType?.isVisible = true
                }
                "both" -> {
                    coverLyricsType?.isVisible = true
                }
                "disabled" -> {
                    coverLyricsType?.isVisible = false
                }
            }
            true
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        if (ApexUtil.isFoldable(requireContext())) {
            addPreferencesFromResource(R.xml.pref_now_playing_screen_foldable)
        }else {
            addPreferencesFromResource(R.xml.pref_now_playing_screen)
        }

        val newBlur: ATESeekBarPreference? = findPreference(NEW_BLUR_AMOUNT)
        newBlur?.isVisible = PreferenceUtil.nowPlayingScreen == NowPlayingScreen.Blur

        val coverLyricsType: ATEListPreference? = findPreference(LYRICS_TYPE)
        when (PreferenceUtil.lyricsMode) {
            "id3" -> {
                coverLyricsType?.isVisible = false
            }
            "synced" -> {
                coverLyricsType?.isVisible = true
            }
            "both" -> {
                coverLyricsType?.isVisible = true
            }
            "disabled" -> {
                coverLyricsType?.isVisible = false
            }
        }

        val lyrics: PreferenceCategory? = findPreference("lyrics")
        lyrics?.isVisible = !PreferenceUtil.isSimpleMode

        val playerBG: ATESwitchPreference? = findPreference(PLAYER_BACKGROUND)
        val colorAnimate: TwoStatePreference? = findPreference(COLOR_ANIMATE)
        val swipeAnywhere: ATEListPreference? = findPreference(SWIPE_ANYWHERE_NOW_PLAYING)
        val expandPanel: ATEListPreference? = findPreference(EXPAND_NOW_PLAYING_PANEL)
        val carouselEffect: TwoStatePreference? = findPreference(CAROUSEL_EFFECT)
        val lyricsScreenOn: TwoStatePreference? = findPreference(SCREEN_ON_LYRICS)
        val lyricsType: ATEListPreference? = findPreference(LYRICS_MODE)

        if (PreferenceUtil.isSimpleMode) {
            playerBG?.isVisible = false
            colorAnimate?.isVisible = false
            swipeAnywhere?.isVisible = false
            expandPanel?.isVisible = false
            carouselEffect?.isVisible = false
            lyricsScreenOn?.isVisible = false
            lyricsType?.isVisible = false
        }else {
            playerBG?.isVisible = true
            colorAnimate?.isVisible = true
            swipeAnywhere?.isVisible = true
            expandPanel?.isVisible = true
            carouselEffect?.isVisible = true
            lyricsScreenOn?.isVisible = true
            lyricsType?.isVisible = true
        }
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
                NowPlayingScreen.Gradient,
                NowPlayingScreen.Peek
            )

        if (adaptiveColor?.isEnabled == false) {
            adaptiveColor.isChecked = false
        }

        val newBlur: ATESeekBarPreference? = findPreference(NEW_BLUR_AMOUNT)
        newBlur?.isVisible = PreferenceUtil.nowPlayingScreen == NowPlayingScreen.Blur
    }

    private fun updateAlbumCoverStyle() {
        val preference: Preference? = findPreference(ALBUM_COVER_STYLE)
        when (PreferenceUtil.nowPlayingScreen) {
            NowPlayingScreen.Card, NowPlayingScreen.Minimal, NowPlayingScreen.Gradient -> {
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
            LYRICS_MODE -> restartActivity()
        }
    }
}
