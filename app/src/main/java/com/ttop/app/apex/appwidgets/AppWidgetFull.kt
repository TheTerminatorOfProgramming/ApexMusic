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
package com.ttop.app.apex.appwidgets

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.RemoteViews
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.ttop.app.apex.R
import com.ttop.app.apex.appwidgets.base.BaseAppWidget
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.extensions.getColorResCompat
import com.ttop.app.apex.extensions.getTintedDrawable
import com.ttop.app.apex.glide.ApexGlideExtension
import com.ttop.app.apex.glide.ApexGlideExtension.asBitmapPalette
import com.ttop.app.apex.glide.ApexGlideExtension.songCoverOptions
import com.ttop.app.apex.glide.WidgetBlurTransform
import com.ttop.app.apex.glide.palette.BitmapPaletteWrapper
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.service.MusicService
import com.ttop.app.apex.service.MusicService.Companion.ACTION_REWIND
import com.ttop.app.apex.service.MusicService.Companion.ACTION_SKIP
import com.ttop.app.apex.service.MusicService.Companion.ACTION_TOGGLE_PAUSE
import com.ttop.app.apex.ui.activities.MainActivity
import com.ttop.app.apex.util.DensityUtil
import com.ttop.app.apex.util.MusicUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.appthemehelper.util.MaterialValueHelper
import com.ttop.app.appthemehelper.util.VersionUtils

