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
package com.ttop.app.apex.extensions

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.widget.Button
import android.widget.CheckBox
import android.widget.SeekBar
import androidx.annotation.AttrRes
import androidx.annotation.CheckResult
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputLayout
import com.ttop.app.apex.R
import com.ttop.app.apex.libraries.appthemehelper.ThemeStore
import com.ttop.app.apex.libraries.appthemehelper.util.ATHColorUtil
import com.ttop.app.apex.libraries.appthemehelper.util.ATHUtil
import com.ttop.app.apex.libraries.appthemehelper.util.MaterialValueHelper

fun Int.ripAlpha(): Int {
    return ATHColorUtil.stripAlpha(this)
}

fun Dialog.colorControlNormal() = resolveColor(android.R.attr.colorControlNormal)

fun Toolbar.backgroundTintList() {
    val surfaceColor = ATHUtil.resolveColor(context, R.attr.colorSurface, Color.BLACK)
    val colorStateList = ColorStateList.valueOf(surfaceColor)
    backgroundTintList = colorStateList
}

fun Context.accentColor() = ThemeStore.accentColor(this)

fun Fragment.accentColor() = ThemeStore.accentColor(requireContext())

fun Context.surfaceColor() = resolveColor(R.attr.colorSurface, Color.WHITE)

fun Fragment.surfaceColor() = resolveColor(R.attr.colorSurface, Color.WHITE)

fun Context.surfaceColor(fallBackColor: Int) = resolveColor(R.attr.colorSurface, fallBackColor)

fun Context.textColorSecondary() = resolveColor(android.R.attr.textColorSecondary)

fun Fragment.textColorSecondary() = resolveColor(android.R.attr.textColorSecondary)

fun Context.colorControlNormal() = resolveColor(android.R.attr.colorControlNormal)

fun Fragment.colorControlNormal() = resolveColor(android.R.attr.colorControlNormal)

fun Context.colorBackground() = resolveColor(android.R.attr.colorBackground)

fun Fragment.colorBackground() = resolveColor(android.R.attr.colorBackground)

fun Context.textColorPrimary() = resolveColor(android.R.attr.textColorPrimary)

fun Fragment.textColorPrimary() = resolveColor(android.R.attr.textColorPrimary)

fun Context.defaultFooterColor() = resolveColor(R.attr.defaultFooterColor)

fun Context.resolveColor(@AttrRes attr: Int, fallBackColor: Int = 0) =
    ATHUtil.resolveColor(this, attr, fallBackColor)

fun Fragment.resolveColor(@AttrRes attr: Int, fallBackColor: Int = 0) =
    ATHUtil.resolveColor(requireContext(), attr, fallBackColor)

fun Dialog.resolveColor(@AttrRes attr: Int, fallBackColor: Int = 0) =
    ATHUtil.resolveColor(context, attr, fallBackColor)

// Don't apply accent colors if Material You is enabled
// Material Components will take care of applying material you colors
fun CheckBox.addAccentColor() {
    buttonTintList = ColorStateList.valueOf(ThemeStore.accentColor(context))
}

fun SeekBar.addAccentColor() {
    val colorState = ColorStateList.valueOf(ThemeStore.accentColor(context))
    progressTintList = colorState
    thumbTintList = colorState
}

fun Slider.addAccentColor() {
    val accentColor = ThemeStore.accentColor(context)
    trackActiveTintList = accentColor.colorStateList
    trackInactiveTintList = ATHColorUtil.withAlpha(accentColor, 0.5F).colorStateList
    thumbTintList = accentColor.colorStateList
}

fun Slider.accent() {
    val accentColor = context.accentColor()
    thumbTintList = accentColor.colorStateList
    trackActiveTintList = accentColor.colorStateList
    trackInactiveTintList = ATHColorUtil.withAlpha(accentColor, 0.1F).colorStateList
}

fun Button.accentTextColor() {
    setTextColor(ThemeStore.accentColor(context))
}

fun MaterialButton.accentOutlineColor() {
    val color = ThemeStore.accentColor(context)
    val colorStateList = ColorStateList.valueOf(color)
    iconTint = colorStateList
    strokeColor = colorStateList
    setTextColor(colorStateList)
    rippleColor = colorStateList
}

fun MaterialButton.elevatedAccentColor() {
    val color = context.darkAccentColorVariant()
    rippleColor = ColorStateList.valueOf(color)
    setBackgroundColor(color)
    setTextColor(MaterialValueHelper.getPrimaryTextColor(context, color.isColorLight))
    iconTint = ColorStateList.valueOf(context.accentColor())
}

fun SeekBar.applyColor(@ColorInt color: Int) {
    thumbTintList = ColorStateList.valueOf(color)
    progressTintList = ColorStateList.valueOf(color)
    progressBackgroundTintList = ColorStateList.valueOf(color)
}

