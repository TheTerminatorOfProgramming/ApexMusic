package com.ttop.app.apex.views

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.core.view.updateLayoutParams
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.shape.MaterialShapeDrawable
import com.ttop.app.apex.databinding.CollapsingAppbarLayoutBinding
import com.ttop.app.apex.databinding.SimpleAppbarLayoutBinding
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.extensions.darkAccentColor
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.ApexUtil.updateCollapsableAppBarTitleTextAppearance
import com.ttop.app.apex.util.PreferenceUtil
import dev.chrisbanes.insetter.applyInsetter


class TopAppBarLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1,
) : AppBarLayout(context, attrs, defStyleAttr) {
    private var simpleAppbarBinding: SimpleAppbarLayoutBinding? = null
    private var collapsingAppbarBinding: CollapsingAppbarLayoutBinding? = null

    val mode: AppBarMode = when (PreferenceUtil.appBarMode) {
        "simple", "simple_no_scroll" -> AppBarMode.SIMPLE
        "expanded", "expanded_no_scroll" -> AppBarMode.COLLAPSING
        else -> AppBarMode.SIMPLE
    }

    init {
        if (mode == AppBarMode.COLLAPSING) {
            collapsingAppbarBinding =
                CollapsingAppbarLayoutBinding.inflate(LayoutInflater.from(context), this, true)
            val isLandscape =
                context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            if (isLandscape) {
                fitsSystemWindows = false
            }

            if (PreferenceUtil.appBarMode == "expanded_no_scroll") {
                collapsingAppbarBinding?.root?.updateLayoutParams<LayoutParams> {
                    scrollFlags = SCROLL_FLAG_NO_SCROLL
                }
            }else  if (PreferenceUtil.appBarMode == "expanded") {
                collapsingAppbarBinding?.root?.updateLayoutParams<LayoutParams> {
                    scrollFlags = (SCROLL_FLAG_SCROLL or SCROLL_FLAG_EXIT_UNTIL_COLLAPSED or SCROLL_FLAG_SNAP or SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED)
                }
            }
        } else {
            simpleAppbarBinding =
                SimpleAppbarLayoutBinding.inflate(LayoutInflater.from(context), this, true)
            simpleAppbarBinding?.root?.applyInsetter {
                type(navigationBars = true) {
                    padding(horizontal = true)
                }
            }
            statusBarForeground = MaterialShapeDrawable.createWithElevationOverlay(context)

            if (PreferenceUtil.appBarMode == "simple_no_scroll") {
                simpleAppbarBinding?.root?.updateLayoutParams<LayoutParams> {
                    scrollFlags = SCROLL_FLAG_NO_SCROLL
                }
            }else  if (PreferenceUtil.appBarMode == "simple") {
                simpleAppbarBinding?.root?.updateLayoutParams<LayoutParams> {
                    scrollFlags = (SCROLL_FLAG_SCROLL or SCROLL_FLAG_ENTER_ALWAYS)
                }
            }
        }
    }

    fun pinWhenScrolled() {
        simpleAppbarBinding?.root?.updateLayoutParams<LayoutParams> {
            scrollFlags = SCROLL_FLAG_NO_SCROLL
        }
    }

    val toolbar: MaterialToolbar
        get() = if (mode == AppBarMode.COLLAPSING) {
            collapsingAppbarBinding?.toolbar!!
        } else {
            simpleAppbarBinding?.toolbar!!
        }

    var title: CharSequence
        get() = if (mode == AppBarMode.COLLAPSING) {
            collapsingAppbarBinding?.collapsingToolbarLayout?.title.toString()
        } else {
            simpleAppbarBinding?.toolbar?.title.toString()
        }
        set(value) {
            if (mode == AppBarMode.COLLAPSING) {
                collapsingAppbarBinding?.collapsingToolbarLayout?.title = value
                if (!value.contains("Apex")) {
                    collapsingAppbarBinding?.collapsingToolbarLayout?.setCollapsedTitleTextColor(
                        context.accentColor()
                    )
                    collapsingAppbarBinding?.collapsingToolbarLayout?.setExpandedTitleColor(
                        context.accentColor()
                    )
                }

                collapsingAppbarBinding?.collapsingToolbarLayout?.let {
                    updateCollapsableAppBarTitleTextAppearance(
                        it
                    )
                }
                collapsingAppbarBinding?.collapsingToolbarLayout?.setContentScrimColor(context.darkAccentColor())
            } else {
                simpleAppbarBinding?.toolbar?.title = value

                if (!value.contains("Apex")) {
                    simpleAppbarBinding?.toolbar?.setTitleTextColor(context.accentColor())
                }

                simpleAppbarBinding?.toolbar?.let {
                    ApexUtil.updateSimpleAppBarTitleTextAppearance(context,
                        it
                    )
                }
            }
        }

    enum class AppBarMode {
        COLLAPSING,
        SIMPLE
    }
}