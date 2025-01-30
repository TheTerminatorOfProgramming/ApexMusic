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
package com.ttop.app.apex.adapter.song

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.ttop.app.apex.R
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.model.Song
import com.ttop.app.apex.util.MusicUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.theme.ThemeMode

class SimpleSongAdapter(
    context: FragmentActivity,
    songs: ArrayList<Song>,
    layoutRes: Int
) : SongAdapter(context, songs, layoutRes) {

    @SuppressLint("NotifyDataSetChanged")
    override fun swapDataSet(dataSet: List<Song>) {
        this.dataSet = dataSet.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(activity).inflate(itemLayoutRes, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val fixedTrackNumber = MusicUtil.getFixedTrackNumber(dataSet[position].trackNumber)

        holder.imageText?.text = if (fixedTrackNumber > 0) fixedTrackNumber.toString() else "-"
        holder.time?.text = MusicUtil.getReadableDurationString(dataSet[position].duration)

        when (PreferenceUtil.getGeneralThemeValue()) {
            ThemeMode.AUTO -> {
                when (activity.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        holder.imageText?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                        holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                        holder.time?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                        holder.menu?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.md_white_1000))
                    }

                    Configuration.UI_MODE_NIGHT_NO,
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                        holder.imageText?.setTextColor(ContextCompat.getColor(activity, R.color.darkColorSurface))
                        holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.darkColorSurface))
                        holder.time?.setTextColor(ContextCompat.getColor(activity, R.color.darkColorSurface))
                        holder.menu?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.darkColorSurface))
                    }

                    else -> {
                        holder.imageText?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                        holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                        holder.time?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                        holder.menu?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.md_white_1000))
                    }
                }
            }

            ThemeMode.AUTO_BLACK -> {
                when (activity.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        holder.imageText?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                        holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                        holder.time?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                        holder.menu?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.md_white_1000))
                    }

                    Configuration.UI_MODE_NIGHT_NO,
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                        holder.imageText?.setTextColor(ContextCompat.getColor(activity, R.color.blackColorSurface))
                        holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.blackColorSurface))
                        holder.time?.setTextColor(ContextCompat.getColor(activity, R.color.blackColorSurface))
                        holder.menu?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.blackColorSurface))
                    }

                    else -> {
                        holder.imageText?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                        holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                        holder.time?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                        holder.menu?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.md_white_1000))
                    }
                }
            }

            ThemeMode.BLACK,
            ThemeMode.DARK -> {
                holder.imageText?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                holder.time?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                holder.menu?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.md_white_1000))
            }

            ThemeMode.LIGHT -> {
                holder.imageText?.setTextColor(ContextCompat.getColor(activity, R.color.darkColorSurface))
                holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.darkColorSurface))
                holder.time?.setTextColor(ContextCompat.getColor(activity, R.color.darkColorSurface))
                holder.menu?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.darkColorSurface))
            }

            ThemeMode.MD3 -> {
                holder.imageText?.setTextColor(activity.accentColor())
                holder.title?.setTextColor(activity.accentColor())
                holder.time?.setTextColor(activity.accentColor())
                holder.menu?.imageTintList = ColorStateList.valueOf(activity.accentColor())
            }
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }
}
