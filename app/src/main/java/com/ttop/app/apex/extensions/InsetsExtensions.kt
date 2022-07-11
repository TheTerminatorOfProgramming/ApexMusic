package com.ttop.app.apex.extensions

import androidx.core.view.WindowInsetsCompat
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.PreferenceUtil

fun WindowInsetsCompat?.getBottomInsets(): Int {
    return if (PreferenceUtil.isFullScreenMode) {
        return 0
    } else {
        this?.getInsets(WindowInsetsCompat.Type.systemBars())?.bottom ?: ApexUtil.navigationBarHeight
    }
}
