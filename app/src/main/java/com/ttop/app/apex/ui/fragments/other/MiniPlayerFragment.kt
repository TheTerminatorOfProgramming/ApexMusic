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
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.GestureDetector
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.text.toSpannable
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.ttop.app.apex.R
import com.ttop.app.apex.databinding.FragmentMiniPlayerBinding
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.extensions.show
import com.ttop.app.apex.extensions.textColorPrimary
import com.ttop.app.apex.extensions.withCenteredButtons
import com.ttop.app.apex.glide.ApexGlideExtension
import com.ttop.app.apex.glide.ApexGlideExtension.songCoverOptions
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.helper.MusicProgressViewUpdateHelper
import com.ttop.app.apex.helper.PlayPauseButtonOnClickHandler
import com.ttop.app.apex.ui.activities.MainActivity
import com.ttop.app.apex.ui.fragments.base.AbsMusicServiceFragment
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.appthemehelper.ThemeStore
import kotlin.math.abs

open class MiniPlayerFragment : AbsMusicServiceFragment(R.layout.fragment_mini_player),
    MusicProgressViewUpdateHelper.Callback, View.OnClickListener {

    private var _binding: FragmentMiniPlayerBinding? = null
    private val binding get() = _binding!!
    private lateinit var progressViewUpdateHelper: MusicProgressViewUpdateHelper

    private var titleColor: Int = 0

    val mainActivity: MainActivity
        get() = activity as MainActivity
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

        view.setOnTouchListener(FlingPlayBackController(requireContext(), view, mainActivity))
        setUpMiniPlayer()
        setUpButtons()
    }

    fun setTitleTextColor(color: Int) {
        titleColor = color
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
        val text = song.artistName.toSpannable()

        title.setSpan(ForegroundColorSpan(textColorPrimary()), 0, title.length, 0)
        text.setSpan(ForegroundColorSpan(textColorPrimary()), 0, text.length, 0)

        /*if (PreferenceUtil.isMiniPlayerTextColored && PreferenceUtil.isAdaptiveColor) {
            title.setSpan(ForegroundColorSpan(Color.parseColor(PreferenceUtil.miniPlayerColorCode)), 0, title.length, 0)
            text.setSpan(ForegroundColorSpan(Color.parseColor(PreferenceUtil.miniPlayerColorCode)), 0, text.length, 0)
        }else {
            title.setSpan(ForegroundColorSpan(textColorPrimary()), 0, title.length, 0)
            text.setSpan(ForegroundColorSpan(textColorPrimary()), 0, text.length, 0)
        }*/

        builder.append(title).append(" • ").append(text)

        binding.miniPlayerTitle.isSelected = true
        binding.miniPlayerTitle.text = builder
        //binding.miniPlayerTitle.setTextColor(Color.parseColor(PreferenceUtil.miniPlayerColorCode))
//        binding.title.isSelected = true
//        binding.title.text = song.title
//        binding.text.isSelected = true
//        binding.text.text = song.artistName
    }

    private fun updateSongCover() {
        val song = MusicPlayerRemote.currentSong

        Glide.with(requireContext())
            .load(ApexGlideExtension.getSongModel(song))
            .transition(ApexGlideExtension.getDefaultTransition())
            .songCoverOptions(song)
            .into(binding.image as ImageView)
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

    class FlingPlayBackController(context: Context, view: View, activity: MainActivity) : View.OnTouchListener {

        private var flingPlayBackController = GestureDetector(context,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    if (PreferenceUtil.isSwipe == "always") {
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
                    }else if (PreferenceUtil.isSwipe == "tab") {
                        if (ApexUtil.isTablet) {
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
                    }
                    return false
                }

                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    activity.expandPanel()
                    return super.onSingleTapUp(e)
                }

                override fun onLongPress(e: MotionEvent) {
                    if (PreferenceUtil.dismissMethod == "long_touch") {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        if (PreferenceUtil.isDismissFailsafe) {
                            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                                .setMessage(R.string.clear_queue)
                                .setTitle(R.string.alert)
                                .setCancelable(false)
                                .setPositiveButton(R.string.yes) { _: DialogInterface?, _: Int ->
                                    MusicPlayerRemote.clearQueue()
                                }
                                .setNegativeButton(R.string.no) { dialog: DialogInterface, _: Int ->
                                    dialog.cancel()
                                }
                            val alertDialog: AlertDialog = builder.create()

                            alertDialog.show()

                            alertDialog.withCenteredButtons()

                        } else {
                            MusicPlayerRemote.clearQueue()
                        }
                        true
                    }
                    super.onLongPress(e)
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