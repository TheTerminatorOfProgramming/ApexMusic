package com.ttop.app.apex.interfaces

import android.view.View
import com.ttop.app.apex.db.PlaylistWithSongs

interface IPlaylistClickListener {
    fun onPlaylistClick(playlistWithSongs: PlaylistWithSongs, view: View)
}