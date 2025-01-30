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
package com.ttop.app.apex.adapter.song

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SectionIndexer
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.ttop.app.apex.EXTRA_ALBUM_ID
import com.ttop.app.apex.R
import com.ttop.app.apex.adapter.base.AbsMultiSelectAdapter
import com.ttop.app.apex.adapter.base.MediaEntryViewHolder
import com.ttop.app.apex.extensions.surfaceColor
import com.ttop.app.apex.glide.ApexColoredTarget
import com.ttop.app.apex.glide.ApexGlideExtension
import com.ttop.app.apex.glide.ApexGlideExtension.asBitmapPalette
import com.ttop.app.apex.glide.ApexGlideExtension.songCoverOptions
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.helper.SortOrder
import com.ttop.app.apex.helper.menu.SongMenuHelper
import com.ttop.app.apex.helper.menu.SongsMenuHelper
import com.ttop.app.apex.libraries.alphabetindex.Helpers.Companion.sectionsHelper
import com.ttop.app.apex.libraries.appthemehelper.ThemeStore.Companion.accentColor
import com.ttop.app.apex.libraries.fastscroller.PopupTextProvider
import com.ttop.app.apex.model.Song
import com.ttop.app.apex.ui.activities.MainActivity
import com.ttop.app.apex.ui.fragments.GridStyle
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.ColorUtil
import com.ttop.app.apex.util.MusicUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.color.MediaNotificationProcessor
import com.ttop.app.apex.util.theme.ThemeMode
import java.util.Locale


/**
 * Created by hemanths on 13/08/17.
 */

