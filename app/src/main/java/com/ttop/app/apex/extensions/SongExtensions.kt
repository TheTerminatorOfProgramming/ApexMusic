package com.ttop.app.apex.extensions

import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat.QueueItem
import com.ttop.app.apex.model.Song
import com.ttop.app.apex.util.MusicUtil

val Song.uri get() = MusicUtil.getSongFileUri(songId = id)

val Song.albumArtUri get() = MusicUtil.getMediaStoreAlbumCoverUri(albumId)

fun ArrayList<Song>.toMediaSessionQueue(): List<QueueItem> {
    return map { song ->
        val mediaDescription = MediaDescriptionCompat.Builder()
            .setMediaId(song.id.toString())
            .setTitle(song.title)
            .setSubtitle(song.artistName)
            .setIconUri(song.albumArtUri)
            .build()
        QueueItem(mediaDescription, song.hashCode().toLong())
    }
}
