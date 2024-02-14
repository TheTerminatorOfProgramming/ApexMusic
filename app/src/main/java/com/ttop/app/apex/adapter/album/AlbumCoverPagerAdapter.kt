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
import com.ttop.app.apex.extensions.keepScreenOn
import com.ttop.app.apex.extensions.materialDialog
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.extensions.withCenteredButtons
import com.ttop.app.apex.glide.ApexColoredTarget
import com.ttop.app.apex.glide.ApexGlideExtension
import com.ttop.app.apex.glide.ApexGlideExtension.asBitmapPalette
import com.ttop.app.apex.glide.ApexGlideExtension.songCoverOptions
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.misc.CustomFragmentStatePagerAdapter
import com.ttop.app.apex.model.Song
import com.ttop.app.apex.ui.activities.MainActivity
import com.ttop.app.apex.ui.fragments.AlbumCoverStyle
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.Card
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.Gradient
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.Minimal
import com.ttop.app.apex.util.MusicUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.color.MediaNotificationProcessor
import com.ttop.app.appthemehelper.util.VersionUtils
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
                }else {
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

            val gestureDetector = GestureDetector(activity, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    if (PreferenceUtil.syncedLyrics) {
                        requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        PreferenceUtil.showLyrics = !PreferenceUtil.showLyrics
                        if (PreferenceUtil.lyricsScreenOn && PreferenceUtil.showLyrics) {
                            mainActivity.keepScreenOn(true)
                        } else if (!PreferenceUtil.isScreenOnEnabled && !PreferenceUtil.showLyrics) {
                            mainActivity.keepScreenOn(false)
                        }

                        if (PreferenceUtil.showLyrics) {
                            showToast(getString(R.string.cover_lyrics_on))
                        }else {
                            showToast(getString(R.string.cover_lyrics_off))
                        }
                    }
                    return super.onDoubleTap(e)
                }

                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    if (MusicPlayerRemote.isPlaying) {
                        MusicPlayerRemote.pauseSong()
                    }else {
                        MusicPlayerRemote.resumePlaying()
                    }
                    return super.onSingleTapConfirmed(e)
                }

                override fun onLongPress(e: MotionEvent) {
                    if (mainActivity.getBottomSheetBehavior().state == STATE_EXPANDED) {
                        if (PreferenceUtil.isEmbedMode) {
                            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            showLyricsDialog()
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
                val data: String? = MusicUtil.getLyrics(song)
                withContext(Dispatchers.Main) {
                    mainActivity.keepScreenOn(PreferenceUtil.lyricsScreenOn)
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle(song.title)
                    builder.setMessage(if (data.isNullOrEmpty()) R.string.no_lyrics_found.toString() else data)

                    builder.setNegativeButton(R.string.dismiss) { _, _ ->
                        mainActivity.keepScreenOn(false)
                        materialDialog().dismiss()
                    }

                    val dialog: AlertDialog = builder.show()

                    val textViewMessage = dialog.findViewById(android.R.id.message) as TextView?
                    val textViewTitle = dialog.findViewById(R.id.alertTitle) as TextView?


                    when (PreferenceUtil.fontSizeLyrics) {
                        "12" -> {
                            dialog.getButton(Dialog.BUTTON_NEGATIVE).textSize = 14f
                            textViewMessage!!.textSize = 12f
                            textViewTitle!!.textSize = 16f
                        }

                        "13" -> {
                            dialog.getButton(Dialog.BUTTON_NEGATIVE).textSize = 15f
                            textViewMessage!!.textSize = 13f
                            textViewTitle!!.textSize = 17f
                        }

                        "14" -> {
                            dialog.getButton(Dialog.BUTTON_NEGATIVE).textSize = 16f
                            textViewMessage!!.textSize = 14f
                            textViewTitle!!.textSize = 18f
                        }

                        "15" -> {
                            dialog.getButton(Dialog.BUTTON_NEGATIVE).textSize = 17f
                            textViewMessage!!.textSize = 15f
                            textViewTitle!!.textSize = 19f
                        }

                        "16" -> {
                            dialog.getButton(Dialog.BUTTON_NEGATIVE).textSize = 18f
                            textViewMessage!!.textSize = 16f
                            textViewTitle!!.textSize = 20f
                        }

                        "17" -> {
                            dialog.getButton(Dialog.BUTTON_NEGATIVE).textSize = 19f
                            textViewMessage!!.textSize = 17f
                            textViewTitle!!.textSize = 21f
                        }

                        "18" -> {
                            dialog.getButton(Dialog.BUTTON_NEGATIVE).textSize = 20f
                            textViewMessage!!.textSize = 18f
                            textViewTitle!!.textSize = 22f
                        }

                        "19" -> {
                            dialog.getButton(Dialog.BUTTON_NEGATIVE).textSize = 21f
                            textViewMessage!!.textSize = 19f
                            textViewTitle!!.textSize = 23f
                        }

                        "20" -> {
                            dialog.getButton(Dialog.BUTTON_NEGATIVE).textSize = 22f
                            textViewMessage!!.textSize = 20f
                            textViewTitle!!.textSize = 24f
                        }

                        "21" -> {
                            dialog.getButton(Dialog.BUTTON_NEGATIVE).textSize = 23f
                            textViewMessage!!.textSize = 21f
                            textViewTitle!!.textSize = 25f
                        }

                        "22" -> {
                            dialog.getButton(Dialog.BUTTON_NEGATIVE).textSize = 24f
                            textViewMessage!!.textSize = 22f
                            textViewTitle!!.textSize = 26f
                        }

                        "23" -> {
                            dialog.getButton(Dialog.BUTTON_NEGATIVE).textSize = 25f
                            textViewMessage!!.textSize = 23f
                            textViewTitle!!.textSize = 27f
                        }

                        "24" -> {
                            dialog.getButton(Dialog.BUTTON_NEGATIVE).textSize = 26f
                            textViewMessage!!.textSize = 24f
                            textViewTitle!!.textSize = 28f
                        }
                    }

                    dialog.setCanceledOnTouchOutside(false)
                    dialog.getButton(Dialog.BUTTON_NEGATIVE).accentTextColor()
                    textViewTitle!!.setTextColor(requireContext().accentColor())

                    dialog.withCenteredButtons()
                }
            }
        }

        private fun getLayoutWithPlayerTheme(): Int {
            return when (PreferenceUtil.nowPlayingScreen) {
                Card, Minimal, Gradient -> R.layout.fragment_album_full_cover
                else -> {
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
                //.checkIgnoreMediaStore()
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