fun Slider.applyColor(@ColorInt color: Int) {
    ColorStateList.valueOf(color).run {
        thumbTintList = this
        trackActiveTintList = this
        trackInactiveTintList = ColorStateList.valueOf(color.addAlpha(0.1f))
        haloTintList = this
    }
}

fun ExtendedFloatingActionButton.accentColor() {
    val color = ThemeStore.accentColor(context)
    val textColor =
        MaterialValueHelper.getPrimaryTextColor(context, ATHColorUtil.isColorLight(color))
    val colorStateList = ColorStateList.valueOf(color)
    val textColorStateList = ColorStateList.valueOf(textColor)
    backgroundTintList = colorStateList
    setTextColor(textColorStateList)
    iconTint = textColorStateList
}

fun FloatingActionButton.accentColor() {
    val color = ThemeStore.accentColor(context)
    val textColor =
        MaterialValueHelper.getPrimaryTextColor(context, ATHColorUtil.isColorLight(color))
    backgroundTintList = ColorStateList.valueOf(color)
    imageTintList = ColorStateList.valueOf(textColor)
}

fun MaterialButton.applyColor(color: Int) {
    val backgroundColorStateList = ColorStateList.valueOf(color)
    val textColorColorStateList = ColorStateList.valueOf(
        MaterialValueHelper.getPrimaryTextColor(
            context,
            ATHColorUtil.isColorLight(color)
        )
    )
    backgroundTintList = backgroundColorStateList
    setTextColor(textColorColorStateList)
    iconTint = textColorColorStateList
}

fun MaterialButton.accentColor() {
    applyColor(ThemeStore.accentColor(context))
}

fun MaterialButton.applyOutlineColor(color: Int) {
    val colorStateList = ColorStateList.valueOf(color)
    iconTint = colorStateList
    strokeColor = colorStateList
    setTextColor(colorStateList)
    rippleColor = colorStateList
}

fun TextInputLayout.accentColor() {
    val accentColor = ThemeStore.accentColor(context)
    val colorState = ColorStateList.valueOf(accentColor)
    boxStrokeColor = accentColor
    defaultHintTextColor = colorState
    isHintAnimationEnabled = true
}

fun CircularProgressIndicator.accentColor() {
    val color = ThemeStore.accentColor(context)
    setIndicatorColor(color)
    trackColor = ATHColorUtil.withAlpha(color, 0.2f)
}

fun CircularProgressIndicator.applyColor(color: Int) {
    setIndicatorColor(color)
    trackColor = ATHColorUtil.withAlpha(color, 0.2f)
}

fun AppCompatImageView.accentColor(): Int = ThemeStore.accentColor(context)

fun TextInputLayout.setTint(background: Boolean = true) {
    val accentColor = ThemeStore.accentColor(context)
    val colorState = ColorStateList.valueOf(accentColor)

    if (background) {
        backgroundTintList = colorState
        defaultHintTextColor = colorState
    } else {
        boxStrokeColor = accentColor
        defaultHintTextColor = colorState
        isHintAnimationEnabled = true
    }
}

@CheckResult
fun Drawable.tint(@ColorInt color: Int): Drawable {
    val tintedDrawable = DrawableCompat.wrap(this).mutate()
    setTint(color)
    return tintedDrawable
}

@CheckResult
fun Drawable.tint(context: Context, @ColorRes color: Int): Drawable =
    tint(context.getColorCompat(color))

@ColorInt
fun Context.getColorCompat(@ColorRes colorRes: Int): Int {
    return ContextCompat.getColor(this, colorRes)
}

@ColorInt
fun Context.darkAccentColor(): Int {
    return ColorUtils.blendARGB(
        if (ThemeStore.isMD3Enabled(applicationContext)) {
            ContextCompat.getColor(applicationContext, R.color.m3_widget_background)
        } else {
            accentColor()
        },
        surfaceColor(),
        if (ThemeStore.isMD3Enabled(applicationContext)) {
            0.7f
        } else {
            if (surfaceColor().isColorLight) 0.9f else 0.92f
        }
    )
}

@ColorInt
fun Context.darkAccentColorVariant(): Int {
    return ColorUtils.blendARGB(
        if (ThemeStore.isMD3Enabled(applicationContext)) {
            ContextCompat.getColor(applicationContext, R.color.m3_widget_background)
        } else {
            accentColor()
        },
        surfaceColor(),
        if (ThemeStore.isMD3Enabled(applicationContext)) {
            0.75f
        } else {
            if (surfaceColor().isColorLight) 0.9f else 0.95f
        }
    )
}

inline val @receiver:ColorInt Int.isColorLight
    get() = ATHColorUtil.isColorLight(this)

inline val Int.colorStateList: ColorStateList
    get() = ColorStateList.valueOf(this)

fun @receiver:ColorInt Int.addAlpha(alpha: Float): Int {
    return ATHColorUtil.withAlpha(this, alpha)
}