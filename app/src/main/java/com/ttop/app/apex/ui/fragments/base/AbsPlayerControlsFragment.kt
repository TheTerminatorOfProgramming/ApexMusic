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
package com.ttop.app.apex.ui.fragments.base

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.google.android.material.slider.Slider
import com.ttop.app.apex.R
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.extensions.whichFragment
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.helper.MusicProgressViewUpdateHelper
import com.ttop.app.apex.service.MusicService
import com.ttop.app.apex.ui.fragments.MusicSeekSkipTouchListener
import com.ttop.app.apex.ui.fragments.NowPlayingScreen
import com.ttop.app.apex.ui.fragments.other.VolumeFragment
import com.ttop.app.apex.util.MusicUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.color.MediaNotificationProcessor

/**
 * Created by hemanths on 24/09/17.
 */

abstract class AbsPlayerControlsFragment(@LayoutRes layout: Int) : AbsMusicServiceFragment(layout),
    MusicProgressViewUpdateHelper.Callback {

    protected abstract fun show()

    protected abstract fun hide()

    abstract fun setColor(color: MediaNotificationProcessor)

    var lastPlaybackControlsColor: Int = 0

    var lastDisabledPlaybackControlsColor: Int = 0

    private var isSeeking = false

    open val progressSlider: Slider? = null

    open val seekBar: SeekBar? = null

    abstract val shuffleButton: ImageButton

    abstract val repeatButton: ImageButton

    open val nextButton: ImageButton? = null

    open val previousButton: ImageButton? = null

    open val volUp: ImageButton? = null

    open val volDown: ImageButton? = null

    open val songTotalTime: TextView? = null

    open val songCurrentProgress: TextView? = null

    private var progressAnimator: ObjectAnimator? = null

    override fun onUpdateProgressViews(progress: Int, total: Int) {
        val nps = PreferenceUtil.nowPlayingScreen
        if (nps == NowPlayingScreen.Classic||
            nps == NowPlayingScreen.Peek) {
            progressSlider?.valueTo = total.toFloat()

            progressSlider?.value =
                progress.toFloat().coerceIn(progressSlider?.valueFrom, progressSlider?.valueTo)

            seekBar?.max = total

            if (isSeeking) {
                seekBar?.progress = progress
            } else {
                progressAnimator =
                    ObjectAnimator.ofInt(seekBar, "progress", progress).apply {
                        duration = SLIDER_ANIMATION_TIME
                        interpolator = LinearInterpolator()
                        start()
                    }

            }
        }else {
            if (seekBar == null) {
                progressSlider?.valueTo = total.toFloat()

                progressSlider?.value =
                    progress.toFloat().coerceIn(progressSlider?.valueFrom, progressSlider?.valueTo)
            } else {
                seekBar?.max = total

                if (isSeeking) {
                    seekBar?.progress = progress
                } else {
                    progressAnimator =
                        ObjectAnimator.ofInt(seekBar, "progress", progress).apply {
                            duration = SLIDER_ANIMATION_TIME
                            interpolator = LinearInterpolator()
                            start()
                        }

                }
            }
        }
        songTotalTime?.text = MusicUtil.getReadableDurationString(total.toLong())
        songCurrentProgress?.text = MusicUtil.getReadableDurationString(progress.toLong())
    }

    private fun setUpProgressSlider() {
        progressSlider?.addOnChangeListener(Slider.OnChangeListener { _, value, fromUser ->
            onProgressChange(value.toInt(), fromUser)
        })
        progressSlider?.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                onStartTrackingTouch()
            }

            override fun onStopTrackingTouch(slider: Slider) {
                onStopTrackingTouch(slider.value.toInt())
            }
        })

        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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

    private lateinit var progressViewUpdateHelper: MusicProgressViewUpdateHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressViewUpdateHelper = MusicProgressViewUpdateHelper(this)
    }

    fun View.showBounceAnimation() {
        clearAnimation()
        scaleX = 0.9f
        scaleY = 0.9f
        isVisible = true
        pivotX = (width / 2).toFloat()
        pivotY = (height / 2).toFloat()

        animate().setDuration(200)
            .setInterpolator(DecelerateInterpolator())
            .scaleX(1.1f)
            .scaleY(1.1f)
            .withEndAction {
                animate().setDuration(200)
                    .setInterpolator(AccelerateInterpolator())
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .start()
            }
            .start()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val nps = PreferenceUtil.nowPlayingScreen
        val npsVolButtons = listOf(NowPlayingScreen.Adaptive, NowPlayingScreen.Gradient)

        if (!npsVolButtons.contains(nps)) {
            hideVolumeIfAvailable()
        }
    }

    override fun onStart() {
        super.onStart()
        setUpProgressSlider()
        setUpPrevNext()
        setUpShuffleButton()
        setUpRepeatButton()
        setupVolButtons()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpPrevNext() {
        nextButton?.setOnTouchListener(MusicSeekSkipTouchListener(requireActivity(), true))
        previousButton?.setOnTouchListener(MusicSeekSkipTouchListener(requireActivity(), false))
    }

    private fun setUpShuffleButton() {
        shuffleButton.setOnClickListener { MusicPlayerRemote.toggleShuffleMode() }
    }

    private fun setUpRepeatButton() {
        repeatButton.setOnClickListener { MusicPlayerRemote.cycleRepeatMode() }
    }

    fun updatePrevNextColor() {
        nextButton?.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN)
        previousButton?.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN)
    }

    open fun updateShuffleState() {
        shuffleButton.setColorFilter(
            when (MusicPlayerRemote.shuffleMode) {
                MusicService.SHUFFLE_MODE_SHUFFLE -> lastPlaybackControlsColor
                else -> lastDisabledPlaybackControlsColor
            }, PorterDuff.Mode.SRC_IN
        )
    }

    open fun updateRepeatState() {
        when (MusicPlayerRemote.repeatMode) {
            MusicService.REPEAT_MODE_NONE -> {
                repeatButton.setImageResource(R.drawable.ic_repeat)
                repeatButton.setColorFilter(
                    lastDisabledPlaybackControlsColor,
                    PorterDuff.Mode.SRC_IN
                )
            }
            MusicService.REPEAT_MODE_ALL -> {
                repeatButton.setImageResource(R.drawable.ic_repeat)
                repeatButton.setColorFilter(
                    lastPlaybackControlsColor,
                    PorterDuff.Mode.SRC_IN
                )
            }
            MusicService.REPEAT_MODE_THIS -> {
                repeatButton.setImageResource(R.drawable.ic_repeat_one)
                repeatButton.setColorFilter(
                    lastPlaybackControlsColor,
                    PorterDuff.Mode.SRC_IN
                )
            }
        }
    }

    private fun setupVolButtons() {
        val mAudioManager: AudioManager = context?.getSystemService()!!

        var lastUpClick: Long
        var lastDownClick: Long

        volUp?.setOnClickListener {
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0)

            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

            lastUpClick = System.currentTimeMillis()

            Handler(Looper.getMainLooper()).postDelayed({
                if (System.currentTimeMillis() - lastUpClick >= 500) {
                    val currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                    val maxVolume: Int = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    val currentVolumeTotal = 100 * currentVolume / maxVolume

                    showToast("New Volume: $currentVolumeTotal%")
                }
            }, 500)
        }

        volDown?.setOnClickListener {
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0)

            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

            lastDownClick = System.currentTimeMillis()

            Handler(Looper.getMainLooper()).postDelayed({
                if (System.currentTimeMillis() - lastDownClick >= 500) {
                    val currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                    val maxVolume: Int = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    val currentVolumeTotal = 100 * currentVolume / maxVolume

                    val newVol = getString(R.string.new_volume)
                    showToast("$newVol: $currentVolumeTotal%")
                }
            }, 500)
        }
    }

    protected var volumeFragment: VolumeFragment? = null

    private fun hideVolumeIfAvailable() {
        if (PreferenceUtil.isVolumeControls) {
            childFragmentManager.commit {
                replace<VolumeFragment>(R.id.volumeFragmentContainer)
            }
            childFragmentManager.executePendingTransactions()
        }
        volumeFragment = whichFragment(R.id.volumeFragmentContainer)
    }

    override fun onResume() {
        super.onResume()
        progressViewUpdateHelper.start()
    }

    override fun onPause() {
        super.onPause()
        progressViewUpdateHelper.stop()
    }

    companion object {
        const val SLIDER_ANIMATION_TIME: Long = 400
    }
}
