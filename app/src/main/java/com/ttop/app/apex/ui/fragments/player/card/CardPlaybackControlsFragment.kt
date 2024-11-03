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
package com.ttop.app.apex.ui.fragments.player.card

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.ttop.app.apex.R
import com.ttop.app.apex.databinding.FragmentCardPlayerPlaybackControlsBinding
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.extensions.applyColor
import com.ttop.app.apex.extensions.getSongInfo
import com.ttop.app.apex.extensions.surfaceColor
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.libraries.appthemehelper.util.TintHelper
import com.ttop.app.apex.ui.fragments.base.AbsPlayerControlsFragment
import com.ttop.app.apex.util.ColorUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.color.MediaNotificationProcessor

class CardPlaybackControlsFragment :
    AbsPlayerControlsFragment(R.layout.fragment_card_player_playback_controls) {


    private var _binding: FragmentCardPlayerPlaybackControlsBinding? = null
    private val binding get() = _binding!!

    override val seekBar: SeekBar
        get() = binding.progressSlider

    override val shuffleButton: ImageButton
        get() = binding.mediaButton.shuffleButton

    override val repeatButton: ImageButton
        get() = binding.mediaButton.repeatButton

    override val nextButton: ImageButton
        get() = binding.mediaButton.nextButton

    override val previousButton: ImageButton
        get() = binding.mediaButton.previousButton

    override val songTotalTime: TextView
        get() = binding.songTotalTime

    override val songCurrentProgress: TextView
        get() = binding.songCurrentProgress

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCardPlayerPlaybackControlsBinding.bind(view)
        setUpPlayPauseFab()
        binding.title.isSelected = true
        binding.artist.isSelected = true
    }

    private fun updateSong() {
        val song = MusicPlayerRemote.currentSong
        binding.title.text = song.title
        binding.artist.text = song.artistName

        binding.songInfo.text = getSongInfo(MusicPlayerRemote.currentSong)
        binding.songInfo.isSelected = true
    }

    override fun onServiceConnected() {
        updatePlayPauseDrawableState()
        updateRepeatState()
        updateShuffleState()
        updateSong()
    }

    override fun onPlayingMetaChanged() {
        super.onPlayingMetaChanged()
        updateSong()
    }

    override fun onPlayStateChanged() {
        updatePlayPauseDrawableState()
    }

    override fun onRepeatModeChanged() {
        updateRepeatState()
    }

    override fun onShuffleModeChanged() {
        updateShuffleState()
    }

    override fun setColor(color: MediaNotificationProcessor) {
        val controlsColor = if (PreferenceUtil.isAdaptiveColor) {
            if (PreferenceUtil.isPlayerBackgroundType) {
                ColorUtil.getComplimentColor(color.secondaryTextColor)
            } else {
                color.secondaryTextColor
            }
        } else {
            if (PreferenceUtil.materialYou) {
                ContextCompat.getColor(requireContext(), R.color.m3_widget_other_text)
            } else {
                accentColor()
            }
        }

        val lastPlaybackColor = if (PreferenceUtil.isAdaptiveColor) {
            ColorUtil.getComplimentColor(color.secondaryTextColor)
        } else {
            if (PreferenceUtil.materialYou) {
                ContextCompat.getColor(requireContext(), R.color.m3_widget_foreground)
            } else {
                ColorUtil.getComplimentColor(accentColor())
            }
        }

        val backgroundColor = if (PreferenceUtil.isAdaptiveColor) {
            color.backgroundColor
        } else {
            if (PreferenceUtil.materialYou) {
                ContextCompat.getColor(
                    requireContext(),
                    R.color.m3_widget_background
                )
            } else {
                context?.surfaceColor()
            }
        }

        if (PreferenceUtil.isAdaptiveColor) {
            binding.colorBackground.setBackgroundColor(
                color.backgroundColor
            )
        } else {
            if (PreferenceUtil.materialYou) {
                binding.colorBackground.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.m3_widget_background
                    )
                )
            } else {
                binding.colorBackground.setBackgroundColor(
                    surfaceColor()
                )
            }
        }

        binding.progressSlider.applyColor(controlsColor)
        binding.title.setTextColor(controlsColor)
        binding.artist.setTextColor(controlsColor)
        binding.songInfo.setTextColor(controlsColor)
        binding.songCurrentProgress.setTextColor(controlsColor)
        binding.songTotalTime.setTextColor(controlsColor)
        binding.image.setColorFilter(controlsColor, PorterDuff.Mode.SRC_IN)

        TintHelper.setTintAuto(
            binding.mediaButton.playPauseButton,
            backgroundColor!!,
            false
        )

        TintHelper.setTintAuto(
            binding.mediaButton.playPauseButton,
            controlsColor,
            true
        )

        binding.mediaButton.nextButton.setColorFilter(controlsColor, PorterDuff.Mode.SRC_IN)
        binding.mediaButton.previousButton.setColorFilter(controlsColor, PorterDuff.Mode.SRC_IN)

        lastPlaybackControlsColor =
            lastPlaybackColor
        lastDisabledPlaybackControlsColor =
            controlsColor

        updateRepeatState()
        updateShuffleState()
    }

    private fun setUpPlayPauseFab() {
        binding.mediaButton.playPauseButton.setOnClickListener {
            if (MusicPlayerRemote.isPlaying) {
                MusicPlayerRemote.pauseSong()
            } else {
                MusicPlayerRemote.resumePlaying()
            }
            it.showBounceAnimation()
        }
    }

    private fun updatePlayPauseDrawableState() {
        if (MusicPlayerRemote.isPlaying) {
            binding.mediaButton.playPauseButton.setImageResource(R.drawable.ic_pause)
        } else {
            binding.mediaButton.playPauseButton.setImageResource(R.drawable.ic_play_arrow_white_32dp)
        }
    }

    public override fun show() {}

    public override fun hide() {}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
