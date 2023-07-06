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
package com.ttop.app.apex.ui.fragments.player.full

import android.content.ContentUris
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.provider.MediaStore
import android.view.HapticFeedbackConstants
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager
import com.ttop.app.apex.EXTRA_ALBUM_ID
import com.ttop.app.apex.R
import com.ttop.app.apex.adapter.song.PlayingQueueAdapter
import com.ttop.app.apex.databinding.FragmentFullBinding
import com.ttop.app.apex.dialogs.*
import com.ttop.app.apex.extensions.*
import com.ttop.app.apex.glide.ApexGlideExtension
import com.bumptech.glide.Glide
import com.ttop.app.apex.glide.ApexGlideExtension.artistImageOptions
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.model.Song
import com.ttop.app.apex.repository.RealRepository
import com.ttop.app.apex.ui.activities.tageditor.AbsTagEditorActivity
import com.ttop.app.apex.ui.activities.tageditor.SongTagEditorActivity
import com.ttop.app.apex.ui.fragments.base.AbsPlayerFragment
import com.ttop.app.apex.ui.fragments.base.goToArtist
import com.ttop.app.apex.ui.fragments.base.goToLyrics
import com.ttop.app.apex.ui.fragments.player.CoverLyricsFragment
import com.ttop.app.apex.ui.fragments.player.PlayerAlbumCoverFragment
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.NavigationUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.RingtoneManager
import com.ttop.app.apex.util.color.MediaNotificationProcessor
import com.ttop.app.appthemehelper.util.ColorUtil
import com.ttop.app.appthemehelper.util.ToolbarContentTintHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.get

class FullPlayerFragment : AbsPlayerFragment(R.layout.fragment_full) {
    private var _binding: FragmentFullBinding? = null
    private val binding get() = _binding!!

    override fun playerToolbar(): Toolbar {
        return binding.playerToolbar
    }

    private var lastColor: Int = 0
    override val paletteColor: Int
        get() = lastColor

    private lateinit var wrappedAdapter: RecyclerView.Adapter<*>
    private var recyclerViewDragDropManager: RecyclerViewDragDropManager? = null
    private var recyclerViewTouchActionGuardManager: RecyclerViewTouchActionGuardManager? = null
    private var playingQueueAdapter: PlayingQueueAdapter? = null
    private lateinit var linearLayoutManager: LinearLayoutManager

    private lateinit var controlsFragment: FullPlaybackControlsFragment

