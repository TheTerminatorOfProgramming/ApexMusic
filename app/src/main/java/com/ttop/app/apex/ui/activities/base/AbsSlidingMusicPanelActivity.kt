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
package com.ttop.app.apex.ui.activities.base

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.core.animation.doOnEnd
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.commit
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_DRAGGING
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_SETTLING
import com.google.android.material.bottomsheet.BottomSheetBehavior.from
import com.ttop.app.apex.ALBUM_COVER_STYLE
import com.ttop.app.apex.ALBUM_COVER_TRANSFORM
import com.ttop.app.apex.AUTO_ROTATE
import com.ttop.app.apex.CAROUSEL_EFFECT
import com.ttop.app.apex.KEEP_SCREEN_ON
import com.ttop.app.apex.LIBRARY_CATEGORIES
import com.ttop.app.apex.NOW_PLAYING_SCREEN_ID
import com.ttop.app.apex.R
import com.ttop.app.apex.SCREEN_ON_LYRICS
import com.ttop.app.apex.SWIPE_ANYWHERE_NOW_PLAYING
import com.ttop.app.apex.SWIPE_ANYWHERE_NOW_PLAYING_NON_FOLDABLE
import com.ttop.app.apex.TAB_TEXT_MODE
import com.ttop.app.apex.TOGGLE_ADD_CONTROLS
import com.ttop.app.apex.databinding.SlidingMusicPanelLayoutBinding
import com.ttop.app.apex.extensions.accentColor
import com.ttop.app.apex.extensions.currentFragment
import com.ttop.app.apex.extensions.darkAccentColor
import com.ttop.app.apex.extensions.dip
import com.ttop.app.apex.extensions.getBottomInsets
import com.ttop.app.apex.extensions.hide
import com.ttop.app.apex.extensions.isColorLight
import com.ttop.app.apex.extensions.keepScreenOn
import com.ttop.app.apex.extensions.maybeSetScreenOn
import com.ttop.app.apex.extensions.peekHeightAnimate
import com.ttop.app.apex.extensions.setLightStatusBar
import com.ttop.app.apex.extensions.setLightStatusBarAuto
import com.ttop.app.apex.extensions.setTaskDescriptionColor
import com.ttop.app.apex.extensions.show
import com.ttop.app.apex.extensions.whichFragment
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.model.CategoryInfo
import com.ttop.app.apex.ui.activities.AppIntroActivity
import com.ttop.app.apex.ui.fragments.LibraryViewModel
import com.ttop.app.apex.ui.fragments.NowPlayingScreen
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.Adaptive
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.Blur
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.Card
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.Gradient
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.Classic
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.Peek
import com.ttop.app.apex.ui.fragments.NowPlayingScreen.Minimal
import com.ttop.app.apex.ui.fragments.base.AbsPlayerFragment
import com.ttop.app.apex.ui.fragments.other.MiniPlayerFragment
import com.ttop.app.apex.ui.fragments.player.adaptive.AdaptiveFragment
import com.ttop.app.apex.ui.fragments.player.blur.BlurPlayerFragment
import com.ttop.app.apex.ui.fragments.player.card.CardFragment
import com.ttop.app.apex.ui.fragments.player.gradient.GradientPlayerFragment
import com.ttop.app.apex.ui.fragments.player.classic.ClassicPlayerFragment
import com.ttop.app.apex.ui.fragments.player.peek.PeekPlayerFragment
import com.ttop.app.apex.ui.fragments.player.minimal.TinyPlayerFragment
import com.ttop.app.apex.ui.fragments.queue.PlayingQueueFragment
import com.ttop.app.apex.util.ApexUtil
import com.ttop.app.apex.util.IntroPrefs
import com.ttop.app.apex.util.PreferenceUtil
import com.ttop.app.apex.util.logD
import org.koin.androidx.viewmodel.ext.android.viewModel


