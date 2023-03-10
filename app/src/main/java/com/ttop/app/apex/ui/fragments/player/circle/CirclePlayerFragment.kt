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
package com.ttop.app.apex.ui.fragments.player.circle

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.provider.MediaStore
import android.view.HapticFeedbackConstants
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.getSystemService
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.slider.Slider
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager
import com.ttop.app.apex.EXTRA_ALBUM_ID
import com.ttop.app.apex.R
import com.ttop.app.apex.adapter.song.PlayingQueueAdapter
import com.ttop.app.apex.databinding.FragmentCirclePlayerBinding
import com.ttop.app.apex.dialogs.*
import com.ttop.app.apex.extensions.*
import com.ttop.app.apex.glide.ApexGlideExtension
import com.ttop.app.apex.glide.GlideApp
import com.ttop.app.apex.glide.GlideRequest
import com.ttop.app.apex.glide.crossfadeListener
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.helper.MusicProgressViewUpdateHelper
import com.ttop.app.apex.helper.MusicProgressViewUpdateHelper.Callback
import com.ttop.app.apex.helper.PlayPauseButtonOnClickHandler
import com.ttop.app.apex.repository.RealRepository
import com.ttop.app.apex.ui.activities.tageditor.AbsTagEditorActivity
import com.ttop.app.apex.ui.activities.tageditor.SongTagEditorActivity
import com.ttop.app.apex.ui.fragments.MusicSeekSkipTouchListener
import com.ttop.app.apex.ui.fragments.base.AbsPlayerFragment
import com.ttop.app.apex.ui.fragments.base.goToAlbum
import com.ttop.app.apex.ui.fragments.base.goToArtist
import com.ttop.app.apex.ui.fragments.base.goToLyrics
import com.ttop.app.apex.ui.fragments.player.PlayerAlbumCoverFragment
import com.ttop.app.apex.util.*
import com.ttop.app.apex.util.color.MediaNotificationProcessor
import com.ttop.app.apex.volume.AudioVolumeObserver
import com.ttop.app.apex.volume.OnAudioVolumeChangedListener
import com.ttop.app.appthemehelper.util.ColorUtil
import com.ttop.app.appthemehelper.util.MaterialValueHelper
import com.ttop.app.appthemehelper.util.TintHelper
import com.ttop.app.appthemehelper.util.ToolbarContentTintHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.tankery.lib.circularseekbar.CircularSeekBar
import org.koin.android.ext.android.get

/**
 * Created by hemanths on 2020-01-06.
 */

