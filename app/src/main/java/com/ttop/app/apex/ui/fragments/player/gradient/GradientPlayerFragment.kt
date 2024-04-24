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
package com.ttop.app.apex.ui.fragments.player.gradient

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.AnimatedVectorDrawable
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.provider.MediaStore
import android.view.HapticFeedbackConstants
import android.view.MenuItem
import android.view.View
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import com.google.android.material.slider.Slider
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils
import com.ttop.app.apex.EXTRA_ALBUM_ID
import com.ttop.app.apex.R
import com.ttop.app.apex.adapter.song.PlayingQueueAdapter
import com.ttop.app.apex.databinding.FragmentGradientPlayerBinding
import com.ttop.app.apex.dialogs.AddToPlaylistDialog
import com.ttop.app.apex.dialogs.CreatePlaylistDialog
import com.ttop.app.apex.dialogs.DeleteSongsDialog
import com.ttop.app.apex.dialogs.PlaybackSpeedDialog
import com.ttop.app.apex.dialogs.SleepTimerDialog
import com.ttop.app.apex.dialogs.SongDetailDialog
import com.ttop.app.apex.dialogs.SongShareDialog
import com.ttop.app.apex.extensions.*
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.helper.MusicProgressViewUpdateHelper
import com.ttop.app.apex.helper.PlayPauseButtonOnClickHandler
import com.ttop.app.apex.repository.RealRepository
import com.ttop.app.apex.service.MusicService
import com.ttop.app.apex.ui.activities.tageditor.AbsTagEditorActivity
import com.ttop.app.apex.ui.activities.tageditor.SongTagEditorActivity
import com.ttop.app.apex.ui.fragments.MusicSeekSkipTouchListener
import com.ttop.app.apex.ui.fragments.base.AbsPlayerFragment
import com.ttop.app.apex.ui.fragments.base.goToArtist
import com.ttop.app.apex.util.MusicUtil
import com.ttop.app.apex.util.NavigationUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.RingtoneManager
import com.ttop.app.apex.util.color.MediaNotificationProcessor
import com.ttop.app.appthemehelper.util.ColorUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.get
import java.lang.StringBuilder

