package com.ttop.app.apex.extensions

import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat

fun Context.showToast(@StringRes stringRes: Int, duration: Int = Toast.LENGTH_SHORT) {
    showToast(getString(stringRes), duration)
}

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

val Context.isLandscape: Boolean get() = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

val Context.isTablet: Boolean get() = resources.configuration.smallestScreenWidthDp >= 600

@ColorInt
fun Context.getColorResCompat(@AttrRes id: Int): Int {
    val resolvedAttr = TypedValue()
    this.theme.resolveAttribute(id, resolvedAttr, true)
    val colorRes = resolvedAttr.run { if (resourceId != 0) resourceId else data }
    return ContextCompat.getColor(this, colorRes)
}

fun Context.getTintedDrawable(@DrawableRes id: Int, @ColorInt color: Int): Drawable {
    return ContextCompat.getDrawable(this, id)?.tint(color)!!
}


fun Context.getDayNightTintedDrawable(@DrawableRes id: Int): Drawable {
    return ContextCompat.getDrawable(this, id)
        ?.tint(getColorResCompat(android.R.attr.textColorPrimary))!!
}
