package com.ttop.app.apex.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.core.view.updateLayoutParams
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.shape.MaterialShapeDrawable
import com.ttop.app.apex.databinding.SimpleAppbarLayoutBinding
import com.ttop.app.apex.extensions.accentColor

import com.ttop.app.apex.extensions.darkAccentColor
import com.ttop.app.apex.extensions.surfaceColor
import com.ttop.app.apex.libraries.appthemehelper.util.VersionUtils
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.PreferenceUtil
import dev.chrisbanes.insetter.applyInsetter

class TopAppBarLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1,
) : AppBarLayout(context, attrs, defStyleAttr) {
    private var simpleAppbarBinding: SimpleAppbarLayoutBinding? = null

    init {
        simpleAppbarBinding =
            SimpleAppbarLayoutBinding.inflate(LayoutInflater.from(context), this, true)
        simpleAppbarBinding?.root?.applyInsetter {
            type(navigationBars = true) {
                padding(horizontal = true)
            }
        }
        statusBarForeground = MaterialShapeDrawable.createWithElevationOverlay(context)

        if (!VersionUtils.hasVanillaIceCream()) {
            if (PreferenceUtil.appbarColor) {
                simpleAppbarBinding?.root?.setBackgroundColor(context.surfaceColor())
            } else {
                simpleAppbarBinding?.root?.setBackgroundColor(context.darkAccentColor())
            }
        }

        simpleAppbarBinding?.root?.updateLayoutParams<LayoutParams> {
            scrollFlags = if (PreferenceUtil.disableAppBarScroll) {
                SCROLL_FLAG_NO_SCROLL
            } else {
                (SCROLL_FLAG_SCROLL or SCROLL_FLAG_ENTER_ALWAYS)
            }
        }
    }

    fun pinWhenScrolled() {
        simpleAppbarBinding?.root?.updateLayoutParams<LayoutParams> {
            scrollFlags = SCROLL_FLAG_NO_SCROLL
        }
    }

    val toolbar: MaterialToolbar
        get() = simpleAppbarBinding?.toolbar!!

    var title: CharSequence
        get() = simpleAppbarBinding?.toolbar?.title.toString()
        set(value) {
            simpleAppbarBinding?.toolbar?.title = value

            if (!value.contains("Apex")) {
                simpleAppbarBinding?.toolbar?.setTitleTextColor(context.accentColor())
            }

            simpleAppbarBinding?.toolbar?.let {
                ApexUtil.updateSimpleAppBarTitleTextAppearance(
                    context,
                    it
                )
            }
        }
}