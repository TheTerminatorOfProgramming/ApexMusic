package com.ttop.app.apex.service

import android.animation.Animator
import android.animation.ValueAnimator
import android.media.MediaPlayer
import androidx.core.animation.doOnEnd
import com.ttop.app.apex.service.playback.Playback
import com.ttop.app.apex.util.PreferenceUtil

class AudioFader {
    companion object {
        fun createFadeAnimator(
            fadeInMp: MediaPlayer,
            fadeOutMp: MediaPlayer,
            endAction: (animator: Animator) -> Unit, /* Code to run when Animator Ends*/
        ): Animator? {
            val duration = PreferenceUtil.crossFadeDuration * 1000
            if (duration == 0) {
                return null
            }
            return ValueAnimator.ofFloat(0f, 1f).apply {
                this.duration = duration.toLong()
                addUpdateListener { animation: ValueAnimator ->
                    fadeInMp.setVolume(
                        animation.animatedValue as Float, animation.animatedValue as Float
                    )
                    fadeOutMp.setVolume(
                        1 - animation.animatedValue as Float,
                        1 - animation.animatedValue as Float
                    )
                }
                doOnEnd {
                    endAction(it)
                }
            }
        }

        fun startFadeAnimator(
            playback: Playback,
            fadeIn: Boolean, /* fadeIn -> true  fadeOut -> false*/
            callback: Runnable? = null, /* Code to run when Animator Ends*/
        ) {
            val duration = PreferenceUtil.audioFadeDuration.toLong()
            if (duration == 0L) {
                callback?.run()
                return
            }
            val startValue = if (fadeIn) 0f else 1.0f
            val endValue = if (fadeIn) 1.0f else 0f
            val animator = ValueAnimator.ofFloat(startValue, endValue)
            animator.duration = duration
            animator.addUpdateListener { animation: ValueAnimator ->
                playback.setVolume(animation.animatedValue as Float)
            }
            animator.doOnEnd {
                callback?.run()
            }
            animator.start()
        }
    }
}