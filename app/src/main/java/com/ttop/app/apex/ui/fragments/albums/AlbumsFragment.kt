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
package com.ttop.app.apex.ui.fragments.albums

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.updatePadding
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.ttop.app.apex.EXTRA_ALBUM_ID
import com.ttop.app.apex.R
import com.ttop.app.apex.adapter.album.AlbumAdapter
import com.ttop.app.apex.extensions.darkAccentColor
import com.ttop.app.apex.extensions.setUpMediaRouteButton
import com.ttop.app.apex.extensions.surfaceColor
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.helper.SortOrder.AlbumSortOrder
import com.ttop.app.apex.interfaces.IAlbumClickListener
import com.ttop.app.apex.libraries.appthemehelper.util.VersionUtils
import com.ttop.app.apex.service.MusicService
import com.ttop.app.apex.ui.fragments.GridStyle
import com.ttop.app.apex.ui.fragments.ReloadType
import com.ttop.app.apex.ui.fragments.base.AbsRecyclerViewCustomGridSizeFragment
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.PreferenceUtil

class AlbumsFragment : AbsRecyclerViewCustomGridSizeFragment<AlbumAdapter, GridLayoutManager>(),
    IAlbumClickListener {

    private var layout: MenuItem? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        libraryViewModel.getAlbums().observe(viewLifecycleOwner) {
            if (it.isNotEmpty())
                adapter?.swapDataSet(it)
            else
                adapter?.swapDataSet(listOf())
        }

        if (!VersionUtils.hasVanillaIceCream()) {
            if (PreferenceUtil.appbarColor) {
                activity?.window?.statusBarColor = surfaceColor()
            } else {
                activity?.window?.statusBarColor = requireActivity().darkAccentColor(requireActivity())
            }
        } else {
            activity?.window?.statusBarColor = surfaceColor()
        }

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

    override val titleRes: Int
        get() = R.string.albums

    override val emptyMessage: Int
        get() = R.string.no_albums

    override val isShuffleVisible: Boolean
        get() = true

    override fun onShuffleClicked() {
        libraryViewModel.getAlbums().value?.let {
            MusicPlayerRemote.setShuffleMode(MusicService.SHUFFLE_MODE_NONE)
            MusicPlayerRemote.openQueue(
                queue = it.shuffled().flatMap { album -> album.songs },
                startPosition = 0,
                startPlaying = true
            )
        }
    }

    override fun createLayoutManager(): GridLayoutManager {
        return GridLayoutManager(requireActivity(), getGridSize())
    }

    override fun createAdapter(): AlbumAdapter {
        val dataSet = if (adapter == null) ArrayList() else adapter!!.dataSet
        return AlbumAdapter(
            requireActivity(),
            dataSet,
            itemLayoutResAlbum(),
            this
        )
    }

    override fun setGridSize(gridSize: Int) {
        layoutManager?.spanCount = gridSize
        adapter?.notifyDataSetChanged()
    }

    override fun loadSortOrder(): String {
        return PreferenceUtil.albumSortOrder
    }

    override fun saveSortOrder(sortOrder: String) {
        PreferenceUtil.albumSortOrder = sortOrder
    }

    override fun loadGridSize(): Int {
        return if (PreferenceUtil.isPerformanceMode) {
            1
        }else {
            PreferenceUtil.albumGridSize
        }
    }

    override fun saveGridSize(gridColumns: Int) {
        PreferenceUtil.albumGridSize = gridColumns
    }

    override fun loadGridSizeLand(): Int {
        return if (PreferenceUtil.isPerformanceMode) {
            1
        }else {
            PreferenceUtil.albumGridSizeLand
        }
    }

    override fun saveGridSizeLand(gridColumns: Int) {
        PreferenceUtil.albumGridSizeLand = gridColumns
    }

    override fun setSortOrder(sortOrder: String) {
        libraryViewModel.forceReload(ReloadType.Albums)
    }

    override fun loadLayoutRes(): Int {
        return PreferenceUtil.albumGridStyle.layoutResId
    }

    override fun saveLayoutRes(layoutRes: Int) {
        PreferenceUtil.albumGridStyle = GridStyle.values().first { gridStyle ->
            gridStyle.layoutResId == layoutRes
        }
    }

    companion object {
        fun newInstance(): AlbumsFragment {
            return AlbumsFragment()
        }
    }

    override fun onAlbumClick(albumId: Long, view: View) {
        findNavController().navigate(
            R.id.albumDetailsFragment,
            bundleOf(EXTRA_ALBUM_ID to albumId),
            null,
            FragmentNavigatorExtras(
                view to albumId.toString()
            )
        )
        reenterTransition = null
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateMenu(menu, inflater)
        val gridSizeItem: MenuItem = menu.findItem(R.id.action_grid_size)
        if (ApexUtil.isLandscape) {
            gridSizeItem.setTitle(R.string.action_grid_size_land)
        }

        val layoutItem = menu.findItem(R.id.action_layout_type)
        setUpGridSizeMenu(gridSizeItem.subMenu!!)
        if (PreferenceUtil.isPerformanceMode) {
            gridSizeItem.isVisible = false
            layoutItem?.isVisible = false
        }else {
            gridSizeItem.isVisible = true
            setupLayoutMenu(layoutItem.subMenu!!)

            if (ApexUtil.isTablet) {
                layoutItem?.isVisible = getGridSize() >= 3
            } else {
                layoutItem?.isVisible = getGridSize() != 1
            }

            layout = layoutItem
        }

        setUpSortOrderMenu(menu.findItem(R.id.action_sort_order).subMenu!!)
        //Setting up cast button
        requireContext().setUpMediaRouteButton(menu)
    }

    private fun setUpSortOrderMenu(
        sortOrderMenu: SubMenu
    ) {
        val currentSortOrder: String? = getSortOrder()
        sortOrderMenu.clear()

        sortOrderMenu.add(
            0,
            R.id.action_album_sort_order_desc,
            0,
            R.string.sort_order_z_a
        ).isChecked =
            currentSortOrder.equals(AlbumSortOrder.ALBUM_A_Z)
        sortOrderMenu.add(
            1,
            R.id.action_album_sort_order_asc,
            0,
            R.string.sort_order_a_z
        ).isChecked =
            currentSortOrder.equals(AlbumSortOrder.ALBUM_Z_A)
        sortOrderMenu.add(
            0,
            R.id.action_album_sort_order_artist,
            2,
            R.string.sort_order_album_artist
        ).isChecked =
            currentSortOrder.equals(AlbumSortOrder.ALBUM_ARTIST)
        sortOrderMenu.add(
            0,
            R.id.action_album_sort_order_year,
            3,
            R.string.year
        ).isChecked =
            currentSortOrder.equals(AlbumSortOrder.ALBUM_YEAR)
        sortOrderMenu.add(
            0,
            R.id.action_album_sort_order_num_songs,
            4,
            R.string.sort_order_num_songs
        ).isChecked =
            currentSortOrder.equals(AlbumSortOrder.ALBUM_NUMBER_OF_SONGS)

        sortOrderMenu.setGroupCheckable(0, true, true)
    }

    private fun setupLayoutMenu(
        subMenu: SubMenu
    ) {
        when (itemLayoutRes()) {
            R.layout.item_card -> subMenu.findItem(R.id.action_layout_card).isChecked = true
            R.layout.item_card_color ->
                subMenu.findItem(R.id.action_layout_colored_card).isChecked = true

            R.layout.item_grid_circle ->
                subMenu.findItem(R.id.action_layout_circular).isChecked = true

            R.layout.image -> subMenu.findItem(R.id.action_layout_image).isChecked = true
            R.layout.item_image_gradient ->
                subMenu.findItem(R.id.action_layout_gradient_image).isChecked = true
        }

        /*if (getGridSize() < 2){
            subMenu.findItem(R.id.action_layout_circular).isChecked = true
        }*/
    }

    private fun setUpGridSizeMenu(
        gridSizeMenu: SubMenu
    ) {
        when (getGridSize()) {
            1 -> gridSizeMenu.findItem(R.id.action_grid_size_1).isChecked = true
            2 -> gridSizeMenu.findItem(R.id.action_grid_size_2).isChecked = true
            3 -> gridSizeMenu.findItem(R.id.action_grid_size_3).isChecked = true
            4 -> gridSizeMenu.findItem(R.id.action_grid_size_4).isChecked = true
            5 -> gridSizeMenu.findItem(R.id.action_grid_size_5).isChecked = true
            6 -> gridSizeMenu.findItem(R.id.action_grid_size_6).isChecked = true
            7 -> gridSizeMenu.findItem(R.id.action_grid_size_7).isChecked = true
            8 -> gridSizeMenu.findItem(R.id.action_grid_size_8).isChecked = true
        }

        val gridSize: Int = maxGridSize
        if (gridSize < 8) {
            gridSizeMenu.findItem(R.id.action_grid_size_8).isVisible = false
        }
        if (gridSize < 7) {
            gridSizeMenu.findItem(R.id.action_grid_size_7).isVisible = false
        }
        if (gridSize < 6) {
            gridSizeMenu.findItem(R.id.action_grid_size_6).isVisible = false
        }
        if (gridSize < 5) {
            gridSizeMenu.findItem(R.id.action_grid_size_5).isVisible = false
        }
        if (gridSize < 4) {
            gridSizeMenu.findItem(R.id.action_grid_size_4).isVisible = false
        }
        if (gridSize < 3) {
            gridSizeMenu.findItem(R.id.action_grid_size_3).isVisible = false
        }
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        if (handleGridSizeMenuItem(item)) {
            return true
        }
        if (handleLayoutResType(item)) {
            return true
        }
        if (handleSortOrderMenuItem(item)) {
            return true
        }
        return super.onMenuItemSelected(item)
    }

    private fun handleSortOrderMenuItem(
        item: MenuItem
    ): Boolean {
        val sortOrder: String = when (item.itemId) {
            R.id.action_album_sort_order_desc -> AlbumSortOrder.ALBUM_A_Z
            R.id.action_album_sort_order_asc -> AlbumSortOrder.ALBUM_Z_A
            R.id.action_album_sort_order_artist -> AlbumSortOrder.ALBUM_ARTIST
            R.id.action_album_sort_order_year -> AlbumSortOrder.ALBUM_YEAR
            R.id.action_album_sort_order_num_songs -> AlbumSortOrder.ALBUM_NUMBER_OF_SONGS
            else -> PreferenceUtil.albumSortOrder
        }
        if (sortOrder != PreferenceUtil.albumSortOrder) {
            item.isChecked = true
            setAndSaveSortOrder(sortOrder)
            return true
        }
        return false
    }

    private fun handleLayoutResType(
        item: MenuItem
    ): Boolean {
        val layoutRes = when (item.itemId) {
            R.id.action_layout_card -> R.layout.item_card
            R.id.action_layout_colored_card -> R.layout.item_card_color
            R.id.action_layout_circular -> R.layout.item_grid_circle
            R.id.action_layout_image -> R.layout.image
            R.id.action_layout_gradient_image -> R.layout.item_image_gradient
            else -> PreferenceUtil.albumGridStyle.layoutResId
        }
        if (layoutRes != PreferenceUtil.albumGridStyle.layoutResId) {
            item.isChecked = true
            setAndSaveLayoutRes(layoutRes)
            return true
        }
        return false
    }

    private fun handleGridSizeMenuItem(
        item: MenuItem
    ): Boolean {
        val gridSize = when (item.itemId) {
            R.id.action_grid_size_1 -> 1
            R.id.action_grid_size_2 -> 2
            R.id.action_grid_size_3 -> 3
            R.id.action_grid_size_4 -> 4
            R.id.action_grid_size_5 -> 5
            R.id.action_grid_size_6 -> 6
            R.id.action_grid_size_7 -> 7
            R.id.action_grid_size_8 -> 8
            else -> 0
        }
        if (gridSize > 0) {
            item.isChecked = true
            setAndSaveGridSize(gridSize)

            if (ApexUtil.isTablet) {
                layout?.isVisible = gridSize >= 3
            } else {
                layout?.isVisible = gridSize != 1
            }
            return true
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        libraryViewModel.forceReload(ReloadType.Albums)
    }

    override fun onPause() {
        super.onPause()
        adapter?.actionMode?.finish()
    }

    override fun loadGridSizeTablet(): Int {
        return if (PreferenceUtil.isPerformanceMode) {
            2
        }else {
            PreferenceUtil.albumGridSizeTablet
        }
    }

    override fun saveGridSizeTablet(gridColumns: Int) {
        PreferenceUtil.albumGridSizeTablet = gridColumns
    }

    override fun loadGridSizeTabletLand(): Int {
        return if (PreferenceUtil.isPerformanceMode) {
            4
        }else {
            PreferenceUtil.albumGridSizeTabletLand
        }
    }

    override fun saveGridSizeTabletLand(gridColumns: Int) {
        PreferenceUtil.albumGridSizeTabletLand = gridColumns
    }
}
