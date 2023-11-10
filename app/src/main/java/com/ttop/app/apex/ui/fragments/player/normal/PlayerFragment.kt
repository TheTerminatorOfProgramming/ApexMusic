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
package com.ttop.app.apex.ui.fragments.player.normal

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.ContentUris
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.GradientDrawable
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.provider.MediaStore
import android.view.HapticFeedbackConstants
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.animation.doOnEnd
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.navOptions
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager
import com.ttop.app.apex.EXTRA_ALBUM_ID
import com.ttop.app.apex.R
import com.ttop.app.apex.adapter.song.PlayingQueueAdapter
import com.ttop.app.apex.databinding.FragmentPlayerBinding
import com.ttop.app.apex.dialogs.*
import com.ttop.app.apex.extensions.*
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.model.Song
import com.ttop.app.apex.repository.RealRepository
import com.ttop.app.apex.ui.activities.tageditor.AbsTagEditorActivity
import com.ttop.app.apex.ui.activities.tageditor.SongTagEditorActivity
import com.ttop.app.apex.ui.fragments.base.AbsPlayerFragment
import com.ttop.app.apex.ui.fragments.base.goToArtist
import com.ttop.app.apex.ui.fragments.base.goToLyrics
import com.ttop.app.apex.ui.fragments.player.PlayerAlbumCoverFragment
import com.ttop.app.apex.util.*
import com.ttop.app.apex.util.color.MediaNotificationProcessor
import com.ttop.app.apex.views.DrawableGradient
import com.ttop.app.appthemehelper.util.ToolbarContentTintHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.get

class PlayerFragment : AbsPlayerFragment(R.layout.fragment_player) {

    private var lastColor: Int = 0
    private var toolbarColor: Int =0
    override val paletteColor: Int
        get() = lastColor

    private lateinit var wrappedAdapter: RecyclerView.Adapter<*>
    private var recyclerViewDragDropManager: RecyclerViewDragDropManager? = null
    private var recyclerViewTouchActionGuardManager: RecyclerViewTouchActionGuardManager? = null
    private var playingQueueAdapter: PlayingQueueAdapter? = null
    private lateinit var linearLayoutManager: LinearLayoutManager

    private lateinit var controlsFragment: PlayerPlaybackControlsFragment
    private var valueAnimator: ValueAnimator? = null

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    private fun colorize(i: Int) {
        if (PreferenceUtil.isPlayerBackgroundType) {
            //GRADIENT
            if (valueAnimator != null) {
                valueAnimator?.cancel()
            }

            valueAnimator = ValueAnimator.ofObject(
                ArgbEvaluator(),
                surfaceColor(),
                i
            )
            valueAnimator?.addUpdateListener { animation ->
                if (isAdded) {
                    val drawable = DrawableGradient(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        intArrayOf(
                            animation.animatedValue as Int,
                            surfaceColor()
                        ), 0
                    )
                    binding.colorGradientBackground.background = drawable
                }
            }
            valueAnimator?.setDuration(ViewUtil.APEX_MUSIC_ANIM_TIME.toLong())?.start()
        }else {
            //SINGLE COLOR
            binding.colorGradientBackground.setBackgroundColor(i)
        }
    }

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

    override fun toolbarIconColor() =
        if (PreferenceUtil.isAdaptiveColor) {
        toolbarColor
    }else {
        colorControlNormal()
    }

    override fun onColorChanged(color: MediaNotificationProcessor) {
        controlsFragment.setColor(color)
        lastColor = color.backgroundColor
        toolbarColor = color.secondaryTextColor
        libraryViewModel.updateColor(color.backgroundColor)

        ToolbarContentTintHelper.colorizeToolbar(
            binding.playerToolbar,
            toolbarIconColor(),
            requireActivity()
        )

        if (PreferenceUtil.isAdaptiveColor) {
            colorize(color.backgroundColor)

            if (PreferenceUtil.isColorAnimate) {
                val animator =
                    binding.colorGradientBackground?.let { controlsFragment.createRevealAnimator(it) }
                animator?.doOnEnd {
                    _binding?.root?.setBackgroundColor(color.backgroundColor)
                }
                animator?.start()
            }
        }

        playingQueueAdapter?.setTextColor(color.secondaryTextColor)
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
        _binding = FragmentPlayerBinding.bind(view)
        setUpSubFragments()
        setUpPlayerToolbar()
        setupRecyclerView()

        playerToolbar().drawAboveSystemBars()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
            R.id.action_reorder -> {
                if (binding.playerQueueSheet.visibility == View.VISIBLE) {
                    if (playingQueueAdapter?.getButtonsActivate() == true) {
                        playingQueueAdapter?.setButtonsActivate(false)
                    }else {
                        playingQueueAdapter?.setButtonsActivate(true)
                    }
                }
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
                    if (binding.playerQueueSheet.visibility == View.VISIBLE){
                        binding.playerQueueSheet.visibility = View.GONE
                    }else{
                        binding.playerQueueSheet.visibility = View.VISIBLE
                    }
                }
            }
        }
        return false
    }

    private fun setUpSubFragments() {
        controlsFragment = whichFragment(R.id.playbackControlsFragment)
        val playerAlbumCoverFragment: PlayerAlbumCoverFragment =
            whichFragment(R.id.playerAlbumCoverFragment)
        playerAlbumCoverFragment.setCallbacks(this)
    }

    private fun setUpPlayerToolbar() {
        binding.playerToolbar.inflateMenu(R.menu.menu_player)
        //binding.playerToolbar.menu.setUpWithIcons()
        binding.playerToolbar.setNavigationOnClickListener {
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.playerToolbar.setOnMenuItemClickListener(this)

        ToolbarContentTintHelper.colorizeToolbar(
            binding.playerToolbar,
            toolbarIconColor(),
            requireActivity()
        )
    }

    private fun setupRecyclerView() {
        playingQueueAdapter = if (ApexUtil.isTablet) {
            if(ApexUtil.isLandscape) {
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
            }else {
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
        }else {
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
        binding.recyclerView.stopScroll()
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

        fun newInstance(): PlayerFragment {
            return PlayerFragment()
        }
    }
}
