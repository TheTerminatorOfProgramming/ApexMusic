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

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ttop.app.apex.R
import com.ttop.app.apex.extensions.openUrl
import com.ttop.app.apex.model.Contributor
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.views.ApexShapeableImageView

class ContributorAdapter(
    private var contributors: List<Contributor>,
) : RecyclerView.Adapter<ContributorAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == HEADER) {
            ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_contributor_header,
                    parent,
                    false
                )
            )
        } else ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_contributor,
                parent,
                false
            )
        )
    }

    companion object {
        const val HEADER: Int = 0
        const val ITEM: Int = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            HEADER
        } else {
            ITEM
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contributor = contributors[position]
        holder.bindData(contributor)
        holder.itemView.setOnClickListener {
            it?.context?.openUrl(contributors[position].link)
        }
    }

    override fun getItemCount(): Int {
        return contributors.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun swapData(it: List<Contributor>) {
        contributors = it
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val text: TextView = itemView.findViewById(R.id.text)
        val image: ApexShapeableImageView = itemView.findViewById(R.id.icon)

        internal fun bindData(contributor: Contributor) {
            title.text = contributor.name
            text.text = contributor.summary
            if (PreferenceUtil.isInternetConnected) {
                Glide.with(image.context)
                    .load(contributor.image)
                    .error(R.drawable.ic_account)
                    .placeholder(R.drawable.ic_account)
                    .dontAnimate()
                    .into(image)
            } else {
                Glide.with(image.context)
                    .load("file:///android_asset/images/${contributor.image}".toUri())
                    .error(R.drawable.ic_account)
                    .placeholder(R.drawable.ic_account)
                    .into(image)
            }
        }
    }
}