class CirclePlayerFragment : AbsPlayerFragment(R.layout.fragment_circle_player), Callback,
    OnAudioVolumeChangedListener,
    CircularSeekBar.OnCircularSeekBarChangeListener {

    private lateinit var progressViewUpdateHelper: MusicProgressViewUpdateHelper
    private var audioVolumeObserver: AudioVolumeObserver? = null

    private val audioManager: AudioManager
        get() = requireContext().getSystemService()!!

    private lateinit var wrappedAdapter: RecyclerView.Adapter<*>
    private var recyclerViewDragDropManager: RecyclerViewDragDropManager? = null
    private var recyclerViewTouchActionGuardManager: RecyclerViewTouchActionGuardManager? = null
    private var playingQueueAdapter: PlayingQueueAdapter? = null
    private lateinit var linearLayoutManager: LinearLayoutManager

    private var _binding: FragmentCirclePlayerBinding? = null
    private val binding get() = _binding!!

    private var rotateAnimator: ObjectAnimator? = null
    private var lastRequest: GlideRequest<Drawable>? = null

    private var progressAnimator: ObjectAnimator? = null
    var isSeeking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressViewUpdateHelper = MusicProgressViewUpdateHelper(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCirclePlayerBinding.bind(view)

        setupViews()
        binding.title.isSelected = true
        binding.album?.isSelected = true
        binding.artist?.isSelected = true

        binding.title.setOnClickListener {
            goToAlbum(requireActivity())
        }
        binding.artist?.setOnClickListener {
            goToArtist(requireActivity())
        }
        binding.songInfo.drawAboveSystemBars()

        setupRecyclerView()

        if (PreferenceUtil.isQueueHidden){
            binding.playerQueueSheet?.visibility = View.GONE
        }else{
            binding.playerQueueSheet?.visibility = View.VISIBLE
        }

        if (PreferenceUtil.queueShowAlways) {
            binding.playerQueueSheet?.visibility = View.VISIBLE
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        val song = MusicPlayerRemote.currentSong
        when (item.itemId) {
            R.id.action_playback_speed -> {
                PlaybackSpeedDialog.newInstance().show(childFragmentManager, "PLAYBACK_SETTINGS")
                return true
            }
            R.id.action_toggle_lyrics -> {
                PreferenceUtil.showLyrics = !PreferenceUtil.showLyrics
                showLyricsIcon(item)
                if (PreferenceUtil.lyricsScreenOn && PreferenceUtil.showLyrics) {
                    mainActivity.keepScreenOn(true)
                } else if (!PreferenceUtil.isScreenOnEnabled && !PreferenceUtil.showLyrics) {
                    mainActivity.keepScreenOn(false)
                }
                return true
            }
            R.id.action_go_to_lyrics -> {
                goToLyrics(requireActivity())
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
                if (ApexUtil.isTablet) {
                    val playerAlbumCoverFragment: PlayerAlbumCoverFragment =
                        whichFragment(R.id.playerAlbumCoverFragment)
                    if (binding.playerQueueSheet?.visibility == View.VISIBLE){
                        PreferenceUtil.isQueueHidden = true
                        binding.playerQueueSheet?.visibility = View.GONE
                        playerAlbumCoverFragment.updatePlayingQueue()
                        requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    }else{
                        PreferenceUtil.isQueueHidden = false
                        binding.playerQueueSheet?.visibility = View.VISIBLE
                        playerAlbumCoverFragment.updatePlayingQueue()
                    }
                }else {
                    requireActivity().findNavController(R.id.fragment_container).navigate(
                        R.id.playing_queue_fragment,
                        null,
                        navOptions { launchSingleTop = true }
                    )
                    mainActivity.collapsePanel()
                }
                return true
            }
            R.id.action_show_lyrics -> {
                goToLyrics(requireActivity())
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
        }
        return false
    }

    private fun setUpPlayerToolbar() {
        binding.playerToolbar.apply {
            inflateMenu(R.menu.menu_player)
            setNavigationOnClickListener { requireActivity().onBackPressed() }
            setOnMenuItemClickListener(this@CirclePlayerFragment)
            ToolbarContentTintHelper.colorizeToolbar(
                this,
                colorControlNormal(),
                requireActivity()
            )
        }
    }

    private fun setupViews() {
        setUpProgressSlider()
        binding.volumeSeekBar.circleProgressColor = accentColor()
        binding.volumeSeekBar.circleColor = ColorUtil.withAlpha(accentColor(), 0.25f)
        setUpPlayPauseFab()
        setUpPrevNext()
        setUpPlayerToolbar()
        binding.albumCoverOverlay.background = ColorDrawable(
            MaterialValueHelper.getPrimaryTextColor(
                requireContext(),
                accentColor().isColorLight
            )
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpPrevNext() {
        updatePrevNextColor()
        binding.nextButton.setOnTouchListener(MusicSeekSkipTouchListener(requireActivity(), true))
        binding.previousButton.setOnTouchListener(MusicSeekSkipTouchListener(requireActivity(),
            false))
    }

    private fun updatePrevNextColor() {
        val accentColor = accentColor()
        binding.nextButton.setColorFilter(accentColor, PorterDuff.Mode.SRC_IN)
        binding.previousButton.setColorFilter(accentColor, PorterDuff.Mode.SRC_IN)
    }

    private fun setUpPlayPauseFab() {
        TintHelper.setTintAuto(
            binding.playPauseButton,
            accentColor(),
            false
        )
        binding.playPauseButton.setOnClickListener(PlayPauseButtonOnClickHandler())
    }

    private fun setupRotateAnimation() {
        rotateAnimator = ObjectAnimator.ofFloat(binding.albumCover, View.ROTATION, 360F).apply {
            interpolator = LinearInterpolator()
            repeatCount = Animation.INFINITE
            duration = 10000
            if (MusicPlayerRemote.isPlaying) {
                start()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        progressViewUpdateHelper.start()
        if (audioVolumeObserver == null) {
            audioVolumeObserver = AudioVolumeObserver(requireActivity())
        }
        audioVolumeObserver?.register(AudioManager.STREAM_MUSIC, this)

        val audioManager = audioManager
        binding.volumeSeekBar.max =
            audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
        binding.volumeSeekBar.progress =
            audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
        binding.volumeSeekBar.setOnSeekBarChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        lastRequest = null
        progressViewUpdateHelper.stop()
    }

    override fun playerToolbar(): Toolbar {
        return binding.playerToolbar
    }

    override fun onShow() {
    }

    override fun onHide() {
    }

    override fun onBackPressed(): Boolean = false

    override fun toolbarIconColor(): Int =
        colorControlNormal()

    override val paletteColor: Int
        get() = Color.BLACK

    override fun onColorChanged(color: MediaNotificationProcessor) {
    }

    override fun onFavoriteToggled() {
    }

    override fun onPlayStateChanged() {
        updatePlayPauseDrawableState()
        if (MusicPlayerRemote.isPlaying) {
            if (rotateAnimator?.isStarted == true) rotateAnimator?.resume() else rotateAnimator?.start()
        } else {
            rotateAnimator?.pause()
        }
    }

    private fun setupRecyclerView() {
        playingQueueAdapter = PlayingQueueAdapter(
            requireActivity() as AppCompatActivity,
            MusicPlayerRemote.playingQueue.toMutableList(),
            MusicPlayerRemote.position,
            R.layout.item_queue_player
        )
        linearLayoutManager = LinearLayoutManager(requireContext())
        recyclerViewTouchActionGuardManager = RecyclerViewTouchActionGuardManager()
        recyclerViewDragDropManager = RecyclerViewDragDropManager()

        val animator = DraggableItemAnimator()
        animator.supportsChangeAnimations = false
        wrappedAdapter =
            recyclerViewDragDropManager?.createWrappedAdapter(playingQueueAdapter!!) as RecyclerView.Adapter<*>
        binding.recyclerView?.layoutManager = linearLayoutManager
        binding.recyclerView?.adapter = wrappedAdapter
        binding.recyclerView?.itemAnimator = animator
        binding.recyclerView?.let { recyclerViewTouchActionGuardManager?.attachRecyclerView(it) }
        binding.recyclerView?.let { recyclerViewDragDropManager?.attachRecyclerView(it) }

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
        binding.recyclerView?.stopScroll()
        linearLayoutManager.scrollToPositionWithOffset(MusicPlayerRemote.position + 1, 0)
    }

    override fun onQueueChanged() {
        super.onQueueChanged()
        updateQueue()
    }

    override fun onPlayingMetaChanged() {
        super.onPlayingMetaChanged()
        updateSong()
        updateQueuePosition()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        updateSong()
        updatePlayPauseDrawableState()
        setupRotateAnimation()
        updateQueue()
    }

    private fun updateSong() {
        val song = MusicPlayerRemote.currentSong
        binding.title.text = song.title
        binding.album?.text = song.albumName
        binding.artist?.text = song.artistName

        if (PreferenceUtil.isSongInfo) {
            binding.songInfo.text = getSongInfo(song)
            binding.songInfo.show()
            binding.songInfo.isSelected = true
        } else {
            binding.songInfo.hide()
        }
        GlideApp.with(this)
            .load(ApexGlideExtension.getSongModel(MusicPlayerRemote.currentSong))
            .simpleSongCoverOptions(MusicPlayerRemote.currentSong)
            .thumbnail(lastRequest)
            .error(GlideApp.with(this).load(R.drawable.default_audio_art).fitCenter())
            .fitCenter().also {
                lastRequest = it.clone()
                it.crossfadeListener()
                    .into(binding.albumCover)
            }
    }

    private fun updatePlayPauseDrawableState() {
        when {
            MusicPlayerRemote.isPlaying -> binding.playPauseButton.setImageResource(R.drawable.ic_pause)
            else -> binding.playPauseButton.setImageResource(R.drawable.ic_play_arrow)
        }
    }

    override fun onAudioVolumeChanged(currentVolume: Int, maxVolume: Int) {
        _binding?.volumeSeekBar?.max = maxVolume.toFloat()
        _binding?.volumeSeekBar?.progress = currentVolume.toFloat()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (audioVolumeObserver != null) {
            audioVolumeObserver!!.unregister()
        }
        _binding = null
    }


    override fun onProgressChanged(
        circularSeekBar: CircularSeekBar?,
        progress: Float,
        fromUser: Boolean,
    ) {
        val audioManager = audioManager
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress.toInt(), 0)
    }

    override fun onStartTrackingTouch(seekBar: CircularSeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: CircularSeekBar?) {
    }

    private fun setUpProgressSlider() {
        binding.progressSlider.applyColor(accentColor())
        val progressSlider = binding.progressSlider
        progressSlider.addOnChangeListener(Slider.OnChangeListener { _, value, fromUser ->
            if (fromUser) {
                onUpdateProgressViews(
                    value.toInt(),
                    MusicPlayerRemote.songDurationMillis
                )
            }
        })
        progressSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                isSeeking = true
                progressViewUpdateHelper.stop()
            }

            override fun onStopTrackingTouch(slider: Slider) {
                isSeeking = false
                MusicPlayerRemote.seekTo(slider.value.toInt())
                progressViewUpdateHelper.start()
            }
        })
    }

    override fun onUpdateProgressViews(progress: Int, total: Int) {
        val progressSlider = binding.progressSlider
        progressSlider.valueTo = total.toFloat()

        progressSlider.valueTo = total.toFloat()

        progressSlider.value =
            progress.toFloat().coerceIn(progressSlider.valueFrom, progressSlider.valueTo)

        binding.songTotalTime.text = MusicUtil.getReadableDurationString(total.toLong())
        binding.songCurrentProgress.text = MusicUtil.getReadableDurationString(progress.toLong())
    }
}
