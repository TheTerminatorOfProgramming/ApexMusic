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
package com.ttop.app.apex.helper.menu

import android.view.MenuItem
import androidx.fragment.app.FragmentActivity
import com.ttop.app.apex.R
import com.ttop.app.apex.dialogs.AddToPlaylistDialog
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.model.Genre
import com.ttop.app.apex.model.Song
import com.ttop.app.apex.repository.GenreRepository
import com.ttop.app.apex.repository.RealRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

object GenreMenuHelper : KoinComponent {
    private val genreRepository by inject<GenreRepository>()
    fun handleMenuClick(activity: FragmentActivity, genre: Genre, item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_play -> {
                MusicPlayerRemote.openQueue(getGenreSongs(genre), 0, true)
                return true
            }
            R.id.action_play_next -> {
                MusicPlayerRemote.playNext(getGenreSongs(genre))
                return true
            }
            R.id.action_add_to_playlist -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val playlists = get<RealRepository>().fetchPlaylists()
                    withContext(Dispatchers.Main) {
                        AddToPlaylistDialog.create(playlists, getGenreSongs(genre))
                            .show(activity.supportFragmentManager, "ADD_PLAYLIST")
                    }
                }
                return true
            }
            R.id.action_add_to_current_playing -> {
                MusicPlayerRemote.enqueue(getGenreSongs(genre))
                return true
            }
        }
        return false
    }

    private fun getGenreSongs(genre: Genre): List<Song> {
        return genreRepository.songs(genre.id)
    }
}
