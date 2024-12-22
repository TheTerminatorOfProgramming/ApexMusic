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
package com.ttop.app.apex.ui.fragments.base

import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Environment
import android.view.HapticFeedbackConstants
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat.SRC_IN
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.transition.MaterialFadeThrough
import com.ttop.app.apex.R
import com.ttop.app.apex.adapter.base.AbsMultiSelectAdapter
import com.ttop.app.apex.databinding.FragmentMainRecyclerBinding
import com.ttop.app.apex.dialogs.CreatePlaylistDialog
import com.ttop.app.apex.dialogs.ImportPlaylistDialog
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.extensions.getDrawableCompat
import com.ttop.app.apex.extensions.surfaceColor
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.interfaces.IScrollHelper
import com.ttop.app.apex.libraries.appthemehelper.common.ATHToolbarActivity
import com.ttop.app.apex.libraries.appthemehelper.util.ToolbarContentTintHelper
import com.ttop.app.apex.libraries.fastscroller.FastScroller
import com.ttop.app.apex.libraries.fastscroller.FastScrollerBuilder
import com.ttop.app.apex.ui.fragments.folder.FoldersFragment.Companion.AUDIO_FILE_FILTER
import com.ttop.app.apex.util.ApexStaticUtil
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.ColorUtil
import com.ttop.app.apex.util.IntroPrefs
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.getExternalStoragePublicDirectory
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.abs

