package com.ttop.app.apex.model.smartplaylist

import com.ttop.app.apex.App
import com.ttop.app.apex.R
import com.ttop.app.apex.model.Song
import kotlinx.parcelize.Parcelize

@Parcelize
class LastAddedPlaylist : AbsSmartPlaylist(
    name = App.getContext().getString(R.string.last_added),
    iconRes = R.drawable.ic_library_add
) {
    override fun songs(): List<Song> {
        return lastAddedRepository.recentSongs()
    }
}