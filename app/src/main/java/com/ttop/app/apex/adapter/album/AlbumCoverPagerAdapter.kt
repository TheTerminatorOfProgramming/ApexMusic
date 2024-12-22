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
package com.ttop.app.apex.adapter.album

import android.app.Dialog
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.GestureDetector
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.ttop.app.apex.R
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.extensions.accentTextColor
import com.ttop.app.apex.extensions.generalThemeValue
import com.ttop.app.apex.extensions.keepScreenOn
import com.ttop.app.apex.extensions.materialDialog
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.extensions.withCenteredButtons
import com.ttop.app.apex.glide.ApexColoredTarget
import com.ttop.app.apex.glide.ApexGlideExtension
import com.ttop.app.apex.glide.ApexGlideExtension.asBitmapPalette
import com.ttop.app.apex.glide.ApexGlideExtension.songCoverOptions
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.libraries.appthemehelper.util.VersionUtils
import com.ttop.app.apex.misc.CustomFragmentStatePagerAdapter
import com.ttop.app.apex.model.Song
import com.ttop.app.apex.ui.activities.MainActivity
import com.ttop.app.apex.ui.fragments.AlbumCoverStyle
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.Adaptive
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.Blur
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.Card
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.Classic
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.Gradient
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.Live
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.Minimal
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.Peek
import com.ttop.app.apex.util.MusicUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.color.MediaNotificationProcessor
import com.ttop.app.apex.util.theme.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlbumCoverPagerAdapter(
    fragmentManager: FragmentManager,
    private val dataSet: List<Song>
) : CustomFragmentStatePagerAdapter(fragmentManager) {

    private var currentColorReceiver: AlbumCoverFragment.ColorReceiver? = null
    private var currentColorReceiverPosition = -1

    override fun getItem(position: Int): Fragment {
        return AlbumCoverFragment.newInstance(dataSet[position])
    }

    override fun getCount(): Int {
        return dataSet.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val o = super.instantiateItem(container, position)
        if (currentColorReceiver != null && currentColorReceiverPosition == position) {
            receiveColor(currentColorReceiver!!, currentColorReceiverPosition)
        }
        return o
    }

    /**
     * Only the latest passed [AlbumCoverFragment.ColorReceiver] is guaranteed to receive a
     * response
     */
    fun receiveColor(colorReceiver: AlbumCoverFragment.ColorReceiver, position: Int) {

        if (getFragment(position) is AlbumCoverFragment) {
            val fragment = getFragment(position) as AlbumCoverFragment
            currentColorReceiver = null
            currentColorReceiverPosition = -1
            fragment.receiveColor(colorReceiver, position)
        } else {
            currentColorReceiver = colorReceiver
            currentColorReceiverPosition = position
        }
    }

    class AlbumCoverFragment : Fragment() {

        private var isColorReady: Boolean = false
        private lateinit var color: MediaNotificationProcessor
        private lateinit var song: Song
        private var colorReceiver: ColorReceiver? = null
        private var request: Int = 0
        private val mainActivity get() = activity as MainActivity

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            if (arguments != null) {
                song = if (VersionUtils.hasT()) {
                    requireArguments().getParcelable(SONG_ARG, Song::class.java)!!
                } else {
                    requireArguments().getParcelable(SONG_ARG)!!
                }
            }
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {


            val view = inflater.inflate(getLayoutWithPlayerTheme(), container, false)

            val gestureDetector =
                GestureDetector(activity, object : GestureDetector.SimpleOnGestureListener() {
                    override fun onDoubleTap(e: MotionEvent): Boolean {
                        if (!PreferenceUtil.isHapticFeedbackDisabled) {
                            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        }

                        if (PreferenceUtil.showLyrics) {
                            PreferenceUtil.showLyrics = false
                        }else {
                            PreferenceUtil.showLyrics = true
                        }

                        return super.onDoubleTap(e)
                    }

                    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                        if (MusicPlayerRemote.isPlaying) {
                            MusicPlayerRemote.pauseSong()
                        } else {
                            MusicPlayerRemote.resumePlaying()
                        }
                        return super.onSingleTapConfirmed(e)
                    }

                    override fun onLongPress(e: MotionEvent) {
                        if (mainActivity.getBottomSheetBehavior().state == STATE_EXPANDED) {
                            if (PreferenceUtil.nowPlayingScreen != Live) {
                                val nps = listOf(Adaptive, Blur, Classic, Gradient, Peek)
                                if (!nps.contains(PreferenceUtil.nowPlayingScreen)) {
                                    if (!PreferenceUtil.isHapticFeedbackDisabled) {
                                        requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                    }
                                    showLyricsDialog()
                                }
                            }
                        }
                        super.onLongPress(e)
                    }

                    override fun onDown(e: MotionEvent): Boolean {
                        return true //this method is needed otherwise the GestureDetector won't work
                    }
                })

            view.setOnTouchListener { _, event ->
                view.performClick()
                gestureDetector.onTouchEvent(event)
            }

            return view
        }

        private fun showLyricsDialog() {
            lifecycleScope.launch(Dispatchers.IO) {
                val data: String? = MusicUtil.getLyrics(MusicPlayerRemote.currentSong)
                withContext(Dispatchers.Main) {
                    mainActivity.keepScreenOn(true)
                    val builder = AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
                    builder.setTitle(MusicPlayerRemote.currentSong.title + " : " + MusicPlayerRemote.currentSong.artistName)
                    builder.setMessage(if (data.isNullOrEmpty()) R.string.no_lyrics_found.toString() else data)

                    builder.setNegativeButton(R.string.dismiss) { _, _ ->
                        if (!PreferenceUtil.isHapticFeedbackDisabled) {
                            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        }
                        mainActivity.keepScreenOn(false)
                        materialDialog().dismiss()
                    }

                    builder.setPositiveButton(R.string.action_refresh) { _, _ ->
                        if (!PreferenceUtil.isHapticFeedbackDisabled) {
                            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        }
                        materialDialog().dismiss()
                        showLyricsDialog()
                    }

                    val dialog: AlertDialog = builder.show()

                    val textViewMessage: TextView? = dialog.findViewById(android.R.id.message)
                    val textViewTitle: TextView? = dialog.findViewById(R.id.alertTitle)


                    textViewTitle?.typeface = Typeface.DEFAULT_BOLD

                    when (context?.generalThemeValue) {
                        ThemeMode.LIGHT -> {
                            dialog.window?.setBackgroundDrawableResource(R.color.md_white_1000)
                            textViewMessage?.setTextColor(Color.BLACK)
                        }

                        ThemeMode.DARK -> {
                            dialog.window?.setBackgroundDrawableResource(R.color.dark_color)
                            textViewMessage?.setTextColor(Color.WHITE)
                        }

                        ThemeMode.BLACK -> {
                            dialog.window?.setBackgroundDrawableResource(R.color.md_black_1000)
                            textViewMessage?.setTextColor(Color.WHITE)
                        }

                        ThemeMode.AUTO -> {
                            when (requireContext().resources?.configuration?.uiMode?.and(
                                Configuration.UI_MODE_NIGHT_MASK
                            )) {
                                Configuration.UI_MODE_NIGHT_YES -> {
                                    if (PreferenceUtil.isBlackMode) {
                                        dialog.window?.setBackgroundDrawableResource(R.color.md_black_1000)
                                        textViewMessage?.setTextColor(Color.WHITE)
                                    } else {
                                        dialog.window?.setBackgroundDrawableResource(R.color.dark_color)
                                        textViewMessage?.setTextColor(Color.WHITE)
                                    }
                                }

                                Configuration.UI_MODE_NIGHT_NO,
                                Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                                    dialog.window?.setBackgroundDrawableResource(R.color.md_white_1000)
                                    textViewMessage?.setTextColor(Color.BLACK)
                                }
                            }
                        }

                        else -> {
                            dialog.window?.setBackgroundDrawableResource(R.color.md_white_1000)
                            textViewMessage?.setTextColor(Color.BLACK)
                        }
                    }

                    dialog.getButton(Dialog.BUTTON_NEGATIVE).textSize = 26f
                    dialog.getButton(Dialog.BUTTON_POSITIVE).textSize = 26f
                    textViewMessage!!.textSize = 24f
                    textViewTitle!!.textSize = 28f

                    dialog.setCanceledOnTouchOutside(false)
                    dialog.getButton(Dialog.BUTTON_NEGATIVE).accentTextColor()
                    dialog.getButton(Dialog.BUTTON_POSITIVE).accentTextColor()
                    textViewTitle.setTextColor(requireContext().accentColor())

                    dialog.withCenteredButtons()
                }
            }
        }

        private fun getLayoutWithPlayerTheme(): Int {
            return when (PreferenceUtil.nowPlayingScreen) {
                Adaptive -> {
                    when (PreferenceUtil.albumCoverStyle) {
                        AlbumCoverStyle.Normal -> R.layout.fragment_album_cover
                        AlbumCoverStyle.Flat -> R.layout.fragment_album_flat_cover
                        AlbumCoverStyle.Circle -> R.layout.fragment_album_circle_cover
                        AlbumCoverStyle.Card -> R.layout.fragment_album_card_cover
                        AlbumCoverStyle.Full -> R.layout.fragment_album_full_cover
                        AlbumCoverStyle.FullCard -> R.layout.fragment_album_full_card_cover
                        AlbumCoverStyle.Peek -> R.layout.fragment_album_peek
                    }
                }

                Blur -> {
                    if (PreferenceUtil.isCarouselEffect) {
                        R.layout.fragment_album_carousel_cover
                    } else {
                        when (PreferenceUtil.albumCoverStyle) {
                            AlbumCoverStyle.Normal -> R.layout.fragment_album_cover
                            AlbumCoverStyle.Flat -> R.layout.fragment_album_flat_cover
                            AlbumCoverStyle.Circle -> R.layout.fragment_album_circle_cover
                            AlbumCoverStyle.Card -> R.layout.fragment_album_card_cover
                            AlbumCoverStyle.Full -> R.layout.fragment_album_full_cover
                            AlbumCoverStyle.FullCard -> R.layout.fragment_album_full_card_cover
                            AlbumCoverStyle.Peek -> R.layout.fragment_album_peek
                        }
                    }
                }

                Card -> {
                    R.layout.fragment_album_full_cover
                }

                Classic -> {
                    if (PreferenceUtil.isCarouselEffect) {
                        R.layout.fragment_album_carousel_cover
                    } else {
                        when (PreferenceUtil.albumCoverStyle) {
                            AlbumCoverStyle.Normal -> R.layout.fragment_album_cover
                            AlbumCoverStyle.Flat -> R.layout.fragment_album_flat_cover
                            AlbumCoverStyle.Circle -> R.layout.fragment_album_circle_cover
                            AlbumCoverStyle.Card -> R.layout.fragment_album_card_cover
                            AlbumCoverStyle.Full -> R.layout.fragment_album_full_cover
                            AlbumCoverStyle.FullCard -> R.layout.fragment_album_full_card_cover
                            AlbumCoverStyle.Peek -> R.layout.fragment_album_peek
                        }
                    }
                }

                Gradient -> {
                    R.layout.fragment_album_full_cover
                }

                Live -> {
                    R.layout.fragment_album_full_cover
                }

                Minimal -> {
                    R.layout.fragment_album_full_cover
                }

                Peek -> {
                    if (PreferenceUtil.isCarouselEffect) {
                        R.layout.fragment_album_carousel_cover
                    } else {
                        when (PreferenceUtil.albumCoverStyle) {
                            AlbumCoverStyle.Normal -> R.layout.fragment_album_cover
                            AlbumCoverStyle.Flat -> R.layout.fragment_album_flat_cover
                            AlbumCoverStyle.Circle -> R.layout.fragment_album_circle_cover
                            AlbumCoverStyle.Card -> R.layout.fragment_album_card_cover
                            AlbumCoverStyle.Full -> R.layout.fragment_album_full_cover
                            AlbumCoverStyle.FullCard -> R.layout.fragment_album_full_card_cover
                            AlbumCoverStyle.Peek -> R.layout.fragment_album_peek
                        }
                    }
                }
            }
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            loadAlbumCover(albumCover = view.findViewById(R.id.player_image))
        }

        override fun onDestroyView() {
            super.onDestroyView()
            colorReceiver = null
        }

        private fun loadAlbumCover(albumCover: ImageView) {
            Glide.with(this).asBitmapPalette().songCoverOptions(song)
                .load(ApexGlideExtension.getSongModel(song))
                .dontAnimate()
                .into(object : ApexColoredTarget(albumCover) {
                    override fun onColorReady(colors: MediaNotificationProcessor) {
                        setColor(colors)
                    }
                })
        }

        private fun setColor(color: MediaNotificationProcessor) {
            this.color = color
            isColorReady = true
            if (colorReceiver != null) {
                colorReceiver!!.onColorReady(color, request)
                colorReceiver = null
            }
        }

        internal fun receiveColor(colorReceiver: ColorReceiver, request: Int) {
            if (isColorReady) {
                colorReceiver.onColorReady(color, request)
            } else {
                this.colorReceiver = colorReceiver
                this.request = request
            }
        }

        interface ColorReceiver {
            fun onColorReady(color: MediaNotificationProcessor, request: Int)
        }

        companion object {

            private const val SONG_ARG = "song"

            fun newInstance(song: Song): AlbumCoverFragment {
                val frag = AlbumCoverFragment()
                frag.arguments = bundleOf(SONG_ARG to song)
                return frag
            }
        }
    }

    companion object {
        val TAG: String = AlbumCoverPagerAdapter::class.java.simpleName
    }
}