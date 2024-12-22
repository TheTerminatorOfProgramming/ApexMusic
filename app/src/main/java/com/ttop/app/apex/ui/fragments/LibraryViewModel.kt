/*
 * Copyright (c) 2020 Hemanth Savarla.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 */
package com.ttop.app.apex.ui.fragments

import android.animation.ValueAnimator
import android.content.Context
import androidx.core.animation.doOnEnd
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.ttop.app.apex.R
import com.ttop.app.apex.RECENT_ALBUMS
import com.ttop.app.apex.RECENT_ARTISTS
import com.ttop.app.apex.TOP_ALBUMS
import com.ttop.app.apex.TOP_ARTISTS
import com.ttop.app.apex.db.HistoryEntity
import com.ttop.app.apex.db.PlaylistEntity
import com.ttop.app.apex.db.PlaylistWithSongs
import com.ttop.app.apex.db.SongEntity
import com.ttop.app.apex.db.toSong
import com.ttop.app.apex.db.toSongEntity
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.interfaces.IMusicServiceEventListener
import com.ttop.app.apex.model.Album
import com.ttop.app.apex.model.Artist
import com.ttop.app.apex.model.Contributor
import com.ttop.app.apex.model.Genre
import com.ttop.app.apex.model.Home
import com.ttop.app.apex.model.Playlist
import com.ttop.app.apex.model.Song
import com.ttop.app.apex.repository.RealRepository
import com.ttop.app.apex.ui.fragments.ReloadType.Albums
import com.ttop.app.apex.ui.fragments.ReloadType.Artists
import com.ttop.app.apex.ui.fragments.ReloadType.Genres
import com.ttop.app.apex.ui.fragments.ReloadType.HomeSections
import com.ttop.app.apex.ui.fragments.ReloadType.Playlists
import com.ttop.app.apex.ui.fragments.ReloadType.Songs
import com.ttop.app.apex.ui.fragments.ReloadType.Suggestions
import com.ttop.app.apex.ui.fragments.search.Filter
import com.ttop.app.apex.util.DensityUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.logD
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class LibraryViewModel(
    private val repository: RealRepository,
) : ViewModel(), IMusicServiceEventListener {

    private val _paletteColor = MutableLiveData<Int>()
    private val home = MutableLiveData<List<Home>>()
    private val suggestions = MutableLiveData<List<Song>>()
    private val albums = MutableLiveData<List<Album>>()
    private val songs = MutableLiveData<List<Song>>()
    private val artists = MutableLiveData<List<Artist>>()
    private val playlists = MutableLiveData<List<PlaylistWithSongs>>()
    private val genres = MutableLiveData<List<Genre>>()
    private val searchResults = MutableLiveData<List<Any>>()
    private val fabMargin = MutableLiveData(0)
    private val songHistory = MutableLiveData<List<Song>>()
    private var previousSongHistory = ArrayList<HistoryEntity>()
    val paletteColor: LiveData<Int> = _paletteColor

    init {
        loadLibraryContent()
    }

    private fun loadLibraryContent() {
        viewModelScope.launch(IO) {
            fetchHomeSections()
            awaitAll(
                async { fetchSuggestions() },
                async { fetchSongs() },
                async { fetchAlbums() },
                async { fetchArtists() },
                async { fetchGenres() },
                async { fetchPlaylists() },
            )
        }
    }

    fun getSearchResult(): LiveData<List<Any>> = searchResults

    fun getSongs(): LiveData<List<Song>> = songs

    fun getAlbums(): LiveData<List<Album>> = albums

    fun getArtists(): LiveData<List<Artist>> = artists

    fun getPlaylists(): LiveData<List<PlaylistWithSongs>> = playlists

    fun getGenre(): LiveData<List<Genre>> = genres

    fun getHome(): LiveData<List<Home>> = home

    fun getSuggestions(): LiveData<List<Song>> = suggestions

    fun getFabMargin(): LiveData<Int> = fabMargin

    private suspend fun fetchSongs() {
        songs.postValue(repository.allSongs())
    }

    private suspend fun fetchAlbums() {
        albums.postValue(repository.fetchAlbums())
    }

    private suspend fun fetchArtists() {
        if (PreferenceUtil.albumArtistsOnly) {
            artists.postValue(repository.albumArtists())
        } else {
            artists.postValue(repository.fetchArtists())
        }
    }

    private suspend fun fetchPlaylists() {
        playlists.postValue(repository.fetchPlaylistWithSongs())
    }

    private suspend fun fetchGenres() {
        genres.postValue(repository.fetchGenres())
    }

    private suspend fun fetchHomeSections() {
        home.postValue(repository.homeSections())
    }

    private suspend fun fetchSuggestions() {
        suggestions.postValue(repository.suggestions())
    }

    fun search(query: String?, filter: Filter) =
        viewModelScope.launch(IO) {
            val result = repository.search(query, filter)
            searchResults.postValue(result)
        }

    fun forceReload(reloadType: ReloadType) = viewModelScope.launch(IO) {
        when (reloadType) {
            Songs -> fetchSongs()
            Albums -> fetchAlbums()
            Artists -> fetchArtists()
            HomeSections -> fetchHomeSections()
            Playlists -> fetchPlaylists()
            Genres -> fetchGenres()
            Suggestions -> fetchSuggestions()
        }
    }

    fun updateColor(newColor: Int) {
        _paletteColor.postValue(newColor)
    }

    override fun onMediaStoreChanged() {
        logD("onMediaStoreChanged")
        loadLibraryContent()
    }

    override fun onServiceConnected() {
        logD("onServiceConnected")
    }

    override fun onServiceDisconnected() {
        logD("onServiceDisconnected")
    }

    override fun onQueueChanged() {
        logD("onQueueChanged")
    }

    override fun onPlayingMetaChanged() {
        logD("onPlayingMetaChanged")
    }

    override fun onPlayStateChanged() {
        logD("onPlayStateChanged")
    }

    override fun onRepeatModeChanged() {
        logD("onRepeatModeChanged")
    }

    override fun onShuffleModeChanged() {
        logD("onShuffleModeChanged")
    }

    override fun onFavoriteStateChanged() {
        logD("onFavoriteStateChanged")
    }

    fun shuffleSongs() = viewModelScope.launch(IO) {
        val songs = repository.allSongs()
        withContext(Main) {
            MusicPlayerRemote.openAndShuffleQueue(songs, true)
        }
    }

    fun renameRoomPlaylist(playListId: Long, name: String) = viewModelScope.launch(IO) {
        repository.renameRoomPlaylist(playListId, name)
    }

    fun deleteSongsInPlaylist(songs: List<SongEntity>) {
        viewModelScope.launch(IO) {
            repository.deleteSongsInPlaylist(songs)
            forceReload(Playlists)
        }
    }

    fun deleteSongsFromPlaylist(playlists: List<PlaylistEntity>) = viewModelScope.launch(IO) {
        repository.deletePlaylistSongs(playlists)
    }

    fun deleteRoomPlaylist(playlists: List<PlaylistEntity>) = viewModelScope.launch(IO) {
        repository.deleteRoomPlaylist(playlists)
    }

    fun albumById(id: Long) = repository.albumById(id)
    suspend fun artistById(id: Long) = repository.artistById(id)
    suspend fun favoritePlaylist() = repository.favoritePlaylist()
    suspend fun isSongFavorite(songId: Long) = repository.isSongFavorite(songId)
    suspend fun insertSongs(songs: List<SongEntity>) = repository.insertSongs(songs)
    suspend fun removeSongFromPlaylist(songEntity: SongEntity) =
        repository.removeSongFromPlaylist(songEntity)

    private suspend fun checkPlaylistExists(playlistName: String): List<PlaylistEntity> =
        repository.checkPlaylistExists(playlistName)

    private suspend fun createPlaylist(playlistEntity: PlaylistEntity): Long =
        repository.createPlaylist(playlistEntity)

    fun importPlaylists() = viewModelScope.launch(IO) {
        val playlists = repository.fetchLegacyPlaylist()
        playlists.forEach { playlist ->
            val playlistEntity = repository.checkPlaylistExists(playlist.name).firstOrNull()
            if (playlistEntity != null) {
                val songEntities = playlist.getSongs().map {
                    it.toSongEntity(playlistEntity.playListId)
                }
                repository.insertSongs(songEntities)
            } else {
                if (playlist != Playlist.empty) {
                    val playListId = createPlaylist(PlaylistEntity(playlistName = playlist.name))
                    val songEntities = playlist.getSongs().map {
                        it.toSongEntity(playListId)
                    }
                    repository.insertSongs(songEntities)
                }
            }
            forceReload(Playlists)
        }
    }

    fun recentSongs(): LiveData<List<Song>> = liveData(IO) {
        emit(repository.recentSongs())
    }

    fun playCountSongs(): LiveData<List<Song>> = liveData(IO) {
        repository.playCountSongs().forEach { song ->
            if (!File(song.data).exists() || song.id == -1L) {
                repository.deleteSongInPlayCount(song)
            }
        }
        emit(repository.playCountSongs().map {
            it.toSong()
        })
    }

    fun artists(type: Int): LiveData<List<Artist>> = liveData(IO) {
        when (type) {
            TOP_ARTISTS -> emit(repository.topArtists())
            RECENT_ARTISTS -> {
                emit(repository.recentArtists())
            }
        }
    }

    fun albums(type: Int): LiveData<List<Album>> = liveData(IO) {
        when (type) {
            TOP_ALBUMS -> emit(repository.topAlbums())
            RECENT_ALBUMS -> {
                emit(repository.recentAlbums())
            }
        }
    }

    fun artist(artistId: Long): LiveData<Artist> = liveData(IO) {
        emit(repository.artistById(artistId))
    }

    fun fetchContributors(): LiveData<List<Contributor>> = liveData(IO) {
        emit(repository.contributor())
    }

    fun observableHistorySongs(): LiveData<List<Song>> {
        viewModelScope.launch(IO) {
            repository.historySong().forEach { song ->
                if (!File(song.data).exists() || song.id == -1L) {
                    repository.deleteSongInHistory(song.id)
                }
            }

            songHistory.postValue(repository.historySong().map {
                it.toSong()
            })
        }
        return songHistory
    }

    fun clearHistory() {
        viewModelScope.launch(IO) {
            previousSongHistory = repository.historySong() as ArrayList<HistoryEntity>

            repository.clearSongHistory()
        }
        songHistory.value = emptyList()
    }


    fun restoreHistory() {
        viewModelScope.launch(IO) {
            if (previousSongHistory.isNotEmpty()) {
                val history = ArrayList<Song>()
                for (song in previousSongHistory) {
                    repository.addSongToHistory(song.toSong())
                    history.add(song.toSong())
                }
                songHistory.postValue(history)
            }
        }
    }

    fun favorites() = repository.favorites()

    fun clearSearchResult() {
        searchResults.value = emptyList()
    }

    fun addToPlaylist(context: Context, playlistName: String, songs: List<Song>) {
        viewModelScope.launch(IO) {
            val playlists = checkPlaylistExists(playlistName)
            if (playlists.isEmpty()) {
                val playlistId: Long =
                    createPlaylist(PlaylistEntity(playlistName = playlistName))
                insertSongs(songs.map { it.toSongEntity(playlistId) })
                withContext(Main) {
                    context.showToast(
                        context.getString(
                            R.string.playlist_created_sucessfully,
                            playlistName
                        )
                    )
                }
            } else {
                val playlist = playlists.firstOrNull()
                if (playlist != null) {
                    insertSongs(songs.map {
                        it.toSongEntity(playListId = playlist.playListId)
                    })
                }
            }
            forceReload(Playlists)
            withContext(Main) {
                context.showToast(
                    context.getString(
                        R.string.added_song_count_to_playlist,
                        songs.size,
                        playlistName
                    )
                )
            }
        }
    }

    fun setFabMargin(context: Context, bottomMargin: Int) {
        val currentValue = DensityUtil.dip2px(context, 16F) +
                bottomMargin
        ValueAnimator.ofInt(fabMargin.value!!, currentValue).apply {
            addUpdateListener {
                fabMargin.postValue(
                    (it.animatedValue as Int)
                )
            }
            doOnEnd {
                fabMargin.postValue(currentValue)
            }
            start()
        }
    }
}

enum class ReloadType {
    Songs,
    Albums,
    Artists,
    HomeSections,
    Playlists,
    Genres,
    Suggestions
}
