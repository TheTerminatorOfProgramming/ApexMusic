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
package com.ttop.app.apex.adapter.playlist

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SectionIndexer
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isGone
import androidx.core.view.setPadding
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.ttop.app.apex.R
import com.ttop.app.apex.adapter.base.AbsMultiSelectAdapter
import com.ttop.app.apex.adapter.base.MediaEntryViewHolder
import com.ttop.app.apex.db.PlaylistEntity
import com.ttop.app.apex.db.PlaylistWithSongs
import com.ttop.app.apex.db.toSongs
import com.ttop.app.apex.extensions.dipToPix
import com.ttop.app.apex.glide.ApexGlideExtension.playlistOptions
import com.ttop.app.apex.glide.playlistPreview.PlaylistPreview
import com.ttop.app.apex.helper.SortOrder.PlaylistSortOrder
import com.ttop.app.apex.helper.menu.PlaylistMenuHelper
import com.ttop.app.apex.helper.menu.SongsMenuHelper
import com.ttop.app.apex.interfaces.IPlaylistClickListener
import com.ttop.app.apex.libraries.alphabetindex.Helpers
import com.ttop.app.apex.libraries.appthemehelper.ThemeStore.Companion.accentColor
import com.ttop.app.apex.libraries.appthemehelper.util.ATHUtil
import com.ttop.app.apex.libraries.appthemehelper.util.TintHelper
import com.ttop.app.apex.libraries.fastscroller.PopupTextProvider
import com.ttop.app.apex.model.Song
import com.ttop.app.apex.util.MusicUtil
import com.ttop.app.apex.util.PreferenceUtil
import java.util.Locale

class PlaylistAdapter(
    override val activity: FragmentActivity,
    var dataSet: List<PlaylistWithSongs>,
    private var itemLayoutRes: Int,
    private val listener: IPlaylistClickListener
) : AbsMultiSelectAdapter<PlaylistAdapter.ViewHolder, PlaylistWithSongs>(
    activity,
    R.menu.menu_playlists_selection
), PopupTextProvider, SectionIndexer {

    private var mSectionPositions: ArrayList<Int>? = null
    private var sectionsTranslator = HashMap<Int, Int>()

    init {
        setHasStableIds(true)
    }

    fun swapDataSet(dataSet: List<PlaylistWithSongs>) {
        this.dataSet = dataSet
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        return dataSet[position].playlistEntity.playListId
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(activity).inflate(itemLayoutRes, parent, false)
        return createViewHolder(view)
    }

    private fun createViewHolder(view: View): ViewHolder {
        return ViewHolder(view)
    }

    private fun getPlaylistTitle(playlist: PlaylistEntity): String {
        return playlist.playlistName.ifEmpty { "-" }
    }

    private fun getPlaylistText(playlist: PlaylistWithSongs): String {
        return MusicUtil.getPlaylistInfoString(activity, playlist.songs.toSongs())
    }

    override fun getPopupText(view: View, position: Int): String {
        val sectionName: String = when (PreferenceUtil.playlistSortOrder) {
            PlaylistSortOrder.PLAYLIST_A_Z, PlaylistSortOrder.PLAYLIST_Z_A -> dataSet[position].playlistEntity.playlistName
            PlaylistSortOrder.PLAYLIST_SONG_COUNT, PlaylistSortOrder.PLAYLIST_SONG_COUNT_DESC -> dataSet[position].songs.size.toString()
            else -> {
                return ""
            }
        }
        return MusicUtil.getSectionName(sectionName)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val playlist = dataSet[position]
        holder.itemView.isActivated = isChecked(playlist)
        holder.title?.text = getPlaylistTitle(playlist.playlistEntity)
        holder.text?.text = getPlaylistText(playlist)
        holder.menu?.isGone = isChecked(playlist)

        Glide.with(activity)
        Glide.with(activity)
            .load(PlaylistPreview(playlist))
            .playlistOptions()
            .into(holder.image!!)

        holder.listCard?.strokeColor = accentColor(activity)
    }

    private fun getIconRes(): Drawable = TintHelper.createTintedDrawable(
        activity,
        R.drawable.ic_playlist_play,
        ATHUtil.resolveColor(activity, androidx.appcompat.R.attr.colorControlNormal)
    )

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun getIdentifier(position: Int): PlaylistWithSongs {
        return dataSet[position]
    }

    override fun getName(model: PlaylistWithSongs): String {
        return model.playlistEntity.playlistName
    }

    override fun onMultipleItemAction(menuItem: MenuItem, selection: List<PlaylistWithSongs>) {
        when (menuItem.itemId) {
            else -> SongsMenuHelper.handleMenuClick(
                activity,
                getSongList(selection),
                menuItem.itemId
            )
        }
    }

    private fun getSongList(playlists: List<PlaylistWithSongs>): List<Song> {
        val songs = mutableListOf<Song>()
        playlists.forEach {
            songs.addAll(it.songs.toSongs())
        }
        return songs
    }

    inner class ViewHolder(itemView: View) : MediaEntryViewHolder(itemView) {
        init {
            menu?.setOnClickListener { view ->
                val popupMenu = PopupMenu(activity, view)
                popupMenu.inflate(R.menu.menu_item_playlist)
                popupMenu.setOnMenuItemClickListener { item ->
                    PlaylistMenuHelper.handleMenuClick(activity, dataSet[layoutPosition], item)
                }
                popupMenu.show()
            }

            imageTextContainer?.apply {
                cardElevation = 0f
                setCardBackgroundColor(Color.TRANSPARENT)
            }
        }

        override fun onClick(v: View?) {
            if (isInQuickSelectMode) {
                toggleChecked(layoutPosition)
            } else {
                itemView.transitionName = "playlist"
                listener.onPlaylistClick(dataSet[layoutPosition], itemView)
            }
        }

        override fun onLongClick(v: View?): Boolean {
            toggleChecked(layoutPosition)
            return true
        }
    }

    companion object {
        val TAG: String = PlaylistAdapter::class.java.simpleName
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
                val section = dataSet[i].playlistEntity.playlistName[0].toString().uppercase(Locale.getDefault())
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
