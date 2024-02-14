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
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.ttop.app.apex.EXTRA_PLAYLIST
import com.ttop.app.apex.R
import com.ttop.app.apex.db.PlaylistWithSongs
import com.ttop.app.apex.extensions.*
import com.ttop.app.apex.helper.M3UWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SavePlaylistDialog : DialogFragment() {
    companion object {
        fun create(playlistWithSongs: PlaylistWithSongs): SavePlaylistDialog {
            return SavePlaylistDialog().apply {
                arguments = bundleOf(
                    EXTRA_PLAYLIST to playlistWithSongs
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val playlistWithSongs = extraNotNull<PlaylistWithSongs>(EXTRA_PLAYLIST).value

        createNewFile(
            "audio/mpegurl",
            playlistWithSongs.playlistEntity.playlistName
        ) { outputStream, data ->
            try {
                if (outputStream != null) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        M3UWriter.writeIO(
                            outputStream,
                            playlistWithSongs
                        )
                        withContext(Dispatchers.Main) {
                            showToast(
                                requireContext().getString(R.string.saved_playlist_to,
                                    data?.lastPathSegment),
                                Toast.LENGTH_LONG
                            )
                            dismiss()
                        }
                    }
                }
            } catch (e: Exception) {
                showToast(
                    getString(R.string.something_wrong) + " "  + e.message
                )
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return materialDialog(R.string.save_playlist_title)
            .setView(R.layout.loading)
            .create().colorButtons()
    }
}
