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

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.ContentUris
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.drawable.GradientDrawable
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.provider.MediaStore
import android.view.HapticFeedbackConstants
import android.view.MenuItem
import android.view.MenuItem.SHOW_AS_ACTION_ALWAYS
import android.view.MenuItem.SHOW_AS_ACTION_NEVER
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.navOptions
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager
import com.ttop.app.apex.EXTRA_ALBUM_ID
import com.ttop.app.apex.R
import com.ttop.app.apex.SHOW_LYRICS
import com.ttop.app.apex.adapter.song.PlayingQueueAdapter
import com.ttop.app.apex.databinding.FragmentPeekPlayerBinding
import com.ttop.app.apex.dialogs.AddToPlaylistDialog
import com.ttop.app.apex.dialogs.CreatePlaylistDialog
import com.ttop.app.apex.dialogs.DeleteSongsDialog
import com.ttop.app.apex.dialogs.PlaybackSpeedDialog
import com.ttop.app.apex.dialogs.SleepTimerDialog
import com.ttop.app.apex.dialogs.SongDetailDialog
import com.ttop.app.apex.dialogs.SongShareDialog
import com.ttop.app.apex.dialogs.VolumeDialog
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.extensions.darkAccentColor
import com.ttop.app.apex.extensions.drawAboveSystemBarsWithPadding
import com.ttop.app.apex.extensions.keepScreenOn
import com.ttop.app.apex.extensions.m3accentColor
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.extensions.surfaceColor
import com.ttop.app.apex.extensions.whichFragment
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.libraries.appthemehelper.ThemeStore
import com.ttop.app.apex.libraries.appthemehelper.util.ATHColorUtil
import com.ttop.app.apex.libraries.appthemehelper.util.ATHUtil
import com.ttop.app.apex.libraries.appthemehelper.util.ToolbarContentTintHelper
import com.ttop.app.apex.libraries.fastscroller.FastScrollNestedScrollView
import com.ttop.app.apex.repository.RealRepository
import com.ttop.app.apex.ui.activities.tageditor.AbsTagEditorActivity
import com.ttop.app.apex.ui.activities.tageditor.SongTagEditorActivity
import com.ttop.app.apex.ui.fragments.base.AbsPlayerFragment
import com.ttop.app.apex.ui.fragments.base.goToArtist
import com.ttop.app.apex.ui.fragments.player.PlayerAlbumCoverFragment
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.ColorUtil
import com.ttop.app.apex.util.MusicUtil
import com.ttop.app.apex.util.NavigationUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.RingtoneManager
import com.ttop.app.apex.util.ViewUtil
import com.ttop.app.apex.util.color.MediaNotificationProcessor
import com.ttop.app.apex.views.DrawableGradient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.get
import java.util.regex.Pattern

