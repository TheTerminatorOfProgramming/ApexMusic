package com.ttop.app.apex.extensions

import androidx.core.view.WindowInsetsCompat
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.RetroUtil

fun WindowInsetsCompat?.safeGetBottomInsets(): Int {
    return if (PreferenceUtil.isFullScreenMode) {
        return 0
    } else {
        this?.getInsets(WindowInsetsCompat.Type.systemBars())?.bottom ?: RetroUtil.getNavigationBarHeight()
    }
}
