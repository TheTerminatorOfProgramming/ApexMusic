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
package com.ttop.app.apex.ui.fragments.player.blur

import android.content.ContentUris
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.provider.MediaStore
import android.view.HapticFeedbackConstants
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager
import com.ttop.app.apex.EXTRA_ALBUM_ID
import com.ttop.app.apex.LYRICS_MODE
import com.ttop.app.apex.NEW_BLUR_AMOUNT
import com.ttop.app.apex.R
import com.ttop.app.apex.SHOW_LYRICS_TABLET
import com.ttop.app.apex.adapter.song.PlayingQueueAdapter
import com.ttop.app.apex.databinding.FragmentBlurPlayerBinding
import com.ttop.app.apex.dialogs.AddToPlaylistDialog
import com.ttop.app.apex.dialogs.CreatePlaylistDialog
import com.ttop.app.apex.dialogs.DeleteSongsDialog
import com.ttop.app.apex.dialogs.PlaybackSpeedDialog
import com.ttop.app.apex.dialogs.SleepTimerDialog
import com.ttop.app.apex.dialogs.SongDetailDialog
import com.ttop.app.apex.dialogs.SongShareDialog
import com.ttop.app.apex.dialogs.VolumeDialog
import com.ttop.app.apex.extensions.drawAboveSystemBars
import com.ttop.app.apex.extensions.keepScreenOn
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.extensions.whichFragment
import com.ttop.app.apex.glide.ApexGlideExtension
import com.ttop.app.apex.glide.ApexGlideExtension.simpleSongCoverOptions
import com.ttop.app.apex.glide.BlurTransformation
import com.ttop.app.apex.glide.crossfadeListener
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.libraries.appthemehelper.util.ToolbarContentTintHelper
import com.ttop.app.apex.libraries.fastscroller.FastScrollNestedScrollView
import com.ttop.app.apex.model.Song
import com.ttop.app.apex.repository.RealRepository
import com.ttop.app.apex.ui.activities.tageditor.AbsTagEditorActivity
import com.ttop.app.apex.ui.activities.tageditor.SongTagEditorActivity
import com.ttop.app.apex.ui.fragments.base.AbsPlayerFragment
import com.ttop.app.apex.ui.fragments.base.goToArtist
import com.ttop.app.apex.ui.fragments.player.LRCFragment
import com.ttop.app.apex.ui.fragments.player.PlayerAlbumCoverFragment
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.MusicUtil
import com.ttop.app.apex.util.NavigationUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.PreferenceUtil.blurAmount
import com.ttop.app.apex.util.RingtoneManager
import com.ttop.app.apex.util.color.MediaNotificationProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.get


