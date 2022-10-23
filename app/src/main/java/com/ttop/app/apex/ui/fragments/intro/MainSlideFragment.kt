package com.ttop.app.apex.ui.fragments.intro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.text.parseAsHtml
import androidx.fragment.app.Fragment
import com.github.appintro.SlidePolicy
import com.ttop.app.apex.R
import com.ttop.app.apex.extensions.showToast

class MainSlideFragment : Fragment() {

    private var desc: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_main_intro, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appName =
            getString(
                R.string.message_welcome,
                "Apex Music"
            ).parseAsHtml()

        desc = view.findViewById(R.id.description) as TextView

        desc?.text = "Hello there! Welcome to Apex Music"

        view.setBackgroundColor(resources.getColor(R.color.md_red_400))
    }
    companion object {
        fun newInstance(fragmentRingtoneIntro: Int): MainSlideFragment {
            return MainSlideFragment()
        }
    }
}