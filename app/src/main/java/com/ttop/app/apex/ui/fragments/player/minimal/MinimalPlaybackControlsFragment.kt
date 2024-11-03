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
package com.ttop.app.apex.ui.fragments.player.minimal

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import com.ttop.app.apex.R
import com.ttop.app.apex.databinding.FragmentMinimalControlsFragmentBinding
import com.ttop.app.apex.libraries.appthemehelper.util.ATHColorUtil
import com.ttop.app.apex.ui.fragments.base.AbsPlayerControlsFragment
import com.ttop.app.apex.util.color.MediaNotificationProcessor

class MinimalPlaybackControlsFragment :
    AbsPlayerControlsFragment(R.layout.fragment_minimal_controls_fragment) {
    private var _binding: FragmentMinimalControlsFragmentBinding? = null
    private val binding get() = _binding!!

    override val shuffleButton: ImageButton
        get() = binding.shuffleButton

    override val repeatButton: ImageButton
        get() = binding.repeatButton

    override fun show() {}

    override fun hide() {}

    override fun setColor(color: MediaNotificationProcessor) {
        lastPlaybackControlsColor = color.secondaryTextColor
        lastDisabledPlaybackControlsColor = ATHColorUtil.withAlpha(color.secondaryTextColor, 0.25f)

        updateRepeatState()
        updateShuffleState()
    }

    override fun onUpdateProgressViews(progress: Int, total: Int) {
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMinimalControlsFragmentBinding.bind(view)
    }

    override fun onServiceConnected() {
        updateRepeatState()
        updateShuffleState()
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
