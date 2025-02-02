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
package com.ttop.app.apex.adapter.album

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SectionIndexer
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.ttop.app.apex.R
import com.ttop.app.apex.adapter.base.AbsMultiSelectAdapter
import com.ttop.app.apex.adapter.base.MediaEntryViewHolder
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.glide.ApexColoredTarget
import com.ttop.app.apex.glide.ApexGlideExtension
import com.ttop.app.apex.glide.ApexGlideExtension.albumCoverOptions
import com.ttop.app.apex.glide.ApexGlideExtension.asBitmapPalette
import com.ttop.app.apex.helper.SortOrder
import com.ttop.app.apex.helper.menu.SongsMenuHelper
import com.ttop.app.apex.interfaces.IAlbumClickListener
import com.ttop.app.apex.libraries.alphabetindex.Helpers
import com.ttop.app.apex.libraries.appthemehelper.ThemeStore.Companion.accentColor
import com.ttop.app.apex.libraries.fastscroller.PopupTextProvider
import com.ttop.app.apex.model.Album
import com.ttop.app.apex.model.Song
import com.ttop.app.apex.ui.activities.MainActivity
import com.ttop.app.apex.ui.fragments.GridStyle
import com.ttop.app.apex.util.MusicUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.color.MediaNotificationProcessor
import com.ttop.app.apex.util.theme.ThemeMode
import java.util.Locale

