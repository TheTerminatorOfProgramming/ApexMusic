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
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
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
import com.ttop.app.apex.glide.ApexGlideExtension
import com.ttop.app.apex.glide.ApexGlideExtension.songCoverOptions
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.helper.MusicPlayerRemote.isPlaying
import com.ttop.app.apex.helper.MusicPlayerRemote.playNextSong
import com.ttop.app.apex.helper.MusicPlayerRemote.removeFromQueue
import com.ttop.app.apex.model.Song
import com.ttop.app.apex.ui.fragments.NowPlayingScreen
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.MusicUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.ViewUtil
import com.ttop.app.appthemehelper.util.ATHUtil
import com.ttop.app.appthemehelper.util.ColorUtil
import me.zhanghai.android.fastscroll.PopupTextProvider


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
    private var activate = false
    private lateinit var imageviewDragView: ImageView
    override fun createViewHolder(view: View): SongQueueAdapter.ViewHolder {
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongQueueAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val song = dataSet[position]
        holder.time?.text = MusicUtil.getReadableDurationString(song.duration)
        if (holder.itemViewType == HISTORY || holder.itemViewType == CURRENT) {
            setAlpha(holder)
        }
        val imageView = holder.dragView as ImageView

        imageviewDragView = imageView

        if (activate) {
            imageView.visibility = View.VISIBLE
        } else {
            imageView.visibility = View.GONE
        }

        when (PreferenceUtil.nowPlayingScreen){
            NowPlayingScreen.Adaptive -> {
                if (PreferenceUtil.isAdaptiveColor) {
                    holder.title?.setTextColor(lastColor)
                    holder.text?.setTextColor(lastColor)
                    holder.text2?.setTextColor(lastColor)
                    holder.menu?.setColorFilter(lastColor)
                    imageView.setColorFilter(lastColor)
                }else {
                    holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                    holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                    holder.text2?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                    holder.menu?.setColorFilter(ContextCompat.getColor(activity, R.color.md_white_1000))
                    imageView.setColorFilter(ContextCompat.getColor(activity, R.color.md_white_1000))
                }
            }
            NowPlayingScreen.Blur -> {
                if (PreferenceUtil.isAdaptiveColor) {
                    holder.title?.setTextColor(lastColor) //PreferenceUtil.
                    holder.text?.setTextColor(lastColor)
                    holder.text2?.setTextColor(lastColor)
                    holder.menu?.setColorFilter(lastColor)
                    imageView.setColorFilter(lastColor)
                }else {
                    holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                    holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                    holder.text2?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                    holder.menu?.setColorFilter(ContextCompat.getColor(activity, R.color.md_white_1000))
                    imageView.setColorFilter(ContextCompat.getColor(activity, R.color.md_white_1000))
                }
            }
            NowPlayingScreen.Card -> {
                val colorBg = ATHUtil.resolveColor(activity, android.R.attr.colorBackground)
                if (ColorUtil.isColorLight(colorBg)) {
                    holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_black_1000))
                    holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.md_black_1000))
                    holder.text2?.setTextColor(ContextCompat.getColor(activity, R.color.md_black_1000))
                    holder.menu?.setColorFilter(ContextCompat.getColor(activity, R.color.md_black_1000))
                    imageView.setColorFilter(ContextCompat.getColor(activity, R.color.md_black_1000))
                }else {
                    holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                    holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                    holder.text2?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                    holder.menu?.setColorFilter(ContextCompat.getColor(activity, R.color.md_white_1000))
                    imageView.setColorFilter(ContextCompat.getColor(activity, R.color.md_white_1000))
                }
            }
            NowPlayingScreen.Gradient -> {
                val colorBg = ATHUtil.resolveColor(activity, android.R.attr.colorBackground)
                if (ColorUtil.isColorLight(colorBg)) {
                    holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_black_1000))
                    holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.md_black_1000))
                    holder.text2?.setTextColor(ContextCompat.getColor(activity, R.color.md_black_1000))
                    holder.menu?.setColorFilter(ContextCompat.getColor(activity, R.color.md_black_1000))
                    imageView.setColorFilter(ContextCompat.getColor(activity, R.color.md_black_1000))
                }else {
                    holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                    holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                    holder.text2?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                    holder.menu?.setColorFilter(ContextCompat.getColor(activity, R.color.md_white_1000))
                    imageView.setColorFilter(ContextCompat.getColor(activity, R.color.md_white_1000))
                }
            }
            NowPlayingScreen.Classic -> {
                if (PreferenceUtil.isAdaptiveColor) {
                    holder.title?.setTextColor(lastColor) //PreferenceUtil.
                    holder.text?.setTextColor(lastColor)
                    holder.text2?.setTextColor(lastColor)
                    holder.menu?.setColorFilter(lastColor)
                    imageView.setColorFilter(lastColor)
                }else {
                    if (ApexUtil.isTablet) {
                        val colorBg = ATHUtil.resolveColor(activity, android.R.attr.colorBackground)
                        if (ColorUtil.isColorLight(colorBg)) {
                            holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_black_1000))
                            holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.md_black_1000))
                            holder.text2?.setTextColor(ContextCompat.getColor(activity, R.color.md_black_1000))
                            holder.menu?.setColorFilter(ContextCompat.getColor(activity, R.color.md_black_1000))
                            imageView.setColorFilter(ContextCompat.getColor(activity, R.color.md_black_1000))
                        }else {
                            holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                            holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                            holder.text2?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                            holder.menu?.setColorFilter(ContextCompat.getColor(activity, R.color.md_white_1000))
                            imageView.setColorFilter(ContextCompat.getColor(activity, R.color.md_white_1000))
                        }
                    }else {
                        holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                        holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                        holder.text2?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                        holder.menu?.setColorFilter(ContextCompat.getColor(activity, R.color.md_white_1000))
                        imageView.setColorFilter(ContextCompat.getColor(activity, R.color.md_white_1000))
                    }
                }
            }
            NowPlayingScreen.Peek -> {
                if (ApexUtil.isTablet) {
                        if (PreferenceUtil.isAdaptiveColor) {
                            holder.title?.setTextColor(lastColor) //PreferenceUtil.
                            holder.text?.setTextColor(lastColor)
                            holder.text2?.setTextColor(lastColor)
                            holder.menu?.setColorFilter(lastColor)
                            imageView.setColorFilter(lastColor)
                        }else {
                            val colorBg = ATHUtil.resolveColor(activity, android.R.attr.colorBackground)
                            if (ColorUtil.isColorLight(colorBg)) {
                                holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_black_1000))
                                holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.md_black_1000))
                                holder.text2?.setTextColor(ContextCompat.getColor(activity, R.color.md_black_1000))
                                holder.menu?.setColorFilter(ContextCompat.getColor(activity, R.color.md_black_1000))
                                imageView.setColorFilter(ContextCompat.getColor(activity, R.color.md_black_1000))
                            }else {
                                holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                holder.text2?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                                holder.menu?.setColorFilter(ContextCompat.getColor(activity, R.color.md_white_1000))
                                imageView.setColorFilter(ContextCompat.getColor(activity, R.color.md_white_1000))
                            }
                        }
                }else {
                    if (PreferenceUtil.isAdaptiveColor) {
                        holder.title?.setTextColor(lastColor) //PreferenceUtil.
                        holder.text?.setTextColor(lastColor)
                        holder.text2?.setTextColor(lastColor)
                        holder.menu?.setColorFilter(lastColor)
                        imageView.setColorFilter(lastColor)
                    }else {
                        val colorBg = ATHUtil.resolveColor(activity, android.R.attr.colorBackground)
                        if (ColorUtil.isColorLight(colorBg)) {
                            holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_black_1000))
                            holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.md_black_1000))
                            holder.text2?.setTextColor(ContextCompat.getColor(activity, R.color.md_black_1000))
                            holder.menu?.setColorFilter(ContextCompat.getColor(activity, R.color.md_black_1000))
                            imageView.setColorFilter(ContextCompat.getColor(activity, R.color.md_black_1000))
                        }else {
                            holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                            holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                            holder.text2?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                            holder.menu?.setColorFilter(ContextCompat.getColor(activity, R.color.md_white_1000))
                            imageView.setColorFilter(ContextCompat.getColor(activity, R.color.md_white_1000))
                        }
                    }
                }
            }
            else -> {
                holder.title?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                holder.text?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                holder.text2?.setTextColor(ContextCompat.getColor(activity, R.color.md_white_1000))
                holder.menu?.setColorFilter(ContextCompat.getColor(activity, R.color.md_white_1000))
                imageView.setColorFilter(ContextCompat.getColor(activity, R.color.md_white_1000))
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

    @SuppressLint("NotifyDataSetChanged")
    private fun activateButtons(activate: Boolean) {
        this.activate = activate
        notifyDataSetChanged() //need to call it for the child views to be re-created with buttons.
    }

    fun setButtonsActivate() {
        if (imageviewDragView.visibility == View.VISIBLE) {
            activateButtons(false)
        }else {
            activateButtons(true)
        }
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
        if (PreferenceUtil.queueStyle == "trio") {
            holder.text2?.alpha = 0.5f
        }

        if(PreferenceUtil.queueStyleLand == "trio") {
            holder.text2?.alpha = 0.5f
        }
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