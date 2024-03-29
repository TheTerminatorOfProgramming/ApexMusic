package com.ttop.app.apex.util

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.core.content.contentValuesOf
import androidx.core.net.toUri
import com.ttop.app.apex.BuildConfig
import com.ttop.app.apex.Constants
import com.ttop.app.apex.R
import com.ttop.app.apex.db.PlaylistEntity
import com.ttop.app.apex.db.toSongEntity
import com.ttop.app.apex.model.Artist
import com.ttop.app.apex.model.Song
import com.ttop.app.apex.model.lyrics.AbsSynchronizedLyrics
import com.ttop.app.apex.repository.Repository
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern


object MusicUtil : KoinComponent {
    fun createShareSongFileIntent(context: Context, song: Song): Intent {
        return Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_STREAM, try {
                    FileProvider.getUriForFile(
                        context,
                        BuildConfig.APPLICATION_ID + ".provider",
                        File(song.data)
                    )
                } catch (e: IllegalArgumentException) {
                    getSongFileUri(song.id)
                }
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            type = "audio/*"
        }
    }

    fun createShareMultipleSongIntent(context: Context, songs: List<Song>): Intent {
        return Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE
            type = "audio/*"

            val files = ArrayList<Uri>()

            for (song in songs) {
                files.add(
                    try {
                        FileProvider.getUriForFile(
                            context,
                            BuildConfig.APPLICATION_ID + ".provider",
                            File(song.data)
                        )
                    } catch (e: IllegalArgumentException) {
                        getSongFileUri(song.id)
                    }
                )
            }
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, files)
        }
    }

    fun buildInfoString(string1: String?, string2: String?): String {
        if (string1.isNullOrEmpty()) {
            return if (string2.isNullOrEmpty()) "" else string2
        }
        return if (string2.isNullOrEmpty()) if (string1.isNullOrEmpty()) "" else string1 else "$string1  •  $string2"
    }

    fun createAlbumArtFile(context: Context): File {
        return File(
            createAlbumArtDir(context),
            System.currentTimeMillis().toString()
        )
    }

    private fun createAlbumArtDir(context: Context): File {
        val albumArtDir = File(
            context.cacheDir,
            "/albumthumbs/"
        )
        if (!albumArtDir.exists()) {
            albumArtDir.mkdirs()
            try {
                File(albumArtDir, ".nomedia").createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return albumArtDir
    }

    fun deleteAlbumArt(context: Context, albumId: Long) {
        val contentResolver = context.contentResolver
        val localUri = "content://media/external/audio/albumart".toUri()
        contentResolver.delete(ContentUris.withAppendedId(localUri, albumId), null, null)
        contentResolver.notifyChange(localUri, null)
    }

    fun getArtistInfoString(
        context: Context,
        artist: Artist,
    ): String {
        val albumCount = artist.albumCount
        val songCount = artist.songCount
        val albumString =
            if (albumCount == 1) context.resources.getString(R.string.album)
            else context.resources.getString(R.string.albums)
        val songString =
            if (songCount == 1) context.resources.getString(R.string.song)
            else context.resources.getString(R.string.songs)
        return "$albumCount $albumString • $songCount $songString"
    }

    //iTunes uses for example 1002 for track 2 CD1 or 3011 for track 11 CD3.
    //this method converts those values to normal tracknumbers
    fun getFixedTrackNumber(trackNumberToFix: Int): Int {
        return trackNumberToFix % 1000
    }

    fun getLyrics(song: Song): String? {
        var lyrics: String? = R.string.no_lyrics_found.toString()
        val file = File(song.data)
        try {
            lyrics = AudioFileIO.read(file).tagOrCreateDefault.getFirst(FieldKey.LYRICS)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (lyrics == null || lyrics.trim { it <= ' ' }.isEmpty() || AbsSynchronizedLyrics
                .isSynchronized(lyrics)
        ) {
            val dir = file.absoluteFile.parentFile
            if (dir != null && dir.exists() && dir.isDirectory) {
                val format = ".*%s.*\\.(lrc|txt)"
                val filename = Pattern.quote(
                    FileUtil.stripExtension(file.name)
                )
                val songtitle = Pattern.quote(song.title)
                val patterns =
                    ArrayList<Pattern>()
                patterns.add(
                    Pattern.compile(
                        String.format(format, filename),
                        Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE
                    )
                )
                patterns.add(
                    Pattern.compile(
                        String.format(format, songtitle),
                        Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE
                    )
                )
                val files =
                    dir.listFiles { f: File ->
                        for (pattern in patterns) {
                            if (pattern.matcher(f.name).matches()) {
                                return@listFiles true
                            }
                        }
                        false
                    }
                if (files != null && files.isNotEmpty()) {
                    for (f in files) {
                        try {
                            val newLyrics =
                                FileUtil.read(f)
                            if (newLyrics != null && newLyrics.trim { it <= ' ' }.isNotEmpty()) {
                                if (AbsSynchronizedLyrics.isSynchronized(newLyrics)) {
                                    return newLyrics
                                }
                                lyrics = newLyrics
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
        return lyrics
    }

    @JvmStatic
    fun getMediaStoreAlbumCoverUri(albumId: Long): Uri {
        val sArtworkUri = "content://media/external/audio/albumart".toUri()
        return ContentUris.withAppendedId(sArtworkUri, albumId)
    }


    fun getPlaylistInfoString(
        context: Context,
        songs: List<Song>,
    ): String {
        val duration = getTotalDuration(songs)
        return buildInfoString(
            getSongCountString(context, songs.size),
            getReadableDurationString(duration)
        )
    }

    fun getReadableDurationString(songDurationMillis: Long): String {
        var minutes = songDurationMillis / 1000 / 60
        val seconds = songDurationMillis / 1000 % 60
        return if (minutes < 60) {
            String.format(
                Locale.getDefault(),
                "%02d:%02d",
                minutes,
                seconds
            )
        } else {
            val hours = minutes / 60
            minutes %= 60
            String.format(
                Locale.getDefault(),
                "%02d:%02d:%02d",
                hours,
                minutes,
                seconds
            )
        }
    }

    fun getSectionName(mediaTitle: String?, stripPrefix: Boolean = false): String {
        var musicMediaTitle = mediaTitle
        return try {
            if (musicMediaTitle.isNullOrEmpty()) {
                return "-"
            }
            musicMediaTitle = musicMediaTitle.trim { it <= ' ' }.lowercase()
            if (stripPrefix) {
                if (musicMediaTitle.startsWith("the ")) {
                    musicMediaTitle = musicMediaTitle.substring(4)
                } else if (musicMediaTitle.startsWith("a ")) {
                    musicMediaTitle = musicMediaTitle.substring(2)
                }
            }

            if (musicMediaTitle.isEmpty()) {
                ""
            } else musicMediaTitle.substring(0, 1).uppercase()
        } catch (e: Exception) {
            ""
        }
    }

    fun getSongCountString(context: Context, songCount: Int): String {
        val songString = if (songCount == 1) context.resources
            .getString(R.string.song) else context.resources.getString(R.string.songs)
        return "$songCount $songString"
    }

    fun getSongFileUri(songId: Long): Uri {
        return ContentUris.withAppendedId(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            songId
        )
    }

    fun getSongFilePath(context: Context, uri: Uri): String {
        val projection = arrayOf(Constants.DATA)
        context.contentResolver.query(uri, projection, null, null, null)?.use {
            if (it.moveToFirst()) {
                return it.getString(0)
            }
        }
        return ""
    }

    fun getTotalDuration(songs: List<Song>): Long {
        var duration: Long = 0
        for (i in songs.indices) {
            duration += songs[i].duration
        }
        return duration
    }

    fun getYearString(year: Int): String {
        return if (year > 0) year.toString() else "-"
    }

    fun indexOfSongInList(songs: List<Song>, songId: Long): Int {
        return songs.indexOfFirst { it.id == songId }
    }

    fun getDateModifiedString(date: Long): String {
        val calendar: Calendar = Calendar.getInstance()
        val pattern = "dd/MM/yyyy hh:mm:ss"
        calendar.timeInMillis = date
        val formatter = SimpleDateFormat(pattern, Locale.ENGLISH)
        return formatter.format(calendar.time)
    }

    fun insertAlbumArt(
        context: Context,
        albumId: Long,
        path: String?,
    ) {
        val contentResolver = context.contentResolver
        val artworkUri = "content://media/external/audio/albumart".toUri()
        contentResolver.delete(ContentUris.withAppendedId(artworkUri, albumId), null, null)
        val values = contentValuesOf(
            "album_id" to albumId,
            "data" to path
        )
        contentResolver.insert(artworkUri, values)
        contentResolver.notifyChange(artworkUri, null)
    }

    fun isArtistNameUnknown(artistName: String?): Boolean {
        if (artistName.isNullOrEmpty()) {
            return false
        }
        if (artistName == Artist.UNKNOWN_ARTIST_DISPLAY_NAME) {
            return true
        }
        val tempName = artistName.trim { it <= ' ' }.lowercase()
        return tempName == "unknown" || tempName == "<unknown>"
    }

    fun isVariousArtists(artistName: String?): Boolean {
        if (artistName.isNullOrEmpty()) {
            return false
        }
        if (artistName == Artist.VARIOUS_ARTISTS_DISPLAY_NAME) {
            return true
        }
        return false
    }

    val repository = get<Repository>()
    suspend fun toggleFavorite(song: Song) {
        withContext(IO) {
            val playlist: PlaylistEntity = repository.favoritePlaylist()
            val songEntity = song.toSongEntity(playlist.playListId)
            val isFavorite = repository.isFavoriteSong(songEntity).isNotEmpty()
            if (isFavorite) {
                repository.removeSongFromPlaylist(songEntity)
            } else {
                repository.insertSongs(listOf(song.toSongEntity(playlist.playListId)))
            }
        }
    }

    suspend fun isFavorite(song: Song) = repository.isSongFavorite(song.id)

    fun songByGenre(genreId: Long): Song {
        return repository.getSongByGenre(genreId)
    }
}