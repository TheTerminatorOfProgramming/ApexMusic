package com.ttop.app.apex.ui.fragments.home

import android.os.Bundle
import android.os.Environment
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat.SRC_IN
import androidx.core.os.bundleOf
import androidx.core.text.toSpannable
import androidx.core.view.doOnLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import com.ttop.app.apex.EXTRA_PLAYLIST_TYPE
import com.ttop.app.apex.HISTORY_PLAYLIST
import com.ttop.app.apex.LAST_ADDED_PLAYLIST
import com.ttop.app.apex.R
import com.ttop.app.apex.TOP_PLAYED_PLAYLIST
import com.ttop.app.apex.adapter.HomeAdapter
import com.ttop.app.apex.databinding.FragmentHomeBinding
import com.ttop.app.apex.dialogs.CreatePlaylistDialog
import com.ttop.app.apex.dialogs.ImportPlaylistDialog
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.extensions.darkAccentColor
import com.ttop.app.apex.extensions.elevatedAccentColor
import com.ttop.app.apex.extensions.getDrawableCompat
import com.ttop.app.apex.extensions.setUpMediaRouteButton
import com.ttop.app.apex.glide.ApexGlideExtension
import com.ttop.app.apex.glide.ApexGlideExtension.songCoverOptions
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.interfaces.IScrollHelper
import com.ttop.app.apex.libraries.appthemehelper.common.ATHToolbarActivity
import com.ttop.app.apex.libraries.appthemehelper.util.ATHColorUtil
import com.ttop.app.apex.libraries.appthemehelper.util.ToolbarContentTintHelper
import com.ttop.app.apex.model.Song
import com.ttop.app.apex.ui.fragments.ReloadType
import com.ttop.app.apex.ui.fragments.base.AbsMainActivityFragment
import com.ttop.app.apex.ui.fragments.folder.FoldersFragment.Companion.AUDIO_FILE_FILTER
import com.ttop.app.apex.util.ApexStaticUtil
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.MusicUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.getExternalStoragePublicDirectory
import kotlinx.coroutines.launch
import java.io.File

