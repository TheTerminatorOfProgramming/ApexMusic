package com.ttop.app.apex.views

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.graphics.drawable.DrawableCompat
import androidx.mediarouter.R
import androidx.mediarouter.R.styleable.MediaRouteButton_externalRouteEnabledDrawable
import androidx.mediarouter.app.MediaRouteActionProvider
import androidx.mediarouter.app.MediaRouteButton
import com.ttop.app.apex.extensions.accentColor

class ThemeableMediaRouteActionProvider(context: Context?) :
    MediaRouteActionProvider(context!!) {

    override fun onCreateMediaRouteButton(): MediaRouteButton {
        val button: MediaRouteButton = super.onCreateMediaRouteButton()
        colorWorkaroundForCastIcon(button)
        return button
    }

    val mediaRouteButton1: MediaRouteButton?
        get() {
            val button: MediaRouteButton? = super.getMediaRouteButton()
            colorWorkaroundForCastIcon(button)
            return button
        }

    @SuppressLint("PrivateResource")
    private fun colorWorkaroundForCastIcon(button: MediaRouteButton?) {
        if (button == null) return
        val castContext: Context =
            ContextThemeWrapper(context, R.style.Theme_MediaRouter)

        val a = castContext.obtainStyledAttributes(
            null,
            R.styleable.MediaRouteButton,
            R.attr.mediaRouteButtonStyle,
            0
        )
        val drawable =
            a.getDrawable(MediaRouteButton_externalRouteEnabledDrawable)
        a.recycle()
        DrawableCompat.setTint(drawable!!, context.accentColor())
        drawable.setState(button.drawableState)
        button.setRemoteIndicatorDrawable(drawable)
    }
}