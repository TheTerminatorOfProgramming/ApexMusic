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

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.GestureDetector
import android.view.HapticFeedbackConstants
import android.view.MenuItem.SHOW_AS_ACTION_ALWAYS
import android.view.MenuItem.SHOW_AS_ACTION_NEVER
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.appcompat.widget.Toolbar
import androidx.core.content.getSystemService
import com.ttop.app.apex.R
import com.ttop.app.apex.databinding.FragmentMinimalPlayerBinding
import com.ttop.app.apex.extensions.drawAboveSystemBars
import com.ttop.app.apex.extensions.getSongInfo
import com.ttop.app.apex.extensions.show
import com.ttop.app.apex.extensions.whichFragment
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.helper.MusicProgressViewUpdateHelper
import com.ttop.app.apex.helper.PlayPauseButtonOnClickHandler
import com.ttop.app.apex.libraries.appthemehelper.util.ToolbarContentTintHelper
import com.ttop.app.apex.model.Song
import com.ttop.app.apex.ui.fragments.base.AbsPlayerFragment
import com.ttop.app.apex.ui.fragments.player.PlayerAlbumCoverFragment
import com.ttop.app.apex.util.MusicUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.ViewUtil
import com.ttop.app.apex.util.color.MediaNotificationProcessor
import kotlin.math.abs