class GradientPlayerFragment : AbsPlayerFragment(R.layout.fragment_gradient_player),
    MusicProgressViewUpdateHelper.Callback,
    View.OnLayoutChangeListener, PopupMenu.OnMenuItemClickListener {
    private var lastColor: Int = 0
    private var lastPlaybackControlsColor: Int = 0
    private var lastDisabledPlaybackControlsColor: Int = 0
    private lateinit var progressViewUpdateHelper: MusicProgressViewUpdateHelper
    private lateinit var wrappedAdapter: RecyclerView.Adapter<*>
    private var recyclerViewDragDropManager: RecyclerViewDragDropManager? = null
    private var recyclerViewTouchActionGuardManager: RecyclerViewTouchActionGuardManager? = null
    private var playingQueueAdapter: PlayingQueueAdapter? = null
    private lateinit var linearLayoutManager: LinearLayoutManager
    private var navBarHeight = 0

    private var _binding: FragmentGradientPlayerBinding? = null
    private val binding get() = _binding!!

    private val embed: TextView get() = binding.embedded
    private val scroll: ScrollView get() = binding.scroll

    private val bottomSheetCallbackList = object : BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            mainActivity.getBottomSheetBehavior().isDraggable = false
            binding.playerQueueSheet.updatePadding(
                top = (slideOffset * binding.statusBarLayout.statusBar.height).toInt()
            )
            binding.container.updatePadding(
                bottom = ((1 - slideOffset) * navBarHeight).toInt()
            )
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            when (newState) {
                STATE_EXPANDED,
                STATE_DRAGGING,
                -> {
                    mainActivity.getBottomSheetBehavior().isDraggable = false
                }
                STATE_COLLAPSED -> {
                    resetToCurrentPosition()
                    mainActivity.getBottomSheetBehavior().isDraggable = true
                }
                else -> {
                    mainActivity.getBottomSheetBehavior().isDraggable = true
                }
            }
        }
    }

    private fun setupFavourite() {
        binding.songFavourite.setOnClickListener {
            toggleFavorite(MusicPlayerRemote.currentSong)
        }
    }

    private fun setupMenu() {
        binding.playbackControlsFragment.playerMenu.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), it)
            popupMenu.setOnMenuItemClickListener(this)
            popupMenu.inflate(R.menu.menu_player)
            popupMenu.menu.removeItem(R.id.action_queue)
            popupMenu.menu.removeItem(R.id.now_playing)
            popupMenu.menu.removeItem(R.id.action_rewind)
            popupMenu.menu.removeItem(R.id.action_fast_forward)
            popupMenu.menu.findItem(R.id.action_toggle_favorite).isVisible = false


            popupMenu.show()
        }
    }

    private fun setupPanel() {
        if (!binding.colorBackground.isLaidOut || binding.colorBackground.isLayoutRequested) {
            binding.colorBackground.addOnLayoutChangeListener(this)
            return
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressViewUpdateHelper = MusicProgressViewUpdateHelper(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGradientPlayerBinding.bind(view)
        setUpMusicControllers()
        setupPanel()
        setupRecyclerView()
        setupSheet()
        setupMenu()
        setupFavourite()

        ViewCompat.setOnApplyWindowInsetsListener(
            (binding.container)
        ) { v: View, insets: WindowInsetsCompat ->
            navBarHeight = insets.getBottomInsets()
            v.updatePadding(bottom = navBarHeight)
            insets
        }
        binding.playbackControlsFragment.root.drawAboveSystemBars()

        binding.queueIcon.setOnClickListener {
            if (binding.playerQueueSheet.visibility == View.VISIBLE) {
                playingQueueAdapter?.setButtonsActivate()
            }
        }

        embed.textSize = 24f

        if (PreferenceUtil.isEmbedLyricsActivated) {
            scroll.visibility = View.VISIBLE
            binding.mask.visibility = View.GONE
        }

        if (PreferenceUtil.lyricsMode == "disabled" || PreferenceUtil.lyricsMode == "synced") {
            playerToolbar()?.menu?.findItem(R.id.action_go_to_lyrics)?.isVisible = false
        }else {
            playerToolbar()?.menu?.findItem(R.id.action_go_to_lyrics)?.isVisible = true
        }

        binding.playbackControlsFragment.close.setOnClickListener {
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        val song = MusicPlayerRemote.currentSong
        requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        when (item.itemId) {
            R.id.action_playback_speed -> {
                PlaybackSpeedDialog.newInstance().show(childFragmentManager, "PLAYBACK_SETTINGS")
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
                    playingQueueAdapter?.setButtonsActivate()
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
                if (scroll.visibility == View.GONE){
                    scroll.visibility = View.VISIBLE
                    if (!PreferenceUtil.isLyricsMessageDisabled) {
                        showToast(getString(R.string.lyrics_message_enabled))
                    }

                    if (PreferenceUtil.lyricsScreenOn) {
                        mainActivity.keepScreenOn(true)
                    }else {
                        mainActivity.keepScreenOn(false)
                    }
                    binding.mask.visibility = View.GONE
                    requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    PreferenceUtil.isEmbedLyricsActivated = true
                }else{
                    scroll.visibility = View.GONE
                    if (!PreferenceUtil.isLyricsMessageDisabled) {
                        showToast(getString(R.string.lyrics_message_disabled))
                    }
                    mainActivity.keepScreenOn(false)

                    binding.mask.visibility = View.VISIBLE
                    requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    PreferenceUtil.isEmbedLyricsActivated = false
                }
            }
        }
        return false
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupSheet() {
        getQueuePanel().addBottomSheetCallback(bottomSheetCallbackList)
        binding.playerQueueSheet.setOnTouchListener { _, _ ->
            mainActivity.getBottomSheetBehavior().isDraggable = false
            getQueuePanel().isDraggable = true
            return@setOnTouchListener false
        }
    }

    private fun getQueuePanel(): BottomSheetBehavior<ConstraintLayout> {
        return from(binding.playerQueueSheet)
    }

    override fun onResume() {
        super.onResume()
        progressViewUpdateHelper.start()
    }

    override fun onPause() {
        super.onPause()
        recyclerViewDragDropManager?.cancelDrag()
        progressViewUpdateHelper.stop()
    }

    override fun playerToolbar(): Toolbar? {
        return null
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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (PreferenceUtil.isEmbedLyricsActivated) {
                scroll.visibility = View.VISIBLE
                binding.mask.visibility = View.GONE
            }else {
                binding.mask.visibility = View.VISIBLE
            }
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (PreferenceUtil.isEmbedLyricsActivated) {
                scroll.visibility = View.VISIBLE
                binding.mask.visibility = View.GONE
            }else {
                binding.mask.visibility = View.VISIBLE
            }
        }
    }

    override val paletteColor: Int
        get() = lastColor

    override fun onColorChanged(color: MediaNotificationProcessor) {
        lastColor = color.backgroundColor
        libraryViewModel.updateColor(color.backgroundColor)
        binding.mask.backgroundTintList = ColorStateList.valueOf(color.backgroundColor)
        binding.colorBackground.setBackgroundColor(color.backgroundColor)
        binding.playerQueueSheet.setBackgroundColor(ColorUtil.darkenColor(color.backgroundColor))

        lastPlaybackControlsColor = color.primaryTextColor

        lastDisabledPlaybackControlsColor = ColorUtil.withAlpha(color.primaryTextColor, 0.3f)

        binding.playbackControlsFragment.title.setTextColor(lastPlaybackControlsColor)
        binding.playbackControlsFragment.artist.setTextColor(lastDisabledPlaybackControlsColor)
        binding.playbackControlsFragment.playPauseButton.setColorFilter(
            lastPlaybackControlsColor,
            PorterDuff.Mode.SRC_IN
        )
        binding.playbackControlsFragment.nextButton.setColorFilter(
            lastPlaybackControlsColor,
            PorterDuff.Mode.SRC_IN
        )
        binding.playbackControlsFragment.previousButton.setColorFilter(
            lastPlaybackControlsColor,
            PorterDuff.Mode.SRC_IN
        )
        binding.songFavourite.setColorFilter(
            lastPlaybackControlsColor,
            PorterDuff.Mode.SRC_IN
        )
        binding.queueIcon.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN)
        binding.playbackControlsFragment.close.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN)
        binding.playbackControlsFragment.playerMenu.setColorFilter(
            lastPlaybackControlsColor,
            PorterDuff.Mode.SRC_IN
        )
        binding.playbackControlsFragment.songCurrentProgress.setTextColor(
            lastDisabledPlaybackControlsColor
        )
        binding.playbackControlsFragment.songTotalTime.setTextColor(
            lastDisabledPlaybackControlsColor
        )
        binding.nextSong.setTextColor(lastPlaybackControlsColor)
        binding.playbackControlsFragment.songInfo.setTextColor(lastDisabledPlaybackControlsColor)

        binding.playbackControlsFragment.progressSlider.applyColor(lastPlaybackControlsColor.ripAlpha())

        updateRepeatState()
        updateShuffleState()
        updatePrevNextColor()

        scroll.setBackgroundColor(color.backgroundColor)
        embed.setTextColor(lastPlaybackControlsColor)
    }

    override fun onFavoriteToggled() {
        toggleFavorite(MusicPlayerRemote.currentSong)
    }

    private fun updateIsFavoriteIcon(animate: Boolean = false) {
        lifecycleScope.launch(Dispatchers.IO) {
            val isFavorite: Boolean =
                libraryViewModel.isSongFavorite(MusicPlayerRemote.currentSong.id)
            withContext(Dispatchers.Main) {
                val icon = if (animate) {
                    if (isFavorite) R.drawable.avd_favorite else R.drawable.avd_unfavorite
                } else {
                    if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
                }
                binding.songFavourite.apply {
                    setImageResource(icon)
                    drawable.also {
                        if (it is AnimatedVectorDrawable) {
                            it.start()
                        }
                    }
                }
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        updateSong()
        updatePlayPauseDrawableState()
        updateQueue()
        updateIsFavoriteIcon()

        val string = StringBuilder()
        string.append("\n").append(MusicUtil.getLyrics(MusicPlayerRemote.currentSong)).append("\n")
        embed.text = string.toString()

    }

    override fun onPlayStateChanged() {
        updatePlayPauseDrawableState()
    }

    override fun onRepeatModeChanged() {
        updateRepeatState()
    }

    override fun onShuffleModeChanged() {
        updateShuffleState()
    }

    override fun onPlayingMetaChanged() {
        super.onPlayingMetaChanged()
        updateSong()
        updateQueuePosition()
        updateIsFavoriteIcon()

        val string = StringBuilder()
        string.append("\n").append(MusicUtil.getLyrics(MusicPlayerRemote.currentSong)).append("\n")
        embed.text = string.toString()
        scroll.scrollTo(0,0)
    }

    override fun onFavoriteStateChanged() {
        updateIsFavoriteIcon(animate = true)
    }

    override fun onQueueChanged() {
        super.onQueueChanged()
        updateLabel()
        playingQueueAdapter?.swapDataSet(MusicPlayerRemote.playingQueue)

        val string = StringBuilder()
        string.append("\n").append(MusicUtil.getLyrics(MusicPlayerRemote.currentSong)).append("\n")
        embed.text = string.toString()
    }

    private fun updateSong() {
        val song = MusicPlayerRemote.currentSong
        binding.playbackControlsFragment.title.text = song.title
        binding.playbackControlsFragment.artist.text = song.artistName
        updateLabel()

        binding.playbackControlsFragment.songInfo.text = getSongInfo(song)
    }

    private fun setUpMusicControllers() {
        setUpPlayPauseFab()
        setUpPrevNext()
        setUpRepeatButton()
        setUpShuffleButton()
        setUpProgressSlider()
        binding.playbackControlsFragment.title.isSelected = true
        binding.playbackControlsFragment.artist.isSelected = true
    }

    private fun updatePlayPauseDrawableState() {
        if (MusicPlayerRemote.isPlaying) {
            binding.playbackControlsFragment.playPauseButton.setImageResource(R.drawable.ic_pause_white_64dp)
        } else {
            binding.playbackControlsFragment.playPauseButton.setImageResource(R.drawable.ic_play_arrow_white_64dp)
        }
    }

    private fun setUpPlayPauseFab() {
        binding.playbackControlsFragment.playPauseButton.setOnClickListener(
            PlayPauseButtonOnClickHandler()
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpPrevNext() {
        updatePrevNextColor()
        binding.playbackControlsFragment.nextButton.setOnTouchListener(
            MusicSeekSkipTouchListener(
                requireActivity(),
                true
            )
        )
        binding.playbackControlsFragment.previousButton.setOnTouchListener(
            MusicSeekSkipTouchListener(requireActivity(), false)
        )
    }

    private fun updatePrevNextColor() {
        binding.playbackControlsFragment.nextButton.setColorFilter(
            lastPlaybackControlsColor,
            PorterDuff.Mode.SRC_IN
        )
        binding.playbackControlsFragment.previousButton.setColorFilter(
            lastPlaybackControlsColor,
            PorterDuff.Mode.SRC_IN
        )
    }

    private fun setUpShuffleButton() {
        binding.playbackControlsFragment.shuffleButton.setOnClickListener { MusicPlayerRemote.toggleShuffleMode() }
    }

    private fun updateShuffleState() {
        when (MusicPlayerRemote.shuffleMode) {
            MusicService.SHUFFLE_MODE_SHUFFLE ->
                binding.playbackControlsFragment.shuffleButton.setColorFilter(
                    lastPlaybackControlsColor,
                    PorterDuff.Mode.SRC_IN
                )
            else -> binding.playbackControlsFragment.shuffleButton.setColorFilter(
                lastDisabledPlaybackControlsColor,
                PorterDuff.Mode.SRC_IN
            )
        }
    }

    private fun setUpRepeatButton() {
        binding.playbackControlsFragment.repeatButton.setOnClickListener { MusicPlayerRemote.cycleRepeatMode() }
    }

    private fun updateRepeatState() {
        when (MusicPlayerRemote.repeatMode) {
            MusicService.REPEAT_MODE_NONE -> {
                binding.playbackControlsFragment.repeatButton.setImageResource(R.drawable.ic_repeat)
                binding.playbackControlsFragment.repeatButton.setColorFilter(
                    lastDisabledPlaybackControlsColor,
                    PorterDuff.Mode.SRC_IN
                )
            }
            MusicService.REPEAT_MODE_ALL -> {
                binding.playbackControlsFragment.repeatButton.setImageResource(R.drawable.ic_repeat)
                binding.playbackControlsFragment.repeatButton.setColorFilter(
                    lastPlaybackControlsColor,
                    PorterDuff.Mode.SRC_IN
                )
            }
            MusicService.REPEAT_MODE_THIS -> {
                binding.playbackControlsFragment.repeatButton.setImageResource(R.drawable.ic_repeat_one)
                binding.playbackControlsFragment.repeatButton.setColorFilter(
                    lastPlaybackControlsColor,
                    PorterDuff.Mode.SRC_IN
                )
            }
        }
    }

    private fun updateLabel() {
        (MusicPlayerRemote.playingQueue.size - 1).apply {
            if (this == (MusicPlayerRemote.position)) {
                binding.nextSong.text = context?.resources?.getString(R.string.last_song)
            } else {
                val title = MusicPlayerRemote.playingQueue[MusicPlayerRemote.position + 1].title
                binding.nextSong.text = title
            }
        }
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
        oldBottom: Int,
    ) {
        val panel = getQueuePanel()
        if (panel.state == STATE_COLLAPSED) {
            panel.peekHeight = binding.container.height
        } else if (panel.state == STATE_EXPANDED) {
            panel.peekHeight = binding.container.height + navBarHeight
        }
    }

    private fun setupRecyclerView() {
        playingQueueAdapter = PlayingQueueAdapter(
            requireActivity() as AppCompatActivity,
            MusicPlayerRemote.playingQueue.toMutableList(),
            MusicPlayerRemote.position,
            R.layout.item_queue
        )
        linearLayoutManager = LinearLayoutManager(requireContext())
        recyclerViewTouchActionGuardManager = RecyclerViewTouchActionGuardManager()
        recyclerViewDragDropManager = RecyclerViewDragDropManager()

        val animator = DraggableItemAnimator()
        animator.supportsChangeAnimations = false
        wrappedAdapter =
            recyclerViewDragDropManager?.createWrappedAdapter(playingQueueAdapter!!) as RecyclerView.Adapter<*>
        binding.recyclerView.apply {
            layoutManager = linearLayoutManager
            adapter = wrappedAdapter
            itemAnimator = animator
            recyclerViewTouchActionGuardManager?.attachRecyclerView(this)
            recyclerViewDragDropManager?.attachRecyclerView(this)
        }

        linearLayoutManager.scrollToPositionWithOffset(MusicPlayerRemote.position + 1, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        getQueuePanel().removeBottomSheetCallback(bottomSheetCallbackList)
        if (recyclerViewDragDropManager != null) {
            recyclerViewDragDropManager?.release()
            recyclerViewDragDropManager = null
        }

        WrapperAdapterUtils.releaseAll(wrappedAdapter)
        _binding = null
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

    private fun setUpProgressSlider() {
        val progressSlider = binding.playbackControlsFragment.progressSlider
        progressSlider.addOnChangeListener(Slider.OnChangeListener { _, value, fromUser ->
            if (fromUser) {
                onUpdateProgressViews(
                    value.toInt(),
                    MusicPlayerRemote.songDurationMillis
                )
            }
        })
        progressSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                progressViewUpdateHelper.stop()
            }

            override fun onStopTrackingTouch(slider: Slider) {
                MusicPlayerRemote.seekTo(slider.value.toInt())
                progressViewUpdateHelper.start()
            }
        })
    }

    override fun onUpdateProgressViews(progress: Int, total: Int) {
        val progressSlider = binding.playbackControlsFragment.progressSlider
        progressSlider.valueTo = total.toFloat()

        progressSlider.value =
            progress.toFloat().coerceIn(progressSlider.valueFrom, progressSlider.valueTo)

        binding.playbackControlsFragment.songTotalTime.text =
            MusicUtil.getReadableDurationString(total.toLong())
        binding.playbackControlsFragment.songCurrentProgress.text =
            MusicUtil.getReadableDurationString(progress.toLong())
    }
}