class AppWidgetFull : BaseAppWidget() {
    private var target: Target<BitmapPaletteWrapper>? = null // for cancellation
    private var _binding: AppWidgetFull? = null
    private val binding get() = _binding!!
    val title: TextView
        get() = binding.title
    val text: TextView
        get() = binding.text
    /**
     * Initialize given widgets to default state, where we launch Music on default click and hide
     * actions if service not running.
     */
    override fun defaultAppWidget(context: Context, appWidgetIds: IntArray) {
        var appWidgetView: RemoteViews? = null

        appWidgetView = when (PreferenceUtil.widgetBackground) {
            "default" -> {
                if (PreferenceUtil.isProgressBar) {
                    when (PreferenceUtil.progressColor) {
                        "black" -> RemoteViews(context.packageName, R.layout.app_widget_full_black)
                        "blue" -> RemoteViews(context.packageName, R.layout.app_widget_full_blue)
                        "green" -> RemoteViews(context.packageName, R.layout.app_widget_full_green)
                        "orange" -> RemoteViews(context.packageName, R.layout.app_widget_full_orange)
                        "purple" -> RemoteViews(context.packageName, R.layout.app_widget_full_purple)
                        "red" -> RemoteViews(context.packageName, R.layout.app_widget_full_red)
                        "teal" -> RemoteViews(context.packageName, R.layout.app_widget_full_teal)
                        "white" -> RemoteViews(context.packageName, R.layout.app_widget_full_white)
                        "yellow" -> RemoteViews(context.packageName, R.layout.app_widget_full_yellow)
                        else -> RemoteViews(context.packageName, R.layout.app_widget_full_teal)
                    }
                }else {
                    RemoteViews(context.packageName, R.layout.app_widget_full_time)
                }
            }
            "day_night" -> {
                if (VersionUtils.hasS()) {
                    if (PreferenceUtil.isProgressBar) {
                        when (PreferenceUtil.progressColor) {
                            "black" -> RemoteViews(context.packageName, R.layout.app_widget_full_day_night_black)
                            "blue" -> RemoteViews(context.packageName, R.layout.app_widget_full_day_night_blue)
                            "green" -> RemoteViews(context.packageName, R.layout.app_widget_full_day_night_green)
                            "orange" -> RemoteViews(context.packageName, R.layout.app_widget_full_day_night_orange)
                            "purple" -> RemoteViews(context.packageName, R.layout.app_widget_full_day_night_purple)
                            "red" -> RemoteViews(context.packageName, R.layout.app_widget_full_day_night_red)
                            "teal" -> RemoteViews(context.packageName, R.layout.app_widget_full_day_night_teal)
                            "white" -> RemoteViews(context.packageName, R.layout.app_widget_full_day_night_white)
                            "yellow" -> RemoteViews(context.packageName, R.layout.app_widget_full_day_night_yellow)
                            else -> RemoteViews(context.packageName, R.layout.app_widget_full_day_night_teal)
                        }
                    }else {
                        RemoteViews(context.packageName, R.layout.app_widget_full_day_night_time) }
                }else {
                    if (PreferenceUtil.isProgressBar) {
                        when (PreferenceUtil.progressColor) {
                            "black" -> RemoteViews(context.packageName, R.layout.app_widget_full_black)
                            "blue" -> RemoteViews(context.packageName, R.layout.app_widget_full_blue)
                            "green" -> RemoteViews(context.packageName, R.layout.app_widget_full_green)
                            "orange" -> RemoteViews(context.packageName, R.layout.app_widget_full_orange)
                            "purple" -> RemoteViews(context.packageName, R.layout.app_widget_full_purple)
                            "red" -> RemoteViews(context.packageName, R.layout.app_widget_full_red)
                            "teal" -> RemoteViews(context.packageName, R.layout.app_widget_full_teal)
                            "white" -> RemoteViews(context.packageName, R.layout.app_widget_full_white)
                            "yellow" -> RemoteViews(context.packageName, R.layout.app_widget_full_yellow)
                            else -> RemoteViews(context.packageName, R.layout.app_widget_full_teal)
                        }

                    }else {
                        RemoteViews(context.packageName, R.layout.app_widget_full_time) }
                }
            }
            "transparent" -> {
                if (PreferenceUtil.isProgressBar) {
                    when (PreferenceUtil.progressColor) {
                        "black" -> RemoteViews(context.packageName, R.layout.app_widget_full_transparent_black)
                        "blue" -> RemoteViews(context.packageName, R.layout.app_widget_full_transparent_blue)
                        "green" -> RemoteViews(context.packageName, R.layout.app_widget_full_transparent_green)
                        "orange" -> RemoteViews(context.packageName, R.layout.app_widget_full_transparent_orange)
                        "purple" -> RemoteViews(context.packageName, R.layout.app_widget_full_transparent_purple)
                        "red" -> RemoteViews(context.packageName, R.layout.app_widget_full_transparent_red)
                        "teal" -> RemoteViews(context.packageName, R.layout.app_widget_full_transparent_teal)
                        "white" -> RemoteViews(context.packageName, R.layout.app_widget_full_transparent_white)
                        "yellow" -> RemoteViews(context.packageName, R.layout.app_widget_full_transparent_yellow)
                        else -> RemoteViews(context.packageName, R.layout.app_widget_full_transparent_teal)
                    }
                }else {
                    RemoteViews(context.packageName, R.layout.app_widget_full_transparent_time)                }
            }
            else -> {
                if (PreferenceUtil.isProgressBar) {
                    when (PreferenceUtil.progressColor) {
                        "black" -> RemoteViews(context.packageName, R.layout.app_widget_full_black)
                        "blue" -> RemoteViews(context.packageName, R.layout.app_widget_full_blue)
                        "green" -> RemoteViews(context.packageName, R.layout.app_widget_full_green)
                        "orange" -> RemoteViews(context.packageName, R.layout.app_widget_full_orange)
                        "purple" -> RemoteViews(context.packageName, R.layout.app_widget_full_purple)
                        "red" -> RemoteViews(context.packageName, R.layout.app_widget_full_red)
                        "teal" -> RemoteViews(context.packageName, R.layout.app_widget_full_teal)
                        "white" -> RemoteViews(context.packageName, R.layout.app_widget_full_white)
                        "yellow" -> RemoteViews(context.packageName, R.layout.app_widget_full_yellow)
                        else -> RemoteViews(context.packageName, R.layout.app_widget_full_teal)
                    }
                }else {
                    RemoteViews(context.packageName, R.layout.app_widget_full_time)                }
            }
        }

        if (PreferenceUtil.widgetBackground == "transparent") {
            appWidgetView.setImageViewBitmap(
                R.id.button_update,
                context.getTintedDrawable(
                    R.drawable.ic_refresh,
                    MaterialValueHelper.getPrimaryTextColor(context, false)
                ).toBitmap()
            )
        }else {
            when (context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    val secondaryColor = MaterialValueHelper.getPrimaryTextColor(context, false)
                    appWidgetView.setImageViewBitmap(
                        R.id.button_update,
                        context.getTintedDrawable(
                            R.drawable.ic_refresh,
                            secondaryColor
                        ).toBitmap()
                    )
                }
                Configuration.UI_MODE_NIGHT_NO,
                Configuration.UI_MODE_NIGHT_UNDEFINED-> {
                    val secondaryColor = MaterialValueHelper.getPrimaryTextColor(context, true)
                    appWidgetView.setImageViewBitmap(
                        R.id.button_update,
                        context.getTintedDrawable(
                            R.drawable.ic_refresh,
                            secondaryColor
                        ).toBitmap()
                    )
                }
            }
        }

        appWidgetView.setViewVisibility(R.id.media_titles, View.INVISIBLE)
        appWidgetView.setImageViewResource(R.id.image, R.drawable.default_audio_art)

