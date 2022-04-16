package code.name.monkey.retro.extensions

import androidx.core.view.WindowInsetsCompat
import code.name.monkey.retro.util.PreferenceUtil
import code.name.monkey.retro.util.RetroUtil

fun WindowInsetsCompat?.safeGetBottomInsets(): Int {
    return if (PreferenceUtil.isFullScreenMode) {
        return 0
    } else {
        this?.getInsets(WindowInsetsCompat.Type.systemBars())?.bottom ?: RetroUtil.getNavigationBarHeight()
    }
}
