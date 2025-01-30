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
package com.ttop.app.apex.adapter.artist

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
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.ttop.app.apex.R
import com.ttop.app.apex.adapter.base.AbsMultiSelectAdapter
import com.ttop.app.apex.adapter.base.MediaEntryViewHolder
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.extensions.hide
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.glide.ApexColoredTarget
import com.ttop.app.apex.glide.ApexGlideExtension
import com.ttop.app.apex.glide.ApexGlideExtension.artistImageOptions
import com.ttop.app.apex.glide.ApexGlideExtension.asBitmapPalette
import com.ttop.app.apex.helper.menu.SongsMenuHelper
import com.ttop.app.apex.interfaces.IAlbumArtistClickListener
import com.ttop.app.apex.interfaces.IArtistClickListener
import com.ttop.app.apex.libraries.alphabetindex.Helpers
import com.ttop.app.apex.libraries.appthemehelper.ThemeStore.Companion.accentColor
import com.ttop.app.apex.libraries.fastscroller.PopupTextProvider
import com.ttop.app.apex.model.Artist
import com.ttop.app.apex.model.Song
import com.ttop.app.apex.ui.activities.MainActivity
import com.ttop.app.apex.ui.fragments.GridStyle
import com.ttop.app.apex.util.MusicUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.color.MediaNotificationProcessor
import com.ttop.app.apex.util.theme.ThemeMode
import java.util.Locale