        if (PreferenceUtil.widgetBackground == "day_night") {
            when (context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    val secondaryColor = MaterialValueHelper.getSecondaryTextColor(context, false)
                    appWidgetView.setImageViewBitmap(
                        R.id.button_next,
                        context.getTintedDrawable(
                            R.drawable.ic_skip_next_outline,
                            secondaryColor
                        ).toBitmap()
                    )
                    appWidgetView.setImageViewBitmap(
                        R.id.button_prev,
                        context.getTintedDrawable(
                            R.drawable.ic_skip_previous_outline,
                            secondaryColor
                        ).toBitmap()
                    )
                    appWidgetView.setImageViewBitmap(
                        R.id.button_toggle_play_pause,
                        context.getTintedDrawable(
                            R.drawable.ic_play_arrow_outline_small,
                            secondaryColor
                        ).toBitmap()
                    )

                }
                Configuration.UI_MODE_NIGHT_NO,
                Configuration.UI_MODE_NIGHT_UNDEFINED-> {
                    val secondaryColor = MaterialValueHelper.getSecondaryTextColor(context, true)
                    appWidgetView.setImageViewBitmap(
                        R.id.button_next,
                        context.getTintedDrawable(
                            R.drawable.ic_skip_next_outline,
                            secondaryColor
                        ).toBitmap()
                    )
                    appWidgetView.setImageViewBitmap(
                        R.id.button_prev,
                        context.getTintedDrawable(
                            R.drawable.ic_skip_previous_outline,
                            secondaryColor
                        ).toBitmap()
                    )
                    appWidgetView.setImageViewBitmap(
                        R.id.button_toggle_play_pause,
                        context.getTintedDrawable(
                            R.drawable.ic_play_arrow_outline_small,
                            secondaryColor
                        ).toBitmap()
                    )
                }
            }
        }else{
            val secondaryColor = MaterialValueHelper.getSecondaryTextColor(context, true)
            appWidgetView.setImageViewBitmap(
                R.id.button_next,
                context.getTintedDrawable(
                    R.drawable.ic_skip_next_outline,
                    secondaryColor
                ).toBitmap()
            )
            appWidgetView.setImageViewBitmap(
                R.id.button_prev,
                context.getTintedDrawable(
                    R.drawable.ic_skip_previous_outline,
                    secondaryColor
                ).toBitmap()
            )
            appWidgetView.setImageViewBitmap(
                R.id.button_toggle_play_pause,
                context.getTintedDrawable(
                    R.drawable.ic_play_arrow_outline_small,
                    secondaryColor
                ).toBitmap()
            )
        }

        linkButtons(context, appWidgetView)

        pushUpdate(context, appWidgetIds, appWidgetView)
    }

    /**
     * Update all active widget instances by pushing changes
     */
    override fun performUpdate(service: MusicService, appWidgetIds: IntArray?) {
        var appWidgetView: RemoteViews? = null

        appWidgetView = when (PreferenceUtil.widgetBackground) {
            "default" -> {
                if (PreferenceUtil.isProgressBar) {
                    when (PreferenceUtil.progressColor) {
                        "black" -> RemoteViews(service.packageName, R.layout.app_widget_full_black)
                        "blue" -> RemoteViews(service.packageName, R.layout.app_widget_full_blue)
                        "green" -> RemoteViews(service.packageName, R.layout.app_widget_full_green)
                        "orange" -> RemoteViews(service.packageName, R.layout.app_widget_full_orange)
                        "purple" -> RemoteViews(service.packageName, R.layout.app_widget_full_purple)
                        "red" -> RemoteViews(service.packageName, R.layout.app_widget_full_red)
                        "teal" -> RemoteViews(service.packageName, R.layout.app_widget_full_teal)
                        "white" -> RemoteViews(service.packageName, R.layout.app_widget_full_white)
                        "yellow" -> RemoteViews(service.packageName, R.layout.app_widget_full_yellow)
                        else -> RemoteViews(service.packageName, R.layout.app_widget_full_teal)
                    }
                }else {
                    RemoteViews(service.packageName, R.layout.app_widget_full_time)
                }
            }
            "day_night" -> {
                if (VersionUtils.hasS()) {
                    if (PreferenceUtil.isProgressBar) {
                        when (PreferenceUtil.progressColor) {
                            "black" -> RemoteViews(service.packageName, R.layout.app_widget_full_day_night_black)
                            "blue" -> RemoteViews(service.packageName, R.layout.app_widget_full_day_night_blue)
                            "green" -> RemoteViews(service.packageName, R.layout.app_widget_full_day_night_green)
                            "orange" -> RemoteViews(service.packageName, R.layout.app_widget_full_day_night_orange)
                            "purple" -> RemoteViews(service.packageName, R.layout.app_widget_full_day_night_purple)
                            "red" -> RemoteViews(service.packageName, R.layout.app_widget_full_day_night_red)
                            "teal" -> RemoteViews(service.packageName, R.layout.app_widget_full_day_night_teal)
                            "white" -> RemoteViews(service.packageName, R.layout.app_widget_full_day_night_white)
                            "yellow" -> RemoteViews(service.packageName, R.layout.app_widget_full_day_night_yellow)
                            else -> RemoteViews(service.packageName, R.layout.app_widget_full_day_night_teal)
                        }
                    }else {
                        RemoteViews(service.packageName, R.layout.app_widget_full_day_night_time) }
                }else {
                    if (PreferenceUtil.isProgressBar) {
                        when (PreferenceUtil.progressColor) {
                            "black" -> RemoteViews(service.packageName, R.layout.app_widget_full_black)
                            "blue" -> RemoteViews(service.packageName, R.layout.app_widget_full_blue)
                            "green" -> RemoteViews(service.packageName, R.layout.app_widget_full_green)
                            "orange" -> RemoteViews(service.packageName, R.layout.app_widget_full_orange)
                            "purple" -> RemoteViews(service.packageName, R.layout.app_widget_full_purple)
                            "red" -> RemoteViews(service.packageName, R.layout.app_widget_full_red)
                            "teal" -> RemoteViews(service.packageName, R.layout.app_widget_full_teal)
                            "white" -> RemoteViews(service.packageName, R.layout.app_widget_full_white)
                            "yellow" -> RemoteViews(service.packageName, R.layout.app_widget_full_yellow)
                            else -> RemoteViews(service.packageName, R.layout.app_widget_full_teal)
                        }

                    }else {
                        RemoteViews(service.packageName, R.layout.app_widget_full_time) }
                }
            }
            "transparent" -> {
                if (PreferenceUtil.isProgressBar) {
                    when (PreferenceUtil.progressColor) {
                        "black" -> RemoteViews(service.packageName, R.layout.app_widget_full_transparent_black)
                        "blue" -> RemoteViews(service.packageName, R.layout.app_widget_full_transparent_blue)
                        "green" -> RemoteViews(service.packageName, R.layout.app_widget_full_transparent_green)
                        "orange" -> RemoteViews(service.packageName, R.layout.app_widget_full_transparent_orange)
                        "purple" -> RemoteViews(service.packageName, R.layout.app_widget_full_transparent_purple)
                        "red" -> RemoteViews(service.packageName, R.layout.app_widget_full_transparent_red)
                        "teal" -> RemoteViews(service.packageName, R.layout.app_widget_full_transparent_teal)
                        "white" -> RemoteViews(service.packageName, R.layout.app_widget_full_transparent_white)
                        "yellow" -> RemoteViews(service.packageName, R.layout.app_widget_full_transparent_yellow)
                        else -> RemoteViews(service.packageName, R.layout.app_widget_full_transparent_teal)
                    }
                }else {
                    RemoteViews(service.packageName, R.layout.app_widget_full_transparent_time)                }
            }
            else -> {
                if (PreferenceUtil.isProgressBar) {
                    when (PreferenceUtil.progressColor) {
                        "black" -> RemoteViews(service.packageName, R.layout.app_widget_full_black)
                        "blue" -> RemoteViews(service.packageName, R.layout.app_widget_full_blue)
                        "green" -> RemoteViews(service.packageName, R.layout.app_widget_full_green)
                        "orange" -> RemoteViews(service.packageName, R.layout.app_widget_full_orange)
                        "purple" -> RemoteViews(service.packageName, R.layout.app_widget_full_purple)
                        "red" -> RemoteViews(service.packageName, R.layout.app_widget_full_red)
                        "teal" -> RemoteViews(service.packageName, R.layout.app_widget_full_teal)
                        "white" -> RemoteViews(service.packageName, R.layout.app_widget_full_white)
                        "yellow" -> RemoteViews(service.packageName, R.layout.app_widget_full_yellow)
                        else -> RemoteViews(service.packageName, R.layout.app_widget_full_teal)
                    }
                }else {
                    RemoteViews(service.packageName, R.layout.app_widget_full_time)                }
            }
        }

        if (PreferenceUtil.widgetBackground == "transparent") {
            appWidgetView.setImageViewBitmap(
                R.id.button_update,
                service.getTintedDrawable(
                    R.drawable.ic_refresh,
                    MaterialValueHelper.getPrimaryTextColor(service, false)
                ).toBitmap()
            )
        }else {
            when (service.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    val secondaryColor = MaterialValueHelper.getPrimaryTextColor(service, false)
                    appWidgetView.setImageViewBitmap(
                        R.id.button_update,
                        service.getTintedDrawable(
                            R.drawable.ic_refresh,
                            secondaryColor
                        ).toBitmap()
                    )
                }
                Configuration.UI_MODE_NIGHT_NO,
                Configuration.UI_MODE_NIGHT_UNDEFINED-> {
                    val secondaryColor = MaterialValueHelper.getPrimaryTextColor(service, true)
                    appWidgetView.setImageViewBitmap(
                        R.id.button_update,
                        service.getTintedDrawable(
                            R.drawable.ic_refresh,
                            secondaryColor
                        ).toBitmap()
                    )
                }
            }
        }

        val isPlaying = service.isPlaying
        val song = service.currentSong

        // Set the titles and artwork
        if (song.title.isEmpty() && song.artistName.isEmpty()) {
            appWidgetView.setViewVisibility(R.id.media_titles, View.INVISIBLE)
            appWidgetView.setViewVisibility(R.id.progress_bar, View.INVISIBLE)
        } else {
            appWidgetView.setViewVisibility(R.id.progress_bar, View.VISIBLE)
            appWidgetView.setViewVisibility(R.id.media_titles, View.VISIBLE)
            appWidgetView.setTextViewText(R.id.title, song.title)
            appWidgetView.setTextViewText(R.id.text, getSongArtist(song))
            appWidgetView.setTextViewText(
                R.id.songText,
                MusicUtil.getReadableDurationString(service.songProgressMillis.toLong()) + "/" + MusicUtil.getReadableDurationString(
                    service.songDurationMillis.toLong()
                )
            )
        }

        if (PreferenceUtil.isDisableWidgetUpdate) {
            appWidgetView.setViewVisibility(R.id.button_update, View.GONE)
        }else {
            appWidgetView.setViewVisibility(R.id.button_update, View.VISIBLE)
        }

        // Set correct drawable for pause state
        val playPauseRes =
            if (isPlaying) R.drawable.ic_pause_outline_small else R.drawable.ic_play_arrow_outline_small
        appWidgetView.setImageViewBitmap(
            R.id.button_toggle_play_pause,
            service.getTintedDrawable(
                playPauseRes,
                MaterialValueHelper.getSecondaryTextColor(service, true)
            ).toBitmap()
        )

        // Set prev/next button drawables
        appWidgetView.setImageViewBitmap(
            R.id.button_next,
            service.getTintedDrawable(
                R.drawable.ic_skip_next_outline,
                MaterialValueHelper.getSecondaryTextColor(service, true)
            ).toBitmap()
        )
        appWidgetView.setImageViewBitmap(
            R.id.button_prev,
            service.getTintedDrawable(
                R.drawable.ic_skip_previous_outline,
                MaterialValueHelper.getSecondaryTextColor(service, true)
            ).toBitmap()
        )
        appWidgetView.setImageViewBitmap(
            R.id.button_toggle_play_pause,

            service.getTintedDrawable(
                R.drawable.ic_play_arrow_outline_small,
                MaterialValueHelper.getSecondaryTextColor(service, true)
            ).toBitmap()
        )
        // Link actions buttons to intents
        linkButtons(service, appWidgetView)

        if (PreferenceUtil.isProgressBar) {
            appWidgetView.setProgressBar(
                R.id.progress_bar,
                service.songDurationMillis,
                service.songProgressMillis,
                false
            )
        }

        if (imageSize == 0) {
            imageSize = 600
        }

        // Load the album cover async and push the update on completion
        service.runOnUiThread {
            if (target != null) {
                Glide.with(service).clear(target)
            }
            target = Glide.with(service).asBitmapPalette().songCoverOptions(song)
                .load(ApexGlideExtension.getSongModel(song))
                .placeholder(R.drawable.default_audio_art)
                .error(R.drawable.default_audio_art)
                .circleCrop()
                .into(object : SimpleTarget<BitmapPaletteWrapper>(
                    imageSize,
                    imageSize
                ) {
                    override fun onResourceReady(
                        resource: BitmapPaletteWrapper,
                        transition: Transition<in BitmapPaletteWrapper>?
                    ) {
                        val palette = resource.palette
                        update(
                            resource.bitmap, palette.getVibrantColor(
                                palette.getMutedColor(
                                    MaterialValueHelper.getSecondaryTextColor(
                                        service, true
                                    )
                                )
                            )
                        )
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        super.onLoadFailed(errorDrawable)
                        update(null, MaterialValueHelper.getSecondaryTextColor(service, true))
                    }

                    private fun update(bitmap: Bitmap?, color: Int) {
                        if (PreferenceUtil.widgetBackground == "day_night") {
                            when (service.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                                Configuration.UI_MODE_NIGHT_YES -> {
                                    // Set correct drawable for pause state
                                    appWidgetView.setImageViewBitmap(
                                        R.id.button_toggle_play_pause,
                                        service.getTintedDrawable(
                                            playPauseRes, service.resources.getColor(com.ttop.app.appthemehelper.R.color.md_white_1000)
                                        ).toBitmap()
                                    )
                                    // Set prev/next button drawables
                                    appWidgetView.setImageViewBitmap(
                                        R.id.button_next,
                                        service.getTintedDrawable(
                                            R.drawable.ic_skip_next_outline, service.resources.getColor(com.ttop.app.appthemehelper.R.color.md_white_1000)
                                        ).toBitmap()
                                    )
                                    appWidgetView.setImageViewBitmap(
                                        R.id.button_prev, service.getTintedDrawable(
                                            R.drawable.ic_skip_previous_outline, service.resources.getColor(com.ttop.app.appthemehelper.R.color.md_white_1000)
                                        ).toBitmap()
                                    )
                                }
                                Configuration.UI_MODE_NIGHT_NO,
                                Configuration.UI_MODE_NIGHT_UNDEFINED-> {
                                    // Set correct drawable for pause state
                                    appWidgetView.setImageViewBitmap(
                                        R.id.button_toggle_play_pause,
                                        service.getTintedDrawable(
                                            playPauseRes, service.resources.getColor(com.ttop.app.appthemehelper.R.color.md_black_1000)
                                        ).toBitmap()
                                    )

                                    // Set prev/next button drawables
                                    appWidgetView.setImageViewBitmap(
                                        R.id.button_next,
                                        service.getTintedDrawable(
                                            R.drawable.ic_skip_next_outline, service.resources.getColor(com.ttop.app.appthemehelper.R.color.md_black_1000)
                                        ).toBitmap()
                                    )
                                    appWidgetView.setImageViewBitmap(
                                        R.id.button_prev, service.getTintedDrawable(
                                            R.drawable.ic_skip_previous_outline, service.resources.getColor(com.ttop.app.appthemehelper.R.color.md_black_1000)
                                        ).toBitmap()
                                    )
                                }
                            }
                        }else {
                            when (PreferenceUtil.buttonColorOnWidgets) {
                                "default_color" -> {
                                    // Set correct drawable for pause state
                                    appWidgetView.setImageViewBitmap(
                                        R.id.button_toggle_play_pause,
                                        service.getTintedDrawable(
                                            playPauseRes, color
                                        ).toBitmap()
                                    )
                                    // Set prev/next button drawables
                                    appWidgetView.setImageViewBitmap(
                                        R.id.button_next,
                                        service.getTintedDrawable(
                                            R.drawable.ic_skip_next_outline, color
                                        ).toBitmap()
                                    )
                                    appWidgetView.setImageViewBitmap(
                                        R.id.button_prev, service.getTintedDrawable(
                                            R.drawable.ic_skip_previous_outline, color
                                        ).toBitmap()
                                    )
                                }
                                "black" -> {
                                    // Set correct drawable for pause state
                                    appWidgetView.setImageViewBitmap(
                                        R.id.button_toggle_play_pause,
                                        service.getTintedDrawable(
                                            playPauseRes,
                                            service.resources.getColor(com.ttop.app.appthemehelper.R.color.md_black_1000)
                                        ).toBitmap()
                                    )
                                    // Set prev/next button drawables
                                    appWidgetView.setImageViewBitmap(
                                        R.id.button_next,
                                        service.getTintedDrawable(
                                            R.drawable.ic_skip_next_outline,
                                            service.resources.getColor(com.ttop.app.appthemehelper.R.color.md_black_1000)
                                        ).toBitmap()
                                    )
                                    appWidgetView.setImageViewBitmap(
                                        R.id.button_prev, service.getTintedDrawable(
                                            R.drawable.ic_skip_previous_outline,
                                            service.resources.getColor(com.ttop.app.appthemehelper.R.color.md_black_1000)
                                        ).toBitmap()
                                    )
                                }
                                "white" -> {
                                    // Set correct drawable for pause state
                                    appWidgetView.setImageViewBitmap(
                                        R.id.button_toggle_play_pause,
                                        service.getTintedDrawable(
                                            playPauseRes,
                                            service.resources.getColor(com.ttop.app.appthemehelper.R.color.md_white_1000)
                                        ).toBitmap()
                                    )
                                    // Set prev/next button drawables
                                    appWidgetView.setImageViewBitmap(
                                        R.id.button_next,
                                        service.getTintedDrawable(
                                            R.drawable.ic_skip_next_outline,
                                            service.resources.getColor(com.ttop.app.appthemehelper.R.color.md_white_1000)
                                        ).toBitmap()
                                    )
                                    appWidgetView.setImageViewBitmap(
                                        R.id.button_prev, service.getTintedDrawable(
                                            R.drawable.ic_skip_previous_outline,
                                            service.resources.getColor(com.ttop.app.appthemehelper.R.color.md_white_1000)
                                        ).toBitmap()
                                    )
                                }
                                "accent" -> {
                                    // Set correct drawable for pause state
                                    appWidgetView.setImageViewBitmap(
                                        R.id.button_toggle_play_pause,
                                        service.getTintedDrawable(
                                            playPauseRes, service.accentColor()
                                        ).toBitmap()
                                    )
                                    // Set prev/next button drawables
                                    appWidgetView.setImageViewBitmap(
                                        R.id.button_next,
                                        service.getTintedDrawable(
                                            R.drawable.ic_skip_next_outline,
                                            service.accentColor()
                                        ).toBitmap()
                                    )
                                    appWidgetView.setImageViewBitmap(
                                        R.id.button_prev, service.getTintedDrawable(
                                            R.drawable.ic_skip_previous_outline,
                                            service.accentColor()
                                        ).toBitmap()
                                    )
                                }
                                "custom" -> {
                                    // Set correct drawable for pause state
                                    appWidgetView.setImageViewBitmap(
                                        R.id.button_toggle_play_pause,
                                        service.getTintedDrawable(
                                            playPauseRes, PreferenceUtil.customWidgetColor
                                        ).toBitmap()
                                    )
                                    // Set prev/next button drawables
                                    appWidgetView.setImageViewBitmap(
                                        R.id.button_next,
                                        service.getTintedDrawable(
                                            R.drawable.ic_skip_next_outline,
                                            PreferenceUtil.customWidgetColor
                                        ).toBitmap()
                                    )
                                    appWidgetView.setImageViewBitmap(
                                        R.id.button_prev, service.getTintedDrawable(
                                            R.drawable.ic_skip_previous_outline,
                                            PreferenceUtil.customWidgetColor
                                        ).toBitmap()
                                    )
                                }
                            }
                        }

                        if (bitmap == null) {
                            createDefaultCircle(service, appWidgetView, appWidgetIds, playPauseRes)
                        }else {
                            appWidgetView.setImageViewBitmap(R.id.image, bitmap)
                            pushUpdate(service, appWidgetIds, appWidgetView)
                        }
                    }
                })
        }
    }

    /**
     * Link up various button actions using [PendingIntent].
     */
    private fun linkButtons(context: Context, views: RemoteViews) {
        val action = Intent(context, MainActivity::class.java)
            .putExtra(
                MainActivity.EXPAND_PANEL,
                PreferenceUtil.isExpandPanel
            )

        val serviceName = ComponentName(context, MusicService::class.java)

        // Home
        action.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        var pendingIntent = PendingIntent.getActivity(
            context, 0, action, if (VersionUtils.hasOreo())
                PendingIntent.FLAG_IMMUTABLE
            else 0
        )
        views.setOnClickPendingIntent(R.id.image, pendingIntent)

        // Previous track
        pendingIntent = buildPendingIntent(context, ACTION_REWIND, serviceName)
        views.setOnClickPendingIntent(R.id.button_prev, pendingIntent)

        // Play and pause
        pendingIntent = buildPendingIntent(context, ACTION_TOGGLE_PAUSE, serviceName)
        views.setOnClickPendingIntent(R.id.button_toggle_play_pause, pendingIntent)

        // Next track
        pendingIntent = buildPendingIntent(context, ACTION_SKIP, serviceName)
        views.setOnClickPendingIntent(R.id.button_next, pendingIntent)

        // Update track
        pendingIntent = buildPendingIntent(context, MusicService.ACTION_UPDATE, serviceName)
        views.setOnClickPendingIntent(R.id.button_update, pendingIntent)
    }

    private fun createDefaultCircle(service: MusicService,appWidgetView: RemoteViews, appWidgetIds: IntArray?, playPauseRes: Int) {
        val song = service.currentSong

        target = Glide.with(service).asBitmapPalette().songCoverOptions(song)
            .load(R.drawable.default_audio_art)
            .circleCrop()
            .into(object : SimpleTarget<BitmapPaletteWrapper>(
               imageSize,
                imageSize
            ) {
                override fun onResourceReady(
                    resource: BitmapPaletteWrapper,
                    transition: Transition<in BitmapPaletteWrapper>?
                ) {
                    val palette = resource.palette
                    update(
                        resource.bitmap, palette.getVibrantColor(
                            palette.getMutedColor(
                                MaterialValueHelper.getSecondaryTextColor(
                                    service, true
                                )
                            )
                        )
                    )
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    update(null, MaterialValueHelper.getSecondaryTextColor(service, true))
                }

                private fun update(bitmap: Bitmap?, color: Int) {
                    if (PreferenceUtil.widgetBackground == "day_night") {
                        when (service.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                            Configuration.UI_MODE_NIGHT_YES -> {
                                // Set correct drawable for pause state
                                appWidgetView.setImageViewBitmap(
                                    R.id.button_toggle_play_pause,
                                    service.getTintedDrawable(
                                        playPauseRes, service.resources.getColor(com.ttop.app.appthemehelper.R.color.md_white_1000)
                                    ).toBitmap()
                                )

                                // Set prev/next button drawables
                                appWidgetView.setImageViewBitmap(
                                    R.id.button_next,
                                    service.getTintedDrawable(
                                        R.drawable.ic_skip_next_outline, service.resources.getColor(com.ttop.app.appthemehelper.R.color.md_white_1000)
                                    ).toBitmap()
                                )
                                appWidgetView.setImageViewBitmap(
                                    R.id.button_prev, service.getTintedDrawable(
                                        R.drawable.ic_skip_previous_outline, service.resources.getColor(com.ttop.app.appthemehelper.R.color.md_white_1000)
                                    ).toBitmap()
                                )
                            }
                            Configuration.UI_MODE_NIGHT_NO,
                            Configuration.UI_MODE_NIGHT_UNDEFINED-> {
                                // Set correct drawable for pause state
                                appWidgetView.setImageViewBitmap(
                                    R.id.button_toggle_play_pause,
                                    service.getTintedDrawable(
                                        playPauseRes, service.resources.getColor(com.ttop.app.appthemehelper.R.color.md_black_1000)
                                    ).toBitmap()
                                )

                                // Set prev/next button drawables
                                appWidgetView.setImageViewBitmap(
                                    R.id.button_next,
                                    service.getTintedDrawable(
                                        R.drawable.ic_skip_next_outline, service.resources.getColor(com.ttop.app.appthemehelper.R.color.md_black_1000)
                                    ).toBitmap()
                                )
                                appWidgetView.setImageViewBitmap(
                                    R.id.button_prev, service.getTintedDrawable(
                                        R.drawable.ic_skip_previous_outline, service.resources.getColor(com.ttop.app.appthemehelper.R.color.md_black_1000)
                                    ).toBitmap()
                                )
                            }
                        }
                    }else {
                        when (PreferenceUtil.buttonColorOnWidgets) {
                            "default_color" -> {
                                // Set correct drawable for pause state
                                appWidgetView.setImageViewBitmap(
                                    R.id.button_toggle_play_pause,
                                    service.getTintedDrawable(
                                        playPauseRes, color
                                    ).toBitmap()
                                )

                                // Set prev/next button drawables
                                appWidgetView.setImageViewBitmap(
                                    R.id.button_next,
                                    service.getTintedDrawable(
                                        R.drawable.ic_skip_next_outline, color
                                    ).toBitmap()
                                )
                                appWidgetView.setImageViewBitmap(
                                    R.id.button_prev, service.getTintedDrawable(
                                        R.drawable.ic_skip_previous_outline, color
                                    ).toBitmap()
                                )
                            }
                            "black" -> {
                                // Set correct drawable for pause state
                                appWidgetView.setImageViewBitmap(
                                    R.id.button_toggle_play_pause,
                                    service.getTintedDrawable(
                                        playPauseRes,
                                        service.resources.getColor(com.ttop.app.appthemehelper.R.color.md_black_1000)
                                    ).toBitmap()
                                )

                                // Set prev/next button drawables
                                appWidgetView.setImageViewBitmap(
                                    R.id.button_next,
                                    service.getTintedDrawable(
                                        R.drawable.ic_skip_next_outline,
                                        service.resources.getColor(com.ttop.app.appthemehelper.R.color.md_black_1000)
                                    ).toBitmap()
                                )
                                appWidgetView.setImageViewBitmap(
                                    R.id.button_prev, service.getTintedDrawable(
                                        R.drawable.ic_skip_previous_outline,
                                        service.resources.getColor(com.ttop.app.appthemehelper.R.color.md_black_1000)
                                    ).toBitmap()
                                )
                            }
                            "white" -> {
                                // Set correct drawable for pause state
                                appWidgetView.setImageViewBitmap(
                                    R.id.button_toggle_play_pause,
                                    service.getTintedDrawable(
                                        playPauseRes,
                                        service.resources.getColor(com.ttop.app.appthemehelper.R.color.md_white_1000)
                                    ).toBitmap()
                                )

                                // Set prev/next button drawables
                                appWidgetView.setImageViewBitmap(
                                    R.id.button_next,
                                    service.getTintedDrawable(
                                        R.drawable.ic_skip_next_outline,
                                        service.resources.getColor(com.ttop.app.appthemehelper.R.color.md_white_1000)
                                    ).toBitmap()
                                )
                                appWidgetView.setImageViewBitmap(
                                    R.id.button_prev, service.getTintedDrawable(
                                        R.drawable.ic_skip_previous_outline,
                                        service.resources.getColor(com.ttop.app.appthemehelper.R.color.md_white_1000)
                                    ).toBitmap()
                                )
                            }
                            "accent" -> {
                                // Set correct drawable for pause state
                                appWidgetView.setImageViewBitmap(
                                    R.id.button_toggle_play_pause,
                                    service.getTintedDrawable(
                                        playPauseRes, service.accentColor()
                                    ).toBitmap()
                                )

                                // Set prev/next button drawables
                                appWidgetView.setImageViewBitmap(
                                    R.id.button_next,
                                    service.getTintedDrawable(
                                        R.drawable.ic_skip_next_outline,
                                        service.accentColor()
                                    ).toBitmap()
                                )
                                appWidgetView.setImageViewBitmap(
                                    R.id.button_prev, service.getTintedDrawable(
                                        R.drawable.ic_skip_previous_outline,
                                        service.accentColor()
                                    ).toBitmap()
                                )
                            }
                            "custom" -> {
                                // Set correct drawable for pause state
                                appWidgetView.setImageViewBitmap(
                                    R.id.button_toggle_play_pause,
                                    service.getTintedDrawable(
                                        playPauseRes, PreferenceUtil.customWidgetColor
                                    ).toBitmap()
                                )

                                // Set prev/next button drawables
                                appWidgetView.setImageViewBitmap(
                                    R.id.button_next,
                                    service.getTintedDrawable(
                                        R.drawable.ic_skip_next_outline,
                                        PreferenceUtil.customWidgetColor
                                    ).toBitmap()
                                )
                                appWidgetView.setImageViewBitmap(
                                    R.id.button_prev, service.getTintedDrawable(
                                        R.drawable.ic_skip_previous_outline,
                                        PreferenceUtil.customWidgetColor
                                    ).toBitmap()
                                )
                            }
                        }
                    }

                    appWidgetView.setImageViewBitmap(R.id.image, bitmap)

                    pushUpdate(service, appWidgetIds, appWidgetView)
                }
            })
    }
    companion object {

        const val NAME: String = "app_widget_full"
        private var mInstance: AppWidgetFull? = null
        private var imageSize = 0
        val instance: AppWidgetFull
            @Synchronized get() {
                if (mInstance == null) {
                    mInstance = AppWidgetFull()
                }
                return mInstance!!
            }
    }
}
