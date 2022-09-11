package com.ttop.app.apex.ui.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.text.parseAsHtml
import com.heinrichreimersoftware.materialintro.app.IntroActivity
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide
import com.ttop.app.apex.R
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.ui.activities.base.AbsBaseActivity
import com.ttop.app.apex.util.ApexUtil

class AppIntroActivity: IntroActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isButtonCtaVisible = true
        isButtonNextVisible = false
        isButtonBackVisible = false
        buttonCtaTintMode = BUTTON_CTA_TINT_MODE_TEXT
        buttonCtaLabel = "Let's Go"

        buttonCtaClickListener = View.OnClickListener {
            startActivity(Intent(applicationContext, PermissionActivity::class.java))
            finish()
        }

        val appName =
            getString(R.string.message_welcome,
                "Apex Music")
                .parseAsHtml()
        addSlide(
            SimpleSlide.Builder()
                .title(R.string.app_name)
                .description(appName)
                .image(R.drawable.icon_intro)
                .background(R.color.md_blue_500)
                .backgroundDark(R.color.md_blue_A400)
                .layout(R.layout.fragment_simple_slide_large_image)
                .build()
        )
    }
}