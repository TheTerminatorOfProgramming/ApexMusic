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
import com.ttop.app.apex.extensions.applyColor
import com.ttop.app.apex.extensions.getSongInfo
import com.ttop.app.apex.extensions.hide
import com.ttop.app.apex.extensions.show
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.helper.PlayPauseButtonOnClickHandler
import com.ttop.app.apex.ui.fragments.base.AbsPlayerControlsFragment
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.color.MediaNotificationProcessor
import com.ttop.app.apex.views.SquigglyProgress
import com.ttop.app.appthemehelper.ThemeStore
import com.ttop.app.appthemehelper.util.ATHUtil
import com.ttop.app.appthemehelper.util.ColorUtil
import com.ttop.app.appthemehelper.util.MaterialValueHelper
import com.ttop.app.appthemehelper.util.TintHelper
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

        binding.titleArtist?.isSelected = true
        binding.title?.isSelected = true
        binding.artist?.isSelected = true
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
            duration = 200
            interpolator = AccelerateInterpolator()
        }

    }

    fun updateSong() {
        val song = MusicPlayerRemote.currentSong

        val string: StringBuilder = StringBuilder()
        string.append(song.title).append(" • ").append(song.artistName)

        binding.titleArtist?.text = string
        binding.title?.text = song.title
        binding.artist?.text = song.artistName
        binding.songInfo.text = getSongInfo(song)

        if (PreferenceUtil.isSongInfo) {
            binding.songInfo.show()
        } else {
            binding.songInfo.hide()
        }
    }

    override fun setColor(color: MediaNotificationProcessor) {
        val controlsColor =
            if (PreferenceUtil.isAdaptiveColor) {
                color.secondaryTextColor
            } else {
                ThemeStore.accentColor(requireContext())
            }

        binding.progressSlider.applyColor(controlsColor)
        binding.playPauseButton.setColorFilter(controlsColor, PorterDuff.Mode.SRC_IN)
        binding.nextButton.setColorFilter(controlsColor, PorterDuff.Mode.SRC_IN)
        binding.previousButton.setColorFilter(controlsColor, PorterDuff.Mode.SRC_IN)

        volumeFragment?.tint(controlsColor)

        if (PreferenceUtil.isAdaptiveColor) {
            if (!ATHUtil.isWindowBackgroundDark(requireContext())) {
                lastPlaybackControlsColor =
                    color.secondaryTextColor
                lastDisabledPlaybackControlsColor =
                    MaterialValueHelper.getSecondaryDisabledTextColor(requireContext(), true)
            } else {
                lastPlaybackControlsColor =
                    color.secondaryTextColor
                lastDisabledPlaybackControlsColor =
                    MaterialValueHelper.getPrimaryDisabledTextColor(requireContext(), false)
            }
            binding.songCurrentProgress.setTextColor(color.secondaryTextColor)
            binding.songTotalTime.setTextColor(color.secondaryTextColor)
        }else {
            if (!ATHUtil.isWindowBackgroundDark(requireContext())) {
                lastPlaybackControlsColor =
                    MaterialValueHelper.getSecondaryTextColor(requireContext(), true)
                lastDisabledPlaybackControlsColor =
                    MaterialValueHelper.getSecondaryDisabledTextColor(requireContext(), true)
            } else {
                lastPlaybackControlsColor =
                    MaterialValueHelper.getPrimaryTextColor(requireContext(), false)
                lastDisabledPlaybackControlsColor =
                    MaterialValueHelper.getPrimaryDisabledTextColor(requireContext(), false)
            }

            val colorBg = ATHUtil.resolveColor(requireContext(), android.R.attr.colorBackground)
            if (ColorUtil.isColorLight(colorBg)) {
                binding.songCurrentProgress.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_black_1000))
                binding.songTotalTime.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_black_1000))
            }else {
                binding.songCurrentProgress.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                binding.songTotalTime.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
            }
        }

        if (PreferenceUtil.isAdaptiveColor) {
            binding.titleArtist?.setTextColor(color.secondaryTextColor)
            binding.title?.setTextColor(color.secondaryTextColor)
            binding.artist?.setTextColor(color.secondaryTextColor)
            binding.songInfo.setTextColor(color.secondaryTextColor)

        }else {
            val colorBg = ATHUtil.resolveColor(requireContext(), android.R.attr.colorBackground)
            if (ColorUtil.isColorLight(colorBg)) {
                binding.titleArtist?.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.md_black_1000
                    )
                )
                binding.title?.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.md_black_1000
                    )
                )
                binding.artist?.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.md_black_1000
                    )
                )
                binding.songInfo.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.md_black_1000
                    )
                )
            } else {
                binding.titleArtist?.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.md_white_1000
                    )
                )
                binding.title?.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.md_white_1000
                    )
                )
                binding.artist?.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.md_white_1000
                    )
                )
                binding.songInfo.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.md_white_1000
                    )
                )
            }
        }

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