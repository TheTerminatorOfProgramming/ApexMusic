package com.ttop.app.apex.model.smartplaylist

import com.ttop.app.apex.App
import com.ttop.app.apex.R
import com.ttop.app.apex.model.Song
import kotlinx.parcelize.Parcelize

@Parcelize
class ShuffleAllPlaylist : AbsSmartPlaylist(
    name = App.getContext().getString(R.string.action_shuffle_all),
    iconRes = R.drawable.ic_shuffle
) {
    override fun songs(): List<Song> {
        return songRepository.songs()
    }
}