open class AlbumAdapter(
    override val activity: FragmentActivity,
    var dataSet: List<Album>,
    var itemLayoutRes: Int,
    val listener: IAlbumClickListener?
) : AbsMultiSelectAdapter<AlbumAdapter.ViewHolder, Album>(
    activity,
    R.menu.menu_media_selection
), PopupTextProvider, SectionIndexer {

    private var mSectionPositions: ArrayList<Int>? = null
    private var sectionsTranslator = HashMap<Int, Int>()

    private val mainActivity get() = activity as MainActivity

    init {
        this.setHasStableIds(true)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun swapDataSet(dataSet: List<Album>) {
        this.dataSet = dataSet
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(activity).inflate(itemLayoutRes, parent, false)
        return createViewHolder(view, viewType)
    }

    protected open fun createViewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(view)
    }

    private fun getAlbumTitle(album: Album): String {
        return album.title
    }

    protected open fun getAlbumText(album: Album): String? {
        return album.albumArtist.let {
            if (it.isNullOrEmpty()) {
                album.artistName
            } else {
                it
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val album = dataSet[position]
        val isChecked = isChecked(album)
        holder.itemView.isActivated = isChecked
        holder.title?.text = getAlbumTitle(album)
        holder.text?.text = getAlbumText(album)
        // Check if imageContainer exists so we can have a smooth transition without
        // CardView clipping, if it doesn't exist in current layout set transition name to image instead.
        if (PreferenceUtil.isPerformanceMode) {
            holder.title?.transitionName = album.id.toString()
        }else {
            if (holder.imageContainer != null) {
                holder.imageContainer?.transitionName = album.id.toString()
            } else {
                holder.image?.transitionName = album.id.toString()
            }
        }
        loadAlbumCover(album, holder)

        holder.listCard?.strokeColor = accentColor(activity)
    }

    protected open fun setColors(color: MediaNotificationProcessor, holder: ViewHolder) {
        if (holder.paletteColorContainer != null) {
            holder.title?.setTextColor(color.primaryTextColor)
            holder.text?.setTextColor(color.secondaryTextColor)
            holder.paletteColorContainer?.setBackgroundColor(color.backgroundColor)
        }else {
            if (PreferenceUtil.albumGridStyle == GridStyle.Image && PreferenceUtil.albumGridSize > 1 && !PreferenceUtil.isPerformanceMode || PreferenceUtil.albumGridStyle == GridStyle.GradientImage && PreferenceUtil.albumGridSize > 1 && !PreferenceUtil.isPerformanceMode) {
                holder.title?.setTextColor(
                    ContextCompat.getColor(
                        mainActivity,
                        R.color.md_white_1000
                    )
                )
                holder.text?.setTextColor(
                    ContextCompat.getColor(
                        mainActivity,
                        R.color.md_white_1000
                    )
                )
                holder.text2?.setTextColor(
                    ContextCompat.getColor(
                        mainActivity,
                        R.color.md_white_1000
                    )
                )
                holder.paletteColorContainer?.setBackgroundColor(
                    ContextCompat.getColor(
                        mainActivity,
                        R.color.md_white_1000
                    )
                )
                holder.menu?.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        mainActivity,
                        R.color.md_white_1000
                    )
                )
            } else {
                when (PreferenceUtil.getGeneralThemeValue()) {
                    ThemeMode.AUTO -> {
                        when (activity.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                            Configuration.UI_MODE_NIGHT_YES -> {
                                holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                holder.text2?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                holder.paletteColorContainer?.setBackgroundColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                holder.menu?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(mainActivity, R.color.md_white_1000))
                            }

                            Configuration.UI_MODE_NIGHT_NO,
                            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                                holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.darkColorSurface))
                                holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.darkColorSurface))
                                holder.text2?.setTextColor(ContextCompat.getColor(activity, R.color.darkColorSurface))
                                holder.paletteColorContainer?.setBackgroundColor(ContextCompat.getColor(activity, R.color.darkColorSurface))
                                holder.menu?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(mainActivity, R.color.darkColorSurface))
                            }

                            else -> {
                                holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                holder.text2?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                holder.paletteColorContainer?.setBackgroundColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                holder.menu?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(mainActivity, R.color.md_white_1000))
                            }
                        }
                    }

                    ThemeMode.AUTO_BLACK -> {
                        when (activity.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                            Configuration.UI_MODE_NIGHT_YES -> {
                                holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                holder.text2?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                holder.paletteColorContainer?.setBackgroundColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                holder.menu?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(mainActivity, R.color.md_white_1000))
                            }

                            Configuration.UI_MODE_NIGHT_NO,
                            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                                holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.blackColorSurface))
                                holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.blackColorSurface))
                                holder.text2?.setTextColor(ContextCompat.getColor(activity, R.color.blackColorSurface))
                                holder.paletteColorContainer?.setBackgroundColor(ContextCompat.getColor(activity, R.color.blackColorSurface))
                                holder.menu?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(mainActivity, R.color.blackColorSurface))
                            }

                            else -> {
                                holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                holder.text2?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                holder.paletteColorContainer?.setBackgroundColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                holder.menu?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(mainActivity, R.color.md_white_1000))
                            }
                        }
                    }

                    ThemeMode.BLACK,
                    ThemeMode.DARK -> {
                        holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                        holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                        holder.text2?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                        holder.paletteColorContainer?.setBackgroundColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                        holder.menu?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(mainActivity, R.color.md_white_1000))
                    }

                    ThemeMode.LIGHT -> {
                        holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.darkColorSurface))
                        holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.darkColorSurface))
                        holder.text2?.setTextColor(ContextCompat.getColor(activity, R.color.darkColorSurface))
                        holder.paletteColorContainer?.setBackgroundColor(ContextCompat.getColor(activity, R.color.darkColorSurface))
                        holder.menu?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(mainActivity, R.color.darkColorSurface))
                    }

                    ThemeMode.MD3 -> {
                        holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.m3_widget_other_text))
                        holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.m3_widget_other_text))
                        holder.text2?.setTextColor(ContextCompat.getColor(activity, R.color.m3_widget_other_text))
                        holder.paletteColorContainer?.setBackgroundColor(ContextCompat.getColor(activity, R.color.m3_widget_other_text))
                        holder.menu?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.m3_widget_other_text))
                    }
                }
            }
        }
        holder.mask?.backgroundTintList = ColorStateList.valueOf(color.primaryTextColor)
        holder.imageContainerCard?.setCardBackgroundColor(color.backgroundColor)
    }

    protected open fun loadAlbumCover(album: Album, holder: ViewHolder) {
        if (holder.image == null) {
            return
        }
        val song = album.safeGetFirstSong()
        Glide.with(activity).asBitmapPalette().albumCoverOptions(song)
            //.checkIgnoreMediaStore()
            .load(ApexGlideExtension.getSongModel(song))
            .into(object : ApexColoredTarget(holder.image!!) {
                override fun onColorReady(colors: MediaNotificationProcessor) {
                    setColors(colors, holder)
                }
            })
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun getItemId(position: Int): Long {
        return dataSet[position].id
    }

    override fun getIdentifier(position: Int): Album? {
        return dataSet[position]
    }

    override fun getName(model: Album): String {
        return model.title
    }

    override fun onMultipleItemAction(
        menuItem: MenuItem,
        selection: List<Album>
    ) {
        SongsMenuHelper.handleMenuClick(activity, getSongList(selection), menuItem.itemId)
    }

    private fun getSongList(albums: List<Album>): List<Song> {
        val songs = ArrayList<Song>()
        for (album in albums) {
            songs.addAll(album.songs)
        }
        return songs
    }

    override fun getPopupText(view: View, position: Int): String {
        return getSectionName(position)
    }

    private fun getSectionName(position: Int): String {
        var sectionName: String? = null
        when (PreferenceUtil.albumSortOrder) {
            SortOrder.AlbumSortOrder.ALBUM_A_Z, SortOrder.AlbumSortOrder.ALBUM_Z_A -> sectionName =
                dataSet[position].title

            SortOrder.AlbumSortOrder.ALBUM_ARTIST -> sectionName = dataSet[position].albumArtist
            SortOrder.AlbumSortOrder.ALBUM_YEAR -> return MusicUtil.getYearString(
                dataSet[position].year
            )
        }
        return MusicUtil.getSectionName(sectionName)
    }

    inner class ViewHolder(itemView: View) : MediaEntryViewHolder(itemView) {

        init {
            menu?.isVisible = false
        }

        override fun onClick(v: View?) {
            super.onClick(v)
            if (isInQuickSelectMode) {
                toggleChecked(layoutPosition)
            } else {
                if (PreferenceUtil.isPerformanceMode) {
                    title?.let {  listener?.onAlbumClick(dataSet[layoutPosition].id, it) }
                }else {
                    image?.let {
                        listener?.onAlbumClick(dataSet[layoutPosition].id, imageContainer ?: it)
                    }
                }
            }
        }

        override fun onLongClick(v: View?): Boolean {
            return toggleChecked(layoutPosition)
        }
    }

    companion object {
        val TAG: String = AlbumAdapter::class.java.simpleName
    }

    override fun getSections(): Array<Any>? {
        val mSections = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val sections: MutableList<String> = ArrayList(27)
        val alphabetFull = ArrayList<String>()
        mSectionPositions = ArrayList()
        run {
            var i = 0
            val size = dataSet.size
            while (i < size) {
                val section = dataSet[i].title[0].toString().uppercase(Locale.getDefault())
                if (!sections.contains(section)) {
                    sections.add(section)
                    mSectionPositions?.add(i)
                }
                i++
            }
        }
        for (element in mSections) {
            alphabetFull.add(element.toString())
        }
        sectionsTranslator = Helpers.sectionsHelper(sections, alphabetFull)
        return alphabetFull.toTypedArray()
    }

    override fun getPositionForSection(sectionIndex: Int): Int {
        return mSectionPositions!![sectionsTranslator[sectionIndex]!!]
    }

    override fun getSectionForPosition(position: Int): Int {
        return 0
    }
}
