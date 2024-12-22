package com.ttop.app.apex.ui.fragments

import android.annotation.SuppressLint
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.ttop.app.apex.helper.MusicPlayerRemote
import com.ttop.app.apex.util.PreferenceUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

/**
 * @param activity, Activity
 * @param next, if the button is next, if false then it's considered previous
 */
class MusicSeekSkipTouchListener(val activity: FragmentActivity, val next: Boolean) :
    View.OnTouchListener {

    private var job: Job? = null
    private var counter = 0
    private var wasSeeking = false

    private var startX = 0f
    private var startY = 0f

    private val scaledTouchSlop = ViewConfiguration.get(activity).scaledTouchSlop

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                job = activity.lifecycleScope.launch(Dispatchers.Default) {
                    counter = 0
                    while (isActive) {
                        delay(500)
                        wasSeeking = true

                        var seekingDuration = MusicPlayerRemote.songProgressMillis
                        if (next) {
                            seekingDuration += (10 * 1000) * (counter.floorDiv(
                                2
                            ) + 1)
                            if (!PreferenceUtil.isHapticFeedbackDisabled) {
                                v?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            }
                        } else {
                            seekingDuration -= (10 * 1000) * (counter.floorDiv(
                                2
                            ) + 1)
                            v?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        }
                        withContext(Dispatchers.Main) {
                            MusicPlayerRemote.seekTo(seekingDuration)
                        }
                        counter = 1
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                job?.cancel()
                val endX = event.x
                val endY = event.y
                if (!wasSeeking && isAClick(startX, endX, startY, endY)) {
                    if (next) {
                        MusicPlayerRemote.playNextSongAuto(MusicPlayerRemote.isPlaying)
                    } else {
                        MusicPlayerRemote.playPreviousSongAuto(MusicPlayerRemote.isPlaying)
                    }
                }

                wasSeeking = false
            }

            MotionEvent.ACTION_CANCEL -> {
                job?.cancel()
            }
        }
        return false
    }

    private fun isAClick(startX: Float, endX: Float, startY: Float, endY: Float): Boolean {
        val differenceX = abs(startX - endX)
        val differenceY = abs(startY - endY)
        return !(differenceX > scaledTouchSlop || differenceY > scaledTouchSlop)
    }
}