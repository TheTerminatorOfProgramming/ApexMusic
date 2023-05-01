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
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.ttop.app.apex.R
import com.ttop.app.apex.appwidgets.base.BaseAppWidget
import com.ttop.app.apex.extensions.getTintedDrawable
import com.ttop.app.apex.glide.ApexGlideExtension
import com.ttop.app.apex.glide.WidgetBlurTransform
import com.ttop.app.apex.service.MusicService
import com.ttop.app.apex.service.MusicService.Companion.ACTION_REWIND
import com.ttop.app.apex.service.MusicService.Companion.ACTION_SKIP
import com.ttop.app.apex.service.MusicService.Companion.ACTION_TOGGLE_PAUSE
import com.ttop.app.apex.ui.activities.MainActivity
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.appthemehelper.util.MaterialValueHelper
import com.ttop.app.appthemehelper.util.VersionUtils

class AppWidgetSquare : BaseAppWidget() {
    private var target: Target<Bitmap>? = null // for cancellation

    /**
     * Initialize given widgets to default state, where we launch Music on default click and hide
     * actions if service not running.
     */
    override fun defaultAppWidget(context: Context, appWidgetIds: IntArray) {
        val appWidgetView = RemoteViews(
            context.packageName, R.layout.app_widget_square
        )

        appWidgetView.setViewVisibility(
            R.id.title,
            View.INVISIBLE
        )

        appWidgetView.setViewVisibility(
            R.id.text,
            View.INVISIBLE
        )

        appWidgetView.setImageViewResource(R.id.image, R.drawable.default_audio_art)
        appWidgetView.setImageViewBitmap(
            R.id.button_next, context.getTintedDrawable(
                R.drawable.ic_skip_next_outline,
                MaterialValueHelper.getPrimaryTextColor(context, false)
            ).toBitmap()
        )
        appWidgetView.setImageViewBitmap(
            R.id.button_prev, context.getTintedDrawable(
                R.drawable.ic_skip_previous_outline,  MaterialValueHelper.getPrimaryTextColor(context, false)
            ).toBitmap()
        )
        appWidgetView.setImageViewBitmap(
            R.id.button_toggle_play_pause, context.getTintedDrawable(
                R.drawable.ic_play_arrow_outline_small,  MaterialValueHelper.getPrimaryTextColor(context, false)
            ).toBitmap()
        )

        linkButtons(context, appWidgetView)

        /*if (MusicPlayerRemote.playingQueue.isNotEmpty()){
            if (!MusicPlayerRemote.isPlaying){
                MusicPlayerRemote.resumePlaying()
                MusicPlayerRemote.pauseSong()
            }else{
                MusicPlayerRemote.pauseSong()
                MusicPlayerRemote.resumePlaying()
            }
        }*/

        pushUpdate(context, appWidgetIds, appWidgetView)
    }

    /**
     * Update all active widget instances by pushing changes
     */
    override fun performUpdate(service: MusicService, appWidgetIds: IntArray?) {
        val appWidgetView = RemoteViews(
            service.packageName, R.layout.app_widget_square
        )

        val isPlaying = service.isPlaying
        val song = service.currentSong

        // Set the titles and artwork
        if (song.title.isEmpty() && song.artistName.isEmpty()) {
            appWidgetView.setViewVisibility(
                R.id.title,
                View.INVISIBLE
            )
            appWidgetView.setViewVisibility(
                R.id.text,
                View.INVISIBLE
            )
            appWidgetView.setViewVisibility(
                R.id.progress_bar,
                View.INVISIBLE
            )
            appWidgetView.setViewVisibility(
                R.id.button_next,
                View.INVISIBLE
            )
            appWidgetView.setViewVisibility(
                R.id.button_prev,
                View.INVISIBLE
            )
        } else {
            if (PreferenceUtil.hideSquareWidgetText) {
                appWidgetView.setViewVisibility(
                    R.id.title,
                    View.INVISIBLE
                )
                appWidgetView.setViewVisibility(
                    R.id.text,
                    View.INVISIBLE
                )
            }else {
                appWidgetView.setViewVisibility(
                    R.id.title,
                    View.VISIBLE
                )
                appWidgetView.setViewVisibility(
                    R.id.text,
                    View.VISIBLE
                )
                appWidgetView.setTextViewText(R.id.title, song.title)
                appWidgetView.setTextViewText(
                    R.id.text,
                    getSongArtist(song)
                )
            }
            appWidgetView.setViewVisibility(
                R.id.progress_bar,
                View.VISIBLE
            )
            appWidgetView.setViewVisibility(
                R.id.button_next,
                View.VISIBLE
            )
            appWidgetView.setViewVisibility(
                R.id.button_prev,
                View.VISIBLE
            )
        }

        val primaryColor = MaterialValueHelper.getPrimaryTextColor(service, false)
        // Set correct drawable for pause state
        if (PreferenceUtil.coloredSquareWidget) {
            val playPauseRes =
                if (isPlaying) R.drawable.ic_pause_outline_small else R.drawable.ic_play_arrow_outline_small
            appWidgetView.setImageViewBitmap(
                R.id.button_toggle_play_pause,
                service.getTintedDrawable(
                    playPauseRes,
                    service.resources.getColor(R.color.md_red_500)
                ).toBitmap()
            )
        }else {
            val playPauseRes =
                if (isPlaying) R.drawable.ic_pause_outline_small else R.drawable.ic_play_arrow_outline_small
            appWidgetView.setImageViewBitmap(
                R.id.button_toggle_play_pause,
                service.getTintedDrawable(
                    playPauseRes,
                    primaryColor
                ).toBitmap()
            )
        }


        // Set prev/next button drawables
        appWidgetView.setImageViewBitmap(
            R.id.button_next,
            service.getTintedDrawable(
                R.drawable.ic_skip_next_outline,
                primaryColor
            ).toBitmap()
        )
        appWidgetView.setImageViewBitmap(
            R.id.button_prev,
            service.getTintedDrawable(
                R.drawable.ic_skip_previous_outline,
                primaryColor
            ).toBitmap()
        )

        // Link actions buttons to intents
        linkButtons(service, appWidgetView)

        appWidgetView.setProgressBar(R.id.progress_bar, service.songDurationMillis, service.songProgressMillis, false)

        if (imageSize == 0) {
            imageSize = 250
        }

        // Load the album cover async and push the update on completion
        val appContext = service.applicationContext
        service.runOnUiThread {
            if (target != null) {
                Glide.with(service).clear(target)
            }
            target = Glide.with(appContext)
                .asBitmap()
                .load(ApexGlideExtension.getSongModel(song))
                .transform(CenterCrop(), WidgetBlurTransform(service))
                .into(object : SimpleTarget<Bitmap>(imageSize, imageSize) {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        update(resource)
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        super.onLoadFailed(errorDrawable)
                        update(null)
                    }

                    private fun update(bitmap: Bitmap?) {
                        if (bitmap == null) {
                            appWidgetView.setImageViewResource(
                                R.id.image,
                                R.drawable.default_audio_art
                            )
                        } else {
                            appWidgetView.setImageViewBitmap(R.id.image, bitmap)
                        }
                        pushUpdate(appContext, appWidgetIds, appWidgetView)
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

    companion object {

        const val NAME: String = "app_widget_square"
        private var mInstance: AppWidgetSquare? = null
        private var imageSize = 0
        val instance: AppWidgetSquare
            @Synchronized get() {
                if (mInstance == null) {
                    mInstance = AppWidgetSquare()
                }
                return mInstance!!
            }
    }
}