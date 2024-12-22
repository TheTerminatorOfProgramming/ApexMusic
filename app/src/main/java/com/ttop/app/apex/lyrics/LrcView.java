/*
 * Copyright (C) 2017 wangchenyan
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.ttop.app.apex.lyrics;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Looper;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import androidx.core.content.ContextCompat;

import com.ttop.app.apex.BuildConfig;
import com.ttop.app.apex.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 歌词 Created by wcy on 2015/11/9.
 */
@SuppressLint("StaticFieldLeak")
public class LrcView extends View {
    private static final long ADJUST_DURATION = 100;
    private static final long TIMELINE_KEEP_TIME = 4 * DateUtils.SECOND_IN_MILLIS;

    private final List<LrcEntry> mLrcEntryList = new ArrayList<>();
    private final TextPaint mLrcPaint = new TextPaint();
    private final TextPaint mTimePaint = new TextPaint();
    private Paint.FontMetrics mTimeFontMetrics;
    private Drawable mPlayDrawable;
    private float mDividerHeight;
    private long mAnimationDuration;
    private int mNormalTextColor;
    private float mNormalTextSize;
    private int mCurrentTextColor;
    private float mCurrentTextSize;
    private int mTimelineTextColor;
    private int mTimelineColor;
    private int mTimeTextColor;
    private int mDrawableWidth;
    private int mTimeTextWidth;
    private String mDefaultLabel;
    private float mLrcPadding;
    private OnPlayClickListener mOnPlayClickListener;
    private ValueAnimator mAnimator;
    private GestureDetector mGestureDetector;
    private Scroller mScroller;
    private float mOffset;
    private int mCurrentLine;
    private Object mFlag;
    private boolean isShowTimeline;
    private final Runnable hideTimelineRunnable =
            new Runnable() {
                @Override
                public void run() {
                    if (hasLrc() && isShowTimeline) {
                        isShowTimeline = false;
                        smoothScrollTo(mCurrentLine);
                    }
                }
            };
    private boolean isTouching;
    private boolean isFling;
    private final GestureDetector.SimpleOnGestureListener mSimpleOnGestureListener =
            new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDown(MotionEvent e) {
                    if (hasLrc() && mOnPlayClickListener != null) {
                        mScroller.forceFinished(true);
                        removeCallbacks(hideTimelineRunnable);
                        isTouching = true;
                        isShowTimeline = true;
                        invalidate();
                        return true;
                    }
                    return super.onDown(e);
                }

