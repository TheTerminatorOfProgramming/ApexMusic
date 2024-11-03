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

package com.ttop.app.apex.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.ttop.app.apex.FAVOURITES
import com.ttop.app.apex.GENRES
import com.ttop.app.apex.PLAYLISTS
import com.ttop.app.apex.R
import com.ttop.app.apex.RECENT_ALBUMS
import com.ttop.app.apex.RECENT_ARTISTS
import com.ttop.app.apex.TOP_ALBUMS
import com.ttop.app.apex.TOP_ARTISTS
import com.ttop.app.apex.db.HistoryEntity
import com.ttop.app.apex.db.PlayCountEntity
import com.ttop.app.apex.db.PlaylistEntity
import com.ttop.app.apex.db.PlaylistWithSongs
import com.ttop.app.apex.db.SongEntity
import com.ttop.app.apex.db.fromHistoryToSongs
import com.ttop.app.apex.db.toSong
import com.ttop.app.apex.model.AbsCustomPlaylist
import com.ttop.app.apex.model.Album
import com.ttop.app.apex.model.Artist
import com.ttop.app.apex.model.Contributor
import com.ttop.app.apex.model.Genre
import com.ttop.app.apex.model.Home
import com.ttop.app.apex.model.Playlist
import com.ttop.app.apex.model.Song
import com.ttop.app.apex.model.smartplaylist.NotPlayedPlaylist
import com.ttop.app.apex.network.LastFMService
import com.ttop.app.apex.network.Result
import com.ttop.app.apex.network.Result.Error
import com.ttop.app.apex.network.Result.Success
import com.ttop.app.apex.network.model.LastFmAlbum
import com.ttop.app.apex.network.model.LastFmArtist
import com.ttop.app.apex.ui.fragments.search.Filter
import com.ttop.app.apex.util.logE

interface Repository {

    fun historySong(): List<HistoryEntity>
    fun favorites(): LiveData<List<SongEntity>>
    fun observableHistorySongs(): LiveData<List<Song>>
    fun albumById(albumId: Long): Album
    fun playlistSongs(playListId: Long): LiveData<List<SongEntity>>
    suspend fun fetchAlbums(): List<Album>
    suspend fun albumByIdAsync(albumId: Long): Album
    suspend fun allSongs(): List<Song>
    fun allSong(): List<Song>
    suspend fun fetchArtists(): List<Artist>
    suspend fun albumArtists(): List<Artist>
    suspend fun fetchLegacyPlaylist(): List<Playlist>
    suspend fun fetchGenres(): List<Genre>
    suspend fun search(query: String?, filter: Filter): MutableList<Any>
    suspend fun getPlaylistSongs(playlist: Playlist): List<Song>
    suspend fun getGenre(genreId: Long): List<Song>
    suspend fun artistInfo(name: String, lang: String?, cache: String?): Result<LastFmArtist>
    suspend fun albumInfo(artist: String, album: String): Result<LastFmAlbum>
    suspend fun artistById(artistId: Long): Artist
    suspend fun albumArtistByName(name: String): Artist
    suspend fun recentArtists(): List<Artist>
    suspend fun topArtists(): List<Artist>
    suspend fun topAlbums(): List<Album>
    suspend fun recentAlbums(): List<Album>
    suspend fun recentArtistsHome(): Home
    suspend fun topArtistsHome(): Home
    suspend fun topAlbumsHome(): Home
    suspend fun recentAlbumsHome(): Home
    suspend fun favoritePlaylistHome(): Home
    suspend fun suggestions(): List<Song>
    suspend fun genresHome(): Home
    suspend fun playlists(): Home
    suspend fun homeSections(): List<Home>
    suspend fun playlist(playlistId: Long): Playlist
    suspend fun fetchPlaylistWithSongs(): List<PlaylistWithSongs>
    suspend fun playlistSongs(playlistWithSongs: PlaylistWithSongs): List<Song>
    suspend fun insertSongs(songs: List<SongEntity>)
    suspend fun checkPlaylistExists(playlistName: String): List<PlaylistEntity>
    suspend fun createPlaylist(playlistEntity: PlaylistEntity): Long
    suspend fun fetchPlaylists(): List<PlaylistEntity>
    suspend fun deleteRoomPlaylist(playlists: List<PlaylistEntity>)
    suspend fun renameRoomPlaylist(playlistId: Long, name: String)
    suspend fun deleteSongsInPlaylist(songs: List<SongEntity>)
    suspend fun removeSongFromPlaylist(songEntity: SongEntity)
    suspend fun deletePlaylistSongs(playlists: List<PlaylistEntity>)
    suspend fun favoritePlaylist(): PlaylistEntity
    suspend fun isFavoriteSong(songEntity: SongEntity): List<SongEntity>
    suspend fun addSongToHistory(currentSong: Song)
    suspend fun songPresentInHistory(currentSong: Song): HistoryEntity?
    suspend fun updateHistorySong(currentSong: Song)
    suspend fun favoritePlaylistSongs(): List<SongEntity>
    suspend fun recentSongs(): List<Song>
    suspend fun topPlayedSongs(): List<Song>
    suspend fun insertSongInPlayCount(playCountEntity: PlayCountEntity)
    suspend fun updateSongInPlayCount(playCountEntity: PlayCountEntity)
    suspend fun deleteSongInPlayCount(playCountEntity: PlayCountEntity)
    suspend fun deleteSongInHistory(songId: Long)
    suspend fun clearSongHistory()
    suspend fun checkSongExistInPlayCount(songId: Long): List<PlayCountEntity>
    suspend fun playCountSongs(): List<PlayCountEntity>
    suspend fun deleteSongs(songs: List<Song>)
    suspend fun contributor(): List<Contributor>
    suspend fun searchArtists(query: String): List<Artist>
    suspend fun searchSongs(query: String): List<Song>
    suspend fun searchAlbums(query: String): List<Album>
    suspend fun isSongFavorite(songId: Long): Boolean
    fun getSongByGenre(genreId: Long): Song
    fun checkPlaylistExists(playListId: Long): LiveData<Boolean>
    fun getPlaylist(playlistId: Long): LiveData<PlaylistWithSongs>
}

