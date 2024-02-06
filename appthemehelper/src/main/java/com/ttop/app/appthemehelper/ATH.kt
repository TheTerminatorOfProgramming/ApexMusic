package com.ttop.app.appthemehelper

import android.content.Context
import android.view.View
import androidx.annotation.ColorInt
import com.ttop.app.appthemehelper.util.TintHelper

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
object ATH {

    fun didThemeValuesChange(context: Context, since: Long): Boolean {
        return ThemeStore.isConfigured(context) && ThemeStore.prefs(context).getLong(
            ThemeStorePrefKeys.VALUES_CHANGED,
            -1
        ) > since
    }

    fun setTint(view: View, @ColorInt color: Int) {
        TintHelper.setTintAuto(view, color, false)
    }

}