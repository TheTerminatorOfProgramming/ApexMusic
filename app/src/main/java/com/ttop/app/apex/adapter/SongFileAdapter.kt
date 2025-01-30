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
package com.ttop.app.apex.adapter

import android.content.res.Configuration
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.MediaStoreSignature
import com.ttop.app.apex.R
import com.ttop.app.apex.adapter.base.AbsMultiSelectAdapter
import com.ttop.app.apex.adapter.base.MediaEntryViewHolder
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.extensions.getTintedDrawable
import com.ttop.app.apex.extensions.surfaceColor
import com.ttop.app.apex.glide.ApexGlideExtension
import com.ttop.app.apex.glide.audiocover.AudioFileCover
import com.ttop.app.apex.interfaces.ICallbacks
import com.ttop.app.apex.libraries.appthemehelper.ThemeStore.Companion.accentColor
import com.ttop.app.apex.libraries.appthemehelper.util.ATHUtil
import com.ttop.app.apex.libraries.fastscroller.PopupTextProvider
import com.ttop.app.apex.util.MusicUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.theme.ThemeMode
import java.io.File
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

class SongFileAdapter(
    override val activity: AppCompatActivity,
    private var dataSet: List<File>,
    private val itemLayoutRes: Int,
    private val iCallbacks: ICallbacks?
) : AbsMultiSelectAdapter<SongFileAdapter.ViewHolder, File>(
    activity, R.menu.menu_media_selection
), PopupTextProvider {

    init {
        this.setHasStableIds(true)
    }

    override fun getItemViewType(position: Int): Int {
        return if (dataSet[position].isDirectory) FOLDER else FILE
    }

    override fun getItemId(position: Int): Long {
        return dataSet[position].hashCode().toLong()
    }

    fun swapDataSet(songFiles: List<File>) {
        this.dataSet = songFiles
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(activity).inflate(itemLayoutRes, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, index: Int) {
        val file = dataSet[index]
        holder.itemView.isActivated = isChecked(file)
        holder.title?.text = getFileTitle(file)
        if (holder.text != null) {
            if (holder.itemViewType == FILE) {
                holder.text?.text = getFileText(file)
            } else {
                holder.text?.isVisible = false
            }
        }

        if (holder.image != null) {
            loadFileImage(file, holder)
        }

        //holder.listCard?.setCardBackgroundColor(activity.surfaceColor())
        holder.listCard?.strokeColor = accentColor(activity)

        when (PreferenceUtil.getGeneralThemeValue()) {
            ThemeMode.AUTO -> {
                when (activity.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                        holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                        holder.menu?.setColorFilter(ContextCompat.getColor(activity, R.color.md_white_1000))
                        holder.imagePlaying?.setColorFilter(ContextCompat.getColor(activity, R.color.md_white_1000))
                    }
                    Configuration.UI_MODE_NIGHT_NO,
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                        holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.darkColorSurface))
                        holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.darkColorSurface))
                        holder.menu?.setColorFilter(ContextCompat.getColor(activity, R.color.darkColorSurface))
                        holder.imagePlaying?.setColorFilter(ContextCompat.getColor(activity, R.color.darkColorSurface))
                    }
                    else -> {
                        holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                        holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                        holder.menu?.setColorFilter(ContextCompat.getColor(activity, R.color.md_white_1000))
                        holder.imagePlaying?.setColorFilter(ContextCompat.getColor(activity, R.color.md_white_1000))
                    }
                }
            }
            ThemeMode.AUTO_BLACK -> {
                when (activity.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                        holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                        holder.menu?.setColorFilter(ContextCompat.getColor(activity, R.color.md_white_1000))
                        holder.imagePlaying?.setColorFilter(ContextCompat.getColor(activity, R.color.md_white_1000))
                    }
                    Configuration.UI_MODE_NIGHT_NO,
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                        holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.blackColorSurface))
                        holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.blackColorSurface))
                        holder.menu?.setColorFilter(ContextCompat.getColor(activity, R.color.blackColorSurface))
                        holder.imagePlaying?.setColorFilter(ContextCompat.getColor(activity, R.color.blackColorSurface))
                    }
                    else -> {
                        holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                        holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                        holder.menu?.setColorFilter(ContextCompat.getColor(activity, R.color.md_white_1000))
                        holder.imagePlaying?.setColorFilter(ContextCompat.getColor(activity, R.color.md_white_1000))
                    }
                }
            }
            ThemeMode.BLACK,
            ThemeMode.DARK -> {
                holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                holder.menu?.setColorFilter(ContextCompat.getColor(activity, R.color.md_white_1000))
                holder.imagePlaying?.setColorFilter(ContextCompat.getColor(activity, R.color.md_white_1000))
            }
            ThemeMode.LIGHT -> {
                holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.darkColorSurface))
                holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.darkColorSurface))
                holder.menu?.setColorFilter(ContextCompat.getColor(activity, R.color.darkColorSurface))
                holder.imagePlaying?.setColorFilter(ContextCompat.getColor(activity, R.color.darkColorSurface))
            }
            ThemeMode.MD3 -> {
                holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.m3_widget_other_text))
                holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.m3_widget_other_text))
                holder.menu?.setColorFilter(ContextCompat.getColor(activity, R.color.m3_widget_other_text))
                holder.imagePlaying?.setColorFilter(ContextCompat.getColor(activity, R.color.m3_widget_other_text))
            }
        }
    }

    private fun getFileTitle(file: File): String {
        return file.name
    }

    private fun getFileText(file: File): String? {
        return if (file.isDirectory) null else readableFileSize(file.length())
    }

    private fun loadFileImage(file: File, holder: ViewHolder) {
        val iconColor = ATHUtil.resolveColor(activity, android.R.attr.colorControlNormal)
        if (file.isDirectory) {
            holder.image?.let {
                it.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN)
                it.setImageResource(R.drawable.ic_folder)
            }
        } else {
            val error = activity.getTintedDrawable(R.drawable.ic_file_music, iconColor)
            Glide.with(activity)
                .load(AudioFileCover(file.path))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .error(error)
                .placeholder(error)
                .transition(ApexGlideExtension.getDefaultTransition())
                .signature(MediaStoreSignature("", file.lastModified(), 0))
                .into(holder.image!!)
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun getIdentifier(position: Int): File {
        return dataSet[position]
    }

    override fun getName(model: File): String {
        return getFileTitle(model)
    }

    override fun onMultipleItemAction(menuItem: MenuItem, selection: List<File>) {
        if (iCallbacks == null) return
        iCallbacks.onMultipleItemAction(menuItem, selection as ArrayList<File>)
    }

    override fun getPopupText(view: View, position: Int): String {
        return if (position >= dataSet.lastIndex) "" else getSectionName(position)
    }

    private fun getSectionName(position: Int): String {
        return MusicUtil.getSectionName(dataSet[position].name)
    }

    inner class ViewHolder(itemView: View) : MediaEntryViewHolder(itemView) {

        init {
            if (menu != null && iCallbacks != null) {
                menu?.setOnClickListener { v ->
                    val position = layoutPosition
                    if (isPositionInRange(position)) {
                        iCallbacks.onFileMenuClicked(dataSet[position], v)
                    }
                }
            }
            if (imageTextContainer != null) {
                imageTextContainer?.cardElevation = 0f
            }
        }

        override fun onClick(v: View?) {
            val position = layoutPosition
            if (isPositionInRange(position)) {
                if (isInQuickSelectMode) {
                    toggleChecked(position)
                } else {
                    iCallbacks?.onFileSelected(dataSet[position])
                }
            }
        }

        override fun onLongClick(v: View?): Boolean {
            val position = layoutPosition
            return isPositionInRange(position) && toggleChecked(position)
        }

        private fun isPositionInRange(position: Int): Boolean {
            return position >= 0 && position < dataSet.size
        }
    }

    companion object {

        private const val FILE = 0
        private const val FOLDER = 1

        fun readableFileSize(size: Long): String {
            if (size <= 0) return "$size B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
            return DecimalFormat("#,##0.##").format(size / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
        }
    }
}
