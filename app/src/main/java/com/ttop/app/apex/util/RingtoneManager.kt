/*
 * Copyright (c) 2019 Hemanth Savarala.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by
 *  the Free Software Foundation either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package com.ttop.app.apex.util

import android.content.Context
import android.content.Intent
import android.provider.BaseColumns
import android.provider.MediaStore
import android.provider.Settings
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakewharton.processphoenix.ProcessPhoenix
import com.ttop.app.apex.BuildConfig
import com.ttop.app.apex.R
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.extensions.withCenteredButtons
import com.ttop.app.apex.model.Song
import com.ttop.app.apex.util.MusicUtil.getSongFileUri
import java.io.File

object RingtoneManager {
    fun setRingtone(context: Context, song: Song) {
        val uri = getSongFileUri(song.id)
        val resolver = context.contentResolver

        try {
            val cursor = resolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.MediaColumns.TITLE),
                BaseColumns._ID + "=?",
                arrayOf(song.id.toString()), null
            )
            cursor.use { cursorSong ->
                if (cursorSong != null && cursorSong.count == 1) {
                    cursorSong.moveToFirst()
                    Settings.System.putString(resolver, Settings.System.RINGTONE, uri.toString())
                    val message = context
                        .getString(R.string.x_has_been_set_as_ringtone, cursorSong.getString(0))
                    context.showToast(message)
                }
            }
        } catch (ignored: SecurityException) {
        }
    }

    fun requiresDialog(context: Context): Boolean {
        return !Settings.System.canWrite(context)
    }

    fun showDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.dialog_title_set_ringtone)
        builder.setMessage(R.string.dialog_message_set_ringtone)

        builder.setPositiveButton(R.string.yes) { _, _ ->
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = ("package:" + context.applicationContext.packageName).toUri()
            context.startActivity(intent)
        }

        builder.setNegativeButton(R.string.no) { _, _ ->
        }
        val alert = builder.create()
        alert.show()
        alert.withCenteredButtons()

        val textViewMessage = alert.findViewById(android.R.id.message) as TextView?

        when (PreferenceUtil.fontSize) {
            "12" -> {
                textViewMessage!!.textSize = 12f
            }

            "13" -> {
                textViewMessage!!.textSize = 13f
            }

            "14" -> {
                textViewMessage!!.textSize = 14f
            }

            "15" -> {
                textViewMessage!!.textSize = 15f
            }

            "16" -> {
                textViewMessage!!.textSize = 16f
            }

            "17" -> {
                textViewMessage!!.textSize = 17f
            }

            "18" -> {
                textViewMessage!!.textSize = 18f

            }

            "19" -> {
                textViewMessage!!.textSize = 19f
            }

            "20" -> {
                textViewMessage!!.textSize = 20f
            }

            "21" -> {
                textViewMessage!!.textSize = 21f
            }

            "22" -> {
                textViewMessage!!.textSize = 22f
            }

            "23" -> {
                textViewMessage!!.textSize = 23f
            }

            "24" -> {
                textViewMessage!!.textSize = 24f
            }
        }

    }
}