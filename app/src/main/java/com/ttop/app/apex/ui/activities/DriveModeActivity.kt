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

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import androidx.lifecycle.lifecycleScope
import com.google.android.material.slider.Slider
import com.ttop.app.apex.R
import com.ttop.app.apex.databinding.ActivityDriveModeBinding
import com.ttop.app.apex.db.toSongEntity
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.extensions.drawAboveSystemBars
import com.ttop.app.apex.glide.ApexGlideExtension
import com.ttop.app.apex.glide.BlurTransformation
import com.bumptech.glide.Glide
import com.ttop.app.apex.dialogs.PlaybackSpeedDialog
import com.ttop.app.apex.dialogs.SleepTimerDialog
import com.ttop.app.apex.extensions.applyColor
import com.ttop.app.apex.glide.ApexGlideExtension.songCoverOptions
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.helper.MusicProgressViewUpdateHelper
import com.ttop.app.apex.helper.MusicProgressViewUpdateHelper.Callback
import com.ttop.app.apex.helper.PlayPauseButtonOnClickHandler
import com.ttop.app.apex.model.Song
import com.ttop.app.apex.repository.RealRepository
import com.ttop.app.apex.service.MusicService
import com.ttop.app.apex.ui.activities.base.AbsMusicServiceActivity
import com.ttop.app.apex.ui.fragments.base.AbsPlayerControlsFragment
import com.ttop.app.apex.util.MusicUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.views.SquigglyProgress
import com.ttop.app.appthemehelper.util.VersionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject


/**
 * Created by hemanths on 2020-02-02.
 */

class DriveModeActivity : AbsMusicServiceActivity(), Callback {

    private lateinit var binding: ActivityDriveModeBinding
    private var lastPlaybackControlsColor: Int = Color.GRAY
    private var lastDisabledPlaybackControlsColor: Int = Color.GRAY
    private lateinit var progressViewUpdateHelper: MusicProgressViewUpdateHelper
    private val repository: RealRepository by inject()

    private var progressAnimator: ObjectAnimator? = null
    var isSeeking = false

    val seekBar: SeekBar
        get() = binding.progressSlider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDriveModeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpMusicControllers()

        progressViewUpdateHelper = MusicProgressViewUpdateHelper(this)
        lastPlaybackControlsColor = accentColor()
        binding.close.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.repeatButton.drawAboveSystemBars()

