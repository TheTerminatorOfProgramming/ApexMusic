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
package com.ttop.app.apex.adapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.ttop.app.apex.R
import com.ttop.app.apex.databinding.PreferenceDialogLibraryCategoriesListitemBinding
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.libraries.appthemehelper.ThemeStore.Companion.accentColor
import com.ttop.app.apex.model.CategoryInfo
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.SwipeAndDragHelper
import com.ttop.app.apex.util.SwipeAndDragHelper.ActionCompletionContract

class CategoryInfoAdapter : RecyclerView.Adapter<CategoryInfoAdapter.ViewHolder>(),
    ActionCompletionContract {
    var categoryInfos: MutableList<CategoryInfo> =
        PreferenceUtil.libraryCategory.toMutableList()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    private val touchHelper: ItemTouchHelper
    fun attachToRecyclerView(recyclerView: RecyclerView?) {
        touchHelper.attachToRecyclerView(recyclerView)
    }

    override fun getItemCount(): Int {
        return categoryInfos.size
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val categoryInfo = categoryInfos[position]
        holder.binding.checkbox.isChecked = categoryInfo.visible
        holder.binding.title.text =
            holder.binding.title.resources.getString(categoryInfo.category.stringRes)

        if (holder.binding.title.text == holder.itemView.context.getString(R.string.action_settings)) {
            holder.binding.checkbox.isChecked = true
            holder.itemView.isEnabled = false
        }

        if (holder.binding.title.text == holder.itemView.context.getString(R.string.queue_short)) {
            holder.binding.checkbox.isChecked = true
            holder.itemView.isEnabled = false
        }

        holder.itemView.setOnClickListener {
            if (!(categoryInfo.visible && isLastCheckedCategory(categoryInfo))) {
                categoryInfo.visible = !categoryInfo.visible
                holder.binding.checkbox.isChecked = categoryInfo.visible
            } else {
                holder.itemView.context.showToast(R.string.you_have_to_select_at_least_one_category)
            }
        }
        holder.binding.dragView.setOnTouchListener { _: View?, event: MotionEvent ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                touchHelper.startDrag(holder)
            }
            false
        }

        when (PreferenceUtil.fontSize) {
            "12" -> {
                holder.binding.title.textSize = 12f
            }

            "13" -> {
                holder.binding.title.textSize = 13f
            }

            "14" -> {
                holder.binding.title.textSize = 14f
            }

            "15" -> {
                holder.binding.title.textSize = 15f
            }

            "16" -> {
                holder.binding.title.textSize = 16f
            }

            "17" -> {
                holder.binding.title.textSize = 17f
            }

            "18" -> {
                holder.binding.title.textSize = 18f
            }

            "19" -> {
                holder.binding.title.textSize = 19f
            }

            "20" -> {
                holder.binding.title.textSize = 20f
            }

            "21" -> {
                holder.binding.title.textSize = 21f
            }

            "22" -> {
                holder.binding.title.textSize = 22f
            }

            "23" -> {
                holder.binding.title.textSize = 23f
            }

            "24" -> {
                holder.binding.title.textSize = 24f
            }
        }


    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ViewHolder {
        return ViewHolder(
            PreferenceDialogLibraryCategoriesListitemBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun onViewMoved(oldPosition: Int, newPosition: Int) {
        val categoryInfo = categoryInfos[oldPosition]
        categoryInfos.removeAt(oldPosition)
        categoryInfos.add(newPosition, categoryInfo)
        notifyItemMoved(oldPosition, newPosition)
    }

    private fun isLastCheckedCategory(categoryInfo: CategoryInfo): Boolean {
        if (categoryInfo.visible) {
            for (c in categoryInfos) {
                if (c !== categoryInfo && c.visible) {
                    return false
                }
            }
        }
        return true
    }

    class ViewHolder(val binding: PreferenceDialogLibraryCategoriesListitemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.checkbox.buttonTintList =
                ColorStateList.valueOf(accentColor(binding.checkbox.context))
        }
    }

    init {
        val swipeAndDragHelper = SwipeAndDragHelper(this)
        touchHelper = ItemTouchHelper(swipeAndDragHelper)
    }
}