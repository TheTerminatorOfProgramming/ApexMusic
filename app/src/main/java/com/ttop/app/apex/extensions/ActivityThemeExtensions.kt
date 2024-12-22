package com.ttop.app.apex.extensions

import android.app.ActivityManager
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isGone
import androidx.fragment.app.FragmentActivity
import com.ttop.app.apex.R
import com.ttop.app.apex.libraries.appthemehelper.util.ATHColorUtil
import com.ttop.app.apex.libraries.appthemehelper.util.VersionUtils
import com.ttop.app.apex.util.PreferenceUtil

fun AppCompatActivity.keepScreenOn(keepScreenOn: Boolean) {
    if (keepScreenOn) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    } else {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}

fun AppCompatActivity.setEdgeToEdgeOrImmersive() {
    if (PreferenceUtil.isFullScreenMode) {
        setImmersiveFullscreen()
    } else {
        setDrawBehindSystemBars()
    }
}

fun AppCompatActivity.setImmersiveFullscreen() {
    if (PreferenceUtil.isFullScreenMode) {
        WindowInsetsControllerCompat(window, window.decorView).apply {
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }

        window.attributes.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets ->
            if (insets.displayCutout != null) {
                insets
            } else {
                // Consume insets if display doesn't have a Cutout
                WindowInsetsCompat.CONSUMED
            }
        }
    }
}

fun AppCompatActivity.exitFullscreen() {
    WindowInsetsControllerCompat(window, window.decorView).apply {
        show(WindowInsetsCompat.Type.systemBars())
    }
}

fun AppCompatActivity.hideStatusBar() {
    hideStatusBar(PreferenceUtil.isFullScreenMode)
}

fun AppCompatActivity.hideStatusBar(fullscreen: Boolean) {
    val statusBar = window.decorView.rootView.findViewById<View>(R.id.status_bar)
    if (statusBar != null) {
        statusBar.isGone = fullscreen
    }
}

fun AppCompatActivity.setDrawBehindSystemBars() {
    if (VersionUtils.hasSv2()) {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.navigationBarColor = Color.BLACK
    } else {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.navigationBarColor = Color.TRANSPARENT
    }
    window.statusBarColor = Color.TRANSPARENT
    window.isNavigationBarContrastEnforced = false
}

fun FragmentActivity.setTaskDescriptionColor(color: Int) {
    var colorFinal = color
    // Task description requires fully opaque color
    colorFinal = ATHColorUtil.stripAlpha(colorFinal)
    // Sets color of entry in the system overview screen

    if (VersionUtils.hasT()) {
        val td = ActivityManager.TaskDescription.Builder()
            .setLabel(title as String?)
            .setIcon(-1)
            .setPrimaryColor(colorFinal)
            .build()

        setTaskDescription(td)
    } else {
        setTaskDescription(
            ActivityManager.TaskDescription(
                title as String?,
                -1,
                colorFinal
            )
        )
    }
}

fun AppCompatActivity.setTaskDescriptionColorAuto() {
    setTaskDescriptionColor(surfaceColor())
}

/**
 * This will set the color of the view with the id "status_bar" on KitKat and Lollipop. On
 * Lollipop if no such view is found it will set the statusbar color using the native method.
 *
 * @param color the new statusbar color (will be shifted down on Lollipop and above)
 */
fun AppCompatActivity.setStatusBarColor(color: Int) {
    val statusBar = window.decorView.rootView.findViewById<View>(R.id.status_bar)
    if (statusBar != null) {
        statusBar.setBackgroundColor(color)
    } else {
        window.statusBarColor = color
    }
}

fun AppCompatActivity.hideSoftKeyboard() {
    val currentFocus: View? = currentFocus
    if (currentFocus != null) {
        val inputMethodManager =
            getSystemService<InputMethodManager>()
        inputMethodManager?.hideSoftInputFromWindow(currentFocus.windowToken, 0)
    }
}