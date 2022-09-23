package com.ttop.app.apex.ui.activities

import android.os.Bundle
import androidx.core.text.parseAsHtml
import com.heinrichreimersoftware.materialintro.app.IntroActivity
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide
import com.ttop.app.apex.R


class AppIntroActivity: IntroActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isButtonCtaVisible = true
        isButtonNextVisible = false
        isButtonBackVisible = false
        buttonCtaTintMode = BUTTON_CTA_TINT_MODE_TEXT
        buttonCtaLabel = "Get Started"

        val appName =
            getString(
                R.string.message_welcome,
                "Apex Music"
            )
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
        addSlide(
            SimpleSlide.Builder()
                .title("Bluetooth Autoplay")
                .description("Bluetooth Autoplay will work only if the app is open or in the recents app list")
                .image(R.drawable.ic_intro_bluetooth)
                .background(R.color.md_deep_purple_500)
                .backgroundDark(R.color.md_deep_purple_A400)
                .layout(R.layout.fragment_simple_slide_large_image)
                .build()
        )
        addSlide(
            SimpleSlide.Builder()
                .title("WhiteList")
                .description("The Whitelist Option will Use any folder that starts with \"Music\" at the start")
                .image(R.drawable.ic_intro_filter_list)
                .background(R.color.md_red_500)
                .backgroundDark(R.color.md_red_A400)
                .layout(R.layout.fragment_simple_slide_large_image)
                .build()
        )
    }
}