    private fun setUpPlayerToolbar() {
        binding.playerToolbar.apply {
            binding.playerToolbar.setNavigationOnClickListener {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFullBinding.bind(view)

        setUpSubFragments()
        setUpPlayerToolbar()
        setupRecyclerView()
        setupArtist()
        binding.nextSong.isSelected = true
        binding.playbackControlsFragment.drawAboveSystemBars()
    }

    private fun setupArtist() {
        binding.artistImage.setOnClickListener {
            goToArtist(mainActivity)
        }
    }

    fun setColor(color: MediaNotificationProcessor) {
        binding.nextSong.setTextColor(color.primaryTextColor)
        binding.nextSongLabel.setTextColor(color.secondaryTextColor)
    }

    private fun setUpSubFragments() {
        controlsFragment = whichFragment(R.id.playbackControlsFragment)
        val coverFragment: PlayerAlbumCoverFragment = whichFragment(R.id.playerAlbumCoverFragment)
        coverFragment.setCallbacks(this)
        coverFragment.removeSlideEffect()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        val song = MusicPlayerRemote.currentSong
        requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        when (item.itemId) {
            R.id.action_playback_speed -> {
                PlaybackSpeedDialog.newInstance().show(childFragmentManager, "PLAYBACK_SETTINGS")
                return true
            }
            R.id.action_toggle_lyrics -> {
                PreferenceUtil.showLyrics = !PreferenceUtil.showLyrics
                showLyricsIcon(item)
                if (PreferenceUtil.lyricsScreenOn && PreferenceUtil.showLyrics) {
                    mainActivity.keepScreenOn(true)
                } else if (!PreferenceUtil.isScreenOnEnabled && !PreferenceUtil.showLyrics) {
                    mainActivity.keepScreenOn(false)
                }
                return true
            }
            R.id.action_go_to_lyrics -> {
                goToLyrics(requireActivity())
                return true
            }
            R.id.action_toggle_favorite -> {
                toggleFavorite(song)
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
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
                if (ApexUtil.isTablet) {
                    val playerAlbumCoverFragment: PlayerAlbumCoverFragment =
                        whichFragment(R.id.playerAlbumCoverFragment)
                    if (binding.playerQueueSheet?.visibility == View.VISIBLE){
                        binding.playerQueueSheet?.visibility = View.GONE
                        playerAlbumCoverFragment.updatePlayingQueue()
                        requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    }else{
                        binding.playerQueueSheet?.visibility = View.VISIBLE
                        playerAlbumCoverFragment.updatePlayingQueue()
                    }
                }else {
                    if (ApexUtil.isLandscape) {
                        requireActivity().findNavController(R.id.fragment_container).navigate(
                            R.id.playing_queue_fragment,
                            null,
                            navOptions { launchSingleTop = true }
                        )
                        mainActivity.collapsePanel()
                    }
                }
                return true
            }
            R.id.action_show_lyrics -> {
                goToLyrics(requireActivity())
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
                    if (binding.playerQueueSheet?.visibility == View.VISIBLE){
                        binding.playerQueueSheet?.visibility = View.GONE
                    }else{
                        binding.playerQueueSheet?.visibility = View.VISIBLE
                    }
                }
                return true
            }
        }
        return false
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

    override fun onColorChanged(color: MediaNotificationProcessor) {
        lastColor = color.backgroundColor
        binding.mask.backgroundTintList = ColorStateList.valueOf(color.backgroundColor)
        controlsFragment.setColor(color)
        setColor(color)
        libraryViewModel.updateColor(color.backgroundColor)
        ToolbarContentTintHelper.colorizeToolbar(binding.playerToolbar, Color.WHITE, activity)
    }

    override fun onFavoriteToggled() {
        toggleFavorite(MusicPlayerRemote.currentSong)
        controlsFragment.onFavoriteToggled()
    }

    override fun toggleFavorite(song: Song) {
        super.toggleFavorite(song)
        if (song.id == MusicPlayerRemote.currentSong.id) {
            updateIsFavorite()
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        updateArtistImage()
        updateLabel()
        updateQueue()
    }

    override fun onPlayingMetaChanged() {
        super.onPlayingMetaChanged()
        updateArtistImage()
        updateLabel()
        updateQueuePosition()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateArtistImage() {
        libraryViewModel.artist(MusicPlayerRemote.currentSong.artistId)
            .observe(viewLifecycleOwner) { artist ->
                if (artist.id != -1L) {
                    Glide.with(requireActivity())
                        .load(ApexGlideExtension.getArtistModel(artist))
                        .artistImageOptions(artist)
                        .into(binding.artistImage)
                }

            }
    }

    override fun onQueueChanged() {
        super.onQueueChanged()
        if (MusicPlayerRemote.playingQueue.isNotEmpty()) updateLabel()
        updateQueue()
    }

    private fun setupRecyclerView() {
        playingQueueAdapter = PlayingQueueAdapter(
            requireActivity() as AppCompatActivity,
            MusicPlayerRemote.playingQueue.toMutableList(),
            MusicPlayerRemote.position,
            R.layout.item_queue_player
        )
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

        linearLayoutManager.scrollToPositionWithOffset(MusicPlayerRemote.position + 1, 0)
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
        linearLayoutManager.scrollToPositionWithOffset(MusicPlayerRemote.position + 1, 0)
    }

    private fun updateLabel() {
        if (MusicPlayerRemote.playingQueue.isNotEmpty()) {
            if ((MusicPlayerRemote.playingQueue.size - 1) == (MusicPlayerRemote.position)) {
                binding.nextSongLabel.setText(R.string.last_song)
                binding.nextSong.hide()
            } else {
                val title = MusicPlayerRemote.playingQueue[MusicPlayerRemote.position + 1].title
                binding.nextSongLabel.setText(R.string.next_song)
                binding.nextSong.apply {
                    text = title
                    show()
                }
            }
        }
    }
}
