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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.ttop.app.apex.R
import com.ttop.app.apex.extensions.accentTextColor
import com.ttop.app.apex.extensions.materialDialog
import com.ttop.app.apex.glide.ApexColoredTarget
import com.ttop.app.apex.glide.ApexGlideExtension
import com.ttop.app.apex.glide.ApexGlideExtension.asBitmapPalette
import com.ttop.app.apex.glide.ApexGlideExtension.songCoverOptions
import com.ttop.app.apex.misc.CustomFragmentStatePagerAdapter
import com.ttop.app.apex.model.Song
import com.ttop.app.apex.ui.activities.MainActivity
import com.ttop.app.apex.ui.fragments.AlbumCoverStyle
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.Card
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.Gradient
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.Tiny
import com.ttop.app.apex.ui.fragments.base.goToLyrics
import com.ttop.app.apex.util.MusicUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.color.MediaNotificationProcessor
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
                song = requireArguments().getParcelable(SONG_ARG)!!
            }
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {


            val view = inflater.inflate(getLayoutWithPlayerTheme(), container, false)

            view.setOnClickListener(object : DoubleClickListener() {
                override fun onDoubleClick(v: View?) {
                    if (mainActivity.getBottomSheetBehavior().state == STATE_EXPANDED) {
                        if (PreferenceUtil.isLyrics) {
                            if (PreferenceUtil.isEmbedMode == "tap" || PreferenceUtil.isEmbedMode == "both") {
                                showLyricsDialog()
                            }
                        }
                    }
                }
            })

            return view
        }

        private fun showLyricsDialog() {
            lifecycleScope.launch(Dispatchers.IO) {
                val data: String? = MusicUtil.getLyrics(song)
                withContext(Dispatchers.Main) {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle(song.title)
                    builder.setMessage(if (data.isNullOrEmpty()) R.string.no_lyrics_found.toString() else data)
                    if (PreferenceUtil.syncedLyrics) {
                        builder.setNegativeButton(R.string.synced_lyrics) { _, _ ->
                            goToLyrics(requireActivity())
                        }
                    }

                    builder.setNeutralButton(R.string.dismiss) { _, _ ->
                        materialDialog().dismiss()
                    }

                    val dialog: AlertDialog = builder.show()
                    dialog.setCanceledOnTouchOutside(false)
                    dialog.getButton(Dialog.BUTTON_NEGATIVE).accentTextColor()
                    dialog.getButton(Dialog.BUTTON_NEUTRAL).accentTextColor()
                }
            }
        }

        private fun getLayoutWithPlayerTheme(): Int {
            return when (PreferenceUtil.nowPlayingScreen) {
                Card, Tiny, Gradient -> R.layout.fragment_album_full_cover
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


            // Abstract class defining methods to check Double Click where Time Delay
            // between the two clicks is set to 300 ms
            abstract class DoubleClickListener : View.OnClickListener {
                var lastClickTime: Long = 0
                override fun onClick(v: View?) {
                    val clickTime = System.currentTimeMillis()
                    if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                        onDoubleClick(v)
                    }
                    lastClickTime = clickTime
                }

                abstract fun onDoubleClick(v: View?)

                companion object {
                    private const val DOUBLE_CLICK_TIME_DELTA: Long = 200 //milliseconds
                }
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
