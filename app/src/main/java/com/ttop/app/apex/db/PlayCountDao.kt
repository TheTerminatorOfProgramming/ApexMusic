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
package com.ttop.app.apex.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface PlayCountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSongInPlayCount(playCountEntity: PlayCountEntity)

    @Update
    fun updateSongInPlayCount(playCountEntity: PlayCountEntity)

    @Delete
    fun deleteSongInPlayCount(playCountEntity: PlayCountEntity)

    @Query("SELECT * FROM PlayCountEntity WHERE id =:songId")
    fun checkSongExistInPlayCount(songId: Long): List<PlayCountEntity>

    @Query("SELECT * FROM PlayCountEntity ORDER BY play_count DESC")
    fun playCountSongs(): List<PlayCountEntity>

    @Query("DELETE FROM SongEntity WHERE id =:songId")
    fun deleteSong(songId: Long)

}
