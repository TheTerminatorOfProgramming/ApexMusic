package code.name.monkey.retro.extensions

import code.name.monkey.retro.model.Song
import code.name.monkey.retro.util.MusicUtil

val Song.uri get() = MusicUtil.getSongFileUri(songId = id)