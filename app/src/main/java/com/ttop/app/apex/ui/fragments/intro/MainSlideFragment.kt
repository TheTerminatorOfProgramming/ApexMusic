package com.ttop.app.apex.ui.fragments.intro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.text.parseAsHtml
import androidx.fragment.app.Fragment
import com.ttop.app.apex.R
import com.ttop.app.appintro.SlideBackgroundColorHolder
import java.util.Locale

class MainSlideFragment(
    override val defaultBackgroundColorRes: Int
) : Fragment(), SlideBackgroundColorHolder {

    private var desc: TextView? = null

    //Required Constructor
    constructor() : this(R.color.bottomSheetColorDark)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_intro_main, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appName = String.format(Locale.getDefault(), getString(R.string.app_name)).parseAsHtml()

        desc = view.findViewById(R.id.description) as TextView

        desc?.text = appName

        view.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.bottomSheetColorDark))
    }
    companion object {
        fun newInstance(): MainSlideFragment {
            return MainSlideFragment(R.color.bottomSheetColorDark)
        }
    }

    override fun setBackgroundColor(backgroundColor: Int) {
        view?.findViewById<ConstraintLayout>(R.id.main)?.setBackgroundColor(backgroundColor)
    }
}