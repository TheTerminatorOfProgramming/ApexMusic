package code.name.monkey.retro.views.insets

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import code.name.monkey.retro.extensions.drawAboveSystemBarsWithPadding
import code.name.monkey.retro.util.RetroUtil

class InsetsConstraintLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    init {
        if (!RetroUtil.isLandscape())
            drawAboveSystemBarsWithPadding()
    }
}