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
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.RemoteViews
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.ttop.app.apex.R
import com.ttop.app.apex.appwidgets.base.BaseAppWidget
import com.ttop.app.apex.extensions.getTintedDrawable
import com.ttop.app.apex.glide.ApexGlideExtension
import com.ttop.app.apex.glide.ApexGlideExtension.asBitmapPalette
import com.ttop.app.apex.glide.ApexGlideExtension.songCoverOptions
import com.ttop.app.apex.glide.BlurTransformation
import com.ttop.app.apex.glide.palette.BitmapPaletteWrapper
import com.ttop.app.apex.service.MusicService
import com.ttop.app.apex.service.MusicService.Companion.ACTION_REWIND
import com.ttop.app.apex.service.MusicService.Companion.ACTION_SKIP
import com.ttop.app.apex.service.MusicService.Companion.ACTION_TOGGLE_PAUSE
import com.ttop.app.apex.ui.activities.MainActivity
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.MusicUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.appthemehelper.util.MaterialValueHelper


class AppWidgetBig : BaseAppWidget() {
    private var target: Target<BitmapPaletteWrapper>? = null // for cancellation
    private var _binding: AppWidgetBig? = null
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
        val appWidgetView = if (PreferenceUtil.widgetStyle) {
            RemoteViews(context.packageName, R.layout.app_widget_big_transparent)
        }else {
            RemoteViews(context.packageName, R.layout.app_widget_big_day_night)
        }

        appWidgetView.setViewVisibility(R.id.media_titles, View.INVISIBLE)
        appWidgetView.setImageViewResource(R.id.image, R.drawable.default_album_art_round)

        val secondaryColor = MaterialValueHelper.getSecondaryTextColor(context, true)
        appWidgetView.setImageViewBitmap(
            R.id.button_next,
            context.getTintedDrawable(
                R.drawable.ic_skip_next_small,
                secondaryColor
            ).toBitmap()
        )
        appWidgetView.setImageViewBitmap(
            R.id.button_prev,
            context.getTintedDrawable(
                R.drawable.ic_skip_previous_small,
                secondaryColor
            ).toBitmap()
        )
        appWidgetView.setImageViewBitmap(
            R.id.button_toggle_play_pause,
            context.getTintedDrawable(
                R.drawable.ic_play_arrow_small,
                secondaryColor
            ).toBitmap()
        )

        appWidgetView.setImageViewBitmap(
            R.id.button_update,
            context.getTintedDrawable(
                R.drawable.ic_refresh,
                secondaryColor
            ).toBitmap()
        )

        appWidgetView.setImageViewBitmap(
            R.id.button_open,
            context.getTintedDrawable(
                R.drawable.app_icon_white,
                secondaryColor
            ).toBitmap()
        )

        linkButtons(context, appWidgetView)