class ArtistAdapter(
    override val activity: FragmentActivity,
    var dataSet: List<Artist>,
    var itemLayoutRes: Int,
    val iArtistClickListener: IArtistClickListener,
    val iAlbumArtistClickListener: IAlbumArtistClickListener? = null
) : AbsMultiSelectAdapter<ArtistAdapter.ViewHolder, Artist>(
    activity, R.menu.menu_media_selection
), PopupTextProvider, SectionIndexer {

    var albumArtistsOnly = false

    private var mSectionPositions: ArrayList<Int>? = null
    private var sectionsTranslator = HashMap<Int, Int>()

    private val mainActivity get() = activity as MainActivity

    init {
        this.setHasStableIds(true)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun swapDataSet(dataSet: List<Artist>) {
        this.dataSet = dataSet
        notifyDataSetChanged()
        albumArtistsOnly = PreferenceUtil.albumArtistsOnly
    }

    override fun getItemId(position: Int): Long {
        return dataSet[position].id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            try {
                LayoutInflater.from(activity).inflate(itemLayoutRes, parent, false)
            } catch (e: Resources.NotFoundException) {
                LayoutInflater.from(activity).inflate(R.layout.item_grid_circle, parent, false)
            }
        return createViewHolder(view)
    }

    private fun createViewHolder(view: View): ViewHolder {
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val artist = dataSet[position]
        val isChecked = isChecked(artist)
        holder.itemView.isActivated = isChecked
        holder.title?.text = artist.name
        holder.text?.hide()
        val transitionName =
            if (albumArtistsOnly) artist.name else artist.id.toString()

        if (PreferenceUtil.isPerformanceMode) {
            holder.title?.transitionName = transitionName
        } else {
            if (holder.imageContainer != null) {
                holder.imageContainer?.transitionName = transitionName
            } else {
                holder.image?.transitionName = transitionName
            }
        }
        loadArtistImage(artist, holder)

        holder.listCard?.strokeColor = accentColor(activity)
    }

    private fun setColors(processor: MediaNotificationProcessor, holder: ViewHolder) {
        holder.mask?.backgroundTintList = ColorStateList.valueOf(processor.primaryTextColor)
        if (holder.paletteColorContainer != null) {
            holder.paletteColorContainer?.setBackgroundColor(processor.backgroundColor)
            holder.title?.setTextColor(processor.primaryTextColor)
        }else {
            if (PreferenceUtil.artistGridStyle == GridStyle.Image && PreferenceUtil.artistGridSize > 1 && !PreferenceUtil.isPerformanceMode || PreferenceUtil.artistGridStyle == GridStyle.GradientImage && PreferenceUtil.artistGridSize > 1 && !PreferenceUtil.isPerformanceMode) {
                holder.title?.setTextColor(
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
            } else {
                when (PreferenceUtil.getGeneralThemeValue()) {
                    ThemeMode.AUTO -> {
                        when (activity.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                            Configuration.UI_MODE_NIGHT_YES -> {
                                holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                holder.paletteColorContainer?.setBackgroundColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                            }

                            Configuration.UI_MODE_NIGHT_NO,
                            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                                holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.darkColorSurface))
                                holder.paletteColorContainer?.setBackgroundColor(ContextCompat.getColor(activity, R.color.darkColorSurface))
                            }

                            else -> {
                                holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                holder.paletteColorContainer?.setBackgroundColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                            }
                        }
                    }

                    ThemeMode.AUTO_BLACK -> {
                        when (activity.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                            Configuration.UI_MODE_NIGHT_YES -> {
                                holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                holder.paletteColorContainer?.setBackgroundColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                            }

                            Configuration.UI_MODE_NIGHT_NO,
                            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                                holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.blackColorSurface))
                                holder.paletteColorContainer?.setBackgroundColor(ContextCompat.getColor(activity, R.color.blackColorSurface))
                            }

                            else -> {
                                holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                holder.paletteColorContainer?.setBackgroundColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                            }
                        }
                    }

                    ThemeMode.BLACK,
                    ThemeMode.DARK -> {
                        holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                        holder.paletteColorContainer?.setBackgroundColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                    }

                    ThemeMode.LIGHT -> {
                        holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.darkColorSurface))
                        holder.paletteColorContainer?.setBackgroundColor(ContextCompat.getColor(activity, R.color.darkColorSurface))
                    }

                    ThemeMode.MD3 -> {
                        holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.m3_widget_other_text))
                        holder.paletteColorContainer?.setBackgroundColor(ContextCompat.getColor(activity, R.color.m3_widget_other_text))
                    }
                }
            }
        }
        holder.imageContainerCard?.setCardBackgroundColor(processor.backgroundColor)
    }

    private fun loadArtistImage(artist: Artist, holder: ViewHolder) {
        if (holder.image == null) {
            return
        }
        Glide.with(activity)
            .asBitmapPalette()
            .load(ApexGlideExtension.getArtistModel(artist))
            .artistImageOptions(artist)
            .transition(ApexGlideExtension.getDefaultTransition())
            .into(object : ApexColoredTarget(holder.image!!) {
                override fun onColorReady(colors: MediaNotificationProcessor) {
                    setColors(colors, holder)
                }
            })
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun getIdentifier(position: Int): Artist {
        return dataSet[position]
    }

    override fun getName(model: Artist): String {
        return model.name
    }

    override fun onMultipleItemAction(
        menuItem: MenuItem,
        selection: List<Artist>
    ) {
        SongsMenuHelper.handleMenuClick(activity, getSongList(selection), menuItem.itemId)
    }

    private fun getSongList(artists: List<Artist>): List<Song> {
        val songs = ArrayList<Song>()
        for (artist in artists) {
            songs.addAll(artist.songs) // maybe async in future?
        }
        return songs
    }

    override fun getPopupText(view: View, position: Int): String {
        return getSectionName(position)
    }

    private fun getSectionName(position: Int): String {
        return MusicUtil.getSectionName(dataSet[position].name)
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
                val artist = dataSet[layoutPosition]
                if (PreferenceUtil.isPerformanceMode) {
                    title?.let { iArtistClickListener.onArtist(artist.id, it) }
                }else {
                    image?.let {
                        if (albumArtistsOnly && iAlbumArtistClickListener != null) {
                            iAlbumArtistClickListener.onAlbumArtist(artist.name, imageContainer ?: it)
                        } else {
                            iArtistClickListener.onArtist(artist.id, imageContainer ?: it)
                        }
                    }
                }
            }
        }

        override fun onLongClick(v: View?): Boolean {
            return toggleChecked(layoutPosition)
        }
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
                val section = dataSet[i].name[0].toString().uppercase(Locale.getDefault())
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
