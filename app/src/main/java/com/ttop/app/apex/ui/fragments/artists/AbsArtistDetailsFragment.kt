package com.ttop.app.apex.ui.fragments.artists

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.text.Spanned
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat.SRC_IN
import androidx.core.os.bundleOf
import androidx.core.text.parseAsHtml
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.Fade
import com.bumptech.glide.Glide
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.transition.MaterialContainerTransform
import com.ttop.app.apex.EXTRA_ALBUM_ID
import com.ttop.app.apex.R
import com.ttop.app.apex.adapter.album.HorizontalAlbumAdapter
import com.ttop.app.apex.adapter.song.SimpleSongAdapter
import com.ttop.app.apex.databinding.FragmentArtistDetailsBinding
import com.ttop.app.apex.dialogs.AddToPlaylistDialog
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.extensions.applyColor
import com.ttop.app.apex.extensions.applyOutlineColor
import com.ttop.app.apex.extensions.m3BgaccentColor
import com.ttop.app.apex.extensions.show
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.extensions.surfaceColor
import com.ttop.app.apex.glide.ApexGlideExtension
import com.ttop.app.apex.glide.ApexGlideExtension.artistImageOptions
import com.ttop.app.apex.glide.ApexGlideExtension.asBitmapPalette
import com.ttop.app.apex.glide.SingleColorTarget
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.helper.SortOrder
import com.ttop.app.apex.interfaces.IAlbumClickListener
import com.ttop.app.apex.libraries.appthemehelper.common.ATHToolbarActivity.getToolbarBackgroundColor
import com.ttop.app.apex.libraries.appthemehelper.util.ToolbarContentTintHelper
import com.ttop.app.apex.model.Artist
import com.ttop.app.apex.network.Result
import com.ttop.app.apex.network.model.LastFmArtist
import com.ttop.app.apex.repository.RealRepository
import com.ttop.app.apex.ui.fragments.base.AbsMainActivityFragment
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.CustomArtistImageUtil
import com.ttop.app.apex.util.MusicUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.logD
import com.ttop.app.apex.util.logE
import com.ttop.app.apex.util.theme.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.get
import java.util.Locale

