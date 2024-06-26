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
import com.ttop.app.apex.extensions.*
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.ui.fragments.base.AbsPlayerControlsFragment
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.color.MediaNotificationProcessor
import com.ttop.app.appthemehelper.util.ATHUtil
import com.ttop.app.appthemehelper.util.ColorUtil
import com.ttop.app.appthemehelper.util.MaterialValueHelper
import com.ttop.app.appthemehelper.util.TintHelper

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
        val colorFinal = if (PreferenceUtil.isAdaptiveColor) {
            color.secondaryTextColor
        } else {
            accentColor().ripAlpha()
        }

        if (PreferenceUtil.isAdaptiveColor) {
            binding.songTotalTime.setTextColor(color.secondaryTextColor)
            binding.songCurrentProgress.setTextColor(color.secondaryTextColor)

            lastPlaybackControlsColor = color.secondaryTextColor
            lastDisabledPlaybackControlsColor = com.ttop.app.apex.util.ColorUtil.getComplimentColor(color.secondaryTextColor)

            binding.title.setTextColor(color.secondaryTextColor)
            binding.artist.setTextColor(color.secondaryTextColor)
            binding.songInfo.setTextColor(color.secondaryTextColor)
        }else {
            if (!ATHUtil.isWindowBackgroundDark(requireContext())
            ) {
                lastPlaybackControlsColor = MaterialValueHelper.getSecondaryTextColor(activity, true)
                lastDisabledPlaybackControlsColor =
                    MaterialValueHelper.getSecondaryDisabledTextColor(activity, true)
            } else {
                lastPlaybackControlsColor = MaterialValueHelper.getPrimaryTextColor(activity, false)
                lastDisabledPlaybackControlsColor =
                    MaterialValueHelper.getPrimaryDisabledTextColor(activity, false)
            }

            val colorBg = ATHUtil.resolveColor(requireContext(), android.R.attr.colorBackground)
            if (ColorUtil.isColorLight(colorBg)) {
                binding.songTotalTime.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_black_1000))
                binding.songCurrentProgress.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_black_1000))
            }else {
                binding.songTotalTime.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                binding.songCurrentProgress.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
            }
        }

        if (PreferenceUtil.isAdaptiveColor) {
            binding.colorBackground.setBackgroundColor(color.backgroundColor)
        }
        seekBar.applyColor(colorFinal)
        binding.image.setColorFilter(colorFinal, PorterDuff.Mode.SRC_IN)
        TintHelper.setTintAuto(
            binding.mediaButton.playPauseButton,
            MaterialValueHelper.getPrimaryTextColor(context, ColorUtil.isColorLight(colorFinal)),
            false
        )
        TintHelper.setTintAuto(binding.mediaButton.playPauseButton, colorFinal, true)

        updateRepeatState()
        updateShuffleState()
        updatePrevNextColor()
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
