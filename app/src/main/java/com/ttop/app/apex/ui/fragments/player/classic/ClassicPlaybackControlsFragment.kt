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
package com.ttop.app.apex.ui.fragments.player.classic

import android.animation.Animator
import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateInterpolator
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.ttop.app.apex.R
import com.ttop.app.apex.databinding.FragmentClassicPlayerPlaybackControlsBinding
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.extensions.applyColor
import com.ttop.app.apex.extensions.getSongInfo
import com.ttop.app.apex.extensions.ripAlpha
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.helper.PlayPauseButtonOnClickHandler
import com.ttop.app.apex.ui.fragments.base.AbsPlayerControlsFragment
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.color.MediaNotificationProcessor
import com.ttop.app.apex.views.SquigglyProgress
import com.ttop.app.appthemehelper.util.ATHUtil
import com.ttop.app.appthemehelper.util.ColorUtil
import com.ttop.app.appthemehelper.util.MaterialValueHelper
import com.ttop.app.appthemehelper.util.TintHelper
import kotlin.math.sqrt

class ClassicPlaybackControlsFragment :
    AbsPlayerControlsFragment(R.layout.fragment_classic_player_playback_controls) {

    private var _binding: FragmentClassicPlayerPlaybackControlsBinding? = null
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentClassicPlayerPlaybackControlsBinding.bind(view)
        setUpMusicControllers()
        binding.playPauseButton.setOnClickListener {
            if (MusicPlayerRemote.isPlaying) {
                MusicPlayerRemote.pauseSong()
            } else {
                MusicPlayerRemote.resumePlaying()
            }
        }
        binding.title.isSelected = true
        binding.artist.isSelected = true
    }

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

    override fun setColor(color: MediaNotificationProcessor) {
        val colorFinal = accentColor().ripAlpha()

        if (PreferenceUtil.isAdaptiveColor) {
            binding.progressSlider.applyColor(color.secondaryTextColor)
            binding.title.setTextColor(color.secondaryTextColor)
            binding.artist.setTextColor(color.secondaryTextColor)
            binding.songInfo.setTextColor(color.secondaryTextColor)
            binding.songCurrentProgress.setTextColor(color.secondaryTextColor)
            binding.songTotalTime.setTextColor(color.secondaryTextColor)

            binding.playPauseButton.setColorFilter(color.backgroundColor)
            binding.playPauseCard.setCardBackgroundColor(color.secondaryTextColor)

            lastPlaybackControlsColor = color.secondaryTextColor
            lastDisabledPlaybackControlsColor = com.ttop.app.apex.util.ColorUtil.getComplimentColor(color.secondaryTextColor)
        }else {
            binding.progressSlider.applyColor(colorFinal)
            TintHelper.setTintAuto(
                binding.playPauseButton,
                MaterialValueHelper.getPrimaryTextColor(
                    requireContext(),
                    ColorUtil.isColorLight(colorFinal)
                ),
                false
            )

            binding.playPauseCard.setCardBackgroundColor(colorFinal)

            val colorBg = ATHUtil.resolveColor(requireContext(), android.R.attr.colorBackground)
            if (ColorUtil.isColorLight(colorBg)) {
                lastPlaybackControlsColor =
                    MaterialValueHelper.getSecondaryTextColor(requireContext(), true)
                lastDisabledPlaybackControlsColor =
                    MaterialValueHelper.getSecondaryDisabledTextColor(requireContext(), true)

                binding.title.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_black_1000))
                binding.artist.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_black_1000))
                binding.songInfo.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_black_1000))
                binding.songCurrentProgress.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_black_1000))
                binding.songTotalTime.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_black_1000))
            } else {
                lastPlaybackControlsColor =
                    MaterialValueHelper.getPrimaryTextColor(requireContext(), false)
                lastDisabledPlaybackControlsColor =
                    MaterialValueHelper.getPrimaryDisabledTextColor(requireContext(), false)

                binding.title.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                binding.artist.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                binding.songInfo.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                binding.songCurrentProgress.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                binding.songTotalTime.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
            }
        }

        updateRepeatState()
        updateShuffleState()
        updatePrevNextColor()
        updatePlayPauseDrawableState()
    }

    private fun updateSong() {
        val song = MusicPlayerRemote.currentSong
        binding.title.text = song.title
        binding.artist.text = song.artistName

        binding.songInfo.text = getSongInfo(song)
        binding.songInfo.isSelected = true
    }

    override fun onServiceConnected() {
        updatePlayPauseDrawableState()
        updateRepeatState()
        updateShuffleState()
        updateSong()
        (seekBar.progressDrawable as? SquigglyProgress)?.animate = MusicPlayerRemote.isPlaying
    }

    override fun onPlayingMetaChanged() {
        updateSong()
    }

    override fun onPlayStateChanged() {
        updatePlayPauseDrawableState()
        (seekBar.progressDrawable as? SquigglyProgress)?.animate = MusicPlayerRemote.isPlaying
    }

    override fun onRepeatModeChanged() {
        updateRepeatState()
    }

    override fun onShuffleModeChanged() {
        updateShuffleState()
    }

    private fun setUpPlayPauseFab() {
        binding.playPauseButton.setOnClickListener(PlayPauseButtonOnClickHandler())
        updatePlayPauseDrawableState()
    }

    private fun updatePlayPauseDrawableState() {
        if (MusicPlayerRemote.isPlaying) {
            binding.playPauseButton.setImageResource(R.drawable.ic_pause_outline_small)
            //binding.playPauseCard.animateRadius(40F)
        } else {
            binding.playPauseButton.setImageResource(R.drawable.ic_play_arrow_outline_small)
            //binding.playPauseCard.animateToCircle()
        }
    }

    private fun setUpMusicControllers() {
        setUpPlayPauseFab()
    }

    public override fun show() {}

    public override fun hide() {}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}