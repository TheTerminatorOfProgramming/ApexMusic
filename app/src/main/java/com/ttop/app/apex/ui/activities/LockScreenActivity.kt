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

import android.app.KeyguardManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.core.content.getSystemService
import com.ttop.app.appthemehelper.util.VersionUtils
import com.ttop.app.apex.R
import com.ttop.app.apex.ui.activities.base.AbsMusicServiceActivity
import com.ttop.app.apex.databinding.ActivityLockScreenBinding
import com.ttop.app.apex.extensions.hideStatusBar
import com.ttop.app.apex.extensions.setTaskDescriptionColorAuto
import com.ttop.app.apex.extensions.whichFragment
import com.ttop.app.apex.ui.fragments.player.lockscreen.LockScreenControlsFragment
import com.ttop.app.apex.glide.GlideApp
import com.ttop.app.apex.glide.ApexGlideExtension
import com.ttop.app.apex.glide.ApexMusicColoredTarget
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.util.color.MediaNotificationProcessor
import com.r0adkll.slidr.Slidr
import com.r0adkll.slidr.model.SlidrConfig
import com.r0adkll.slidr.model.SlidrListener
import com.r0adkll.slidr.model.SlidrPosition

class LockScreenActivity : AbsMusicServiceActivity() {
    private lateinit var binding: ActivityLockScreenBinding
    private var fragment: LockScreenControlsFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lockScreenInit()
        binding = ActivityLockScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideStatusBar()
        setTaskDescriptionColorAuto()

        val config = SlidrConfig.Builder().listener(object : SlidrListener {
            override fun onSlideStateChanged(state: Int) {
            }

            override fun onSlideChange(percent: Float) {
            }

            override fun onSlideOpened() {
            }

            override fun onSlideClosed(): Boolean {
                if (VersionUtils.hasOreo()) {
                    val keyguardManager =
                        getSystemService<KeyguardManager>()
                    keyguardManager?.requestDismissKeyguard(this@LockScreenActivity, null)
                }
                finish()
                return true
            }
        }).position(SlidrPosition.BOTTOM).build()

        Slidr.attach(this, config)

        fragment = whichFragment<LockScreenControlsFragment>(R.id.playback_controls_fragment)

        binding.slide.apply {
            translationY = 100f
            alpha = 0f
            animate().translationY(0f).alpha(1f).setDuration(1500).start()
        }
    }

    @Suppress("Deprecation")
    private fun lockScreenInit() {
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true)
        } else {
            this.window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        }
    }

    override fun onPlayingMetaChanged() {
        super.onPlayingMetaChanged()
        updateSongs()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        updateSongs()
    }

    private fun updateSongs() {
        val song = MusicPlayerRemote.currentSong
        GlideApp.with(this)
            .asBitmapPalette()
            .songCoverOptions(song)
            .load(ApexGlideExtension.getSongModel(song))
            .dontAnimate()
            .into(object : ApexMusicColoredTarget(binding.image) {
                override fun onColorReady(colors: MediaNotificationProcessor) {
                    fragment?.setColor(colors)
                }
            })
    }
}