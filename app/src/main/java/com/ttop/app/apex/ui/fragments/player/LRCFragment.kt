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
package com.ttop.app.apex.ui.fragments.player

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.ttop.app.apex.R
import com.ttop.app.apex.databinding.FragmentLrcBinding
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.helper.MusicProgressViewUpdateHelper
import com.ttop.app.apex.lyrics.CoverLrcView
import com.ttop.app.apex.model.lyrics.Lyrics
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.*
import com.ttop.app.apex.ui.fragments.base.AbsMusicServiceFragment
import com.ttop.app.apex.util.LyricUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.color.MediaNotificationProcessor
import com.ttop.app.appthemehelper.util.ATHUtil
import com.ttop.app.appthemehelper.util.ColorUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LRCFragment : AbsMusicServiceFragment(R.layout.fragment_lrc),
    MusicProgressViewUpdateHelper.Callback {

    private var _binding: FragmentLrcBinding? = null
    private val binding get() = _binding!!
    private var callbacks: Callbacks? = null
    private var progressViewUpdateHelper: MusicProgressViewUpdateHelper? = null

    private val lrcView: CoverLrcView get() = binding.lyricsView

    var lyrics: Lyrics? = null

    private fun updateLyrics() {
        val song = MusicPlayerRemote.currentSong
        lifecycleScope.launch(Dispatchers.IO) {
            val lrcFile = LyricUtil.getSyncedLyricsFile(song)
            if (lrcFile != null) {
                binding.lyricsView.loadLrc(lrcFile)
            } else {
                val embeddedLyrics = LyricUtil.getEmbeddedSyncedLyrics(song.data)
                if (embeddedLyrics != null) {
                    binding.lyricsView.loadLrc(embeddedLyrics)
                } else {
                    withContext(Dispatchers.Main) {
                        binding.lyricsView.reset()
                        binding.lyricsView.setLabel(context?.getString(R.string.no_lyrics_found))
                    }
                }
            }
        }

    }

    override fun onUpdateProgressViews(progress: Int, total: Int) {
        binding.lyricsView.updateTime(progress.toLong())
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLrcBinding.bind(view)
        progressViewUpdateHelper = MusicProgressViewUpdateHelper(this, 500, 1000)
        maybeInitLyrics()
        lrcView.apply {
            setDraggable(true) { time ->
                MusicPlayerRemote.seekTo(time.toInt())
                MusicPlayerRemote.resumePlaying()
                true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        maybeInitLyrics()
        updateLyrics()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        progressViewUpdateHelper?.stop()
        _binding = null
    }

    override fun onServiceConnected() {
        updateLyrics()
    }

    override fun onPlayingMetaChanged() {
        updateLyrics()
    }

    override fun onQueueChanged() {

    }

    private fun setLRCViewColors(@ColorInt primaryColor: Int, @ColorInt secondaryColor: Int) {
        lrcView.apply {
            setCurrentColor(primaryColor)
            setTimeTextColor(primaryColor)
            setTimelineColor(primaryColor)
            setNormalColor(secondaryColor)
            setTimelineTextColor(primaryColor)
            setPlayDrawableColor(primaryColor)
        }
    }

    private fun maybeInitLyrics() {
        binding.lyricsView.isVisible = true

        val lyrics: View = lrcView
        ObjectAnimator.ofFloat(lyrics, View.ALPHA, 1F).apply {
            doOnEnd {
                lyrics.isVisible = true
            }
            start()
        }

        progressViewUpdateHelper?.start()
    }

    fun notifyColorChange(color: MediaNotificationProcessor) {
        //callbacks?.onColorChanged(color)
        when (PreferenceUtil.nowPlayingScreen) {
            Blur -> setLRCViewColors(Color.WHITE, Color.BLACK)
            else -> {
                if (PreferenceUtil.isAdaptiveColor) {
                    if (PreferenceUtil.isPlayerBackgroundType) {
                        setLRCViewColors(com.ttop.app.apex.util.ColorUtil.getComplimentColor(color.secondaryTextColor), color.secondaryTextColor)
                    }else {
                        setLRCViewColors(color.secondaryTextColor, com.ttop.app.apex.util.ColorUtil.getComplimentColor(color.secondaryTextColor))
                    }
                }else {
                    val colorBg = ATHUtil.resolveColor(requireContext(), android.R.attr.colorBackground)
                    if (ColorUtil.isColorLight(colorBg)) {
                        setLRCViewColors(accentColor(), Color.BLACK)
                    }else {
                        setLRCViewColors(accentColor(), Color.WHITE)
                    }
                }
            }
        }
    }

    fun setCallbacks(listener: Callbacks) {
        callbacks = listener
    }

    interface Callbacks {

        fun onColorChanged(color: MediaNotificationProcessor)

        fun onFavoriteToggled()
    }

    companion object {
        val TAG: String = LRCFragment::class.java.simpleName
    }
}