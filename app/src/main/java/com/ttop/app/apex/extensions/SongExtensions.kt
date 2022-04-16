package com.ttop.app.apex.extensions

import com.ttop.app.apex.model.Song
import com.ttop.app.apex.util.MusicUtil

val Song.uri get() = MusicUtil.getSongFileUri(songId = id)