class BlurPlayerFragment : AbsPlayerFragment(R.layout.fragment_blur_player),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private var lastRequest: RequestBuilder<Drawable>? = null

    override fun playerToolbar(): Toolbar {
        return binding.playerToolbar
    }

    private lateinit var playbackControlsFragment: BlurPlaybackControlsFragment
    private var lrcFragment: LRCFragment? = null
    private lateinit var wrappedAdapter: RecyclerView.Adapter<*>
    private var recyclerViewDragDropManager: RecyclerViewDragDropManager? = null
    private var recyclerViewTouchActionGuardManager: RecyclerViewTouchActionGuardManager? = null
    private var playingQueueAdapter: PlayingQueueAdapter? = null
    private lateinit var linearLayoutManager: LinearLayoutManager

    private var lastColor: Int = 0

    private var _binding: FragmentBlurPlayerBinding? = null
    private val binding get() = _binding!!

    private val embed: TextView get() = binding.embedded
    private val scroll: FastScrollNestedScrollView get() = binding.scroll

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBlurPlayerBinding.bind(view)
        setUpSubFragments()
        setUpPlayerToolbar()
        setupRecyclerView()
        binding.playerToolbar.drawAboveSystemBars()

        embed.textSize = 24f
        embed.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))

        playerToolbar().menu?.findItem(R.id.action_go_to_lyrics)?.isVisible =
            !(PreferenceUtil.lyricsMode == "disabled" || PreferenceUtil.lyricsMode == "synced")
    }

    private fun setUpSubFragments() {
        playbackControlsFragment = whichFragment(R.id.playbackControlsFragment)
        val playerAlbumCoverFragment: PlayerAlbumCoverFragment =
            whichFragment(R.id.playerAlbumCoverFragment)
        playerAlbumCoverFragment.setCallbacks(this)

        if (ApexUtil.isTablet) {
            lrcFragment =
                childFragmentManager.findFragmentById(R.id.lrcFragment) as LRCFragment
            lrcFragment!!.setCallbacks(this)
        }
    }

    private fun setUpPlayerToolbar() {
        binding.playerToolbar.apply {
            inflateMenu(R.menu.menu_player)
            binding.playerToolbar.setNavigationOnClickListener {
                if (!PreferenceUtil.isHapticFeedbackDisabled) {
                    requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                }

                mainActivity.collapsePanel()
            }
            ToolbarContentTintHelper.colorizeToolbar(this, Color.WHITE, activity)
        }.setOnMenuItemClickListener(this)

        when (PreferenceUtil.customToolbarAction) {
            "disabled" -> {
                binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_details)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_equalizer)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                binding.playerToolbar.menu.findItem(R.id.action_playback_speed)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_share)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_volume)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            }

            "add_to_playlist" -> {
                binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                binding.playerToolbar.menu.findItem(R.id.action_details)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_equalizer)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                binding.playerToolbar.menu.findItem(R.id.action_playback_speed)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_share)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_volume)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            }

            "details" -> {
                binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_details)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                binding.playerToolbar.menu.findItem(R.id.action_equalizer)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                binding.playerToolbar.menu.findItem(R.id.action_playback_speed)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_share)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_volume)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            }

            "drive_mode" -> {
                binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_details)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_equalizer)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                binding.playerToolbar.menu.findItem(R.id.action_playback_speed)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_share)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_volume)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            }

            "equalizer" -> {
                binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_details)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_equalizer)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                binding.playerToolbar.menu.findItem(R.id.action_playback_speed)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_share)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_volume)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            }

            "playback_settings" -> {
                binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_details)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_equalizer)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                binding.playerToolbar.menu.findItem(R.id.action_playback_speed)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_share)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_volume)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            }

            "save_playing_queue" -> {
                binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_details)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_equalizer)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                binding.playerToolbar.menu.findItem(R.id.action_playback_speed)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                binding.playerToolbar.menu.findItem(R.id.action_share)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_volume)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            }

            "share" -> {
                binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_details)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_equalizer)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                binding.playerToolbar.menu.findItem(R.id.action_playback_speed)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_share)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                binding.playerToolbar.menu.findItem(R.id.action_volume)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            }

            "volume" -> {
                binding.playerToolbar.menu.findItem(R.id.action_add_to_playlist)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_details)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_equalizer)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_go_to_drive_mode)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                binding.playerToolbar.menu.findItem(R.id.action_playback_speed)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_save_playing_queue)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_share)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                binding.playerToolbar.menu.findItem(R.id.action_volume)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            }
        }
    }

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

            R.id.action_queue -> {
                if (!ApexUtil.isTablet) {
                    scroll.visibility = View.GONE
                    if (binding.playerQueueSheet.visibility == View.VISIBLE) {
                        binding.playerQueueSheet.visibility = View.GONE
                        binding.playerAlbumCoverFragment.alpha = 1f
                    } else {
                        binding.playerQueueSheet.visibility = View.VISIBLE
                        binding.playerAlbumCoverFragment.alpha = 0f
                    }
                }
            }

            R.id.action_go_to_lyrics -> {
                if (ApexUtil.isTablet) {
                    when (PreferenceUtil.lyricsMode) {
                        "disabled", "synced" -> {
                        }

                        "id3" -> {
                            if (binding.playerQueueSheet.visibility == View.VISIBLE) {
                                binding.playerQueueSheet.visibility = View.GONE
                                scroll.visibility = View.VISIBLE

                                if (PreferenceUtil.lyricsScreenOn) {
                                    mainActivity.keepScreenOn(true)
                                } else {
                                    mainActivity.keepScreenOn(false)
                                }
                            } else {
                                binding.playerQueueSheet.visibility = View.VISIBLE
                                scroll.visibility = View.GONE

                                mainActivity.keepScreenOn(false)
                            }
                        }

                        "both" -> {
                            if (PreferenceUtil.showLyricsTablet) {
                                if (binding.lrcFragment?.visibility == View.VISIBLE) {
                                    binding.lrcFragment?.visibility = View.GONE
                                    scroll.visibility = View.VISIBLE
                                } else {
                                    binding.lrcFragment?.visibility = View.VISIBLE
                                    scroll.visibility = View.GONE
                                }
                            } else {
                                if (binding.playerQueueSheet.visibility == View.VISIBLE) {
                                    binding.playerQueueSheet.visibility = View.GONE
                                    scroll.visibility = View.VISIBLE

                                    if (PreferenceUtil.lyricsScreenOn) {
                                        mainActivity.keepScreenOn(true)
                                    } else {
                                        mainActivity.keepScreenOn(false)
                                    }
                                } else {
                                    binding.playerQueueSheet.visibility = View.VISIBLE
                                    scroll.visibility = View.GONE

                                    mainActivity.keepScreenOn(false)
                                }
                            }
                        }
                    }
                } else {
                    binding.playerQueueSheet.visibility = View.GONE
                    if (scroll.visibility == View.GONE) {
                        scroll.visibility = View.VISIBLE

                        if (PreferenceUtil.lyricsScreenOn) {
                            mainActivity.keepScreenOn(true)
                        } else {
                            mainActivity.keepScreenOn(false)
                        }

                        binding.playerAlbumCoverFragment.alpha = 0f
                    } else {
                        scroll.visibility = View.GONE

                        binding.playerAlbumCoverFragment.alpha = 1f
                        mainActivity.keepScreenOn(false)
                    }
                }
            }

            R.id.action_volume -> {
                VolumeDialog.newInstance().show(childFragmentManager, "VOLUME")
                return true
            }
        }
        return false
    }

    override fun onFavoriteToggled() {
        toggleFavorite(MusicPlayerRemote.currentSong)
    }

    override fun onColorChanged(color: MediaNotificationProcessor) {
        playbackControlsFragment.setColor(color)
        lastColor = color.backgroundColor
        libraryViewModel.updateColor(color.backgroundColor)
        if (ApexUtil.isTablet) {
            lrcFragment?.notifyColorChange(color)
        }
        ToolbarContentTintHelper.colorizeToolbar(binding.playerToolbar, Color.WHITE, activity)
        playingQueueAdapter?.setTextColor(color.secondaryTextColor)
    }

    override fun toggleFavorite(song: Song) {
        super.toggleFavorite(song)
        if (song.id == MusicPlayerRemote.currentSong.id) {
            updateIsFavorite()
        }
    }

    override fun onShow() {
    }

    override fun onHide() {
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun toolbarIconColor(): Int {
        return Color.WHITE
    }

    override val paletteColor: Int
        get() = lastColor

    private fun updateBlur() {
        // https://github.com/bumptech/glide/issues/527#issuecomment-148840717
        Glide.with(this)
            .load(ApexGlideExtension.getSongModel(MusicPlayerRemote.currentSong))
            .simpleSongCoverOptions(MusicPlayerRemote.currentSong)
            .transform(
                BlurTransformation.Builder(requireContext()).blurRadius(blurAmount.toFloat())
                    .build()
            ).thumbnail(lastRequest)
            .error(Glide.with(this).load(ColorDrawable(Color.DKGRAY)).fitCenter())
            .also {
                lastRequest = it.clone()
                it.crossfadeListener()
                    .into(binding.colorBackground)
            }
    }

    private fun setupRecyclerView() {
        playingQueueAdapter = if (ApexUtil.isTablet) {
            if (ApexUtil.isLandscape) {
                when (PreferenceUtil.queueStyleLand) {
                    "normal" -> {
                        PlayingQueueAdapter(
                            requireActivity() as AppCompatActivity,
                            MusicPlayerRemote.playingQueue.toMutableList(),
                            MusicPlayerRemote.position,
                            R.layout.item_queue_player_plain
                        )
                    }

                    "duo" -> {
                        PlayingQueueAdapter(
                            requireActivity() as AppCompatActivity,
                            MusicPlayerRemote.playingQueue.toMutableList(),
                            MusicPlayerRemote.position,
                            R.layout.item_queue_duo
                        )
                    }

                    "trio" -> {
                        PlayingQueueAdapter(
                            requireActivity() as AppCompatActivity,
                            MusicPlayerRemote.playingQueue.toMutableList(),
                            MusicPlayerRemote.position,
                            R.layout.item_queue_trio
                        )
                    }

                    else -> {
                        PlayingQueueAdapter(
                            requireActivity() as AppCompatActivity,
                            MusicPlayerRemote.playingQueue.toMutableList(),
                            MusicPlayerRemote.position,
                            R.layout.item_queue_player_plain
                        )
                    }
                }
            } else {
                when (PreferenceUtil.queueStyle) {
                    "normal" -> {
                        PlayingQueueAdapter(
                            requireActivity() as AppCompatActivity,
                            MusicPlayerRemote.playingQueue.toMutableList(),
                            MusicPlayerRemote.position,
                            R.layout.item_queue_player_plain
                        )
                    }

                    "duo" -> {
                        PlayingQueueAdapter(
                            requireActivity() as AppCompatActivity,
                            MusicPlayerRemote.playingQueue.toMutableList(),
                            MusicPlayerRemote.position,
                            R.layout.item_queue_duo
                        )
                    }

                    "trio" -> {
                        PlayingQueueAdapter(
                            requireActivity() as AppCompatActivity,
                            MusicPlayerRemote.playingQueue.toMutableList(),
                            MusicPlayerRemote.position,
                            R.layout.item_queue_trio
                        )
                    }

                    else -> {
                        PlayingQueueAdapter(
                            requireActivity() as AppCompatActivity,
                            MusicPlayerRemote.playingQueue.toMutableList(),
                            MusicPlayerRemote.position,
                            R.layout.item_queue_player_plain
                        )
                    }
                }
            }
        } else {
            PlayingQueueAdapter(
                requireActivity() as AppCompatActivity,
                MusicPlayerRemote.playingQueue.toMutableList(),
                MusicPlayerRemote.position,
                R.layout.item_queue_player
            )
        }
        linearLayoutManager = LinearLayoutManager(requireContext())
        recyclerViewTouchActionGuardManager = RecyclerViewTouchActionGuardManager()
        recyclerViewDragDropManager = RecyclerViewDragDropManager()

        val animator = DraggableItemAnimator()
        animator.supportsChangeAnimations = false
        wrappedAdapter =
            recyclerViewDragDropManager?.createWrappedAdapter(playingQueueAdapter!!) as RecyclerView.Adapter<*>
        binding.recyclerView.layoutManager = linearLayoutManager
        binding.recyclerView.adapter = wrappedAdapter
        binding.recyclerView.itemAnimator = animator
        binding.recyclerView.let { recyclerViewTouchActionGuardManager?.attachRecyclerView(it) }
        binding.recyclerView.let { recyclerViewDragDropManager?.attachRecyclerView(it) }

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
        binding.recyclerView.stopScroll()
        linearLayoutManager.scrollToPositionWithOffset(MusicPlayerRemote.position, 0)
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

    override fun onServiceConnected() {
        updateIsFavorite()
        updateBlur()
        updateQueue()

        val data: String? = MusicUtil.getLyrics(MusicPlayerRemote.currentSong)
        val string = StringBuilder()
        string.append(data).append("\n")
        embed.text =
            (if (data.isNullOrEmpty()) R.string.no_lyrics_found.toString() else string.toString())
    }

    override fun onPlayingMetaChanged() {
        updateIsFavorite()
        updateBlur()
        updateQueuePosition()

        val data: String? = MusicUtil.getLyrics(MusicPlayerRemote.currentSong)
        val string = StringBuilder()
        string.append(data).append("\n")
        embed.text =
            (if (data.isNullOrEmpty()) R.string.no_lyrics_found.toString() else string.toString())

        scroll.scrollTo(0, 0)
    }

    override fun onPause() {
        super.onPause()
        lastRequest = null
    }

    override fun onResume() {
        super.onResume()
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .unregisterOnSharedPreferenceChangeListener(this)
        _binding = null
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            NEW_BLUR_AMOUNT -> {
                updateBlur()
            }

            SHOW_LYRICS_TABLET -> {
                if (ApexUtil.isTablet) {
                    //modified
                    if (PreferenceUtil.lyricsMode == "synced" && PreferenceUtil.showLyrics && PreferenceUtil.showLyricsTablet || PreferenceUtil.lyricsMode == "both" && PreferenceUtil.showLyrics && PreferenceUtil.showLyricsTablet) {
                        if (binding.scroll.visibility == View.GONE) {
                            binding.lrcFragment?.visibility = View.VISIBLE
                            binding.playerQueueSheet.visibility = View.GONE
                        }
                    } else {
                        binding.lrcFragment?.visibility = View.GONE
                        binding.playerQueueSheet.visibility = View.VISIBLE
                    }
                }
            }

            LYRICS_MODE -> {
                if (PreferenceUtil.lyricsMode == "id3" || PreferenceUtil.lyricsMode == "disabled") {
                    PreferenceUtil.showLyrics = false
                    PreferenceUtil.showLyricsTablet = false
                }
            }
        }
    }
}
