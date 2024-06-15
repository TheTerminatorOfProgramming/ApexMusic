package com.ttop.app.apex.ui.fragments.intro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    constructor() : this(com.ttop.app.appthemehelper.R.color.md_green_500)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_intro_language, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        niceSpinner = view.findViewById(R.id.spinner)!!
        val dataset: List<String> = LinkedList(mutableListOf(getString(R.string.system_default), getString(R.string.bengali),
            getString(R.string.english), getString(R.string.french), getString(R.string.german), getString(R.string.hindi), getString(R.string.indonesian), getString(R.string.italian), getString(R.string.japanese), getString(R.string.mandarin), getString(R.string.portuguese),
            getString(R.string.punjabi), getString(R.string.spanish), getString(R.string.turkish)))
        niceSpinner.attachDataSource(dataset)

        when (PreferenceUtil.languageCode) {
            "auto" -> niceSpinner.selectedIndex = 0
            "bn" -> niceSpinner.selectedIndex = 1
            "en" -> niceSpinner.selectedIndex = 2
            "fr" -> niceSpinner.selectedIndex = 3
            "de" -> niceSpinner.selectedIndex = 4
            "hi" -> niceSpinner.selectedIndex = 5
            "in" -> niceSpinner.selectedIndex = 6
            "it" -> niceSpinner.selectedIndex = 7
            "ja" -> niceSpinner.selectedIndex = 8
            "zh" -> niceSpinner.selectedIndex = 9
            "pt" -> niceSpinner.selectedIndex = 10
            "pa" -> niceSpinner.selectedIndex = 11
            "es" -> niceSpinner.selectedIndex = 12
            "tr" -> niceSpinner.selectedIndex = 13
        }

        view.setBackgroundColor(ContextCompat.getColor(requireActivity(), com.ttop.app.appthemehelper.R.color.md_green_500))

        niceSpinner.onSpinnerItemSelectedListener = OnSpinnerItemSelectedListener { parent, _, position, _ ->
            val item: String = parent.getItemAtPosition(position) as String
            setLanguage(item)
            }
    }

    private fun setLanguage(item: String) {
        var itemCode = ""
        when (item) {
            getString(R.string.system_default) -> itemCode = "auto"
            getString(R.string.bengali) -> itemCode = "bn"
            getString(R.string.english) -> itemCode = "en"
            getString(R.string.english) -> itemCode = "fr"
            getString(R.string.german) -> itemCode = "de"
            getString(R.string.hindi) -> itemCode = "hi"
            getString(R.string.indonesian) -> itemCode = "in"
            getString(R.string.italian) -> itemCode = "it"
            getString(R.string.japanese) -> itemCode = "ja"
            getString(R.string.mandarin) -> itemCode = "zh"
            getString(R.string.portuguese) -> itemCode = "pt"
            getString(R.string.punjabi) -> itemCode = "pa"
            getString(R.string.spanish) -> itemCode = "es"
            getString(R.string.turkish) -> itemCode = "tr"
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
            return LanguageSlideFragment(com.ttop.app.appthemehelper.R.color.md_green_500)
        }
    }

    override fun setBackgroundColor(backgroundColor: Int) {
        view?.findViewById<ConstraintLayout>(R.id.main)?.setBackgroundColor(backgroundColor)
    }
}