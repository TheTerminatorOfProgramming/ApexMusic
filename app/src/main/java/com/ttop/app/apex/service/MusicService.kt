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

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.database.ContentObserver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.provider.MediaStore
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.Builder
import android.util.Log
import android.widget.Toast
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.media.MediaBrowserServiceCompat
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.color.DynamicColors
import com.ttop.app.apex.AUTO_ACTION_1
import com.ttop.app.apex.AUTO_ACTION_2
import com.ttop.app.apex.BLUETOOTH_PLAYBACK
import com.ttop.app.apex.BuildConfig
import com.ttop.app.apex.CAR_CONNECTED
import com.ttop.app.apex.CROSS_FADE_DURATION
import com.ttop.app.apex.NOTIFICATION_ACTION_1
import com.ttop.app.apex.NOTIFICATION_ACTION_2
import com.ttop.app.apex.PLAYBACK_PITCH
import com.ttop.app.apex.PLAYBACK_SPEED
import com.ttop.app.apex.R
import com.ttop.app.apex.TOGGLE_HEADSET
import com.ttop.app.apex.USE_NOTIFY_ACTIONS_AUTO
import com.ttop.app.apex.appwidgets.AppWidgetBig
import com.ttop.app.apex.appwidgets.AppWidgetCircle
import com.ttop.app.apex.appwidgets.AppWidgetClassic
import com.ttop.app.apex.appwidgets.AppWidgetFull
import com.ttop.app.apex.auto.AutoMediaIDHelper
import com.ttop.app.apex.auto.AutoMusicProvider
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.extensions.uri
import com.ttop.app.apex.glide.ApexGlideExtension.getDefaultTransition
import com.ttop.app.apex.glide.ApexGlideExtension.getSongModel
import com.ttop.app.apex.glide.ApexGlideExtension.songCoverOptions
import com.ttop.app.apex.helper.ShuffleHelper.makeShuffleList
import com.ttop.app.apex.libraries.appthemehelper.util.VersionUtils
import com.ttop.app.apex.model.Song
import com.ttop.app.apex.model.Song.Companion.emptySong
import com.ttop.app.apex.model.smartplaylist.AbsSmartPlaylist
import com.ttop.app.apex.providers.HistoryStore
import com.ttop.app.apex.providers.MusicPlaybackQueueStore
import com.ttop.app.apex.providers.SongPlayCountStore
import com.ttop.app.apex.service.notification.PlayingNotification
import com.ttop.app.apex.service.notification.PlayingNotificationImpl24
import com.ttop.app.apex.service.playback.Playback.PlaybackCallbacks
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.MusicUtil.isFavorite
import com.ttop.app.apex.util.MusicUtil.toggleFavorite
import com.ttop.app.apex.util.PackageValidator
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.PreferenceUtil.crossFadeDuration
import com.ttop.app.apex.util.PreferenceUtil.isBluetoothSpeaker
import com.ttop.app.apex.util.PreferenceUtil.isHeadsetPlugged
import com.ttop.app.apex.util.PreferenceUtil.isPauseOnZeroVolume
import com.ttop.app.apex.util.PreferenceUtil.playbackPitch
import com.ttop.app.apex.util.PreferenceUtil.playbackSpeed
import com.ttop.app.apex.util.PreferenceUtil.registerOnSharedPreferenceChangedListener
import com.ttop.app.apex.util.PreferenceUtil.unregisterOnSharedPreferenceChangedListener
import com.ttop.app.apex.volume.AudioVolumeObserver
import com.ttop.app.apex.volume.OnAudioVolumeChangedListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.get
import java.util.Objects
import java.util.Random
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


