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
package com.ttop.app.apex.helper

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.ServiceConnection
import android.database.Cursor
import android.net.Uri
import android.os.IBinder
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.ttop.app.apex.R
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.model.Song
import com.ttop.app.apex.repository.SongRepository
import com.ttop.app.apex.service.CastPlayer
import com.ttop.app.apex.service.MusicService
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.getExternalStorageDirectory
import com.ttop.app.apex.util.logE
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.util.Random
import java.util.WeakHashMap
import kotlin.collections.set


object MusicPlayerRemote : KoinComponent {
    val TAG: String = MusicPlayerRemote::class.java.simpleName
    private val mConnectionMap = WeakHashMap<Context, ServiceBinder>()
    var musicService: MusicService? = null

    private val songRepository by inject<SongRepository>()

    @JvmStatic
    val isPlaying: Boolean
        get() = musicService != null && musicService!!.isPlaying

    fun isPlaying(song: Song): Boolean {
        return if (!isPlaying) {
            false
        } else song.id == currentSong.id
    }

    val currentSong: Song
        get() = if (musicService != null) {
            musicService!!.currentSong
        } else Song.emptySong

    val nextSong: Song?
        get() = if (musicService != null) {
            musicService?.nextSong
        } else Song.emptySong

    var position: Int
        get() = if (musicService != null) {
            musicService!!.position
        } else -1
        set(position) {
            if (musicService != null) {
                musicService!!.position = position
            }
        }

    @JvmStatic
    val playingQueue: List<Song>
        get() = if (musicService != null) {
            musicService?.playingQueue as List<Song>
        } else listOf()

    val songProgressMillis: Int
        get() = if (musicService != null) {
            musicService!!.songProgressMillis
        } else -1

    val songDurationMillis: Int
        get() = if (musicService != null) {
            musicService!!.songDurationMillis
        } else -1

    val repeatMode: Int
        get() = if (musicService != null) {
            musicService!!.repeatMode
        } else MusicService.REPEAT_MODE_NONE

    @JvmStatic
    val shuffleMode: Int
        get() = if (musicService != null) {
            musicService!!.shuffleMode
        } else MusicService.SHUFFLE_MODE_NONE

    val audioSessionId: Int
        get() = if (musicService != null) {
            musicService!!.audioSessionId
        } else -1

    fun bindToService(context: Context, callback: ServiceConnection): ServiceToken? {

        val realActivity = (context as Activity).parent ?: context
        val contextWrapper = ContextWrapper(realActivity)
        val intent = Intent(contextWrapper, MusicService::class.java)
        try {
            context.startService(intent)
        } catch (e: Exception) {
            ContextCompat.startForegroundService(context, intent)
        }
        val binder = ServiceBinder(callback)

        if (contextWrapper.bindService(
                Intent().setClass(contextWrapper, MusicService::class.java),
                binder,
                Context.BIND_AUTO_CREATE
            )
        ) {
            mConnectionMap[contextWrapper] = binder
            return ServiceToken(contextWrapper)
        }
        return null
    }

    fun unbindFromService(token: ServiceToken?) {
        if (token == null) {
            return
        }
        val mContextWrapper = token.mWrappedContext
        val mBinder = mConnectionMap.remove(mContextWrapper) ?: return
        mContextWrapper.unbindService(mBinder)
        if (mConnectionMap.isEmpty()) {
            musicService = null
        }
    }

