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

package com.ttop.app.apex.preferences

import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat.SRC_IN
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceViewHolder
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.ttop.app.apex.R
import com.ttop.app.apex.databinding.PreferenceDialogNowPlayingScreenBinding
import com.ttop.app.apex.databinding.PreferenceNowPlayingScreenItemBinding
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.extensions.centeredColorButtons
import com.ttop.app.apex.extensions.colorControlNormal
import com.ttop.app.apex.extensions.materialDialog
import com.ttop.app.apex.extensions.surfaceColor
import com.ttop.app.apex.libraries.appthemehelper.common.prefs.supportv7.ATEDialogPreference
import com.ttop.app.apex.ui.fragments.AlbumCoverStyle
import com.ttop.app.apex.ui.fragments.AlbumCoverStyle.values
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.ViewUtil
import com.ttop.app.apex.util.theme.ThemeMode

class AlbumCoverStylePreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1,
    defStyleRes: Int = -1
) : ATEDialogPreference(context, attrs, defStyleAttr, defStyleRes) {

    private val mLayoutRes = R.layout.preference_dialog_now_playing_screen

    override fun getDialogLayoutResource(): Int {
        return mLayoutRes
    }

    init {
        layoutResource = R.layout.custom_preference
        icon?.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                context.colorControlNormal(),
                SRC_IN
            )
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val cardview = holder.itemView.findViewById<MaterialCardView>(R.id.listCard)
        cardview?.strokeColor = com.ttop.app.apex.libraries.appthemehelper.ThemeStore.accentColor(context)
        cardview?.setBackgroundColor(context.surfaceColor())

        val title = holder.itemView.findViewById<TextView>(android.R.id.title)
        val summary = holder.itemView.findViewById<TextView>(android.R.id.summary)

        when (PreferenceUtil.getGeneralThemeValue()) {
            ThemeMode.AUTO -> {
                when (context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        title.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
                        summary.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
                    }
                    Configuration.UI_MODE_NIGHT_NO,
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                        title.setTextColor(ContextCompat.getColor(context, R.color.darkColorSurface))
                        summary.setTextColor(ContextCompat.getColor(context, R.color.darkColorSurface))
                    }
                    else -> {
                        title.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
                        summary.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
                    }
                }
            }
            ThemeMode.AUTO_BLACK -> {
                when (context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        title.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
                        summary.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
                    }
                    Configuration.UI_MODE_NIGHT_NO,
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                        title.setTextColor(ContextCompat.getColor(context, R.color.blackColorSurface))
                        summary.setTextColor(ContextCompat.getColor(context, R.color.blackColorSurface))
                    }
                    else -> {
                        title.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
                        summary.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
                    }
                }
            }
            ThemeMode.BLACK,
            ThemeMode.DARK -> {
                title.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
                summary.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
            }
            ThemeMode.LIGHT -> {
                title.setTextColor(ContextCompat.getColor(context, R.color.darkColorSurface))
                summary.setTextColor(ContextCompat.getColor(context, R.color.darkColorSurface))
            }
            ThemeMode.MD3 -> {
                title.setTextColor(ContextCompat.getColor(context, R.color.m3_widget_other_text))
                summary.setTextColor(ContextCompat.getColor(context, R.color.m3_widget_other_text))
            }
        }
    }
}

class AlbumCoverStylePreferenceDialog : DialogFragment(),
    ViewPager.OnPageChangeListener {

    private var viewPagerPosition: Int = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = PreferenceDialogNowPlayingScreenBinding.inflate(layoutInflater)
        binding.nowPlayingScreenViewPager.apply {
            adapter = AlbumCoverStyleAdapter(requireContext())
            addOnPageChangeListener(this@AlbumCoverStylePreferenceDialog)
            pageMargin = ViewUtil.convertDpToPixel(32f, resources).toInt()
            currentItem = PreferenceUtil.albumCoverStyle.ordinal
        }

        val dialogTitle = TextView(requireContext())
        dialogTitle.text = ContextCompat.getString(requireContext(), R.string.pref_title_now_playing_screen_appearance)
        dialogTitle.setTextColor(accentColor())
        dialogTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25f)
        dialogTitle.textAlignment = View.TEXT_ALIGNMENT_CENTER

        return materialDialog(R.string.pref_title_album_cover_style)
            .setCustomTitle(dialogTitle)
            .setPositiveButton(R.string.set) { _, _ ->
                val coverStyle = AlbumCoverStyle.entries[viewPagerPosition]
                PreferenceUtil.albumCoverStyle = coverStyle
            }
            .setView(binding.root)
            .create()
            .centeredColorButtons()
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        this.viewPagerPosition = position
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    private class AlbumCoverStyleAdapter(private val context: Context) :
        PagerAdapter() {

        override fun instantiateItem(collection: ViewGroup, position: Int): Any {
            val albumCoverStyle = AlbumCoverStyle.entries[position]

            val inflater = LayoutInflater.from(context)
            val binding = PreferenceNowPlayingScreenItemBinding.inflate(inflater, collection, true)

            Glide.with(context).load(albumCoverStyle.drawableResId).into(binding.image)
            binding.title.setText(albumCoverStyle.titleRes)
            when (PreferenceUtil.getGeneralThemeValue()) {
                ThemeMode.AUTO -> {
                    when (context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                        Configuration.UI_MODE_NIGHT_YES -> {
                            binding.title.setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.md_white_1000
                                )
                            )
                        }

                        Configuration.UI_MODE_NIGHT_NO,
                        Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                            binding.title.setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.darkColorSurface
                                )
                            )
                        }

                        else -> {
                            binding.title.setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.md_white_1000
                                )
                            )
                        }
                    }
                }

                ThemeMode.AUTO_BLACK -> {
                    when (context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                        Configuration.UI_MODE_NIGHT_YES -> {
                            binding.title.setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.md_white_1000
                                )
                            )
                        }

                        Configuration.UI_MODE_NIGHT_NO,
                        Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                            binding.title.setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.blackColorSurface
                                )
                            )
                        }

                        else -> {
                            binding.title.setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.md_white_1000
                                )
                            )
                        }
                    }
                }

                ThemeMode.BLACK,
                ThemeMode.DARK -> {
                    binding.title.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
                }

                ThemeMode.LIGHT -> {
                    binding.title.setTextColor(ContextCompat.getColor(context, R.color.darkColorSurface))
                }

                ThemeMode.MD3 -> {
                    binding.title.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.m3_widget_other_text
                        )
                    )
                }
            }
            return binding.root
        }

        override fun destroyItem(
            collection: ViewGroup,
            position: Int,
            view: Any
        ) {
            collection.removeView(view as View)
        }

        override fun getCount(): Int {
            return AlbumCoverStyle.entries.size
        }

        override fun isViewFromObject(view: View, instace: Any): Boolean {
            return view === instace
        }

        override fun getPageTitle(position: Int): CharSequence {
            return context.getString(AlbumCoverStyle.entries[position].titleRes)
        }
    }

    companion object {
        val TAG: String = AlbumCoverStylePreferenceDialog::class.java.simpleName

        fun newInstance(): AlbumCoverStylePreferenceDialog {
            return AlbumCoverStylePreferenceDialog()
        }
    }
}