/**
 * @author Karim Abou Zeid (kabouzeid), Andrew Neal. Modified by TTOP
 */
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
class MusicService : MediaBrowserServiceCompat(),
    OnSharedPreferenceChangeListener, PlaybackCallbacks, OnAudioVolumeChangedListener {
    private val musicBind: IBinder = MusicBinder()

    @JvmField
    var nextPosition = -1

    @JvmField
    var pendingQuit = false

    private val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    private lateinit var playbackManager: PlaybackManager

    private var mPackageValidator: PackageValidator? = null
    private val mMusicProvider = get<AutoMusicProvider>(AutoMusicProvider::class.java)
    private lateinit var storage: PersistentStorage
    private var trackEndedByCrossfade = false
    private val serviceScope = CoroutineScope(Job() + Main)

    @JvmField
    var position = -1
    private val appWidgetBig = AppWidgetBig.instance
    private val appWidgetCircle = AppWidgetCircle.instance
    private val appWidgetClassic = AppWidgetClassic.instance
    private val appWidgetFull = AppWidgetFull.instance
    private val widgetIntentReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val command = intent.getStringExtra(EXTRA_APP_WIDGET_NAME)
            val ids = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)

            if (command != null) {
                when (command) {
                    AppWidgetBig.NAME -> {
                        appWidgetBig.performUpdate(this@MusicService, ids)
                    }

                    AppWidgetClassic.NAME -> {
                        appWidgetClassic.performUpdate(this@MusicService, ids)
                    }

                    AppWidgetFull.NAME -> {
                        appWidgetFull.performUpdate(this@MusicService, ids)
                    }

                    AppWidgetCircle.NAME -> {
                        appWidgetCircle.performUpdate(this@MusicService, ids)
                    }
                }
            }
            updateWidget()
        }
    }

    private val bluetoothConnectedIntentFilter = IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED)
    private val bluetoothDisconnectedIntentFilter =
        IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED)
    private val bluetoothRequestDisconnectIntentFilter =
        IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED)
    private var bluetoothConnectedRegistered = false
    private var bluetoothDisconnectedRegistered = false
    private var bluetoothRequestDisconnectRegistered = false
    private val headsetReceiverIntentFilter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
    private var headsetReceiverRegistered = false
    private val internetReceiverIntentFilter = IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
    private var internetReceiverRegistered = false
    var mediaSession: MediaSessionCompat? = null
    private lateinit var mediaStoreObserver: ContentObserver
    private var musicPlayerHandlerThread: HandlerThread? = null
    private var notHandledMetaChangedForCurrentTrack = false
    private var originalPlayingQueue = ArrayList<Song>()

    @JvmField
    var playingQueue = ArrayList<Song>()

    private var playerHandler: Handler? = null

    private var playingNotification: PlayingNotification? = null

    private val updateFavoriteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            isCurrentFavorite { isFavorite ->
                if (!isForeground) {
                    playingNotification?.updateMetadata(currentSong) {
                        playingNotification?.setPlaying(isPlaying)
                        playingNotification?.updateFavorite(isFavorite)
                        startForegroundOrNotify()
                    }
                } else {
                    playingNotification?.updateFavorite(isFavorite)
                    startForegroundOrNotify()
                }

                appWidgetCircle.notifyChange(this@MusicService, FAVORITE_STATE_CHANGED)
            }
        }
    }

    private var queuesRestored = false

    var repeatMode = 0
        private set(value) {
            when (value) {
                REPEAT_MODE_NONE, REPEAT_MODE_ALL, REPEAT_MODE_THIS -> {
                    field = value
                    PreferenceManager.getDefaultSharedPreferences(this).edit {
                        putInt(SAVED_REPEAT_MODE, value)
                    }
                    prepareNext()
                    handleAndSendChangeInternal(REPEAT_MODE_CHANGED)
                }
            }
        }

    @JvmField
    var shuffleMode = 0
    private val songPlayCountHelper = SongPlayCountHelper()
    var connected = false

    private val bluetoothReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            val am: AudioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager
            if (action != null) {
                val device: BluetoothDevice? = if (VersionUtils.hasT()) {
                    intent.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE,
                        BluetoothDevice::class.java
                    )
                } else {
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                }

                if (BluetoothDevice.ACTION_ACL_CONNECTED == action && isBluetoothSpeaker) {
                    if (PreferenceUtil.specificDevice) {
                        if (device?.address == PreferenceUtil.bluetoothDevice) {
                            connected = true

                            Handler(Looper.getMainLooper()).postDelayed(
                                Runnable {
                                    if (playingQueue.isNotEmpty() && !isPlaying) {
                                        if (!am.isMusicActive) {
                                            play()
                                        }
                                    }
                                }, PreferenceUtil.bluetoothDelay.toLong()
                            )
                        }
                    } else {
                        Handler(Looper.getMainLooper()).postDelayed(
                            Runnable {
                                if (playingQueue.isNotEmpty()) {
                                    if (!am.isMusicActive) {
                                        play()
                                    }
                                }
                            }, PreferenceUtil.bluetoothDelay.toLong()
                        )
                    }
                }

                if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED == action || BluetoothDevice.ACTION_ACL_DISCONNECTED == action) {
                    if (PreferenceUtil.specificDevice) {
                        if (device?.address == PreferenceUtil.bluetoothDevice) {
                            connected = false
                            pause(true)
                        }
                    }
                }
            }
        }
    }

    private var receivedHeadsetConnected = false
    private val headsetReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action != null) {
                if (Intent.ACTION_HEADSET_PLUG == action) {
                    when (intent.getIntExtra("state", -1)) {
                        0 -> pause()
                        // Check whether the current song is empty which means the playing queue hasn't restored yet
                        1 -> if (currentSong != emptySong) {
                            Handler(Looper.getMainLooper()).postDelayed(
                                Runnable {
                                    if (playingQueue.isNotEmpty()) {
                                        play()
                                    }
                                }, 1000
                            )
                        } else {
                            receivedHeadsetConnected = true
                        }
                    }
                }
            }
        }
    }
    private val internetReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Handler(Looper.getMainLooper()).postDelayed(
                Runnable {
                    PreferenceUtil.isInternetConnected = ApexUtil.isNetworkAvailable(context)
                }, 1000
            )

        }
    }
    private var throttledSeekHandler: ThrottledSeekHandler? = null
    private var uiThreadHandler: Handler? = null
    private var wakeLock: WakeLock? = null
    private var notificationManager: NotificationManager? = null
    private var isForeground = false
    override fun onCreate() {
        super.onCreate()
        val powerManager = getSystemService<PowerManager>()
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, javaClass.name)
        }
        wakeLock?.setReferenceCounted(false)
        musicPlayerHandlerThread = HandlerThread("PlaybackHandler")
        musicPlayerHandlerThread?.start()
        playerHandler = Handler(musicPlayerHandlerThread!!.looper)

        playbackManager = PlaybackManager(this)
        playbackManager.setCallbacks(this)
        setupMediaSession()

        uiThreadHandler = Handler(Looper.getMainLooper())
        sessionToken = mediaSession?.sessionToken
        notificationManager = getSystemService()
        initNotification()
        mediaStoreObserver = MediaStoreObserver(this, playerHandler!!)
        throttledSeekHandler = ThrottledSeekHandler(this, Handler(mainLooper))
        contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true,
            mediaStoreObserver
        )
        contentResolver.registerContentObserver(
            MediaStore.Audio.Media.INTERNAL_CONTENT_URI,
            true,
            mediaStoreObserver
        )
        val audioVolumeObserver = AudioVolumeObserver(this)
        audioVolumeObserver.register(AudioManager.STREAM_MUSIC, this)
        registerOnSharedPreferenceChangedListener(this)
        restoreState()
        sendBroadcast(Intent("$APEX_MUSIC_PACKAGE_NAME.APEX_MUSIC_SERVICE_CREATED"))
        registerHeadsetEvents()
        registerBluetoothConnected()
        registerBluetoothDisconnected()
        registerBluetoothRequestDisconnect()
        registerInternetEvents()
        ContextCompat.registerReceiver(
            this,
            widgetIntentReceiver,
            IntentFilter(APP_WIDGET_UPDATE),
            ContextCompat.RECEIVER_EXPORTED
        )
        ContextCompat.registerReceiver(
            this,
            updateFavoriteReceiver,
            IntentFilter(FAVORITE_STATE_CHANGED),
            ContextCompat.RECEIVER_EXPORTED
        )
        mPackageValidator = PackageValidator(this, R.xml.allowed_media_browser_callers)
        mMusicProvider.setMusicService(this)
        storage = PersistentStorage.getInstance(this)
        updateWidget()
        AutoConnectionDetector(this).registerCarConnectionReceiver()
    }

    override fun onDestroy() {
        unregisterReceiver(widgetIntentReceiver)
        unregisterReceiver(updateFavoriteReceiver)
        if (headsetReceiverRegistered) {
            unregisterReceiver(headsetReceiver)
            headsetReceiverRegistered = false
        }

        if (bluetoothConnectedRegistered) {
            unregisterReceiver(bluetoothReceiver)
            bluetoothConnectedRegistered = false
            bluetoothDisconnectedRegistered = false
            bluetoothRequestDisconnectRegistered = false
        }

        if (internetReceiverRegistered) {
            unregisterReceiver(internetReceiver)
            internetReceiverRegistered = false
        }
        AutoConnectionDetector(this).unRegisterCarConnectionReceiver()
        scheduler.shutdownNow()
        mediaSession?.isActive = false
        quit()
        releaseResources()
        serviceScope.cancel()
        contentResolver.unregisterContentObserver(mediaStoreObserver)
        unregisterOnSharedPreferenceChangedListener(this)
        wakeLock?.release()
        sendBroadcast(Intent("$APEX_MUSIC_PACKAGE_NAME.APEX_MUSIC_SERVICE_DESTROYED"))
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartServiceIntent = Intent(applicationContext, this.javaClass)
        restartServiceIntent.setPackage(packageName)
        ContextCompat.startForegroundService(applicationContext, restartServiceIntent)
        super.onTaskRemoved(rootIntent)
    }

    private fun acquireWakeLock() {
        wakeLock?.acquire(30000)
    }

    private var pausedByZeroVolume = false
    override fun onAudioVolumeChanged(currentVolume: Int, maxVolume: Int) {
        if (isPauseOnZeroVolume) {
            if (isPlaying && currentVolume < 1) {
                pause()
                pausedByZeroVolume = true
            } else if (pausedByZeroVolume && currentVolume >= 1) {
                play()
                pausedByZeroVolume = false
            }
        }
    }

    fun addSong(position: Int, song: Song) {
        playingQueue.add(position, song)
        originalPlayingQueue.add(position, song)
        notifyChange(QUEUE_CHANGED)
    }

    fun addSong(song: Song) {
        playingQueue.add(song)
        originalPlayingQueue.add(song)
        notifyChange(QUEUE_CHANGED)
    }

    fun addSongs(position: Int, songs: List<Song>?) {
        playingQueue.addAll(position, songs!!)
        originalPlayingQueue.addAll(position, songs)
        notifyChange(QUEUE_CHANGED)
    }

    fun addSongs(songs: List<Song>?) {
        playingQueue.addAll(songs!!)
        originalPlayingQueue.addAll(songs)
        notifyChange(QUEUE_CHANGED)
    }

    fun clearQueue() {
        playingQueue.clear()
        originalPlayingQueue.clear()
        setPosition(-1)
        sendChangeInternal(QUEUE_CHANGED)
        notifyChange(QUEUE_CHANGED)
    }

    fun currentPosition(): Int {
        return position
    }

    fun cycleRepeatMode() {
        repeatMode = when (repeatMode) {
            REPEAT_MODE_NONE -> REPEAT_MODE_ALL
            REPEAT_MODE_ALL -> REPEAT_MODE_THIS
            else -> REPEAT_MODE_NONE
        }
    }

    val audioSessionId: Int
        get() = playbackManager.audioSessionId

    val currentSong: Song
        get() = getSongAt(getPosition())

    val nextSong: Song?
        get() = if (isLastTrack && repeatMode == REPEAT_MODE_NONE) {
            null
        } else {
            getSongAt(getNextPosition(false))
        }

    private fun getNextPosition(force: Boolean): Int {
        var position = getPosition() + 1
        when (repeatMode) {
            REPEAT_MODE_ALL -> if (isLastTrack) {
                position = 0
            }

            REPEAT_MODE_THIS -> if (force) {
                if (isLastTrack) {
                    position = 0
                }
            } else {
                position -= 1
            }

            REPEAT_MODE_NONE -> if (isLastTrack) {
                position -= 1
            }

            else -> if (isLastTrack) {
                position -= 1
            }
        }
        return position
    }

    private fun getPosition(): Int {
        return position
    }

    private fun setPosition(position: Int) {
        openTrackAndPrepareNextAt(position) { success ->
            if (success) {
                notifyChange(PLAY_STATE_CHANGED)
            }
        }
    }

    private fun getPreviousPosition(force: Boolean): Int {
        var newPosition = getPosition() - 1
        when (repeatMode) {
            REPEAT_MODE_ALL -> if (newPosition < 0) {
                newPosition = playingQueue.size - 1
            }

            REPEAT_MODE_THIS -> if (force) {
                if (newPosition < 0) {
                    newPosition = playingQueue.size - 1
                }
            } else {
                newPosition = getPosition()
            }

            REPEAT_MODE_NONE -> if (newPosition < 0) {
                newPosition = 0
            }

            else -> if (newPosition < 0) {
                newPosition = 0
            }
        }
        return newPosition
    }

    fun getQueueDurationMillis(position: Int): Long {
        var duration: Long = 0
        for (i in position + 1 until playingQueue.size) {
            duration += playingQueue[i].duration
        }
        return duration
    }

    private fun getShuffleMode(): Int {
        return shuffleMode
    }

    fun setShuffleMode(shuffleMode: Int) {
        PreferenceManager.getDefaultSharedPreferences(this).edit {
            putInt(SAVED_SHUFFLE_MODE, shuffleMode)
        }
        when (shuffleMode) {
            SHUFFLE_MODE_SHUFFLE -> {
                this.shuffleMode = shuffleMode
                makeShuffleList(playingQueue, getPosition())
                position = 0
            }

            SHUFFLE_MODE_NONE -> {
                this.shuffleMode = shuffleMode
                val currentSongId = Objects.requireNonNull(currentSong).id
                playingQueue = ArrayList(originalPlayingQueue)
                var newPosition = 0
                for (song in playingQueue) {
                    if (song.id == currentSongId) {
                        newPosition = playingQueue.indexOf(song)
                    }
                }
                position = newPosition
            }
        }
        handleAndSendChangeInternal(SHUFFLE_MODE_CHANGED)
        notifyChange(QUEUE_CHANGED)
    }

    private fun getSongAt(position: Int): Song {
        return if ((position >= 0) && (position < playingQueue.size)) {
            playingQueue[position]
        } else {
            emptySong
        }
    }

    val songDurationMillis: Int
        get() = playbackManager.songDurationMillis

    val songProgressMillis: Int
        get() = playbackManager.songProgressMillis

    fun handleAndSendChangeInternal(what: String) {
        handleChangeInternal(what)
        sendChangeInternal(what)
    }

    private fun initNotification() {
        playingNotification =
            PlayingNotificationImpl24.from(this, notificationManager!!, mediaSession!!)
    }

    private val isLastTrack: Boolean
        get() = getPosition() == playingQueue.size - 1

    val isPlaying: Boolean
        get() = playbackManager.isPlaying

    fun moveSong(from: Int, to: Int) {
        if (from == to) {
            return
        }
        val currentPosition = getPosition()
        val songToMove = playingQueue.removeAt(from)
        playingQueue.add(to, songToMove)
        if (getShuffleMode() == SHUFFLE_MODE_NONE) {
            val tmpSong = originalPlayingQueue.removeAt(from)
            originalPlayingQueue.add(to, tmpSong)
        }
        when {
            currentPosition in to until from -> {
                position = currentPosition + 1
            }

            currentPosition in (from + 1)..to -> {
                position = currentPosition - 1
            }

            from == currentPosition -> {
                position = to
            }
        }
        notifyChange(QUEUE_CHANGED)
    }

    private fun notifyChange(what: String) {
        handleAndSendChangeInternal(what)
        sendPublicIntent(what)
    }

    override fun onBind(intent: Intent): IBinder {
        // For Android auto, need to call super, or onGetRoot won't be called.
        return if ("android.media.browse.MediaBrowserService" == intent.action) {
            super.onBind(intent)!!
        } else musicBind
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?,
    ): BrowserRoot {


        // Check origin to ensure we're not allowing any arbitrary app to browse app contents
        return if (!mPackageValidator!!.isKnownCaller(clientPackageName, clientUid)) {
            // Request from an untrusted package: return an empty browser root
            BrowserRoot(AutoMediaIDHelper.MEDIA_ID_EMPTY_ROOT, null)
        } else {
            /**
             * By default return the browsable root. Treat the EXTRA_RECENT flag as a special case
             * and return the recent root instead.
             */
            val isRecentRequest = rootHints?.getBoolean(BrowserRoot.EXTRA_RECENT) ?: false
            val browserRootPath =
                if (isRecentRequest) AutoMediaIDHelper.RECENT_ROOT else AutoMediaIDHelper.MEDIA_ID_ROOT
            BrowserRoot(browserRootPath, null)
        }
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<List<MediaBrowserCompat.MediaItem>>,
    ) {
        if (parentId == AutoMediaIDHelper.RECENT_ROOT) {
            result.sendResult(listOf(storage.recentSong()))
        } else {
            result.sendResult(mMusicProvider.getChildren(parentId, resources))
        }
    }

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences, key: String?,
    ) {
        when (key) {
            PLAYBACK_SPEED, PLAYBACK_PITCH -> {
                updateMediaSessionPlaybackState()
                playbackManager.setPlaybackSpeedPitch(playbackSpeed, playbackPitch)
            }

            CROSS_FADE_DURATION -> {
                val progress = songProgressMillis
                val wasPlaying = isPlaying

                if (playbackManager.maybeSwitchToCrossFade(crossFadeDuration)) {
                    restorePlaybackState(wasPlaying, progress)
                } else {
                    playbackManager.setCrossFadeDuration(crossFadeDuration)
                }
            }

            TOGGLE_HEADSET -> registerHeadsetEvents()
            BLUETOOTH_PLAYBACK -> {
                registerBluetoothConnected()
                registerBluetoothDisconnected()
                registerBluetoothRequestDisconnect()
            }

            NOTIFICATION_ACTION_1,
            NOTIFICATION_ACTION_2,
            CAR_CONNECTED,
            AUTO_ACTION_1,
            AUTO_ACTION_2,
            USE_NOTIFY_ACTIONS_AUTO -> {
                update()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && intent.action != null) {
            serviceScope.launch {
                restoreQueuesAndPositionIfNecessary()
                when (intent.action) {
                    ACTION_TOGGLE_PAUSE -> {
                        if (isPlaying) {
                            pause()
                        } else {
                            play()
                        }
                    }

                    ACTION_PAUSE -> pause()
                    ACTION_PLAY -> play()
                    ACTION_PLAY_PLAYLIST -> playFromPlaylist(intent)
                    ACTION_REWIND -> {
                        playPreviousSongAuto(true, isPlaying)
                    }

                    ACTION_SKIP -> {
                        playNextSongAuto(true, isPlaying)
                    }

                    ACTION_STOP, ACTION_QUIT -> {
                        pendingQuit = false
                        quit()
                    }

                    ACTION_PENDING_QUIT -> pendingQuit = true
                    TOGGLE_FAVORITE -> toggleFavorite()
                    UPDATE_NOTIFY -> {
                        update()
                    }

                    ACTION_UPDATE -> {
                        appWidgetBig.notifyThemeChange(this@MusicService)
                        appWidgetClassic.notifyThemeChange(this@MusicService)
                        appWidgetCircle.notifyThemeChange(this@MusicService)
                        appWidgetFull.notifyThemeChange(this@MusicService)

                        updateWidget()
                    }
                }
            }
        }
        return START_NOT_STICKY
    }

    fun update() {
        if (VersionUtils.hasT()) {
            updateMediaSessionPlaybackState()
        } else {
            updatePlaybackControls()
        }
    }

    override fun onTrackEnded() {
        acquireWakeLock()
        // if there is a timer finished, don't continue
        if (pendingQuit
            || repeatMode == REPEAT_MODE_NONE && isLastTrack
        ) {
            notifyChange(PLAY_STATE_CHANGED)
            seek(0, false)
            if (pendingQuit) {
                pendingQuit = false
                quit()
            }
        } else {
            playNextSong(false)
        }
        releaseWakeLock()
    }

    override fun onTrackEndedWithCrossfade() {
        trackEndedByCrossfade = true
        onTrackEnded()
    }

    override fun onTrackWentToNext() {
        if (pendingQuit || repeatMode == REPEAT_MODE_NONE && isLastTrack) {
            playbackManager.setNextDataSource(null)
            pause(false)
            seek(0)
            if (pendingQuit) {
                pendingQuit = false
                quit()
            }
        } else {
            position = nextPosition
            prepareNextImpl()
            notifyChange(META_CHANGED)
        }
    }

    override fun onPlayStateChanged() {
        notifyChange(PLAY_STATE_CHANGED)
    }

    override fun onUnbind(intent: Intent): Boolean {
        if (!isPlaying) {
            stopSelf()
        }
        return true
    }

    fun openQueue(
        playingQueue: List<Song>?,
        startPosition: Int,
        startPlaying: Boolean,
    ) {
        if (!playingQueue.isNullOrEmpty()
            && startPosition >= 0 && startPosition < playingQueue.size
        ) {
            // it is important to copy the playing queue here first as we might add/remove songs later
            originalPlayingQueue = ArrayList(playingQueue)
            this.playingQueue = ArrayList(originalPlayingQueue)
            var position = startPosition
            if (shuffleMode == SHUFFLE_MODE_SHUFFLE) {
                makeShuffleList(this.playingQueue, startPosition)
                position = 0
            }
            if (startPlaying) {
                playSongAt(position)
            } else {
                setPosition(position)
            }
            notifyChange(QUEUE_CHANGED)
        }
    }

    @Synchronized
    fun openTrackAndPrepareNextAt(position: Int, completion: (success: Boolean) -> Unit) {
        this.position = position
        openCurrent { success ->
            completion(success)
            if (success) {
                prepareNextImpl()
            }
            notifyChange(META_CHANGED)
            notHandledMetaChangedForCurrentTrack = false
        }
    }

    fun pause(force: Boolean = false) {
        playbackManager.pause(force) {
            notifyChange(PLAY_STATE_CHANGED)
        }
    }

    @Synchronized
    fun play() {
        playbackManager.play { playSongAt(getPosition()) }
        if (notHandledMetaChangedForCurrentTrack) {
            handleChangeInternal(META_CHANGED)
            notHandledMetaChangedForCurrentTrack = false
        }
        notifyChange(PLAY_STATE_CHANGED)
    }

    fun playNextSong(force: Boolean) {
        playSongAt(getNextPosition(force))
    }

    fun playNextSongAuto(force: Boolean, isPlaying: Boolean) {
        playSongAtImpl(getNextPosition(force), isPlaying)
    }

    fun playPreviousSong(force: Boolean) {
        playSongAt(getPreviousPosition(force))
        /*if (songProgressMillis < 5000) {
            seek(0)
        }else{
            playSongAt(getPreviousPosition(force))
        }*/
    }

    fun playPreviousSongAuto(force: Boolean, isPlaying: Boolean) {
        playSongAtImpl(getPreviousPosition(force), isPlaying)
        /*if (songProgressMillis < 5000) {
            seek(0)
        }else{
            playSongAtImpl(getPreviousPosition(force), isPlaying)
        }*/
    }

    fun playSongAt(position: Int) {
        // Every chromecast method needs to run on main thread or you are greeted with IllegalStateException
        // So it will use Main dispatcher
        // And by using Default dispatcher for local playback we are reduce the burden of main thread
        serviceScope.launch(if (playbackManager.isLocalPlayback) Default else Main) {
            openTrackAndPrepareNextAt(position) { success ->
                if (success) {
                    play()
                } else {
                    runOnUiThread {
                        showToast(R.string.unplayable_file)
                        playNextSong(true)
                    }
                }
            }
        }
    }

    fun playSong(position: Long) {
        openTrackAndPrepareNextAt(position.toInt()) { success ->
            if (success) {
                play()
            }
        }
    }

    private fun playSongAtImpl(position: Int, play: Boolean) {
        openTrackAndPrepareNextAt(position) { success ->
            if (success) {
                if (play) {
                    play()
                }
            } else {
                runOnUiThread {
                    showToast(R.string.unplayable_file)
                    playNextSong(true)
                }
            }
        }
    }

    @Synchronized
    fun prepareNextImpl() {
        try {
            val nextPosition = getNextPosition(false)
            playbackManager.setNextDataSource(getSongAt(nextPosition).uri.toString())
            this.nextPosition = nextPosition
        } catch (ignored: Exception) {
        }
    }

    fun toggleFavorite() {
        serviceScope.launch {
            toggleFavorite(currentSong)
            sendBroadcast(Intent(FAVORITE_STATE_CHANGED))
        }
    }

    fun isCurrentFavorite(completion: (isFavorite: Boolean) -> Unit) {
        serviceScope.launch(IO) {
            val isFavorite = isFavorite(currentSong)
            withContext(Main) {
                completion(isFavorite)
            }
        }
    }

    fun quit() {
        pause()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        isForeground = false
        notificationManager?.cancel(PlayingNotification.NOTIFICATION_ID)

        stopSelf()
    }

    private fun releaseWakeLock() {
        if (wakeLock!!.isHeld) {
            wakeLock?.release()
        }
    }

    fun removeSong(position: Int) {
        if (getShuffleMode() == SHUFFLE_MODE_NONE) {
            playingQueue.removeAt(position)
            originalPlayingQueue.removeAt(position)
        } else {
            originalPlayingQueue.remove(playingQueue.removeAt(position))
        }
        rePosition(position)
        notifyChange(QUEUE_CHANGED)
    }

    private fun removeSongImpl(song: Song) {
        val deletePosition = playingQueue.indexOf(song)
        if (deletePosition != -1) {
            playingQueue.removeAt(deletePosition)
            rePosition(deletePosition)
        }

        val originalDeletePosition = originalPlayingQueue.indexOf(song)
        if (originalDeletePosition != -1) {
            originalPlayingQueue.removeAt(originalDeletePosition)
            rePosition(originalDeletePosition)
        }
    }

    fun removeSong(song: Song) {
        removeSongImpl(song)
        notifyChange(QUEUE_CHANGED)
    }

    fun removeSongs(songs: List<Song>) {
        for (song in songs) {
            removeSongImpl(song)
        }
        notifyChange(QUEUE_CHANGED)
    }

    private fun rePosition(deletedPosition: Int) {
        val currentPosition = getPosition()
        if (deletedPosition < currentPosition) {
            position = currentPosition - 1
        } else if (deletedPosition == currentPosition) {
            if (playingQueue.size > deletedPosition) {
                setPosition(position)
            } else {
                setPosition(position - 1)
            }
        }
    }

    private suspend fun restoreQueuesAndPositionIfNecessary() {
        if (!queuesRestored && playingQueue.isEmpty()) {
            withContext(IO) {
                val restoredQueue =
                    MusicPlaybackQueueStore.getInstance(this@MusicService).savedPlayingQueue
                val restoredOriginalQueue =
                    MusicPlaybackQueueStore.getInstance(this@MusicService).savedOriginalPlayingQueue
                val restoredPosition =
                    PreferenceManager.getDefaultSharedPreferences(this@MusicService).getInt(
                        SAVED_POSITION, -1
                    )
                val restoredPositionInTrack =
                    PreferenceManager.getDefaultSharedPreferences(this@MusicService).getInt(
                        SAVED_POSITION_IN_TRACK, -1
                    )
                if (restoredQueue.size > 0 && restoredQueue.size == restoredOriginalQueue.size && restoredPosition != -1) {
                    originalPlayingQueue = ArrayList(restoredOriginalQueue)
                    playingQueue = ArrayList(restoredQueue)
                    position = restoredPosition
                    withContext(Main) {
                        openCurrent {
                            prepareNext()
                            if (restoredPositionInTrack > 0) {
                                seek(restoredPositionInTrack)
                            }
                            notHandledMetaChangedForCurrentTrack = true
                            sendChangeInternal(META_CHANGED)
                        }
                        if (receivedHeadsetConnected) {
                            play()
                            receivedHeadsetConnected = false
                        }
                    }

                    sendChangeInternal(QUEUE_CHANGED)
                    //REMOVE IN v3.70
                    //mediaSession?.setQueueTitle(getString(R.string.now_playing_queue))
                    //mediaSession?.setQueue(playingQueue.toMediaSessionQueue())
                }
            }
            queuesRestored = true
        }
    }

    fun runOnUiThread(runnable: Runnable?) {
        uiThreadHandler?.post(runnable!!)
    }

    fun savePositionInTrack() {
        PreferenceManager.getDefaultSharedPreferences(this).edit {
            putInt(SAVED_POSITION_IN_TRACK, songProgressMillis)
        }
    }

    @Synchronized
    fun seek(millis: Int, force: Boolean = true): Int {
        return try {
            val newPosition = playbackManager.seek(millis, force)
            throttledSeekHandler?.notifySeek()
            newPosition
        } catch (e: Exception) {
            -1
        }
    }

    // to let other apps know whats playing. i.e. last.fm (scrobbling) or musixmatch
    fun sendPublicIntent(what: String) {
        val intent = Intent(what.replace(APEX_MUSIC_PACKAGE_NAME, MUSIC_PACKAGE_NAME))
        val song = currentSong
        intent.putExtra("id", song.id)
        intent.putExtra("artist", song.artistName)
        intent.putExtra("album", song.albumName)
        intent.putExtra("track", song.title)
        intent.putExtra("duration", song.duration)
        intent.putExtra("position", songProgressMillis.toLong())
        intent.putExtra("playing", isPlaying)
        intent.putExtra("scrobbling_source", APEX_MUSIC_PACKAGE_NAME)
        @Suppress("Deprecation")
        sendStickyBroadcast(intent)
    }

    fun toggleShuffle() {
        if (getShuffleMode() == SHUFFLE_MODE_NONE) {
            setShuffleMode(SHUFFLE_MODE_SHUFFLE)
        } else {
            setShuffleMode(SHUFFLE_MODE_NONE)
        }
    }

    fun updateMediaSessionPlaybackState() {
        val stateBuilder = Builder()
            .setActions(MEDIA_SESSION_ACTIONS)
            .setState(
                if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                songProgressMillis.toLong(),
                playbackSpeed
            )
        setCustomAction(stateBuilder)
        mediaSession?.setPlaybackState(stateBuilder.build())
    }

    private fun rebuildMetaData() {
        playingNotification?.updateMetadata(currentSong) { startForegroundOrNotify() }
    }

    fun updatePlaybackControls() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val barNotifications = notificationManager.activeNotifications
        for (notification in barNotifications) {
            if (notification.id == PlayingNotification.NOTIFICATION_ID) {
                updateNotification()
                rebuildMetaData()
            }
        }

        if (VersionUtils.hasT()) {
            updateMediaSessionPlaybackState()
        }
    }

    private fun updateNotification() {
        if (playingNotification != null && currentSong.id != -1L) {
            stopForegroundAndNotification()
            initNotification()
        }
    }

    @SuppressLint("CheckResult")
    fun updateMediaSessionMetaData(onCompletion: () -> Unit) {
        Log.i(TAG, "onResourceReady: ")
        val song = currentSong
        if (song.id == -1L) {
            mediaSession?.setMetadata(null)
            return
        }
        val metaData = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artistName)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, song.albumArtist)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.albumName)
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.duration)
            .putLong(
                MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER,
                (getPosition() + 1).toLong()
            )
            .putLong(MediaMetadataCompat.METADATA_KEY_YEAR, song.year.toLong())
            .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, null)
            .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, playingQueue.size.toLong())

        if (VersionUtils.hasT()) {
            Glide.with(this)
                .asBitmap()
                .songCoverOptions(song)
                .load(getSongModel(song))
                .into(object :
                CustomTarget<Bitmap?>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    metaData.putBitmap(
                        MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                        BitmapFactory.decodeResource(
                            resources,
                            R.drawable.default_audio_art
                        )
                    )
                    mediaSession?.setMetadata(metaData.build())
                    onCompletion()
                }

                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap?>?,
                ) {
                    metaData.putBitmap(
                        MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                        resource
                    )
                    mediaSession?.setMetadata(metaData.build())
                    onCompletion()
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
        } else {
            mediaSession?.setMetadata(metaData.build())
            onCompletion()
        }
    }

    private fun handleChangeInternal(what: String) {
        when (what) {
            PLAY_STATE_CHANGED -> {
                updateMediaSessionPlaybackState()
                val isPlaying = isPlaying
                if (!isPlaying && songProgressMillis > 0) {
                    savePositionInTrack()
                }
                songPlayCountHelper.notifyPlayStateChanged(isPlaying)
                playingNotification?.setPlaying(isPlaying)
                startForegroundOrNotify()
            }

            FAVORITE_STATE_CHANGED -> {
                isCurrentFavorite { isFavorite ->
                    playingNotification?.updateFavorite(isFavorite)

                    startForegroundOrNotify()
                }
            }

            META_CHANGED -> {
                playingNotification?.updateMetadata(currentSong) { startForegroundOrNotify() }
                isCurrentFavorite { isFavorite ->
                    playingNotification?.updateFavorite(isFavorite)
                    startForegroundOrNotify()
                }

                // We must call updateMediaSessionPlaybackState after the load of album art is completed
                // if we are loading it or it won't be updated in the notification
                updateMediaSessionMetaData(::updateMediaSessionPlaybackState)
                savePosition()
                savePositionInTrack()
                serviceScope.launch(IO) {
                    val currentSong = currentSong
                    HistoryStore.getInstance(this@MusicService).addSongId(currentSong.id)
                    if (songPlayCountHelper.shouldBumpPlayCount()) {
                        SongPlayCountStore.getInstance(this@MusicService)
                            .bumpPlayCount(songPlayCountHelper.song.id)
                    }
                    songPlayCountHelper.notifySongChanged(currentSong)
                    storage.saveSong(currentSong)
                }
            }

            QUEUE_CHANGED -> {
                updateMediaSessionMetaData(::updateMediaSessionPlaybackState) // because playing queue size might have changed
                saveQueues()
                if (playingQueue.size > 0) {
                    prepareNext()
                } else {
                    stopForegroundAndNotification()
                }
            }
        }
    }

    private fun startForegroundOrNotify() {
        if (playingNotification != null && currentSong.id != -1L) {
            if (!isForeground && isPlaying) {
                // Specify that this is a media service, if supported.
                startForeground(
                    PlayingNotification.NOTIFICATION_ID, playingNotification!!.build(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
                isForeground = true
            } else {
                // If we are already in foreground just update the notification
                notificationManager?.notify(
                    PlayingNotification.NOTIFICATION_ID, playingNotification!!.build()
                )
            }
        }
    }

    private fun stopForegroundAndNotification() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        notificationManager?.cancel(PlayingNotification.NOTIFICATION_ID)
        isForeground = false
    }

    @Synchronized
    private fun openCurrent(completion: (success: Boolean) -> Unit) {
        val force = if (!trackEndedByCrossfade) {
            true
        } else {
            trackEndedByCrossfade = false
            false
        }
        playbackManager.setDataSource(currentSong, force) { success ->
            completion(success)
        }
    }

    fun switchToLocalPlayback() {
        playbackManager.switchToLocalPlayback(this::restorePlaybackState)
    }

    fun switchToRemotePlayback(castPlayer: CastPlayer) {
        playbackManager.switchToRemotePlayback(castPlayer, this::restorePlaybackState)
    }

    private fun restorePlaybackState(wasPlaying: Boolean, progress: Int) {
        playbackManager.setCallbacks(this)
        openTrackAndPrepareNextAt(position) { success ->
            if (success) {
                seek(progress)
                if (wasPlaying) {
                    play()
                } else {
                    pause()
                }
            }
        }
        playbackManager.setCrossFadeDuration(crossFadeDuration)
    }

    private fun playFromPlaylist(intent: Intent) {
        val playlist: AbsSmartPlaylist? = if (VersionUtils.hasT()) {
            intent.getParcelableExtra(INTENT_EXTRA_PLAYLIST, AbsSmartPlaylist::class.java)
        } else {
            intent.getParcelableExtra(INTENT_EXTRA_PLAYLIST)
        }

        val shuffleMode = intent.getIntExtra(INTENT_EXTRA_SHUFFLE_MODE, getShuffleMode())
        if (playlist != null) {
            val playlistSongs = playlist.songs()
            if (playlistSongs.isNotEmpty()) {
                if (shuffleMode == SHUFFLE_MODE_SHUFFLE) {
                    val startPosition = Random().nextInt(playlistSongs.size)
                    openQueue(playlistSongs, startPosition, true)
                    setShuffleMode(shuffleMode)
                } else {
                    openQueue(playlistSongs, 0, true)
                }
            } else {
                runOnUiThread {
                    showToast(R.string.playlist_is_empty, Toast.LENGTH_LONG)
                }
            }
        } else {
            runOnUiThread {
                showToast(R.string.playlist_is_empty, Toast.LENGTH_LONG)
            }
        }
    }

    private fun prepareNext() {
        prepareNextImpl()
    }

    private fun registerBluetoothConnected() {
        Log.i(TAG, "registerBluetoothConnected: ")
        ContextCompat.registerReceiver(
            this,
            bluetoothReceiver,
            IntentFilter(bluetoothConnectedIntentFilter),
            ContextCompat.RECEIVER_EXPORTED
        )

        if (!bluetoothConnectedRegistered) {
            ContextCompat.registerReceiver(
                this,
                bluetoothReceiver,
                IntentFilter(bluetoothConnectedIntentFilter),
                ContextCompat.RECEIVER_EXPORTED
            )
            bluetoothConnectedRegistered = true
        }
    }

    private fun registerBluetoothDisconnected() {
        Log.i(TAG, "registerBluetoothDisconnected: ")
        ContextCompat.registerReceiver(
            this,
            bluetoothReceiver,
            IntentFilter(bluetoothDisconnectedIntentFilter),
            ContextCompat.RECEIVER_EXPORTED
        )
        if (!bluetoothDisconnectedRegistered) {
            ContextCompat.registerReceiver(
                this,
                bluetoothReceiver,
                IntentFilter(bluetoothDisconnectedIntentFilter),
                ContextCompat.RECEIVER_EXPORTED
            )
            bluetoothDisconnectedRegistered = true
        }
    }

    private fun registerBluetoothRequestDisconnect() {
        Log.i(TAG, "registerBluetoothRequestDisconnect: ")
        ContextCompat.registerReceiver(
            this,
            bluetoothReceiver,
            IntentFilter(bluetoothRequestDisconnectIntentFilter),
            ContextCompat.RECEIVER_EXPORTED
        )
        if (!bluetoothRequestDisconnectRegistered) {
            ContextCompat.registerReceiver(
                this,
                bluetoothReceiver,
                IntentFilter(bluetoothRequestDisconnectIntentFilter),
                ContextCompat.RECEIVER_EXPORTED
            )
            bluetoothRequestDisconnectRegistered = true
        }
    }

    private fun registerHeadsetEvents() {
        if (!headsetReceiverRegistered && isHeadsetPlugged) {
            ContextCompat.registerReceiver(
                this,
                headsetReceiver,
                IntentFilter(headsetReceiverIntentFilter),
                ContextCompat.RECEIVER_EXPORTED
            )
            headsetReceiverRegistered = true
        }
    }

    private fun registerInternetEvents() {
        if (!internetReceiverRegistered) {
            ContextCompat.registerReceiver(
                this,
                internetReceiver,
                IntentFilter(internetReceiverIntentFilter),
                ContextCompat.RECEIVER_EXPORTED
            )
            internetReceiverRegistered = true
        }
    }


    private fun releaseResources() {
        playerHandler?.removeCallbacksAndMessages(null)
        musicPlayerHandlerThread?.quitSafely()
        playbackManager.release()
        mediaSession?.release()
    }

    fun restoreState(completion: () -> Unit = {}) {
        shuffleMode = PreferenceManager.getDefaultSharedPreferences(this).getInt(
            SAVED_SHUFFLE_MODE, 0
        )
        repeatMode = PreferenceManager.getDefaultSharedPreferences(this).getInt(
            SAVED_REPEAT_MODE, 0
        )
        handleAndSendChangeInternal(SHUFFLE_MODE_CHANGED)
        handleAndSendChangeInternal(REPEAT_MODE_CHANGED)
        serviceScope.launch {
            restoreQueuesAndPositionIfNecessary()
            completion()
        }
    }

    private fun savePosition() {
        PreferenceManager.getDefaultSharedPreferences(this).edit {
            putInt(SAVED_POSITION, getPosition())
        }
    }

    private fun saveQueues() {
        serviceScope.launch(IO) {
            MusicPlaybackQueueStore.getInstance(this@MusicService)
                .saveQueues(playingQueue, originalPlayingQueue)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (!DynamicColors.isDynamicColorAvailable()) {
            when (applicationContext.resources?.configuration?.uiMode?.and(UI_MODE_NIGHT_MASK)) {
                UI_MODE_NIGHT_YES -> {
                    appWidgetBig.notifyThemeChange(this)
                    appWidgetClassic.notifyThemeChange(this)
                    appWidgetFull.notifyThemeChange(this)
                }

                UI_MODE_NIGHT_NO -> {
                    appWidgetBig.notifyThemeChange(this)
                    appWidgetClassic.notifyThemeChange(this)
                    appWidgetFull.notifyThemeChange(this)
                }
            }
        }
    }

    private fun sendChangeInternal(what: String) {
        sendBroadcast(Intent(what))
        appWidgetBig.notifyChange(this, what)
        appWidgetClassic.notifyChange(this, what)
        appWidgetFull.notifyChange(this, what)
        appWidgetCircle.notifyChange(this, what)
    }

    private fun setCustomAction(stateBuilder: Builder) {
        var repeatIcon = R.drawable.ic_repeat // REPEAT_MODE_NONE
        if (repeatMode == REPEAT_MODE_THIS) {
            repeatIcon = R.drawable.ic_repeat_one
        } else if (repeatMode == REPEAT_MODE_ALL) {
            repeatIcon = R.drawable.ic_repeat_white_circle
        }

        val shuffleIcon =
            if (getShuffleMode() == SHUFFLE_MODE_NONE) R.drawable.ic_shuffle_off_circled else R.drawable.ic_shuffle_on_circled

        val updateIcon = R.drawable.ic_update

        val quitIcon = R.drawable.ic_close

        val clearIcon = R.drawable.ic_clear_all

        val action1 = PreferenceUtil.isAction1
        val action2 = PreferenceUtil.isAction2

        val autoAction1 = PreferenceUtil.isAutoAction1
        val autoAction2 = PreferenceUtil.isAutoAction2

        if (PreferenceUtil.isCarConnected) {
            if (PreferenceUtil.isNotificationActionsOnAuto) {
                if (action1 != "none") {
                    when (action1) {
                        "repeat" -> {
                            stateBuilder.addCustomAction(
                                PlaybackStateCompat.CustomAction.Builder(
                                    CYCLE_REPEAT,
                                    getString(R.string.action_cycle_repeat),
                                    repeatIcon
                                )
                                    .build()
                            )
                        }

                        "shuffle" -> {
                            stateBuilder.addCustomAction(
                                PlaybackStateCompat.CustomAction.Builder(
                                    TOGGLE_SHUFFLE,
                                    getString(R.string.action_toggle_shuffle),
                                    shuffleIcon
                                )
                                    .build()
                            )
                        }

                        "update" -> {
                            stateBuilder.addCustomAction(
                                PlaybackStateCompat.CustomAction.Builder(
                                    UPDATE_NOTIFY, getString(R.string.action_update), updateIcon
                                )
                                    .build()
                            )
                        }

                        "quit" -> {
                            stateBuilder.addCustomAction(
                                PlaybackStateCompat.CustomAction.Builder(
                                    ACTION_QUIT, getString(R.string.close), quitIcon
                                )
                                    .build()
                            )
                        }

                        "clear_queue" -> {
                            stateBuilder.addCustomAction(
                                PlaybackStateCompat.CustomAction.Builder(
                                    QUEUE_CHANGED,
                                    getString(R.string.action_clear_playing_queue),
                                    clearIcon
                                )
                                    .build()
                            )
                        }
                    }
                }

                if (action2 != "none") {
                    when (action2) {
                        "repeat" -> {
                            stateBuilder.addCustomAction(
                                PlaybackStateCompat.CustomAction.Builder(
                                    CYCLE_REPEAT,
                                    getString(R.string.action_cycle_repeat),
                                    repeatIcon
                                )
                                    .build()
                            )
                        }

                        "shuffle" -> {
                            stateBuilder.addCustomAction(
                                PlaybackStateCompat.CustomAction.Builder(
                                    TOGGLE_SHUFFLE,
                                    getString(R.string.action_toggle_shuffle),
                                    shuffleIcon
                                )
                                    .build()
                            )
                        }

                        "update" -> {
                            stateBuilder.addCustomAction(
                                PlaybackStateCompat.CustomAction.Builder(
                                    UPDATE_NOTIFY, getString(R.string.action_update), updateIcon
                                )
                                    .build()
                            )
                        }

                        "quit" -> {
                            stateBuilder.addCustomAction(
                                PlaybackStateCompat.CustomAction.Builder(
                                    ACTION_QUIT, getString(R.string.close), quitIcon
                                )
                                    .build()
                            )
                        }

                        "clear_queue" -> {
                            stateBuilder.addCustomAction(
                                PlaybackStateCompat.CustomAction.Builder(
                                    QUEUE_CHANGED,
                                    getString(R.string.action_clear_playing_queue),
                                    clearIcon
                                )
                                    .build()
                            )
                        }
                    }
                }
            } else {
                if (autoAction1 != "none") {
                    when (autoAction1) {
                        "repeat" -> {
                            stateBuilder.addCustomAction(
                                PlaybackStateCompat.CustomAction.Builder(
                                    CYCLE_REPEAT,
                                    getString(R.string.action_cycle_repeat),
                                    repeatIcon
                                )
                                    .build()
                            )
                        }

                        "shuffle" -> {
                            stateBuilder.addCustomAction(
                                PlaybackStateCompat.CustomAction.Builder(
                                    TOGGLE_SHUFFLE,
                                    getString(R.string.action_toggle_shuffle),
                                    shuffleIcon
                                )
                                    .build()
                            )
                        }

                        "clear_queue" -> {
                            stateBuilder.addCustomAction(
                                PlaybackStateCompat.CustomAction.Builder(
                                    QUEUE_CHANGED,
                                    getString(R.string.action_clear_playing_queue),
                                    clearIcon
                                )
                                    .build()
                            )
                        }
                    }
                }
                if (autoAction2 != "none") {
                    when (autoAction2) {
                        "repeat" -> {
                            stateBuilder.addCustomAction(
                                PlaybackStateCompat.CustomAction.Builder(
                                    CYCLE_REPEAT,
                                    getString(R.string.action_cycle_repeat),
                                    repeatIcon
                                )
                                    .build()
                            )
                        }

                        "shuffle" -> {
                            stateBuilder.addCustomAction(
                                PlaybackStateCompat.CustomAction.Builder(
                                    TOGGLE_SHUFFLE,
                                    getString(R.string.action_toggle_shuffle),
                                    shuffleIcon
                                )
                                    .build()
                            )
                        }

                        "clear_queue" -> {
                            stateBuilder.addCustomAction(
                                PlaybackStateCompat.CustomAction.Builder(
                                    QUEUE_CHANGED,
                                    getString(R.string.action_clear_playing_queue),
                                    clearIcon
                                )
                                    .build()
                            )
                        }
                    }
                }
            }
        } else {
            if (action1 != "none") {
                when (action1) {
                    "repeat" -> {
                        stateBuilder.addCustomAction(
                            PlaybackStateCompat.CustomAction.Builder(
                                CYCLE_REPEAT, getString(R.string.action_cycle_repeat), repeatIcon
                            )
                                .build()
                        )
                    }

                    "shuffle" -> {
                        stateBuilder.addCustomAction(
                            PlaybackStateCompat.CustomAction.Builder(
                                TOGGLE_SHUFFLE,
                                getString(R.string.action_toggle_shuffle),
                                shuffleIcon
                            )
                                .build()
                        )
                    }

                    "update" -> {
                        stateBuilder.addCustomAction(
                            PlaybackStateCompat.CustomAction.Builder(
                                UPDATE_NOTIFY, getString(R.string.action_update), updateIcon
                            )
                                .build()
                        )
                    }

                    "quit" -> {
                        stateBuilder.addCustomAction(
                            PlaybackStateCompat.CustomAction.Builder(
                                ACTION_QUIT, getString(R.string.close), quitIcon
                            )
                                .build()
                        )
                    }

                    "clear_queue" -> {
                        stateBuilder.addCustomAction(
                            PlaybackStateCompat.CustomAction.Builder(
                                QUEUE_CHANGED,
                                getString(R.string.action_clear_playing_queue),
                                clearIcon
                            )
                                .build()
                        )
                    }
                }
            }

            if (action2 != "none") {
                when (action2) {
                    "repeat" -> {
                        stateBuilder.addCustomAction(
                            PlaybackStateCompat.CustomAction.Builder(
                                CYCLE_REPEAT, getString(R.string.action_cycle_repeat), repeatIcon
                            )
                                .build()
                        )
                    }

                    "shuffle" -> {
                        stateBuilder.addCustomAction(
                            PlaybackStateCompat.CustomAction.Builder(
                                TOGGLE_SHUFFLE,
                                getString(R.string.action_toggle_shuffle),
                                shuffleIcon
                            )
                                .build()
                        )
                    }

                    "update" -> {
                        stateBuilder.addCustomAction(
                            PlaybackStateCompat.CustomAction.Builder(
                                UPDATE_NOTIFY, getString(R.string.action_update), updateIcon
                            )
                                .build()
                        )
                    }

                    "quit" -> {
                        stateBuilder.addCustomAction(
                            PlaybackStateCompat.CustomAction.Builder(
                                ACTION_QUIT, getString(R.string.close), quitIcon
                            )
                                .build()
                        )
                    }

                    "clear_queue" -> {
                        stateBuilder.addCustomAction(
                            PlaybackStateCompat.CustomAction.Builder(
                                QUEUE_CHANGED,
                                getString(R.string.action_clear_playing_queue),
                                clearIcon
                            )
                                .build()
                        )
                    }
                }
            }
        }
    }

    fun updateWidget() {
        val hideTimelineRunnable = Runnable {
            if (playingQueue.isNotEmpty()) {
                appWidgetBig.performUpdate(this@MusicService, null)
                appWidgetClassic.performUpdate(this@MusicService, null)
                appWidgetFull.performUpdate(this@MusicService, null)
            }
        }
        scheduler.scheduleWithFixedDelay(hideTimelineRunnable, 0, 1000, TimeUnit.MILLISECONDS)
    }

    private fun setupMediaSession() {
        val mediaButtonReceiverComponentName = ComponentName(
            applicationContext,
            MediaButtonIntentReceiver::class.java
        )

        val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
        mediaButtonIntent.component = mediaButtonReceiverComponentName
        val mediaButtonReceiverPendingIntent = PendingIntent.getBroadcast(
            applicationContext, 0, mediaButtonIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        mediaSession = MediaSessionCompat(
            baseContext,
            BuildConfig.APPLICATION_ID,
            mediaButtonReceiverComponentName,
            mediaButtonReceiverPendingIntent
        )
        val mediaSessionCallback = MediaSessionCallback(this)
        mediaSession?.setCallback(mediaSessionCallback)
        mediaSession?.isActive = true
        mediaSession?.setMediaButtonReceiver(mediaButtonReceiverPendingIntent)
    }

    inner class MusicBinder : Binder() {
        val service: MusicService
            get() = this@MusicService
    }

    companion object {
        val TAG: String = MusicService::class.java.simpleName
        const val APEX_MUSIC_PACKAGE_NAME = "com.ttop.app.apex"
        const val MUSIC_PACKAGE_NAME = "com.android.music"
        const val ACTION_TOGGLE_PAUSE = "$APEX_MUSIC_PACKAGE_NAME.togglepause"
        const val ACTION_PLAY = "$APEX_MUSIC_PACKAGE_NAME.play"
        const val ACTION_PLAY_PLAYLIST = "$APEX_MUSIC_PACKAGE_NAME.play.playlist"
        const val ACTION_PAUSE = "$APEX_MUSIC_PACKAGE_NAME.pause"
        const val ACTION_STOP = "$APEX_MUSIC_PACKAGE_NAME.stop"
        const val ACTION_SKIP = "$APEX_MUSIC_PACKAGE_NAME.skip"
        const val ACTION_REWIND = "$APEX_MUSIC_PACKAGE_NAME.rewind"
        const val ACTION_QUIT = "$APEX_MUSIC_PACKAGE_NAME.quitservice"
        const val ACTION_PENDING_QUIT = "$APEX_MUSIC_PACKAGE_NAME.pendingquitservice"
        const val INTENT_EXTRA_PLAYLIST = APEX_MUSIC_PACKAGE_NAME + "intentextra.playlist"
        const val INTENT_EXTRA_SHUFFLE_MODE = "$APEX_MUSIC_PACKAGE_NAME.intentextra.shufflemode"
        const val APP_WIDGET_UPDATE = "$APEX_MUSIC_PACKAGE_NAME.appwidgetupdate"
        const val EXTRA_APP_WIDGET_NAME = APEX_MUSIC_PACKAGE_NAME + "app_widget_name"

        // Do not change these three strings as it will break support with other apps (e.g. last.fm
        // scrobbling)
        const val META_CHANGED = "$APEX_MUSIC_PACKAGE_NAME.metachanged"
        const val QUEUE_CHANGED = "$APEX_MUSIC_PACKAGE_NAME.queuechanged"
        const val PLAY_STATE_CHANGED = "$APEX_MUSIC_PACKAGE_NAME.playstatechanged"
        const val FAVORITE_STATE_CHANGED = "$APEX_MUSIC_PACKAGE_NAME.favoritestatechanged"
        const val REPEAT_MODE_CHANGED = "$APEX_MUSIC_PACKAGE_NAME.repeatmodechanged"
        const val SHUFFLE_MODE_CHANGED = "$APEX_MUSIC_PACKAGE_NAME.shufflemodechanged"
        const val MEDIA_STORE_CHANGED = "$APEX_MUSIC_PACKAGE_NAME.mediastorechanged"
        const val CYCLE_REPEAT = "$APEX_MUSIC_PACKAGE_NAME.cyclerepeat"
        const val TOGGLE_SHUFFLE = "$APEX_MUSIC_PACKAGE_NAME.toggleshuffle"
        const val TOGGLE_FAVORITE = "$APEX_MUSIC_PACKAGE_NAME.togglefavorite"
        const val UPDATE_NOTIFY = "$APEX_MUSIC_PACKAGE_NAME.updatenotify"
        const val ACTION_UPDATE = "$APEX_MUSIC_PACKAGE_NAME.updateWidgets"
        const val SAVED_POSITION = "POSITION"
        const val SAVED_POSITION_IN_TRACK = "POSITION_IN_TRACK"
        const val SAVED_SHUFFLE_MODE = "SHUFFLE_MODE"
        const val SAVED_REPEAT_MODE = "REPEAT_MODE"
        const val SHUFFLE_MODE_NONE = 0
        const val SHUFFLE_MODE_SHUFFLE = 1
        const val REPEAT_MODE_NONE = 0
        const val REPEAT_MODE_ALL = 1
        const val REPEAT_MODE_THIS = 2

        private const val MEDIA_SESSION_ACTIONS = (PlaybackStateCompat.ACTION_PLAY
                or PlaybackStateCompat.ACTION_PAUSE
                or PlaybackStateCompat.ACTION_PLAY_PAUSE
                or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                or PlaybackStateCompat.ACTION_STOP
                or PlaybackStateCompat.ACTION_SEEK_TO)
    }
}