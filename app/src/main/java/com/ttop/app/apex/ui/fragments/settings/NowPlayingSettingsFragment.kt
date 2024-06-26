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
import com.ttop.app.apex.CUSTOMIZABLE_TOOLBAR_ACTION
import com.ttop.app.apex.DURATION_SAME
import com.ttop.app.apex.EXPAND_NOW_PLAYING_PANEL
import com.ttop.app.apex.FAST_FORWARD_DURATION
import com.ttop.app.apex.LYRICS_MODE
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
import com.ttop.app.apex.SWIPE_ANYWHERE_NOW_PLAYING_NON_FOLDABLE
import com.ttop.app.apex.TOGGLE_AUTOPLAY
import com.ttop.app.apex.ui.fragments.NowPlayingScreen
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.appthemehelper.common.prefs.supportv7.ATEListPreference
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
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            return@setOnPreferenceChangeListener true
        }

        val lyricsScreenOn: TwoStatePreference? = findPreference(SCREEN_ON_LYRICS)
        lyricsScreenOn?.setOnPreferenceChangeListener { _, _ ->
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            true
        }

        val autoplay: TwoStatePreference? = findPreference(TOGGLE_AUTOPLAY)
        autoplay?.setOnPreferenceChangeListener { _, _ ->
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            true
        }

        val colorAnimate: TwoStatePreference? = findPreference(COLOR_ANIMATE)
        colorAnimate?.isChecked = PreferenceUtil.isColorAnimate

        if (PreferenceUtil.nowPlayingScreen == NowPlayingScreen.Adaptive) {
            colorAnimate?.isEnabled = false
            colorAnimate?.isChecked = false
        }

        colorAnimate?.setOnPreferenceChangeListener { _, _ ->
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            restartActivity()
            true
        }

        val adaptiveColor: ATESwitchPreference? = findPreference(ADAPTIVE_COLOR_APP)
        adaptiveColor?.isEnabled =
            PreferenceUtil.nowPlayingScreen in listOf(
                NowPlayingScreen.Adaptive,
                NowPlayingScreen.Card,
                NowPlayingScreen.Classic,
                NowPlayingScreen.Peek,
            )

        adaptiveColor?.setOnPreferenceChangeListener { _, _ ->
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            restartActivity()
            true
        }

        val playerBG: ATESwitchPreference? = findPreference(PLAYER_BACKGROUND)
        if (PreferenceUtil.nowPlayingScreen == NowPlayingScreen.Adaptive) {
            playerBG?.isEnabled = false
            playerBG?.isChecked = false
        }

        playerBG?.setOnPreferenceChangeListener { _, _ ->
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            restartActivity()
            true
        }

        val shuffleState: ATESwitchPreference? = findPreference(SHUFFLE_STATE)
        shuffleState?.setOnPreferenceChangeListener { _, _ ->
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            true
        }

        val rwdDuration: Preference? = findPreference(REWIND_DURATION)

        val ffDuration: Preference? = findPreference(FAST_FORWARD_DURATION)
        ffDuration?.setOnPreferenceChangeListener { _, newValue ->
            val duration = newValue as Int

            if (PreferenceUtil.isDurationSame) {
                PreferenceUtil.rewindDuration = duration
            }
            true
        }

        val durationSame: ATESwitchPreference? = findPreference(DURATION_SAME)
        if (PreferenceUtil.isDurationSame) {
            PreferenceUtil.rewindDuration =  PreferenceUtil.fastForwardDuration
        }
        rwdDuration?.isEnabled = !PreferenceUtil.isDurationSame

        durationSame?.setOnPreferenceChangeListener { _, newValue ->
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }

            val enabled = newValue as Boolean

            if (enabled) {
                PreferenceUtil.rewindDuration =  PreferenceUtil.fastForwardDuration
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

        val swipeAnywhereNonFoldable: ATESwitchPreference? = findPreference(SWIPE_ANYWHERE_NOW_PLAYING_NON_FOLDABLE)
        swipeAnywhereNonFoldable?.setOnPreferenceChangeListener { _, _ ->
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            true
        }

        val lyricsType: ATEListPreference? = findPreference(LYRICS_MODE)
        lyricsType?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue == "disabled") {
                PreferenceUtil.showLyrics = false
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

        val newBlur: Preference? = findPreference(NEW_BLUR_AMOUNT)
        newBlur?.isVisible = PreferenceUtil.nowPlayingScreen == NowPlayingScreen.Blur

        val lyrics: PreferenceCategory? = findPreference("lyrics")
        lyrics?.isVisible = !PreferenceUtil.isSimpleMode

        val playerBG: ATESwitchPreference? = findPreference(PLAYER_BACKGROUND)
        val colorAnimate: TwoStatePreference? = findPreference(COLOR_ANIMATE)
        val swipeAnywhere: ATEListPreference? = findPreference(SWIPE_ANYWHERE_NOW_PLAYING)
        val swipeAnywhereNonFoldable: ATESwitchPreference? = findPreference(SWIPE_ANYWHERE_NOW_PLAYING_NON_FOLDABLE)
        val expandPanel: ATEListPreference? = findPreference(EXPAND_NOW_PLAYING_PANEL)
        val carouselEffect: TwoStatePreference? = findPreference(CAROUSEL_EFFECT)
        val lyricsScreenOn: TwoStatePreference? = findPreference(SCREEN_ON_LYRICS)
        val lyricsType: ATEListPreference? = findPreference(LYRICS_MODE)

        if (PreferenceUtil.isSimpleMode) {
            playerBG?.isVisible = false
            colorAnimate?.isVisible = false
            swipeAnywhere?.isVisible = false
            swipeAnywhereNonFoldable?.isVisible = false
            expandPanel?.isVisible = false
            carouselEffect?.isVisible = false
            lyricsScreenOn?.isVisible = false
            lyricsType?.isVisible = false
        }else {
            playerBG?.isVisible = true
            colorAnimate?.isVisible = true
            swipeAnywhere?.isVisible = true
            swipeAnywhereNonFoldable?.isVisible = true
            expandPanel?.isVisible = true
            carouselEffect?.isVisible = true
            lyricsScreenOn?.isVisible = true
            lyricsType?.isVisible = true
        }

        if (PreferenceUtil.nowPlayingScreen == NowPlayingScreen.Adaptive || PreferenceUtil.nowPlayingScreen == NowPlayingScreen.Card || PreferenceUtil.nowPlayingScreen == NowPlayingScreen.Gradient || PreferenceUtil.nowPlayingScreen == NowPlayingScreen.Minimal) {
            if (PreferenceUtil.isCarouselEffect) {
                carouselEffect?.isChecked = false
                carouselEffect?.isEnabled = false
            }
        }else {
            carouselEffect?.isEnabled = true
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
                NowPlayingScreen.Peek
            )

        if (adaptiveColor?.isEnabled == false) {
            adaptiveColor.isChecked = false
        }

        val colorAnimate: TwoStatePreference? = findPreference(COLOR_ANIMATE)
        if (PreferenceUtil.nowPlayingScreen == NowPlayingScreen.Adaptive) {
            colorAnimate?.isEnabled = false
            colorAnimate?.isChecked = false
        }

        val playerBG: ATESwitchPreference? = findPreference(PLAYER_BACKGROUND)
        if (PreferenceUtil.nowPlayingScreen == NowPlayingScreen.Adaptive) {
            playerBG?.isEnabled = false
            playerBG?.isChecked = false
        }

        val newBlur: Preference? = findPreference(NEW_BLUR_AMOUNT)
        newBlur?.isVisible = PreferenceUtil.nowPlayingScreen == NowPlayingScreen.Blur

        val carouselEffect: TwoStatePreference? = findPreference(CAROUSEL_EFFECT)
        val customToolbar: Preference? = findPreference(CUSTOMIZABLE_TOOLBAR_ACTION)
        when (PreferenceUtil.nowPlayingScreen) {
            NowPlayingScreen.Adaptive -> {
                carouselEffect?.isChecked = false
                carouselEffect?.isEnabled = false
                customToolbar?.isEnabled = false
                PreferenceUtil.customToolbarAction = "disabled"
            }
            NowPlayingScreen.Blur -> {
                carouselEffect?.isEnabled = true
                customToolbar?.isEnabled = true
            }
            NowPlayingScreen.Card -> {
                carouselEffect?.isChecked = false
                carouselEffect?.isEnabled = false
                customToolbar?.isEnabled = true
            }
            NowPlayingScreen.Classic -> {
                carouselEffect?.isEnabled = true
                customToolbar?.isEnabled = true
            }
            NowPlayingScreen.Gradient -> {
                carouselEffect?.isChecked = false
                carouselEffect?.isEnabled = false
                customToolbar?.isEnabled = true
            }
            NowPlayingScreen.Minimal -> {
                carouselEffect?.isChecked = false
                carouselEffect?.isEnabled = false
                customToolbar?.isEnabled = true
            }
            NowPlayingScreen.Peek -> {
                carouselEffect?.isEnabled = true
                customToolbar?.isEnabled = true
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
        updateNowPlayingScreenSummary()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        PreferenceUtil.unregisterOnSharedPreferenceChangedListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        when (key) {
            NOW_PLAYING_SCREEN_ID -> {
                updateNowPlayingScreenSummary()
            }
            ALBUM_COVER_STYLE -> updateAlbumCoverStyleSummary()
            CIRCULAR_ALBUM_ART, CAROUSEL_EFFECT -> invalidateSettings()
            LYRICS_MODE,
            CUSTOMIZABLE_TOOLBAR_ACTION -> restartActivity()
        }
    }
}
