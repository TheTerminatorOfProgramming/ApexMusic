package com.ttop.app.apex.views

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Property
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import com.ttop.app.apex.R
import java.text.NumberFormat

class NumberRollView(context: Context?, attrs: AttributeSet?) :
    FrameLayout(context!!, attrs) {
    private var mUpNumber: TextView? = null
    private var mDownNumber: TextView? = null
    private var mNumber = 0f
    private var mLastRollAnimator: Animator? = null
    private var mStringId = R.string.x_selected
    private var mStringIdForZero = 0
    override fun onFinishInflate() {
        super.onFinishInflate()
        mUpNumber = findViewById(R.id.up)
        mDownNumber = findViewById(R.id.down)
        assert(mUpNumber != null)
        assert(mDownNumber != null)
        setNumberRoll(mNumber)
    }

    /**
     * Sets a number to display.
     * @param animate Whether it should smoothly animate to the number.
     */
    fun setNumber(number: Int, animate: Boolean) {
        if (mLastRollAnimator != null) mLastRollAnimator!!.cancel()
        if (animate) {
            val rollAnimator: Animator =
                ObjectAnimator.ofFloat(this, NUMBER_PROPERTY, number.toFloat())
            rollAnimator.interpolator = LinearInterpolator()
            rollAnimator.start()
            mLastRollAnimator = rollAnimator
        } else {
            setNumberRoll(number.toFloat())
        }
    }

    /**
     * Gets the current number roll position.
     */
    private fun getNumberRoll(): Float {
        return mNumber
    }

    /**
     * Sets the number roll position.
     */
    private fun setNumberRoll(number: Float) {
        mNumber = number
        val downNumber = number.toInt()
        val upNumber = downNumber + 1
        val numberFormatter = NumberFormat.getIntegerInstance()
        var newString = if (mStringId != 0) {
            if (upNumber == 0 && mStringIdForZero != 0) resources.getString(mStringIdForZero) else resources.getString(
                mStringId,
                upNumber
            )
        } else {
            numberFormatter.format(upNumber.toLong())
        }
        if (newString != mUpNumber!!.text.toString()) {
            mUpNumber!!.text = newString
        }
        newString = if (mStringId != 0) {
            if (downNumber == 0 && mStringIdForZero != 0) resources.getString(mStringIdForZero) else resources.getString(
                mStringId,
                downNumber
            )
        } else {
            numberFormatter.format(downNumber.toLong())
        }
        if (newString != mDownNumber!!.text.toString()) {
            mDownNumber!!.text = newString
        }
        val offset = number % 1.0f
        mUpNumber!!.translationY = mUpNumber!!.height * (offset - 1.0f)
        mDownNumber!!.translationY = mDownNumber!!.height * offset
        mUpNumber!!.alpha = offset
        mDownNumber!!.alpha = 1.0f - offset
    }

    companion object {
        /**
         * A Property wrapper around the `number` functionality handled by the
         * [NumberRollView.setNumberRoll] and [NumberRollView.getNumberRoll]
         * methods.
         */
        val NUMBER_PROPERTY: Property<NumberRollView, Float> =
            object : Property<NumberRollView, Float>(
                Float::class.java, ""
            ) {
                override fun set(view: NumberRollView, value: Float) {
                    view.setNumberRoll(value)
                }

                override fun get(view: NumberRollView): Float {
                    return view.getNumberRoll()
                }
            }
    }
}