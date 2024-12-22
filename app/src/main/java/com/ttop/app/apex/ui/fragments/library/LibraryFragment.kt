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
package com.ttop.app.apex.ui.fragments.library

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat.SRC_IN
import androidx.core.text.parseAsHtml
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.ttop.app.apex.R
import com.ttop.app.apex.databinding.FragmentLibraryBinding
import com.ttop.app.apex.dialogs.CreatePlaylistDialog
import com.ttop.app.apex.dialogs.ImportPlaylistDialog
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.extensions.getDrawableCompat
import com.ttop.app.apex.extensions.setUpMediaRouteButton
import com.ttop.app.apex.extensions.whichFragment
import com.ttop.app.apex.libraries.appthemehelper.ThemeStore
import com.ttop.app.apex.libraries.appthemehelper.common.ATHToolbarActivity.getToolbarBackgroundColor
import com.ttop.app.apex.libraries.appthemehelper.util.ToolbarContentTintHelper
import com.ttop.app.apex.model.CategoryInfo
import com.ttop.app.apex.ui.fragments.base.AbsMainActivityFragment
import com.ttop.app.apex.util.PreferenceUtil

class LibraryFragment : AbsMainActivityFragment(R.layout.fragment_library) {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLibraryBinding.bind(view)

        mainActivity.setBottomNavVisibility(true)
        mainActivity.setSupportActionBar(binding.toolbar)
        mainActivity.supportActionBar?.title = null
        binding.toolbar.navigationIcon = if (PreferenceUtil.isVoiceSearch) {
            getDrawableCompat(R.drawable.ic_voice)
        } else {
            getDrawableCompat(R.drawable.ic_search)
        }

        binding.toolbar.setNavigationOnClickListener {
            PreferenceUtil.isSearchFromNavigation = true
            findNavController().navigate(R.id.action_search, null, navOptions)
        }

        binding.toolbar.navigationIcon?.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                requireContext().accentColor(),
                SRC_IN
            )

        setupNavigationController()
        setupTitle()
    }

    private fun setupTitle() {
        /*val color = ThemeStore.accentColor(requireContext())
        val hexColor = String.format("#%06X", 0xFFFFFF and color)
        val appName = "Apex <span  style='color:$hexColor';>Music</span>".parseAsHtml()*/
        binding.appNameText.text = ContextCompat.getString(requireContext(), R.string.app_name)
        binding.appNameText.setTextColor(accentColor())
    }

    private fun setupNavigationController() {
        val navHostFragment = whichFragment<NavHostFragment>(R.id.fragment_container)
        val navController = navHostFragment.navController
        val navInflater = navController.navInflater
        val navGraph = navInflater.inflate(R.navigation.library_graph)

        val categoryInfo: CategoryInfo = PreferenceUtil.libraryCategory.first { it.visible }
        if (categoryInfo.visible) {
            navGraph.setStartDestination(categoryInfo.category.id)
        }
        navController.graph = navGraph
        NavigationUI.setupWithNavController(mainActivity.navigationView, navController)
        navController.addOnDestinationChangedListener { _, _, _ ->
            binding.appBarLayout.setExpanded(true, true)
        }
    }

    override fun onPrepareMenu(menu: Menu) {
        //ToolbarContentTintHelper.handleOnPrepareOptionsMenu(requireActivity(), binding.toolbar, accentColor())
        binding.toolbar.overflowIcon?.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                requireContext().accentColor(),
                SRC_IN
            )
        val yourdrawable = menu.findItem(R.id.action_scan).icon // change 0 with 1,2 ...
        yourdrawable!!.mutate()
        yourdrawable.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            requireContext().accentColor(),
            SRC_IN
        )
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        ToolbarContentTintHelper.handleOnCreateOptionsMenu(
            requireContext(),
            binding.toolbar,
            menu,
            getToolbarBackgroundColor(binding.toolbar)
        )
        //Setting up cast button
        requireContext().setUpMediaRouteButton(menu)

        val yourdrawable = menu.findItem(R.id.action_scan).icon // change 0 with 1,2 ...
        yourdrawable!!.mutate()
        yourdrawable.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            requireContext().accentColor(),
            SRC_IN
        )
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_import_playlist -> ImportPlaylistDialog().show(
                childFragmentManager,
                "ImportPlaylist"
            )

            R.id.action_add_to_playlist -> CreatePlaylistDialog.create(emptyList()).show(
                childFragmentManager,
                "ShowCreatePlaylistDialog"
            )
        }
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
