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
package com.ttop.app.apex.ui.fragments.albums

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.text.parseAsHtml
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.Fade
import com.bumptech.glide.Glide
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform
import com.ttop.app.apex.EXTRA_ALBUM_ID
import com.ttop.app.apex.EXTRA_ARTIST_ID
import com.ttop.app.apex.EXTRA_ARTIST_NAME
import com.ttop.app.apex.R
import com.ttop.app.apex.adapter.album.HorizontalAlbumAdapter
import com.ttop.app.apex.adapter.song.SimpleSongAdapter
import com.ttop.app.apex.databinding.FragmentAlbumDetailsBinding
import com.ttop.app.apex.dialogs.AddToPlaylistDialog
import com.ttop.app.apex.dialogs.DeleteSongsDialog
import com.ttop.app.apex.extensions.applyColor
import com.ttop.app.apex.extensions.applyOutlineColor
import com.ttop.app.apex.extensions.findActivityNavController
import com.ttop.app.apex.extensions.show
import com.ttop.app.apex.extensions.surfaceColor
import com.ttop.app.apex.glide.ApexGlideExtension
import com.ttop.app.apex.glide.ApexGlideExtension.albumCoverOptions
import com.ttop.app.apex.glide.ApexGlideExtension.artistImageOptions
import com.ttop.app.apex.glide.ApexGlideExtension.asBitmapPalette
import com.ttop.app.apex.glide.SingleColorTarget
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.helper.SortOrder.AlbumSongSortOrder.Companion.SONG_A_Z
import com.ttop.app.apex.helper.SortOrder.AlbumSongSortOrder.Companion.SONG_DURATION
import com.ttop.app.apex.helper.SortOrder.AlbumSongSortOrder.Companion.SONG_TRACK_LIST
import com.ttop.app.apex.helper.SortOrder.AlbumSongSortOrder.Companion.SONG_Z_A
import com.ttop.app.apex.interfaces.IAlbumClickListener
import com.ttop.app.apex.libraries.appthemehelper.common.ATHToolbarActivity.getToolbarBackgroundColor
import com.ttop.app.apex.libraries.appthemehelper.util.ToolbarContentTintHelper
import com.ttop.app.apex.model.Album
import com.ttop.app.apex.model.Artist
import com.ttop.app.apex.network.Result
import com.ttop.app.apex.network.model.LastFmAlbum
import com.ttop.app.apex.repository.RealRepository
import com.ttop.app.apex.ui.activities.tageditor.AbsTagEditorActivity
import com.ttop.app.apex.ui.activities.tageditor.AlbumTagEditorActivity
import com.ttop.app.apex.ui.fragments.base.AbsMainActivityFragment
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.MusicUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.logD
import com.ttop.app.apex.util.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.text.Collator

