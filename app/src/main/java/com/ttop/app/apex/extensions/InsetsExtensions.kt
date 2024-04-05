package com.ttop.app.apex.extensions

import androidx.core.view.WindowInsetsCompat
import com.ttop.app.apex.util.ApexUtil

fun WindowInsetsCompat?.getBottomInsets(): Int {
    return  this?.getInsets(WindowInsetsCompat.Type.systemBars())?.bottom ?: ApexUtil.navigationBarHeight
}
