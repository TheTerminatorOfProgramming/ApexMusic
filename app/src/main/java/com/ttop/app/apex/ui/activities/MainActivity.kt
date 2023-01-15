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

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.contains
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.ttop.app.apex.DESATURATED_COLOR
import com.ttop.app.apex.MATERIAL_YOU
import com.ttop.app.apex.R
import com.ttop.app.apex.extensions.*
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


class MainActivity : AbsCastActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
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

        PreferenceUtil.isSamsungSoundPluginInstalled = checkSamsungSoundPlugin()

        requestedOrientation = if (ApexUtil.isTablet) {
            if (PreferenceUtil.isAutoRotate) {
                ActivityInfo.SCREEN_ORIENTATION_SENSOR
            }else {
                if (ApexUtil.isLandscape) {
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                }else {
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
            }
        }else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    private fun checkSamsungSoundPlugin(): Boolean {
        val packages: List<ApplicationInfo>
        val pm: PackageManager = packageManager
        packages = pm.getInstalledApplications(0)
        for (packageInfo in packages) {
            if (packageInfo.packageName == "com.samsung.android.soundassistant")
                return true
        }
        return false
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

    override fun onSupportNavigateUp(): Boolean =
        findNavController(R.id.fragment_container).navigateUp()

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val expand = intent?.extra<Boolean>(EXPAND_PANEL)?.value ?: false
        if (expand && PreferenceUtil.isExpandPanel) {
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when(key) {
            MATERIAL_YOU, DESATURATED_COLOR -> postRecreate()
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
            finish()
            overridePendingTransition(0, 0)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        requestedOrientation = if (ApexUtil.isTablet) {
            if (PreferenceUtil.isAutoRotate) {
                ActivityInfo.SCREEN_ORIENTATION_SENSOR
            }else {
                if (ApexUtil.isLandscape) {
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                }else {
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
            }
        }else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    fun restart() {
        val intent = intent
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        finish()
        overridePendingTransition(0, 0)
        startActivity(intent)
        overridePendingTransition(0, 0)
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
