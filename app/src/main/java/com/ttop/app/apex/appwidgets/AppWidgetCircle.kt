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
import android.widget.RemoteViews
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.ttop.app.apex.R
import com.ttop.app.apex.appwidgets.base.BaseAppWidget
import com.ttop.app.apex.glide.ApexGlideExtension
import com.ttop.app.apex.glide.ApexGlideExtension.asBitmapPalette
import com.ttop.app.apex.glide.ApexGlideExtension.songCoverOptions
import com.ttop.app.apex.glide.palette.BitmapPaletteWrapper
import com.ttop.app.apex.service.MusicService
import com.ttop.app.apex.service.MusicService.Companion.ACTION_TOGGLE_PAUSE
import com.ttop.app.apex.service.MusicService.Companion.TOGGLE_FAVORITE
import com.ttop.app.apex.ui.activities.MainActivity
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.MusicUtil
import com.ttop.app.apex.util.PreferenceUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class AppWidgetCircle : BaseAppWidget() {
    private var target: Target<BitmapPaletteWrapper>? = null // for cancellation

    /**
     * Initialize given widgets to default state, where we launch Music on default click and hide
     * actions if service not running.
     */
    override fun defaultAppWidget(context: Context, appWidgetIds: IntArray) {
        val appWidgetView = RemoteViews(context.packageName, R.layout.app_widget_circle_md3)

        appWidgetView.setImageViewResource(R.id.image, R.drawable.default_album_art_round)

        // Set correct drawable for pause state
        val playRes = R.drawable.ic_play_arrow_md3

        appWidgetView.setImageViewResource(
            R.id.button_toggle_play_pause, playRes
        )

        // Set correct drawable for favorite state
        val favoriteRes = R.drawable.ic_favorite_border_widget_md3

        appWidgetView.setImageViewResource(
            R.id.button_toggle_favorite, favoriteRes
        )

        linkButtons(context, appWidgetView)
        pushUpdate(context, appWidgetIds, appWidgetView)
    }

    /**
     * Update all active widget instances by pushing changes
     */
    override fun performUpdate(service: MusicService, appWidgetIds: IntArray?) {
        val appWidgetView = RemoteViews(service.packageName, R.layout.app_widget_circle_md3)

        val isPlaying = service.isPlaying
        val song = service.currentSong

        // Set correct drawable for pause state
        val playPauseRes = if (isPlaying) R.drawable.ic_pause_md3 else R.drawable.ic_play_arrow_md3

        appWidgetView.setImageViewResource(
            R.id.button_toggle_play_pause, playPauseRes
        )

        val isFavorite = runBlocking(Dispatchers.IO) {
            return@runBlocking MusicUtil.repository.isSongFavorite(song.id)
        }
        val favoriteRes =
            if (isFavorite) R.drawable.ic_favorite_widget_md3 else R.drawable.ic_favorite_border_widget_md3

        appWidgetView.setImageViewResource(
            R.id.button_toggle_favorite, favoriteRes
        )

        // Link actions buttons to intents
        linkButtons(service, appWidgetView)

        if (imageSize == 0) {
            val p = ApexUtil.getScreenSize(service)
            imageSize = p.x.coerceAtMost(p.y)
        }

        // Load the album cover async and push the update on completion
        service.runOnUiThread {
            if (target != null) {
                Glide.with(service).clear(target)
            }
            target = Glide.with(service).asBitmapPalette().songCoverOptions(song)
                .load(ApexGlideExtension.getSongModel(song))
                .placeholder(R.drawable.default_album_art_round)
                .error(R.drawable.default_album_art_round)
                .apply(RequestOptions.circleCropTransform())
                .into(object : CustomTarget<BitmapPaletteWrapper>(imageSize, imageSize) {
                    override fun onResourceReady(
                        resource: BitmapPaletteWrapper,
                        transition: Transition<in BitmapPaletteWrapper>?,
                    ) {
                        update(resource.bitmap)
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        super.onLoadFailed(errorDrawable)
                        update(null)
                    }

                    private fun update(bitmap: Bitmap?) {
                        if (bitmap == null) {
                            createDefaultCircle(service, appWidgetView, appWidgetIds, playPauseRes)
                        } else {
                            appWidgetView.setImageViewBitmap(R.id.image, bitmap)
                            pushUpdate(service, appWidgetIds, appWidgetView)
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }
    }

    private fun createDefaultCircle(
        service: MusicService,
        appWidgetView: RemoteViews,
        appWidgetIds: IntArray?,
        playPauseRes: Int
    ) {
        target = Glide.with(service).asBitmapPalette()
            .load(R.drawable.default_album_art_round)
            .circleCrop()
            .into(object : CustomTarget<BitmapPaletteWrapper>(
                imageSize, imageSize
            ) {
                override fun onResourceReady(
                    resource: BitmapPaletteWrapper,
                    transition: Transition<in BitmapPaletteWrapper>?
                ) {
                    update(resource.bitmap)
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    update(null)
                }

                override fun onLoadCleared(placeholder: Drawable?) {}

                private fun update(bitmap: Bitmap?) {
                    appWidgetView.setImageViewBitmap(R.id.image, bitmap)

                    pushUpdate(service, appWidgetIds, appWidgetView)
                }
            })
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
            context, 0, action, PendingIntent.FLAG_IMMUTABLE
        )

        views.setOnClickPendingIntent(R.id.image, pendingIntent)
        // Favorite track
        pendingIntent = buildPendingIntent(context, TOGGLE_FAVORITE, serviceName)
        views.setOnClickPendingIntent(R.id.button_toggle_favorite, pendingIntent)

        // Play and pause
        pendingIntent = buildPendingIntent(context, ACTION_TOGGLE_PAUSE, serviceName)
        views.setOnClickPendingIntent(R.id.button_toggle_play_pause, pendingIntent)
    }

    companion object {

        const val NAME = "app_widget_circle"

        private var mInstance: AppWidgetCircle? = null
        private var imageSize = 0

        val instance: AppWidgetCircle
            @Synchronized get() {
                if (mInstance == null) {
                    mInstance = AppWidgetCircle()
                }
                return mInstance!!
            }
    }
}