class PeekPlayerFragment : AbsPlayerFragment(R.layout.fragment_peek_player),
    View.OnLayoutChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var controlsFragment: PeekPlayerControlFragment
    private var valueAnimator: ValueAnimator? = null
    private var lastColor: Int = 0
    private var toolbarColor: Int = 0
    private var _binding: FragmentPeekPlayerBinding? = null
    private val binding get() = _binding!!

    private val embed: TextView get() = binding.embedded
    private val scroll: FastScrollNestedScrollView get() = binding.scroll
    private val scrollCard: MaterialCardView? get() = binding.scrollCard

    private lateinit var wrappedAdapter: RecyclerView.Adapter<*>
    private var recyclerViewDragDropManager: RecyclerViewDragDropManager? = null
    private var recyclerViewTouchActionGuardManager: RecyclerViewTouchActionGuardManager? = null
    private var playingQueueAdapter: PlayingQueueAdapter? = null
    private lateinit var linearLayoutManager: LinearLayoutManager

    private var animationDuration: Int = 0

    override fun onMenuItemClick(item: MenuItem): Boolean {
        val song = MusicPlayerRemote.currentSong
        if (!PreferenceUtil.isHapticFeedbackDisabled) {
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
        when (item.itemId) {
            R.id.action_playback_speed -> {
                PlaybackSpeedDialog.newInstance().show(childFragmentManager, "PLAYBACK_SETTINGS")
                return true
            }

            R.id.action_toggle_favorite -> {
                toggleFavorite(song)
                if (!PreferenceUtil.isHapticFeedbackDisabled) {
                    requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                }
                return true
            }

            R.id.action_share -> {
                SongShareDialog.create(song).show(childFragmentManager, "SHARE_SONG")
                return true
            }

            R.id.action_go_to_drive_mode -> {
                NavigationUtil.gotoDriveMode(requireActivity())
                return true
            }

            R.id.action_delete_from_device -> {
                DeleteSongsDialog.create(song).show(childFragmentManager, "DELETE_SONGS")
                return true
            }

            R.id.action_add_to_playlist -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    val playlists = get<RealRepository>().fetchPlaylists()
                    withContext(Dispatchers.Main) {
                        AddToPlaylistDialog.create(playlists, song)
                            .show(childFragmentManager, "ADD_PLAYLIST")
                    }
                }
                return true
            }

            R.id.action_clear_playing_queue -> {
                MusicPlayerRemote.clearQueue()
                return true
            }

            R.id.action_save_playing_queue -> {
                CreatePlaylistDialog.create(ArrayList(MusicPlayerRemote.playingQueue))
                    .show(childFragmentManager, "ADD_TO_PLAYLIST")
                return true
            }

            R.id.action_tag_editor -> {
                val intent = Intent(activity, SongTagEditorActivity::class.java)
                intent.putExtra(AbsTagEditorActivity.EXTRA_ID, song.id)
                startActivity(intent)
                return true
            }

            R.id.action_details -> {
                SongDetailDialog.create(song).show(childFragmentManager, "SONG_DETAIL")
                return true
            }

            R.id.action_go_to_album -> {
                //Hide Bottom Bar First, else Bottom Sheet doesn't collapse fully
                mainActivity.setBottomNavVisibility(false)
                mainActivity.collapsePanel()
                requireActivity().findNavController(R.id.fragment_container).navigate(
                    R.id.albumDetailsFragment,
                    bundleOf(EXTRA_ALBUM_ID to song.albumId)
                )
                return true
            }

            R.id.action_go_to_artist -> {
                goToArtist(requireActivity())
                return true
            }

            R.id.now_playing -> {
                requireActivity().findNavController(R.id.fragment_container).navigate(
                    R.id.playing_queue_fragment,
                    null,
                    navOptions { launchSingleTop = true }
                )
                mainActivity.collapsePanel()
                return true
            }

            R.id.action_equalizer -> {
                NavigationUtil.openEqualizer(requireActivity())
                return true
            }

            R.id.action_sleep_timer -> {
                SleepTimerDialog().show(parentFragmentManager, "SLEEP_TIMER")
                return true
            }

            R.id.action_set_as_ringtone -> {
                requireContext().run {
                    if (RingtoneManager.requiresDialog(this)) {
                        RingtoneManager.showDialog(this)
                    } else {
                        RingtoneManager.setRingtone(this, song)
                    }
                }

                return true
            }

            R.id.action_go_to_genre -> {
                val retriever = MediaMetadataRetriever()
                val trackUri =
                    ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        song.id
                    )
                retriever.setDataSource(activity, trackUri)
                var genre: String? =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
                if (genre == null) {
                    genre = "Not Specified"
                }
                showToast(genre)
                return true
            }

            R.id.action_go_to_lyrics -> {
                if (ApexUtil.isTablet) {
                    if (binding.playerQueueSheet?.visibility == View.VISIBLE) {
                        PreferenceUtil.showId3Lyrics = true
                        binding.playerToolbar.menu.findItem(R.id.action_go_to_lyrics)
                            .setIcon(R.drawable.ic_lyrics_off_outline)
                        binding.playerQueueSheet?.let {
                            ApexUtil.fadeAnimator(
                                scrollCard!!, it, animationDuration,
                                tobeShownVisibleCode = true,
                                showListener = true
                            )
                        }

                        mainActivity.keepScreenOn(true)
                    } else {
                        PreferenceUtil.showId3Lyrics = false
                        binding.playerToolbar.menu.findItem(R.id.action_go_to_lyrics)
                            .setIcon(R.drawable.ic_lyrics_on_outline)
                        binding.playerQueueSheet?.let {
                            ApexUtil.fadeAnimator(
                                it, scrollCard!!, animationDuration,
                                tobeShownVisibleCode = true,
                                showListener = true
                            )
                        }

                        if (PreferenceUtil.showLyrics) {
                            mainActivity.keepScreenOn(true)
                        } else {
                            mainActivity.keepScreenOn(false)
                        }
                    }
                } else {
                    if (scrollCard?.visibility == View.GONE) {
                        binding.playerToolbar.menu.findItem(R.id.action_go_to_lyrics)
                            .setIcon(R.drawable.ic_lyrics_off_outline)
                        ApexUtil.fadeAnimator(
                            scrollCard!!, binding.playerAlbumCoverFragment, animationDuration,
                            tobeShownVisibleCode = true,
                            showListener = false
                        )

                        mainActivity.keepScreenOn(true)
                    } else {
                        binding.playerToolbar.menu.findItem(R.id.action_go_to_lyrics)
                            .setIcon(R.drawable.ic_lyrics_on_outline)
                        ApexUtil.fadeAnimator(
                            binding.playerAlbumCoverFragment, scrollCard!!, animationDuration,
                            tobeShownVisibleCode = false,
                            showListener = true
                        )

                        if (PreferenceUtil.showLyrics) {
                            mainActivity.keepScreenOn(true)
                        } else {
                            mainActivity.keepScreenOn(false)
                        }
                    }
                }

                ToolbarContentTintHelper.colorizeToolbar(
                    binding.playerToolbar,
                    toolbarIconColor(),
                    requireActivity()
                )
            }

            R.id.action_volume -> {
                VolumeDialog.newInstance().show(childFragmentManager, "VOLUME")
                return true
            }
        }
        return false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPeekPlayerBinding.bind(view)
        setUpPlayerToolbar()
        setUpSubFragments()
        setupRecyclerView()

        binding.root.drawAboveSystemBarsWithPadding()

        PreferenceUtil.registerOnSharedPreferenceChangedListener(this)

        embed.textSize = 24f

        animationDuration = resources.getInteger(android.R.integer.config_longAnimTime)

        if (PreferenceUtil.showId3Lyrics) {
            if (ApexUtil.isTablet) {
                binding.playerToolbar.menu.findItem(R.id.action_go_to_lyrics)
                    .setIcon(R.drawable.ic_lyrics_off_outline)
                binding.playerQueueSheet?.visibility = View.GONE
                scrollCard?.visibility = View.VISIBLE

                mainActivity.keepScreenOn(true)

                ToolbarContentTintHelper.colorizeToolbar(
                    binding.playerToolbar,
                    toolbarIconColor(),
                    requireActivity()
                )
            }
        }
    }

    private fun setUpSubFragments() {
        controlsFragment =
            whichFragment(R.id.playbackControlsFragment) as PeekPlayerControlFragment

        val coverFragment =
            whichFragment(R.id.playerAlbumCoverFragment) as PlayerAlbumCoverFragment
        coverFragment.setCallbacks(this)
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
            setOnMenuItemClickListener(this@PeekPlayerFragment)
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

    override fun playerToolbar(): Toolbar {
        return binding.playerToolbar
    }

    private fun setupRecyclerView() {
        playingQueueAdapter = if (PreferenceUtil.isPerformanceMode) {
            PlayingQueueAdapter(
                requireActivity() as AppCompatActivity,
                MusicPlayerRemote.playingQueue.toMutableList(),
                MusicPlayerRemote.position,
                R.layout.item_queue_no_image
            )
        }else {
            if (ApexUtil.isLandscape) {
                PlayingQueueAdapter(
                    requireActivity() as AppCompatActivity,
                    MusicPlayerRemote.playingQueue.toMutableList(),
                    MusicPlayerRemote.position,
                    R.layout.item_queue_duo
                )
            } else {
                PlayingQueueAdapter(
                    requireActivity() as AppCompatActivity,
                    MusicPlayerRemote.playingQueue.toMutableList(),
                    MusicPlayerRemote.position,
                    R.layout.item_queue
                )
            }
        }

        linearLayoutManager = LinearLayoutManager(requireContext())
        recyclerViewTouchActionGuardManager = RecyclerViewTouchActionGuardManager()
        recyclerViewDragDropManager = RecyclerViewDragDropManager()

        val animator = DraggableItemAnimator()
        animator.supportsChangeAnimations = false
        wrappedAdapter =
            recyclerViewDragDropManager?.createWrappedAdapter(playingQueueAdapter!!) as RecyclerView.Adapter<*>
        binding.recyclerView?.layoutManager = linearLayoutManager
        binding.recyclerView?.adapter = wrappedAdapter
        binding.recyclerView?.itemAnimator = animator
        binding.recyclerView?.let { recyclerViewTouchActionGuardManager?.attachRecyclerView(it) }
        binding.recyclerView?.let { recyclerViewDragDropManager?.attachRecyclerView(it) }

        linearLayoutManager.scrollToPositionWithOffset(MusicPlayerRemote.position, 0)
    }

    private fun updateQueuePosition() {
        playingQueueAdapter?.setCurrent(MusicPlayerRemote.position)
        resetToCurrentPosition()
    }

    private fun updateQueue() {
        playingQueueAdapter?.swapDataSet(MusicPlayerRemote.playingQueue, MusicPlayerRemote.position)
        resetToCurrentPosition()
    }

    private fun resetToCurrentPosition() {
        binding.recyclerView?.stopScroll()
        linearLayoutManager.scrollToPositionWithOffset(MusicPlayerRemote.position, 0)
    }

    private fun getQueuePanel(): BottomSheetBehavior<MaterialCardView> {
        return BottomSheetBehavior.from(binding.playerQueueSheet!!)
    }

    override fun onQueueChanged() {
        super.onQueueChanged()
        updateQueue()

        val data: String? = MusicUtil.getLyrics(MusicPlayerRemote.currentSong)
        val string = StringBuilder()
        string.append(data).append("\n")
        embed.text =
            (if (data.isNullOrEmpty()) R.string.no_lyrics_found.toString() else string.toString())
    }

    override fun onShow() {
    }

    override fun onHide() {
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun toolbarIconColor(): Int {
        return if (PreferenceUtil.isAdaptiveColor) {
            toolbarColor
        } else {
            if (PreferenceUtil.materialYou) {
                ContextCompat.getColor(requireContext(), R.color.m3_widget_other_text)
            } else {
                accentColor()
            }

        }
    }

    override val paletteColor: Int
        get() = lastColor

    override fun onColorChanged(color: MediaNotificationProcessor) {
        lastColor = color.backgroundColor
        toolbarColor = color.secondaryTextColor
        libraryViewModel.updateColor(color.backgroundColor)
        controlsFragment.setColor(color)

        ToolbarContentTintHelper.colorizeToolbar(
            binding.playerToolbar,
            toolbarIconColor(),
            requireActivity()
        )

        if (PreferenceUtil.isAdaptiveColor) {
            colorize(color)

            if (PreferenceUtil.isColorAnimate) {
                val animator =
                    binding.colorGradientBackground.let { controlsFragment.createRevealAnimator(it) }
                animator.doOnEnd {
                    _binding?.root?.setBackgroundColor(color.backgroundColor)
                }
                animator.start()
            }
        } else {
            colorizeAccent()
        }

        val colorBg = ATHUtil.resolveColor(requireContext(), android.R.attr.colorBackground)

        if (PreferenceUtil.isAdaptiveColor) {
            if (PreferenceUtil.isPlayerBackgroundType) {
                embed.setTextColor(ColorUtil.getComplimentColor(color.secondaryTextColor))
            } else {
                embed.setTextColor(color.secondaryTextColor)
            }

            binding.playerQueueSheet?.strokeColor = ColorUtil.getComplimentColor(color.secondaryTextColor)
            scrollCard?.strokeColor = ColorUtil.getComplimentColor(color.secondaryTextColor)
        } else {
            binding.playerQueueSheet?.strokeColor = accentColor()
            scrollCard?.strokeColor = accentColor()
            if (PreferenceUtil.materialYou) {
                embed.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.m3_widget_other_text
                    )
                )
            } else {
                if (ApexUtil.isTablet) {
                    when (PreferenceUtil.baseTheme) {
                        "light" -> {
                            embed.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.md_black_1000
                                )
                            )
                        }

                        "dark" -> {
                            if (PreferenceUtil.isBlackMode) {
                                embed.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.md_white_1000
                                    )
                                )
                            } else {
                                embed.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.md_white_1000
                                    )
                                )
                            }
                        }

                        "auto" -> {
                            when (requireContext().resources?.configuration?.uiMode?.and(
                                Configuration.UI_MODE_NIGHT_MASK
                            )) {
                                Configuration.UI_MODE_NIGHT_YES -> {
                                    if (PreferenceUtil.isBlackMode) {
                                        embed.setTextColor(
                                            ContextCompat.getColor(
                                                requireContext(),
                                                R.color.md_white_1000
                                            )
                                        )
                                    } else {
                                        embed.setTextColor(
                                            ContextCompat.getColor(
                                                requireContext(),
                                                R.color.md_white_1000
                                            )
                                        )
                                    }
                                }

                                Configuration.UI_MODE_NIGHT_NO,
                                Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                                    embed.setTextColor(
                                        ContextCompat.getColor(
                                            requireContext(),
                                            R.color.md_black_1000
                                        )
                                    )
                                }
                            }
                        }
                    }
                } else {
                    if (ATHColorUtil.isColorLight(colorBg)) {
                        embed.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.md_black_1000
                            )
                        )
                    } else {
                        embed.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.md_white_1000
                            )
                        )
                    }
                }
            }
        }

        playingQueueAdapter?.setTextColor(color.secondaryTextColor)
        playingQueueAdapter?.setBackgroundColor(color.backgroundColor)
    }

    private fun colorize(i: MediaNotificationProcessor) {
        if (PreferenceUtil.isPlayerBackgroundType) {
            //GRADIENT
            if (valueAnimator != null) {
                valueAnimator?.cancel()
            }

            valueAnimator = ValueAnimator.ofObject(
                ArgbEvaluator(),
                i.backgroundColor,
                i.secondaryTextColor
            )
            valueAnimator?.addUpdateListener { animation ->
                if (isAdded) {
                    val drawable = DrawableGradient(
                        GradientDrawable.Orientation.TR_BL,
                        intArrayOf(
                            i.secondaryTextColor,
                            i.backgroundColor,
                            i.backgroundColor,
                            i.backgroundColor,
                            surfaceColor()
                        ), 0
                    )
                    binding.colorGradientBackground.background = drawable
                }
            }
            valueAnimator?.setDuration(ViewUtil.APEX_MUSIC_ANIM_TIME.toLong())?.start()
        } else {
            //SINGLE COLOR
            binding.colorGradientBackground.setBackgroundColor(i.backgroundColor)
        }
    }

    private fun colorizeAccent() {
        if (PreferenceUtil.isPlayerBackgroundType) {
            //GRADIENT
            if (valueAnimator != null) {
                valueAnimator?.cancel()
            }

            if (PreferenceUtil.materialYou) {
                valueAnimator = ValueAnimator.ofObject(
                    ArgbEvaluator(),
                    accentColor(),
                    m3accentColor(),
                    accentColor()
                )

                valueAnimator?.addUpdateListener {
                    if (isAdded) {
                        val drawable = DrawableGradient(
                            GradientDrawable.Orientation.TR_BL,
                            intArrayOf(
                                accentColor(),
                                m3accentColor(),
                                m3accentColor(),
                                surfaceColor()
                            ), 0
                        )
                        binding.colorGradientBackground.background = drawable
                    }
                }
            }else {
                valueAnimator = ValueAnimator.ofObject(
                    ArgbEvaluator(),
                    surfaceColor(),
                    accentColor()
                )

                valueAnimator?.addUpdateListener {
                    if (isAdded) {
                        val drawable = DrawableGradient(
                            GradientDrawable.Orientation.TR_BL,
                            intArrayOf(
                                accentColor(),
                                surfaceColor()
                            ), 0
                        )
                        binding.colorGradientBackground.background = drawable
                    }
                }
            }

            valueAnimator?.setDuration(ViewUtil.APEX_MUSIC_ANIM_TIME.toLong())?.start()
        } else {
            //SINGLE COLOR
            if (PreferenceUtil.materialYou) {
                binding.colorGradientBackground.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.m3_widget_background
                    )
                )
            } else {
                binding.colorGradientBackground.setBackgroundColor(surfaceColor())
            }
        }
    }

    override fun onFavoriteToggled() {
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        controlsFragment.updateSong()
        updateQueue()

        val data: String? = MusicUtil.getLyrics(MusicPlayerRemote.currentSong)
        val string = StringBuilder()
        string.append(data).append("\n")
        embed.text =
            (if (data.isNullOrEmpty()) R.string.no_lyrics_found.toString() else string.toString())
    }

    override fun onPlayingMetaChanged() {
        super.onPlayingMetaChanged()
        controlsFragment.updateSong()
        updateQueuePosition()

        val data: String? = MusicUtil.getLyrics(MusicPlayerRemote.currentSong)
        val string = StringBuilder()
        string.append(data).append("\n")

        embed.text =
            (if (data.isNullOrEmpty()) R.string.no_lyrics_found.toString() else string.toString())

        scroll.scrollTo(0, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        PreferenceUtil.unregisterOnSharedPreferenceChangedListener(this)
    }

    override fun onLayoutChange(
        v: View?,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        oldLeft: Int,
        oldTop: Int,
        oldRight: Int,
        oldBottom: Int
    ) {
        val height = binding.playerContainer.height
        val width = binding.playerContainer.width
        val finalHeight = height - (binding.playbackControlsFragment.rootView.height + width)
        val panel = getQueuePanel()
        panel.peekHeight = finalHeight
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            SHOW_LYRICS -> {
                if (ApexUtil.isTablet) {
                    if (PreferenceUtil.showLyrics && PreferenceUtil.showId3Lyrics) {
                        PreferenceUtil.showId3Lyrics = false

                        binding.playerToolbar.menu.findItem(R.id.action_go_to_lyrics)
                            .setIcon(R.drawable.ic_lyrics_on_outline)
                        binding.playerQueueSheet?.let {
                            ApexUtil.fadeAnimator(
                                it, scrollCard!!, animationDuration,
                                tobeShownVisibleCode = true,
                                showListener = true
                            )
                        }

                        ToolbarContentTintHelper.colorizeToolbar(
                            binding.playerToolbar,
                            toolbarIconColor(),
                            requireActivity()
                        )

                        mainActivity.keepScreenOn(true)
                    }
                }
            }
        }
    }
}