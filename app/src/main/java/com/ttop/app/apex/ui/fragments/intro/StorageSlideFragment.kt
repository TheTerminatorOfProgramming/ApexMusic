package com.ttop.app.apex.ui.fragments.intro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ttop.app.apex.R

class StorageSlideFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_storage_intro, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.md_cyan_400))
    }
    companion object {
        fun newInstance(): StorageSlideFragment {
            return StorageSlideFragment()
        }
    }
}