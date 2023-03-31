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
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.ttop.app.apex.R
import com.ttop.app.apex.appwidgets.base.BaseAppWidget
import com.ttop.app.apex.extensions.getTintedDrawable
import com.ttop.app.apex.glide.ApexGlideExtension
import com.ttop.app.apex.glide.ApexGlideExtension.asBitmapPalette
import com.ttop.app.apex.glide.ApexGlideExtension.songCoverOptions
import com.ttop.app.apex.glide.palette.BitmapPaletteWrapper
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.service.MusicService
import com.ttop.app.apex.service.MusicService.Companion.ACTION_REWIND
import com.ttop.app.apex.service.MusicService.Companion.ACTION_SKIP
import com.ttop.app.apex.service.MusicService.Companion.ACTION_TOGGLE_PAUSE
import com.ttop.app.apex.ui.activities.MainActivity
import com.ttop.app.apex.util.DensityUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.appthemehelper.util.MaterialValueHelper
import com.ttop.app.appthemehelper.util.VersionUtils


class AppWidgetClassic : BaseAppWidget() {
    private var target: Target<BitmapPaletteWrapper>? = null // for cancellation

    /**
     * Initialize given widgets to default state, where we launch Music on default click and hide
     * actions if service not running.
     */
    override fun defaultAppWidget(context: Context, appWidgetIds: IntArray) {
        var appWidgetView: RemoteViews? = null

        appWidgetView = if (VersionUtils.hasS()) {
            if (PreferenceUtil.widgetColors) {
                RemoteViews(context.packageName, R.layout.app_widget_classic_day_night)
            } else if (PreferenceUtil.widgetTransparency) {
                RemoteViews(context.packageName, R.layout.app_widget_classic_transparent)
            }else {
                RemoteViews(context.packageName, R.layout.app_widget_classic)
            }
        } else {
            if (PreferenceUtil.widgetTransparency) {
                RemoteViews(context.packageName, R.layout.app_widget_classic_transparent)
            }else {
                RemoteViews(context.packageName, R.layout.app_widget_classic)
            }
        }

        appWidgetView.setViewVisibility(R.id.media_titles, View.INVISIBLE)
        appWidgetView.setImageViewResource(R.id.image, R.drawable.default_audio_art)
        if (PreferenceUtil.widgetColors) {
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

        if (MusicPlayerRemote.playingQueue.isNotEmpty()){
            if (!MusicPlayerRemote.isPlaying){
                MusicPlayerRemote.resumePlaying()
                MusicPlayerRemote.pauseSong()
            }else{
                MusicPlayerRemote.pauseSong()
                MusicPlayerRemote.resumePlaying()
            }
        }

        pushUpdate(context, appWidgetIds, appWidgetView)
    }

    /**
     * Update all active widget instances by pushing changes
     */
    override fun performUpdate(service: MusicService, appWidgetIds: IntArray?) {
        var appWidgetView: RemoteViews? = null

        appWidgetView = if (VersionUtils.hasS()) {
            if (PreferenceUtil.widgetColors) {
                RemoteViews(service.packageName, R.layout.app_widget_classic_day_night)
            } else if (PreferenceUtil.widgetTransparency) {
                RemoteViews(service.packageName, R.layout.app_widget_classic_transparent)
            } else {
                RemoteViews(service.packageName, R.layout.app_widget_classic)
            }
        } else {
            if (PreferenceUtil.widgetTransparency) {
                RemoteViews(service.packageName, R.layout.app_widget_classic_transparent)
            } else {
                RemoteViews(service.packageName, R.layout.app_widget_classic)
            }
        }

        val isPlaying = service.isPlaying
        val song = service.currentSong

        // Set the titles and artwork
        if (song.title.isEmpty() && song.artistName.isEmpty()) {
            appWidgetView.setViewVisibility(R.id.media_titles, View.INVISIBLE)
        } else {
            appWidgetView.setViewVisibility(R.id.media_titles, View.VISIBLE)
            appWidgetView.setTextViewText(R.id.title, song.title)
            appWidgetView.setTextViewText(R.id.text, getSongArtist(song))
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

        if (imageSize == 0) {
            imageSize =
                service.resources.getDimensionPixelSize(R.dimen.app_widget_card_image_size)
        }

        // Load the album cover async and push the update on completion
        service.runOnUiThread {
            if (target != null) {
                Glide.with(service).clear(target)
            }

            if (PreferenceUtil.isClassicCircle) {
                target = Glide.with(service).asBitmapPalette().songCoverOptions(song)
                    .load(ApexGlideExtension.getSongModel(song))
                    .placeholder(R.drawable.default_audio_art)
                    .error(R.drawable.default_audio_art)
                    .circleCrop()
                    .into(object : SimpleTarget<BitmapPaletteWrapper>(imageSize, imageSize) {
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

                            if (bitmap == null) {
                                createDefaultCircle(service, appWidgetView, appWidgetIds, playPauseRes)
                            }else {
                                appWidgetView.setImageViewBitmap(R.id.image, bitmap)
                                pushUpdate(service, appWidgetIds, appWidgetView)
                            }
                        }
                    })
            } else {
                target = Glide.with(service).asBitmapPalette().songCoverOptions(song)
                    .load(ApexGlideExtension.getSongModel(song))
                    .centerCrop()
                    .into(object : SimpleTarget<BitmapPaletteWrapper>(imageSize, imageSize) {
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

                            val image = getAlbumArtDrawable(service.resources, bitmap)
                            val roundedBitmap = createRoundedBitmap(
                                image,
                                imageSize,
                                imageSize,
                                DensityUtil.dip2px(service, PreferenceUtil.widgetImage.toFloat())
                                    .toFloat(),
                                DensityUtil.dip2px(service, PreferenceUtil.widgetImage.toFloat())
                                    .toFloat(),
                                DensityUtil.dip2px(service, PreferenceUtil.widgetImage.toFloat())
                                    .toFloat(),
                                DensityUtil.dip2px(service, PreferenceUtil.widgetImage.toFloat())
                                    .toFloat()
                            )
                            appWidgetView.setImageViewBitmap(R.id.image, roundedBitmap)

                            pushUpdate(service, appWidgetIds, appWidgetView)
                        }
                    })
            }
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
            context, 0, action, if (VersionUtils.hasMarshmallow())
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
    }

    private fun createDefaultCircle(service: MusicService,appWidgetView: RemoteViews, appWidgetIds: IntArray?, playPauseRes: Int) {
        val song = service.currentSong

        target = Glide.with(service).asBitmapPalette().songCoverOptions(song)
            .load(R.drawable.default_audio_art)
            .circleCrop()
            .into(object : SimpleTarget<BitmapPaletteWrapper>(imageSize, imageSize) {
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

                    appWidgetView.setImageViewBitmap(R.id.image, bitmap)

                    pushUpdate(service, appWidgetIds, appWidgetView)
                }
            })
    }
    companion object {

        const val NAME = "app_widget_md3"

        private var mInstance: AppWidgetClassic? = null
        private var imageSize = 0

        val instance: AppWidgetClassic
            @Synchronized get() {
                if (mInstance == null) {
                    mInstance = AppWidgetClassic()
                }
                return mInstance!!
            }
    }
}