abstract class AbsSlidingMusicPanelActivity : AbsMusicServiceActivity(),
    SharedPreferences.OnSharedPreferenceChangeListener {
    companion object {
        val TAG: String = AbsSlidingMusicPanelActivity::class.java.simpleName
    }

    var fromNotification = false
    private var windowInsets: WindowInsetsCompat? = null
    protected val libraryViewModel by viewModel<LibraryViewModel>()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>
    private lateinit var playerFragment: AbsPlayerFragment
    private var miniPlayerFragment: MiniPlayerFragment? = null
    private var nowPlayingScreen: NowPlayingScreen? = null
    private var taskColor: Int = 0
    private var paletteColor: Int = Color.WHITE

    private var panelStateBefore: Int? = null
    private var panelStateCurrent: Int? = null
    private val panelState: Int
        get() = bottomSheetBehavior.state
    private lateinit var binding: SlidingMusicPanelLayoutBinding
    private var isInOneTabMode = false

    private var navigationBarColorAnimator: ValueAnimator? = null
    private val argbEvaluator: ArgbEvaluator = ArgbEvaluator()

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
                if(handleBackPress()){
                    return
                }
                val navHostFragment  =
                    supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
                if(!navHostFragment.navController.navigateUp()){
                    finish()
                }
            }
        }

    private val bottomSheetCallbackList by lazy {
        object : BottomSheetBehavior.BottomSheetCallback() {
            var oldOffSet = 0f
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                setMiniPlayerAlphaProgress(slideOffset)
                navigationBarColorAnimator?.cancel()

                val inRangeExpanding = oldOffSet < slideOffset
                val inRangeCollapsing = oldOffSet > slideOffset
                oldOffSet = slideOffset

                if (inRangeCollapsing) {
                    if (PreferenceUtil.isMiniPlayerTransparent) {
                        binding.slidingPanel.alpha = 0.7f
                    }
                    window.navigationBarColor = darkAccentColor()
                }

                if (inRangeExpanding) {
                    if (PreferenceUtil.isMiniPlayerTransparent) {
                        binding.slidingPanel.alpha = 1f
                    }

                    when (PreferenceUtil.nowPlayingScreen) {
                        Adaptive -> {
                            if (PreferenceUtil.isAdaptiveColor) {
                                window.navigationBarColor = paletteColor
                            }else {
                                window.navigationBarColor = accentColor()
                            }
                        }
                        Blur -> {
                            window.navigationBarColor = Color.BLACK
                        }
                        Card -> {
                            if (PreferenceUtil.isAdaptiveColor) {
                                window.navigationBarColor = paletteColor
                            }else {
                                window.navigationBarColor = accentColor()
                            }
                        }
                        Classic -> {
                            if (PreferenceUtil.isAdaptiveColor) {
                                window.navigationBarColor = paletteColor
                            }else {
                                window.navigationBarColor = accentColor()
                            }
                        }
                        Gradient -> {
                            window.navigationBarColor = paletteColor
                        }
                        Minimal -> {
                            window.navigationBarColor = paletteColor
                        }
                        Peek -> {
                            if (PreferenceUtil.isAdaptiveColor) {
                                window.navigationBarColor = paletteColor
                            }else {
                                window.navigationBarColor = accentColor()
                            }
                        }
                    }
                }
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if(panelStateCurrent != null){
                    panelStateBefore = panelStateCurrent
                }
                panelStateCurrent = newState
                when (newState) {
                    STATE_EXPANDED -> {
                        onPanelExpanded()
                        if (PreferenceUtil.lyricsScreenOn && PreferenceUtil.showLyrics) {
                            keepScreenOn(true)
                        }

                        if (PreferenceUtil.lyricsScreenOn) {
                            keepScreenOn(true)
                        }
                    }
                    STATE_COLLAPSED -> {
                        onPanelCollapsed()
                        if ((PreferenceUtil.lyricsScreenOn && PreferenceUtil.showLyrics) || !PreferenceUtil.isScreenOnEnabled) {
                            keepScreenOn(false)
                        }

                        if ((PreferenceUtil.lyricsScreenOn) || !PreferenceUtil.isScreenOnEnabled) {
                            keepScreenOn(false)
                        }

                        if (PreferenceUtil.isWidgetPanel && ApexUtil.isTablet) {
                            PreferenceUtil.isWidgetPanel = false
                            postRecreate()
                        }
                    }
                    STATE_SETTLING, STATE_DRAGGING -> {
                        if (fromNotification) {
                            binding.navigationView.bringToFront()
                            fromNotification = false
                        }
                    }
                    STATE_HIDDEN -> {
                        MusicPlayerRemote.clearQueue()
                        window.navigationBarColor = darkAccentColor()
                    }
                    else -> {
                        logD("Do a flip")
                    }
                }
            }
        }
    }

    fun getBottomSheetBehavior() = bottomSheetBehavior

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!hasPermissions()) {
            startActivity(
                Intent(
                    this@AbsSlidingMusicPanelActivity,
                    AppIntroActivity::class.java
                )
            )
            finish()
        } else {
            if (!IntroPrefs(applicationContext).hasIntroSlidesShown) {
                startActivity(
                    Intent(
                        this@AbsSlidingMusicPanelActivity,
                        AppIntroActivity::class.java
                    )
                )
                finish()
            }
        }

        binding = SlidingMusicPanelLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.setOnApplyWindowInsetsListener { _, insets ->
            windowInsets = WindowInsetsCompat.toWindowInsetsCompat(insets)
            insets
        }
        chooseFragmentForTheme()
        setupSlidingUpPanel()
        setupBottomSheet()
        updateColor()

        binding.slidingPanel.backgroundTintList = ColorStateList.valueOf(darkAccentColor())
        navigationView.backgroundTintList = ColorStateList.valueOf(darkAccentColor())
        binding.slidingPanel.alpha = 1f

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        window.navigationBarColor = darkAccentColor()
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior = from(binding.slidingPanel)
        bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallbackList)
        bottomSheetBehavior.isHideable = true
        bottomSheetBehavior.significantVelocityThreshold = 300
        setMiniPlayerAlphaProgress(0F)
    }

    override fun onResume() {
        super.onResume()
        PreferenceUtil.registerOnSharedPreferenceChangedListener(this)
        if (nowPlayingScreen != PreferenceUtil.nowPlayingScreen) {
            postRecreate()
        }

        if (bottomSheetBehavior.state == STATE_EXPANDED) {
            setMiniPlayerAlphaProgress(1f)
            if (PreferenceUtil.isMiniPlayerTransparent) {
                binding.slidingPanel.alpha = 1f
            }

            when (PreferenceUtil.nowPlayingScreen) {
                Adaptive -> {
                    if (PreferenceUtil.isAdaptiveColor) {
                        window.navigationBarColor = paletteColor
                    }else {
                        window.navigationBarColor = accentColor()
                    }
                }
                Blur -> {
                    window.navigationBarColor = Color.BLACK
                }
                Card -> {
                    if (PreferenceUtil.isAdaptiveColor) {
                        window.navigationBarColor = paletteColor
                    }else {
                        window.navigationBarColor = accentColor()
                    }
                }
                Classic -> {
                    if (PreferenceUtil.isAdaptiveColor) {
                        window.navigationBarColor = paletteColor
                    }else {
                        window.navigationBarColor = accentColor()
                    }
                }
                Gradient -> {
                    window.navigationBarColor = paletteColor
                }
                Minimal -> {
                    window.navigationBarColor = paletteColor
                }
                Peek -> {
                    if (PreferenceUtil.isAdaptiveColor) {
                        window.navigationBarColor = paletteColor
                    }else {
                        window.navigationBarColor = accentColor()
                    }
                }
            }
        }

        if (bottomSheetBehavior.state == STATE_COLLAPSED) {
            window.navigationBarColor = darkAccentColor()
        }

        if (bottomSheetBehavior.state == STATE_HIDDEN) {
            window.navigationBarColor = darkAccentColor()
        }

        if (PreferenceUtil.isMiniPlayerTransparent) {
            if (bottomSheetBehavior.state == STATE_COLLAPSED) {
                binding.slidingPanel.alpha = 0.7f
            }
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!handleBackPress()) {
                    remove()
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        bottomSheetBehavior.removeBottomSheetCallback(bottomSheetCallbackList)
        PreferenceUtil.unregisterOnSharedPreferenceChangedListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            TOGGLE_ADD_CONTROLS -> {
                miniPlayerFragment?.setUpButtons()
            }
            NOW_PLAYING_SCREEN_ID -> {
                chooseFragmentForTheme()
                binding.slidingPanel.updateLayoutParams<ViewGroup.LayoutParams> {
                    height = if (nowPlayingScreen != Peek) {
                        ViewGroup.LayoutParams.MATCH_PARENT
                    } else {
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    }
                    onServiceConnected()
                }
            }
            ALBUM_COVER_TRANSFORM, CAROUSEL_EFFECT,
            ALBUM_COVER_STYLE,
            -> {
                chooseFragmentForTheme()
                onServiceConnected()
            }
            SWIPE_ANYWHERE_NOW_PLAYING -> {
                playerFragment.addSwipeDetector()
            }
            SWIPE_ANYWHERE_NOW_PLAYING_NON_FOLDABLE -> {
                playerFragment.addSwipeDetectorNonFoldable()
            }
            LIBRARY_CATEGORIES -> {
                updateTabs()
                refreshTabs()
            }
            TAB_TEXT_MODE -> {
                navigationView.labelVisibilityMode = PreferenceUtil.tabTitleMode
            }

            SCREEN_ON_LYRICS -> {
                when (PreferenceUtil.lyricsMode) {
                    "id3" -> {
                        keepScreenOn(bottomSheetBehavior.state == STATE_EXPANDED && PreferenceUtil.lyricsScreenOn || PreferenceUtil.isScreenOnEnabled)
                    }
                    "synced" -> {
                        keepScreenOn(bottomSheetBehavior.state == STATE_EXPANDED && PreferenceUtil.lyricsScreenOn && PreferenceUtil.showLyrics || PreferenceUtil.isScreenOnEnabled)
                    }
                    "both" -> {
                        keepScreenOn(bottomSheetBehavior.state == STATE_EXPANDED && PreferenceUtil.lyricsScreenOn && PreferenceUtil.showLyrics || bottomSheetBehavior.state == STATE_EXPANDED && PreferenceUtil.lyricsScreenOn || PreferenceUtil.isScreenOnEnabled)
                    }
                    "disabled" -> {
                        keepScreenOn(PreferenceUtil.isScreenOnEnabled)
                    }
                }
            }
            KEEP_SCREEN_ON -> {
                maybeSetScreenOn()
            }
            AUTO_ROTATE -> {
                requestedOrientation = if (ApexUtil.isTablet) {
                    if (PreferenceUtil.isAutoRotate) {
                        ActivityInfo.SCREEN_ORIENTATION_SENSOR
                    } else {
                        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    }
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
            }
        }
    }

    fun collapsePanel() {
        bottomSheetBehavior.state = STATE_COLLAPSED
    }

    fun expandPanel() {
        bottomSheetBehavior.state = STATE_EXPANDED
    }

    private fun setMiniPlayerAlphaProgress(progress: Float) {
        if (progress < 0) return
        val alpha = 1 - progress
        miniPlayerFragment?.view?.alpha = 1 - (progress / 0.2F)
        miniPlayerFragment?.view?.isGone = alpha == 0f
        binding.navigationView.translationY = progress * 500
        binding.navigationView.alpha = alpha
        //if (!isLandscape) {
       //     binding.navigationView.translationY = progress * 500
        //    binding.navigationView.alpha = alpha
        //}
        binding.playerFragmentContainer.alpha = (progress - 0.2F) / 0.2F
    }

    open fun onPanelCollapsed() {
        setMiniPlayerAlphaProgress(0F)
        // restore values
        setLightStatusBarAuto()
        setTaskDescriptionColor(taskColor)
        //playerFragment?.onHide()
        if (PreferenceUtil.isMiniPlayerTransparent) {
            binding.slidingPanel.alpha = 0.7f
        }

        window?.navigationBarColor = darkAccentColor()
    }

    open fun onPanelExpanded() {
        setMiniPlayerAlphaProgress(1F)
        onPaletteColorChanged()
        //playerFragment?.onShow()
        if (PreferenceUtil.isMiniPlayerTransparent) {
            binding.slidingPanel.alpha = 1f
        }

        when (PreferenceUtil.nowPlayingScreen) {
            Adaptive -> {
                if (PreferenceUtil.isAdaptiveColor) {
                    window.navigationBarColor = paletteColor
                }else {
                    window.navigationBarColor = accentColor()
                }
            }
            Blur -> {
                window.navigationBarColor = Color.BLACK
            }
            Card -> {
                if (PreferenceUtil.isAdaptiveColor) {
                    window.navigationBarColor = paletteColor
                }else {
                    window.navigationBarColor = accentColor()
                }
            }
            Classic -> {
                if (PreferenceUtil.isAdaptiveColor) {
                    window.navigationBarColor = paletteColor
                }else {
                    window.navigationBarColor = accentColor()
                }
            }
            Gradient -> {
                window.navigationBarColor = paletteColor
            }
            Minimal -> {
                window.navigationBarColor = paletteColor
            }
            Peek -> {
                if (PreferenceUtil.isAdaptiveColor) {
                    window.navigationBarColor = paletteColor
                }else {
                    window.navigationBarColor = accentColor()
                }
            }
        }
    }

    private fun setupSlidingUpPanel() {
        binding.slidingPanel.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.slidingPanel.viewTreeObserver.removeOnGlobalLayoutListener(this)
                if (nowPlayingScreen != Peek) {
                    binding.slidingPanel.updateLayoutParams<ViewGroup.LayoutParams> {
                        height = ViewGroup.LayoutParams.MATCH_PARENT
                    }
                }

                when (panelState) {
                    STATE_EXPANDED -> onPanelExpanded()
                    STATE_COLLAPSED -> onPanelCollapsed()
                    else -> {
                        // playerFragment!!.onHide()
                    }
                }
            }
        })
    }

    val navigationView get() = binding.navigationView

    val slidingPanel get() = binding.slidingPanel

    val isBottomNavVisible get() = navigationView.isVisible && navigationView is BottomNavigationView

    override fun onServiceConnected() {
        super.onServiceConnected()
        hideBottomSheet(false)
    }

    fun refreshTabs() {
        binding.navigationView.menu.getItem(1).isChecked = true
        binding.navigationView.menu.getItem(0).isChecked = true
    }

    override fun onQueueChanged() {
        super.onQueueChanged()
        // Mini player should be hidden in Playing Queue
        // it may pop up if hideBottomSheet is called
        if (currentFragment(R.id.fragment_container) !is PlayingQueueFragment) {
            hideBottomSheet(MusicPlayerRemote.playingQueue.isEmpty())
        }
    }

    private fun handleBackPress(): Boolean {
        if (bottomSheetBehavior.peekHeight != 0 && playerFragment.onBackPressed()) return true
        if (panelState == STATE_EXPANDED || (panelState == STATE_SETTLING && panelStateBefore != STATE_EXPANDED)) {
            collapsePanel()
            return true
        }
        return false
    }

    private fun onPaletteColorChanged() {
        if (panelState == STATE_EXPANDED) {
            setTaskDescColor(paletteColor)
            val isColorLight = paletteColor.isColorLight
            when (PreferenceUtil.nowPlayingScreen) {
                Adaptive -> {
                    if (PreferenceUtil.isAdaptiveColor) {
                        window.navigationBarColor = paletteColor
                    }else {
                        window.navigationBarColor = accentColor()
                    }
                }
                Blur -> {
                    window.navigationBarColor = Color.BLACK
                    setLightStatusBar(false)
                }
                Card -> {
                    if (PreferenceUtil.isAdaptiveColor) {
                        window.navigationBarColor = paletteColor
                    }else {
                        window.navigationBarColor = accentColor()
                    }
                    setLightStatusBar(false)
                }
                Classic -> {
                    if (PreferenceUtil.isAdaptiveColor) {
                        window.navigationBarColor = paletteColor
                    }else {
                        window.navigationBarColor = accentColor()
                    }
                    setLightStatusBar(isColorLight)
                }
                Gradient -> {
                    window.navigationBarColor = paletteColor
                    setLightStatusBar(isColorLight)
                }
                Minimal -> {
                    window.navigationBarColor = paletteColor
                    setLightStatusBar(isColorLight)
                }
                Peek -> {
                    if (PreferenceUtil.isAdaptiveColor) {
                        window.navigationBarColor = paletteColor
                    }else {
                        window.navigationBarColor = accentColor()
                    }
                }
            }
        }
    }

    private fun setTaskDescColor(color: Int) {
        taskColor = color
        if (panelState == STATE_COLLAPSED) {
            setTaskDescriptionColor(color)
        }
    }

    fun updateTabs() {
        binding.navigationView.menu.clear()
        val currentTabs: List<CategoryInfo> = PreferenceUtil.libraryCategory
        for (tab in currentTabs) {
            if (tab.visible) {
                val menu = tab.category
                binding.navigationView.menu.add(0, menu.id, 0, menu.stringRes)
                    .setIcon(menu.icon)
            }
        }
        if (binding.navigationView.menu.size() == 1) {
            isInOneTabMode = true
            binding.navigationView.isVisible = false
        } else {
            isInOneTabMode = false
        }
    }

    private fun updateColor() {
        libraryViewModel.paletteColor.observe(this) { color ->
            this.paletteColor = color
            onPaletteColorChanged()
        }
    }

    fun setBottomNavVisibility(
        visible: Boolean,
        animate: Boolean = false,
        hideBottomSheet: Boolean = MusicPlayerRemote.playingQueue.isEmpty(),
    ) {
        if (isInOneTabMode) {
            hideBottomSheet(
                hide = hideBottomSheet,
                animate = animate,
                isBottomNavVisible = false
            )
            return
        }
        if (visible xor navigationView.isVisible) {
            val mAnimate = animate && bottomSheetBehavior.state == STATE_COLLAPSED
            if (mAnimate) {
                if (visible) {
                    binding.navigationView.bringToFront()
                    binding.navigationView.show()
                } else {
                    binding.navigationView.hide()
                }
            } else {
                binding.navigationView.isVisible = visible
                if (visible && bottomSheetBehavior.state != STATE_EXPANDED) {
                    binding.navigationView.bringToFront()
                }
            }
        }
        hideBottomSheet(
            hide = hideBottomSheet,
            animate = animate,
            isBottomNavVisible = visible && navigationView is BottomNavigationView
        )
    }

    fun hideBottomSheet(
        hide: Boolean,
        animate: Boolean = false,
        isBottomNavVisible: Boolean = navigationView.isVisible && navigationView is BottomNavigationView,
    ) {
        val heightOfBar = windowInsets.getBottomInsets() + dip(R.dimen.mini_player_height)

        val heightOfBarWithTabs = heightOfBar + dip(R.dimen.bottom_nav_height)
        if (hide) {
            bottomSheetBehavior.peekHeight = -windowInsets.getBottomInsets()
            bottomSheetBehavior.state = STATE_COLLAPSED
            libraryViewModel.setFabMargin(
                this,
                if (isBottomNavVisible) dip(R.dimen.bottom_nav_height) else 0
            )
        } else {
            if (MusicPlayerRemote.playingQueue.isNotEmpty()) {
                binding.slidingPanel.elevation = 0F
                binding.navigationView.elevation = 5F
                if (isBottomNavVisible) {
                    logD("List")
                    if (animate) {
                        bottomSheetBehavior.peekHeightAnimate(heightOfBarWithTabs)
                    } else {
                        bottomSheetBehavior.peekHeight = heightOfBarWithTabs
                    }
                    libraryViewModel.setFabMargin(
                        this,
                        dip(R.dimen.bottom_nav_mini_player_height)
                    )
                } else {
                    logD("Details")
                    if (animate) {
                        bottomSheetBehavior.peekHeightAnimate(heightOfBar).doOnEnd {
                            binding.slidingPanel.bringToFront()
                        }
                    } else {
                        bottomSheetBehavior.peekHeight = heightOfBar
                        binding.slidingPanel.bringToFront()
                    }
                    libraryViewModel.setFabMargin(this, dip(R.dimen.mini_player_height))
                }
            }
        }
    }

    fun setAllowDragging(allowDragging: Boolean) {
        bottomSheetBehavior.isDraggable = allowDragging
        hideBottomSheet(false)
    }

    private fun chooseFragmentForTheme() {
        nowPlayingScreen = PreferenceUtil.nowPlayingScreen

        val fragment: AbsPlayerFragment = when (nowPlayingScreen) {
            Blur -> BlurPlayerFragment()
            Adaptive -> AdaptiveFragment()
            Card -> CardFragment()
            Classic -> ClassicPlayerFragment()
            Gradient -> GradientPlayerFragment()
            Minimal -> TinyPlayerFragment()
            Peek -> PeekPlayerFragment()
            else -> ClassicPlayerFragment()
        } // must extend AbsPlayerFragment
        supportFragmentManager.commit(allowStateLoss = true) {
            replace(R.id.playerFragmentContainer, fragment)
        }
        supportFragmentManager.executePendingTransactions()
        playerFragment = whichFragment(R.id.playerFragmentContainer)
        miniPlayerFragment = whichFragment<MiniPlayerFragment>(R.id.miniPlayerFragment)
    }
}