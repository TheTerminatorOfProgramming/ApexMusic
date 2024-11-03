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

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.google.android.material.transition.MaterialFade
import com.ttop.app.apex.R
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.logD

abstract class AbsRecyclerViewCustomGridSizeFragment<A : RecyclerView.Adapter<*>, LM : RecyclerView.LayoutManager> :
    AbsRecyclerViewFragment<A, LM>() {

    private var gridSize: Int = 0
    private var sortOrder: String? = null
    private var currentLayoutRes: Int = 0
    private val isLandscape: Boolean
        get() = ApexUtil.isLandscape

    private val isTablet: Boolean
        get() = ApexUtil.isTablet
    val maxGridSize: Int
        get() = if (isLandscape) {
            resources.getInteger(R.integer.max_columns_land)
        } else {
            resources.getInteger(R.integer.max_columns)
        }

    fun itemLayoutRes(): Int {
        return if (getGridSize() > maxGridSizeForList) {
            loadLayoutRes()
        } else {
            R.layout.item_list
        }
    }

    fun itemLayoutResArtist(): Int {
        return if (getGridSize() > maxGridSizeForList) {
            loadLayoutRes()
        } else {
            R.layout.item_list_artist
        }
    }

    fun itemLayoutResAlbum(): Int {
        return if (getGridSize() > maxGridSizeForList) {
            loadLayoutRes()
        } else {
            R.layout.item_list_album
        }
    }

    fun itemLayoutResPlaylist(): Int {
        return if (getGridSize() > maxGridSizeForList) {
            loadLayoutRes()
        } else {
            R.layout.item_list_playlist
        }
    }

    fun setAndSaveLayoutRes(layoutRes: Int) {
        saveLayoutRes(layoutRes)
        invalidateAdapter()
    }

    private val maxGridSizeForList: Int
        get() = if (isTablet) {
            2
        } else {
            if (isLandscape) {
                resources.getInteger(R.integer.default_list_columns_land)
            } else 1
        }


    fun getGridSize(): Int {
        if (gridSize == 0) {
            gridSize = if (ApexUtil.isTablet) {
                if (isLandscape) {
                    loadGridSizeTabletLand()
                } else {
                    loadGridSizeTablet()
                }
            } else {
                if (isLandscape) {
                    loadGridSizeLand()
                } else {
                    loadGridSize()
                }
            }
        }
        return gridSize
    }

    fun getSortOrder(): String? {
        if (sortOrder == null) {
            sortOrder = loadSortOrder()
        }
        return sortOrder
    }

    fun setAndSaveSortOrder(sortOrder: String) {
        this.sortOrder = sortOrder
        logD(sortOrder)
        saveSortOrder(sortOrder)
        setSortOrder(sortOrder)
    }

    fun setAndSaveGridSize(gridSize: Int) {
        val oldLayoutRes = itemLayoutRes()
        this.gridSize = gridSize

        if (ApexUtil.isTablet) {
            if (isLandscape) {
                saveGridSizeTabletLand(gridSize)
            } else {
                saveGridSizeTablet(gridSize)
            }
        } else {
            if (isLandscape) {
                saveGridSizeLand(gridSize)
            } else {
                saveGridSize(gridSize)
            }
        }
        recyclerView.isVisible = false
        invalidateLayoutManager()
        // only recreate the adapter and layout manager if the layout currentLayoutRes has changed+
        if (oldLayoutRes != itemLayoutRes()) {
            invalidateAdapter()
        } else {
            setGridSize(gridSize)
        }

        val transition = MaterialFade().apply {
            addTarget(recyclerView)
        }
        TransitionManager.beginDelayedTransition(container, transition)
        recyclerView.isVisible = true
    }

    protected abstract fun setGridSize(gridSize: Int)

    protected abstract fun setSortOrder(sortOrder: String)

    protected abstract fun loadSortOrder(): String

    protected abstract fun saveSortOrder(sortOrder: String)

    protected abstract fun loadGridSize(): Int

    protected abstract fun saveGridSize(gridColumns: Int)

    protected abstract fun loadGridSizeLand(): Int

    protected abstract fun saveGridSizeLand(gridColumns: Int)

    protected abstract fun loadGridSizeTablet(): Int

    protected abstract fun saveGridSizeTablet(gridColumns: Int)

    protected abstract fun loadGridSizeTabletLand(): Int

    protected abstract fun saveGridSizeTabletLand(gridColumns: Int)

    protected abstract fun loadLayoutRes(): Int

    protected abstract fun saveLayoutRes(layoutRes: Int)
}
