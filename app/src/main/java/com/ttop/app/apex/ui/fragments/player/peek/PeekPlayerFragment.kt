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
package com.ttop.app.apex.ui.fragments.player.peek

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.ContentUris
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.GradientDrawable
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.provider.MediaStore
import android.view.HapticFeedbackConstants
import android.view.MenuItem
import android.view.View
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager
import com.ttop.app.apex.EXTRA_ALBUM_ID
import com.ttop.app.apex.R
import com.ttop.app.apex.adapter.song.PlayingQueueAdapter
import com.ttop.app.apex.databinding.FragmentPeekPlayerBinding
import com.ttop.app.apex.dialogs.AddToPlaylistDialog
import com.ttop.app.apex.dialogs.CreatePlaylistDialog
import com.ttop.app.apex.dialogs.DeleteSongsDialog
import com.ttop.app.apex.dialogs.PlaybackSpeedDialog
import com.ttop.app.apex.dialogs.SleepTimerDialog
import com.ttop.app.apex.dialogs.SongDetailDialog
import com.ttop.app.apex.dialogs.SongShareDialog
import com.ttop.app.apex.extensions.colorControlNormal
import com.ttop.app.apex.extensions.darkAccentColor
import com.ttop.app.apex.extensions.drawAboveSystemBarsWithPadding
import com.ttop.app.apex.extensions.keepScreenOn
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.extensions.surfaceColor
import com.ttop.app.apex.extensions.whichFragment
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.repository.RealRepository
import com.ttop.app.apex.ui.activities.tageditor.AbsTagEditorActivity
import com.ttop.app.apex.ui.activities.tageditor.SongTagEditorActivity
import com.ttop.app.apex.ui.fragments.base.AbsPlayerFragment
import com.ttop.app.apex.ui.fragments.base.goToArtist
import com.ttop.app.apex.ui.fragments.player.PlayerAlbumCoverFragment
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.MusicUtil
import com.ttop.app.apex.util.NavigationUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.RingtoneManager
import com.ttop.app.apex.util.ViewUtil
import com.ttop.app.apex.util.color.MediaNotificationProcessor
import com.ttop.app.apex.views.DrawableGradient
import com.ttop.app.appthemehelper.ThemeStore
import com.ttop.app.appthemehelper.util.ATHUtil
import com.ttop.app.appthemehelper.util.ColorUtil
import com.ttop.app.appthemehelper.util.ToolbarContentTintHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.get
import java.lang.StringBuilder

