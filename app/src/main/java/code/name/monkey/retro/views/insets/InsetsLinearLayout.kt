package code.name.monkey.retro.views.insets

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import code.name.monkey.retro.extensions.drawAboveSystemBarsWithPadding
import code.name.monkey.retro.util.RetroUtil

class InsetsLinearLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    init {
        if (!RetroUtil.isLandscape())
            drawAboveSystemBarsWithPadding()
    }
}