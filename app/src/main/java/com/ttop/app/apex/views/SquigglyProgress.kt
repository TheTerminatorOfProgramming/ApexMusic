package com.ttop.app.apex.views

/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.SystemClock
import androidx.annotation.VisibleForTesting
import com.ttop.app.apex.libraries.appthemehelper.util.ATHColorUtil
import kotlin.math.abs
import kotlin.math.cos


private const val TWO_PI = (Math.PI * 2f).toFloat()

@VisibleForTesting
internal const val DISABLED_ALPHA = 77

class SquigglyProgress : Drawable() {

    private val wavePaint = Paint()
    private val linePaint = Paint()
    private val path = Path()
    private var heightFraction = 1f
    private var heightAnimator: ValueAnimator? = null
    private var phaseOffset = 0f
    private var lastFrameTime = -1L

    /* distance over which amplitude drops to zero, measured in wavelengths */
    private val transitionPeriods = 1.5f

    /* wave endpoint as percentage of bar when play position is zero */
    private val minWaveEndpoint = 0f

    /* wave endpoint as percentage of bar when play position matches wave endpoint */
    private val matchedWaveEndpoint = 0f

    // Horizontal length of the sine wave
    var waveLength = 80f

    // Height of each peak of the sine wave
    var lineAmplitude = 6f

    // Line speed in px per second
    var phaseSpeed = 16f

    // Progress stroke width, both for wave and solid line
    var strokeWidth = 8f
        set(value) {
            if (field == value) {
                return
            }
            field = value
            wavePaint.strokeWidth = value
            linePaint.strokeWidth = value
        }

    // Enables a transition region where the amplitude
    // of the wave is reduced linearly across it.
    var transitionEnabled = true
        set(value) {
            field = value
            invalidateSelf()
        }

    init {
        wavePaint.strokeCap = Paint.Cap.ROUND
        linePaint.strokeCap = Paint.Cap.ROUND
        wavePaint.strokeWidth = strokeWidth
        linePaint.strokeWidth = strokeWidth
        linePaint.style = Paint.Style.STROKE
        wavePaint.style = Paint.Style.STROKE
        linePaint.alpha = DISABLED_ALPHA
    }

