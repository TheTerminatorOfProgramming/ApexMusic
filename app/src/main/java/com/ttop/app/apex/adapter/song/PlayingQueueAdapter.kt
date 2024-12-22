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
import android.graphics.Color
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceGroupAdapter
import com.bumptech.glide.Glide
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange
import com.h6ah4i.android.widget.advrecyclerview.draggable.annotation.DraggableItemStateFlags
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionDefault
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionRemoveItem
import com.ttop.app.apex.R
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.extensions.generalThemeValue
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.extensions.surfaceColor
import com.ttop.app.apex.glide.ApexGlideExtension
import com.ttop.app.apex.glide.ApexGlideExtension.songCoverOptions
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.helper.MusicPlayerRemote.isPlaying
import com.ttop.app.apex.helper.MusicPlayerRemote.playNextSong
import com.ttop.app.apex.helper.MusicPlayerRemote.removeFromQueue
import com.ttop.app.apex.libraries.appthemehelper.ThemeStore
import com.ttop.app.apex.libraries.appthemehelper.ThemeStore.Companion.accentColor
import com.ttop.app.apex.libraries.appthemehelper.util.ATHColorUtil
import com.ttop.app.apex.libraries.appthemehelper.util.ATHUtil
import com.ttop.app.apex.libraries.fastscroller.PopupTextProvider
import com.ttop.app.apex.model.Song
import com.ttop.app.apex.ui.fragments.NowPlayingScreen
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.ColorUtil
import com.ttop.app.apex.util.MusicUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.ViewUtil
import com.ttop.app.apex.util.theme.ThemeMode

