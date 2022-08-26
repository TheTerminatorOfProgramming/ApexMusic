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
import android.content.SharedPreferences
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager
import com.ttop.app.apex.R
import com.ttop.app.apex.SNOWFALL
import com.ttop.app.apex.adapter.song.PlayingQueueAdapter
import com.ttop.app.apex.databinding.FragmentPlayerBinding
import com.ttop.app.apex.extensions.*
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.model.Song
import com.ttop.app.apex.ui.fragments.base.AbsPlayerFragment
import com.ttop.app.apex.ui.fragments.player.PlayerAlbumCoverFragment
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.ViewUtil
import com.ttop.app.apex.util.color.MediaNotificationProcessor
import com.ttop.app.apex.views.DrawableGradient
import com.ttop.app.appthemehelper.util.ToolbarContentTintHelper

class PlayerFragment : AbsPlayerFragment(R.layout.fragment_player),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private var lastColor: Int = 0
    override val paletteColor: Int
        get() = lastColor

    private lateinit var wrappedAdapter: RecyclerView.Adapter<*>
    private var recyclerViewDragDropManager: RecyclerViewDragDropManager? = null
    private var recyclerViewSwipeManager: RecyclerViewSwipeManager? = null
    private var recyclerViewTouchActionGuardManager: RecyclerViewTouchActionGuardManager? = null
    private var playingQueueAdapter: PlayingQueueAdapter? = null
    private lateinit var linearLayoutManager: LinearLayoutManager

    private lateinit var controlsFragment: PlayerPlaybackControlsFragment
    private var valueAnimator: ValueAnimator? = null

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!


    private fun colorize(i: Int) {
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

    override fun toolbarIconColor() = colorControlNormal()

    override fun onColorChanged(color: MediaNotificationProcessor) {
        controlsFragment.setColor(color)
        lastColor = color.backgroundColor
        libraryViewModel.updateColor(color.backgroundColor)

        ToolbarContentTintHelper.colorizeToolbar(
            binding.playerToolbar,
            colorControlNormal(),
            requireActivity()
        )

        if (PreferenceUtil.isAdaptiveColor) {
            colorize(color.backgroundColor)
        }
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
        startOrStopSnow(PreferenceUtil.isSnowFalling)

        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .registerOnSharedPreferenceChangeListener(this)
        playerToolbar().drawAboveSystemBars()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .unregisterOnSharedPreferenceChangeListener(this)
        _binding = null
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
        binding.playerToolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }
        binding.playerToolbar.setOnMenuItemClickListener(this)

        ToolbarContentTintHelper.colorizeToolbar(
            binding.playerToolbar,
            colorControlNormal(),
            requireActivity()
        )
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == SNOWFALL) {
            startOrStopSnow(PreferenceUtil.isSnowFalling)
        }
    }

    private fun startOrStopSnow(isSnowFalling: Boolean) {
        if (_binding == null) return
        if (isSnowFalling && !surfaceColor().isColorLight) {
            binding.snowfallView.isVisible = true
            binding.snowfallView.restartFalling()
        } else {
            binding.snowfallView.isVisible = false
            binding.snowfallView.stopFalling()
        }
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

        fun newInstance(): PlayerFragment {
            return PlayerFragment()
        }
    }
}
