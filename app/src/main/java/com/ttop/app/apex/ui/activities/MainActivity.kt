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
package com.ttop.app.apex.ui.activities

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.lifecycle.lifecycleScope
import androidx.navigation.contains
import androidx.navigation.ui.setupWithNavController
import com.ttop.app.apex.R
import com.ttop.app.apex.appwidgets.AppWidgetBig
import com.ttop.app.apex.appwidgets.AppWidgetCircle
import com.ttop.app.apex.appwidgets.AppWidgetClassic
import com.ttop.app.apex.appwidgets.AppWidgetFull
import com.ttop.app.apex.extensions.currentFragment
import com.ttop.app.apex.extensions.extra
import com.ttop.app.apex.extensions.findNavController
import com.ttop.app.apex.extensions.hideStatusBar
import com.ttop.app.apex.extensions.setTaskDescriptionColorAuto
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.helper.SearchQueryHelper.getSongs
import com.ttop.app.apex.interfaces.IScrollHelper
import com.ttop.app.apex.model.CategoryInfo
import com.ttop.app.apex.model.Song
import com.ttop.app.apex.repository.PlaylistSongsLoader
import com.ttop.app.apex.service.MusicService
import com.ttop.app.apex.ui.activities.base.AbsCastActivity
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.logE
import com.ttop.app.appthemehelper.util.VersionUtils
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get


class MainActivity : AbsCastActivity() {
    companion object {
        const val TAG = "MainActivity"
        const val EXPAND_PANEL = "expand_panel"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTaskDescriptionColorAuto()
        hideStatusBar()
        updateTabs()

        PreferenceUtil.shouldRecreate = false

        setupNavigationController()

        WhatsNewFragment.showChangeLog(this)

        requestedOrientation = if (ApexUtil.isTablet) {
            if (PreferenceUtil.isAutoRotate) {
                ActivityInfo.SCREEN_ORIENTATION_SENSOR
            } else {
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }

        if (PreferenceUtil.isDisableWidgets) {
            //Big Widget
            packageManager.setComponentEnabledSetting(
                ComponentName(
                    applicationContext,
                    AppWidgetBig::class.java
                ), COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
            )
            //Classic Widget
            packageManager.setComponentEnabledSetting(
                ComponentName(
                    applicationContext,
                    AppWidgetClassic::class.java
                ), COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
            )
            //Circle Widget
            packageManager.setComponentEnabledSetting(
                ComponentName(
                    applicationContext,
                    AppWidgetCircle::class.java
                ), COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
            )
            //Full Widget
            packageManager.setComponentEnabledSetting(
                ComponentName(
                    applicationContext,
                    AppWidgetFull::class.java
                ), COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
            )
        }

        PreferenceUtil.isInternetConnected = ApexUtil.isNetworkAvailable(applicationContext)

        //UN-CODE IF DISTRIBUTING TO USER TO TEST. TIME IS 24 HOUR
        //val validUntil = "7/2/2024 0000"
        //val sdf = SimpleDateFormat("dd/MM/yyyy HHmm", Locale.getDefault())
        //val strDate: Date? = sdf.parse(validUntil)
        //if (Date().after(strDate)) {
        //    val id = Process.myPid()
        //    Process.killProcess(id)
        //}
    }

    private fun setupNavigationController() {
        val navController = findNavController(R.id.fragment_container)
        val navInflater = navController.navInflater
        val navGraph = navInflater.inflate(R.navigation.main_graph)

        val categoryInfo: CategoryInfo = PreferenceUtil.libraryCategory.first { it.visible }
        if (categoryInfo.visible) {
            if (!navGraph.contains(PreferenceUtil.lastTab)) PreferenceUtil.lastTab =
                categoryInfo.category.id
            navGraph.setStartDestination(
                if (PreferenceUtil.rememberLastTab) {
                    PreferenceUtil.lastTab.let {
                        if (it == 0) {
                            categoryInfo.category.id
                        } else {
                            it
                        }
                    }
                } else categoryInfo.category.id
            )
        }
        navController.graph = navGraph
        navigationView.setupWithNavController(navController)
        // Scroll Fragment to top
        navigationView.setOnItemReselectedListener {
            currentFragment(R.id.fragment_container).apply {
                if (this is IScrollHelper) {
                    scrollToTop()
                }
            }
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == navGraph.startDestinationId) {
                currentFragment(R.id.fragment_container)?.enterTransition = null
            }
            when (destination.id) {
                R.id.action_home, R.id.action_song, R.id.action_album, R.id.action_artist, R.id.action_folder, R.id.action_playlist, R.id.action_genre, R.id.action_search -> {
                    // Save the last tab
                    if (PreferenceUtil.rememberLastTab) {
                        saveTab(destination.id)
                    }
                    // Show Bottom Navigation Bar
                    setBottomNavVisibility(visible = true, animate = true)
                }
                R.id.playing_queue_fragment -> {
                    setBottomNavVisibility(visible = true, hideBottomSheet = true)
                }
                R.id.action_settings_fragment -> {
                    setBottomNavVisibility(
                        visible = true,
                        animate = true
                    )
                }
                else -> setBottomNavVisibility(
                    visible = false,
                    animate = true
                ) // Hide Bottom Navigation Bar
            }
        }
    }

