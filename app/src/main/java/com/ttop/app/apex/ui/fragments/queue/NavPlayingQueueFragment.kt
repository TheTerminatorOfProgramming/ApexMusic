/*
 * Copyright (c) 2024 Jesse Collins (TTOP).
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
package com.ttop.app.apex.ui.fragments.queue

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager
import com.ttop.app.apex.R
import com.ttop.app.apex.adapter.song.PlayingQueueAdapter
import com.ttop.app.apex.databinding.FragmentNavPlayingQueueBinding
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.extensions.darkAccentColor
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.ui.activities.MainActivity
import com.ttop.app.apex.ui.fragments.LibraryViewModel
import com.ttop.app.apex.ui.fragments.base.AbsMusicServiceFragment
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.ThemedFastScroller
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class NavPlayingQueueFragment : AbsMusicServiceFragment(R.layout.fragment_nav_playing_queue) {

    private var _binding: FragmentNavPlayingQueueBinding? = null
    private val binding get() = _binding!!
    private var wrappedAdapter: RecyclerView.Adapter<*>? = null
    private var recyclerViewDragDropManager: RecyclerViewDragDropManager? = null
    private var recyclerViewTouchActionGuardManager: RecyclerViewTouchActionGuardManager? = null
    private var playingQueueAdapter: PlayingQueueAdapter? = null
    private lateinit var linearLayoutManager: LinearLayoutManager

    val libraryViewModel: LibraryViewModel by activityViewModel()

    val mainActivity: MainActivity
        get() = activity as MainActivity

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentNavPlayingQueueBinding.bind(view)
        if (activity?.supportFragmentManager?.isStateSaved?.not() == true) {
            setupToolbar()
            setUpRecyclerView()

            checkForMargins()

            binding.shuffleButton.apply {
                setOnClickListener {
                    libraryViewModel.shuffleSongs()
                }
            }
        } else {
            val currentFragment: Fragment? =
                activity?.supportFragmentManager?.findFragmentById(R.id.fragment_container)
            if (currentFragment != null) {
                activity?.supportFragmentManager?.beginTransaction()
                    ?.detach(currentFragment)?.commitAllowingStateLoss()

                activity?.supportFragmentManager?.beginTransaction()
                    ?.attach(currentFragment)?.commitAllowingStateLoss()
            }
            setupToolbar()
            setUpRecyclerView()

            checkForMargins()

            binding.shuffleButton.apply {
                setOnClickListener {
                    libraryViewModel.shuffleSongs()
                }
            }
        }

        binding.shuffleButton.backgroundTintList = ColorStateList.valueOf(accentColor())
        binding.shuffleButton.imageTintList =
            ColorStateList.valueOf(requireContext().darkAccentColor())

        libraryViewModel.getFabMargin().observe(viewLifecycleOwner) {
            binding.shuffleButton.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = if (ApexUtil.isTablet) {
                    if (MusicPlayerRemote.playingQueue.isNotEmpty()) {
                        ApexUtil.dpToMargin(74)
                    } else {
                        ApexUtil.dpToMargin(10)
                    }
                } else {
                    if (MusicPlayerRemote.playingQueue.isNotEmpty()) {
                        ApexUtil.dpToMargin(154)
                    } else {
                        ApexUtil.dpToMargin(100)
                    }
                }
            }
        }
        activity?.window?.statusBarColor = requireActivity().darkAccentColor()
    }

    private fun checkForMargins() {
        binding.recyclerView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            bottomMargin = if (ApexUtil.isTablet) {
                if (MusicPlayerRemote.playingQueue.isNotEmpty()) {
                    ApexUtil.dpToMargin(64)
                } else {
                    ApexUtil.dpToMargin(0)
                }
            } else {
                if (MusicPlayerRemote.playingQueue.isNotEmpty()) {
                    ApexUtil.dpToMargin(144)
                } else {
                    ApexUtil.dpToMargin(80)
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val currentFragment: Fragment? =
            activity?.supportFragmentManager?.findFragmentById(R.id.fragment_container)
        if (currentFragment != null) {
            activity?.supportFragmentManager?.beginTransaction()
                ?.detach(currentFragment)?.commitAllowingStateLoss()

            activity?.supportFragmentManager?.beginTransaction()
                ?.attach(currentFragment)?.commitAllowingStateLoss()
        }
        checkForMargins()
    }

    private fun setUpRecyclerView() {
        playingQueueAdapter = PlayingQueueAdapter(
            requireActivity() as AppCompatActivity,
            MusicPlayerRemote.playingQueue.toMutableList(),
            MusicPlayerRemote.position,
            R.layout.item_nav_queue
        )
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

        if (PreferenceUtil.scrollbarStyle != "disabled") {
            ThemedFastScroller.create(
                binding.recyclerView,
                PreferenceUtil.scrollbarStyle == "auto_hide"
            )
        }
    }

    override fun onQueueChanged() {
        updateQueue()
        checkForMargins()
    }

    override fun onResume() {
        super.onResume()
        checkForMargins()
    }

    override fun onMediaStoreChanged() {
        updateQueue()
    }

    override fun onPlayingMetaChanged() {
        updateQueuePosition()
    }

    private fun updateQueuePosition() {
        playingQueueAdapter?.setCurrent(MusicPlayerRemote.position)
        resetToCurrentPosition()
    }

    private fun updateQueue() {
        playingQueueAdapter?.swapDataSet(MusicPlayerRemote.playingQueue, MusicPlayerRemote.position)
    }

    private fun resetToCurrentPosition() {
        binding.recyclerView.stopScroll()
        linearLayoutManager.scrollToPositionWithOffset(MusicPlayerRemote.position, 0)
    }

    private fun setupToolbar() {
        binding.appBarLayout.toolbar.isTitleCentered = true

        binding.appBarLayout.title = getString(R.string.queue_short)

        binding.appBarLayout.toolbar.apply {
            navigationIcon = null
        }
    }
}