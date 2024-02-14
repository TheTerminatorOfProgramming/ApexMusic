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
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.AnimatedVectorDrawable
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.getSystemService
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import com.google.android.material.slider.Slider
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils
import com.ttop.app.apex.R
import com.ttop.app.apex.adapter.song.PlayingQueueAdapter
import com.ttop.app.apex.databinding.FragmentGradientPlayerBinding
import com.ttop.app.apex.extensions.*
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.helper.MusicProgressViewUpdateHelper
import com.ttop.app.apex.helper.PlayPauseButtonOnClickHandler
import com.ttop.app.apex.service.MusicService
import com.ttop.app.apex.ui.fragments.MusicSeekSkipTouchListener
import com.ttop.app.apex.ui.fragments.base.AbsPlayerFragment
import com.ttop.app.apex.util.MusicUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.color.MediaNotificationProcessor
import com.ttop.app.appthemehelper.util.ColorUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        binding.playbackControlsFragment.songFavourite.setOnClickListener {
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
        setupVolButtons()

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

        if (!PreferenceUtil.isVolumeControls) {
            binding.playbackControlsFragment.volUpButton.visibility = View.GONE
            binding.playbackControlsFragment.volDownButton.visibility = View.GONE
        }else {
            binding.playbackControlsFragment.volUpButton.visibility = View.VISIBLE
            binding.playbackControlsFragment.volDownButton.visibility = View.VISIBLE
        }
    }

    private fun setupVolButtons() {
        val mAudioManager: AudioManager = context?.getSystemService()!!

        var lastUpClick: Long
        var lastDownClick: Long

        binding.playbackControlsFragment.volUpButton.setOnClickListener {
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0)

            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

            lastUpClick = System.currentTimeMillis()

            Handler(Looper.getMainLooper()).postDelayed({
                if (System.currentTimeMillis() - lastUpClick >= 500) {
                    val currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                    val maxVolume: Int = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    val currentVolumeTotal = 100 * currentVolume / maxVolume

                    val newVol = getString(R.string.new_volume)

                    showToast("$newVol: $currentVolumeTotal%")
                }
            }, 500)
        }

        binding.playbackControlsFragment.volDownButton.setOnClickListener {
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0)

            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

            lastDownClick = System.currentTimeMillis()

            Handler(Looper.getMainLooper()).postDelayed({
                if (System.currentTimeMillis() - lastDownClick >= 500) {
                    val currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                    val maxVolume: Int = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    val currentVolumeTotal = 100 * currentVolume / maxVolume

                    showToast("New Volume: $currentVolumeTotal%")
                }
            }, 500)
        }
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
        var wasExpanded = false
        if (getQueuePanel().state == STATE_EXPANDED) {
            wasExpanded = getQueuePanel().state == STATE_EXPANDED
            getQueuePanel().state = STATE_COLLAPSED
            return wasExpanded
        }
        return wasExpanded
    }

    override fun toolbarIconColor(): Int {
        return Color.WHITE
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
        binding.playbackControlsFragment.songFavourite.setColorFilter(
            lastPlaybackControlsColor,
            PorterDuff.Mode.SRC_IN
        )
        binding.queueIcon.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN)
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

        binding.playbackControlsFragment.volUpButton.setColorFilter(lastPlaybackControlsColor)
        binding.playbackControlsFragment.volDownButton.setColorFilter(lastPlaybackControlsColor)

        updateRepeatState()
        updateShuffleState()
        updatePrevNextColor()
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
                binding.playbackControlsFragment.songFavourite.apply {
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
    }

    override fun onFavoriteStateChanged() {
        updateIsFavoriteIcon(animate = true)
    }

    override fun onQueueChanged() {
        super.onQueueChanged()
        updateLabel()
        playingQueueAdapter?.swapDataSet(MusicPlayerRemote.playingQueue)
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
        binding.shuffleButton.setOnClickListener { MusicPlayerRemote.toggleShuffleMode() }
    }

    fun updateShuffleState() {
        when (MusicPlayerRemote.shuffleMode) {
            MusicService.SHUFFLE_MODE_SHUFFLE ->
                binding.shuffleButton.setColorFilter(
                    lastPlaybackControlsColor,
                    PorterDuff.Mode.SRC_IN
                )
            else -> binding.shuffleButton.setColorFilter(
                lastDisabledPlaybackControlsColor,
                PorterDuff.Mode.SRC_IN
            )
        }
    }

    private fun setUpRepeatButton() {
        binding.repeatButton.setOnClickListener { MusicPlayerRemote.cycleRepeatMode() }
    }

    fun updateRepeatState() {
        when (MusicPlayerRemote.repeatMode) {
            MusicService.REPEAT_MODE_NONE -> {
                binding.repeatButton.setImageResource(R.drawable.ic_repeat)
                binding.repeatButton.setColorFilter(
                    lastDisabledPlaybackControlsColor,
                    PorterDuff.Mode.SRC_IN
                )
            }
            MusicService.REPEAT_MODE_ALL -> {
                binding.repeatButton.setImageResource(R.drawable.ic_repeat)
                binding.repeatButton.setColorFilter(
                    lastPlaybackControlsColor,
                    PorterDuff.Mode.SRC_IN
                )
            }
            MusicService.REPEAT_MODE_THIS -> {
                binding.repeatButton.setImageResource(R.drawable.ic_repeat_one)
                binding.repeatButton.setColorFilter(
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