                @Override
                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                    if (hasLrc()) {
                        mOffset -= distanceY;
                        mOffset = Math.min(mOffset, getOffset(0));
                        mOffset = Math.max(mOffset, getOffset(mLrcEntryList.size() - 1));
                        invalidate();
                        getParent().requestDisallowInterceptTouchEvent(true);
                        return true;
                    }
                    return super.onScroll(e1, e2, distanceX, distanceY);
                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    if (hasLrc()) {
                        mScroller.fling(
                                0,
                                (int) mOffset,
                                0,
                                (int) velocityY,
                                0,
                                0,
                                (int) getOffset(mLrcEntryList.size() - 1),
                                (int) getOffset(0));
                        isFling = true;
                        return true;
                    }
                    return super.onFling(e1, e2, velocityX, velocityY);
                }

                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    if (hasLrc()
                            && isShowTimeline
                            && mPlayDrawable.getBounds().contains((int) e.getX(), (int) e.getY())) {
                        int centerLine = getCenterLine();
                        long centerLineTime = mLrcEntryList.get(centerLine).getTime();
                        // onPlayClick 消费了才更新 UI
                        if (mOnPlayClickListener != null && mOnPlayClickListener.onPlayClick(centerLineTime)) {
                            isShowTimeline = false;
                            removeCallbacks(hideTimelineRunnable);
                            mCurrentLine = centerLine;
                            invalidate();
                            return true;
                        }
                    }
                    return super.onSingleTapConfirmed(e);
                }
            };
    private int mTextGravity; // 歌词显示位置，靠左/居中/靠右

    public LrcView(Context context) {
        this(context, null);
    }

    public LrcView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LrcView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.LrcView);
        mCurrentTextSize =
                ta.getDimension(
                        R.styleable.LrcView_lrcTextSize, getResources().getDimension(R.dimen.lrc_text_size));
        mNormalTextSize =
                ta.getDimension(
                        R.styleable.LrcView_lrcNormalTextSize,
                        getResources().getDimension(R.dimen.lrc_text_size));
        if (mNormalTextSize == 0) {
            mNormalTextSize = mCurrentTextSize;
        }

        mDividerHeight =
                ta.getDimension(
                        R.styleable.LrcView_lrcDividerHeight,
                        getResources().getDimension(R.dimen.lrc_divider_height));
        int defDuration = getResources().getInteger(R.integer.lrc_animation_duration);
        mAnimationDuration = ta.getInt(R.styleable.LrcView_lrcAnimationDuration, defDuration);
        mAnimationDuration = (mAnimationDuration < 0) ? defDuration : mAnimationDuration;
        mNormalTextColor =
                ta.getColor(
                        R.styleable.LrcView_lrcNormalTextColor,
                        ContextCompat.getColor(getContext(), R.color.lrc_normal_text_color));
        mCurrentTextColor =
                ta.getColor(
                        R.styleable.LrcView_lrcCurrentTextColor,
                        ContextCompat.getColor(getContext(), R.color.lrc_current_text_color));
        mTimelineTextColor =
                ta.getColor(
                        R.styleable.LrcView_lrcTimelineTextColor,
                        ContextCompat.getColor(getContext(), R.color.lrc_timeline_text_color));
        mDefaultLabel = ta.getString(R.styleable.LrcView_lrcLabel);
        mDefaultLabel =
                TextUtils.isEmpty(mDefaultLabel) ? getContext().getString(R.string.empty) : mDefaultLabel;
        mLrcPadding = ta.getDimension(R.styleable.LrcView_lrcPadding, 0);
        mTimelineColor =
                ta.getColor(
                        R.styleable.LrcView_lrcTimelineColor,
                        ContextCompat.getColor(getContext(), R.color.lrc_timeline_color));
        float timelineHeight =
                ta.getDimension(
                        R.styleable.LrcView_lrcTimelineHeight,
                        getResources().getDimension(R.dimen.lrc_timeline_height));
        mPlayDrawable = ta.getDrawable(R.styleable.LrcView_lrcPlayDrawable);
        mPlayDrawable =
                (mPlayDrawable == null)
                        ? ContextCompat.getDrawable(getContext(), R.drawable.ic_play_arrow)
                        : mPlayDrawable;
        mTimeTextColor =
                ta.getColor(
                        R.styleable.LrcView_lrcTimeTextColor,
                        ContextCompat.getColor(getContext(), R.color.lrc_time_text_color));
        float timeTextSize =
                ta.getDimension(
                        R.styleable.LrcView_lrcTimeTextSize,
                        getResources().getDimension(R.dimen.lrc_time_text_size));
        mTextGravity = ta.getInteger(R.styleable.LrcView_lrcTextGravity, LrcEntry.GRAVITY_CENTER);

        ta.recycle();

        mDrawableWidth = (int) getResources().getDimension(R.dimen.lrc_drawable_width);
        mTimeTextWidth = (int) getResources().getDimension(R.dimen.lrc_time_width);

        mLrcPaint.setAntiAlias(true);
        mLrcPaint.setTextSize(mCurrentTextSize);
        mLrcPaint.setTextAlign(Paint.Align.LEFT);
        mTimePaint.setAntiAlias(true);
        mTimePaint.setTextSize(timeTextSize);
        mTimePaint.setTextAlign(Paint.Align.CENTER);
        //noinspection SuspiciousNameCombination
        mTimePaint.setStrokeWidth(timelineHeight);
        mTimePaint.setStrokeCap(Paint.Cap.ROUND);
        mTimeFontMetrics = mTimePaint.getFontMetrics();

        mGestureDetector = new GestureDetector(getContext(), mSimpleOnGestureListener);
        mGestureDetector.setIsLongpressEnabled(false);
        mScroller = new Scroller(getContext());
    }

    public void setCurrentColor(int currentColor) {
        mCurrentTextColor = currentColor;
        postInvalidate();
    }

    public void setTimelineTextColor(int timelineTextColor) {
        mTimelineTextColor = timelineTextColor;
        postInvalidate();
    }

    public void setTimelineColor(int timelineColor) {
        mTimelineColor = timelineColor;
        postInvalidate();
    }


    public void setTimeTextColor(int timeTextColor) {
        mTimeTextColor = timeTextColor;
        postInvalidate();
    }


    public void setDraggable(boolean draggable, OnPlayClickListener onPlayClickListener) {
        if (draggable) {
            if (onPlayClickListener == null) {
                throw new IllegalArgumentException(
                        "if draggable == true, onPlayClickListener must not be null");
            }
            mOnPlayClickListener = onPlayClickListener;
        } else {
            mOnPlayClickListener = null;
        }
    }

    public void setLabel(String label) {
        runOnUi(
                () -> {
                    mDefaultLabel = label;
                    invalidate();
                });
    }

    public void loadLrc(File lrcFile) {
        loadLrc(lrcFile, null);
    }

    public void loadLrc(File mainLrcFile, File secondLrcFile) {
        runOnUi(() -> {
            reset();

            StringBuilder sb = new StringBuilder("file://");
            sb.append(mainLrcFile.getPath());
            if (secondLrcFile != null) {
                sb.append("#").append(secondLrcFile.getPath());
            }
            String flag = sb.toString();
            setFlag(flag);
            new AsyncTask<File, Integer, List<LrcEntry>>() {
                @Override
                protected List<LrcEntry> doInBackground(File... params) {
                    return LrcUtils.parseLrc(params);
                }

                @Override
                protected void onPostExecute(List<LrcEntry> lrcEntries) {
                    if (getFlag() == flag) {
                        onLrcLoaded(lrcEntries);
                        setFlag(null);
                    }
                }
            }.execute(mainLrcFile, secondLrcFile);
        });
    }

    public void loadLrc(String lrcText) {
        loadLrc(lrcText, null);
    }

    public void loadLrc(String mainLrcText, String secondLrcText) {
        runOnUi(
                () -> {
                    reset();

                    StringBuilder sb = new StringBuilder("file://");
                    sb.append(mainLrcText);
                    if (secondLrcText != null) {
                        sb.append("#").append(secondLrcText);
                    }
                    String flag = sb.toString();
                    setFlag(flag);
                    new AsyncTask<String, Integer, List<LrcEntry>>() {
                        @Override
                        protected List<LrcEntry> doInBackground(String... params) {
                            return LrcUtils.parseLrc(params);
                        }

                        @Override
                        protected void onPostExecute(List<LrcEntry> lrcEntries) {
                            if (getFlag() == flag) {
                                onLrcLoaded(lrcEntries);
                                setFlag(null);
                            }
                        }
                    }.execute(mainLrcText, secondLrcText);
                });
    }

    public boolean hasLrc() {
        return !mLrcEntryList.isEmpty();
    }

    public void updateTime(long time) {
        runOnUi(
                () -> {
                    if (!hasLrc()) {
                        return;
                    }

                    int line = findShowLine(time);
                    if (line != mCurrentLine) {
                        mCurrentLine = line;
                        if (!isShowTimeline) {
                            smoothScrollTo(line);
                        } else {
                            invalidate();
                        }
                    }
                });
    }

    @Deprecated
    public void onDrag(long time) {
        updateTime(time);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            initPlayDrawable();
            initEntryList();
            if (hasLrc()) {
                smoothScrollTo(mCurrentLine, 0L);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int centerY = getHeight() / 2;

        if (!hasLrc()) {
            mLrcPaint.setColor(mCurrentTextColor);
            @SuppressLint("DrawAllocation")
            StaticLayout staticLayout =
                    new StaticLayout(
                            mDefaultLabel,
                            mLrcPaint,
                            (int) getLrcWidth(),
                            Layout.Alignment.ALIGN_CENTER,
                            1f,
                            0f,
                            false);
            drawText(canvas, staticLayout, centerY);
            return;
        }

        int centerLine = getCenterLine();

        if (isShowTimeline) {
            mPlayDrawable.draw(canvas);

            mTimePaint.setColor(mTimelineColor);
            canvas.drawLine(mTimeTextWidth, centerY, getWidth() - mTimeTextWidth, centerY, mTimePaint);

            mTimePaint.setColor(mTimeTextColor);
            String timeText = LrcUtils.formatTime(mLrcEntryList.get(centerLine).getTime());
            float timeX = getWidth() - mTimeTextWidth / 2;
            float timeY = centerY - (mTimeFontMetrics.descent + mTimeFontMetrics.ascent) / 2;
            canvas.drawText(timeText, timeX, timeY, mTimePaint);
        }

        canvas.translate(0, mOffset);

        float y = 0;
        for (int i = 0; i < mLrcEntryList.size(); i++) {
            if (i > 0) {
                y +=
                        ((mLrcEntryList.get(i - 1).getHeight() + mLrcEntryList.get(i).getHeight()) >> 1)
                                + mDividerHeight;
            }
            if (BuildConfig.DEBUG) {
                // mLrcPaint.setTypeface(ResourcesCompat.getFont(getContext(), R.font.sans));
            }
            if (i == mCurrentLine) {
                mLrcPaint.setTextSize(mCurrentTextSize);
                mLrcPaint.setColor(mCurrentTextColor);
            } else if (isShowTimeline && i == centerLine) {
                mLrcPaint.setColor(mTimelineTextColor);
            } else {
                mLrcPaint.setTextSize(mNormalTextSize);
                mLrcPaint.setColor(mNormalTextColor);
            }
            drawText(canvas, mLrcEntryList.get(i).getStaticLayout(), y);
        }
    }

    private void drawText(Canvas canvas, StaticLayout staticLayout, float y) {
        canvas.save();
        canvas.translate(mLrcPadding, y - (staticLayout.getHeight() >> 1));
        staticLayout.draw(canvas);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP
                || event.getAction() == MotionEvent.ACTION_CANCEL) {
            isTouching = false;
            if (hasLrc() && !isFling) {
                adjustCenter();
                postDelayed(hideTimelineRunnable, TIMELINE_KEEP_TIME);
            }
        }
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mOffset = mScroller.getCurrY();
            invalidate();
        }

        if (isFling && mScroller.isFinished()) {
            isFling = false;
            if (hasLrc() && !isTouching) {
                adjustCenter();
                postDelayed(hideTimelineRunnable, TIMELINE_KEEP_TIME);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        removeCallbacks(hideTimelineRunnable);
        super.onDetachedFromWindow();
    }

    private void onLrcLoaded(List<LrcEntry> entryList) {
        if (entryList != null && !entryList.isEmpty()) {
            mLrcEntryList.addAll(entryList);
        }

        Collections.sort(mLrcEntryList);

        initEntryList();
        invalidate();
    }

    private void initPlayDrawable() {
        int l = (mTimeTextWidth - mDrawableWidth) / 2;
        int t = getHeight() / 2 - mDrawableWidth / 2;
        int r = l + mDrawableWidth;
        int b = t + mDrawableWidth;
        mPlayDrawable.setBounds(l, t, r, b);
    }

    private void initEntryList() {
        if (!hasLrc() || getWidth() == 0) {
            return;
        }

        for (LrcEntry lrcEntry : mLrcEntryList) {
            lrcEntry.init(mLrcPaint, (int) getLrcWidth(), mTextGravity);
        }

        mOffset = getHeight() / 2;
    }

    private void reset() {
        endAnimation();
        mScroller.forceFinished(true);
        isShowTimeline = false;
        isTouching = false;
        isFling = false;
        removeCallbacks(hideTimelineRunnable);
        mLrcEntryList.clear();
        mOffset = 0;
        mCurrentLine = 0;
        //invalidate();
    }

    private void adjustCenter() {
        smoothScrollTo(getCenterLine(), ADJUST_DURATION);
    }

    private void smoothScrollTo(int line) {
        smoothScrollTo(line, mAnimationDuration);
    }

    private void smoothScrollTo(int line, long duration) {
        float offset = getOffset(line);
        endAnimation();

        mAnimator = ValueAnimator.ofFloat(mOffset, offset);
        mAnimator.setDuration(duration);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.addUpdateListener(
                animation -> {
                    mOffset = (float) animation.getAnimatedValue();
                    invalidate();
                });
        mAnimator.start();
    }

    private void endAnimation() {
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.end();
        }
    }

    private int findShowLine(long time) {
        int left = 0;
        int right = mLrcEntryList.size();
        while (left <= right) {
            int middle = (left + right) / 2;
            long middleTime = mLrcEntryList.get(middle).getTime();

            if (time < middleTime) {
                right = middle - 1;
            } else {
                if (middle + 1 >= mLrcEntryList.size() || time < mLrcEntryList.get(middle + 1).getTime()) {
                    return middle;
                }

                left = middle + 1;
            }
        }

        return 0;
    }

    private int getCenterLine() {
        int centerLine = 0;
        float minDistance = Float.MAX_VALUE;
        for (int i = 0; i < mLrcEntryList.size(); i++) {
            if (Math.abs(mOffset - getOffset(i)) < minDistance) {
                minDistance = Math.abs(mOffset - getOffset(i));
                centerLine = i;
            }
        }
        return centerLine;
    }

    private float getOffset(int line) {
        if (mLrcEntryList.get(line).getOffset() == Float.MIN_VALUE) {
            float offset = getHeight() / 2;
            for (int i = 1; i <= line; i++) {
                offset -=
                        ((mLrcEntryList.get(i - 1).getHeight() + mLrcEntryList.get(i).getHeight()) >> 1)
                                + mDividerHeight;
            }
            mLrcEntryList.get(line).setOffset(offset);
        }

        return mLrcEntryList.get(line).getOffset();
    }

    private float getLrcWidth() {
        return getWidth() - mLrcPadding * 2;
    }

    private void runOnUi(Runnable r) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            r.run();
        } else {
            post(r);
        }
    }

    private Object getFlag() {
        return mFlag;
    }

    private void setFlag(Object flag) {
        this.mFlag = flag;
    }

    public interface OnPlayClickListener {
        boolean onPlayClick(long time);
    }
}
