package com.ttop.app.apex.libraries.appintro

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.ttop.app.apex.R

abstract class AppIntro2 : AppIntroBase() {

    override val layoutId = R.layout.appintro_intro_layout2

    private lateinit var backgroundFrame: ConstraintLayout
    private lateinit var bottomBar: View
    private lateinit var skipImageButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        backgroundFrame = findViewById(R.id.background)
        bottomBar = findViewById(R.id.bottom)
        skipImageButton = findViewById(R.id.skip)
        if (isRtl) {
            skipImageButton.scaleX = -1F
        }
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        goToLastSlide()
    }
}