        pushUpdate(context, appWidgetIds, appWidgetView)
    }

    /**
     * Update all active widget instances by pushing changes
     */
    override fun performUpdate(service: MusicService, appWidgetIds: IntArray?) {
        val appWidgetView = if (PreferenceUtil.widgetStyle) {
            RemoteViews(service.packageName, R.layout.app_widget_big_transparent)
        }else {
            RemoteViews(service.packageName, R.layout.app_widget_big_day_night)
        }

        val isPlaying = service.isPlaying
        val song = service.currentSong

        // Set the titles and artwork
        if (song.title.isEmpty() && song.artistName.isEmpty()) {
            appWidgetView.setViewVisibility(R.id.media_titles, View.INVISIBLE)
        } else {
            appWidgetView.setViewVisibility(R.id.media_titles, View.VISIBLE)
            if (ApexUtil.isLandscape && !ApexUtil.isTablet) {
                appWidgetView.setTextViewText(R.id.title, song.title + " • " + getSongArtist(song))
            }else {
                appWidgetView.setTextViewText(R.id.title, song.title)
                appWidgetView.setTextViewText(R.id.text, getSongArtist(song))
            }
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
            if (isPlaying) R.drawable.ic_pause_small else R.drawable.ic_play_arrow_small
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
                R.drawable.ic_skip_next_small,
                MaterialValueHelper.getSecondaryTextColor(service, true)
            ).toBitmap()
        )
        appWidgetView.setImageViewBitmap(
            R.id.button_prev,
            service.getTintedDrawable(
                R.drawable.ic_skip_previous_small,
                MaterialValueHelper.getSecondaryTextColor(service, true)
            ).toBitmap()
        )
        appWidgetView.setImageViewBitmap(
            R.id.button_toggle_play_pause,
            service.getTintedDrawable(
                R.drawable.ic_play_arrow_small,
                MaterialValueHelper.getSecondaryTextColor(service, true)
            ).toBitmap()
        )
        appWidgetView.setImageViewBitmap(
            R.id.refresh_button,
            service.getTintedDrawable(
                R.drawable.ic_refresh,
                MaterialValueHelper.getSecondaryTextColor(service, true)
            ).toBitmap()
        )
        appWidgetView.setImageViewBitmap(
            R.id.button_open,
            service.getTintedDrawable(
                R.drawable.app_icon_white,
                MaterialValueHelper.getSecondaryTextColor(service, true)
            ).toBitmap()
        )
        // Link actions buttons to intents
        linkButtons(service, appWidgetView)

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
                .transform(BlurTransformation.Builder(service).build())
                .placeholder(R.drawable.default_album_art_round)
                .error(R.drawable.default_album_art_round)
                .into(object : CustomTarget<BitmapPaletteWrapper>(
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

                    override fun onLoadCleared(placeholder: Drawable?) {}

                    private fun update(bitmap: Bitmap?, color: Int) {
                        // Set correct drawable for pause state
                        appWidgetView.setImageViewBitmap(
                            R.id.button_toggle_play_pause,
                            service.getTintedDrawable(
                                playPauseRes,
                                ContextCompat.getColor(service, com.ttop.app.appthemehelper.R.color.md_white_1000)
                            ).toBitmap()
                        )
                        // Set prev/next button drawables
                        appWidgetView.setImageViewBitmap(
                            R.id.button_next,
                            service.getTintedDrawable(
                                R.drawable.ic_skip_next_small,
                                ContextCompat.getColor(service, com.ttop.app.appthemehelper.R.color.md_white_1000)
                            ).toBitmap()
                        )
                        appWidgetView.setImageViewBitmap(
                            R.id.button_prev, service.getTintedDrawable(
                                R.drawable.ic_skip_previous_small,
                                ContextCompat.getColor(service, com.ttop.app.appthemehelper.R.color.md_white_1000)
                            ).toBitmap()
                        )

                        appWidgetView.setImageViewBitmap(
                            R.id.button_update, service.getTintedDrawable(
                                R.drawable.ic_refresh,
                                ContextCompat.getColor(service, com.ttop.app.appthemehelper.R.color.md_white_1000)
                            ).toBitmap()
                        )

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
                PreferenceUtil.isExpandPanel != "disabled"
            )

        val serviceName = ComponentName(context, MusicService::class.java)

        // Home
        action.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        var pendingIntent = PendingIntent.getActivity(
            context, 0, action, PendingIntent.FLAG_IMMUTABLE)

        views.setOnClickPendingIntent(R.id.button_open, pendingIntent)

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
            .load(R.drawable.default_album_art_round)
            .transform(BlurTransformation.Builder(service).build())
            .placeholder(R.drawable.default_album_art_round)
            .error(R.drawable.default_album_art_round)
            .into(object : CustomTarget<BitmapPaletteWrapper>(
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

                override fun onLoadCleared(placeholder: Drawable?) {}

                private fun update(bitmap: Bitmap?, color: Int) {
                    // Set correct drawable for pause state
                    appWidgetView.setImageViewBitmap(
                        R.id.button_toggle_play_pause,
                        service.getTintedDrawable(
                            playPauseRes,
                            ContextCompat.getColor(service, com.ttop.app.appthemehelper.R.color.md_white_1000)
                        ).toBitmap()
                    )
                    // Set prev/next button drawables
                    appWidgetView.setImageViewBitmap(
                        R.id.button_next,
                        service.getTintedDrawable(
                            R.drawable.ic_skip_next_small,
                            ContextCompat.getColor(service, com.ttop.app.appthemehelper.R.color.md_white_1000)
                        ).toBitmap()
                    )
                    appWidgetView.setImageViewBitmap(
                        R.id.button_prev, service.getTintedDrawable(
                            R.drawable.ic_skip_previous_small,
                            ContextCompat.getColor(service, com.ttop.app.appthemehelper.R.color.md_white_1000)
                        ).toBitmap()
                    )

                    appWidgetView.setImageViewBitmap(
                        R.id.button_update, service.getTintedDrawable(
                            R.drawable.ic_refresh,
                            ContextCompat.getColor(service, com.ttop.app.appthemehelper.R.color.md_white_1000)
                        ).toBitmap()
                    )

                    if (bitmap == null) {
                        createDefaultCircle(service, appWidgetView, appWidgetIds, playPauseRes)
                    }else {
                        appWidgetView.setImageViewBitmap(R.id.image, bitmap)
                        pushUpdate(service, appWidgetIds, appWidgetView)
                    }
                }
            })
    }
    companion object {

        const val NAME: String = "app_widget_big"
        private var mInstance: AppWidgetBig? = null
        private var imageSize = 0
        val instance: AppWidgetBig
            @Synchronized get() {
                if (mInstance == null) {
                    mInstance = AppWidgetBig()
                }
                return mInstance!!
            }
    }
}