open class SongAdapter(
    final override val activity: FragmentActivity,
    var dataSet: MutableList<Song>,
    protected var itemLayoutRes: Int,
    showSectionName: Boolean = true
) : AbsMultiSelectAdapter<SongAdapter.ViewHolder, Song>(
    activity,
    R.menu.menu_media_selection
), PopupTextProvider, SectionIndexer {

    private var showSectionName = true
    private var mSectionPositions: ArrayList<Int>? = null
    private var sectionsTranslator = HashMap<Int, Int>()

    private val mainActivity get() = activity as MainActivity

    init {
        this.showSectionName = showSectionName
        this.setHasStableIds(true)
    }

    @SuppressLint("NotifyDataSetChanged")
    open fun swapDataSet(dataSet: List<Song>) {
        this.dataSet = ArrayList(dataSet)
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        return dataSet[position].id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            try {
                LayoutInflater.from(activity).inflate(itemLayoutRes, parent, false)
            } catch (e: Resources.NotFoundException) {
                LayoutInflater.from(activity).inflate(R.layout.item_list, parent, false)
            }
        return createViewHolder(view)
    }

    protected open fun createViewHolder(view: View): ViewHolder {
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = dataSet[position]
        val isChecked = isChecked(song)

        holder.itemView.isActivated = isChecked
        holder.menu?.isGone = isChecked
        holder.title?.text = getSongTitle(song)
        holder.text?.text = getSongText(song)
        holder.text2?.text = getSongText2(song)

        if (!PreferenceUtil.isPerformanceMode) {
            loadAlbumCover(song, holder)
        }

        holder.listCard?.strokeColor = accentColor(activity)
        //holder.listCard?.setCardBackgroundColor(ContextCompat.getColor(mainActivity, android.R.color.transparent))
    }

    private fun setColors(color: MediaNotificationProcessor, holder: ViewHolder) {
        if (holder.paletteColorContainer != null) {
            holder.title?.setTextColor(color.primaryTextColor)
            holder.text?.setTextColor(color.secondaryTextColor)
            holder.text2?.setTextColor(color.secondaryTextColor)
            holder.paletteColorContainer?.setBackgroundColor(color.backgroundColor)
            holder.menu?.imageTintList = ColorStateList.valueOf(color.primaryTextColor)
        }else {
            if (itemLayoutRes == R.layout.item_queue) {
                when (PreferenceUtil.getGeneralThemeValue()) {
                    ThemeMode.AUTO -> {
                        when (activity.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                            Configuration.UI_MODE_NIGHT_YES -> {
                                holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                holder.text2?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                holder.paletteColorContainer?.setBackgroundColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                holder.menu?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.md_white_1000))
                                holder.dragView?.setColorFilter(ContextCompat.getColor(mainActivity, R.color.md_white_1000))
                            }

                            Configuration.UI_MODE_NIGHT_NO,
                            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                                holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.darkColorSurface))
                                holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.darkColorSurface))
                                holder.text2?.setTextColor(ContextCompat.getColor(activity, R.color.darkColorSurface))
                                holder.paletteColorContainer?.setBackgroundColor(ContextCompat.getColor(activity, R.color.darkColorSurface))
                                holder.menu?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.darkColorSurface))
                                holder.dragView?.setColorFilter(ContextCompat.getColor(mainActivity, R.color.darkColorSurface))
                            }

                            else -> {
                                holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                holder.text2?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                holder.paletteColorContainer?.setBackgroundColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                holder.menu?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.md_white_1000))
                                holder.dragView?.setColorFilter(ContextCompat.getColor(mainActivity, R.color.md_white_1000))
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
                                holder.menu?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.md_white_1000))
                                holder.dragView?.setColorFilter(ContextCompat.getColor(mainActivity, R.color.md_white_1000))
                            }

                            Configuration.UI_MODE_NIGHT_NO,
                            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                                holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.blackColorSurface))
                                holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.blackColorSurface))
                                holder.text2?.setTextColor(ContextCompat.getColor(activity, R.color.blackColorSurface))
                                holder.paletteColorContainer?.setBackgroundColor(ContextCompat.getColor(activity, R.color.blackColorSurface))
                                holder.menu?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.blackColorSurface))
                                holder.dragView?.setColorFilter(ContextCompat.getColor(mainActivity, R.color.blackColorSurface))
                            }

                            else -> {
                                holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                holder.text2?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                holder.paletteColorContainer?.setBackgroundColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                holder.menu?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.md_white_1000))
                                holder.dragView?.setColorFilter(ContextCompat.getColor(mainActivity, R.color.md_white_1000))
                            }
                        }
                    }

                    ThemeMode.BLACK,
                    ThemeMode.DARK -> {
                        holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                        holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                        holder.text2?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                        holder.paletteColorContainer?.setBackgroundColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                        holder.menu?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.md_white_1000))
                        holder.dragView?.setColorFilter(ContextCompat.getColor(mainActivity, R.color.md_white_1000))
                    }

                    ThemeMode.LIGHT -> {
                        holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.darkColorSurface))
                        holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.darkColorSurface))
                        holder.text2?.setTextColor(ContextCompat.getColor(activity, R.color.darkColorSurface))
                        holder.paletteColorContainer?.setBackgroundColor(ContextCompat.getColor(activity, R.color.darkColorSurface))
                        holder.menu?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.darkColorSurface))
                        holder.dragView?.setColorFilter(ContextCompat.getColor(mainActivity, R.color.darkColorSurface))
                    }

                    ThemeMode.MD3 -> {
                        holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.m3_widget_other_text))
                        holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.m3_widget_other_text))
                        holder.text2?.setTextColor(ContextCompat.getColor(activity, R.color.m3_widget_other_text))
                        holder.paletteColorContainer?.setBackgroundColor(ContextCompat.getColor(activity, R.color.m3_widget_other_text))
                        holder.menu?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.m3_widget_other_text))
                        holder.dragView?.setColorFilter(ContextCompat.getColor(mainActivity, R.color.m3_widget_other_text))
                    }
                }
            }else {
                if (PreferenceUtil.songGridStyle == GridStyle.Image && PreferenceUtil.songGridSize > 1 && !PreferenceUtil.isPerformanceMode || PreferenceUtil.songGridStyle == GridStyle.GradientImage && PreferenceUtil.songGridSize > 1 && !PreferenceUtil.isPerformanceMode) {
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
                                    holder.menu?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.md_white_1000))
                                    holder.dragView?.setColorFilter(R.color.md_white_1000)
                                }

                                Configuration.UI_MODE_NIGHT_NO,
                                Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                                    holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.darkColorSurface))
                                    holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.darkColorSurface))
                                    holder.text2?.setTextColor(ContextCompat.getColor(activity, R.color.darkColorSurface))
                                    holder.paletteColorContainer?.setBackgroundColor(ContextCompat.getColor(activity, R.color.darkColorSurface))
                                    holder.menu?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.darkColorSurface))
                                    holder.dragView?.setColorFilter(R.color.darkColorSurface)
                                }

                                else -> {
                                    holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                    holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                    holder.text2?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                    holder.paletteColorContainer?.setBackgroundColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                    holder.menu?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.md_white_1000))
                                    holder.dragView?.setColorFilter(R.color.md_white_1000)
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
                                    holder.menu?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.md_white_1000))
                                    holder.dragView?.setColorFilter(R.color.md_white_1000)
                                }

                                Configuration.UI_MODE_NIGHT_NO,
                                Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                                    holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.blackColorSurface))
                                    holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.blackColorSurface))
                                    holder.text2?.setTextColor(ContextCompat.getColor(activity, R.color.blackColorSurface))
                                    holder.paletteColorContainer?.setBackgroundColor(ContextCompat.getColor(activity, R.color.blackColorSurface))
                                    holder.menu?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.blackColorSurface))
                                    holder.dragView?.setColorFilter(R.color.blackColorSurface)
                                }

                                else -> {
                                    holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                    holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                    holder.text2?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                    holder.paletteColorContainer?.setBackgroundColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                    holder.menu?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.md_white_1000))
                                    holder.dragView?.setColorFilter(R.color.md_white_1000)
                                }
                            }
                        }

                        ThemeMode.BLACK,
                        ThemeMode.DARK -> {
                            holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                            holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                            holder.text2?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                            holder.paletteColorContainer?.setBackgroundColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                            holder.menu?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.md_white_1000))
                            holder.dragView?.setColorFilter(R.color.md_white_1000)
                        }

                        ThemeMode.LIGHT -> {
                            holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.darkColorSurface))
                            holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.darkColorSurface))
                            holder.text2?.setTextColor(ContextCompat.getColor(activity, R.color.darkColorSurface))
                            holder.paletteColorContainer?.setBackgroundColor(ContextCompat.getColor(activity, R.color.darkColorSurface))
                            holder.menu?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.darkColorSurface))
                            holder.dragView?.setColorFilter(R.color.darkColorSurface)
                        }

                        ThemeMode.MD3 -> {
                            holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.m3_widget_other_text))
                            holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.m3_widget_other_text))
                            holder.text2?.setTextColor(ContextCompat.getColor(activity, R.color.m3_widget_other_text))
                            holder.paletteColorContainer?.setBackgroundColor(ContextCompat.getColor(activity, R.color.m3_widget_other_text))
                            holder.menu?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.m3_widget_other_text))
                            holder.dragView?.setColorFilter(R.color.m3_widget_other_text)
                        }
                    }
                }
            }

        }
        holder.mask?.backgroundTintList = ColorStateList.valueOf(color.primaryTextColor)
    }

    protected open fun loadAlbumCover(song: Song, holder: ViewHolder) {
        if (holder.image == null) {
            return
        }
        Glide.with(activity).asBitmapPalette().songCoverOptions(song)
            .load(ApexGlideExtension.getSongModel(song))
            .into(object : ApexColoredTarget(holder.image!!) {
                override fun onColorReady(colors: MediaNotificationProcessor) {
                    setColors(colors, holder)
                }
            })
    }

    private fun getSongTitle(song: Song): String {
        return song.title
    }

    private fun getSongText(song: Song): String {
        return song.artistName
    }

    private fun getSongText2(song: Song): String {
        return song.albumName
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun getIdentifier(position: Int): Song? {
        return dataSet[position]
    }

    override fun getName(model: Song): String {
        return model.title
    }

    override fun onMultipleItemAction(menuItem: MenuItem, selection: List<Song>) {
        SongsMenuHelper.handleMenuClick(activity, selection, menuItem.itemId)
    }

    override fun getPopupText(view: View, position: Int): String {
        val sectionName: String? = when (PreferenceUtil.songSortOrder) {
            SortOrder.SongSortOrder.SONG_DEFAULT -> return MusicUtil.getSectionName(dataSet[position].title, true)
            SortOrder.SongSortOrder.SONG_A_Z, SortOrder.SongSortOrder.SONG_Z_A -> dataSet[position].title
            SortOrder.SongSortOrder.SONG_ALBUM -> dataSet[position].albumName
            SortOrder.SongSortOrder.SONG_ARTIST -> dataSet[position].artistName
            SortOrder.SongSortOrder.SONG_YEAR -> return MusicUtil.getYearString(dataSet[position].year)
            SortOrder.SongSortOrder.COMPOSER -> dataSet[position].composer
            SortOrder.SongSortOrder.SONG_ALBUM_ARTIST -> dataSet[position].albumArtist
            else -> {
                return ""
            }
        }
        return MusicUtil.getSectionName(sectionName)
    }

    open inner class ViewHolder(itemView: View) : MediaEntryViewHolder(itemView) {
        protected open var songMenuRes = SongMenuHelper.MENU_RES
        protected open val song: Song
            get() = dataSet[layoutPosition]

        init {
            menu?.setOnClickListener(object : SongMenuHelper.OnClickSongMenu(activity) {
                override val song: Song
                    get() = this@ViewHolder.song

                override val menuRes: Int
                    get() = songMenuRes

                override fun onMenuItemClick(item: MenuItem): Boolean {
                    return onSongMenuItemClick(item) || super.onMenuItemClick(item)
                }
            })
        }

        protected open fun onSongMenuItemClick(item: MenuItem): Boolean {
            if (image != null && image!!.isVisible) {
                when (item.itemId) {
                    R.id.action_go_to_album -> {
                        activity.findNavController(R.id.fragment_container)
                            .navigate(
                                R.id.albumDetailsFragment,
                                bundleOf(EXTRA_ALBUM_ID to song.albumId)
                            )
                        return true
                    }
                }
            }
            return false
        }

        override fun onClick(v: View?) {
            if (isInQuickSelectMode) {
                toggleChecked(layoutPosition)
            } else {
                MusicPlayerRemote.openQueue(dataSet, layoutPosition, true)
                if (PreferenceUtil.isExpandPanel == "default_song" || PreferenceUtil.isExpandPanel == "enhanced_song" ) {
                    mainActivity.expandPanel()
                }
            }
        }

        override fun onLongClick(v: View?): Boolean {
            return toggleChecked(layoutPosition)
        }
    }

    companion object {
        val TAG: String = SongAdapter::class.java.simpleName
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
        sectionsTranslator = sectionsHelper(sections, alphabetFull)
        return alphabetFull.toTypedArray()
    }

    override fun getPositionForSection(sectionIndex: Int): Int {
        return mSectionPositions!![sectionsTranslator[sectionIndex]!!]
    }

    override fun getSectionForPosition(position: Int): Int {
        return 0
    }
}
