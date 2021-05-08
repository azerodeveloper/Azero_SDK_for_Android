/*
* Copyright (c) 2019 SoundAI. All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.azero.sampleapp.activity.weather.widget;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import androidx.annotation.Nullable;
import android.util.AttributeSet;

import com.azero.sampleapp.R;


public class SquareTextView extends androidx.appcompat.widget.AppCompatTextView {
    private final Paint mThumbPaint = new Paint();
    private final Paint mTrackPaint = new Paint();
    float startAngle = 0f;
    float sweepAngle = 180f;
    int thumbColor;
    int trackColor;
    float thumbWidth;
    float trackWidth;

    private final RectF mRect = new RectF();

    public SquareTextView(Context context) {
        this(context, null);
    }

    public SquareTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public SquareTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SquareTextView);
            thumbColor = a.getColor(R.styleable.SquareTextView_thumbColor, Color.TRANSPARENT);
            trackColor = a.getColor(R.styleable.SquareTextView_trackColor, Color.TRANSPARENT);

            thumbWidth = a.getDimensionPixelSize(R.styleable.SquareTextView_thumbWidth, 0);
            trackWidth = a.getDimensionPixelSize(R.styleable.SquareTextView_trackWidth, 0);
        }
        // Paints for the round scrollbar.
        // Set up the thumb paint
        mThumbPaint.setAntiAlias(true);
        mThumbPaint.setStrokeCap(Paint.Cap.ROUND);
        mThumbPaint.setStyle(Paint.Style.STROKE);
        mThumbPaint.setColor(thumbColor);


        // Set up the track paint
        mTrackPaint.setAntiAlias(true);
        mTrackPaint.setStrokeCap(Paint.Cap.ROUND);
        mTrackPaint.setStyle(Paint.Style.STROKE);
        mTrackPaint.setColor(trackColor);


    }

    @Override
    public boolean onPreDraw() {
        return super.onPreDraw();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        initRect();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawRoundScrollbars(canvas);
    }


    void initRect() {
        mRect.set(

                getPaddingLeft() + thumbWidth / 2,
                getPaddingTop() + thumbWidth / 2,
                getWidth() - getPaddingRight() - thumbWidth / 2,
                getHeight() - getPaddingBottom() - thumbWidth / 2);
    }

    public void drawRoundScrollbars(Canvas canvas) {
        final int saveCount = canvas.getSaveCount();
        canvas.save();
        initRect();
        mThumbPaint.setStrokeWidth(thumbWidth);
        mTrackPaint.setStrokeWidth(trackWidth);
        canvas.rotate(-90, mRect.centerX(), mRect.centerY());
        canvas.drawArc(mRect, startAngle, sweepAngle, false, mThumbPaint);
        canvas.drawCircle(mRect.centerX(), mRect.centerY(), mRect.height() / 2, mTrackPaint);
        canvas.restoreToCount(saveCount);
    }


    private void setThumbColor(int thumbColor) {
        if (mThumbPaint.getColor() != thumbColor) {
            mThumbPaint.setColor(thumbColor);
        }
        this.thumbColor = thumbColor;
    }

    private void setTrackColor(int trackColor) {
        if (mTrackPaint.getColor() != trackColor) {
            mTrackPaint.setColor(trackColor);
        }
        this.trackColor = trackColor;
    }

    public float getStartAngle() {
        return startAngle;
    }

    public void setStartAngle(float startAngle) {
        this.startAngle = startAngle;
    }

    public float getSweepAngle() {
        return sweepAngle;
    }

    public void setSweepAngle(float sweepAngle) {
        this.sweepAngle = sweepAngle;
    }

    public float getTrackWidth() {
        return trackWidth;
    }

    public void setTrackWidth(float trackWidth) {
        this.trackWidth = trackWidth;
    }

    public Paint getmThumbPaint() {
        return mThumbPaint;
    }

    public int getThumbColor() {
        return thumbColor;
    }

    public float getThumbWidth() {
        return thumbWidth;
    }

    public void setThumbWidth(float thumbWidth) {
        this.thumbWidth = thumbWidth;
    }
}