abstract class AbsArtistDetailsFragment : AbsMainActivityFragment(R.layout.fragment_artist_details),
    IAlbumClickListener {
    private var _binding: FragmentArtistDetailsBinding? = null
    private val binding get() = _binding!!

    abstract val detailsViewModel: ArtistDetailsViewModel
    abstract val artistId: Long?
    abstract val artistName: String?
    private lateinit var artist: Artist
    private lateinit var songAdapter: SimpleSongAdapter
    private lateinit var albumAdapter: HorizontalAlbumAdapter
    private var forceDownload: Boolean = false
    private var lang: String? = null
    private var biography: Spanned? = null

    private val savedSongSortOrder: String
        get() = PreferenceUtil.artistDetailSongSortOrder

    private val savedAlbumSortOrder: String
        get() = PreferenceUtil.artistAlbumSortOrder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.fragment_container
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(surfaceColor())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentArtistDetailsBinding.bind(view)
        enterTransition = Fade()
        exitTransition = Fade()
        mainActivity.addMusicServiceEventListener(detailsViewModel)
        mainActivity.setSupportActionBar(binding.toolbar)
        binding.toolbar.title = null
        binding.artistCoverContainer.transitionName = (artistId ?: artistName).toString()
        postponeEnterTransition()
        detailsViewModel.getArtist().observe(viewLifecycleOwner) {
            view.doOnPreDraw {
                startPostponedEnterTransition()
            }
            showArtist(it)
        }
        setupRecyclerView()

        binding.fragmentArtistContent.playAction.apply {
            setOnClickListener { MusicPlayerRemote.openQueue(artist.sortedSongs, 0, true) }
        }
        binding.fragmentArtistContent.shuffleAction.apply {
            setOnClickListener { MusicPlayerRemote.openAndShuffleQueue(artist.songs, true) }
        }

        binding.fragmentArtistContent.biographyText.setOnClickListener {
            if (binding.fragmentArtistContent.biographyText.maxLines == 4) {
                binding.fragmentArtistContent.biographyText.maxLines = Integer.MAX_VALUE
            } else {
                binding.fragmentArtistContent.biographyText.maxLines = 4
            }
        }

        setupSongSortButton()

        setupAlbumSortButton()

        binding.appBarLayout?.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(requireContext())

        binding.toolbar.navigationIcon?.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                requireContext().accentColor(),
                SRC_IN
            )

        binding.fragmentArtistContent.songSortOrder.iconTint = ColorStateList.valueOf(accentColor())

        binding.fragmentArtistContent.albumSortOrder.iconTint = ColorStateList.valueOf(accentColor())

        if (PreferenceUtil.getGeneralThemeValue() == ThemeMode.MD3) {
            binding.appBarLayout?.setBackgroundColor(requireContext().m3BgaccentColor())
        }else {
            binding.appBarLayout?.setBackgroundColor(surfaceColor())
        }
    }

    private fun setupRecyclerView() {
        albumAdapter = HorizontalAlbumAdapter(requireActivity(), ArrayList(), this)
        binding.fragmentArtistContent.albumRecyclerView.apply {
            itemAnimator = DefaultItemAnimator()
            layoutManager = GridLayoutManager(this.context, 1, GridLayoutManager.HORIZONTAL, false)
            adapter = albumAdapter
        }
        songAdapter = SimpleSongAdapter(requireActivity(), ArrayList(), R.layout.item_song)
        binding.fragmentArtistContent.recyclerView.apply {
            itemAnimator = DefaultItemAnimator()
            layoutManager = LinearLayoutManager(this.context)
            adapter = songAdapter
        }
    }

    private fun showArtist(artist: Artist) {
        if (artist.songCount == 0) {
            findNavController().navigateUp()
            return
        }
        this.artist = artist
        loadArtistImage(artist)
        if (PreferenceUtil.isAllowedToDownloadMetadata(requireContext())) {
            loadBiography(artist.name)
        }
        binding.artistTitle.text = artist.name
        binding.text.text = String.format(
            "%s • %s",
            MusicUtil.getArtistInfoString(requireContext(), artist),
            MusicUtil.getReadableDurationString(MusicUtil.getTotalDuration(artist.songs))
        )

        if (PreferenceUtil.getGeneralThemeValue() == ThemeMode.MD3) {
            binding.text.setTextColor(ContextCompat.getColor(requireContext(), R.color.m3_widget_other_text))
        }else {
            binding.text.setTextColor(accentColor())
        }

        val songText = resources.getQuantityString(
            R.plurals.albumSongs,
            artist.songCount,
            artist.songCount
        )
        val albumText = resources.getQuantityString(
            R.plurals.albums,
            artist.songCount,
            artist.songCount
        )
        binding.fragmentArtistContent.songTitle.text = songText
        binding.fragmentArtistContent.albumTitle.text = albumText
        songAdapter.swapDataSet(artist.sortedSongs)
        albumAdapter.swapDataSet(artist.sortedAlbums)

        if (PreferenceUtil.getGeneralThemeValue() == ThemeMode.MD3) {
            binding.artistTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.m3_widget_other_text))
            binding.fragmentArtistContent.songTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.m3_widget_other_text))
            binding.fragmentArtistContent.albumTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.m3_widget_other_text))
            binding.fragmentArtistContent.biographyTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.m3_widget_other_text))
            binding.fragmentArtistContent.listenersLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.m3_widget_other_text))
            binding.fragmentArtistContent.scrobblesLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.m3_widget_other_text))
        }else {
            binding.artistTitle.setTextColor(accentColor())
            binding.fragmentArtistContent.songTitle.setTextColor(accentColor())
            binding.fragmentArtistContent.albumTitle.setTextColor(accentColor())
            binding.fragmentArtistContent.biographyTitle.setTextColor(accentColor())
            binding.fragmentArtistContent.listenersLabel.setTextColor(accentColor())
            binding.fragmentArtistContent.scrobblesLabel.setTextColor(accentColor())
        }
    }

    private fun loadBiography(
        name: String,
        lang: String? = Locale.getDefault().language,
    ) {
        biography = null
        this.lang = lang
        detailsViewModel.getArtistInfo(name, lang, null)
            .observe(viewLifecycleOwner) { result ->
                when (result) {
                    is Result.Loading -> logD("Loading")
                    is Result.Error -> logE("Error")
                    is Result.Success -> artistInfo(result.data)
                }
            }
    }

    private fun artistInfo(lastFmArtist: LastFmArtist?) {
        if (lastFmArtist != null && lastFmArtist.artist != null && lastFmArtist.artist.bio != null) {
            val bioContent = lastFmArtist.artist.bio.content
            if (bioContent != null && bioContent.trim { it <= ' ' }.isNotEmpty()) {
                binding.fragmentArtistContent.run {
                    biographyText.isVisible = true
                    biographyTitle.isVisible = true
                    biography = bioContent.parseAsHtml()
                    biographyText.text = biography
                    if (lastFmArtist.artist.stats.listeners.isNotEmpty()) {
                        listeners.show()
                        listenersLabel.show()
                        scrobbles.show()
                        scrobblesLabel.show()
                        listeners.text =
                            ApexUtil.formatValue(lastFmArtist.artist.stats.listeners.toFloat())
                        scrobbles.text =
                            ApexUtil.formatValue(lastFmArtist.artist.stats.playcount.toFloat())
                    }
                }
            }
        }

        // If the "lang" parameter is set and no biography is given, retry with default language
        if (biography == null && lang != null) {
            loadBiography(artist.name, null)
        }

        when (PreferenceUtil.getGeneralThemeValue()) {
            ThemeMode.AUTO -> {
                when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        binding.fragmentArtistContent.listeners.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.fragmentArtistContent.scrobbles.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.fragmentArtistContent.biographyText.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                    }

                    Configuration.UI_MODE_NIGHT_NO,
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                        binding.fragmentArtistContent.listeners.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkColorSurface))
                        binding.fragmentArtistContent.scrobbles.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkColorSurface))
                        binding.fragmentArtistContent.biographyText.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkColorSurface))
                    }

                    else -> {
                        binding.fragmentArtistContent.listeners.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.fragmentArtistContent.scrobbles.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.fragmentArtistContent.biographyText.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                    }
                }
            }

            ThemeMode.AUTO_BLACK -> {
                when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        binding.fragmentArtistContent.listeners.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.fragmentArtistContent.scrobbles.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.fragmentArtistContent.biographyText.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                    }

                    Configuration.UI_MODE_NIGHT_NO,
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                        binding.fragmentArtistContent.listeners.setTextColor(ContextCompat.getColor(requireContext(), R.color.blackColorSurface))
                        binding.fragmentArtistContent.scrobbles.setTextColor(ContextCompat.getColor(requireContext(), R.color.blackColorSurface))
                        binding.fragmentArtistContent.biographyText.setTextColor(ContextCompat.getColor(requireContext(), R.color.blackColorSurface))
                    }

                    else -> {
                        binding.fragmentArtistContent.listeners.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.fragmentArtistContent.scrobbles.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                        binding.fragmentArtistContent.biographyText.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                    }
                }
            }

            ThemeMode.BLACK,
            ThemeMode.DARK -> {
                binding.fragmentArtistContent.listeners.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                binding.fragmentArtistContent.scrobbles.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                binding.fragmentArtistContent.biographyText.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
            }

            ThemeMode.LIGHT -> {
                binding.fragmentArtistContent.listeners.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkColorSurface))
                binding.fragmentArtistContent.scrobbles.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkColorSurface))
                binding.fragmentArtistContent.biographyText.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkColorSurface))
            }

            ThemeMode.MD3 -> {
                binding.fragmentArtistContent.listeners.setTextColor(requireContext().accentColor())
                binding.fragmentArtistContent.scrobbles.setTextColor(requireContext().accentColor())
                binding.fragmentArtistContent.biographyText.setTextColor(requireContext().accentColor())
            }
        }
    }

    private fun loadArtistImage(artist: Artist) {
        Glide.with(requireContext()).asBitmapPalette().artistImageOptions(artist)
            .load(ApexGlideExtension.getArtistModel(artist))
            .dontAnimate()
            .into(object : SingleColorTarget(binding.image) {
                override fun onColorReady(color: Int) {
                }
            })

        _binding?.fragmentArtistContent?.apply {
            shuffleAction.applyOutlineColor(accentColor())
            playAction.applyOutlineColor(accentColor())
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

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return handleSortOrderMenuItem(item)
    }

    private fun handleSortOrderMenuItem(item: MenuItem): Boolean {
        val songs = artist.songs
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

            R.id.action_set_artist_image -> {
                selectImageLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
                return true
            }

            R.id.action_reset_artist_image -> {
                showToast(resources.getString(R.string.updating))
                lifecycleScope.launch {
                    CustomArtistImageUtil.getInstance(requireContext())
                        .resetCustomArtistImage(artist)
                }
                forceDownload = true
                return true
            }
        }
        return true
    }

    private fun setupSongSortButton() {
        binding.fragmentArtistContent.songSortOrder.setOnClickListener {
            PopupMenu(requireContext(), binding.fragmentArtistContent.songSortOrder).apply {
                inflate(R.menu.menu_artist_song_sort_order)
                setUpSortOrderMenu(menu)
                setOnMenuItemClickListener { item ->
                    val sortOrder = when (item.itemId) {
                        R.id.action_sort_order_title -> SortOrder.ArtistSongSortOrder.SONG_A_Z
                        R.id.action_sort_order_title_desc -> SortOrder.ArtistSongSortOrder.SONG_Z_A
                        R.id.action_sort_order_album -> SortOrder.ArtistSongSortOrder.SONG_ALBUM
                        R.id.action_sort_order_year -> SortOrder.ArtistSongSortOrder.SONG_YEAR
                        R.id.action_sort_order_song_duration -> SortOrder.ArtistSongSortOrder.SONG_DURATION
                        else -> {
                            throw IllegalArgumentException("invalid ${item.title}")
                        }
                    }
                    item.isChecked = true
                    setSaveSortOrder(sortOrder)
                    return@setOnMenuItemClickListener true
                }
                show()
            }
        }
    }

    private fun setupAlbumSortButton() {
        binding.fragmentArtistContent.albumSortOrder.setOnClickListener {
            PopupMenu(requireContext(), binding.fragmentArtistContent.albumSortOrder).apply {
                inflate(R.menu.menu_artist_album_sort_order)
                setUpSortAlbumOrderMenu(menu)
                setOnMenuItemClickListener { item ->
                    val sortOrder = when (item.itemId) {
                        R.id.action_sort_order_title -> SortOrder.ArtistAlbumSortOrder.ARTIST_ALBUM_A_Z
                        R.id.action_sort_order_title_desc -> SortOrder.ArtistAlbumSortOrder.ARTIST_ALBUM_Z_A
                        R.id.action_sort_order_year -> SortOrder.ArtistAlbumSortOrder.ARTIST_ALBUM_YEAR
                        R.id.action_sort_order_year_desc -> SortOrder.ArtistAlbumSortOrder.ARTIST_ALBUM_YEAR_DESC
                        else -> {
                            throw IllegalArgumentException("invalid ${item.title}")
                        }
                    }
                    item.isChecked = true
                    setSaveAlbumSortOrder(sortOrder)
                    return@setOnMenuItemClickListener true
                }
                show()
            }
        }
    }

    private fun setSaveSortOrder(sortOrder: String) {
        PreferenceUtil.artistDetailSongSortOrder = sortOrder
        songAdapter.swapDataSet(artist.sortedSongs)
    }

    private fun setSaveAlbumSortOrder(sortOrder: String) {
        PreferenceUtil.artistAlbumSortOrder = sortOrder
        albumAdapter.swapDataSet(artist.sortedAlbums)
        binding.fragmentArtistContent.albumRecyclerView.scrollToPosition(0)
    }

    private fun setUpSortOrderMenu(sortOrder: Menu) {
        when (savedSongSortOrder) {
            SortOrder.ArtistSongSortOrder.SONG_A_Z -> sortOrder.findItem(R.id.action_sort_order_title).isChecked =
                true

            SortOrder.ArtistSongSortOrder.SONG_Z_A -> sortOrder.findItem(R.id.action_sort_order_title_desc).isChecked =
                true

            SortOrder.ArtistSongSortOrder.SONG_ALBUM ->
                sortOrder.findItem(R.id.action_sort_order_album).isChecked = true

            SortOrder.ArtistSongSortOrder.SONG_YEAR ->
                sortOrder.findItem(R.id.action_sort_order_year).isChecked = true

            SortOrder.ArtistSongSortOrder.SONG_DURATION ->
                sortOrder.findItem(R.id.action_sort_order_song_duration).isChecked = true

            else -> {
                throw IllegalArgumentException("invalid $savedSongSortOrder")
            }
        }
    }

    private fun setUpSortAlbumOrderMenu(sortOrder: Menu) {
        when (savedAlbumSortOrder) {
            SortOrder.ArtistAlbumSortOrder.ARTIST_ALBUM_A_Z -> sortOrder.findItem(R.id.action_sort_order_title).isChecked =
                true

            SortOrder.ArtistAlbumSortOrder.ARTIST_ALBUM_Z_A -> sortOrder.findItem(R.id.action_sort_order_title_desc).isChecked =
                true

            SortOrder.ArtistAlbumSortOrder.ARTIST_ALBUM_YEAR ->
                sortOrder.findItem(R.id.action_sort_order_year).isChecked = true

            SortOrder.ArtistAlbumSortOrder.ARTIST_ALBUM_YEAR_DESC ->
                sortOrder.findItem(R.id.action_sort_order_year_desc).isChecked = true

            else -> {
                throw IllegalArgumentException("invalid $savedAlbumSortOrder")
            }
        }
    }

    private val selectImageLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            lifecycleScope.launch {
                if (uri != null) {
                    CustomArtistImageUtil.getInstance(requireContext())
                        .setCustomArtistImage(artist, uri)
                }
            }
        }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_artist_detail, menu)

        ToolbarContentTintHelper.handleOnCreateOptionsMenu(
            requireContext(),
            binding.toolbar,
            menu,
            getToolbarBackgroundColor(binding.toolbar)
        )

        binding.toolbar.overflowIcon?.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                requireContext().accentColor(),
                SRC_IN
            )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}