    private fun saveTab(id: Int) {
        if (PreferenceUtil.libraryCategory.firstOrNull { it.category.id == id }?.visible == true) {
            PreferenceUtil.lastTab = id
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val expand = if (PreferenceUtil.isExpandPanel == "enhanced") {
            intent?.getBooleanExtra(EXPAND_PANEL, PreferenceUtil.isExpandPanel == "enhanced")
        }else {
            intent?.extra<Boolean>(EXPAND_PANEL)?.value ?: false
        }

        if (expand == true && PreferenceUtil.isExpandPanel != "disabled") {
            if (MusicPlayerRemote.playingQueue.isNotEmpty()) {
                fromNotification = true
                if (ApexUtil.isTablet) {
                    PreferenceUtil.isWidgetPanel = true
                }
                slidingPanel.bringToFront()
                expandPanel()
                intent?.removeExtra(EXPAND_PANEL)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (PreferenceUtil.shouldRecreate) {
            PreferenceUtil.shouldRecreate = false
            postRecreate()
        }

        if (PreferenceUtil.shouldRecreateTabs) {
            PreferenceUtil.shouldRecreateTabs = false
            refreshTabs()
            val intent = intent
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            if (VersionUtils.hasUpsideDownCake()) {
                finish()
                overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
                startActivity(intent)
                overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0)
            }else {
                finish()
                overridePendingTransition(0, 0)
                startActivity(intent)
                overridePendingTransition(0, 0)
            }
        }

        requestedOrientation = if (ApexUtil.isTablet) {
            if (PreferenceUtil.isAutoRotate) {
                ActivityInfo.SCREEN_ORIENTATION_SENSOR
            }else {
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        intent ?: return
        handlePlaybackIntent(intent)
    }

    private fun handlePlaybackIntent(intent: Intent) {
        lifecycleScope.launch(IO) {
            val uri: Uri? = intent.data
            val mimeType: String? = intent.type
            var handled = false
            if (intent.action != null &&
                intent.action == MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH
            ) {
                val songs: List<Song> = getSongs(intent.extras!!)
                if (MusicPlayerRemote.shuffleMode == MusicService.SHUFFLE_MODE_SHUFFLE) {
                    MusicPlayerRemote.openAndShuffleQueue(songs, true)
                } else {
                    MusicPlayerRemote.openQueue(songs, 0, true)
                }
                handled = true
            }
            if (uri != null && uri.toString().isNotEmpty()) {
                MusicPlayerRemote.playFromUri(this@MainActivity, uri)
                handled = true
            } else if (MediaStore.Audio.Playlists.CONTENT_TYPE == mimeType) {
                val id = parseLongFromIntent(intent, "playlistId", "playlist")
                if (id >= 0L) {
                    val position: Int = intent.getIntExtra("position", 0)
                    val songs: List<Song> = PlaylistSongsLoader.getPlaylistSongList(get(), id)
                    MusicPlayerRemote.openQueue(songs, position, true)
                    handled = true
                }
            } else if (MediaStore.Audio.Albums.CONTENT_TYPE == mimeType) {
                val id = parseLongFromIntent(intent, "albumId", "album")
                if (id >= 0L) {
                    val position: Int = intent.getIntExtra("position", 0)
                    val songs = libraryViewModel.albumById(id).songs
                    MusicPlayerRemote.openQueue(
                        songs,
                        position,
                        true
                    )
                    handled = true
                }
            } else if (MediaStore.Audio.Artists.CONTENT_TYPE == mimeType) {
                val id = parseLongFromIntent(intent, "artistId", "artist")
                if (id >= 0L) {
                    val position: Int = intent.getIntExtra("position", 0)
                    val songs: List<Song> = libraryViewModel.artistById(id).songs
                    MusicPlayerRemote.openQueue(
                        songs,
                        position,
                        true
                    )
                    handled = true
                }
            }
            if (handled) {
                setIntent(Intent())
            }
        }
    }

    private fun parseLongFromIntent(
        intent: Intent,
        longKey: String,
        stringKey: String,
    ): Long {
        var id = intent.getLongExtra(longKey, -1)
        if (id < 0) {
            val idString = intent.getStringExtra(stringKey)
            if (idString != null) {
                try {
                    id = idString.toLong()
                } catch (e: NumberFormatException) {
                    logE(e)
                }
            }
        }
        return id
    }
}
