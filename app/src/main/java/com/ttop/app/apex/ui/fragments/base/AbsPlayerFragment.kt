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
package com.ttop.app.apex.ui.fragments.base

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.provider.MediaStore
import android.view.GestureDetector
import android.view.HapticFeedbackConstants
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.menu.MenuItemImpl
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.navOptions
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.ttop.app.apex.EXTRA_ALBUM_ID
import com.ttop.app.apex.EXTRA_ARTIST_ID
import com.ttop.app.apex.R
import com.ttop.app.apex.db.PlaylistEntity
import com.ttop.app.apex.db.toSongEntity
import com.ttop.app.apex.dialogs.AddToPlaylistDialog
import com.ttop.app.apex.dialogs.CreatePlaylistDialog
import com.ttop.app.apex.dialogs.DeleteSongsDialog
import com.ttop.app.apex.dialogs.PlaybackSpeedDialog
import com.ttop.app.apex.dialogs.SleepTimerDialog
import com.ttop.app.apex.dialogs.SongDetailDialog
import com.ttop.app.apex.dialogs.SongShareDialog
import com.ttop.app.apex.dialogs.VolumeDialog
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.extensions.accentTextColor
import com.ttop.app.apex.extensions.currentFragment
import com.ttop.app.apex.extensions.getTintedDrawable
import com.ttop.app.apex.extensions.hide
import com.ttop.app.apex.extensions.keepScreenOn
import com.ttop.app.apex.extensions.materialDialog
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.extensions.whichFragment
import com.ttop.app.apex.extensions.withCenteredButtons
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.interfaces.IPaletteColorHolder
import com.ttop.app.apex.model.Song
import com.ttop.app.apex.repository.RealRepository
import com.ttop.app.apex.service.MusicService
import com.ttop.app.apex.ui.activities.MainActivity
import com.ttop.app.apex.ui.activities.tageditor.AbsTagEditorActivity
import com.ttop.app.apex.ui.activities.tageditor.SongTagEditorActivity
import com.ttop.app.apex.ui.fragments.LibraryViewModel
import com.ttop.app.apex.ui.fragments.NowPlayingScreen
import com.ttop.app.apex.ui.fragments.ReloadType
import com.ttop.app.apex.ui.fragments.player.PlayerAlbumCoverFragment
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.MusicUtil
import com.ttop.app.apex.util.NavigationUtil
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.RingtoneManager
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import kotlin.math.abs