class TinyPlayerFragment : AbsPlayerFragment(R.layout.fragment_minimal_player),
    MusicProgressViewUpdateHelper.Callback {
    private var _binding: FragmentMinimalPlayerBinding? = null
    private val binding get() = _binding!!

    private var lastColor: Int = 0
    private var toolbarColor: Int = 0
    private var isDragEnabled = false
    lateinit var animator: ObjectAnimator

    override fun playerToolbar(): Toolbar {
        return binding.playerToolbar
    }

    override fun onShow() {}

    override fun onHide() {}

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun toolbarIconColor(): Int {
        return toolbarColor
    }

    override val paletteColor: Int
        get() = lastColor

    override fun onColorChanged(color: MediaNotificationProcessor) {
        lastColor = color.backgroundColor
        libraryViewModel.updateColor(color.backgroundColor)
        toolbarColor = color.secondaryTextColor
        controlsFragment.setColor(color)

        binding.title.setTextColor(color.primaryTextColor)
        binding.playerSongTotalTime.setTextColor(color.primaryTextColor)
        binding.text.setTextColor(color.secondaryTextColor)
        binding.songInfo.setTextColor(color.secondaryTextColor)
        ViewUtil.setProgressDrawable(binding.progressBar, color.backgroundColor)

        Handler(Looper.myLooper()!!).post {
            ToolbarContentTintHelper.colorizeToolbar(
                binding.playerToolbar,
                color.secondaryTextColor,
                requireActivity()
            )
        }
    }


    override fun onFavoriteToggled() {
        toggleFavorite(MusicPlayerRemote.currentSong)
    }

    private lateinit var controlsFragment: MinimalPlaybackControlsFragment
    private lateinit var progressViewUpdateHelper: MusicProgressViewUpdateHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressViewUpdateHelper = MusicProgressViewUpdateHelper(this)
    }

    override fun onResume() {
        super.onResume()
        progressViewUpdateHelper.start()
    }

    override fun onPause() {
        super.onPause()
        progressViewUpdateHelper.stop()
    }

    private fun updateSong() {
        val song = MusicPlayerRemote.currentSong
        binding.title.text = song.title
        binding.text.text = String.format("%s \n%s", song.albumName, song.artistName)

        binding.songInfo.text = getSongInfo(song)
        binding.songInfo.show()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMinimalPlayerBinding.bind(view)
        binding.title.isSelected = true
        binding.progressBar.setOnClickListener(PlayPauseButtonOnClickHandler())
        binding.progressBar.setOnTouchListener(ProgressHelper(requireContext()))

        setUpPlayerToolbar()
        setUpSubFragments()

        playerToolbar().drawAboveSystemBars()
    }

    private fun setUpSubFragments() {
        controlsFragment = whichFragment(R.id.playbackControlsFragment)
        val playerAlbumCoverFragment: PlayerAlbumCoverFragment =
            whichFragment(R.id.playerAlbumCoverFragment)
        playerAlbumCoverFragment.setCallbacks(this)
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
            setOnMenuItemClickListener(this@TinyPlayerFragment)
        }

        when (PreferenceUtil.customToolbarAction) {
            "disabled" -> {
                binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_details)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_equalizer)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode)
                    .setShowAsAction(SHOW_AS_ACTION_ALWAYS)
                binding.playerToolbar.menu.findItem(R.id.action_playback_speed)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_share)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_volume)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
            }

            "add_to_playlist" -> {
                binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist)
                    .setShowAsAction(SHOW_AS_ACTION_ALWAYS)
                binding.playerToolbar.menu.findItem(R.id.action_details)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_equalizer)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode)
                    .setShowAsAction(SHOW_AS_ACTION_ALWAYS)
                binding.playerToolbar.menu.findItem(R.id.action_playback_speed)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_share)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_volume)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
            }

            "details" -> {
                binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_details)
                    .setShowAsAction(SHOW_AS_ACTION_ALWAYS)
                binding.playerToolbar.menu.findItem(R.id.action_equalizer)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode)
                    .setShowAsAction(SHOW_AS_ACTION_ALWAYS)
                binding.playerToolbar.menu.findItem(R.id.action_playback_speed)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_share)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_volume)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
            }

            "drive_mode" -> {
                binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_details)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_equalizer)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode)
                    .setShowAsAction(SHOW_AS_ACTION_ALWAYS)
                binding.playerToolbar.menu.findItem(R.id.action_playback_speed)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_share)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_volume)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
            }

            "equalizer" -> {
                binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_details)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_equalizer)
                    .setShowAsAction(SHOW_AS_ACTION_ALWAYS)
                binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode)
                    .setShowAsAction(SHOW_AS_ACTION_ALWAYS)
                binding.playerToolbar.menu.findItem(R.id.action_playback_speed)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_share)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_volume)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
            }

            "playback_settings" -> {
                binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_details)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_equalizer)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode)
                    .setShowAsAction(SHOW_AS_ACTION_ALWAYS)
                binding.playerToolbar.menu.findItem(R.id.action_playback_speed)
                    .setShowAsAction(SHOW_AS_ACTION_ALWAYS)
                binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_share)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_volume)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
            }

            "save_playing_queue" -> {
                binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_details)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_equalizer)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode)
                    .setShowAsAction(SHOW_AS_ACTION_ALWAYS)
                binding.playerToolbar.menu.findItem(R.id.action_playback_speed)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue)
                    .setShowAsAction(SHOW_AS_ACTION_ALWAYS)
                binding.playerToolbar.menu.findItem(R.id.action_share)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_volume)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
            }

            "share" -> {
                binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_details)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_equalizer)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode)
                    .setShowAsAction(SHOW_AS_ACTION_ALWAYS)
                binding.playerToolbar.menu.findItem(R.id.action_playback_speed)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_share)
                    .setShowAsAction(SHOW_AS_ACTION_ALWAYS)
                binding.playerToolbar.menu.findItem(R.id.action_volume)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
            }

            "volume" -> {
                binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_details)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_equalizer)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode)
                    .setShowAsAction(SHOW_AS_ACTION_ALWAYS)
                binding.playerToolbar.menu.findItem(R.id.action_playback_speed)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_share)
                    .setShowAsAction(SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_volume)
                    .setShowAsAction(SHOW_AS_ACTION_ALWAYS)
            }
        }

        when (PreferenceUtil.customToolbarAction2) {
            "disabled" -> {
                if (binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "add_to_playlist") {
                    binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_details).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "details") {
                    binding.playerToolbar.menu.findItem(R.id.action_details)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_equalizer).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "equalizer") {
                    binding.playerToolbar.menu.findItem(R.id.action_equalizer)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "drive_mode") {
                    binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_playback_speed).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "playback_settings") {
                    binding.playerToolbar.menu.findItem(R.id.action_playback_speed)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "save_playing_queue") {
                    binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_share).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "share") {
                    binding.playerToolbar.menu.findItem(R.id.action_share)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_volume).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "volume") {
                    binding.playerToolbar.menu.findItem(R.id.action_volume)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }
            }

            "add_to_playlist" -> {
                binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist)
                    .setShowAsAction(SHOW_AS_ACTION_ALWAYS)

                if (binding.playerToolbar.menu.findItem(R.id.action_details).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "details") {
                    binding.playerToolbar.menu.findItem(R.id.action_details)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_equalizer).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "equalizer") {
                    binding.playerToolbar.menu.findItem(R.id.action_equalizer)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "drive_mode") {
                    binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_playback_speed).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "playback_settings") {
                    binding.playerToolbar.menu.findItem(R.id.action_playback_speed)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "save_playing_queue") {
                    binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_share).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "share") {
                    binding.playerToolbar.menu.findItem(R.id.action_share)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_volume).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "volume") {
                    binding.playerToolbar.menu.findItem(R.id.action_volume)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }
            }

            "details" -> {
                if (binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "add_to_playlist") {
                    binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                binding.playerToolbar.menu.findItem(R.id.action_details)
                    .setShowAsAction(SHOW_AS_ACTION_ALWAYS)

                if (binding.playerToolbar.menu.findItem(R.id.action_equalizer).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "equalizer") {
                    binding.playerToolbar.menu.findItem(R.id.action_equalizer)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "drive_mode") {
                    binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_playback_speed).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "playback_settings") {
                    binding.playerToolbar.menu.findItem(R.id.action_playback_speed)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "save_playing_queue") {
                    binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_share).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "share") {
                    binding.playerToolbar.menu.findItem(R.id.action_share)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_volume).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "volume") {
                    binding.playerToolbar.menu.findItem(R.id.action_volume)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }
            }

            "drive_mode" -> {
                if (binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "add_to_playlist") {
                    binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_details).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "details") {
                    binding.playerToolbar.menu.findItem(R.id.action_details)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_equalizer).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "equalizer") {
                    binding.playerToolbar.menu.findItem(R.id.action_equalizer)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode)
                    .setShowAsAction(SHOW_AS_ACTION_ALWAYS)

                if (binding.playerToolbar.menu.findItem(R.id.action_playback_speed).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "playback_settings") {
                    binding.playerToolbar.menu.findItem(R.id.action_playback_speed)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "save_playing_queue") {
                    binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_share).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "share") {
                    binding.playerToolbar.menu.findItem(R.id.action_share)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_volume).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "volume") {
                    binding.playerToolbar.menu.findItem(R.id.action_volume)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }
            }

            "equalizer" -> {
                if (binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "add_to_playlist") {
                    binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_details).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "details") {
                    binding.playerToolbar.menu.findItem(R.id.action_details)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                binding.playerToolbar.menu.findItem(R.id.action_equalizer)
                    .setShowAsAction(SHOW_AS_ACTION_ALWAYS)

                if (binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "drive_mode") {
                    binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_playback_speed).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "playback_settings") {
                    binding.playerToolbar.menu.findItem(R.id.action_playback_speed)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "save_playing_queue") {
                    binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_share).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "share") {
                    binding.playerToolbar.menu.findItem(R.id.action_share)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_volume).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "volume") {
                    binding.playerToolbar.menu.findItem(R.id.action_volume)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }
            }

            "playback_settings" -> {
                if (binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "add_to_playlist") {
                    binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_details).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "details") {
                    binding.playerToolbar.menu.findItem(R.id.action_details)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_equalizer).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "equalizer") {
                    binding.playerToolbar.menu.findItem(R.id.action_equalizer)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "drive_mode") {
                    binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                binding.playerToolbar.menu.findItem(R.id.action_playback_speed)
                    .setShowAsAction(SHOW_AS_ACTION_ALWAYS)

                if (binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "save_playing_queue") {
                    binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_share).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "share") {
                    binding.playerToolbar.menu.findItem(R.id.action_share)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_volume).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "volume") {
                    binding.playerToolbar.menu.findItem(R.id.action_volume)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }
            }

            "save_playing_queue" -> {
                if (binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "add_to_playlist") {
                    binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_details).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "details") {
                    binding.playerToolbar.menu.findItem(R.id.action_details)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_equalizer).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "equalizer") {
                    binding.playerToolbar.menu.findItem(R.id.action_equalizer)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "drive_mode") {
                    binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_playback_speed).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "playback_settings") {
                    binding.playerToolbar.menu.findItem(R.id.action_playback_speed)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue)
                    .setShowAsAction(SHOW_AS_ACTION_ALWAYS)

                if (binding.playerToolbar.menu.findItem(R.id.action_share).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "share") {
                    binding.playerToolbar.menu.findItem(R.id.action_share)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_volume).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "volume") {
                    binding.playerToolbar.menu.findItem(R.id.action_volume)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }
            }

            "share" -> {
                if (binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "add_to_playlist") {
                    binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_details).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "details") {
                    binding.playerToolbar.menu.findItem(R.id.action_details)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_equalizer).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "equalizer") {
                    binding.playerToolbar.menu.findItem(R.id.action_equalizer)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "drive_mode") {
                    binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_playback_speed).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "playback_settings") {
                    binding.playerToolbar.menu.findItem(R.id.action_playback_speed)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "save_playing_queue") {
                    binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                binding.playerToolbar.menu.findItem(R.id.action_share)
                    .setShowAsAction(SHOW_AS_ACTION_ALWAYS)

                if (binding.playerToolbar.menu.findItem(R.id.action_volume).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "volume") {
                    binding.playerToolbar.menu.findItem(R.id.action_volume)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }
            }

            "volume" -> {
                if (binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "add_to_playlist") {
                    binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_details).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "details") {
                    binding.playerToolbar.menu.findItem(R.id.action_details)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_equalizer).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "equalizer") {
                    binding.playerToolbar.menu.findItem(R.id.action_equalizer)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "drive_mode") {
                    binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_playback_speed).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "playback_settings") {
                    binding.playerToolbar.menu.findItem(R.id.action_playback_speed)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "save_playing_queue") {
                    binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                if (binding.playerToolbar.menu.findItem(R.id.action_share).showAsActionFlag != SHOW_AS_ACTION_ALWAYS && PreferenceUtil.customToolbarAction != "share") {
                    binding.playerToolbar.menu.findItem(R.id.action_share)
                        .setShowAsAction(SHOW_AS_ACTION_NEVER)
                }

                binding.playerToolbar.menu.findItem(R.id.action_volume)
                    .setShowAsAction(SHOW_AS_ACTION_ALWAYS)
            }
        }
    }

    override fun toggleFavorite(song: Song) {
        super.toggleFavorite(song)
        if (song.id == MusicPlayerRemote.currentSong.id) {
            updateIsFavorite()
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        updateSong()
    }

    override fun onPlayingMetaChanged() {
        super.onPlayingMetaChanged()
        updateSong()
    }

    override fun onUpdateProgressViews(progress: Int, total: Int) {
        binding.progressBar.max = total

        if (isDragEnabled) {
            binding.progressBar.progress = progress
        } else {
            animator = ObjectAnimator.ofInt(binding.progressBar, "progress", progress)

            val animatorSet = AnimatorSet()
            animatorSet.playSequentially(animator)

            animatorSet.duration = 1500
            animatorSet.interpolator = LinearInterpolator()
            animatorSet.start()
        }
        binding.playerSongTotalTime.text = String.format(
            "%s/%s", MusicUtil.getReadableDurationString(total.toLong()),
            MusicUtil.getReadableDurationString(progress.toLong())
        )
    }

    inner class ProgressHelper(context: Context) : View.OnTouchListener {
        private var initialY: Int = 0
        private var initialProgress = 0
        private var progress: Int = 0
        private val displayHeight = resources.displayMetrics.heightPixels
        private var gestureDetector: GestureDetector

        init {
            gestureDetector = GestureDetector(context, object :
                GestureDetector.SimpleOnGestureListener() {

                override fun onLongPress(e: MotionEvent) {
                    if (abs(e.y - initialY) <= 2) {
                        vibrate()
                        isDragEnabled = true
                        binding.progressBar.parent.requestDisallowInterceptTouchEvent(true)
                        animator.pause()
                    }
                    super.onLongPress(e)
                }

                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    if (abs(velocityX) > abs(velocityY)) {
                        if (velocityX < 0) {
                            MusicPlayerRemote.playNextSongAuto(MusicPlayerRemote.isPlaying)
                            return true
                        } else if (velocityX > 0) {
                            MusicPlayerRemote.playPreviousSongAuto(MusicPlayerRemote.isPlaying)
                            return true
                        }
                    }
                    return false
                }
            })
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    initialProgress = MusicPlayerRemote.songProgressMillis
                    initialY = event.y.toInt()
                    progressViewUpdateHelper.stop()
                }

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    progressViewUpdateHelper.start()
                    if (isDragEnabled) {
                        MusicPlayerRemote.seekTo(progress)
                        isDragEnabled = false
                        return true
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    if (isDragEnabled) {
                        val diffY = (initialY - event.y).toInt()
                        progress =
                            initialProgress + diffY * (binding.progressBar.max / displayHeight) // Multiplier
                        if (progress > 0 && progress < binding.progressBar.max) {
                            onUpdateProgressViews(
                                progress,
                                MusicPlayerRemote.songDurationMillis
                            )
                        }
                    }
                }
            }
            return gestureDetector.onTouchEvent(event)
        }

        private fun vibrate() {
            val v = requireContext().getSystemService<Vibrator>()
            v?.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
