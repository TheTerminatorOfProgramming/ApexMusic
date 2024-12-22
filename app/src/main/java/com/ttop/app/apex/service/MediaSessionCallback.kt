/*
 * Copyright (c) 2019 Hemanth Savarala.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by
 *  the Free Software Foundation either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package com.ttop.app.apex.service

import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.media.session.MediaSessionCompat
import com.ttop.app.apex.auto.AutoMediaIDHelper
import com.ttop.app.apex.helper.MusicPlayerRemote.cycleRepeatMode
import com.ttop.app.apex.helper.ShuffleHelper.makeShuffleList
import com.ttop.app.apex.model.Album
import com.ttop.app.apex.model.Artist
import com.ttop.app.apex.model.Playlist
import com.ttop.app.apex.model.Song
import com.ttop.app.apex.repository.AlbumRepository
import com.ttop.app.apex.repository.ArtistRepository
import com.ttop.app.apex.repository.GenreRepository
import com.ttop.app.apex.repository.PlaylistRepository
import com.ttop.app.apex.repository.SongRepository
import com.ttop.app.apex.repository.TopPlayedRepository
import com.ttop.app.apex.service.MusicService.Companion.ACTION_QUIT
import com.ttop.app.apex.service.MusicService.Companion.CYCLE_REPEAT
import com.ttop.app.apex.service.MusicService.Companion.QUEUE_CHANGED
import com.ttop.app.apex.service.MusicService.Companion.TOGGLE_FAVORITE
import com.ttop.app.apex.service.MusicService.Companion.TOGGLE_SHUFFLE
import com.ttop.app.apex.service.MusicService.Companion.UPDATE_NOTIFY
import com.ttop.app.apex.util.MusicUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.logD
import com.ttop.app.apex.util.logE
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Created by hemanths on 2019-08-01.
 */

