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
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import com.bumptech.glide.Glide
import com.r0adkll.slidr.Slidr
import com.r0adkll.slidr.model.SlidrConfig
import com.r0adkll.slidr.model.SlidrListener
import com.r0adkll.slidr.model.SlidrPosition
import com.ttop.app.apex.BuildConfig
import com.ttop.app.apex.R
import com.ttop.app.apex.databinding.ActivityLockScreenBinding
import com.ttop.app.apex.extensions.setTaskDescriptionColorAuto
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.extensions.whichFragment
import com.ttop.app.apex.glide.ApexColoredTarget
import com.ttop.app.apex.glide.ApexGlideExtension
import com.ttop.app.apex.glide.ApexGlideExtension.asBitmapPalette
import com.ttop.app.apex.glide.ApexGlideExtension.songCoverOptions
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.ui.activities.base.AbsMusicServiceActivity
import com.ttop.app.apex.ui.fragments.player.lockscreen.LockScreenControlsFragment
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.color.MediaNotificationProcessor
import com.ttop.app.appthemehelper.util.VersionUtils


class LockScreenActivity : AbsMusicServiceActivity() {
    private lateinit var binding: ActivityLockScreenBinding
    private var fragment: LockScreenControlsFragment? = null

    val handler = Handler(Looper.getMainLooper())
    private var isLooping = true
    private val runnable: Runnable = Runnable {
        if (isLooping) {
            startLooping()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lockScreenInit()
        binding = ActivityLockScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setTaskDescriptionColorAuto()

        val config = SlidrConfig.Builder().listener(object : SlidrListener {
            override fun onSlideStateChanged(state: Int) {
            }

            override fun onSlideChange(percent: Float) {
            }

            override fun onSlideOpened() {
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onSlideClosed(): Boolean {
                val keyguardManager =
                    getSystemService<KeyguardManager>()
                keyguardManager?.requestDismissKeyguard(this@LockScreenActivity, null)
                finish()
                return true
            }
        }).position(SlidrPosition.BOTTOM).build()

        Slidr.attach(this, config)

        fragment = whichFragment<LockScreenControlsFragment>(R.id.playback_controls_fragment)

        if (ApexUtil.checkForBiometrics(this)) {
            if (BuildConfig.DEBUG) {
                showToast(R.string.biometrics)
            }
            if (isLooping) {
                handler.postDelayed(runnable,1000)
            }
        }

        binding.music.setOnClickListener {
            val i = Intent(this, MainActivity::class.java)
            finish() //Kill the activity from which you will go to next activity
            startActivity(i)
        }
    }

    private fun startLooping() {
        val keyguardManager =
            getSystemService<KeyguardManager>()
        if (keyguardManager?.isDeviceLocked == false) {
            isLooping = false
            finish()
        }
        handler.postDelayed(runnable, 1000)
    }

    @Suppress("Deprecation")
    private fun lockScreenInit() {
        if (VersionUtils.hasOreoMR1()) {
            setShowWhenLocked(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            )
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
        Glide.with(this)
            .asBitmapPalette()
            .songCoverOptions(song)
            .load(ApexGlideExtension.getSongModel(song))
            .dontAnimate()
            .into(object : ApexColoredTarget(binding.image) {
                override fun onColorReady(colors: MediaNotificationProcessor) {
                    fragment?.setColor(colors)
                }
            }
        )
    }
}