class PlayingQueueAdapter(
    activity: FragmentActivity,
    dataSet: MutableList<Song>,
    private var current: Int,
    itemLayoutRes: Int,
) : SongQueueAdapter(activity, dataSet, itemLayoutRes),
    DraggableItemAdapter<PlayingQueueAdapter.ViewHolder>,
    SwipeableItemAdapter<PlayingQueueAdapter.ViewHolder>,
    PopupTextProvider {

    private var songToRemove: Song? = null
    private var lastColor: Int = 0
    private var backColor: Int = 0
    private lateinit var imageviewDragView: ImageView
    override fun createViewHolder(view: View): SongQueueAdapter.ViewHolder {
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongQueueAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val song = dataSet[position]
        holder.time?.text = MusicUtil.getReadableDurationString(song.duration)
        if (holder.itemViewType == HISTORY) {
            setAlpha(holder)
        }

        if (holder.itemViewType == CURRENT) {
            holder.imagePlaying?.visibility = View.VISIBLE
            holder.imageContainer?.visibility = View.GONE
        } else {
            holder.imagePlaying?.visibility = View.GONE
            holder.imageContainer?.visibility = View.VISIBLE
        }

        val imageView = holder.dragView as ImageView

        imageviewDragView = imageView

        imageView.visibility = View.VISIBLE

        if (itemLayoutRes == R.layout.item_nav_queue) {
            if (holder.itemViewType == HISTORY) {
                holder.listCard?.strokeWidth = 0
            } else if (holder.itemViewType == CURRENT) {
                holder.listCard?.strokeColor = accentColor(activity)
                holder.title?.setTextColor(activity.accentColor())
                holder.text?.setTextColor(activity.accentColor())
                holder.text2?.setTextColor(activity.accentColor())
                holder.menu?.setColorFilter(activity.accentColor())
                holder.imagePlaying?.setColorFilter(activity.accentColor())
                imageView.setColorFilter(activity.accentColor())
            } else {
                if (PreferenceUtil.materialYou) {
                    if (holder.itemViewType == CURRENT) {
                        holder.listCard?.strokeColor = accentColor(activity)
                        holder.title?.setTextColor(activity.accentColor())
                        holder.text?.setTextColor(activity.accentColor())
                        holder.text2?.setTextColor(activity.accentColor())
                        holder.menu?.setColorFilter(activity.accentColor())
                        holder.imagePlaying?.setColorFilter(activity.accentColor())
                        imageView.setColorFilter(activity.accentColor())
                    } else {
                        holder.listCard?.strokeColor = ContextCompat.getColor(
                            activity,
                            R.color.m3_widget_other_text
                        )
                        holder.title?.setTextColor(
                            ContextCompat.getColor(
                                activity,
                                R.color.m3_widget_other_text
                            )
                        )
                        holder.text?.setTextColor(
                            ContextCompat.getColor(
                                activity,
                                R.color.m3_widget_other_text
                            )
                        )
                        holder.text2?.setTextColor(
                            ContextCompat.getColor(
                                activity,
                                R.color.m3_widget_other_text
                            )
                        )
                        holder.menu?.setColorFilter(
                            ContextCompat.getColor(
                                activity,
                                R.color.m3_widget_other_text
                            )
                        )
                        holder.imagePlaying?.setColorFilter(
                            ContextCompat.getColor(
                                activity,
                                R.color.m3_widget_other_text
                            )
                        )
                        imageView.setColorFilter(
                            ContextCompat.getColor(
                                activity,
                                R.color.m3_widget_other_text
                            )
                        )
                    }
                } else {
                    if (holder.itemViewType == CURRENT) {
                        holder.listCard?.strokeColor = accentColor(activity)
                        holder.title?.setTextColor(activity.accentColor())
                        holder.text?.setTextColor(activity.accentColor())
                        holder.text2?.setTextColor(activity.accentColor())
                        holder.menu?.setColorFilter(activity.accentColor())
                        holder.imagePlaying?.setColorFilter(activity.accentColor())
                        imageView.setColorFilter(activity.accentColor())
                    } else {
                        val colorBg =
                            ATHUtil.resolveColor(activity, android.R.attr.colorBackground)
                        if (ATHColorUtil.isColorLight(colorBg)) {
                            holder.listCard?.strokeColor = ContextCompat.getColor(
                                activity,
                                R.color.md_black_1000
                            )
                            holder.title?.setTextColor(
                                ContextCompat.getColor(
                                    activity,
                                    R.color.md_black_1000
                                )
                            )
                            holder.text?.setTextColor(
                                ContextCompat.getColor(
                                    activity,
                                    R.color.md_black_1000
                                )
                            )
                            holder.text2?.setTextColor(
                                ContextCompat.getColor(
                                    activity,
                                    R.color.md_black_1000
                                )
                            )
                            holder.menu?.setColorFilter(
                                ContextCompat.getColor(
                                    activity,
                                    R.color.md_black_1000
                                )
                            )
                            holder.imagePlaying?.setColorFilter(
                                ContextCompat.getColor(
                                    activity,
                                    R.color.md_black_1000
                                )
                            )
                            imageView.setColorFilter(
                                ContextCompat.getColor(
                                    activity,
                                    R.color.md_black_1000
                                )
                            )
                        } else {
                            holder.listCard?.strokeColor = ContextCompat.getColor(
                                activity,
                                R.color.md_white_1000
                            )
                            holder.title?.setTextColor(
                                ContextCompat.getColor(
                                    activity,
                                    R.color.md_white_1000
                                )
                            )
                            holder.text?.setTextColor(
                                ContextCompat.getColor(
                                    activity,
                                    R.color.md_white_1000
                                )
                            )
                            holder.text2?.setTextColor(
                                ContextCompat.getColor(
                                    activity,
                                    R.color.md_white_1000
                                )
                            )
                            holder.menu?.setColorFilter(
                                ContextCompat.getColor(
                                    activity,
                                    R.color.md_white_1000
                                )
                            )
                            holder.imagePlaying?.setColorFilter(
                                ContextCompat.getColor(
                                    activity,
                                    R.color.md_white_1000
                                )
                            )
                            imageView.setColorFilter(
                                ContextCompat.getColor(
                                    activity,
                                    R.color.md_white_1000
                                )
                            )
                        }
                    }
                }
            }
        }

        if (PreferenceUtil.nowPlayingScreen == NowPlayingScreen.Blur) {
            holder.listCard?.backgroundTintList = ColorStateList.valueOf(activity.surfaceColor())

            when (activity.generalThemeValue) {
                ThemeMode.LIGHT -> {
                    when (holder.itemViewType) {
                        HISTORY -> {
                            holder.listCard?.strokeWidth = 0

                        }
                        CURRENT -> {
                            holder.listCard?.strokeWidth =
                                ViewUtil.convertDpToPixel(2f, activity.resources).toInt()
                            holder.listCard?.strokeColor = ContextCompat.getColor(
                                activity,
                                R.color.darkColorSurface
                            )
                        }
                        else -> {
                            holder.listCard?.strokeWidth = ViewUtil.convertDpToPixel(2f, activity.resources).toInt()
                            holder.listCard?.strokeColor = ContextCompat.getColor(
                                activity,
                                R.color.darkColorSurface
                            )
                        }
                    }
                    holder.title?.setTextColor(
                        ContextCompat.getColor(
                            activity,
                            R.color.md_black_1000
                        )
                    )
                    holder.text?.setTextColor(
                        ContextCompat.getColor(
                            activity,
                            R.color.md_black_1000
                        )
                    )
                    holder.text2?.setTextColor(
                        ContextCompat.getColor(
                            activity,
                            R.color.md_black_1000
                        )
                    )
                    holder.menu?.setColorFilter(
                        ContextCompat.getColor(
                            activity,
                            R.color.md_black_1000
                        )
                    )
                    holder.imagePlaying?.setColorFilter(
                        ContextCompat.getColor(
                            activity,
                            R.color.md_black_1000
                        )
                    )
                    imageView.setColorFilter(
                        ContextCompat.getColor(
                            activity,
                            R.color.md_black_1000
                        )
                    )
                }

                ThemeMode.DARK, ThemeMode.BLACK -> {
                    when (holder.itemViewType) {
                        HISTORY -> {
                            holder.listCard?.strokeWidth = 0
                        }
                        CURRENT -> {
                            holder.listCard?.strokeWidth =
                                ViewUtil.convertDpToPixel(2f, activity.resources).toInt()
                            holder.listCard?.strokeColor = ContextCompat.getColor(
                                activity,
                                R.color.md_white_1000
                            )
                        }
                        else -> {
                            holder.listCard?.strokeWidth = ViewUtil.convertDpToPixel(2f, activity.resources).toInt()
                            holder.listCard?.strokeColor = ContextCompat.getColor(
                                activity,
                                R.color.md_white_1000
                            )
                        }
                    }

                    holder.title?.setTextColor(
                        ContextCompat.getColor(
                            activity,
                            R.color.md_white_1000
                        )
                    )
                    holder.text?.setTextColor(
                        ContextCompat.getColor(
                            activity,
                            R.color.md_white_1000
                        )
                    )
                    holder.text2?.setTextColor(
                        ContextCompat.getColor(
                            activity,
                            R.color.md_white_1000
                        )
                    )
                    holder.menu?.setColorFilter(
                        ContextCompat.getColor(
                            activity,
                            R.color.md_white_1000
                        )
                    )
                    holder.imagePlaying?.setColorFilter(
                        ContextCompat.getColor(
                            activity,
                            R.color.md_white_1000
                        )
                    )
                    imageView.setColorFilter(
                        ContextCompat.getColor(
                            activity,
                            R.color.md_white_1000
                        )
                    )
                }

                ThemeMode.AUTO, ThemeMode.AUTO_BLACK -> {
                    when (activity.resources?.configuration?.uiMode?.and(
                        Configuration.UI_MODE_NIGHT_MASK
                    )) {
                        Configuration.UI_MODE_NIGHT_YES -> {
                            when (holder.itemViewType) {
                                HISTORY -> {
                                    holder.listCard?.strokeWidth = 0
                                }
                                CURRENT -> {
                                    holder.listCard?.strokeWidth =
                                        ViewUtil.convertDpToPixel(2f, activity.resources).toInt()
                                    holder.listCard?.strokeColor = ContextCompat.getColor(
                                        activity,
                                        R.color.md_white_1000
                                    )
                                }
                                else -> {
                                    holder.listCard?.strokeWidth = ViewUtil.convertDpToPixel(2f, activity.resources).toInt()
                                    holder.listCard?.strokeColor = ContextCompat.getColor(
                                        activity,
                                        R.color.md_white_1000
                                    )
                                }
                            }
                            holder.title?.setTextColor(
                                ContextCompat.getColor(
                                    activity,
                                    R.color.md_white_1000
                                )
                            )
                            holder.text?.setTextColor(
                                ContextCompat.getColor(
                                    activity,
                                    R.color.md_white_1000
                                )
                            )
                            holder.text2?.setTextColor(
                                ContextCompat.getColor(
                                    activity,
                                    R.color.md_white_1000
                                )
                            )
                            holder.menu?.setColorFilter(
                                ContextCompat.getColor(
                                    activity,
                                    R.color.md_white_1000
                                )
                            )
                            holder.imagePlaying?.setColorFilter(
                                ContextCompat.getColor(
                                    activity,
                                    R.color.md_white_1000
                                )
                            )
                            imageView.setColorFilter(
                                ContextCompat.getColor(
                                    activity,
                                    R.color.md_white_1000
                                )
                            )
                        }

                        Configuration.UI_MODE_NIGHT_NO,
                        Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                            when (holder.itemViewType) {
                                HISTORY -> {
                                    holder.listCard?.strokeWidth = 0
                                }
                                CURRENT -> {
                                    holder.listCard?.strokeWidth =
                                        ViewUtil.convertDpToPixel(2f, activity.resources).toInt()
                                    holder.listCard?.strokeColor = ContextCompat.getColor(
                                        activity,
                                        R.color.md_black_1000
                                    )
                                }
                                else -> {
                                    holder.listCard?.strokeWidth = ViewUtil.convertDpToPixel(2f, activity.resources).toInt()
                                    holder.listCard?.strokeColor = ContextCompat.getColor(
                                        activity,
                                        R.color.md_black_1000
                                    )
                                }
                            }
                            holder.title?.setTextColor(
                                ContextCompat.getColor(
                                    activity,
                                    R.color.md_black_1000
                                )
                            )
                            holder.text?.setTextColor(
                                ContextCompat.getColor(
                                    activity,
                                    R.color.md_black_1000
                                )
                            )
                            holder.text2?.setTextColor(
                                ContextCompat.getColor(
                                    activity,
                                    R.color.md_black_1000
                                )
                            )
                            holder.menu?.setColorFilter(
                                ContextCompat.getColor(
                                    activity,
                                    R.color.md_black_1000
                                )
                            )
                            holder.imagePlaying?.setColorFilter(
                                ContextCompat.getColor(
                                    activity,
                                    R.color.md_black_1000
                                )
                            )
                            imageView.setColorFilter(
                                ContextCompat.getColor(
                                    activity,
                                    R.color.md_black_1000
                                )
                            )
                        }
                    }
                }
            }
        }else if (itemLayoutRes == R.layout.item_queue || itemLayoutRes == R.layout.item_queue_duo || itemLayoutRes == R.layout.item_queue_no_image) {
            if (PreferenceUtil.isAdaptiveColor) {
                holder.listCard?.backgroundTintList = ColorStateList.valueOf(backColor)
                if (holder.itemViewType == HISTORY) {
                    holder.listCard?.strokeWidth = 0
                }else if (holder.itemViewType == CURRENT) {
                    holder.listCard?.strokeWidth = ViewUtil.convertDpToPixel(2f, activity.resources).toInt()
                    holder.listCard?.strokeColor = lastColor
                    holder.title?.setTextColor(lastColor)
                    holder.text?.setTextColor(lastColor)
                    holder.text2?.setTextColor(lastColor)
                    holder.menu?.setColorFilter(lastColor)
                    holder.imagePlaying?.setColorFilter(lastColor)
                    imageView.setColorFilter(lastColor)
                } else {
                    holder.listCard?.strokeWidth = ViewUtil.convertDpToPixel(2f, activity.resources).toInt()
                    holder.listCard?.strokeColor = ColorUtil.getComplimentColor(
                        lastColor
                    )
                    holder.title?.setTextColor(
                        ColorUtil.getComplimentColor(
                            lastColor
                        )
                    )
                    holder.text?.setTextColor(
                        ColorUtil.getComplimentColor(
                            lastColor
                        )
                    )
                    holder.text2?.setTextColor(
                        ColorUtil.getComplimentColor(
                            lastColor
                        )
                    )
                    holder.menu?.setColorFilter(
                        ColorUtil.getComplimentColor(
                            lastColor
                        )
                    )
                    holder.imagePlaying?.setColorFilter(
                        ColorUtil.getComplimentColor(
                            lastColor
                        )
                    )
                    imageView.setColorFilter(
                        ColorUtil.getComplimentColor(
                            lastColor
                        )
                    )
                }
            } else {
                if (PreferenceUtil.materialYou) {
                    holder.listCard?.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(
                        activity,
                        R.color.m3_widget_background
                    ))
                    if (holder.itemViewType == HISTORY) {
                        holder.listCard?.strokeWidth = 0
                    } else if (holder.itemViewType == CURRENT) {
                        holder.listCard?.strokeWidth = ViewUtil.convertDpToPixel(2f, activity.resources).toInt()
                        holder.listCard?.strokeColor = activity.accentColor()
                        holder.title?.setTextColor(activity.accentColor())
                        holder.text?.setTextColor(activity.accentColor())
                        holder.text2?.setTextColor(activity.accentColor())
                        holder.menu?.setColorFilter(activity.accentColor())
                        holder.imagePlaying?.setColorFilter(activity.accentColor())
                        imageView.setColorFilter(activity.accentColor())
                    } else {
                        holder.listCard?.strokeWidth = ViewUtil.convertDpToPixel(2f, activity.resources).toInt()
                        holder.listCard?.strokeColor = ContextCompat.getColor(
                            activity,
                            R.color.m3_widget_other_text
                        )
                        holder.title?.setTextColor(
                            ContextCompat.getColor(
                                activity,
                                R.color.m3_widget_other_text
                            )
                        )
                        holder.text?.setTextColor(
                            ContextCompat.getColor(
                                activity,
                                R.color.m3_widget_other_text
                            )
                        )
                        holder.text2?.setTextColor(
                            ContextCompat.getColor(
                                activity,
                                R.color.m3_widget_other_text
                            )
                        )
                        holder.menu?.setColorFilter(
                            ContextCompat.getColor(
                                activity,
                                R.color.m3_widget_other_text
                            )
                        )
                        holder.imagePlaying?.setColorFilter(
                            ContextCompat.getColor(
                                activity,
                                R.color.m3_widget_other_text
                            )
                        )
                        imageView.setColorFilter(
                            ContextCompat.getColor(
                                activity,
                                R.color.m3_widget_other_text
                            )
                        )
                    }
                } else {
                    holder.listCard?.backgroundTintList = ColorStateList.valueOf(activity.surfaceColor())
                    if (holder.itemViewType == HISTORY) {
                        holder.listCard?.strokeWidth = 0
                    } else if (holder.itemViewType == CURRENT) {
                        holder.listCard?.strokeWidth = ViewUtil.convertDpToPixel(2f, activity.resources).toInt()
                        holder.listCard?.strokeColor = activity.accentColor()
                        holder.title?.setTextColor(activity.accentColor())
                        holder.text?.setTextColor(activity.accentColor())
                        holder.text2?.setTextColor(activity.accentColor())
                        holder.menu?.setColorFilter(activity.accentColor())
                        holder.imagePlaying?.setColorFilter(activity.accentColor())
                        imageView.setColorFilter(activity.accentColor())
                    } else {
                        holder.listCard?.strokeWidth = ViewUtil.convertDpToPixel(2f, activity.resources).toInt()
                        holder.listCard?.strokeColor = ColorUtil.getComplimentColor(activity.accentColor())
                        val colorBg =
                            ATHUtil.resolveColor(activity, android.R.attr.colorBackground)
                        if (ATHColorUtil.isColorLight(colorBg)) {
                            holder.listCard?.strokeColor = ContextCompat.getColor(
                                activity,
                                R.color.md_black_1000
                            )
                            holder.title?.setTextColor(
                                ContextCompat.getColor(
                                    activity,
                                    R.color.md_black_1000
                                )
                            )
                            holder.text?.setTextColor(
                                ContextCompat.getColor(
                                    activity,
                                    R.color.md_black_1000
                                )
                            )
                            holder.text2?.setTextColor(
                                ContextCompat.getColor(
                                    activity,
                                    R.color.md_black_1000
                                )
                            )
                            holder.menu?.setColorFilter(
                                ContextCompat.getColor(
                                    activity,
                                    R.color.md_black_1000
                                )
                            )
                            holder.imagePlaying?.setColorFilter(
                                ContextCompat.getColor(
                                    activity,
                                    R.color.md_black_1000
                                )
                            )
                            imageView.setColorFilter(
                                ContextCompat.getColor(
                                    activity,
                                    R.color.md_black_1000
                                )
                            )
                        } else {
                            holder.listCard?.strokeColor = ContextCompat.getColor(
                                activity,
                                R.color.md_white_1000
                            )
                            holder.title?.setTextColor(
                                ContextCompat.getColor(
                                    activity,
                                    R.color.md_white_1000
                                )
                            )
                            holder.text?.setTextColor(
                                ContextCompat.getColor(
                                    activity,
                                    R.color.md_white_1000
                                )
                            )
                            holder.text2?.setTextColor(
                                ContextCompat.getColor(
                                    activity,
                                    R.color.md_white_1000
                                )
                            )
                            holder.menu?.setColorFilter(
                                ContextCompat.getColor(
                                    activity,
                                    R.color.md_white_1000
                                )
                            )
                            holder.imagePlaying?.setColorFilter(
                                ContextCompat.getColor(
                                    activity,
                                    R.color.md_white_1000
                                )
                            )
                            imageView.setColorFilter(
                                ContextCompat.getColor(
                                    activity,
                                    R.color.md_white_1000
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position < current) {
            return HISTORY
        } else if (position > current) {
            return UP_NEXT
        }
        return CURRENT
    }

    override fun loadAlbumCover(song: Song, holder: SongQueueAdapter.ViewHolder) {
        if (holder.image == null) {
            return
        }
        Glide.with(activity)
            .load(ApexGlideExtension.getSongModel(song))
            .songCoverOptions(song)
            .into(holder.image!!)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun swapDataSet(dataSet: List<Song>, position: Int) {
        this.dataSet = dataSet.toMutableList()
        current = position
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setCurrent(current: Int) {
        this.current = current
        notifyDataSetChanged()
    }

    private fun setAlpha(holder: SongQueueAdapter.ViewHolder) {
        holder.image?.alpha = 0.5f
        holder.title?.alpha = 0.5f
        holder.text?.alpha = 0.5f
        holder.text2?.alpha = 0.5f
        holder.paletteColorContainer?.alpha = 0.5f
        holder.dragView?.alpha = 0.5f
        holder.menu?.alpha = 0.5f
    }

    override fun getPopupText(view: View, position: Int): String {
        return MusicUtil.getSectionName(dataSet[position].title)
    }

    override fun onCheckCanStartDrag(holder: ViewHolder, position: Int, x: Int, y: Int): Boolean {
        return ViewUtil.hitTest(holder.imageText!!, x, y) || ViewUtil.hitTest(
            holder.dragView!!,
            x,
            y
        )
    }

    override fun onGetItemDraggableRange(holder: ViewHolder, position: Int): ItemDraggableRange? {
        return null
    }

    override fun onMoveItem(fromPosition: Int, toPosition: Int) {
        MusicPlayerRemote.moveSong(fromPosition, toPosition)
    }

    override fun onCheckCanDrop(draggingPosition: Int, dropPosition: Int): Boolean {
        return true
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onItemDragStarted(position: Int) {
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onItemDragFinished(fromPosition: Int, toPosition: Int, result: Boolean) {
        notifyDataSetChanged()
    }

    fun setSongToRemove(song: Song) {
        songToRemove = song
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setTextColor(color: Int) {
        lastColor = color
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setBackgroundColor(color: Int) {
        backColor = color
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : SongQueueAdapter.ViewHolder(itemView) {
        @DraggableItemStateFlags
        private var mDragStateFlags: Int = 0

        override var songMenuRes: Int
            get() = R.menu.menu_item_playing_queue_song
            set(value) {
                super.songMenuRes = value
            }

        init {
            dragView?.isVisible = false
        }

        override fun onClick(v: View?) {
            if (isInQuickSelectMode) {
                toggleChecked(layoutPosition)
            } else {
                MusicPlayerRemote.playSongAt(layoutPosition)
            }
        }

        override fun onSongMenuItemClick(item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.action_remove_from_playing_queue -> {
                    removeFromQueue(layoutPosition)
                    return true
                }
            }
            return super.onSongMenuItemClick(item)
        }

        @DraggableItemStateFlags
        override fun getDragStateFlags(): Int {
            return mDragStateFlags
        }

        override fun setDragStateFlags(@DraggableItemStateFlags flags: Int) {
            mDragStateFlags = flags
        }

        override fun getSwipeableContainerView(): View {
            return dummyContainer!!
        }
    }

    companion object {

        private const val HISTORY = 0
        private const val CURRENT = 1
        private const val UP_NEXT = 2
    }

    override fun onSwipeItem(holder: ViewHolder, position: Int, result: Int): SwipeResultAction {
        return if (result == SwipeableItemConstants.RESULT_CANCELED) {
            SwipeResultActionDefault()
        } else {
            SwipedResultActionRemoveItem(this, position)
        }
    }

    override fun onGetSwipeReactionType(holder: ViewHolder, position: Int, x: Int, y: Int): Int {
        return if (onCheckCanStartDrag(holder, position, x, y)) {
            SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_BOTH_H
        } else {
            SwipeableItemConstants.REACTION_CAN_SWIPE_BOTH_H
        }
    }

    override fun onSwipeItemStarted(holder: ViewHolder, p1: Int) {
    }

    override fun onSetSwipeBackground(holder: ViewHolder, position: Int, result: Int) {
    }

    internal class SwipedResultActionRemoveItem(
        private val adapter: PlayingQueueAdapter,
        private val position: Int
    ) : SwipeResultActionRemoveItem() {

        private var songToRemove: Song? = null
        override fun onPerformAction() {
            // currentlyShownSnackbar = null
        }

        override fun onSlideAnimationEnd() {
            // initializeSnackBar(adapter, position, activity, isPlaying)
            songToRemove = adapter.dataSet[position]
            // If song removed was the playing song, then play the next song
            if (isPlaying(songToRemove!!)) {
                playNextSong()
            }
            // Swipe animation is much smoother when we do the heavy lifting after it's completed
            adapter.setSongToRemove(songToRemove!!)
            removeFromQueue(songToRemove!!)
        }
    }
}