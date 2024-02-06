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
package com.ttop.app.apex.appwidgets.base

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.model.Song
import com.ttop.app.apex.service.MusicService
import com.ttop.app.apex.service.MusicService.Companion.APP_WIDGET_UPDATE
import com.ttop.app.apex.service.MusicService.Companion.EXTRA_APP_WIDGET_NAME
import com.ttop.app.apex.service.MusicService.Companion.FAVORITE_STATE_CHANGED
import com.ttop.app.apex.service.MusicService.Companion.META_CHANGED
import com.ttop.app.apex.service.MusicService.Companion.PLAY_STATE_CHANGED
import com.ttop.app.apex.service.MusicService.Companion.SAVED_POSITION_IN_TRACK

abstract class BaseAppWidget : AppWidgetProvider() {
    private val musicService = MusicPlayerRemote.musicService
    /**
     * {@inheritDoc}
     */

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        defaultAppWidget(context, appWidgetIds)
        val updateIntent = Intent(APP_WIDGET_UPDATE)
        updateIntent.putExtra(EXTRA_APP_WIDGET_NAME, NAME)
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        updateIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY)
        context.sendBroadcast(updateIntent)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        musicService?.let { performUpdate(it, null) }

        val serviceIntent = Intent(context, MusicService::class.java)
        context.startForegroundService(serviceIntent)
    }

    /**
     * Handle a change notification coming over from [MusicService]
     */
    fun notifyChange(service: MusicService, what: String) {
        if (hasInstances(service)) {
            if (META_CHANGED == what || PLAY_STATE_CHANGED == what || FAVORITE_STATE_CHANGED == what || SAVED_POSITION_IN_TRACK == what) {
                performUpdate(service, null)
            }
        }
    }

    open fun notifyThemeChange(service: MusicService?) {
        if (service != null) {
            if (hasInstances(service.applicationContext)) {
                performUpdate(service, null)
            }
        }
    }

    protected fun pushUpdate(
        context: Context,
        appWidgetIds: IntArray?,
        views: RemoteViews
    ) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        if (appWidgetIds != null) {
            appWidgetManager.updateAppWidget(appWidgetIds, views)
        } else {
            appWidgetManager.updateAppWidget(ComponentName(context, javaClass), views)
        }
    }

    /**
     * Check against [AppWidgetManager] if there are any instances of this widget.
     */
    private fun hasInstances(context: Context): Boolean {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val mAppWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(
                context, javaClass
            )
        )
        return mAppWidgetIds.isNotEmpty()
    }

    protected fun buildPendingIntent(
        context: Context,
        action: String,
        serviceName: ComponentName
    ): PendingIntent {
        val intent = Intent(action)
        intent.component = serviceName
        return  PendingIntent.getForegroundService(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    protected abstract fun defaultAppWidget(context: Context, appWidgetIds: IntArray)

    abstract fun performUpdate(service: MusicService, appWidgetIds: IntArray?)

    protected fun getSongArtist(song: Song): String {
        return song.artistName
    }

    companion object {

        const val NAME: String = "app_widget"
    }
}