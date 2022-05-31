package com.ttop.app.apex.extensions

import com.ttop.app.apex.model.Song
import com.ttop.app.apex.util.MusicUtil
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat.QueueItem

val Song.uri get() = MusicUtil.getSongFileUri(songId = id)


fun ArrayList<Song>.toMediaSessionQueue(): List<QueueItem> {
    return map {
        val mediaDescription = MediaDescriptionCompat.Builder()
            .setMediaId(it.id.toString())
            .setTitle(it.title)
            .setSubtitle(it.artistName)
            .build()
        QueueItem(mediaDescription, it.hashCode().toLong())
    }
}