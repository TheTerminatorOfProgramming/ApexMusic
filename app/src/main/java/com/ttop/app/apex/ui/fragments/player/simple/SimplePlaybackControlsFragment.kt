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
package com.ttop.app.apex.ui.fragments.player.simple

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageButton
import com.ttop.app.apex.R
import com.ttop.app.apex.databinding.FragmentSimpleControlsFragmentBinding
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.extensions.getSongInfo
import com.ttop.app.apex.extensions.hide
import com.ttop.app.apex.extensions.show
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.ui.fragments.base.AbsPlayerControlsFragment
import com.ttop.app.apex.ui.fragments.base.goToAlbum
import com.ttop.app.apex.ui.fragments.base.goToArtist
import com.ttop.app.apex.util.MusicUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.color.MediaNotificationProcessor
import com.ttop.app.appthemehelper.util.ATHUtil
import com.ttop.app.appthemehelper.util.ColorUtil
import com.ttop.app.appthemehelper.util.MaterialValueHelper
import com.ttop.app.appthemehelper.util.TintHelper

/**
 * @author Hemanth S (h4h13).
 */

class SimplePlaybackControlsFragment :
    AbsPlayerControlsFragment(R.layout.fragment_simple_controls_fragment) {

    private var _binding: FragmentSimpleControlsFragmentBinding? = null
    private val binding get() = _binding!!

    override val shuffleButton: ImageButton
        get() = binding.shuffleButton

    override val repeatButton: ImageButton
        get() = binding.repeatButton

    override val nextButton: ImageButton
        get() = binding.nextButton

    override val previousButton: ImageButton
        get() = binding.previousButton

    override fun onPlayStateChanged() {
        updatePlayPauseDrawableState()
    }

    override fun onRepeatModeChanged() {
        updateRepeatState()
    }

    override fun onShuffleModeChanged() {
        updateShuffleState()
    }

    override fun onServiceConnected() {
        updatePlayPauseDrawableState()
        updateRepeatState()
        updateShuffleState()
        updateSong()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSimpleControlsFragmentBinding.bind(view)
        setUpPlayPauseFab()
        binding.title.isSelected = true
        binding.artist.isSelected = true

        binding.title.setOnClickListener {
            goToAlbum(requireActivity())
        }
        binding.artist.setOnClickListener {
            goToArtist(requireActivity())
        }
    }

    private fun updateSong() {
        val song = MusicPlayerRemote.currentSong
        binding.title.text = song.title
        binding.artist.text = song.artistName

        if (PreferenceUtil.isSongInfo) {
            binding.songInfo.text = getSongInfo(song)
            binding.songInfo.show()
            binding.songInfo.isSelected = true
        } else {
            binding.songInfo.hide()
        }
    }

    override fun onPlayingMetaChanged() {
        super.onPlayingMetaChanged()
        updateSong()
    }

    public override fun show() {
        binding.playPauseButton.animate()
            .scaleX(1f)
            .scaleY(1f)
            .rotation(360f)
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

    override fun onUpdateProgressViews(progress: Int, total: Int) {
        binding.songCurrentProgress.text = String.format(
            "%s / %s",
            MusicUtil.getReadableDurationString(progress.toLong()),
            MusicUtil.getReadableDurationString(total.toLong())
        )
    }

    override fun setColor(color: MediaNotificationProcessor) {
        val colorFinal = if (PreferenceUtil.isAdaptiveColor) {
            color.secondaryTextColor
        } else {
            accentColor()
        }

        TintHelper.setTintAuto(
            binding.playPauseButton,
            MaterialValueHelper.getPrimaryTextColor(
                requireContext(),
                ColorUtil.isColorLight(colorFinal)
            ),
            false
        )
        TintHelper.setTintAuto(binding.playPauseButton, colorFinal, true)
        binding.artist.setTextColor(colorFinal)

        if (PreferenceUtil.isAdaptiveColor) {
            binding.title.setTextColor(colorFinal)
            binding.songCurrentProgress.setTextColor(colorFinal)
            binding.songInfo.setTextColor(colorFinal)

            val colorBg = ATHUtil.resolveColor(requireContext(), android.R.attr.colorBackground)
            if (ColorUtil.isColorLight(colorBg)) {
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
        }else {
            val colorBg = ATHUtil.resolveColor(requireContext(), android.R.attr.colorBackground)
            if (ColorUtil.isColorLight(colorBg)) {
                lastPlaybackControlsColor =
                    MaterialValueHelper.getSecondaryTextColor(requireContext(), true)
                lastDisabledPlaybackControlsColor =
                    MaterialValueHelper.getSecondaryDisabledTextColor(requireContext(), true)

                context?.resources?.let { binding.title.setTextColor(it.getColor(R.color.md_black_1000)) }
                context?.resources?.let { binding.songInfo.setTextColor(it.getColor(R.color.md_black_1000)) }
                context?.resources?.let { binding.songCurrentProgress.setTextColor(it.getColor(R.color.md_black_1000)) }
            } else {
                lastPlaybackControlsColor =
                    MaterialValueHelper.getPrimaryTextColor(requireContext(), false)
                lastDisabledPlaybackControlsColor =
                    MaterialValueHelper.getPrimaryDisabledTextColor(requireContext(), false)

                context?.resources?.let { binding.title.setTextColor(it.getColor(R.color.md_white_1000)) }
                context?.resources?.let { binding.songInfo.setTextColor(it.getColor(R.color.md_white_1000)) }
                context?.resources?.let { binding.songCurrentProgress.setTextColor(it.getColor(R.color.md_white_1000)) }
            }
        }

        updateRepeatState()
        updateShuffleState()
        updatePrevNextColor()
    }

    private fun setUpPlayPauseFab() {
        binding.playPauseButton.setOnClickListener {
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
            binding.playPauseButton.setImageResource(R.drawable.ic_pause)
        } else {
            binding.playPauseButton.setImageResource(R.drawable.ic_play_arrow_white_32dp)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