class RealRepository(
    private val context: Context,
    private val lastFMService: LastFMService,
    private val songRepository: SongRepository,
    private val albumRepository: AlbumRepository,
    private val artistRepository: ArtistRepository,
    private val genreRepository: GenreRepository,
    private val lastAddedRepository: LastAddedRepository,
    private val playlistRepository: PlaylistRepository,
    private val searchRepository: RealSearchRepository,
    private val topPlayedRepository: TopPlayedRepository,
    private val roomRepository: RoomRepository,
    private val localDataRepository: LocalDataRepository,
) : Repository {

    override suspend fun deleteSongs(songs: List<Song>) = roomRepository.deleteSongs(songs)

    override suspend fun contributor(): List<Contributor> = localDataRepository.contributors()

    override suspend fun searchSongs(query: String): List<Song> = songRepository.songs(query)

    override suspend fun searchAlbums(query: String): List<Album> = albumRepository.albums(query)

    override suspend fun isSongFavorite(songId: Long): Boolean =
        roomRepository.isSongFavorite(context, songId)

    override fun getSongByGenre(genreId: Long): Song = genreRepository.song(genreId)

    override suspend fun searchArtists(query: String): List<Artist> =
        artistRepository.artists(query)

    override suspend fun fetchAlbums(): List<Album> = albumRepository.albums()

    override suspend fun albumByIdAsync(albumId: Long): Album = albumRepository.album(albumId)

    override fun albumById(albumId: Long): Album = albumRepository.album(albumId)

    override suspend fun fetchArtists(): List<Artist> = artistRepository.artists()

    override suspend fun albumArtists(): List<Artist> = artistRepository.albumArtists()

    override suspend fun artistById(artistId: Long): Artist = artistRepository.artist(artistId)

    override suspend fun albumArtistByName(name: String): Artist =
        artistRepository.albumArtist(name)

    override suspend fun recentArtists(): List<Artist> = lastAddedRepository.recentArtists()

    override suspend fun recentAlbums(): List<Album> = lastAddedRepository.recentAlbums()

    override suspend fun topArtists(): List<Artist> = topPlayedRepository.topArtists()

    override suspend fun topAlbums(): List<Album> = topPlayedRepository.topAlbums()

    override suspend fun fetchLegacyPlaylist(): List<Playlist> = playlistRepository.playlists()

    override suspend fun fetchGenres(): List<Genre> = genreRepository.genres()

    override suspend fun allSongs(): List<Song> = songRepository.songs()

    override fun allSong(): List<Song> = songRepository.songs()

    override suspend fun search(query: String?, filter: Filter): MutableList<Any> =
        searchRepository.searchAll(context, query, filter)

    override suspend fun getPlaylistSongs(playlist: Playlist): List<Song> =
        if (playlist is AbsCustomPlaylist) {
            playlist.songs()
        } else {
            PlaylistSongsLoader.getPlaylistSongList(context, playlist.id)
        }

    override suspend fun getGenre(genreId: Long): List<Song> = genreRepository.songs(genreId)

    override suspend fun artistInfo(
        name: String,
        lang: String?,
        cache: String?,
    ): Result<LastFmArtist> {
        return try {
            Success(lastFMService.artistInfo(name, lang, cache))
        } catch (e: Exception) {
            logE(e)
            Error(e)
        }
    }

    override suspend fun albumInfo(
        artist: String,
        album: String,
    ): Result<LastFmAlbum> {
        return try {
            val lastFmAlbum = lastFMService.albumInfo(artist, album)
            Success(lastFmAlbum)
        } catch (e: Exception) {
            logE(e)
            Error(e)
        }
    }

    override suspend fun homeSections(): List<Home> {
        val homeSections = mutableListOf<Home>()
        val sections: List<Home> = listOf(
            favoritePlaylistHome(),
            topArtistsHome(),
            topAlbumsHome(),
            recentArtistsHome(),
            recentAlbumsHome()
        )
        for (section in sections) {
            if (section.arrayList.isNotEmpty()) {
                homeSections.add(section)
            }
        }
        return homeSections
    }


    override suspend fun playlist(playlistId: Long) =
        playlistRepository.playlist(playlistId)

    override suspend fun fetchPlaylistWithSongs(): List<PlaylistWithSongs> =
        roomRepository.playlistWithSongs()

    override fun getPlaylist(playlistId: Long): LiveData<PlaylistWithSongs> =
        roomRepository.getPlaylist(playlistId)


    override suspend fun playlistSongs(playlistWithSongs: PlaylistWithSongs): List<Song> =
        playlistWithSongs.songs.map {
            it.toSong()
        }

    override fun playlistSongs(playListId: Long): LiveData<List<SongEntity>> =
        roomRepository.getSongs(playListId)

    override suspend fun insertSongs(songs: List<SongEntity>) =
        roomRepository.insertSongs(songs)

    override suspend fun checkPlaylistExists(playlistName: String): List<PlaylistEntity> =
        roomRepository.checkPlaylistExists(playlistName)

    override fun checkPlaylistExists(playListId: Long): LiveData<Boolean> =
        roomRepository.checkPlaylistExists(playListId)

    override suspend fun createPlaylist(playlistEntity: PlaylistEntity): Long =
        roomRepository.createPlaylist(playlistEntity)

    override suspend fun fetchPlaylists(): List<PlaylistEntity> = roomRepository.playlists()

    override suspend fun deleteRoomPlaylist(playlists: List<PlaylistEntity>) =
        roomRepository.deletePlaylistEntities(playlists)

    override suspend fun renameRoomPlaylist(playlistId: Long, name: String) =
        roomRepository.renamePlaylistEntity(playlistId, name)

    override suspend fun deleteSongsInPlaylist(songs: List<SongEntity>) =
        roomRepository.deleteSongsInPlaylist(songs)

    override suspend fun removeSongFromPlaylist(songEntity: SongEntity) =
        roomRepository.removeSongFromPlaylist(songEntity)

    override suspend fun deletePlaylistSongs(playlists: List<PlaylistEntity>) =
        roomRepository.deletePlaylistSongs(playlists)

    override suspend fun favoritePlaylist(): PlaylistEntity =
        roomRepository.favoritePlaylist(context.getString(R.string.favorites))

    override suspend fun isFavoriteSong(songEntity: SongEntity): List<SongEntity> =
        roomRepository.isFavoriteSong(songEntity)

    override suspend fun addSongToHistory(currentSong: Song) =
        roomRepository.addSongToHistory(currentSong)

    override suspend fun songPresentInHistory(currentSong: Song): HistoryEntity? =
        roomRepository.songPresentInHistory(currentSong)

    override suspend fun updateHistorySong(currentSong: Song) =
        roomRepository.updateHistorySong(currentSong)

    override suspend fun favoritePlaylistSongs(): List<SongEntity> =
        roomRepository.favoritePlaylistSongs(context.getString(R.string.favorites))

    override suspend fun recentSongs(): List<Song> = lastAddedRepository.recentSongs()

    override suspend fun topPlayedSongs(): List<Song> = topPlayedRepository.topTracks()

    override suspend fun insertSongInPlayCount(playCountEntity: PlayCountEntity) =
        roomRepository.insertSongInPlayCount(playCountEntity)

    override suspend fun updateSongInPlayCount(playCountEntity: PlayCountEntity) =
        roomRepository.updateSongInPlayCount(playCountEntity)

    override suspend fun deleteSongInPlayCount(playCountEntity: PlayCountEntity) =
        roomRepository.deleteSongInPlayCount(playCountEntity)

    override suspend fun deleteSongInHistory(songId: Long) =
        roomRepository.deleteSongInHistory(songId)

    override suspend fun clearSongHistory() {
        roomRepository.clearSongHistory()
    }

    override suspend fun checkSongExistInPlayCount(songId: Long): List<PlayCountEntity> =
        roomRepository.checkSongExistInPlayCount(songId)

    override suspend fun playCountSongs(): List<PlayCountEntity> =
        roomRepository.playCountSongs()

    override fun observableHistorySongs(): LiveData<List<Song>> =
        roomRepository.observableHistorySongs().map {
            it.fromHistoryToSongs()
        }

    override fun historySong(): List<HistoryEntity> =
        roomRepository.historySongs()

    override fun favorites(): LiveData<List<SongEntity>> =
        roomRepository.favoritePlaylistLiveData(context.getString(R.string.favorites))

    override suspend fun suggestions(): List<Song> {
        return NotPlayedPlaylist().songs().shuffled().takeIf {
            it.size > 9
        } ?: emptyList()
    }

    override suspend fun genresHome(): Home {
        val genres = genreRepository.genres().shuffled()
        return Home(genres, GENRES, R.string.genres)
    }

    override suspend fun playlists(): Home {
        val playlist = playlistRepository.playlists()
        return Home(playlist, PLAYLISTS, R.string.playlists)
    }

    override suspend fun recentArtistsHome(): Home {
        val artists = lastAddedRepository.recentArtists().take(5)
        return Home(artists, RECENT_ARTISTS, R.string.recent_artists)
    }

    override suspend fun recentAlbumsHome(): Home {
        val albums = lastAddedRepository.recentAlbums().take(5)
        return Home(albums, RECENT_ALBUMS, R.string.recent_albums)
    }

    override suspend fun topAlbumsHome(): Home {
        val albums = topPlayedRepository.topAlbums().take(5)
        return Home(albums, TOP_ALBUMS, R.string.top_albums)
    }

    override suspend fun topArtistsHome(): Home {
        val artists = topPlayedRepository.topArtists().take(5)
        return Home(artists, TOP_ARTISTS, R.string.top_artists)
    }

    override suspend fun favoritePlaylistHome(): Home {
        val songs = favoritePlaylistSongs().map {
            it.toSong()
        }
        return Home(songs, FAVOURITES, R.string.favorites)
    }
}