        binding.songTitle.isSelected = true
        binding.album.isSelected = true
        binding.artist.isSelected = true
    }

    private fun setUpMusicControllers() {
        setUpPlayPauseFab()
        setUpPrevNext()
        setUpRepeatButton()
        setUpShuffleButton()
        setUpProgressSlider()
        setupFavouriteToggle()
        setupTabletMode()
    }

    private fun setupFavouriteToggle() {
        binding.songFavourite.setOnClickListener {
            toggleFavorite(MusicPlayerRemote.currentSong)
        }
    }

    private fun toggleFavorite(song: Song) {
        lifecycleScope.launch(Dispatchers.IO) {
            val playlist = repository.favoritePlaylist()
            val songEntity = song.toSongEntity(playlist.playListId)
            val isFavorite = repository.isSongFavorite(song.id)
            if (isFavorite) {
                repository.removeSongFromPlaylist(songEntity)
            } else {
                repository.insertSongs(listOf(song.toSongEntity(playlist.playListId)))
            }
            sendBroadcast(Intent(MusicService.FAVORITE_STATE_CHANGED))
        }
    }

    private fun updateFavorite() {
        lifecycleScope.launch(Dispatchers.IO) {
            val isFavorite: Boolean =
                repository.isSongFavorite(MusicPlayerRemote.currentSong.id)
            withContext(Dispatchers.Main) {
                binding.songFavourite.setImageResource(if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border)
            }
        }
    }

    private fun setUpProgressSlider() {
        binding.progressSlider.applyColor(accentColor())

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                onProgressChange(progress, fromUser)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                onStartTrackingTouch()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                onStopTrackingTouch(seekBar?.progress ?: 0)
            }
        })
    }

    private fun onProgressChange(value: Int, fromUser: Boolean) {
        if (fromUser) {
            onUpdateProgressViews(value, MusicPlayerRemote.songDurationMillis)
        }
    }

    private fun onStartTrackingTouch() {
        isSeeking = true
        progressViewUpdateHelper.stop()
        progressAnimator?.cancel()
    }

    private fun onStopTrackingTouch(value: Int) {
        isSeeking = false
        MusicPlayerRemote.seekTo(value)
        progressViewUpdateHelper.start()
    }

    override fun onPause() {
        super.onPause()
        progressViewUpdateHelper.stop()
    }

    override fun onResume() {
        super.onResume()
        progressViewUpdateHelper.start()
    }

    private fun setUpPrevNext() {
        binding.nextButton.setOnClickListener {
            if (PreferenceUtil.isAutoplay) {
                MusicPlayerRemote.playNextSongAuto(MusicPlayerRemote.isPlaying)
            } else {
                MusicPlayerRemote.playNextSong()
            }
        }
        binding.previousButton.setOnClickListener {
            if (PreferenceUtil.isAutoplay) {
                MusicPlayerRemote.playPreviousSongAuto(MusicPlayerRemote.isPlaying)
            }else {
                MusicPlayerRemote.playPreviousSong()
            }
        }
    }

    private fun setUpShuffleButton() {
        binding.shuffleButton.setOnClickListener { MusicPlayerRemote.toggleShuffleMode() }
    }

    private fun setUpRepeatButton() {
        binding.repeatButton.setOnClickListener { MusicPlayerRemote.cycleRepeatMode() }
    }

    private fun setUpPlayPauseFab() {
        binding.playPauseButton.setOnClickListener(PlayPauseButtonOnClickHandler())
    }

    private fun setupTabletMode() {
        binding.playbackButton?.setOnClickListener{
            PlaybackSpeedDialog.newInstance().show(supportFragmentManager, "PLAYBACK_SETTINGS")
        }

        binding.timerButton?.setOnClickListener{
            SleepTimerDialog().show(supportFragmentManager, "SLEEP_TIMER")
        }
    }

    override fun onRepeatModeChanged() {
        super.onRepeatModeChanged()
        updateRepeatState()
    }

    override fun onShuffleModeChanged() {
        super.onShuffleModeChanged()
        updateShuffleState()
    }

    override fun onPlayStateChanged() {
        super.onPlayStateChanged()
        updatePlayPauseDrawableState()
        (seekBar.progressDrawable as? SquigglyProgress)?.animate = MusicPlayerRemote.isPlaying
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        updatePlayPauseDrawableState()
        updateSong()
        updateRepeatState()
        updateShuffleState()
        updateFavorite()
        (seekBar.progressDrawable as? SquigglyProgress)?.animate = MusicPlayerRemote.isPlaying
    }

    private fun updatePlayPauseDrawableState() {
        if (MusicPlayerRemote.isPlaying) {
            binding.playPauseButton.setImageResource(R.drawable.ic_pause)
        } else {
            binding.playPauseButton.setImageResource(R.drawable.ic_play_arrow)
        }
    }

    fun updateShuffleState() {
        when (MusicPlayerRemote.shuffleMode) {
            MusicService.SHUFFLE_MODE_SHUFFLE -> binding.shuffleButton.setColorFilter(
                lastPlaybackControlsColor,
                PorterDuff.Mode.SRC_IN
            )
            else -> binding.shuffleButton.setColorFilter(
                lastDisabledPlaybackControlsColor,
                PorterDuff.Mode.SRC_IN
            )
        }
    }

    private fun updateRepeatState() {
        when (MusicPlayerRemote.repeatMode) {
            MusicService.REPEAT_MODE_NONE -> {
                binding.repeatButton.setImageResource(R.drawable.ic_repeat)
                binding.repeatButton.setColorFilter(
                    lastDisabledPlaybackControlsColor,
                    PorterDuff.Mode.SRC_IN
                )
            }
            MusicService.REPEAT_MODE_ALL -> {
                binding.repeatButton.setImageResource(R.drawable.ic_repeat)
                binding.repeatButton.setColorFilter(
                    lastPlaybackControlsColor,
                    PorterDuff.Mode.SRC_IN
                )
            }
            MusicService.REPEAT_MODE_THIS -> {
                binding.repeatButton.setImageResource(R.drawable.ic_repeat_one)
                binding.repeatButton.setColorFilter(
                    lastPlaybackControlsColor,
                    PorterDuff.Mode.SRC_IN
                )
            }
        }
    }

    override fun onPlayingMetaChanged() {
        super.onPlayingMetaChanged()
        updateSong()
        updateFavorite()
    }

    override fun onFavoriteStateChanged() {
        super.onFavoriteStateChanged()
        updateFavorite()
    }

    private fun updateSong() {
        val song = MusicPlayerRemote.currentSong

        binding.songTitle.text = song.title
        binding.album.text = song.albumName
        binding.artist.text = song.artistName

        Glide.with(this)
            .load(ApexGlideExtension.getSongModel(song))
            .songCoverOptions(song)
            .transform(BlurTransformation.Builder(this).build())
            .into(binding.image)
    }

    override fun onUpdateProgressViews(progress: Int, total: Int) {
            seekBar.max = total

            if (isSeeking) {
                seekBar.progress = progress
            } else {
                progressAnimator =
                    ObjectAnimator.ofInt(seekBar, "progress", progress).apply {
                        duration = AbsPlayerControlsFragment.SLIDER_ANIMATION_TIME
                        interpolator = LinearInterpolator()
                        start()
                    }
            }
            binding.songTotalTime.text = MusicUtil.getReadableDurationString(total.toLong())
            binding.songCurrentProgress.text = MusicUtil.getReadableDurationString(progress.toLong())

        binding.songTotalTime.text = MusicUtil.getReadableDurationString(total.toLong())
        binding.songCurrentProgress.text = MusicUtil.getReadableDurationString(progress.toLong())
    }
}
