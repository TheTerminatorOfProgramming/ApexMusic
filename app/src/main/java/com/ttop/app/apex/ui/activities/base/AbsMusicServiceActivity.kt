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
package com.ttop.app.apex.ui.activities.base

import android.Manifest
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.ttop.app.apex.R
import com.ttop.app.apex.db.toPlayCount
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.interfaces.IMusicServiceEventListener
import com.ttop.app.apex.libraries.appthemehelper.util.VersionUtils
import com.ttop.app.apex.repository.RealRepository
import com.ttop.app.apex.service.MusicService.Companion.FAVORITE_STATE_CHANGED
import com.ttop.app.apex.service.MusicService.Companion.MEDIA_STORE_CHANGED
import com.ttop.app.apex.service.MusicService.Companion.META_CHANGED
import com.ttop.app.apex.service.MusicService.Companion.PLAY_STATE_CHANGED
import com.ttop.app.apex.service.MusicService.Companion.QUEUE_CHANGED
import com.ttop.app.apex.service.MusicService.Companion.REPEAT_MODE_CHANGED
import com.ttop.app.apex.service.MusicService.Companion.SHUFFLE_MODE_CHANGED
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.logD
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.lang.ref.WeakReference

abstract class AbsMusicServiceActivity : AbsBaseActivity(), IMusicServiceEventListener {

    private val mMusicServiceEventListeners = ArrayList<IMusicServiceEventListener>()
    private val repository: RealRepository by inject()
    private var serviceToken: MusicPlayerRemote.ServiceToken? = null
    private var musicStateReceiver: MusicStateReceiver? = null
    private var receiverRegistered: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        serviceToken = MusicPlayerRemote.bindToService(this, object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                this@AbsMusicServiceActivity.onServiceConnected()
            }

            override fun onServiceDisconnected(name: ComponentName) {
                this@AbsMusicServiceActivity.onServiceDisconnected()
            }
        })

        setPermissionDeniedMessage(getString(R.string.permission_external_storage_denied))
    }

    override fun onDestroy() {
        super.onDestroy()
        MusicPlayerRemote.unbindFromService(serviceToken)
        if (receiverRegistered) {
            unregisterReceiver(musicStateReceiver)
            receiverRegistered = false
        }
    }

    fun addMusicServiceEventListener(listenerI: IMusicServiceEventListener?) {
        if (listenerI != null) {
            mMusicServiceEventListeners.add(listenerI)
        }
    }

    fun removeMusicServiceEventListener(listenerI: IMusicServiceEventListener?) {
        if (listenerI != null) {
            mMusicServiceEventListeners.remove(listenerI)
        }
    }

    override fun onServiceConnected() {
        if (!receiverRegistered) {
            musicStateReceiver = MusicStateReceiver(this)

            val filter = IntentFilter()
            filter.addAction(PLAY_STATE_CHANGED)
            filter.addAction(SHUFFLE_MODE_CHANGED)
            filter.addAction(REPEAT_MODE_CHANGED)
            filter.addAction(META_CHANGED)
            filter.addAction(QUEUE_CHANGED)
            filter.addAction(MEDIA_STORE_CHANGED)
            filter.addAction(FAVORITE_STATE_CHANGED)

            ContextCompat.registerReceiver(
                this,
                musicStateReceiver,
                filter,
                ContextCompat.RECEIVER_EXPORTED
            )

            receiverRegistered = true
        }

        for (listener in mMusicServiceEventListeners) {
            listener.onServiceConnected()
        }
    }

    override fun onServiceDisconnected() {
        if (receiverRegistered) {
            unregisterReceiver(musicStateReceiver)
            receiverRegistered = false
        }

        for (listener in mMusicServiceEventListeners) {
            listener.onServiceDisconnected()
        }
    }

    override fun onPlayingMetaChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onPlayingMetaChanged()
        }
        lifecycleScope.launch(Dispatchers.IO) {
            val entity = repository.songPresentInHistory(MusicPlayerRemote.currentSong)
            if (entity != null) {
                repository.updateHistorySong(MusicPlayerRemote.currentSong)
            } else {
                // Check whether pause history option is ON or OFF
                if (!PreferenceUtil.pauseHistory) {
                    repository.addSongToHistory(MusicPlayerRemote.currentSong)
                }
            }
            val songs = repository.checkSongExistInPlayCount(MusicPlayerRemote.currentSong.id)
            if (songs.isNotEmpty()) {
                repository.updateSongInPlayCount(songs.first().apply {
                    playCount += 1
                })
            } else {
                repository.insertSongInPlayCount(MusicPlayerRemote.currentSong.toPlayCount())
            }
        }
    }

    override fun onQueueChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onQueueChanged()
        }
    }

    override fun onPlayStateChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onPlayStateChanged()
        }
    }

    override fun onMediaStoreChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onMediaStoreChanged()
        }
    }

    override fun onRepeatModeChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onRepeatModeChanged()
        }
    }

    override fun onShuffleModeChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onShuffleModeChanged()
        }
    }

    override fun onFavoriteStateChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onFavoriteStateChanged()
        }
    }

    override fun onHasPermissionsChanged(hasPermissions: Boolean) {
        super.onHasPermissionsChanged(hasPermissions)
        val intent = Intent(MEDIA_STORE_CHANGED)
        intent.putExtra(
            "from_permissions_changed",
            true
        ) // just in case we need to know this at some point
        sendBroadcast(intent)
        logD("sendBroadcast $hasPermissions")
    }

    override fun getPermissionsToRequest(): Array<String> {
        return mutableListOf<String>().apply {
            if (VersionUtils.hasT()) {
                add(Manifest.permission.READ_MEDIA_AUDIO)
                add(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }

            add(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        }.toTypedArray()
    }

    private class MusicStateReceiver(activity: AbsMusicServiceActivity) : BroadcastReceiver() {

        private val reference: WeakReference<AbsMusicServiceActivity> = WeakReference(activity)

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            val activity = reference.get()
            if (activity != null && action != null) {
                when (action) {
                    FAVORITE_STATE_CHANGED -> activity.onFavoriteStateChanged()
                    META_CHANGED -> activity.onPlayingMetaChanged()
                    QUEUE_CHANGED -> activity.onQueueChanged()
                    PLAY_STATE_CHANGED -> activity.onPlayStateChanged()
                    REPEAT_MODE_CHANGED -> {
                        activity.onRepeatModeChanged()
                        MusicPlayerRemote.updateNotification()
                    }

                    SHUFFLE_MODE_CHANGED -> activity.onShuffleModeChanged()
                    MEDIA_STORE_CHANGED -> activity.onMediaStoreChanged()
                }
            }
        }
    }

    companion object {
        val TAG: String = AbsMusicServiceActivity::class.java.simpleName
    }
}
