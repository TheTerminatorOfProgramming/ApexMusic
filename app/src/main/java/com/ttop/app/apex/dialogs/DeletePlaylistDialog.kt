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
package com.ttop.app.apex.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.core.text.parseAsHtml
import androidx.fragment.app.DialogFragment
import com.ttop.app.apex.EXTRA_PLAYLIST
import com.ttop.app.apex.R
import com.ttop.app.apex.db.PlaylistEntity
import com.ttop.app.apex.extensions.centeredColorButtons

import com.ttop.app.apex.extensions.extraNotNull
import com.ttop.app.apex.extensions.materialDialog
import com.ttop.app.apex.ui.fragments.LibraryViewModel
import com.ttop.app.apex.ui.fragments.ReloadType
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class DeletePlaylistDialog : DialogFragment() {

    private val libraryViewModel by activityViewModel<LibraryViewModel>()

    companion object {

        fun create(playlist: PlaylistEntity): DeletePlaylistDialog {
            val list = mutableListOf<PlaylistEntity>()
            list.add(playlist)
            return create(list)
        }

        fun create(playlists: List<PlaylistEntity>): DeletePlaylistDialog {
            return DeletePlaylistDialog().apply {
                arguments = bundleOf(EXTRA_PLAYLIST to playlists)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val playlists = extraNotNull<List<PlaylistEntity>>(EXTRA_PLAYLIST).value
        val title: Int
        val message: CharSequence
        //noinspection ConstantConditions
        if (playlists.size > 1) {
            title = R.string.delete_playlists_title
            message =
                String.format(getString(R.string.delete_x_playlists), playlists.size).parseAsHtml()
        } else {
            title = R.string.delete_playlist_title
            message =
                String.format(getString(R.string.delete_playlist_x), playlists[0].playlistName)
                    .parseAsHtml()
        }

        return materialDialog(title)
            .setTitle(title)
            .setMessage(message)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                libraryViewModel.deleteSongsFromPlaylist(playlists)
                libraryViewModel.deleteRoomPlaylist(playlists)
                libraryViewModel.forceReload(ReloadType.Playlists)
            }
            .create()
            .centeredColorButtons()
    }
}
