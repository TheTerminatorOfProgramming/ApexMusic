package com.ttop.app.apex.ui.fragments.intro

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import com.ttop.app.apex.R
import com.ttop.app.apex.extensions.installLanguageAndRecreate
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.appintro.SlideBackgroundColorHolder
import org.angmarch.views.NiceSpinner
import org.angmarch.views.OnSpinnerItemSelectedListener
import java.util.LinkedList


class LanguageSlideFragment(
    override val defaultBackgroundColorRes: Int
) : Fragment(), SlideBackgroundColorHolder {

    private lateinit var niceSpinner: NiceSpinner

    //Required Constructor
    constructor() : this(com.ttop.app.appthemehelper.R.color.md_yellow_A400)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_intro_language, container, false)

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

       niceSpinner = view.findViewById(com.ttop.app.apex.R.id.spinner) as NiceSpinner
        val dataset: List<String> = LinkedList(mutableListOf(getString(com.ttop.app.apex.R.string.system_default), getString(com.ttop.app.apex.R.string.bengali),
            getString(com.ttop.app.apex.R.string.english), getString(com.ttop.app.apex.R.string.indonesian), getString(com.ttop.app.apex.R.string.japanese),
            getString(com.ttop.app.apex.R.string.portuguese), getString(com.ttop.app.apex.R.string.spanish)))
        niceSpinner.attachDataSource(dataset)

        when (PreferenceUtil.languageCode) {
            "auto" -> niceSpinner.selectedIndex = 0
            "bn" -> niceSpinner.selectedIndex = 1
            "en" -> niceSpinner.selectedIndex = 2
            "in" -> niceSpinner.selectedIndex = 3
            "ja" -> niceSpinner.selectedIndex = 4
            "pt" -> niceSpinner.selectedIndex = 5
            "es" -> niceSpinner.selectedIndex = 6
        }

        view.setBackgroundColor(ContextCompat.getColor(requireActivity(), com.ttop.app.appthemehelper.R.color.md_yellow_A400))

        niceSpinner.onSpinnerItemSelectedListener = OnSpinnerItemSelectedListener { parent, _, position, _ ->
            val item: String = parent.getItemAtPosition(position) as String
            setLanguage(item)
            }
    }

    private fun setLanguage(item: String) {
        var itemCode = ""
        when (item) {
            "Default" -> itemCode = "auto"
            "Bengali" -> itemCode = "bn"
            "English" -> itemCode = "en"
            "Indonesian" -> itemCode = "in"
            "Japanese" -> itemCode = "ja"
            "Portuguese" -> itemCode = "pt"
            "Spanish" -> itemCode = "es"
        }

        if (itemCode as? String == "auto") {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
        } else {
            // Install the languages from Play Store first and then set the application locale
            requireActivity().installLanguageAndRecreate(itemCode) {
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(
                        itemCode as? String
                    )
                )
            }
        }
        PreferenceUtil.languageCode = itemCode
    }
    companion object {
        fun newInstance(): LanguageSlideFragment {
            return LanguageSlideFragment(com.ttop.app.appthemehelper.R.color.md_yellow_A400)
        }
    }

    override fun setBackgroundColor(backgroundColor: Int) {
        view?.findViewById<ConstraintLayout>(R.id.main)?.setBackgroundColor(backgroundColor)
    }
}