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
package com.ttop.app.apex.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.os.bundleOf
import androidx.fragment.app.findFragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ttop.app.apex.EXTRA_ALBUM_ID
import com.ttop.app.apex.EXTRA_ARTIST_ID
import com.ttop.app.apex.FAVOURITES
import com.ttop.app.apex.R
import com.ttop.app.apex.RECENT_ALBUMS
import com.ttop.app.apex.RECENT_ARTISTS
import com.ttop.app.apex.TOP_ALBUMS
import com.ttop.app.apex.TOP_ARTISTS
import com.ttop.app.apex.adapter.album.AlbumAdapter
import com.ttop.app.apex.adapter.artist.ArtistAdapter
import com.ttop.app.apex.adapter.song.SongAdapter
import com.ttop.app.apex.interfaces.IAlbumClickListener
import com.ttop.app.apex.interfaces.IArtistClickListener
import com.ttop.app.apex.model.Album
import com.ttop.app.apex.model.Artist
import com.ttop.app.apex.model.Home
import com.ttop.app.apex.model.Song
import com.ttop.app.apex.ui.fragments.home.HomeFragment
import com.ttop.app.apex.util.PreferenceUtil

class HomeAdapter(private val activity: AppCompatActivity) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), IArtistClickListener, IAlbumClickListener {

    private var list = listOf<Home>()

    override fun getItemViewType(position: Int): Int {
        return list[position].homeSection
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layout =
            LayoutInflater.from(activity).inflate(R.layout.section_recycler_view, parent, false)
        return when (viewType) {
            RECENT_ARTISTS, TOP_ARTISTS -> ArtistViewHolder(layout)
            FAVOURITES -> PlaylistViewHolder(layout)
            TOP_ALBUMS, RECENT_ALBUMS -> AlbumViewHolder(layout)
            else -> {
                ArtistViewHolder(layout)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val home = list[position]
        when (getItemViewType(position)) {
            RECENT_ALBUMS -> {
                val viewHolder = holder as AlbumViewHolder
                viewHolder.bindView(home)
                viewHolder.clickableArea.setOnClickListener {
                    it.findFragment<HomeFragment>().setSharedAxisXTransitions()
                    activity.findNavController(R.id.fragment_container).navigate(
                        R.id.detailListFragment,
                        bundleOf("type" to RECENT_ALBUMS)
                    )
                }
            }

            TOP_ALBUMS -> {
                val viewHolder = holder as AlbumViewHolder
                viewHolder.bindView(home)
                viewHolder.clickableArea.setOnClickListener {
                    it.findFragment<HomeFragment>().setSharedAxisXTransitions()
                    activity.findNavController(R.id.fragment_container).navigate(
                        R.id.detailListFragment,
                        bundleOf("type" to TOP_ALBUMS)
                    )
                }
            }

            RECENT_ARTISTS -> {
                val viewHolder = holder as ArtistViewHolder
                viewHolder.bindView(home)
                viewHolder.clickableArea.setOnClickListener {
                    it.findFragment<HomeFragment>().setSharedAxisXTransitions()
                    activity.findNavController(R.id.fragment_container).navigate(
                        R.id.detailListFragment,
                        bundleOf("type" to RECENT_ARTISTS)
                    )
                }
            }

            TOP_ARTISTS -> {
                val viewHolder = holder as ArtistViewHolder
                viewHolder.bindView(home)
                viewHolder.clickableArea.setOnClickListener {
                    it.findFragment<HomeFragment>().setSharedAxisXTransitions()
                    activity.findNavController(R.id.fragment_container).navigate(
                        R.id.detailListFragment,
                        bundleOf("type" to TOP_ARTISTS)
                    )
                }
            }

            FAVOURITES -> {
                val viewHolder = holder as PlaylistViewHolder
                viewHolder.bindView(home)
                viewHolder.clickableArea.setOnClickListener {
                    it.findFragment<HomeFragment>().setSharedAxisXTransitions()
                    activity.findNavController(R.id.fragment_container).navigate(
                        R.id.detailListFragment,
                        bundleOf("type" to FAVOURITES)
                    )
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun swapData(sections: List<Home>) {
        list = sections
        notifyDataSetChanged()
    }

    @Suppress("UNCHECKED_CAST")
    private inner class AlbumViewHolder(view: View) : AbsHomeViewItem(view) {
        fun bindView(home: Home) {
            title.setText(home.titleRes)
            recyclerView.apply {
                adapter = albumAdapter(home.arrayList as List<Album>)
                layoutManager = gridLayoutManager()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private inner class ArtistViewHolder(view: View) : AbsHomeViewItem(view) {
        fun bindView(home: Home) {
            title.setText(home.titleRes)
            recyclerView.apply {
                layoutManager = linearLayoutManager()
                adapter = artistsAdapter(home.arrayList as List<Artist>)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private inner class PlaylistViewHolder(view: View) : AbsHomeViewItem(view) {
        fun bindView(home: Home) {
            title.setText(home.titleRes)
            recyclerView.apply {
                val songAdapter = SongAdapter(
                    activity,
                    home.arrayList as MutableList<Song>,
                    R.layout.item_circle
                )
                layoutManager = linearLayoutManager()
                adapter = songAdapter
            }
        }
    }

    open class AbsHomeViewItem(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerView)
        val title: AppCompatTextView = itemView.findViewById(R.id.title)
        val clickableArea: ViewGroup = itemView.findViewById(R.id.clickable_area)
    }

    private fun artistsAdapter(artists: List<Artist>) =
        ArtistAdapter(activity, artists, PreferenceUtil.homeArtistGridStyle, this)

    private fun albumAdapter(albums: List<Album>) =
        AlbumAdapter(activity, albums, PreferenceUtil.homeAlbumGridStyle, this)

    private fun gridLayoutManager() =
        GridLayoutManager(activity, 1, GridLayoutManager.HORIZONTAL, false)

    private fun linearLayoutManager() =
        LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)

    override fun onArtist(artistId: Long, view: View) {
        activity.findNavController(R.id.fragment_container).navigate(
            R.id.artistDetailsFragment,
            bundleOf(EXTRA_ARTIST_ID to artistId),
            null,
            FragmentNavigatorExtras(
                view to artistId.toString()
            )
        )
    }

    override fun onAlbumClick(albumId: Long, view: View) {
        activity.findNavController(R.id.fragment_container).navigate(
            R.id.albumDetailsFragment,
            bundleOf(EXTRA_ALBUM_ID to albumId),
            null,
            FragmentNavigatorExtras(
                view to albumId.toString()
            )
        )
    }
}