class HomeFragment :
    AbsMainActivityFragment(R.layout.fragment_home), IScrollHelper {

    private var _binding: HomeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val homeBinding = FragmentHomeBinding.bind(view)
        _binding = HomeBinding(homeBinding)
        mainActivity.setSupportActionBar(binding.toolbar)
        mainActivity.supportActionBar?.title = null
        setupListeners()

        enterTransition = MaterialFadeThrough().addTarget(binding.contentContainer)
        reenterTransition = MaterialFadeThrough().addTarget(binding.contentContainer)

        checkForMargins()

        val homeAdapter = HomeAdapter(mainActivity)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(mainActivity)
            adapter = homeAdapter
        }
        libraryViewModel.getSuggestions().observe(viewLifecycleOwner) {
            loadSuggestions(it)
        }
        libraryViewModel.getHome().observe(viewLifecycleOwner) {
            homeAdapter.swapData(it)
        }

        setupTitle()
        colorButtons()
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        view.doOnLayout {
            adjustPlaylistButtons()
        }
        activity?.window?.statusBarColor = requireActivity().darkAccentColor()
    }

    private fun adjustPlaylistButtons() {
        val buttons =
            listOf(binding.history, binding.lastAdded, binding.topPlayed, binding.actionShuffle)
        buttons.maxOf { it.lineCount }.let { maxLineCount ->
            buttons.forEach { button ->
                // Set the highest line count to every button for consistency
                button.setLines(maxLineCount)
            }
        }
    }

    private fun setupListeners() {

        binding.lastAdded.setOnClickListener {
            findNavController().navigate(
                R.id.detailListFragment,
                bundleOf(EXTRA_PLAYLIST_TYPE to LAST_ADDED_PLAYLIST)
            )
            setSharedAxisYTransitions()
        }

        binding.topPlayed.setOnClickListener {
            findNavController().navigate(
                R.id.detailListFragment,
                bundleOf(EXTRA_PLAYLIST_TYPE to TOP_PLAYED_PLAYLIST)
            )
            setSharedAxisYTransitions()
        }

        binding.actionShuffle.setOnClickListener {
            libraryViewModel.shuffleSongs()
        }

        binding.history.setOnClickListener {
            findNavController().navigate(
                R.id.detailListFragment,
                bundleOf(EXTRA_PLAYLIST_TYPE to HISTORY_PLAYLIST)
            )
            setSharedAxisYTransitions()
        }

        // Reload suggestions
        binding.suggestions.refreshButton.setOnClickListener {
            libraryViewModel.forceReload(
                ReloadType.Suggestions
            )
        }
    }

    private fun setupTitle() {
        binding.toolbar.navigationIcon = if (PreferenceUtil.isVoiceSearch) {
            getDrawableCompat(R.drawable.ic_voice)
        } else {
            getDrawableCompat(R.drawable.ic_search)
        }
        binding.toolbar.setNavigationOnClickListener {
            PreferenceUtil.isSearchFromNavigation = true
            findNavController().navigate(R.id.action_search, null, navOptions)
        }
        val builder = SpannableStringBuilder()

        val title = "Apex".toSpannable()
        val title2 = "Music"

        title.setSpan(ForegroundColorSpan(accentColor()), 0, title.length, 0)

        builder.append(title).append(" ").append(title2)

        binding.appBarLayout.title = builder

        binding.toolbar.navigationIcon?.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                requireContext().accentColor(),
                SRC_IN
            )
    }

    private fun colorButtons() {
        binding.history.elevatedAccentColor()
        binding.lastAdded.elevatedAccentColor()
        binding.topPlayed.elevatedAccentColor()
        binding.actionShuffle.elevatedAccentColor()
    }

    private fun checkForMargins() {
        binding.recyclerView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            bottomMargin = if (ApexUtil.isTablet) {
                if (MusicPlayerRemote.playingQueue.isNotEmpty()) {
                    ApexUtil.dpToMargin(64)
                } else {
                    ApexUtil.dpToMargin(0)
                }
            } else {
                if (MusicPlayerRemote.playingQueue.isNotEmpty()) {
                    ApexUtil.dpToMargin(144)
                } else {
                    ApexUtil.dpToMargin(80)
                }
            }
        }
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        menu.removeItem(R.id.action_grid_size)
        menu.removeItem(R.id.action_layout_type)
        menu.removeItem(R.id.action_sort_order)
        ToolbarContentTintHelper.handleOnCreateOptionsMenu(
            requireContext(),
            binding.toolbar,
            menu,
            ATHToolbarActivity.getToolbarBackgroundColor(binding.toolbar)
        )
        //Setting up cast button
        requireContext().setUpMediaRouteButton(menu)

        if (!ApexUtil.isTablet) {
            menu.removeItem(R.id.action_refresh)
        }

        val yourdrawable = menu.findItem(R.id.action_scan_media).icon
        yourdrawable!!.mutate()
        yourdrawable.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            requireContext().accentColor(),
            SRC_IN
        )
    }

    override fun scrollToTop() {
        binding.container.scrollTo(0, 0)
        binding.appBarLayout.setExpanded(true)
    }

    fun setSharedAxisXTransitions() {
        exitTransition =
            MaterialSharedAxis(MaterialSharedAxis.X, true).addTarget(CoordinatorLayout::class.java)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    private fun setSharedAxisYTransitions() {
        exitTransition =
            MaterialSharedAxis(MaterialSharedAxis.Y, true).addTarget(CoordinatorLayout::class.java)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
    }

    private fun loadSuggestions(songs: List<Song>) {
        if (!PreferenceUtil.homeSuggestions || songs.isEmpty()) {
            binding.suggestions.root.isVisible = false
            return
        }
        val images = listOf(
            binding.suggestions.image1,
            binding.suggestions.image2,
            binding.suggestions.image3,
            binding.suggestions.image4,
            binding.suggestions.image5,
            binding.suggestions.image6,
            binding.suggestions.image7,
            binding.suggestions.image8
        )
        val color = accentColor()
        binding.suggestions.message.apply {
            setTextColor(color)
            setOnClickListener {
                it.isClickable = false
                it.postDelayed({ it.isClickable = true }, 500)
                if (MusicPlayerRemote.isPlaying) {
                    MusicPlayerRemote.clearQueue()
                }

                val song = MusicUtil.repository.allSong()

                MusicPlayerRemote.openAndShuffleQueue(song, false)
                MusicPlayerRemote.playNext(songs.subList(0, 8), false)
                MusicPlayerRemote.moveSong(0, 9)
                MusicPlayerRemote.playSongAt(0)
            }
        }
        binding.suggestions.card6.setCardBackgroundColor(ATHColorUtil.withAlpha(color, 0.12f))
        images.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                it.isClickable = false
                it.postDelayed({ it.isClickable = true }, 500)
                if (MusicPlayerRemote.isPlaying) {
                    MusicPlayerRemote.clearQueue()
                }

                val song = MusicUtil.repository.allSong()

                MusicPlayerRemote.openAndShuffleQueue(song, false)
                MusicPlayerRemote.playNext(songs[index], false)
                MusicPlayerRemote.moveSong(1, 0)
                MusicPlayerRemote.playSongAt(0)
            }
            Glide.with(this)
                .load(ApexGlideExtension.getSongModel(songs[index]))
                .songCoverOptions(songs[index])
                .into(imageView)
        }
    }

    companion object {

        const val TAG: String = "BannerHomeFragment"

        @JvmStatic
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
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

            R.id.action_refresh -> {
                activity?.recreate()
            }

            R.id.action_scan_media -> {
                lifecycleScope.launch {
                    val file =
                        File(getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).canonicalPath)
                    ApexStaticUtil.listPaths(
                        file,
                        AUDIO_FILE_FILTER
                    ) { paths -> ApexStaticUtil.scanPaths(requireActivity(), paths) }
                }
            }
        }
        return false
    }

    override fun onPrepareMenu(menu: Menu) {
        super.onPrepareMenu(menu)
        //ToolbarContentTintHelper.setToolbarContentColor(requireActivity(), binding.toolbar, binding.toolbar.menu, accentColor(), accentColor(), accentColor(), accentColor())
        binding.toolbar.overflowIcon?.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                requireContext().accentColor(),
                SRC_IN
            )
    }

    override fun onResume() {
        super.onResume()
        checkForMargins()
        libraryViewModel.forceReload(ReloadType.HomeSections)
        exitTransition = null
    }

    override fun onQueueChanged() {
        super.onQueueChanged()
        checkForMargins()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
