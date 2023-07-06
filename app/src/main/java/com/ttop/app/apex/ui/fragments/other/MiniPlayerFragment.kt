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
package com.ttop.app.apex.ui.fragments.other

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.text.toSpannable
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ttop.app.apex.R
import com.ttop.app.apex.databinding.FragmentMiniPlayerBinding
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.extensions.show
import com.ttop.app.apex.extensions.textColorPrimary
import com.ttop.app.apex.extensions.textColorSecondary
import com.ttop.app.apex.glide.ApexGlideExtension
import com.ttop.app.apex.glide.ApexGlideExtension.songCoverOptions
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.helper.MusicProgressViewUpdateHelper
import com.ttop.app.apex.helper.PlayPauseButtonOnClickHandler
import com.ttop.app.apex.ui.fragments.base.AbsMusicServiceFragment
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.appthemehelper.ThemeStore
import com.ttop.app.appthemehelper.util.VersionUtils
import kotlin.math.abs

open class MiniPlayerFragment : AbsMusicServiceFragment(R.layout.fragment_mini_player),
    MusicProgressViewUpdateHelper.Callback, View.OnClickListener {

    private var _binding: FragmentMiniPlayerBinding? = null
    private val binding get() = _binding!!
    private lateinit var progressViewUpdateHelper: MusicProgressViewUpdateHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressViewUpdateHelper = MusicProgressViewUpdateHelper(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.actionNext ->  {
                if (PreferenceUtil.isAutoplay) {
                    MusicPlayerRemote.playNextSongAuto(MusicPlayerRemote.isPlaying)
                } else {
                    MusicPlayerRemote.playNextSong()
                }
            }
            R.id.actionPrevious ->  {
                if (PreferenceUtil.isAutoplay) {
                    MusicPlayerRemote.playPreviousSongAuto(MusicPlayerRemote.isPlaying)
                }else {
                    MusicPlayerRemote.playPreviousSong()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMiniPlayerBinding.bind(view)

        binding.imageTextContainer.radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, PreferenceUtil.miniImage.toFloat(), context?.resources?.displayMetrics)

        if (PreferenceUtil.progressBarStyle){
            if (PreferenceUtil.progressBarAlignment){
                binding.progressBar.visibility = View.GONE
                binding.progressBarHorizontalTop.visibility = View.VISIBLE
                binding.progressBarHorizontalBottom.visibility = View.GONE
            }else{
                binding.progressBar.visibility = View.GONE
                binding.progressBarHorizontalTop.visibility = View.GONE
                binding.progressBarHorizontalBottom.visibility = View.VISIBLE
            }
        }else{
            binding.progressBar.visibility = View.VISIBLE
            binding.progressBarHorizontalTop.visibility = View.GONE
            binding.progressBarHorizontalBottom.visibility = View.GONE
        }

        view.setOnTouchListener(FlingPlayBackController(requireContext()))
        setUpMiniPlayer()
        setUpButtons()
    }

    fun setUpButtons() {
        if (ApexUtil.isTablet) {
            binding.actionNext.show()
            binding.actionPrevious.show()
        } else {
            binding.actionNext.isVisible = PreferenceUtil.isExtraControls
            binding.actionPrevious.isVisible = PreferenceUtil.isExtraControls
        }
        binding.actionNext.setOnClickListener(this)
        binding.actionPrevious.setOnClickListener(this)
    }

    private fun setUpMiniPlayer() {
        setUpPlayPauseButton()
        binding.progressBar.accentColor()
        binding.progressBarHorizontalTop.supportProgressTintList = context?.let {
            ThemeStore.accentColor(
                it
            )
        }?.let { ColorStateList.valueOf(it) }

        binding.progressBarHorizontalBottom.supportProgressTintList = context?.let {
            ThemeStore.accentColor(
                it
            )
        }?.let { ColorStateList.valueOf(it) }
    }

    private fun setUpPlayPauseButton() {
        binding.miniPlayerPlayPauseButton.setOnClickListener(PlayPauseButtonOnClickHandler())
    }

    private fun updateSongTitle() {

        val song = MusicPlayerRemote.currentSong

        val builder = SpannableStringBuilder()

        val title = song.title.toSpannable()
        title.setSpan(ForegroundColorSpan(textColorPrimary()), 0, title.length, 0)

        val text = song.artistName.toSpannable()
        text.setSpan(ForegroundColorSpan(textColorSecondary()), 0, text.length, 0)

        builder.append(title).append(" • ").append(text)

        binding.miniPlayerTitle.isSelected = true
        binding.miniPlayerTitle.text = builder

//        binding.title.isSelected = true
//        binding.title.text = song.title
//        binding.text.isSelected = true
//        binding.text.text = song.artistName
    }

    private fun updateSongCover() {
        val song = MusicPlayerRemote.currentSong

        if (PreferenceUtil.isMiniPlayerCircle) {
            Glide.with(requireContext())
                .load(ApexGlideExtension.getSongModel(song))
                .circleCrop()
                .transition(ApexGlideExtension.getDefaultTransition())
                .songCoverOptions(song)
                .into(binding.image)
        }else {
            Glide.with(requireContext())
                .load(ApexGlideExtension.getSongModel(song))
                .transition(ApexGlideExtension.getDefaultTransition())
                .songCoverOptions(song)
                .into(binding.image)
        }
    }

    override fun onServiceConnected() {
        updateSongTitle()
        updateSongCover()
        updatePlayPauseDrawableState()
    }

    override fun onPlayingMetaChanged() {
        updateSongTitle()
        updateSongCover()
    }

    override fun onPlayStateChanged() {
        updatePlayPauseDrawableState()
    }

    override fun onUpdateProgressViews(progress: Int, total: Int) {
        binding.progressBar.max = total
        binding.progressBar.progress = progress

        binding.progressBarHorizontalTop.max = total
        binding.progressBarHorizontalTop.progress = progress

        binding.progressBarHorizontalBottom.max = total
        binding.progressBarHorizontalBottom.progress = progress
    }

    override fun onResume() {
        super.onResume()
        progressViewUpdateHelper.start()
    }

    override fun onPause() {
        super.onPause()
        progressViewUpdateHelper.stop()
    }

    protected fun updatePlayPauseDrawableState() {
        if (MusicPlayerRemote.isPlaying) {
            binding.miniPlayerPlayPauseButton.setImageResource(R.drawable.ic_pause)
        } else {
            binding.miniPlayerPlayPauseButton.setImageResource(R.drawable.ic_play_arrow)
        }
    }

    class FlingPlayBackController(context: Context) : View.OnTouchListener {

        private var flingPlayBackController = GestureDetector(context,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    if (PreferenceUtil.isSwipe) {
                        if (abs(velocityX) > abs(velocityY)) {
                            if (velocityX < 0) {
                                if (PreferenceUtil.isAutoplay) {
                                    MusicPlayerRemote.playNextSongAuto(MusicPlayerRemote.isPlaying)
                                } else {
                                    MusicPlayerRemote.playNextSong()
                                }
                                return true
                            } else if (velocityX > 0) {
                                if (PreferenceUtil.isAutoplay) {
                                    MusicPlayerRemote.playPreviousSongAuto(MusicPlayerRemote.isPlaying)
                                }else {
                                    MusicPlayerRemote.playPreviousSong()
                                }
                                return true
                            }
                        }
                        return false
                    }
                    return false
                }
            })

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            return flingPlayBackController.onTouchEvent(event)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
