/*
 * Copyright (c) 2020 Hemanth Savarla.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 */
package com.ttop.app.apex.ui.fragments.other

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.doOnPreDraw
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialContainerTransform
import com.ttop.app.apex.Constants.USER_BANNER
import com.ttop.app.apex.Constants.USER_PROFILE
import com.ttop.app.apex.R
import com.ttop.app.apex.databinding.FragmentUserInfoBinding
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.extensions.applyToolbar
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.glide.ApexGlideExtension
import com.ttop.app.apex.glide.ApexGlideExtension.profileBannerOptions
import com.ttop.app.apex.glide.ApexGlideExtension.userProfileOptions
import com.ttop.app.apex.ui.fragments.LibraryViewModel
import com.ttop.app.apex.util.ImageUtil
import com.ttop.app.apex.util.PreferenceUtil.userName
import com.ttop.app.appthemehelper.util.VersionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.io.File

class UserInfoFragment : Fragment() {

    private var _binding: FragmentUserInfoBinding? = null
    private val binding get() = _binding!!
    private val libraryViewModel: LibraryViewModel by activityViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.fragment_container
            duration = 300L
            scrimColor = Color.TRANSPARENT
        }
        _binding = FragmentUserInfoBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyToolbar(binding.toolbar)

        binding.nameContainer.accentColor()
        binding.next.accentColor()
        binding.name.setText(userName)

        binding.userImage.setOnClickListener {
            showUserImageOptions()
        }

        binding.bannerImage.setOnClickListener {
            showBannerImageOptions()
        }

        binding.next.setOnClickListener {
            val nameString = binding.name.text.toString().trim { it <= ' ' }
            if (nameString.isEmpty()) {
                showToast(R.string.error_empty_name)
                return@setOnClickListener
            }
            userName = nameString
            findNavController().navigateUp()
        }

        loadProfile()
        postponeEnterTransition()
        view.doOnPreDraw {
            startPostponedEnterTransition()
        }
        libraryViewModel.getFabMargin().observe(viewLifecycleOwner) {
            binding.next.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = it
            }
        }
    }

    private fun showBannerImageOptions() {
        val list = requireContext().resources.getStringArray(R.array.image_settings_options)
        MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.banner_image)
            .setItems(list) { _, which ->
                when (which) {
                    0 -> selectBannerImage()
                    1 -> {
                        val appDir = requireContext().filesDir
                        val file = File(appDir, USER_BANNER)
                        file.delete()
                        loadProfile()
                    }
                }
            }
            .setNegativeButton(R.string.action_cancel, null)
            .create()
            .show()
    }

    private fun showUserImageOptions() {
        val list = requireContext().resources.getStringArray(R.array.image_settings_options)
        MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.profile_image)
            .setItems(list) { _, which ->
                when (which) {
                    0 -> pickNewPhoto()
                    1 -> {
                        val appDir = requireContext().filesDir
                        val file = File(appDir, USER_PROFILE)
                        file.delete()
                        loadProfile()
                    }
                }
            }
            .setNegativeButton(R.string.action_cancel, null)
            .create()
            .show()
    }

    private fun loadProfile() {
        binding.bannerImage.let {
            Glide.with(this)
                .load(ApexGlideExtension.getBannerModel())
                .profileBannerOptions(ApexGlideExtension.getBannerModel())
                .into(it)
        }
        Glide.with(this)
            .load(ApexGlideExtension.getUserModel())
            .userProfileOptions(ApexGlideExtension.getUserModel(), requireContext())
            .into(binding.userImage)
    }

    private fun selectBannerImage() {
        ImagePicker.with(this)
            .compress(1440)
            .provider(ImageProvider.GALLERY)
            .crop(16f, 9f)
            .createIntent {
                startForBannerImageResult.launch(it)
            }
    }

    private fun pickNewPhoto() {
        ImagePicker.with(this)
            .provider(ImageProvider.GALLERY)
            .cropSquare()
            .compress(1440)
            .createIntent {
                startForProfileImageResult.launch(it)
            }
    }

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            saveImage(result) { fileUri ->
                setAndSaveUserImage(fileUri)
            }
        }

    private val startForBannerImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            saveImage(result) { fileUri ->
                setAndSaveBannerImage(fileUri)
            }
        }

    private fun saveImage(result: ActivityResult, doIfResultOk: (uri: Uri) -> Unit) {
        val resultCode = result.resultCode
        val data = result.data
        when (resultCode) {
            Activity.RESULT_OK -> {
                data?.data?.let { uri ->
                    doIfResultOk(uri)
                }
            }
            ImagePicker.RESULT_ERROR -> {
                showToast(ImagePicker.getError(data))
            }
            else -> {
                showToast(R.string.task_cancelled)
            }
        }
    }

    private fun setAndSaveBannerImage(fileUri: Uri) {
        Glide.with(this)
            .asBitmap()
            .load(fileUri)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .listener(object : RequestListener<Bitmap> {
                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean,
                ): Boolean {
                    resource?.let { saveImage(it, USER_BANNER) }
                    return false
                }

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean,
                ): Boolean {
                    return false
                }
            })
            .into(binding.bannerImage)
    }

    private fun saveImage(bitmap: Bitmap, fileName: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val appDir = requireContext().filesDir
            val file = File(appDir, fileName)
            var successful: Boolean
            if (VersionUtils.hasR()) {
                file.outputStream().buffered().use {
                    successful = ImageUtil.resizeBitmap(bitmap, 2048)
                        .compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 100, it)
                }
            }else {
                file.outputStream().buffered().use {
                    successful = ImageUtil.resizeBitmap(bitmap, 2048)
                        .compress(Bitmap.CompressFormat.WEBP, 100, it)
                }
            }

            if (successful) {
                withContext(Dispatchers.Main) {
                    showToast(R.string.message_updated)
                }
            }
        }
    }

    private fun setAndSaveUserImage(fileUri: Uri) {
        Glide.with(this)
            .asBitmap()
            .load(fileUri)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .listener(object : RequestListener<Bitmap> {
                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean,
                ): Boolean {
                    resource?.let { saveImage(it, USER_PROFILE) }
                    return false
                }

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean,
                ): Boolean {
                    return false
                }
            })
            .into(binding.userImage)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
