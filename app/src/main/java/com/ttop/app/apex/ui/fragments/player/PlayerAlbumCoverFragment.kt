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
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.viewpager.widget.ViewPager
import com.ttop.app.apex.R
import com.ttop.app.apex.SHOW_LYRICS
import com.ttop.app.apex.adapter.album.AlbumCoverPagerAdapter
import com.ttop.app.apex.adapter.album.AlbumCoverPagerAdapter.AlbumCoverFragment
import com.ttop.app.apex.databinding.FragmentPlayerAlbumCoverBinding
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.extensions.isColorLight
import com.ttop.app.apex.extensions.keepScreenOn
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.extensions.surfaceColor
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.helper.MusicProgressViewUpdateHelper
import com.ttop.app.apex.libraries.appthemehelper.util.ATHColorUtil
import com.ttop.app.apex.libraries.appthemehelper.util.ATHUtil
import com.ttop.app.apex.lyrics.CoverLrcView
import com.ttop.app.apex.model.lyrics.Lyrics
import com.ttop.app.apex.transform.CarousalPagerTransformer
import com.ttop.app.apex.ui.activities.MainActivity
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.Adaptive
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.Blur
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.Card
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.Classic
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.Gradient
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.Live
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.Minimal
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.Peek
import com.ttop.app.apex.ui.fragments.base.AbsMusicServiceFragment
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.ColorUtil
import com.ttop.app.apex.util.LyricUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.ViewUtil
import com.ttop.app.apex.util.color.MediaNotificationProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayerAlbumCoverFragment : AbsMusicServiceFragment(R.layout.fragment_player_album_cover),
    ViewPager.OnPageChangeListener, MusicProgressViewUpdateHelper.Callback,
    SharedPreferences.OnSharedPreferenceChangeListener {

    private var _binding: FragmentPlayerAlbumCoverBinding? = null
    private val binding get() = _binding!!
    private var callbacks: Callbacks? = null
    private var currentPosition: Int = 0
    val viewPager get() = binding.viewPager
    private val mainActivity get() = activity as MainActivity

    private val colorReceiver = object : AlbumCoverFragment.ColorReceiver {
        override fun onColorReady(color: MediaNotificationProcessor, request: Int) {
            if (currentPosition == request) {
                notifyColorChange(color)
            }
        }
    }
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
        _binding = FragmentPlayerAlbumCoverBinding.bind(view)
        setupViewPager()
        progressViewUpdateHelper = MusicProgressViewUpdateHelper(this, 500, 1000)

        maybeInitLyrics()
        lrcView.apply {
            setDraggable(true) { time ->
                MusicPlayerRemote.seekTo(time.toInt())
                MusicPlayerRemote.resumePlaying()
                true
            }
        }

        if (PreferenceUtil.showLyrics) {
            binding.cardView.strokeWidth = ViewUtil.convertDpToPixel(5f, resources).toInt()
        }else {
            binding.cardView.strokeWidth = 0
        }
    }

    private fun setupViewPager() {
        binding.viewPager.addOnPageChangeListener(this)
        val nps = PreferenceUtil.nowPlayingScreen

        when (nps) {
            Adaptive -> {
                binding.viewPager.offscreenPageLimit = 2
            }

            Blur -> {
                if (PreferenceUtil.isCarouselEffect) {
                    val metrics = resources.displayMetrics
                    val ratio = metrics.heightPixels.toFloat() / metrics.widthPixels.toFloat()
                    binding.viewPager.clipToPadding = false
                    val padding =
                        if (ratio >= 1.777f) {
                            40
                        } else {
                            100
                        }
                    binding.viewPager.setPadding(padding, 0, padding, 0)
                    binding.viewPager.pageMargin = 0
                    binding.viewPager.setPageTransformer(
                        false,
                        CarousalPagerTransformer(requireContext())
                    )
                } else {
                    binding.viewPager.offscreenPageLimit = 2
                    binding.viewPager.setPageTransformer(
                        true,
                        PreferenceUtil.albumCoverTransform
                    )
                }
            }

            Card -> {
                binding.viewPager.offscreenPageLimit = 2
            }

            Classic -> {
                if (PreferenceUtil.isCarouselEffect) {
                    val metrics = resources.displayMetrics
                    val ratio = metrics.heightPixels.toFloat() / metrics.widthPixels.toFloat()
                    binding.viewPager.clipToPadding = false
                    val padding =
                        if (ratio >= 1.777f) {
                            40
                        } else {
                            100
                        }
                    binding.viewPager.setPadding(padding, 0, padding, 0)
                    binding.viewPager.pageMargin = 0
                    binding.viewPager.setPageTransformer(
                        false,
                        CarousalPagerTransformer(requireContext())
                    )
                } else {
                    binding.viewPager.offscreenPageLimit = 2
                    binding.viewPager.setPageTransformer(
                        true,
                        PreferenceUtil.albumCoverTransform
                    )
                }
            }

            Gradient -> {
                binding.viewPager.offscreenPageLimit = 2
            }

            Live -> {
                if (PreferenceUtil.isCarouselEffect) {
                    val metrics = resources.displayMetrics
                    val ratio = metrics.heightPixels.toFloat() / metrics.widthPixels.toFloat()
                    binding.viewPager.clipToPadding = false
                    val padding =
                        if (ratio >= 1.777f) {
                            40
                        } else {
                            100
                        }
                    binding.viewPager.setPadding(padding, 0, padding, 0)
                    binding.viewPager.pageMargin = 0
                    binding.viewPager.setPageTransformer(
                        false,
                        CarousalPagerTransformer(requireContext())
                    )
                } else {
                    binding.viewPager.offscreenPageLimit = 2
                    binding.viewPager.setPageTransformer(
                        true,
                        PreferenceUtil.albumCoverTransform
                    )
                }
            }

            Minimal -> {
                binding.viewPager.offscreenPageLimit = 2
            }

            Peek -> {
                if (PreferenceUtil.isCarouselEffect) {
                    val metrics = resources.displayMetrics
                    val ratio = metrics.heightPixels.toFloat() / metrics.widthPixels.toFloat()
                    binding.viewPager.clipToPadding = false
                    val padding =
                        if (ratio >= 1.777f) {
                            40
                        } else {
                            100
                        }
                    binding.viewPager.setPadding(padding, 0, padding, 0)
                    binding.viewPager.pageMargin = 0
                    binding.viewPager.setPageTransformer(
                        false,
                        CarousalPagerTransformer(requireContext())
                    )
                } else {
                    binding.viewPager.offscreenPageLimit = 2
                    binding.viewPager.setPageTransformer(
                        true,
                        PreferenceUtil.albumCoverTransform
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        maybeInitLyrics()
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .registerOnSharedPreferenceChangeListener(this)
        updateLyrics()

        if (PreferenceUtil.showLyrics) {
            binding.cardView.strokeWidth = ViewUtil.convertDpToPixel(5f, resources).toInt()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .unregisterOnSharedPreferenceChangeListener(this)
        binding.viewPager.removeOnPageChangeListener(this)
        progressViewUpdateHelper?.stop()
        _binding = null
    }

    override fun onServiceConnected() {
        updatePlayingQueue()
        updateLyrics()
    }

    override fun onPlayingMetaChanged() {
        if (viewPager.currentItem != MusicPlayerRemote.position) {
            viewPager.setCurrentItem(MusicPlayerRemote.position, true)
        }
        updateLyrics()
    }

    override fun onQueueChanged() {
        updatePlayingQueue()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        when (key) {
            SHOW_LYRICS -> {
                //modified
                if (PreferenceUtil.showLyrics) {
                    binding.cardView.strokeWidth = ViewUtil.convertDpToPixel(5f, resources).toInt()

                    mainActivity.keepScreenOn(true)
                    maybeInitLyrics()
                } else {
                    binding.cardView.strokeWidth = 0
                    mainActivity.keepScreenOn(false)
                    showLyrics(false)
                    progressViewUpdateHelper?.stop()
                }
            }
        }
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

        binding.cardView?.strokeColor = primaryColor
    }

    private fun showLyrics(visible: Boolean) {
        binding.lyricsView.isVisible = false
        binding.viewPager.isVisible = true

        val lyrics: View = lrcView
        ObjectAnimator.ofFloat(viewPager, View.ALPHA, if (visible) 0F else 1F).start()
        ObjectAnimator.ofFloat(lyrics, View.ALPHA, if (visible) 1F else 0F).apply {
            doOnEnd {
                lyrics.isVisible = visible
            }
            start()
        }
    }

    private fun maybeInitLyrics() {
        val nps = PreferenceUtil.nowPlayingScreen
        // Don't show lyrics container for below conditions

        if (lyricViewNpsList.contains(nps) && PreferenceUtil.showLyrics) {
            showLyrics(true)
            progressViewUpdateHelper?.start()
        } else {
            showLyrics(false)
            progressViewUpdateHelper?.stop()
        }

        if (nps == Live) {
            showLyrics(true)
            progressViewUpdateHelper?.start()
        }
    }

    private fun updatePlayingQueue() {
        binding.viewPager.apply {
            adapter = AlbumCoverPagerAdapter(parentFragmentManager, MusicPlayerRemote.playingQueue)
            setCurrentItem(MusicPlayerRemote.position, true)
            onPageSelected(MusicPlayerRemote.position)
            (adapter as AlbumCoverPagerAdapter).notifyDataSetChanged()
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageSelected(position: Int) {
        currentPosition = position
        if (binding.viewPager.adapter != null) {
            (binding.viewPager.adapter as AlbumCoverPagerAdapter).receiveColor(
                colorReceiver,
                position
            )
        }

        if (position < MusicPlayerRemote.position) {
            MusicPlayerRemote.playPreviousSongAuto(MusicPlayerRemote.isPlaying)
        }

        if (position > MusicPlayerRemote.position) {
            MusicPlayerRemote.playNextSongAuto(MusicPlayerRemote.isPlaying)
        }
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    private fun notifyColorChange(color: MediaNotificationProcessor) {
        callbacks?.onColorChanged(color)

        val decorView = activity?.window?.decorView
        WindowCompat.getInsetsController(
            activity?.window!!,
            decorView!!
        ).isAppearanceLightStatusBars =
            surfaceColor().isColorLight

        when (PreferenceUtil.nowPlayingScreen) {
            Blur -> setLRCViewColors(Color.WHITE, Color.BLACK)
            Gradient -> setLRCViewColors(
                ColorUtil.getComplimentColor(color.secondaryTextColor),
                color.secondaryTextColor
            )

            else -> {
                if (PreferenceUtil.isAdaptiveColor) {
                    if (PreferenceUtil.isPlayerBackgroundType) {
                        setLRCViewColors(
                            ColorUtil.getComplimentColor(color.secondaryTextColor),
                            color.secondaryTextColor
                        )
                    } else {
                        setLRCViewColors(
                            color.secondaryTextColor,
                            ColorUtil.getComplimentColor(color.secondaryTextColor)
                        )
                    }
                } else {
                    if (PreferenceUtil.materialYou) {
                        setLRCViewColors(
                            accentColor(),
                            ContextCompat.getColor(requireContext(), R.color.m3_widget_other_text)
                        )
                    } else {
                        val colorBg =
                            ATHUtil.resolveColor(requireContext(), android.R.attr.colorBackground)
                        if (ATHColorUtil.isColorLight(colorBg)) {
                            setLRCViewColors(accentColor(), Color.BLACK)
                        } else {
                            setLRCViewColors(accentColor(), Color.WHITE)
                        }
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
        val TAG: String = PlayerAlbumCoverFragment::class.java.simpleName
    }

    private val lyricViewNpsList =
        listOf(Blur, Classic, Adaptive, Card, Gradient, Peek)
}