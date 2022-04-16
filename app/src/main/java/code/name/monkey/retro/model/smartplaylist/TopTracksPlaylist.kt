package code.name.monkey.retro.model.smartplaylist

import code.name.monkey.retro.App
import code.name.monkey.retro.R
import code.name.monkey.retro.model.Song
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