    private fun getFilePathFromUri(context: Context, uri: Uri): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)

        try {
            cursor = context.contentResolver.query(uri, projection, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(columnIndex)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return null
    }

    fun playSongAt(position: Int) {
        musicService?.playSongAt(position)
    }

    fun pauseSong() {
        musicService?.pause()
    }

    /**
     * Async
     */
    fun playNextSong() {
        musicService?.playNextSong(true)
    }

    fun playNextSongAuto(isPlaying: Boolean) {
        musicService?.playNextSongAuto(true, isPlaying)
    }

    /**
     * Async
     */
    fun playPreviousSong() {
        musicService?.playPreviousSong(true)
    }

    fun playPreviousSongAuto(isPlaying: Boolean) {
        musicService?.playPreviousSongAuto(true, isPlaying)
    }

    fun resumePlaying() {
        musicService?.play()
    }

    /**
     * Async
     */
    @JvmStatic
    fun openQueue(queue: List<Song>, startPosition: Int, startPlaying: Boolean) {
        if (!tryToHandleOpenPlayingQueue(
                queue,
                startPosition,
                startPlaying
            ) && musicService != null
        ) {
            musicService?.openQueue(queue, startPosition, startPlaying)
        }
    }

    @JvmStatic
    fun openAndShuffleQueue(queue: List<Song>, startPlaying: Boolean) {
        var startPosition = 0
        if (queue.isNotEmpty()) {
            startPosition = Random().nextInt(queue.size)
        }

        if (!tryToHandleOpenPlayingQueue(
                queue,
                startPosition,
                startPlaying
            ) && musicService != null
        ) {
            musicService?.openQueue(queue, startPosition, startPlaying)
            setShuffleMode(MusicService.SHUFFLE_MODE_SHUFFLE)
        }
    }

    private fun tryToHandleOpenPlayingQueue(
        queue: List<Song>,
        startPosition: Int,
        startPlaying: Boolean,
    ): Boolean {
        if (playingQueue === queue) {
            if (startPlaying) {
                playSongAt(startPosition)
            } else {
                position = startPosition
            }
            return true
        }
        return false
    }

    fun getQueueDurationMillis(position: Int): Long {
        return if (musicService != null) {
            musicService!!.getQueueDurationMillis(position)
        } else -1
    }

    fun seekTo(millis: Int): Int {
        return if (musicService != null) {
            musicService!!.seek(millis)
        } else -1
    }

    fun cycleRepeatMode(): Boolean {
        if (musicService != null) {
            musicService?.cycleRepeatMode()
            return true
        }
        return false
    }

    fun toggleShuffleMode(): Boolean {
        if (musicService != null) {
            musicService?.toggleShuffle()
            return true
        }
        return false
    }

    fun setShuffleMode(shuffleMode: Int): Boolean {
        if (musicService != null) {
            musicService!!.setShuffleMode(shuffleMode)
            return true
        }
        return false
    }

    fun playNext(song: Song): Boolean {
        if (musicService != null) {
            if (playingQueue.isNotEmpty()) {
                musicService?.addSong(position + 1, song)
            } else {
                val queue = ArrayList<Song>()
                queue.add(song)
                openQueue(queue, 0, false)
            }
            musicService?.showToast(R.string.added_title_to_playing_queue)
            return true
        }
        return false
    }

    @SuppressLint("StringFormatInvalid")
    fun playNext(songs: List<Song>): Boolean {
        if (musicService != null) {
            if (playingQueue.isNotEmpty()) {
                musicService?.addSongs(position + 1, songs)
            } else {
                openQueue(songs, 0, false)
            }
            val toast =
                if (songs.size == 1) musicService!!.resources.getString(R.string.added_title_to_playing_queue) else musicService!!.resources.getString(
                    R.string.added_x_titles_to_playing_queue,
                    songs.size
                )
            musicService?.showToast(toast, Toast.LENGTH_SHORT)
            return true
        }
        return false
    }

    fun playNext(songs: List<Song>, message: Boolean): Boolean {
        if (musicService != null) {
            if (playingQueue.isNotEmpty()) {
                musicService?.addSongs(position + 1, songs)
            } else {
                openQueue(songs, 0, false)
            }
            val toast =
                if (songs.size == 1) musicService!!.resources.getString(R.string.added_title_to_playing_queue) else musicService!!.resources.getString(
                    R.string.added_x_titles_to_playing_queue,
                    songs.size
                )
            if (message) {
                Toast.makeText(musicService, toast, Toast.LENGTH_SHORT).show()
            }
            return true
        }
        return false
    }

    fun playNext(song: Song, message: Boolean): Boolean {
        if (musicService != null) {
            if (playingQueue.isNotEmpty()) {
                musicService?.addSong(position + 1, song)
            } else {
                val queue = ArrayList<Song>()
                queue.add(song)
                openQueue(queue, 0, false)
            }
            if (message) {
                Toast.makeText(
                    musicService,
                    musicService!!.resources.getString(R.string.added_title_to_playing_queue),
                    Toast.LENGTH_SHORT
                ).show()
            }
            return true
        }
        return false
    }

    fun enqueue(song: Song): Boolean {
        if (musicService != null) {
            if (playingQueue.isNotEmpty()) {
                musicService?.addSong(song)
            } else {
                val queue = ArrayList<Song>()
                queue.add(song)
                openQueue(queue, 0, false)
            }
            musicService?.showToast(R.string.added_title_to_playing_queue)
            return true
        }
        return false
    }

    fun enqueue(songs: List<Song>): Boolean {
        if (musicService != null) {
            if (playingQueue.isNotEmpty()) {
                musicService?.addSongs(songs)
            } else {
                openQueue(songs, 0, false)
            }
            val toast =
                if (songs.size == 1) musicService!!.resources.getString(R.string.added_title_to_playing_queue) else musicService!!.resources.getString(
                    R.string.added_x_titles_to_playing_queue,
                    songs.size
                )
            musicService?.showToast(toast)
            return true
        }
        return false
    }

    @JvmStatic
    fun removeFromQueue(song: Song): Boolean {
        if (musicService != null) {
            musicService!!.removeSong(song)
            return true
        }
        return false
    }

    @JvmStatic
    fun removeFromQueue(songs: List<Song>): Boolean {
        if (musicService != null) {
            musicService!!.removeSongs(songs)
            return true
        }
        return false
    }

    fun removeFromQueue(position: Int): Boolean {
        if (musicService != null && position >= 0 && position < playingQueue.size) {
            musicService!!.removeSong(position)
            return true
        }
        return false
    }

    fun moveSong(from: Int, to: Int): Boolean {
        if (musicService != null && from >= 0 && to >= 0 && from < playingQueue.size && to < playingQueue.size) {
            musicService!!.moveSong(from, to)
            return true
        }
        return false
    }

    fun clearQueue(): Boolean {
        if (musicService != null) {
            musicService!!.clearQueue()
            return true
        }
        return false
    }

    @JvmStatic
    fun playFromUri(context: Context, uri: Uri) {
        if (musicService != null) {

            var songs: List<Song>? = null
            if (uri.scheme != null && uri.authority != null) {
                if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
                    var songId: String? = null
                    if (uri.authority == "com.android.providers.media.documents") {
                        songId = getSongIdFromMediaProvider(uri)
                    } else if (uri.authority == "media") {
                        songId = uri.lastPathSegment
                    }
                    if (songId != null) {
                        songs = songRepository.songs(songId)
                    }
                }
            }
            if (songs.isNullOrEmpty()) {
                var songFile: File? = null
                if (uri.authority != null && uri.authority == "com.android.externalstorage.documents") {
                    val path = uri.path?.split(":".toRegex(), 2)?.get(1)
                    if (path != null) {
                        songFile = File(getExternalStorageDirectory(), path)
                    }
                }
                if (songFile == null) {
                    val path = getFilePathFromUri(context, uri)
                    if (path != null)
                        songFile = File(path)
                }
                if (songFile == null && uri.path != null) {
                    songFile = File(uri.path!!)
                }
                if (songFile != null) {
                    songs = songRepository.songsByFilePath(songFile.absolutePath, true)
                }
            }
            if (!songs.isNullOrEmpty()) {
                openQueue(songs, 0, true)
            } else {
                try {
                    context.showToast(R.string.unplayable_file)
                    playNextSong()
                } catch (e: Exception) {
                    logE("The file is not listed in the media store")
                }
            }
        }
    }

    fun updateNotification() {
        musicService?.updatePlaybackControls()
    }

    private fun getSongIdFromMediaProvider(uri: Uri): String {
        return DocumentsContract.getDocumentId(uri).split(":".toRegex())
            .dropLastWhile { it.isEmpty() }.toTypedArray()[1]
    }

    fun switchToRemotePlayback(castPlayer: CastPlayer) {
        musicService?.switchToRemotePlayback(castPlayer)
    }

    fun switchToLocalPlayback() {
        musicService?.switchToLocalPlayback()
    }

    class ServiceBinder internal constructor(private val mCallback: ServiceConnection?) :
        ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.service
            mCallback?.onServiceConnected(className, service)
        }

        override fun onServiceDisconnected(className: ComponentName) {
            mCallback?.onServiceDisconnected(className)
            musicService = null
        }
    }

    class ServiceToken internal constructor(internal var mWrappedContext: ContextWrapper)
}