abstract class AbsRecyclerViewFragment<A : RecyclerView.Adapter<*>, LM : RecyclerView.LayoutManager> :
    AbsMainActivityFragment(R.layout.fragment_main_recycler), IScrollHelper {

    private var _binding: FragmentMainRecyclerBinding? = null
    private val binding get() = _binding!!
    protected var adapter: A? = null
    protected var layoutManager: LM? = null
    val shuffleButton get() = binding.shuffleButton
    abstract val isShuffleVisible: Boolean

    val toolbar: Toolbar get() = binding.appBarLayout.toolbar
    val appBarLayout: AppBarLayout get() = binding.appBarLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMainRecyclerBinding.bind(view)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        enterTransition = MaterialFadeThrough().addTarget(binding.recyclerView)
        reenterTransition = MaterialFadeThrough().addTarget(binding.recyclerView)
        mainActivity.setSupportActionBar(toolbar)
        mainActivity.supportActionBar?.title = null
        initLayoutManager()
        initAdapter()
        checkForMargins()
        setUpRecyclerView()
        setupToolbar()

        // Add listeners when shuffle is visible
        if (isShuffleVisible) {
            binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0) {
                        binding.shuffleButton.hide()
                    } else if (dy < 0) {
                        binding.shuffleButton.show()
                    }

                }
            })

            binding.shuffleButton.apply {
                setOnClickListener {
                    onShuffleClicked()
                }
            }

            binding.shuffleButton.backgroundTintList = ColorStateList.valueOf(accentColor())
            binding.shuffleButton.imageTintList = ColorStateList.valueOf(surfaceColor())
        } else {
            binding.shuffleButton.isVisible = false
        }

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

        if (!IntroPrefs(requireContext()).hasIntroShown) {
            TapTargetView.showFor(
                activity,
                TapTarget.forView(
                    binding.shuffleButton,
                    getString(R.string.shuffle_button),
                    getString(R.string.shuffle_button_desc)
                )
                    .targetCircleColor(R.color.black_color)
                    .tintTarget(false)
                    .outerCircleColorInt(accentColor())
                    .icon(ResourcesCompat.getDrawable(resources, R.drawable.ic_shuffle, null))
            )

            IntroPrefs(requireContext()).hasIntroShown = true
        }
    }

    open fun onShuffleClicked() {
    }

    private fun setupToolbar() {
        toolbar.navigationIcon = if (PreferenceUtil.isVoiceSearch) {
            getDrawableCompat(R.drawable.ic_voice)
        } else {
            getDrawableCompat(R.drawable.ic_search)
        }

        toolbar.navigationIcon?.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                requireContext().accentColor(),
                SRC_IN
            )

        toolbar.setNavigationOnClickListener {
            PreferenceUtil.isSearchFromNavigation = true
            findNavController().navigate(
                R.id.action_search,
                null,
                navOptions
            )
        }
        val appName = resources.getString(titleRes)
        binding.appBarLayout.title = appName

        binding.appBarLayout.pinWhenScrolled()
    }

    abstract val titleRes: Int

    private fun setUpRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = this@AbsRecyclerViewFragment.layoutManager
            adapter = this@AbsRecyclerViewFragment.adapter
        }


    }

    protected open fun createFastScroller(recyclerView: RecyclerView): FastScroller {
        return FastScrollerBuilder(recyclerView).useMd2Style().build()
    }

    private fun initAdapter() {
        adapter = createAdapter()
        adapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                checkIsEmpty()
            }
        })
    }

    protected open val emptyMessage: Int
        @StringRes get() = R.string.empty

    private fun getEmojiByUnicode(unicode: Int): String {
        return String(Character.toChars(unicode))
    }

    private fun checkIsEmpty() {
        binding.emptyText.setText(emptyMessage)
        binding.empty.isVisible = adapter!!.itemCount == 0
    }

    private fun checkForMargins() {
        binding.recyclerView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            bottomMargin = if (ApexUtil.isTablet) {
                if (MusicPlayerRemote.playingQueue.isNotEmpty()) {
                    ApexUtil.dpToMargin(55)
                } else {
                    ApexUtil.dpToMargin(0)
                }
            } else {
                if (MusicPlayerRemote.playingQueue.isNotEmpty()) {
                    ApexUtil.dpToMargin(135)
                } else {
                    ApexUtil.dpToMargin(80)
                }
            }
        }
    }

    private fun initLayoutManager() {
        layoutManager = createLayoutManager()
    }

    protected abstract fun createLayoutManager(): LM

    protected abstract fun createAdapter(): A

    protected fun invalidateLayoutManager() {
        initLayoutManager()
        binding.recyclerView.layoutManager = layoutManager
    }

    protected fun invalidateAdapter() {
        initAdapter()
        checkIsEmpty()
        binding.recyclerView.adapter = adapter
    }

    val recyclerView get() = binding.recyclerView

    val container get() = binding.root

    override fun scrollToTop() {
        recyclerView.scrollToPosition(0)
        binding.appBarLayout.setExpanded(true, true)
    }

    override fun onPrepareMenu(menu: Menu) {
        toolbar.overflowIcon?.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                requireContext().accentColor(),
                SRC_IN
            )

        val yourdrawable = menu.findItem(R.id.action_scan_media).icon
        yourdrawable!!.mutate()
        yourdrawable.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            requireContext().accentColor(),
            SRC_IN
        )
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        ToolbarContentTintHelper.handleOnCreateOptionsMenu(
            requireContext(),
            toolbar,
            menu,
            ATHToolbarActivity.getToolbarBackgroundColor(toolbar)
        )

        val yourdrawable = menu.findItem(R.id.action_scan_media).icon
        yourdrawable!!.mutate()
        yourdrawable.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            requireContext().accentColor(),
            SRC_IN
        )
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_import_playlist -> ImportPlaylistDialog().show(
                childFragmentManager,
                "ImportPlaylist"
            )

            R.id.action_add_to_playlist -> CreatePlaylistDialog.create(emptyList()).show(
                childFragmentManager,
                "ShowCreatePlaylistDialog"
            )

            R.id.action_scan_media -> {
                lifecycleScope.launch {
                    val file =
                        File(getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).canonicalPath)
                    ApexStaticUtil.listPaths(
                        file,
                        AUDIO_FILE_FILTER
                    ) { paths -> ApexStaticUtil.scanPaths(requireActivity(), paths) }
                }
            }
            R.id.action_toggle_index -> {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                PreferenceUtil.isIndexVisible = !PreferenceUtil.isIndexVisible
                if (PreferenceUtil.isIndexVisible) {
                    if (ApexUtil.isTablet) {
                        recyclerView.updatePadding(right = ApexUtil.dpToPixel(60f, requireContext()).toInt())
                    }else {
                        recyclerView.updatePadding(right = ApexUtil.dpToPixel(40f, requireContext()).toInt())
                    }

                    recyclerView.setIndexBarVisibility(true)
                }else {
                    recyclerView.updatePadding(right = 0)
                    recyclerView.setIndexBarVisibility(false)
                }
            }
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        checkForMargins()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPause() {
        super.onPause()
        (adapter as? AbsMultiSelectAdapter<*, *>)?.actionMode?.finish()
    }

    override fun onQueueChanged() {
        super.onQueueChanged()
        checkForMargins()
    }
}