class MediaSessionCallback(
    private val musicService: MusicService,
) : MediaSessionCompat.Callback(), KoinComponent {

    private val songRepository by inject<SongRepository>()
    private val albumRepository by inject<AlbumRepository>()
    private val artistRepository by inject<ArtistRepository>()
    private val genreRepository by inject<GenreRepository>()
    private val playlistRepository by inject<PlaylistRepository>()
    private val topPlayedRepository by inject<TopPlayedRepository>()

    override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
        super.onPlayFromMediaId(mediaId, extras)
        val musicId = AutoMediaIDHelper.extractMusicID(mediaId!!)
        logD("Music Id $musicId")
        val itemId = musicId?.toLong() ?: -1
        val songs: ArrayList<Song> = ArrayList()
        when (val category = AutoMediaIDHelper.extractCategory(mediaId)) {
            AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_SONGS -> {
                val allSongs = songRepository.songs().toMutableList()
                var songIndex = MusicUtil.indexOfSongInList(allSongs, itemId)
                if (songIndex == -1) {
                    songIndex = 0
                }
                musicService.openQueue(allSongs, songIndex, true)
                musicService.play()
            }

            AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM -> {
                val album: Album = albumRepository.album(itemId)
                songs.addAll(album.songs)
                musicService.openQueue(songs, 0, true)
            }

            AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST -> {
                val artist: Artist = artistRepository.artist(itemId)
                songs.addAll(artist.songs)
                musicService.openQueue(songs, 0, true)
            }

            AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM_ARTIST -> {
                val artist: Artist =
                    artistRepository.albumArtist(albumRepository.album(itemId).albumArtist!!)
                songs.addAll(artist.songs)
                musicService.openQueue(songs, 0, true)
            }

            AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST -> {
                val playlist: Playlist = playlistRepository.playlist(itemId)
                songs.addAll(playlist.getSongs())
                musicService.openQueue(songs, 0, true)
            }

            AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_GENRE -> {
                songs.addAll(genreRepository.songs(itemId))
                musicService.openQueue(songs, 0, true)
            }

            AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_SHUFFLE -> {
                val allSongs = songRepository.songs().toMutableList()
                makeShuffleList(allSongs, -1)
                musicService.openQueue(allSongs, 0, true)
            }

            AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_HISTORY,
            AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_SUGGESTIONS,
            AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_TOP_TRACKS,
            AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_QUEUE,
            -> {
                val tracks: List<Song> = when (category) {
                    AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_HISTORY -> topPlayedRepository.recentlyPlayedTracks()
                    AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_SUGGESTIONS -> topPlayedRepository.recentlyPlayedTracks()
                    AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_TOP_TRACKS -> topPlayedRepository.recentlyPlayedTracks()
                    else -> musicService.playingQueue
                }
                songs.addAll(tracks)
                var songIndex = MusicUtil.indexOfSongInList(tracks, itemId)
                if (songIndex == -1) {
                    songIndex = 0
                }
                musicService.openQueue(songs, songIndex, true)
            }
        }
        musicService.play()
    }

    override fun onPlayFromSearch(query: String?, extras: Bundle?) {
        val songs = ArrayList<Song>()
        if (query.isNullOrEmpty()) {
            // The user provided generic string e.g. 'Play music'
            // Build appropriate playlist queue
            songs.addAll(songRepository.songs())
        } else {
            // Build a queue based on songs that match "query" or "extras" param
            val mediaFocus: String? = extras?.getString(MediaStore.EXTRA_MEDIA_FOCUS)
            if (mediaFocus == MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE) {
                val artistQuery = extras.getString(MediaStore.EXTRA_MEDIA_ARTIST)
                if (artistQuery != null) {
                    artistRepository.artists(artistQuery).forEach {
                        songs.addAll(it.songs)
                    }
                }
            } else if (mediaFocus == MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE) {
                val albumQuery = extras.getString(MediaStore.EXTRA_MEDIA_ALBUM)
                if (albumQuery != null) {
                    albumRepository.albums(albumQuery).forEach {
                        songs.addAll(it.songs)
                    }
                }
            }
        }

        if (songs.isEmpty()) {
            // No focus found, search by query for song title
            query?.also {
                songs.addAll(songRepository.songs(it))
            }
        }

        musicService.openQueue(songs, 0, true)

        musicService.play()
    }

    override fun onPrepare() {
        super.onPrepare()
        if (musicService.currentSong != Song.emptySong)
            musicService.restoreState(::onPlay)
    }

    override fun onPlay() {
        super.onPlay()
        if (musicService.currentSong != Song.emptySong) musicService.play()
    }

    override fun onPause() {
        super.onPause()
        musicService.pause()
    }

    override fun onSkipToNext() {
        super.onSkipToNext()
        val isPlaying = musicService.isPlaying
        musicService.playNextSongAuto(true, isPlaying)
    }

    override fun onSkipToPrevious() {
        super.onSkipToPrevious()
        val isPlaying = musicService.isPlaying
        musicService.playPreviousSongAuto(true, isPlaying)
    }

    override fun onStop() {
        super.onStop()
        musicService.quit()
    }

    override fun onSeekTo(pos: Long) {
        super.onSeekTo(pos)
        musicService.seek(pos.toInt())
    }

    override fun onCustomAction(action: String, extras: Bundle?) {
        when (action) {
            CYCLE_REPEAT -> {
                cycleRepeatMode()
                musicService.updateMediaSessionPlaybackState()
            }

            TOGGLE_SHUFFLE -> {
                musicService.toggleShuffle()
                musicService.updateMediaSessionPlaybackState()
            }

            TOGGLE_FAVORITE -> {
                musicService.toggleFavorite()
                musicService.updateMediaSessionPlaybackState()
            }

            UPDATE_NOTIFY -> {
                musicService.updatePlaybackControls()
            }

            ACTION_QUIT -> {
                musicService.quit()
            }

            QUEUE_CHANGED -> {
                musicService.clearQueue()
            }

            else -> {
                logE("Unsupported action: $action")
            }
        }
        musicService.updatePlaybackControls()
    }
}