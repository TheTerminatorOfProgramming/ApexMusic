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

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.ttop.app.apex.R
import com.ttop.app.apex.databinding.FragmentCardPlayerBinding
import com.ttop.app.apex.extensions.drawAboveSystemBars
import com.ttop.app.apex.extensions.whichFragment
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.model.Song
import com.ttop.app.apex.ui.fragments.base.AbsPlayerFragment
import com.ttop.app.apex.ui.fragments.player.PlayerAlbumCoverFragment
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.color.MediaNotificationProcessor
import com.ttop.app.appthemehelper.util.ToolbarContentTintHelper
import kotlinx.coroutines.DelicateCoroutinesApi

class CardFragment : AbsPlayerFragment(R.layout.fragment_card_player) {
    override fun playerToolbar(): Toolbar {
        return binding.playerToolbar
    }

    private var lastColor: Int = 0
    override val paletteColor: Int
        get() = lastColor

    private lateinit var playbackControlsFragment: CardPlaybackControlsFragment
    private var _binding: FragmentCardPlayerBinding? = null
    private val binding get() = _binding!!


    override fun onShow() {
        playbackControlsFragment.show()
    }

    override fun onHide() {
        playbackControlsFragment.hide()
        onBackPressed()
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun toolbarIconColor(): Int {
        return if (PreferenceUtil.isAdaptiveColor) {
            lastColor
        }else {
            ContextCompat.getColor(requireContext(), R.color.md_white_1000)
        }
    }

    override fun onColorChanged(color: MediaNotificationProcessor) {
        playbackControlsFragment.setColor(color)
        lastColor = color.secondaryTextColor
        libraryViewModel.updateColor(color.secondaryTextColor)
        ToolbarContentTintHelper.colorizeToolbar(binding.playerToolbar, toolbarIconColor(), activity)
    }

    override fun toggleFavorite(song: Song) {
        super.toggleFavorite(song)
        if (song.id == MusicPlayerRemote.currentSong.id) {
            updateIsFavorite()
        }
    }

    override fun onFavoriteToggled() {
        toggleFavorite(MusicPlayerRemote.currentSong)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCardPlayerBinding.bind(view)
        setUpSubFragments()
        setUpPlayerToolbar()
        (binding.playbackControlsFragment.parent as View).drawAboveSystemBars()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun setUpSubFragments() {
        playbackControlsFragment = whichFragment(R.id.playbackControlsFragment)
        val playerAlbumCoverFragment: PlayerAlbumCoverFragment =
            whichFragment(R.id.playerAlbumCoverFragment)
        playerAlbumCoverFragment.setCallbacks(this)
        /*GlobalScope.launch {
            playerAlbumCoverFragment.removeSlideEffect()
        }*/
    }

    private fun setUpPlayerToolbar() {
        binding.playerToolbar.apply {
            inflateMenu(R.menu.menu_player)
            setNavigationOnClickListener {
                if (!PreferenceUtil.isHapticFeedbackDisabled) {
                    requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                }

                mainActivity.collapsePanel()
            }
            setOnMenuItemClickListener(this@CardFragment)

            //ToolbarContentTintHelper.colorizeToolbar(this, Color.WHITE, activity)
        }

        when (PreferenceUtil.customToolbarAction) {
            "disabled" -> {
                binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_details).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_equalizer).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_playback_speed).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_share).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_volume).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            }
            "add_to_playlist" -> {
                binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                binding.playerToolbar.menu.findItem(R.id.action_details).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_equalizer).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_playback_speed).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_share).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_volume).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            }
            "details" -> {
                binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_details).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                binding.playerToolbar.menu.findItem(R.id.action_equalizer).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_playback_speed).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_share).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_volume).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            }
            "drive_mode" -> {
                binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_details).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_equalizer).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                binding.playerToolbar.menu.findItem(R.id.action_playback_speed).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_share).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_volume).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            }
            "equalizer" -> {
                binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_details).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_equalizer).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_playback_speed).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_share).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_volume).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            }
            "playback_settings" -> {
                binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_details).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_equalizer).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_playback_speed).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_share).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_volume).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            }
            "save_playing_queue" -> {
                binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_details).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_equalizer).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_playback_speed).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                binding.playerToolbar.menu.findItem(R.id.action_share).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_volume).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            }
            "share" -> {
                binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_details).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_equalizer).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_playback_speed).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_share).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                binding.playerToolbar.menu.findItem(R.id.action_volume).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            }
            "volume" -> {
                binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_details).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_equalizer).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_playback_speed).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_share).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_volume).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            }
        }
    }

    override fun onServiceConnected() {
        updateIsFavorite()
    }

    override fun onPlayingMetaChanged() {
        updateIsFavorite()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
