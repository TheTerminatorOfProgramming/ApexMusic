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

package com.ttop.app.apex.views

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import com.ttop.app.apex.R
import com.ttop.app.apex.databinding.ListItemViewNoCardBinding
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.extensions.hide
import com.ttop.app.apex.extensions.show
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.theme.ThemeMode

/**
 * Created by hemanths on 2019-10-02.
 */
class ListItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1
) : FrameLayout(context, attrs, defStyleAttr) {

    private var binding =
        ListItemViewNoCardBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        context.withStyledAttributes(attrs, R.styleable.ListItemView) {
            if (hasValue(R.styleable.ListItemView_listItemIcon)) {
                binding.icon.setImageDrawable(getDrawable(R.styleable.ListItemView_listItemIcon))
                when (PreferenceUtil.getGeneralThemeValue()) {
                    ThemeMode.AUTO -> {
                        when (context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                            Configuration.UI_MODE_NIGHT_YES -> {
                                binding.icon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.md_white_1000))
                            }
                            Configuration.UI_MODE_NIGHT_NO,
                            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                                binding.icon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.darkColorSurface))
                            }
                            else -> {
                                binding.icon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.md_white_1000))
                            }
                        }
                    }
                    ThemeMode.AUTO_BLACK -> {
                        when (context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                            Configuration.UI_MODE_NIGHT_YES -> {
                                binding.icon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.md_white_1000))
                            }
                            Configuration.UI_MODE_NIGHT_NO,
                            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                                binding.icon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.blackColorSurface))
                            }
                            else -> {
                                binding.icon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.md_white_1000))
                            }
                        }
                    }
                    ThemeMode.BLACK,
                    ThemeMode.DARK -> {
                        binding.icon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.md_white_1000))
                    }
                    ThemeMode.LIGHT -> {
                        binding.icon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.darkColorSurface))
                    }
                    ThemeMode.MD3 -> {
                        binding.icon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.m3_widget_other_text))
                    }
                }
            } else {
                binding.icon.hide()
            }

            binding.title.text = getText(R.styleable.ListItemView_listItemTitle)
            when (PreferenceUtil.getGeneralThemeValue()) {
                ThemeMode.AUTO -> {
                    when (context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                        Configuration.UI_MODE_NIGHT_YES -> {
                            binding.title.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
                        }
                        Configuration.UI_MODE_NIGHT_NO,
                        Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                            binding.title.setTextColor(ContextCompat.getColor(context, R.color.darkColorSurface))
                        }
                        else -> {
                            binding.title.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
                        }
                    }
                }
                ThemeMode.AUTO_BLACK -> {
                    when (context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                        Configuration.UI_MODE_NIGHT_YES -> {
                            binding.title.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
                        }
                        Configuration.UI_MODE_NIGHT_NO,
                        Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                            binding.title.setTextColor(ContextCompat.getColor(context, R.color.blackColorSurface))
                        }
                        else -> {
                            binding.title.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
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
                    binding.title.setTextColor(ContextCompat.getColor(context, R.color.m3_widget_other_text))
                }
            }
            if (hasValue(R.styleable.ListItemView_listItemSummary)) {
                binding.summary.text = getText(R.styleable.ListItemView_listItemSummary)
                when (PreferenceUtil.getGeneralThemeValue()) {
                    ThemeMode.AUTO -> {
                        when (context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                            Configuration.UI_MODE_NIGHT_YES -> {
                                binding.summary.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
                            }
                            Configuration.UI_MODE_NIGHT_NO,
                            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                                binding.summary.setTextColor(ContextCompat.getColor(context, R.color.darkColorSurface))
                            }
                            else -> {
                                binding.summary.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
                            }
                        }
                    }
                    ThemeMode.AUTO_BLACK -> {
                        when (context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                            Configuration.UI_MODE_NIGHT_YES -> {
                                binding.summary.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
                            }
                            Configuration.UI_MODE_NIGHT_NO,
                            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                                binding.summary.setTextColor(ContextCompat.getColor(context, R.color.blackColorSurface))
                            }
                            else -> {
                                binding.summary.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
                            }
                        }
                    }
                    ThemeMode.BLACK,
                    ThemeMode.DARK -> {
                        binding.summary.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
                    }
                    ThemeMode.LIGHT -> {
                        binding.summary.setTextColor(ContextCompat.getColor(context, R.color.darkColorSurface))
                    }
                    ThemeMode.MD3 -> {
                        binding.summary.setTextColor(ContextCompat.getColor(context, R.color.m3_widget_other_text))
                    }
                }
            } else {
                binding.summary.hide()
            }
        }
    }

    fun setSummary(appVersion: String) {
        binding.summary.show()
        binding.summary.text = appVersion
        when (PreferenceUtil.getGeneralThemeValue()) {
            ThemeMode.AUTO -> {
                when (context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        binding.summary.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
                    }
                    Configuration.UI_MODE_NIGHT_NO,
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                        binding.summary.setTextColor(ContextCompat.getColor(context, R.color.darkColorSurface))
                    }
                    else -> {
                        binding.summary.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
                    }
                }
            }
            ThemeMode.AUTO_BLACK -> {
                when (context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        binding.summary.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
                    }
                    Configuration.UI_MODE_NIGHT_NO,
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                        binding.summary.setTextColor(ContextCompat.getColor(context, R.color.blackColorSurface))
                    }
                    else -> {
                        binding.summary.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
                    }
                }
            }
            ThemeMode.BLACK,
            ThemeMode.DARK -> {
                binding.summary.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000))
            }
            ThemeMode.LIGHT -> {
                binding.summary.setTextColor(ContextCompat.getColor(context, R.color.darkColorSurface))
            }
            ThemeMode.MD3 -> {
                binding.summary.setTextColor(ContextCompat.getColor(context, R.color.m3_widget_other_text))
            }
        }
    }
}