class PeekPlayerFragment : AbsPlayerFragment(R.layout.fragment_peek_player),
    View.OnLayoutChangeListener {

    private lateinit var controlsFragment: PeekPlayerControlFragment
    private var valueAnimator: ValueAnimator? = null
    private var lastColor: Int = 0
    private var toolbarColor: Int =0
    private var _binding: FragmentPeekPlayerBinding? = null
    private val binding get() = _binding!!

    private val embed: TextView get() = binding.embedded
    private val scroll: ScrollView get() = binding.scroll

    private lateinit var wrappedAdapter: RecyclerView.Adapter<*>
    private var recyclerViewDragDropManager: RecyclerViewDragDropManager? = null
    private var recyclerViewTouchActionGuardManager: RecyclerViewTouchActionGuardManager? = null
    private var playingQueueAdapter: PlayingQueueAdapter? = null
    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onMenuItemClick(item: MenuItem): Boolean {
        val song = MusicPlayerRemote.currentSong
        requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        when (item.itemId) {
            R.id.action_playback_speed -> {
                PlaybackSpeedDialog.newInstance().show(childFragmentManager, "PLAYBACK_SETTINGS")
                return true
            }
            R.id.action_toggle_favorite -> {
                toggleFavorite(song)
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                return true
            }
            R.id.action_share -> {
                SongShareDialog.create(song).show(childFragmentManager, "SHARE_SONG")
                return true
            }
            R.id.action_go_to_drive_mode -> {
                NavigationUtil.gotoDriveMode(requireActivity())
                return true
            }
            R.id.action_reorder -> {
                if (binding.playerQueueSheet.visibility == View.VISIBLE) {
                    playingQueueAdapter?.setButtonsActivate()
                }
                return true
            }
            R.id.action_delete_from_device -> {
                DeleteSongsDialog.create(song).show(childFragmentManager, "DELETE_SONGS")
                return true
            }
            R.id.action_add_to_playlist -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    val playlists = get<RealRepository>().fetchPlaylists()
                    withContext(Dispatchers.Main) {
                        AddToPlaylistDialog.create(playlists, song)
                            .show(childFragmentManager, "ADD_PLAYLIST")
                    }
                }
                return true
            }
            R.id.action_clear_playing_queue -> {
                MusicPlayerRemote.clearQueue()
                return true
            }
            R.id.action_save_playing_queue -> {
                CreatePlaylistDialog.create(ArrayList(MusicPlayerRemote.playingQueue))
                    .show(childFragmentManager, "ADD_TO_PLAYLIST")
                return true
            }
            R.id.action_tag_editor -> {
                val intent = Intent(activity, SongTagEditorActivity::class.java)
                intent.putExtra(AbsTagEditorActivity.EXTRA_ID, song.id)
                startActivity(intent)
                return true
            }
            R.id.action_details -> {
                SongDetailDialog.create(song).show(childFragmentManager, "SONG_DETAIL")
                return true
            }
            R.id.action_go_to_album -> {
                //Hide Bottom Bar First, else Bottom Sheet doesn't collapse fully
                mainActivity.setBottomNavVisibility(false)
                mainActivity.collapsePanel()
                requireActivity().findNavController(R.id.fragment_container).navigate(
                    R.id.albumDetailsFragment,
                    bundleOf(EXTRA_ALBUM_ID to song.albumId)
                )
                return true
            }
            R.id.action_go_to_artist -> {
                goToArtist(requireActivity())
                return true
            }
            R.id.now_playing -> {
                requireActivity().findNavController(R.id.fragment_container).navigate(
                    R.id.playing_queue_fragment,
                    null,
                    navOptions { launchSingleTop = true }
                )
                mainActivity.collapsePanel()
                return true
            }
            R.id.action_equalizer -> {
                NavigationUtil.openEqualizer(requireActivity(), childFragmentManager, requireActivity().getString(R.string.equalizer_apex))
                return true
            }
            R.id.action_sleep_timer -> {
                SleepTimerDialog().show(parentFragmentManager, "SLEEP_TIMER")
                return true
            }
            R.id.action_set_as_ringtone -> {
                requireContext().run {
                    if (RingtoneManager.requiresDialog(this)) {
                        RingtoneManager.showDialog(this)
                    } else {
                        RingtoneManager.setRingtone(this, song)
                    }
                }

                return true
            }
            R.id.action_go_to_genre -> {
                val retriever = MediaMetadataRetriever()
                val trackUri =
                    ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        song.id
                    )
                retriever.setDataSource(activity, trackUri)
                var genre: String? =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
                if (genre == null) {
                    genre = "Not Specified"
                }
                showToast(genre)
                return true
            }
            R.id.action_queue -> {
                if (binding.playerQueueSheet.visibility == View.VISIBLE){
                    PreferenceUtil.isQueueHiddenPeek = true
                    binding.playerQueueSheet.visibility = View.GONE
                }else{
                    PreferenceUtil.isQueueHiddenPeek = false
                    binding.playerQueueSheet.visibility = View.VISIBLE
                }
            }
            R.id.action_go_to_lyrics -> {
                if (ApexUtil.isTablet) {
                    if (binding.playerQueueSheet.visibility == View.VISIBLE){
                        binding.playerQueueSheet.visibility = View.GONE
                        scroll.visibility = View.VISIBLE
                        if (!PreferenceUtil.isLyricsMessageDisabled) {
                            showToast(getString(R.string.lyrics_message_enabled))
                        }

                        if (PreferenceUtil.lyricsScreenOn) {
                            mainActivity.keepScreenOn(true)
                        }else {
                            mainActivity.keepScreenOn(false)
                        }

                        requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        PreferenceUtil.isEmbedLyricsActivated = true
                    }else{
                        binding.playerQueueSheet.visibility = View.VISIBLE
                        scroll.visibility = View.GONE
                        if (!PreferenceUtil.isLyricsMessageDisabled) {
                            showToast(getString(R.string.lyrics_message_disabled))
                        }
                        mainActivity.keepScreenOn(false)
                        requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        PreferenceUtil.isEmbedLyricsActivated = false
                    }
                }else {
                    binding.playerQueueSheet.visibility = View.GONE
                    if (scroll.visibility == View.GONE){
                        scroll.visibility = View.VISIBLE
                        if (!PreferenceUtil.isLyricsMessageDisabled) {
                            showToast(getString(R.string.lyrics_message_enabled))
                        }
                        playerToolbar().menu?.findItem(R.id.action_queue)?.isEnabled = false
                        playerToolbar().menu?.findItem(R.id.now_playing)?.isEnabled = false

                        if (PreferenceUtil.lyricsScreenOn) {
                            mainActivity.keepScreenOn(true)
                        }else {
                            mainActivity.keepScreenOn(false)
                        }

                        requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        PreferenceUtil.isEmbedLyricsActivated = true
                    }else{
                        scroll.visibility = View.GONE
                        if (!PreferenceUtil.isLyricsMessageDisabled) {
                            showToast(getString(R.string.lyrics_message_disabled))
                        }
                        playerToolbar().menu?.findItem(R.id.action_queue)?.isEnabled = true
                        playerToolbar().menu?.findItem(R.id.now_playing)?.isEnabled = true
                        mainActivity.keepScreenOn(false)
                        requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        PreferenceUtil.isEmbedLyricsActivated = false
                    }
                }
            }
        }
        return false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPeekPlayerBinding.bind(view)
        setUpPlayerToolbar()
        setUpSubFragments()
        setupRecyclerView()

        binding.root.drawAboveSystemBarsWithPadding()

        embed.textSize = 24f

        if (PreferenceUtil.isEmbedLyricsActivated) {
            if (ApexUtil.isTablet) {
                binding.playerQueueSheet.visibility = View.GONE
                scroll.visibility = View.VISIBLE
            }else {
                binding.playerQueueSheet.visibility = View.GONE
                scroll.visibility = View.VISIBLE
                playerToolbar().menu?.findItem(R.id.action_queue)?.isEnabled = false
            }
        }

        if (PreferenceUtil.lyricsMode == "disabled" || PreferenceUtil.lyricsMode == "synced") {
            playerToolbar().menu?.findItem(R.id.action_go_to_lyrics)?.isVisible = false
        }else {
            playerToolbar().menu?.findItem(R.id.action_go_to_lyrics)?.isVisible = true
        }
    }

    private fun setUpSubFragments() {
        controlsFragment =
            whichFragment(R.id.playbackControlsFragment) as PeekPlayerControlFragment

        val coverFragment =
            whichFragment(R.id.playerAlbumCoverFragment) as PlayerAlbumCoverFragment
        coverFragment.setCallbacks(this)
    }

    private fun setUpPlayerToolbar() {
        binding.playerToolbar.apply {
            inflateMenu(R.menu.menu_player)
            setNavigationOnClickListener {
                requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            setOnMenuItemClickListener(this@PeekPlayerFragment)
            ToolbarContentTintHelper.colorizeToolbar(
                this,
                toolbarIconColor(),
                requireActivity()
            )
        }
    }

    override fun playerToolbar(): Toolbar {
        return binding.playerToolbar
    }

    private fun setupRecyclerView() {
        playingQueueAdapter = if (ApexUtil.isTablet) {
            if(ApexUtil.isLandscape) {
                when (PreferenceUtil.queueStyleLand) {
                    "normal" -> {
                        PlayingQueueAdapter(
                            requireActivity() as AppCompatActivity,
                            MusicPlayerRemote.playingQueue.toMutableList(),
                            MusicPlayerRemote.position,
                            R.layout.item_queue_player_plain
                        )
                    }
                    "duo" -> {
                        PlayingQueueAdapter(
                            requireActivity() as AppCompatActivity,
                            MusicPlayerRemote.playingQueue.toMutableList(),
                            MusicPlayerRemote.position,
                            R.layout.item_queue_duo
                        )
                    }
                    "trio" -> {
                        PlayingQueueAdapter(
                            requireActivity() as AppCompatActivity,
                            MusicPlayerRemote.playingQueue.toMutableList(),
                            MusicPlayerRemote.position,
                            R.layout.item_queue_trio
                        )
                    }
                    else -> {
                        PlayingQueueAdapter(
                            requireActivity() as AppCompatActivity,
                            MusicPlayerRemote.playingQueue.toMutableList(),
                            MusicPlayerRemote.position,
                            R.layout.item_queue_player_plain
                        )
                    }
                }
            }else {
                when (PreferenceUtil.queueStyle) {
                    "normal" -> {
                        PlayingQueueAdapter(
                            requireActivity() as AppCompatActivity,
                            MusicPlayerRemote.playingQueue.toMutableList(),
                            MusicPlayerRemote.position,
                            R.layout.item_queue_player_plain
                        )
                    }
                    "duo" -> {
                        PlayingQueueAdapter(
                            requireActivity() as AppCompatActivity,
                            MusicPlayerRemote.playingQueue.toMutableList(),
                            MusicPlayerRemote.position,
                            R.layout.item_queue_duo
                        )
                    }
                    "trio" -> {
                        PlayingQueueAdapter(
                            requireActivity() as AppCompatActivity,
                            MusicPlayerRemote.playingQueue.toMutableList(),
                            MusicPlayerRemote.position,
                            R.layout.item_queue_trio
                        )
                    }
                    else -> {
                        PlayingQueueAdapter(
                            requireActivity() as AppCompatActivity,
                            MusicPlayerRemote.playingQueue.toMutableList(),
                            MusicPlayerRemote.position,
                            R.layout.item_queue_player_plain
                        )
                    }
                }
            }
        }else {
           PlayingQueueAdapter(
                requireActivity() as AppCompatActivity,
                MusicPlayerRemote.playingQueue.toMutableList(),
                MusicPlayerRemote.position,
                R.layout.item_queue
            )
        }

        linearLayoutManager = LinearLayoutManager(requireContext())
        recyclerViewTouchActionGuardManager = RecyclerViewTouchActionGuardManager()
        recyclerViewDragDropManager = RecyclerViewDragDropManager()

        val animator = DraggableItemAnimator()
        animator.supportsChangeAnimations = false
        wrappedAdapter =
            recyclerViewDragDropManager?.createWrappedAdapter(playingQueueAdapter!!) as RecyclerView.Adapter<*>
        binding.recyclerView.layoutManager = linearLayoutManager
        binding.recyclerView.adapter = wrappedAdapter
        binding.recyclerView.itemAnimator = animator
        recyclerViewTouchActionGuardManager?.attachRecyclerView(binding.recyclerView)
        recyclerViewDragDropManager?.attachRecyclerView(binding.recyclerView)

        linearLayoutManager.scrollToPositionWithOffset(MusicPlayerRemote.position + 1, 0)
    }

    private fun updateQueuePosition() {
        playingQueueAdapter?.setCurrent(MusicPlayerRemote.position)
        resetToCurrentPosition()
    }

    private fun updateQueue() {
        playingQueueAdapter?.swapDataSet(MusicPlayerRemote.playingQueue, MusicPlayerRemote.position)
        resetToCurrentPosition()
    }

    private fun resetToCurrentPosition() {
        binding.recyclerView.stopScroll()
        linearLayoutManager.scrollToPositionWithOffset(MusicPlayerRemote.position + 1, 0)
    }

    private fun getQueuePanel(): BottomSheetBehavior<MaterialCardView> {
        return BottomSheetBehavior.from(binding.playerQueueSheet)
    }

    override fun onQueueChanged() {
        super.onQueueChanged()
        updateQueue()

        val string = StringBuilder()
        string.append(MusicUtil.getLyrics(MusicPlayerRemote.currentSong)).append("\n")
        embed.text = string.toString()
    }

    override fun onShow() {
    }

    override fun onHide() {
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun toolbarIconColor() =
        if (PreferenceUtil.isAdaptiveColor) {
        toolbarColor
    }else {
        colorControlNormal()
    }

    override val paletteColor: Int
        get() = lastColor

    override fun onColorChanged(color: MediaNotificationProcessor) {
        lastColor = color.primaryTextColor
        toolbarColor = color.secondaryTextColor
        libraryViewModel.updateColor(color.primaryTextColor)
        controlsFragment.setColor(color)

        ToolbarContentTintHelper.colorizeToolbar(
            binding.playerToolbar,
            toolbarIconColor(),
            requireActivity()
        )

        if (PreferenceUtil.isAdaptiveColor) {
            colorize(color.backgroundColor)

            if (PreferenceUtil.isColorAnimate) {
                val animator =
                    binding.colorGradientBackground.let { controlsFragment.createRevealAnimator(it) }
                animator.doOnEnd {
                    _binding?.root?.setBackgroundColor(color.backgroundColor)
                }
                animator.start()
            }
        }

        playingQueueAdapter?.setTextColor(color.secondaryTextColor)

        val controlsColor =
            if (PreferenceUtil.isAdaptiveColor) {
                color.secondaryTextColor
            } else {
                ThemeStore.accentColor(requireContext())
            }

        binding.playerQueueSubHeader?.setTextColor(controlsColor)
        val colorBg = ATHUtil.resolveColor(requireContext(), android.R.attr.colorBackground)

        if (PreferenceUtil.materialYou) {
            if (PreferenceUtil.isAdaptiveColor) {
                embed.setBackgroundColor(color.backgroundColor)
                embed.setTextColor(color.secondaryTextColor)
            }else {
                embed.setBackgroundColor(requireContext().darkAccentColor())

                if (ColorUtil.isColorLight(colorBg)) {
                    embed.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_black_1000))
                }else {
                    embed.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                }
            }
        }else {
            if (PreferenceUtil.isAdaptiveColor) {
                embed.setBackgroundColor(color.backgroundColor)
                embed.setTextColor(color.secondaryTextColor)
            }else {
                if (ApexUtil.isTablet) {
                    when (PreferenceUtil.baseTheme) {
                        "light" -> {
                            embed.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_black_1000))
                        }
                        "dark" -> {
                            if (PreferenceUtil.isBlackMode) {
                                embed.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                            }else {
                                embed.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                            }
                        }
                        "auto" -> {
                            when (requireContext().resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                                Configuration.UI_MODE_NIGHT_YES -> {
                                    if (PreferenceUtil.isBlackMode) {
                                        embed.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                                    }else {
                                        embed.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                                    }
                                }
                                Configuration.UI_MODE_NIGHT_NO,
                                Configuration.UI_MODE_NIGHT_UNDEFINED-> {
                                    embed.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_black_1000))
                                }
                            }
                        }
                    }

                }else {
                    embed.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (PreferenceUtil.isEmbedLyricsActivated) {
                if (ApexUtil.isTablet) {
                    binding.playerQueueSheet.visibility = View.GONE
                    scroll.visibility = View.VISIBLE
                }else {
                    binding.playerQueueSheet.visibility = View.GONE
                    scroll.visibility = View.VISIBLE
                    playerToolbar().menu?.findItem(R.id.action_queue)?.isEnabled = false
                }
            }
        }else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (PreferenceUtil.isEmbedLyricsActivated) {
                if (ApexUtil.isTablet) {
                    binding.playerQueueSheet.visibility = View.GONE
                    scroll.visibility = View.VISIBLE
                }else {
                    binding.playerQueueSheet.visibility = View.GONE
                    scroll.visibility = View.VISIBLE
                    playerToolbar().menu?.findItem(R.id.action_queue)?.isEnabled = false
                }
            }
        }
    }

    private fun colorize(i: Int) {
        if (PreferenceUtil.isPlayerBackgroundType) {
            //GRADIENT
            if (valueAnimator != null) {
                valueAnimator?.cancel()
            }

            valueAnimator = ValueAnimator.ofObject(
                ArgbEvaluator(),
                surfaceColor(),
                i
            )
            valueAnimator?.addUpdateListener { animation ->
                if (isAdded) {
                    val drawable = DrawableGradient(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        intArrayOf(
                            animation.animatedValue as Int,
                            surfaceColor()
                        ), 0
                    )
                    binding.colorGradientBackground.background = drawable
                }
            }
            valueAnimator?.setDuration(ViewUtil.APEX_MUSIC_ANIM_TIME.toLong())?.start()
        }else {
            //SINGLE COLOR
            binding.colorGradientBackground.setBackgroundColor(i)
        }
    }

    override fun onFavoriteToggled() {
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        controlsFragment.updateSong()
        updateQueue()

        val string = StringBuilder()
        string.append(MusicUtil.getLyrics(MusicPlayerRemote.currentSong)).append("\n")
        embed.text = string.toString()
    }

    override fun onPlayingMetaChanged() {
        super.onPlayingMetaChanged()
        controlsFragment.updateSong()
        updateQueuePosition()

        val string = StringBuilder()
        string.append(MusicUtil.getLyrics(MusicPlayerRemote.currentSong)).append("\n")
        embed.text = string.toString()
        scroll.scrollTo(0,0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onLayoutChange(
        v: View?,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        oldLeft: Int,
        oldTop: Int,
        oldRight: Int,
        oldBottom: Int
    ) {
        val height = binding.playerContainer.height
        val width = binding.playerContainer.width
        val finalHeight = height - (binding.playbackControlsFragment.rootView.height + width)
        val panel = getQueuePanel()
        panel.peekHeight = finalHeight
    }
}