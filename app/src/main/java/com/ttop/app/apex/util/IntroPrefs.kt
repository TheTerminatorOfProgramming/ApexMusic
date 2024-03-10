package com.ttop.app.apex.util

import android.content.Context
import androidx.core.content.edit
import com.ttop.app.apex.INTRO_SHOWN
import com.ttop.app.apex.INTRO_SLIDES_SHOWN

class IntroPrefs(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("IntroPrefs",Context.MODE_PRIVATE)

    var hasIntroSlidesShown
        get() = sharedPreferences.getBoolean(
            INTRO_SLIDES_SHOWN, false
        )

        set(value) = sharedPreferences.edit {
            putBoolean(INTRO_SLIDES_SHOWN, value)}

    var hasIntroShown
        get() = sharedPreferences.getBoolean(
            INTRO_SHOWN, false
        )

        set(value) = sharedPreferences.edit {
            putBoolean(INTRO_SHOWN, value)}

}