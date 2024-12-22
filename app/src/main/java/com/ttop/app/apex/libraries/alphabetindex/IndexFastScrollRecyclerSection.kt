package com.ttop.app.apex.libraries.alphabetindex

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.widget.SectionIndexer
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ttop.app.apex.extensions.surfaceColor

/*
 * Created by MyInnos on 31-01-2017.
 * Updated by AbandonedCart 07-2022.
 */
class IndexFastScrollRecyclerSection(
    context: Context,
    recyclerView: IndexFastScrollRecyclerView
) : RecyclerView.AdapterDataObserver() {
    private var mIndexBarWidth: Float
    private var mIndexBarMarginLeft: Float
    private var mIndexBarMarginRight: Float
    private var mIndexBarMarginTop: Float
    private var mIndexBarMarginBottom: Float
    private val mPreviewPadding: Float
    private val mDensity: Float
    private val mScaledDensity: Float
    private var mListViewWidth = 0
    private var mListViewHeight = 0
    private var mCurrentSection = -1
    private var mIsIndexing = false
    private val mRecyclerView: RecyclerView?
    private var mIndexer: SectionIndexer? = null
    private var mSections: Array<String>? = null
    private var mIndexbarRect: RectF? = null
    private var setIndexTextSize: Int
    private var setPreviewPadding: Int
    private var previewVisibility = true
    private var setIndexBarCornerRadius: Int
    private var setTypeface: Typeface? = null
    private var setIndexBarVisibility = true
    private var setSetIndexBarHighLightTextVisibility = false
    private var setIndexBarVibration = false
    private var setIndexBarStrokeVisibility = true
    private var mIndexBarStrokeWidth: Int

    @ColorInt
    private var mIndexBarStrokeColor: Int

    @ColorInt
    private var indexbarBackgroudColor: Int

    @ColorInt
    private var indexbarTextColor: Int

    @ColorInt
    private var indexbarHighLightTextColor: Int
    private var setPreviewTextSize: Int

    @ColorInt
    private var previewBackgroundColor: Int

    @ColorInt
    private var previewTextColor: Int
    private var previewBackgroudAlpha: Int
    private var indexbarBackgroudAlpha: Int
    fun draw(canvas: Canvas) {
        if (setIndexBarVisibility) {
            val indexbarPaint = Paint()
            indexbarPaint.color = indexbarBackgroudColor
            indexbarPaint.alpha = indexbarBackgroudAlpha
            indexbarPaint.isAntiAlias = true
            canvas.drawRoundRect(
                mIndexbarRect!!, setIndexBarCornerRadius * mDensity,
                setIndexBarCornerRadius * mDensity, indexbarPaint
            )
            if (setIndexBarStrokeVisibility) {
                indexbarPaint.style = Paint.Style.STROKE
                indexbarPaint.color = mIndexBarStrokeColor
                indexbarPaint.strokeWidth = mIndexBarStrokeWidth.toFloat() // set stroke width
                canvas.drawRoundRect(
                    mIndexbarRect!!, setIndexBarCornerRadius * mDensity,
                    setIndexBarCornerRadius * mDensity, indexbarPaint
                )
            }
            if (mSections != null && mSections!!.size > 0) {
                // Preview is shown when mCurrentSection is set
                if (previewVisibility && mCurrentSection >= 0 && mSections!![mCurrentSection] != "") {
                    val previewPaint = Paint()
                    previewPaint.color = previewBackgroundColor
                    previewPaint.alpha = previewBackgroudAlpha
                    /*previewPaint.isAntiAlias = true
                    previewPaint.setShadowLayer(
                        3f, 0f, 0f,
                        Color.argb(64, 0, 0, 0)
                    )*/
                    val previewTextPaint = Paint()
                    previewTextPaint.color = previewTextColor
                    previewTextPaint.isAntiAlias = true
                    previewTextPaint.textSize = setPreviewTextSize * mScaledDensity
                    previewTextPaint.typeface = setTypeface
                    val previewTextWidth =
                        previewTextPaint.measureText(mSections!![mCurrentSection])
                    var previewSize =
                        2 * mPreviewPadding + previewTextPaint.descent() - previewTextPaint.ascent()
                    previewSize = Math.max(previewSize, previewTextWidth + 2 * mPreviewPadding)
                    val previewRect = RectF(
                        (mListViewWidth - previewSize) / 2,
                        (mListViewHeight - previewSize) / 2,
                        (mListViewWidth - previewSize) / 2 + previewSize,
                        (mListViewHeight - previewSize) / 2 + previewSize
                    )
                    canvas.drawRoundRect(previewRect, 5 * mDensity, 5 * mDensity, previewPaint)
                    canvas.drawText(
                        mSections!![mCurrentSection],
                        previewRect.left + (previewSize - previewTextWidth) / 2 - 1,
                        previewRect.top + (previewSize - (previewTextPaint.descent() - previewTextPaint.ascent())) / 2 - previewTextPaint.ascent(),
                        previewTextPaint
                    )
                    setPreviewFadeTimeout(200)
                }
                val indexPaint = Paint()
                indexPaint.color = indexbarTextColor
                indexPaint.isAntiAlias = true
                indexPaint.textSize = setIndexTextSize * mScaledDensity
                indexPaint.typeface = setTypeface
                val sectionHeight =
                    (mIndexbarRect!!.height() - mIndexBarMarginTop - mIndexBarMarginBottom) / mSections!!.size
                val paddingTop = (sectionHeight - (indexPaint.descent() - indexPaint.ascent())) / 2
                for (i in mSections!!.indices) {
                    if (setSetIndexBarHighLightTextVisibility) {
                        if (mCurrentSection > -1 && i == mCurrentSection) {
                            indexPaint.typeface = Typeface.create(setTypeface, Typeface.BOLD)
                            indexPaint.textSize = (setIndexTextSize + 3) * mScaledDensity
                            indexPaint.color = indexbarHighLightTextColor
                        } else {
                            indexPaint.typeface = setTypeface
                            indexPaint.textSize = setIndexTextSize * mScaledDensity
                            indexPaint.color = indexbarTextColor
                        }
                        val paddingLeft = (mIndexBarWidth - indexPaint.measureText(
                            mSections!![i]
                        )) / 2
                        canvas.drawText(
                            mSections!![i],
                            mIndexbarRect!!.left + paddingLeft,
                            mIndexbarRect!!.top + mIndexBarMarginTop + sectionHeight * i + paddingTop - indexPaint.ascent(),
                            indexPaint
                        )
                    } else {
                        val paddingLeft = (mIndexBarWidth - indexPaint.measureText(
                            mSections!![i]
                        )) / 2
                        canvas.drawText(
                            mSections!![i],
                            mIndexbarRect!!.left + paddingLeft,
                            mIndexbarRect!!.top + mIndexBarMarginTop + sectionHeight * i + paddingTop - indexPaint.ascent(),
                            indexPaint
                        )
                    }
                }
            }
        }
    }

    fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN ->                 // If down event occurs inside index bar region, start indexing
                if (contains(ev.x, ev.y)) {
                    if (setIndexBarVibration) {
                        mRecyclerView?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    }
                    // It demonstrates that the motion event started from index bar
                    mIsIndexing = true
                    // Determine which section the point is in, and move the list to that section
                    mCurrentSection = getSectionByPoint(ev.y)
                    scrollToPosition()
                    return true
                }
            MotionEvent.ACTION_MOVE -> if (mIsIndexing) {
                // If this event moves inside index bar
                if (contains(ev.x, ev.y)) {
                    // Determine which section the point is in, and move the list to that section
                    mCurrentSection = getSectionByPoint(ev.y)
                    scrollToPosition()
                }
                return true
            }
            MotionEvent.ACTION_UP -> if (mIsIndexing) {
                mIsIndexing = false
                mCurrentSection = -1
            }
        }
        return false
    }

    private fun scrollToPosition() {
        try {
            val position = mIndexer?.getPositionForSection(mCurrentSection)
            val layoutManager = mRecyclerView!!.layoutManager
            if (layoutManager is LinearLayoutManager) {
                position?.let { layoutManager.scrollToPositionWithOffset(it, 0) }
            } else position?.let { layoutManager?.scrollToPosition(it) }
        } catch (e: Exception) {
            Log.d("INDEX_BAR", "Data size returns null")
        }
    }

    fun onSizeChanged(w: Int, h: Int) {
        mListViewWidth = w
        mListViewHeight = h
        mIndexbarRect = RectF(
            w - mIndexBarMarginLeft - mIndexBarWidth,
            mIndexBarMarginTop,
            w - mIndexBarMarginRight,
            h - mIndexBarMarginBottom - if (mRecyclerView!!.clipToPadding) 0 else mRecyclerView.paddingBottom
        )
    }

    fun setAdapter(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder?>?) {
        if (adapter is SectionIndexer) {
            adapter.registerAdapterDataObserver(this)
            mIndexer = adapter
            val list: ArrayList<String> = ArrayList()
            for (name in mIndexer?.sections!!) {
                list.add(name.toString())
            }
            mSections = list.toTypedArray()
        }
    }

    override fun onChanged() {
        super.onChanged()
        updateSections()
    }

    fun updateSections() {
        val list: ArrayList<String> = ArrayList()
        for (name in mIndexer?.sections!!) {
            list.add(name.toString())
        }
        mSections = list.toTypedArray()
    }

    fun contains(x: Float, y: Float): Boolean {
        // Determine if the point is in index bar region, which includes the right margin of the bar
        return x >= mIndexbarRect!!.left && y >= mIndexbarRect!!.top && y <= mIndexbarRect!!.top + mIndexbarRect!!.height()
    }

    private fun getSectionByPoint(y: Float): Int {
        if (mSections == null || mSections?.isEmpty() == true) return 0
        if (y < mIndexbarRect!!.top + mIndexBarMarginTop) return 0
        return if (y >= mIndexbarRect!!.top + mIndexbarRect!!.height() - mIndexBarMarginTop) mSections!!.size - 1 else ((y - mIndexbarRect!!.top - mIndexBarMarginTop) / ((mIndexbarRect!!.height() - mIndexBarMarginBottom - mIndexBarMarginTop) / mSections!!.size)).toInt()
    }

    private var mLastFadeRunnable: Runnable? = null
    private fun setPreviewFadeTimeout(delay: Long) {
        if (mRecyclerView != null) {
            if (mLastFadeRunnable != null) {
                mRecyclerView.removeCallbacks(mLastFadeRunnable)
            }
            mLastFadeRunnable = Runnable { mRecyclerView.invalidate() }
            mRecyclerView.postDelayed(mLastFadeRunnable, delay)
        }
    }

    private fun convertTransparentValueToBackgroundAlpha(value: Float): Int {
        return (255 * value).toInt()
    }

    /**
     * @param value int to set the text size of the index bar
     */
    fun setIndexTextSize(value: Int) {
        setIndexTextSize = value
    }

    /**
     * @param value float to set the width of the index bar
     */
    fun setIndexBarWidth(value: Float) {
        mIndexBarWidth = value
    }

    /**
     * @param value float to set the margin of the index bar
     */
    fun setIndexBarMargin(value: Float) {
        mIndexBarMarginLeft = value
        mIndexBarMarginRight = value
        mIndexBarMarginTop = value
        mIndexBarMarginBottom = value
    }

    /**
     * @param value float to set the top margin of the index bar
     */
    fun setIndexBarTopMargin(value: Float) {
        mIndexBarMarginTop = value
    }

    /**
     * @param value float to set the bottom margin of the index bar
     */
    fun setIndexBarBottomMargin(value: Float) {
        mIndexBarMarginBottom = value
    }

    /**
     * @param value float to set the left margin of the index bar
     */
    fun setIndexBarHorizontalMargin(value: Float) {
        mIndexBarMarginLeft = value
        mIndexBarMarginRight = value
    }

    /**
     * @param value float to set the right margin of the index bar
     */
    fun setIndexBarVerticalMargin(value: Float) {
        mIndexBarMarginTop = value
        mIndexBarMarginBottom = value
    }

    /**
     * @param value int to set preview padding
     */
    fun setPreviewPadding(value: Int) {
        setPreviewPadding = value
    }

    /**
     * @param value int to set the radius of the index bar
     */
    fun setIndexBarCornerRadius(value: Int) {
        setIndexBarCornerRadius = value
    }

    /**
     * @param value float to set the transparency of the color for index bar
     */
    fun setIndexBarTransparentValue(value: Float) {
        indexbarBackgroudAlpha = convertTransparentValueToBackgroundAlpha(value)
    }

    /**
     * @param typeface Typeface to set the typeface of the preview & the index bar
     */
    fun setTypeface(typeface: Typeface?) {
        setTypeface = typeface
    }

    /**
     * @param shown boolean to show or hide the index bar
     */
    fun setIndexBarVisibility(shown: Boolean) {
        setIndexBarVisibility = shown
    }

    /**
     * @param shown boolean to show or hide the index bar
     */
    fun setIndexBarStrokeVisibility(shown: Boolean) {
        setIndexBarStrokeVisibility = shown
    }

    /**
     * @param shown boolean to show or hide the preview box
     */
    fun setPreviewVisibility(shown: Boolean) {
        previewVisibility = shown
    }

    /**
     * @param value int to set the text size of the preview box
     */
    fun setIndexBarStrokeWidth(value: Int) {
        mIndexBarStrokeWidth = value
    }

    /**
     * @param value int to set the text size of the preview box
     */
    fun setPreviewTextSize(value: Int) {
        setPreviewTextSize = value
    }

    /**
     * @param color The color for the preview box
     */
    fun setPreviewColor(@ColorInt color: Int) {
        previewBackgroundColor = color
    }

    /**
     * @param color The text color for the preview box
     */
    fun setPreviewTextColor(@ColorInt color: Int) {
        previewTextColor = color
    }

    /**
     * @param value float to set the transparency value of the preview box
     */
    fun setPreviewTransparentValue(value: Float) {
        previewBackgroudAlpha = convertTransparentValueToBackgroundAlpha(value)
    }

    /**
     * @param color The color for the scroll track
     */
    fun setIndexBarColor(@ColorInt color: Int) {
        indexbarBackgroudColor = color
    }

    /**
     * @param color The text color for the index bar
     */
    fun setIndexBarTextColor(@ColorInt color: Int) {
        indexbarTextColor = color
    }

    /**
     * @param color The text color for the index bar
     */
    fun setIndexBarStrokeColor(@ColorInt color: Int) {
        mIndexBarStrokeColor = color
    }

    /**
     * @param color The text color for the index bar
     */
    fun setIndexBarHighLightTextColor(@ColorInt color: Int) {
        indexbarHighLightTextColor = color
    }

    /**
     * @param shown boolean to show or hide the index bar
     */
    fun setIndexBarHighLightTextVisibility(shown: Boolean) {
        setSetIndexBarHighLightTextVisibility = shown
    }

    /**
     * @param vibrate boolean to enable or disable vibration of the index bar when navigating
     */
    fun setIndexBarVibration(vibrate: Boolean) {
        setIndexBarVibration = vibrate
    }

    init {
        setIndexTextSize = recyclerView.setIndexTextSize
        val setIndexbarWidth = recyclerView.mIndexBarWidth
        val setIndexbarMarginLeft = recyclerView.mIndexBarMarginLeft
        val setIndexbarMarginRight = recyclerView.mIndexBarMarginRight
        val setIndexbarMarginTop = recyclerView.mIndexBarMarginTop
        val setIndexbarMarginBottom = recyclerView.mIndexBarMarginBottom
        setPreviewPadding = recyclerView.mPreviewPadding
        setPreviewTextSize = recyclerView.mPreviewTextSize
        previewBackgroundColor = recyclerView.mPreviewBackgroundColor
        previewTextColor = recyclerView.mPreviewTextColor
        previewBackgroudAlpha =
            convertTransparentValueToBackgroundAlpha(recyclerView.mPreviewTransparentValue)
        mIndexBarStrokeColor = recyclerView.mSetIndexBarStrokeColor
        mIndexBarStrokeWidth = recyclerView.mIndexBarStrokeWidth
        setIndexBarCornerRadius = recyclerView.mIndexBarCornerRadius
        indexbarBackgroudColor = recyclerView.mIndexBarBackgroundColor
        indexbarTextColor = recyclerView.mIndexBarTextColor
        indexbarHighLightTextColor = recyclerView.mIndexBarHighLightTextColor
        indexbarBackgroudAlpha =
            convertTransparentValueToBackgroundAlpha(recyclerView.mIndexBarTransparentValue)
        mDensity = context.resources.displayMetrics.density
        mScaledDensity = context.resources.displayMetrics.scaledDensity
        mRecyclerView = recyclerView
        mRecyclerView.setLayoutManager(LinearLayoutManager(context))
        setAdapter(mRecyclerView.getAdapter())
        mIndexBarWidth = setIndexbarWidth * mDensity
        mIndexBarMarginLeft = setIndexbarMarginLeft * mDensity
        mIndexBarMarginRight = setIndexbarMarginRight * mDensity
        mIndexBarMarginTop = setIndexbarMarginTop * mDensity
        mIndexBarMarginBottom = setIndexbarMarginBottom * mDensity
        mPreviewPadding = setPreviewPadding * mDensity
    }
}