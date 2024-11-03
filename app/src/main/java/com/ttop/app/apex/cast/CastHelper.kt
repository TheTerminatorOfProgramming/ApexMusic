package com.ttop.app.apex.cast

import androidx.core.net.toUri
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaInfo.STREAM_TYPE_BUFFERED
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaMetadata.KEY_ALBUM_TITLE
import com.google.android.gms.cast.MediaMetadata.KEY_ARTIST
import com.google.android.gms.cast.MediaMetadata.KEY_TITLE
import com.google.android.gms.cast.MediaMetadata.KEY_TRACK_NUMBER
import com.google.android.gms.cast.MediaMetadata.MEDIA_TYPE_MUSIC_TRACK
import com.google.android.gms.common.images.WebImage
import com.ttop.app.apex.cast.ApexWebServer.Companion.MIME_TYPE_AUDIO
import com.ttop.app.apex.cast.ApexWebServer.Companion.PART_COVER_ART
import com.ttop.app.apex.cast.ApexWebServer.Companion.PART_SONG
import com.ttop.app.apex.model.Song
import com.ttop.app.apex.util.ApexUtil
import java.net.MalformedURLException
import java.net.URL

object CastHelper {

    private const val CAST_MUSIC_METADATA_ID = "metadata_id"
    private const val CAST_MUSIC_METADATA_ALBUM_ID = "metadata_album_id"
    private const val CAST_URL_PROTOCOL = "http"

    fun Song.toMediaInfo(): MediaInfo? {
        val song = this
        val baseUrl: URL
        try {
            baseUrl = URL(CAST_URL_PROTOCOL, ApexUtil.getIpAddress(true), SERVER_PORT, "")
        } catch (e: MalformedURLException) {
            return null
        }

        val songUrl = "$baseUrl/$PART_SONG?id=${song.id}"
        val albumArtUrl = "$baseUrl/$PART_COVER_ART?id=${song.albumId}"
        val musicMetadata = MediaMetadata(MEDIA_TYPE_MUSIC_TRACK).apply {
            putInt(CAST_MUSIC_METADATA_ID, song.id.toInt())
            putInt(CAST_MUSIC_METADATA_ALBUM_ID, song.albumId.toInt())
            putString(KEY_TITLE, song.title)
            putString(KEY_ARTIST, song.artistName)
            putString(KEY_ALBUM_TITLE, song.albumName)
            putInt(KEY_TRACK_NUMBER, song.trackNumber)
            addImage(WebImage(albumArtUrl.toUri()))
        }
        return MediaInfo.Builder(songUrl).apply {
            setStreamType(STREAM_TYPE_BUFFERED)
            setContentType(MIME_TYPE_AUDIO)
            setMetadata(musicMetadata)
            setStreamDuration(song.duration)
        }.build()
    }
}