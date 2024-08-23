package com.ttop.app.apex.ui.fragments.intro

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.ttop.app.apex.R
import com.ttop.app.apex.util.ColorUtil
import com.ttop.app.appintro.SlideBackgroundColorHolder

class LanguageSlideFragment(
    override val defaultBackgroundColorRes: Int
) : Fragment(), SlideBackgroundColorHolder {

    private lateinit var selectLang: MaterialButton

    //Required Constructor
    constructor() : this(com.ttop.app.appthemehelper.R.color.md_green_500)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_intro_language, container, false)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectLang = view.findViewById(R.id.selectLang)

        selectLang.setOnClickListener{
                val intent = Intent(Settings.ACTION_APP_LOCALE_SETTINGS)
                val uri = Uri.fromParts("package", context?.packageName, null)
                intent.data = uri
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context?.startActivity(intent)
            }

        selectLang.backgroundTintList = ColorStateList.valueOf(ColorUtil.getComplimentColor(com.ttop.app.appthemehelper.R.color.md_green_500))
        view.setBackgroundColor(ContextCompat.getColor(requireActivity(), com.ttop.app.appthemehelper.R.color.md_green_500))
    }

    companion object {
        fun newInstance(): LanguageSlideFragment {
            return LanguageSlideFragment(com.ttop.app.appthemehelper.R.color.md_green_500)
        }
    }

    override fun setBackgroundColor(backgroundColor: Int) {
        view?.findViewById<ConstraintLayout>(R.id.main)?.setBackgroundColor(backgroundColor)
    }
}