abstract class AbsPlayerFragment(@LayoutRes layout: Int) : AbsMusicServiceFragment(layout),
    Toolbar.OnMenuItemClickListener, IPaletteColorHolder, PlayerAlbumCoverFragment.Callbacks {

    val libraryViewModel: LibraryViewModel by activityViewModel()

    val mainActivity: MainActivity
        get() = activity as MainActivity

    private var playerAlbumCoverFragment: PlayerAlbumCoverFragment? = null

    private fun goToLyrics() {
        val data: String? = MusicUtil.getLyrics(MusicPlayerRemote.currentSong)
        mainActivity.keepScreenOn(true)
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(MusicPlayerRemote.currentSong.title)
        builder.setMessage(if (data.isNullOrEmpty()) R.string.no_lyrics_found.toString() else data)

        builder.setNegativeButton(R.string.dismiss) { _, _ ->
            mainActivity.keepScreenOn(false)
            materialDialog().dismiss()
        }

        val dialog: AlertDialog = builder.show()

        val textViewMessage: TextView? = dialog.findViewById(android.R.id.message)
        val textViewTitle: TextView? = dialog.findViewById(R.id.alertTitle)

        dialog.getButton(Dialog.BUTTON_NEGATIVE).textSize = 26f
        textViewMessage!!.textSize = 24f
        textViewTitle!!.textSize = 28f

        dialog.setCanceledOnTouchOutside(false)
        dialog.getButton(Dialog.BUTTON_NEGATIVE).accentTextColor()
        textViewTitle.setTextColor(mainActivity.accentColor())

        dialog.withCenteredButtons()
    }

    override fun onMenuItemClick(
        item: MenuItem,
    ): Boolean {
        val song = MusicPlayerRemote.currentSong
        if (!PreferenceUtil.isHapticFeedbackDisabled) {
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
        when (item.itemId) {
            R.id.action_playback_speed -> {
                PlaybackSpeedDialog.newInstance().show(childFragmentManager, "PLAYBACK_SETTINGS")
                return true
            }

            R.id.action_go_to_lyrics -> {
                goToLyrics()
                return true
            }

            R.id.action_toggle_favorite -> {
                toggleFavorite(song)
                if (!PreferenceUtil.isHapticFeedbackDisabled) {
                    requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                }
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

            R.id.action_delete_from_device -> {
                DeleteSongsDialog.create(song).show(childFragmentManager, "DELETE_SONGS")
                return true
            }

            R.id.action_add_to_playlist -> {
                lifecycleScope.launch(IO) {
                    val playlists = get<RealRepository>().fetchPlaylists()
                    withContext(Main) {
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
                NavigationUtil.openEqualizer(requireActivity())
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

            R.id.action_fast_forward -> {
                if (MusicPlayerRemote.songDurationMillis - MusicPlayerRemote.songProgressMillis > 10000) {
                    MusicPlayerRemote.pauseSong()
                    MusicPlayerRemote.seekTo(MusicPlayerRemote.songProgressMillis + 10000)
                    MusicPlayerRemote.resumePlaying()
                }
                return true
            }

            R.id.action_rewind -> {
                if (MusicPlayerRemote.songProgressMillis > 10000) {
                    MusicPlayerRemote.pauseSong()
                    MusicPlayerRemote.seekTo(MusicPlayerRemote.songProgressMillis - 10000)
                    MusicPlayerRemote.resumePlaying()
                }
                return true
            }

            R.id.action_volume -> {
                VolumeDialog.newInstance().show(childFragmentManager, "VOLUME")
                return true
            }
        }
        return false
    }

    abstract fun playerToolbar(): Toolbar?

    abstract fun onShow()

    abstract fun onHide()

    abstract fun onBackPressed(): Boolean

    abstract fun toolbarIconColor(): Int

    override fun onServiceConnected() {
        updateIsFavorite()
    }

    override fun onPlayingMetaChanged() {
        updateIsFavorite()
    }

    override fun onFavoriteStateChanged() {
        updateIsFavorite(animate = true)
    }

    protected open fun toggleFavorite(song: Song) {
        lifecycleScope.launch(IO) {
            val playlist: PlaylistEntity = libraryViewModel.favoritePlaylist()
            val songEntity = song.toSongEntity(playlist.playListId)
            val isFavorite = libraryViewModel.isSongFavorite(song.id)
            if (isFavorite) {
                libraryViewModel.removeSongFromPlaylist(songEntity)
            } else {
                libraryViewModel.insertSongs(listOf(song.toSongEntity(playlist.playListId)))
            }
            libraryViewModel.forceReload(ReloadType.Playlists)
            requireContext().sendBroadcast(Intent(MusicService.FAVORITE_STATE_CHANGED))
        }
    }

    fun updateIsFavorite(animate: Boolean = false) {
        lifecycleScope.launch(IO) {
            val isFavorite: Boolean =
                libraryViewModel.isSongFavorite(MusicPlayerRemote.currentSong.id)
            withContext(Main) {
                val icon = if (animate) {
                    if (isFavorite) R.drawable.avd_favorite else R.drawable.avd_unfavorite
                } else {
                    if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
                }
                val drawable = requireContext().getTintedDrawable(
                    icon,
                    toolbarIconColor()
                )
                if (playerToolbar() != null) {
                    playerToolbar()?.menu?.findItem(R.id.action_toggle_favorite)?.apply {
                        setIcon(drawable)
                        title =
                            if (isFavorite) getString(R.string.action_remove_from_favorites)
                            else getString(R.string.action_add_to_favorites)
                        getIcon().also {
                            if (it is AnimatedVectorDrawable) {
                                it.start()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireContext().theme.applyStyle(R.style.CircleFABOverlay, true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playerAlbumCoverFragment = whichFragment(R.id.playerAlbumCoverFragment)
        playerAlbumCoverFragment?.setCallbacks(this)

        view.findViewById<RelativeLayout>(R.id.statusBarShadow)?.hide()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onResume() {
        super.onResume()

        when (PreferenceUtil.nowPlayingScreen) {
            NowPlayingScreen.Adaptive -> {
                playerToolbar()?.menu?.removeItem(R.id.now_playing)
                playerToolbar()?.menu?.removeItem(R.id.action_rewind)
                playerToolbar()?.menu?.removeItem(R.id.action_fast_forward)
            }

            NowPlayingScreen.Blur -> {
                playerToolbar()?.menu?.removeItem(R.id.now_playing)
                playerToolbar()?.menu?.removeItem(R.id.action_rewind)
                playerToolbar()?.menu?.removeItem(R.id.action_fast_forward)
            }

            NowPlayingScreen.Card -> {
                playerToolbar()?.menu?.removeItem(R.id.action_rewind)
                playerToolbar()?.menu?.removeItem(R.id.action_fast_forward)
            }

            NowPlayingScreen.Gradient -> {
                playerToolbar()?.menu?.removeItem(R.id.action_rewind)
                playerToolbar()?.menu?.removeItem(R.id.action_fast_forward)
            }

            NowPlayingScreen.Classic -> {
                playerToolbar()?.menu?.removeItem(R.id.now_playing)
                playerToolbar()?.menu?.removeItem(R.id.action_rewind)
                playerToolbar()?.menu?.removeItem(R.id.action_fast_forward)
            }

            NowPlayingScreen.Peek -> {
                if (ApexUtil.isTablet) {
                    playerToolbar()?.menu?.removeItem(R.id.now_playing)
                }

                playerToolbar()?.menu?.removeItem(R.id.action_rewind)
                playerToolbar()?.menu?.removeItem(R.id.action_fast_forward)
            }

            NowPlayingScreen.Live -> {
                playerToolbar()?.menu?.removeItem(R.id.now_playing)
                playerToolbar()?.menu?.removeItem(R.id.action_go_to_lyrics)

                playerToolbar()?.menu?.removeItem(R.id.action_rewind)
                playerToolbar()?.menu?.removeItem(R.id.action_fast_forward)
            }

            NowPlayingScreen.Minimal -> {
            }
        }
    }

    val MenuItem.showAsActionFlag: Int
        @SuppressLint("RestrictedApi")
        get() {
            this as MenuItemImpl
            return when {
                requiresActionButton() -> MenuItemImpl.SHOW_AS_ACTION_ALWAYS
                requestsActionButton() -> MenuItemImpl.SHOW_AS_ACTION_IF_ROOM
                showsTextAsAction() -> MenuItemImpl.SHOW_AS_ACTION_WITH_TEXT
                else -> MenuItemImpl.SHOW_AS_ACTION_NEVER
            }
        }

    override fun onStart() {
        super.onStart()
        view?.setOnTouchListener(
            SwipeDetector(
                requireContext(),
                playerAlbumCoverFragment?.viewPager,
                requireView()
            )
        )
    }

    class SwipeDetector(val context: Context, private val viewPager: ViewPager?, val view: View) :
        View.OnTouchListener {
        private var flingPlayBackController: GestureDetector = GestureDetector(
            context,
            object : GestureDetector.SimpleOnGestureListener() {

                override fun onScroll(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    distanceX: Float,
                    distanceY: Float
                ): Boolean {
                    return when {
                        abs(distanceX) > abs(distanceY) -> {
                            // Disallow Intercept Touch Event so that parent(BottomSheet) doesn't consume the events
                            view.parent.requestDisallowInterceptTouchEvent(true)
                            true
                        }

                        else -> {
                            false
                        }
                    }
                }
            })

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            viewPager?.dispatchTouchEvent(event)
            return flingPlayBackController.onTouchEvent(event)
        }
    }

    companion object {
        val TAG: String = AbsPlayerFragment::class.java.simpleName
        const val VISIBILITY_ANIM_DURATION: Long = 300
    }
}

fun goToArtist(activity: Activity) {
    if (activity !is MainActivity) return
    val song = MusicPlayerRemote.currentSong
    activity.apply {

        // Remove exit transition of current fragment so
        // it doesn't exit with a weird transition
        currentFragment(R.id.fragment_container)?.exitTransition = null

        //Hide Bottom Bar First, else Bottom Sheet doesn't collapse fully
        setBottomNavVisibility(false)
        if (getBottomSheetBehavior().state == BottomSheetBehavior.STATE_EXPANDED) {
            collapsePanel()
        }

        findNavController(R.id.fragment_container).navigate(
            R.id.artistDetailsFragment,
            bundleOf(EXTRA_ARTIST_ID to song.artistId)
        )
    }
}