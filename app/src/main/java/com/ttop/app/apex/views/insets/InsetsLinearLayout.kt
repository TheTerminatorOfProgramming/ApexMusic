package com.ttop.app.apex.views.insets

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.ttop.app.apex.extensions.drawAboveSystemBarsWithPadding
import com.ttop.app.apex.util.ApexUtil

class InsetsLinearLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    init {
        if (!ApexUtil.isLandscape)
            drawAboveSystemBarsWithPadding()
    }
}