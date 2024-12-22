package com.ttop.app.apex.ui.fragments.playlists

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialSharedAxis
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.ttop.app.apex.R
import com.ttop.app.apex.adapter.song.OrderablePlaylistSongAdapter
import com.ttop.app.apex.databinding.FragmentPlaylistDetailNewBinding
import com.ttop.app.apex.db.PlaylistWithSongs
import com.ttop.app.apex.db.toSongs
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.extensions.elevatedAccentColor
import com.ttop.app.apex.extensions.surfaceColor
import com.ttop.app.apex.glide.ApexGlideExtension.playlistOptions
import com.ttop.app.apex.glide.playlistPreview.PlaylistPreview
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.helper.menu.PlaylistMenuHelper
import com.ttop.app.apex.model.Song
import com.ttop.app.apex.ui.fragments.base.AbsMainActivityFragment
import com.ttop.app.apex.util.MusicUtil
import com.ttop.app.apex.util.PreferenceUtil

import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf


class PlaylistDetailsFragment : AbsMainActivityFragment(R.layout.fragment_playlist_detail_new) {
    private val arguments by navArgs<PlaylistDetailsFragmentArgs>()
    private val viewModel by viewModel<PlaylistDetailsViewModel> {
        parametersOf(arguments.extraPlaylistId)
    }

    private var _binding: FragmentPlaylistDetailNewBinding? = null
    private val binding get() = _binding!!

    private lateinit var playlist: PlaylistWithSongs
    private lateinit var playlistSongAdapter: OrderablePlaylistSongAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform(requireContext(), true).apply {
            drawingViewId = R.id.fragment_container
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(surfaceColor())
            setPathMotion(MaterialArcMotion())
        }

        requireActivity().window.statusBarColor = surfaceColor()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPlaylistDetailNewBinding.bind(view)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).addTarget(view)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
        mainActivity.setSupportActionBar(binding.toolbar)
        binding.toolbar.title = null
//        binding.container.transitionName = playlist.playlistEntity.playlistName

        setUpRecyclerView()

        binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        setupButtons()
        viewModel.getPlaylist().observe(viewLifecycleOwner) { playlistWithSongs ->
            playlist = playlistWithSongs
            Glide.with(this)
                .load(PlaylistPreview(playlistWithSongs))
                .playlistOptions()
                .into(binding.image)
            binding.title.text = playlist.playlistEntity.playlistName
            binding.subtitle.text =
                MusicUtil.getPlaylistInfoString(requireContext(), playlist.songs.toSongs())
            binding.collapsingAppBarLayout.title = playlist.playlistEntity.playlistName
        }
        viewModel.getSongs().observe(viewLifecycleOwner) {
            songs(it.toSongs())
        }
        viewModel.playlistExists().observe(viewLifecycleOwner) {
            if (!it) {
                findNavController().navigateUp()
            }
        }
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        binding.appBarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(requireContext())
    }

    private fun setupButtons() {
        binding.playButton.apply {
            setOnClickListener {
                MusicPlayerRemote.openQueue(playlistSongAdapter.dataSet, 0, true)
            }
            accentColor()
        }
        binding.shuffleButton.apply {
            setOnClickListener {
                MusicPlayerRemote.openAndShuffleQueue(playlistSongAdapter.dataSet, true)
            }
            elevatedAccentColor()
        }
    }

    private fun setUpRecyclerView() {
        playlistSongAdapter = OrderablePlaylistSongAdapter(
            arguments.extraPlaylistId,
            requireActivity(),
            ArrayList(),
            if (PreferenceUtil.isPerformanceMode) {
                R.layout.item_queue_no_image
            }else {
                R.layout.item_queue
            }
        )

        val dragDropManager = RecyclerViewDragDropManager()

        val wrappedAdapter: RecyclerView.Adapter<*> =
            dragDropManager.createWrappedAdapter(playlistSongAdapter)

        binding.recyclerView.apply {
            adapter = wrappedAdapter
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = DraggableItemAnimator()
            dragDropManager.attachRecyclerView(this)
        }
        playlistSongAdapter.registerAdapterDataObserver(object :
            RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                checkIsEmpty()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        requireActivity().window.statusBarColor = surfaceColor()
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_playlist_detail, menu)
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return PlaylistMenuHelper.handleMenuClick(requireActivity(), playlist, item)
    }

    private fun checkIsEmpty() {
        binding.empty.isVisible = playlistSongAdapter.itemCount == 0
        binding.emptyText.isVisible = playlistSongAdapter.itemCount == 0
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onPause() {
        playlistSongAdapter.saveSongs(playlist.playlistEntity)
        super.onPause()
    }

    private fun showEmptyView() {
        binding.empty.isVisible = true
        binding.emptyText.isVisible = true
    }

    fun songs(songs: List<Song>) {
        binding.progressIndicator.hide()
        if (songs.isNotEmpty()) {
            playlistSongAdapter.swapDataSet(songs)
        } else {
            showEmptyView()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}