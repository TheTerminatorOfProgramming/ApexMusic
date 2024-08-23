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
import com.ttop.app.apex.ADAPTIVE_COLOR_APP
import com.ttop.app.apex.ALBUM_COVER_STYLE
import com.ttop.app.apex.ALBUM_COVER_TRANSFORM
import com.ttop.app.apex.CAROUSEL_EFFECT
import com.ttop.app.apex.CIRCULAR_ALBUM_ART
import com.ttop.app.apex.COLOR_ANIMATE
import com.ttop.app.apex.CUSTOMIZABLE_TOOLBAR_ACTION
import com.ttop.app.apex.DISABLE_QUEUE
import com.ttop.app.apex.DURATION_SAME
import com.ttop.app.apex.FAST_FORWARD_DURATION
import com.ttop.app.apex.LYRICS_MODE
import com.ttop.app.apex.NAV_BAR_BLACK
import com.ttop.app.apex.NEW_BLUR_AMOUNT
import com.ttop.app.apex.NOW_PLAYING_SCREEN_ID
import com.ttop.app.apex.PLAYER_BACKGROUND
import com.ttop.app.apex.R
import com.ttop.app.apex.REWIND_DURATION
import com.ttop.app.apex.SCREEN_ON_LYRICS
import com.ttop.app.apex.SHUFFLE_STATE
import com.ttop.app.apex.SWIPE_ANYWHERE_NOW_PLAYING_NON_FOLDABLE
import com.ttop.app.apex.TOGGLE_AUTOPLAY
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.Adaptive
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.Blur
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.Card
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.Classic
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.Gradient
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.Live
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.Minimal
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.Peek
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.appthemehelper.common.prefs.supportv7.ATEListPreference
import com.ttop.app.appthemehelper.common.prefs.supportv7.ATESwitchPreference

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
        colorAnimate?.setOnPreferenceChangeListener { _, _ ->
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            true
        }

        val adaptiveColor: ATESwitchPreference? = findPreference(ADAPTIVE_COLOR_APP)
        adaptiveColor?.setOnPreferenceChangeListener { _, newValue ->
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }

            val adaptiveColorEnabled = newValue as Boolean
            val nps = PreferenceUtil.nowPlayingScreen
            val npsList = listOf(Adaptive, Card, Classic, Live)
            val blackNavBar: ATESwitchPreference? = findPreference(NAV_BAR_BLACK)

            blackNavBar?.isEnabled = PreferenceUtil.isBlackMode && adaptiveColorEnabled && npsList.contains(nps)
            true
        }

        val playerBG: ATESwitchPreference? = findPreference(PLAYER_BACKGROUND)
        playerBG?.setOnPreferenceChangeListener { _, _ ->
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
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

            PreferenceUtil.rewindDuration =  PreferenceUtil.fastForwardDuration
            rwdDuration?.isEnabled = !enabled

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
                PreferenceUtil.showLyricsTablet = false
            }
            true
        }

        val blackNavBar: ATESwitchPreference? = findPreference(NAV_BAR_BLACK)
        blackNavBar?.setOnPreferenceChangeListener { _, _ ->
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            true
        }

        val playerQueueDisabled: ATESwitchPreference? = findPreference(DISABLE_QUEUE)
        playerQueueDisabled?.setOnPreferenceChangeListener { _, _ ->
            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            true
        }
    }

    private fun blackNavBarEnabled() {
        val nps = PreferenceUtil.nowPlayingScreen
        val npsList = listOf(Adaptive, Card, Classic, Live)
        val blackNavBar: ATESwitchPreference? = findPreference(NAV_BAR_BLACK)

        if (PreferenceUtil.isBlackMode && PreferenceUtil.isAdaptiveColor && npsList.contains(nps)) {
            blackNavBar?.isEnabled = true
        }else {
            blackNavBar?.isEnabled = false
            blackNavBar?.isChecked = false
        }
    }


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        if (ApexUtil.isFoldable(requireContext())) {
            addPreferencesFromResource(R.xml.pref_now_playing_screen_foldable)
        }else {
            addPreferencesFromResource(R.xml.pref_now_playing_screen)
        }

        val newBlur: Preference? = findPreference(NEW_BLUR_AMOUNT)
        newBlur?.isVisible = PreferenceUtil.nowPlayingScreen == Blur

        blackNavBarEnabled()

        val adaptiveColor: ATESwitchPreference? = findPreference(ADAPTIVE_COLOR_APP)
        val playerBG: ATESwitchPreference? = findPreference(PLAYER_BACKGROUND)
        val colorAnimate: TwoStatePreference? = findPreference(COLOR_ANIMATE)
        val carouselEffect: TwoStatePreference? = findPreference(CAROUSEL_EFFECT)
        val customToolbar: Preference? = findPreference(CUSTOMIZABLE_TOOLBAR_ACTION)

        when (PreferenceUtil.nowPlayingScreen) {
            Adaptive -> {
                adaptiveColor?.isEnabled = true
                playerBG?.isEnabled = false
                playerBG?.isChecked = false
                colorAnimate?.isEnabled = false
                colorAnimate?.isChecked = false
                newBlur?.isVisible = false
                carouselEffect?.isChecked = false
                carouselEffect?.isEnabled = false
            }
            Blur -> {
                adaptiveColor?.isEnabled = false
                adaptiveColor?.isChecked = false
                playerBG?.isEnabled = false
                playerBG?.isChecked = false
                colorAnimate?.isEnabled = false
                colorAnimate?.isChecked = false
                newBlur?.isVisible = true
                carouselEffect?.isEnabled = true
                customToolbar?.isEnabled = true
            }
            Card -> {
                adaptiveColor?.isEnabled = true
                playerBG?.isEnabled = false
                playerBG?.isChecked = false
                colorAnimate?.isEnabled = false
                colorAnimate?.isChecked = false
                newBlur?.isVisible = false
                carouselEffect?.isChecked = false
                carouselEffect?.isEnabled = false
                customToolbar?.isEnabled = true
            }
            Classic -> {
                adaptiveColor?.isEnabled = true
                playerBG?.isEnabled = true
                colorAnimate?.isEnabled = true
                newBlur?.isVisible = false
                carouselEffect?.isEnabled = true
                customToolbar?.isEnabled = true
            }
            Gradient -> {
                adaptiveColor?.isEnabled = false
                adaptiveColor?.isChecked = false
                playerBG?.isEnabled = false
                playerBG?.isChecked = false
                colorAnimate?.isEnabled = false
                colorAnimate?.isChecked = false
                newBlur?.isVisible = false
                carouselEffect?.isChecked = false
                carouselEffect?.isEnabled = false
                customToolbar?.isEnabled = true
            }
            Live -> {
                adaptiveColor?.isEnabled = true
                playerBG?.isEnabled = true
                colorAnimate?.isEnabled = true
                newBlur?.isVisible = false
                carouselEffect?.isEnabled = true
                PreferenceUtil.customToolbarAction = "disabled"
            }
            Minimal -> {
                adaptiveColor?.isEnabled = false
                adaptiveColor?.isChecked = false
                playerBG?.isEnabled = false
                playerBG?.isChecked = false
                colorAnimate?.isEnabled = false
                colorAnimate?.isChecked = false
                newBlur?.isVisible = false
                carouselEffect?.isChecked = false
                carouselEffect?.isEnabled = false
                customToolbar?.isEnabled = true
            }
            Peek -> {
                adaptiveColor?.isEnabled = true
                playerBG?.isEnabled = true
                colorAnimate?.isEnabled = true
                newBlur?.isVisible = false
                carouselEffect?.isEnabled = true
                customToolbar?.isEnabled = true
            }
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
        val playerBG: ATESwitchPreference? = findPreference(PLAYER_BACKGROUND)
        val colorAnimate: TwoStatePreference? = findPreference(COLOR_ANIMATE)
        val newBlur: Preference? = findPreference(NEW_BLUR_AMOUNT)
        val carouselEffect: TwoStatePreference? = findPreference(CAROUSEL_EFFECT)
        val customToolbar: Preference? = findPreference(CUSTOMIZABLE_TOOLBAR_ACTION)

        when (PreferenceUtil.nowPlayingScreen) {
            Adaptive -> {
                adaptiveColor?.isEnabled = true
                playerBG?.isEnabled = false
                playerBG?.isChecked = false
                colorAnimate?.isEnabled = false
                colorAnimate?.isChecked = false
                newBlur?.isVisible = false
                carouselEffect?.isChecked = false
                carouselEffect?.isEnabled = false
            }
            Blur -> {
                adaptiveColor?.isEnabled = false
                adaptiveColor?.isChecked = false
                playerBG?.isEnabled = false
                playerBG?.isChecked = false
                colorAnimate?.isEnabled = false
                colorAnimate?.isChecked = false
                newBlur?.isVisible = true
                carouselEffect?.isEnabled = true
                customToolbar?.isEnabled = true
            }
            Card -> {
                adaptiveColor?.isEnabled = true
                playerBG?.isEnabled = false
                playerBG?.isChecked = false
                colorAnimate?.isEnabled = false
                colorAnimate?.isChecked = false
                newBlur?.isVisible = false
                carouselEffect?.isChecked = false
                carouselEffect?.isEnabled = false
                customToolbar?.isEnabled = true
            }
            Classic -> {
                adaptiveColor?.isEnabled = true
                playerBG?.isEnabled = true
                colorAnimate?.isEnabled = true
                newBlur?.isVisible = false
                carouselEffect?.isEnabled = true
                customToolbar?.isEnabled = true
            }
            Gradient -> {
                adaptiveColor?.isEnabled = false
                adaptiveColor?.isChecked = false
                playerBG?.isEnabled = false
                playerBG?.isChecked = false
                colorAnimate?.isEnabled = false
                colorAnimate?.isChecked = false
                newBlur?.isVisible = false
                carouselEffect?.isChecked = false
                carouselEffect?.isEnabled = false
                customToolbar?.isEnabled = true
            }
            Live -> {
                adaptiveColor?.isEnabled = true
                playerBG?.isEnabled = true
                colorAnimate?.isEnabled = true
                newBlur?.isVisible = false
                carouselEffect?.isEnabled = true
                customToolbar?.isEnabled = true
            }
            Minimal -> {
                adaptiveColor?.isEnabled = false
                adaptiveColor?.isChecked = false
                playerBG?.isEnabled = false
                playerBG?.isChecked = false
                colorAnimate?.isEnabled = false
                colorAnimate?.isChecked = false
                newBlur?.isVisible = false
                carouselEffect?.isChecked = false
                carouselEffect?.isEnabled = false
                customToolbar?.isEnabled = true
            }
            Peek -> {
                adaptiveColor?.isEnabled = true
                playerBG?.isEnabled = true
                colorAnimate?.isEnabled = true
                newBlur?.isVisible = false
                carouselEffect?.isEnabled = true
                customToolbar?.isEnabled = true
            }
        }
        blackNavBarEnabled()
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
        }
    }
}
