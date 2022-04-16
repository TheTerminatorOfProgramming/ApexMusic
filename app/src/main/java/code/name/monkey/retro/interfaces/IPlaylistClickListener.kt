package code.name.monkey.retro.interfaces

import android.view.View
import code.name.monkey.retro.db.PlaylistWithSongs

interface IPlaylistClickListener {
    fun onPlaylistClick(playlistWithSongs: PlaylistWithSongs, view: View)
}