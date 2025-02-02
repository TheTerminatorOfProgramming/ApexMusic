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
package com.ttop.app.apex.ui.fragments.player.peek

import android.animation.Animator
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateInterpolator
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.ttop.app.apex.R
import com.ttop.app.apex.databinding.FragmentPeekControlPlayerBinding
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.extensions.applyColor
import com.ttop.app.apex.extensions.getSongInfo
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.helper.PlayPauseButtonOnClickHandler
import com.ttop.app.apex.libraries.appthemehelper.ThemeStore
import com.ttop.app.apex.libraries.appthemehelper.util.TintHelper
import com.ttop.app.apex.ui.fragments.base.AbsPlayerControlsFragment
import com.ttop.app.apex.util.ColorUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.color.MediaNotificationProcessor
import com.ttop.app.apex.util.theme.ThemeMode
import com.ttop.app.apex.views.SquigglyProgress
import kotlin.math.sqrt

/**
 * Created by hemanths on 2019-10-04.
 */

class PeekPlayerControlFragment : AbsPlayerControlsFragment(R.layout.fragment_peek_control_player) {

    private var _binding: FragmentPeekControlPlayerBinding? = null
    private val binding get() = _binding!!

    override val seekBar: SeekBar
        get() = binding.progressSlider

    override val shuffleButton: ImageButton
        get() = binding.shuffleButton

    override val repeatButton: ImageButton
        get() = binding.repeatButton

    override val nextButton: ImageButton
        get() = binding.nextButton

    override val previousButton: ImageButton
        get() = binding.previousButton

    override val songTotalTime: TextView
        get() = binding.songTotalTime

    override val songCurrentProgress: TextView
        get() = binding.songCurrentProgress

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPeekControlPlayerBinding.bind(view)
        setUpPlayPauseFab()

        binding.title.isSelected = true
        binding.artist.isSelected = true
        binding.songInfo.isSelected = true
    }

    override fun show() {}

    override fun hide() {}

    fun createRevealAnimator(view: View): Animator {
        val location = IntArray(2)
        binding.playPauseButton.getLocationOnScreen(location)
        val x = (location[0] + binding.playPauseButton.measuredWidth / 2)
        val y = (location[1] + binding.playPauseButton.measuredHeight / 2)
        val endRadius = sqrt((x * x + y * y).toFloat())
        val startRadius =
            binding.playPauseButton.measuredWidth.coerceAtMost(binding.playPauseButton.measuredHeight)
        return ViewAnimationUtils.createCircularReveal(
            view, x, y, startRadius.toFloat(),
            endRadius
        ).apply {
            duration = 300
            interpolator = AccelerateInterpolator()
        }

    }

    fun updateSong() {
        val song = MusicPlayerRemote.currentSong

        binding.title.text = song.title
        binding.artist.text = song.artistName
        binding.songInfo.text = getSongInfo(song)
    }

    override fun setColor(color: MediaNotificationProcessor) {
        val controlsColor = if (PreferenceUtil.isAdaptiveColor) {
            color.secondaryTextColor
        } else {
            if (PreferenceUtil.getGeneralThemeValue() == ThemeMode.MD3) {
                ContextCompat.getColor(requireContext(), R.color.m3_widget_other_text)
            } else {
                ThemeStore.accentColor(requireContext())
            }
        }

        val lastPlaybackColor = if (PreferenceUtil.isAdaptiveColor) {
            ColorUtil.getComplimentColor(color.secondaryTextColor)
        } else {
            if (PreferenceUtil.getGeneralThemeValue() == ThemeMode.MD3) {
                ContextCompat.getColor(requireContext(), R.color.m3_widget_foreground)
            } else {
                ColorUtil.getComplimentColor(accentColor())
            }
        }

        binding.playPauseButton.setColorFilter(controlsColor, PorterDuff.Mode.SRC_IN)

        binding.progressSlider.applyColor(controlsColor)
        lastPlaybackControlsColor = lastPlaybackColor
        lastDisabledPlaybackControlsColor = controlsColor

        binding.songCurrentProgress.setTextColor(controlsColor)
        binding.songTotalTime.setTextColor(controlsColor)

        binding.title.setTextColor(controlsColor)
        binding.artist.setTextColor(controlsColor)
        binding.songInfo.setTextColor(controlsColor)

        binding.nextButton.setColorFilter(controlsColor, PorterDuff.Mode.SRC_IN)
        binding.previousButton.setColorFilter(controlsColor, PorterDuff.Mode.SRC_IN)

        updateRepeatState()
        updateShuffleState()
    }

    private fun updatePlayPauseDrawableState() {
        if (MusicPlayerRemote.isPlaying) {
            binding.playPauseButton.setImageResource(R.drawable.ic_pause)
        } else {
            binding.playPauseButton.setImageResource(R.drawable.ic_play_arrow_white_32dp)
        }
    }

    private fun setUpPlayPauseFab() {
        TintHelper.setTintAuto(binding.playPauseButton, Color.WHITE, true)
        TintHelper.setTintAuto(binding.playPauseButton, Color.BLACK, false)
        binding.playPauseButton.setOnClickListener(PlayPauseButtonOnClickHandler())
    }

    override fun onPlayStateChanged() {
        super.onPlayStateChanged()
        updatePlayPauseDrawableState()

        (seekBar.progressDrawable as? SquigglyProgress)?.animate = MusicPlayerRemote.isPlaying
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        updatePlayPauseDrawableState()

        (seekBar.progressDrawable as? SquigglyProgress)?.animate = MusicPlayerRemote.isPlaying
    }

    override fun onRepeatModeChanged() {
        updateRepeatState()
    }

    override fun onShuffleModeChanged() {
        updateShuffleState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}