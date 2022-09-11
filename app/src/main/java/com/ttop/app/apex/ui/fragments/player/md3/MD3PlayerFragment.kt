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
package com.ttop.app.apex.ui.fragments.player.md3

import android.content.ContentUris
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.provider.MediaStore
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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager
import com.ttop.app.apex.EXTRA_ALBUM_ID
import com.ttop.app.apex.R
import com.ttop.app.apex.adapter.song.PlayingQueueAdapter
import com.ttop.app.apex.databinding.FragmentMd3PlayerBinding
import com.ttop.app.apex.dialogs.*
import com.ttop.app.apex.extensions.drawAboveSystemBars
import com.ttop.app.apex.extensions.keepScreenOn
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.model.Song
import com.ttop.app.apex.repository.RealRepository
import com.ttop.app.apex.ui.activities.tageditor.AbsTagEditorActivity
import com.ttop.app.apex.ui.activities.tageditor.SongTagEditorActivity
import com.ttop.app.apex.ui.fragments.base.AbsPlayerFragment
import com.ttop.app.apex.ui.fragments.base.goToArtist
import com.ttop.app.apex.ui.fragments.base.goToLyrics
import com.ttop.app.apex.ui.fragments.player.PlayerAlbumCoverFragment
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.NavigationUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.RingtoneManager
import com.ttop.app.apex.util.color.MediaNotificationProcessor
import com.ttop.app.appthemehelper.util.ATHUtil
import com.ttop.app.appthemehelper.util.ToolbarContentTintHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.get

class MD3PlayerFragment : AbsPlayerFragment(R.layout.fragment_md3_player) {

    private var lastColor: Int = 0
    override val paletteColor: Int
        get() = lastColor

    private lateinit var controlsFragment: MD3PlaybackControlsFragment

    private lateinit var wrappedAdapter: RecyclerView.Adapter<*>
    private var recyclerViewDragDropManager: RecyclerViewDragDropManager? = null
    private var recyclerViewSwipeManager: RecyclerViewSwipeManager? = null
    private var recyclerViewTouchActionGuardManager: RecyclerViewTouchActionGuardManager? = null
    private var playingQueueAdapter: PlayingQueueAdapter? = null
    private lateinit var linearLayoutManager: LinearLayoutManager

    private var _binding: FragmentMd3PlayerBinding? = null
    private val binding get() = _binding!!

    override fun onShow() {
        controlsFragment.show()
    }

    override fun onHide() {
        controlsFragment.hide()
        onBackPressed()
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun toolbarIconColor(): Int {
        return ATHUtil.resolveColor(requireContext(), R.attr.colorControlNormal)
    }

    override fun onColorChanged(color: MediaNotificationProcessor) {
        controlsFragment.setColor(color)
        lastColor = color.backgroundColor
        libraryViewModel.updateColor(color.backgroundColor)

        ToolbarContentTintHelper.colorizeToolbar(
            binding.playerToolbar,
            ATHUtil.resolveColor(requireContext(), R.attr.colorControlNormal),
            requireActivity()
        )
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
        _binding = FragmentMd3PlayerBinding.bind(view)
        setUpSubFragments()
        setupRecyclerView()
        setUpPlayerToolbar()
        playerToolbar().drawAboveSystemBars()

        if (PreferenceUtil.isQueueHidden){
            binding.playerQueueSheet?.visibility = View.GONE
        }else{
            binding.playerQueueSheet?.visibility = View.VISIBLE
        }

        if (PreferenceUtil.queueShowAlways) {
            binding.playerQueueSheet?.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        val song = MusicPlayerRemote.currentSong
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
                    if (binding.playerQueueSheet?.visibility == View.VISIBLE){
                        PreferenceUtil.isQueueHidden = true
                        binding.playerQueueSheet?.visibility = View.GONE
                    }else{
                        PreferenceUtil.isQueueHidden = false
                        binding.playerQueueSheet?.visibility = View.VISIBLE
                    }
                }else {
                    requireActivity().findNavController(R.id.fragment_container).navigate(
                        R.id.playing_queue_fragment,
                        null,
                        navOptions { launchSingleTop = true }
                    )
                    mainActivity.collapsePanel()
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
        }
        return false
    }

    private fun setUpSubFragments() {
        controlsFragment =
            childFragmentManager.findFragmentById(R.id.playbackControlsFragment) as MD3PlaybackControlsFragment
        val playerAlbumCoverFragment =
            childFragmentManager.findFragmentById(R.id.playerAlbumCoverFragment) as PlayerAlbumCoverFragment
        playerAlbumCoverFragment.setCallbacks(this)
    }

    private fun setUpPlayerToolbar() {
        binding.playerToolbar.inflateMenu(R.menu.menu_player)
        //binding.playerToolbar.menu.setUpWithIcons()
        binding.playerToolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }
        binding.playerToolbar.setOnMenuItemClickListener(this)

        ToolbarContentTintHelper.colorizeToolbar(
            binding.playerToolbar,
            ATHUtil.resolveColor(requireContext(), R.attr.colorControlNormal),
            requireActivity()
        )
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
        recyclerViewSwipeManager = RecyclerViewSwipeManager()

        val animator = DraggableItemAnimator()
        animator.supportsChangeAnimations = false
        wrappedAdapter =
            recyclerViewDragDropManager?.createWrappedAdapter(playingQueueAdapter!!) as RecyclerView.Adapter<*>
        wrappedAdapter =
            recyclerViewSwipeManager?.createWrappedAdapter(wrappedAdapter) as RecyclerView.Adapter<*>
        binding.recyclerView?.layoutManager = linearLayoutManager
        binding.recyclerView?.adapter = wrappedAdapter
        binding.recyclerView?.itemAnimator = animator
        binding.recyclerView?.let { recyclerViewTouchActionGuardManager?.attachRecyclerView(it) }
        binding.recyclerView?.let { recyclerViewDragDropManager?.attachRecyclerView(it) }
        binding.recyclerView?.let { recyclerViewSwipeManager?.attachRecyclerView(it) }

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

    override fun onQueueChanged() {
        super.onQueueChanged()
        updateQueue()
    }

    override fun onServiceConnected() {
        updateIsFavorite()
        updateQueue()
    }

    override fun onPlayingMetaChanged() {
        updateIsFavorite()
        updateQueuePosition()
    }

    override fun playerToolbar(): Toolbar {
        return binding.playerToolbar
    }

    companion object {

        fun newInstance(): MD3PlayerFragment {
            return MD3PlayerFragment()
        }
    }
}