    var animate: Boolean = true
        set(value) {
            if (field == value) {
                return
            }
            field = value
            if (field) {
                lastFrameTime = SystemClock.uptimeMillis()
            }
            heightAnimator?.cancel()
            heightAnimator =
                ValueAnimator.ofFloat(heightFraction, if (animate) 1f else 0f).apply {
                    if (animate) {
                        startDelay = 60
                        duration = 800
//                        interpolator = Interpolators.EMPHASIZED_DECELERATE
                    } else {
                        duration = 550
//                        interpolator = Interpolators.STANDARD_DECELERATE
                    }
                    addUpdateListener {
                        heightFraction = it.animatedValue as Float
                        invalidateSelf()
                    }
                    addListener(
                        object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                heightAnimator = null
                            }
                        }
                    )
                    start()
                }
        }

    override fun draw(canvas: Canvas) {
        if (animate) {
            invalidateSelf()
            val now = SystemClock.uptimeMillis()
            phaseOffset += (now - lastFrameTime) / 1000f * phaseSpeed
            phaseOffset %= waveLength
            lastFrameTime = now
        }

        val progress = level / 10_000f
        var totalWidth = bounds.width().toFloat()

        if (transitionEnabled) {
            totalWidth -= transitionPeriods * waveLength
        }
        val totalProgressPx = bounds.width().toFloat() * progress
        val waveProgressPx =
            totalWidth *
                    (if (!transitionEnabled || progress > matchedWaveEndpoint) progress
                    else
                        lerp(
                            minWaveEndpoint,
                            matchedWaveEndpoint,
                            lerpInv(0f, matchedWaveEndpoint, progress)
                        ))

        // Build Wiggly Path
        val waveStart = -phaseOffset - waveLength / 2f
        val waveEnd = if (transitionEnabled) bounds.width().toFloat() else waveProgressPx

        // helper function, computes amplitude for wave segment
        val computeAmplitude: (Float, Float) -> Float = { x, sign ->
            if (transitionEnabled) {
                val length = transitionPeriods * waveLength
                val coeff =
                    lerpInvSat(waveProgressPx + length / 2f, waveProgressPx - length / 2f, x)
                sign * heightFraction * lineAmplitude * coeff
            } else {
                sign * heightFraction * lineAmplitude
            }
        }

        // Reset path object to the start
        path.rewind()
        path.moveTo(waveStart, 0f)

        // Build the wave, incrementing by half the wavelength each time
        var currentX = waveStart
        var waveSign = 1f
        var currentAmp = computeAmplitude(currentX, waveSign)
        val dist = waveLength / 2f
        while (currentX < waveEnd) {
            waveSign = -waveSign
            val nextX = currentX + dist
            val midX = currentX + dist / 2
            val nextAmp = computeAmplitude(nextX, waveSign)
            path.cubicTo(midX, currentAmp, midX, nextAmp, nextX, nextAmp)
            currentAmp = nextAmp
            currentX = nextX
        }

        // translate to the start position of the progress bar for all draw commands
        val clipTop = lineAmplitude + strokeWidth
        canvas.save()
        canvas.translate(bounds.left.toFloat(), bounds.centerY().toFloat())

        // Draw path up to progress position
        canvas.save()
        canvas.clipRect(0f, -1f * clipTop, totalProgressPx, clipTop)
        canvas.drawPath(path, wavePaint)
        canvas.restore()

        if (transitionEnabled) {
            // If there's a smooth transition, we draw the rest of the
            // path in a different color (using different clip params)
            canvas.save()
            canvas.clipRect(totalProgressPx, -1f * clipTop, bounds.width().toFloat(), clipTop)
            canvas.drawPath(path, linePaint)
            canvas.restore()
        } else {
            // No transition, just draw a flat line to the end of the region.
            // The discontinuity is hidden by the progress bar thumb shape.
            canvas.drawLine(totalProgressPx, 0f, totalWidth, 0f, linePaint)
        }

        // Draw round line cap at the beginning of the wave
        val startAmp = cos(abs(waveStart) / waveLength * TWO_PI)
        canvas.drawPoint(0f, startAmp * lineAmplitude * heightFraction, wavePaint)

        canvas.restore()
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        wavePaint.colorFilter = colorFilter
        linePaint.colorFilter = colorFilter
    }

    override fun setAlpha(alpha: Int) {
        updateColors(wavePaint.color, alpha)
    }

    override fun getAlpha(): Int {
        return wavePaint.alpha
    }

    override fun setTint(tintColor: Int) {
        updateColors(tintColor, alpha)
    }

    override fun onLevelChange(level: Int): Boolean {
        return animate
    }

    override fun setTintList(tint: ColorStateList?) {
        if (tint == null) {
            return
        }
        updateColors(tint.defaultColor, alpha)
    }

    private fun updateColors(tintColor: Int, alpha: Int) {
        wavePaint.color = ATHColorUtil.withAlpha(tintColor, alpha / 255f)
        linePaint.color =
            ATHColorUtil.withAlpha(tintColor, (DISABLED_ALPHA * (alpha / 255f)) / 255f)
    }

    fun constrain(amount: Float, low: Float, high: Float): Float {
        return if (amount < low) low else if (amount > high) high else amount
    }

    private fun lerp(start: Float, stop: Float, amount: Float): Float {
        return start + (stop - start) * amount
    }

    /**
     * Returns the interpolation scalar (s) that satisfies the equation: `value = `[ ][.lerp]`(a, b, s)`
     *
     *
     * If `a == b`, then this function will return 0.
     */
    fun lerpInv(a: Float, b: Float, value: Float): Float {
        return if (a != b) (value - a) / (b - a) else 0.0f
    }

    /** Returns the single argument constrained between [0.0, 1.0].  */
    fun saturate(value: Float): Float {
        return constrain(value, 0.0f, 1.0f)
    }

    /** Returns the saturated (constrained between [0, 1]) result of [.lerpInv].  */
    fun lerpInvSat(a: Float, b: Float, value: Float): Float {
        return saturate(lerpInv(a, b, value))
    }
}