class AlbumDetailsFragment : AbsMainActivityFragment(R.layout.fragment_album_details),
    IAlbumClickListener {

    private var _binding: FragmentAlbumDetailsBinding? = null
    private val binding get() = _binding!!

    private val arguments by navArgs<AlbumDetailsFragmentArgs>()
    private val detailsViewModel by viewModel<AlbumDetailsViewModel> {
        parametersOf(arguments.extraAlbumId)
    }

    private lateinit var simpleSongAdapter: SimpleSongAdapter
    private lateinit var album: Album
    private var albumArtistExists = false

    private val savedSortOrder: String
        get() = PreferenceUtil.albumDetailSongSortOrder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.fragment_container
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(surfaceColor())
            setPathMotion(MaterialArcMotion())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAlbumDetailsBinding.bind(view)
        enterTransition = Fade()
        exitTransition = Fade()
        mainActivity.addMusicServiceEventListener(detailsViewModel)
        mainActivity.setSupportActionBar(binding.toolbar)

        binding.toolbar.title = " "
        binding.albumCoverContainer.transitionName = arguments.extraAlbumId.toString()
        postponeEnterTransition()
        detailsViewModel.getAlbum().observe(viewLifecycleOwner) {
            view.doOnPreDraw {
                startPostponedEnterTransition()
            }
            albumArtistExists = !it.albumArtist.isNullOrEmpty()
            showAlbum(it)
            binding.artistImage.transitionName = if (albumArtistExists) {
                album.albumArtist
            } else {
                album.artistId.toString()
            }
        }

        setupRecyclerView()
        binding.artistImage.setOnClickListener { artistView ->
            if (albumArtistExists) {
                findActivityNavController(R.id.fragment_container)
                    .navigate(
                        R.id.albumArtistDetailsFragment,
                        bundleOf(EXTRA_ARTIST_NAME to album.albumArtist),
                        null,
                        FragmentNavigatorExtras(artistView to album.albumArtist.toString())
                    )
            } else {
                findActivityNavController(R.id.fragment_container)
                    .navigate(
                        R.id.artistDetailsFragment,
                        bundleOf(EXTRA_ARTIST_ID to album.artistId),
                        null,
                        FragmentNavigatorExtras(artistView to album.artistId.toString())
                    )
            }

        }
        binding.fragmentAlbumContent.playAction.setOnClickListener {
            MusicPlayerRemote.openQueue(album.songs, 0, true)
            if (PreferenceUtil.isExpandPanel == "default_song" || PreferenceUtil.isExpandPanel == "enhanced_song" ) {
                mainActivity.expandPanel()
            }
        }
        binding.fragmentAlbumContent.shuffleAction.setOnClickListener {
            MusicPlayerRemote.openAndShuffleQueue(
                album.songs,
                true
            )
            if (PreferenceUtil.isExpandPanel == "default_song" || PreferenceUtil.isExpandPanel == "enhanced_song" ) {
                mainActivity.expandPanel()
            }
        }

        binding.fragmentAlbumContent.aboutAlbumText.setOnClickListener {
            if (binding.fragmentAlbumContent.aboutAlbumText.maxLines == 4) {
                binding.fragmentAlbumContent.aboutAlbumText.maxLines = Integer.MAX_VALUE
            } else {
                binding.fragmentAlbumContent.aboutAlbumText.maxLines = 4
            }
        }

        binding.appBarLayout?.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(requireContext())
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceActivity?.removeMusicServiceEventListener(detailsViewModel)
    }

    private fun setupRecyclerView() {
        simpleSongAdapter = SimpleSongAdapter(
            requireActivity() as AppCompatActivity,
            ArrayList(),
            R.layout.item_song
        )
        binding.fragmentAlbumContent.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = DefaultItemAnimator()
            isNestedScrollingEnabled = false
            adapter = simpleSongAdapter
        }
    }

    private fun showAlbum(album: Album) {
        if (album.songs.isEmpty()) {
            findNavController().navigateUp()
            return
        }
        this.album = album

        binding.albumTitle.text = album.title
        val songText = resources.getQuantityString(
            R.plurals.albumSongs,
            album.songCount,
            album.songCount
        )
        binding.fragmentAlbumContent.songTitle.text = songText


        val albumTracksNumber: StringBuilder = StringBuilder()
        albumTracksNumber.append(album.songCount).append(" ").append(getString(R.string.songs))

        if (MusicUtil.getYearString(album.year) == "-") {
            binding.albumText.text = String.format(
                "%s • %s • %s",
                if (albumArtistExists) album.albumArtist else album.artistName,
                MusicUtil.getReadableDurationString(MusicUtil.getTotalDuration(album.songs)),
                albumTracksNumber
            )
        } else {
            binding.albumText.text = String.format(
                "%s • %s • %s • %s",
                album.artistName,
                MusicUtil.getYearString(album.year),
                MusicUtil.getReadableDurationString(MusicUtil.getTotalDuration(album.songs)),
                albumTracksNumber
            )
        }
        loadAlbumCover(album)
        simpleSongAdapter.swapDataSet(album.songs)
        if (albumArtistExists) {
            detailsViewModel.getAlbumArtist(album.albumArtist.toString())
                .observe(viewLifecycleOwner) {
                    loadArtistImage(it)
                }
        } else {
            detailsViewModel.getArtist(album.artistId).observe(viewLifecycleOwner) {
                loadArtistImage(it)
            }
        }


        detailsViewModel.getAlbumInfo(album).observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    logD("Loading")
                }

                is Result.Error -> {
                    logE("Error")
                }

                is Result.Success -> {
                    aboutAlbum(result.data)
                }
            }
        }
    }

    private fun moreAlbums(albums: List<Album>) {
        binding.fragmentAlbumContent.moreTitle.show()
        binding.fragmentAlbumContent.moreRecyclerView.show()
        binding.fragmentAlbumContent.moreTitle.text =
            String.format(getString(R.string.label_more_from), album.artistName)

        val albumAdapter =
            HorizontalAlbumAdapter(requireActivity() as AppCompatActivity, albums, this)
        binding.fragmentAlbumContent.moreRecyclerView.layoutManager = GridLayoutManager(
            requireContext(),
            1,
            GridLayoutManager.HORIZONTAL,
            false
        )
        binding.fragmentAlbumContent.moreRecyclerView.adapter = albumAdapter
    }

    private fun aboutAlbum(lastFmAlbum: LastFmAlbum) {
        if (lastFmAlbum.album != null) {
            if (lastFmAlbum.album.wiki != null) {
                binding.fragmentAlbumContent.aboutAlbumText.show()
                binding.fragmentAlbumContent.aboutAlbumTitle.show()
                binding.fragmentAlbumContent.aboutAlbumTitle.text =
                    String.format(getString(R.string.about_album_label), lastFmAlbum.album.name)
                binding.fragmentAlbumContent.aboutAlbumText.text =
                    lastFmAlbum.album.wiki.content.parseAsHtml()
            }
            if (lastFmAlbum.album.listeners.isNotEmpty()) {
                binding.fragmentAlbumContent.listeners.show()
                binding.fragmentAlbumContent.listenersLabel.show()
                binding.fragmentAlbumContent.scrobbles.show()
                binding.fragmentAlbumContent.scrobblesLabel.show()

                binding.fragmentAlbumContent.listeners.text =
                    ApexUtil.formatValue(lastFmAlbum.album.listeners.toFloat())
                binding.fragmentAlbumContent.scrobbles.text =
                    ApexUtil.formatValue(lastFmAlbum.album.playcount.toFloat())
            }
        }
    }

    private fun loadArtistImage(artist: Artist) {
        detailsViewModel.getMoreAlbums(artist).observe(viewLifecycleOwner) {
            moreAlbums(it)
        }
        Glide.with(requireContext())
            .load(ApexGlideExtension.getArtistModel(artist))
            .artistImageOptions(artist)
            .dontAnimate()
            .into(binding.artistImage)
    }

    private fun loadAlbumCover(album: Album) {
        Glide.with(requireContext()).asBitmapPalette()
            .albumCoverOptions(album.safeGetFirstSong())
            //.checkIgnoreMediaStore()
            .load(ApexGlideExtension.getSongModel(album.safeGetFirstSong()))
            .into(object : SingleColorTarget(binding.image) {
                override fun onColorReady(color: Int) {
                    setColors(color)
                }
            })
    }

    private fun setColors(color: Int) {
        _binding?.fragmentAlbumContent?.apply {
            shuffleAction.applyColor(color)
            playAction.applyOutlineColor(color)
        }
    }

    override fun onAlbumClick(albumId: Long, view: View) {
        findNavController().navigate(
            R.id.albumDetailsFragment,
            bundleOf(EXTRA_ALBUM_ID to albumId),
            null,
            FragmentNavigatorExtras(
                view to albumId.toString()
            )
        )
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_album_detail, menu)
        val sortOrder = menu.findItem(R.id.action_sort_order)
        setUpSortOrderMenu(sortOrder.subMenu!!)
        ToolbarContentTintHelper.handleOnCreateOptionsMenu(
            requireContext(),
            binding.toolbar,
            menu,
            getToolbarBackgroundColor(binding.toolbar)
        )
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return handleSortOrderMenuItem(item)
    }

    private fun handleSortOrderMenuItem(item: MenuItem): Boolean {
        var sortOrder: String? = null
        val songs = simpleSongAdapter.dataSet
        when (item.itemId) {
            android.R.id.home -> findNavController().navigateUp()
            R.id.action_play_next -> {
                MusicPlayerRemote.playNext(songs)
                return true
            }

            R.id.action_add_to_current_playing -> {
                MusicPlayerRemote.enqueue(songs)
                return true
            }

            R.id.action_add_to_playlist -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    val playlists = get<RealRepository>().fetchPlaylists()
                    withContext(Dispatchers.Main) {
                        AddToPlaylistDialog.create(playlists, songs)
                            .show(childFragmentManager, "ADD_PLAYLIST")
                    }
                }
                return true
            }

            R.id.action_delete_from_device -> {
                DeleteSongsDialog.create(songs).show(childFragmentManager, "DELETE_SONGS")
                return true
            }

            R.id.action_tag_editor -> {
                val intent = Intent(requireContext(), AlbumTagEditorActivity::class.java)
                intent.putExtra(AbsTagEditorActivity.EXTRA_ID, album.id)
                val options = ActivityOptions.makeSceneTransitionAnimation(
                    requireActivity(),
                    binding.albumCoverContainer,
                    "${getString(R.string.transition_album_art)}_${album.id}"
                )
                startActivity(
                    intent, options.toBundle()
                )
                return true
            }

            R.id.action_sort_order_title -> sortOrder = SONG_A_Z
            R.id.action_sort_order_title_desc -> sortOrder = SONG_Z_A
            R.id.action_sort_order_track_list -> sortOrder = SONG_TRACK_LIST
            R.id.action_sort_order_artist_song_duration -> sortOrder = SONG_DURATION
        }
        if (sortOrder != null) {
            item.isChecked = true
            setSaveSortOrder(sortOrder)
        }
        return true
    }

    private fun setUpSortOrderMenu(sortOrder: SubMenu) {
        when (savedSortOrder) {
            SONG_A_Z -> sortOrder.findItem(R.id.action_sort_order_title).isChecked = true
            SONG_Z_A -> sortOrder.findItem(R.id.action_sort_order_title_desc).isChecked = true
            SONG_TRACK_LIST ->
                sortOrder.findItem(R.id.action_sort_order_track_list).isChecked = true

            SONG_DURATION ->
                sortOrder.findItem(R.id.action_sort_order_artist_song_duration).isChecked = true
        }
    }

    private fun setSaveSortOrder(sortOrder: String) {
        PreferenceUtil.albumDetailSongSortOrder = sortOrder
        val songs = when (sortOrder) {
            SONG_TRACK_LIST -> album.songs.sortedWith { o1, o2 ->
                o1.trackNumber.compareTo(
                    o2.trackNumber
                )
            }

            SONG_A_Z -> {
                val collator = Collator.getInstance()
                album.songs.sortedWith { o1, o2 -> collator.compare(o1.title, o2.title) }
            }

            SONG_Z_A -> {
                val collator = Collator.getInstance()
                album.songs.sortedWith { o1, o2 -> collator.compare(o2.title, o1.title) }
            }

            SONG_DURATION -> album.songs.sortedWith { o1, o2 ->
                o1.duration.compareTo(
                    o2.duration
                )
            }

            else -> throw IllegalArgumentException("invalid $sortOrder")
        }
        album = album.copy(songs = songs)
        simpleSongAdapter.swapDataSet(album.songs)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
