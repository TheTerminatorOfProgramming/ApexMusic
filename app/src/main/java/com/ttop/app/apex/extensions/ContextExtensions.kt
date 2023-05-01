package com.ttop.app.apex.extensions

import android.content.ClipData
import android.content.ClipboardManager
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

fun Context.showLongToast(@StringRes stringRes: Int, duration: Int = Toast.LENGTH_LONG) {
    showToast(getString(stringRes), duration)
}

fun Context.showLongToast(message: String, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, message, duration).show()
}

val Context.isLandscape: Boolean get() = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

val Context.isTablet: Boolean get() = resources.configuration.smallestScreenWidthDp >= 600

fun Context.getTintedDrawable(@DrawableRes id: Int, @ColorInt color: Int): Drawable {
    return ContextCompat.getDrawable(this, id)?.tint(color)!!
}

fun Context.copyToClipboard(text: CharSequence){
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("label",text)
    clipboard.setPrimaryClip(clip)
}

@ColorInt
fun Context.getColorResCompat(@AttrRes id: Int): Int {
    val resolvedAttr = TypedValue()
    this.theme.resolveAttribute(id, resolvedAttr, true)
    val colorRes = resolvedAttr.run { if (resourceId != 0) resourceId else data }
    return ContextCompat.getColor(this, colorRes)
}