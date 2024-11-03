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

import com.ttop.app.apex.model.Song
import com.ttop.app.apex.model.lyrics.AbsSynchronizedLyrics
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File

object LyricUtil {
    private val fileName = PreferenceUtil.lyricsPath
    private val lrcRootPath = "$fileName/Apex/Lyrics/"

    //So in Apex, Lrc file can be same folder as Music File or in Apex Folder
    private fun isLrcFileExist(title: String, artist: String, album: String): Boolean {
        val file = File(getLrcPath(title, artist, album))
        return file.exists()
    }

    private fun isLrcOriginalFileExist(path: String): Boolean {
        val file = File(getLrcOriginalPath(path))
        return file.exists()
    }

    private fun getLocalLyricFile(title: String, artist: String, album: String): File? {
        val file = File(getLrcPath(title, artist, album))
        return if (file.exists()) {
            file
        } else {
            null
        }
    }

    private fun getLocalLyricOriginalFile(path: String): File? {
        val file = File(getLrcOriginalPath(path))
        return if (file.exists()) {
            file
        } else {
            null
        }
    }

    private fun getLrcPath(title: String, artist: String, album: String): String {
        return "$lrcRootPath$title-$artist-$album.lrc"
    }

    private fun getLrcOriginalPath(filePath: String): String {
        return filePath.replace(filePath.substring(filePath.lastIndexOf(".") + 1), "lrc")
    }

    fun getSyncedLyricsFile(song: Song): File? {
        return when {
            isLrcOriginalFileExist(song.data) -> {
                getLocalLyricOriginalFile(song.data)
            }

            isLrcFileExist(song.title, song.artistName, song.albumName) -> {
                getLocalLyricFile(song.title, song.artistName, song.albumName)
            }

            else -> {
                null
            }
        }
    }

    fun getEmbeddedSyncedLyrics(data: String): String? {
        val embeddedLyrics = try {
            AudioFileIO.read(File(data)).tagOrCreateDefault.getFirst(FieldKey.LYRICS)
        } catch (e: Exception) {
            return null
        }
        return if (AbsSynchronizedLyrics.isSynchronized(embeddedLyrics)) {
            embeddedLyrics
        } else {
            null
        }
    }
}