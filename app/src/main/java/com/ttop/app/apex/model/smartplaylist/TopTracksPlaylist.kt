package com.ttop.app.apex.model.smartplaylist

import com.ttop.app.apex.App
import com.ttop.app.apex.R
import com.ttop.app.apex.model.Song
import kotlinx.parcelize.Parcelize

@Parcelize
class TopTracksPlaylist : AbsSmartPlaylist(
    name = App.getContext().getString(R.string.my_top_tracks),
    iconRes = R.drawable.ic_trending_up
) {
    override fun songs(): List<Song> {
        return topPlayedRepository.topTracks()
    }
}