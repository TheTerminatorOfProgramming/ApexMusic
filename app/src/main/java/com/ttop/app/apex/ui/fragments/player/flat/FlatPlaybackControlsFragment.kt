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
package com.ttop.app.apex.ui.fragments.player.flat

import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import com.ttop.app.apex.R
import com.ttop.app.apex.databinding.FragmentFlatPlayerPlaybackControlsBinding
import com.ttop.app.apex.extensions.*
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.helper.MusicProgressViewUpdateHelper.Callback
import com.ttop.app.apex.helper.PlayPauseButtonOnClickHandler
import com.ttop.app.apex.ui.fragments.base.AbsPlayerControlsFragment
import com.ttop.app.apex.ui.fragments.base.goToAlbum
import com.ttop.app.apex.ui.fragments.base.goToArtist
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.color.MediaNotificationProcessor
import com.ttop.app.appthemehelper.util.ATHUtil
import com.ttop.app.appthemehelper.util.ColorUtil
import com.ttop.app.appthemehelper.util.MaterialValueHelper
import com.ttop.app.appthemehelper.util.TintHelper

class FlatPlaybackControlsFragment :
    AbsPlayerControlsFragment(R.layout.fragment_flat_player_playback_controls), Callback {

    private var _binding: FragmentFlatPlayerPlaybackControlsBinding? = null
    private val binding get() = _binding!!

    override val seekBar: SeekBar
        get() = binding.progressSlider

    override val shuffleButton: ImageButton
        get() = binding.shuffleButton

    override val repeatButton: ImageButton
        get() = binding.repeatButton

    override val nextButton: ImageButton?
        get() = null

    override val previousButton: ImageButton?
        get() = null

    override val songTotalTime: TextView
        get() = binding.songTotalTime

    override val songCurrentProgress: TextView
        get() = binding.songCurrentProgress

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFlatPlayerPlaybackControlsBinding.bind(view)
        binding.playPauseButton.setOnClickListener(PlayPauseButtonOnClickHandler())
        binding.title.isSelected = true
        binding.album.isSelected = true
        binding.artist.isSelected = true

        binding.title.setOnClickListener {
            goToAlbum(requireActivity())
        }
        binding.artist.setOnClickListener {
            goToArtist(requireActivity())
        }
    }

    public override fun show() {
        binding.playPauseButton.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    public override fun hide() {
        binding.playPauseButton.apply {
            scaleX = 0f
            scaleY = 0f
            rotation = 0f
        }
    }

    override fun setColor(color: MediaNotificationProcessor) {
        if (ATHUtil.isWindowBackgroundDark(requireContext())) {
            lastPlaybackControlsColor =
                MaterialValueHelper.getSecondaryTextColor(requireContext(), false)
            lastDisabledPlaybackControlsColor =
                MaterialValueHelper.getSecondaryDisabledTextColor(requireContext(), false)
        } else {
            lastPlaybackControlsColor =
                MaterialValueHelper.getPrimaryTextColor(requireContext(), true)
            lastDisabledPlaybackControlsColor =
                MaterialValueHelper.getPrimaryDisabledTextColor(requireContext(), true)
        }

        val colorFinal = if (PreferenceUtil.isAdaptiveColor) {
            color.secondaryTextColor
        } else {
            accentColor().ripAlpha()
        }

        updateTextColors(colorFinal)
        volumeFragment?.setTintable(colorFinal)
        binding.progressSlider.applyColor(colorFinal)
        updateRepeatState()
        updateShuffleState()
    }

    private fun updateTextColors(color: Int) {
        val isDark = ColorUtil.isColorLight(color)
        val darkColor = ColorUtil.darkenColor(color)
        val colorPrimary = MaterialValueHelper.getPrimaryTextColor(context, isDark)
        val colorSecondary =
            MaterialValueHelper.getSecondaryTextColor(context, ColorUtil.isColorLight(darkColor))

        TintHelper.setTintAuto(binding.playPauseButton, colorPrimary, false)
        TintHelper.setTintAuto(binding.playPauseButton, color, true)

        binding.title.setBackgroundColor(color)
        binding.title.setTextColor(colorPrimary)
        binding.album.setBackgroundColor(darkColor)
        binding.album.setTextColor(colorSecondary)
        binding.artist.setBackgroundColor(darkColor)
        binding.artist.setTextColor(colorSecondary)
        binding.songInfo.setBackgroundColor(darkColor)
        binding.songInfo.setTextColor(colorSecondary)
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

    private fun updatePlayPauseDrawableState() {
        if (MusicPlayerRemote.isPlaying) {
            binding.playPauseButton.setImageResource(R.drawable.ic_pause)
        } else {
            binding.playPauseButton.setImageResource(R.drawable.ic_play_arrow_white_32dp)
        }
    }

    private fun updateSong() {
        val song = MusicPlayerRemote.currentSong
        binding.title.text = song.title
        binding.album.text = song.albumName
        binding.artist.text = song.artistName

        if (PreferenceUtil.isSongInfo) {
            binding.songInfo.text = getSongInfo(song)
            binding.songInfo.show()
            binding.songInfo.isSelected = true
        } else {
            binding.songInfo.hide()
        }
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