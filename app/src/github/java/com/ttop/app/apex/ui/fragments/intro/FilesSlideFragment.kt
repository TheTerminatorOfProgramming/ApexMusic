package com.ttop.app.apex.ui.fragments.intro

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ttop.app.apex.R
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.appintro.SlidePolicy
import com.ttop.app.appthemehelper.util.VersionUtils


class FilesSlideFragment : Fragment() {

    private lateinit var filesBtn: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_files_intro, container, false)

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        filesBtn = view.findViewById(R.id.permission_files) as Button

        filesBtn.setOnClickListener {
            ApexUtil.enableManageAllFiles(requireContext(), requireActivity())
        }

        view.setBackgroundColor(ContextCompat.getColor(requireActivity(), com.ttop.app.appthemehelper.R.color.md_yellow_A400))
    }
    companion object {
        fun newInstance(): FilesSlideFragment {
            return FilesSlideFragment()
        }
    }
}