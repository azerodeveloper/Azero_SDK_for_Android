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

package com.azero.sampleapp.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Scroller;

import com.azero.sampleapp.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @Author: sjy
 * @Date: 2019/9/3
 */
public class SlidingView extends FrameLayout {

    /**
     * @param SHADOW_WIDTH 阴影宽度默认值
     * @param SNAP_VELOCITY 最低滑动速度
     */
    private static final int SHADOW_WIDTH = 16;
    private static final int SNAP_VELOCITY = 600;

    private Activity mActivity;
    private Scroller mScroller;
    private Drawable mLeftShadow;

    private VelocityTracker mVelocityTracker;

    private int mShadowWidth;
    private int mLastInterceptX;
    private int mLastInterceptY;
    private int mLastTouchX;
    private int mLastTouchY;
    private int mTouchSlop;

    private boolean mIsConsumed = false;
    private boolean mIsSlide = false;

    public SlidingView(@NonNull Context context) {
        this(context, null);
    }

    public SlidingView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        initView(context);
    }

    private void initView(Context context) {
        mScroller = new Scroller(context);
        mLeftShadow = getResources().getDrawable(R.drawable.shape_left_shadow);
        int density = (int) getResources().getDisplayMetrics().density;
        mShadowWidth = SHADOW_WIDTH * density;
    }

    public void onBindActivity(Activity activity) {
        mActivity = activity;
        ViewGroup decorView = (ViewGroup) mActivity.getWindow().getDecorView();
        View child = decorView.getChildAt(0);
        decorView.removeView(child);
        addView(child);
        decorView.addView(this);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        mIsSlide = false;
        int x = (int) motionEvent.getX();
        int y = (int) motionEvent.getY();
        executeObtainVelocity(motionEvent);

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsSlide = false;
                executeSetPosition(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                executeCheckSlideState(x, y);
                break;
            case MotionEvent.ACTION_UP:
                executeReleaseParameter();
                executeReleaseVelocity();
                break;
            default:
                break;
        }
        return mIsSlide;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        int x = (int) motionEvent.getX();
        int y = (int) motionEvent.getY();

        executeObtainVelocity(motionEvent);
        mVelocityTracker.computeCurrentVelocity(1000);
        float xVelocity = mVelocityTracker.getXVelocity();
        float yVelocity = mVelocityTracker.getYVelocity();

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastTouchX = x;
                mLastTouchY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                executeViewScroll(motionEvent, x, y, xVelocity, yVelocity);
                break;
            case MotionEvent.ACTION_UP:
                executeViewController(xVelocity);

                mIsConsumed = false;
                mLastTouchX = mLastTouchY = 0;
                executeReleaseVelocity();
                break;
            default:
                break;
        }
        executeReleaseVelocity();
        return true;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), 0);
            postInvalidate();
        } else if (-getScrollX() >= getWidth()) {
            mActivity.finish();
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        executeDrawShadow(canvas);
    }

    private void executeViewController(float xVelocity) {
        int halfWidth = getWidth() / 2;
        if (-getScrollX() > halfWidth) {
            executeScrollClose();
        } else if (xVelocity > SNAP_VELOCITY) {
            executeScrollClose();
        } else if (xVelocity <= -SNAP_VELOCITY) {
            executeScrollBack();
        } else {
            executeScrollBack();
        }
    }

    private void executeViewScroll(MotionEvent motionEvent, int x, int y, float xVelocity, float yVelocity) {
        int deltaX = x - mLastTouchX;
        int deltaY = y - mLastTouchY;

        mIsConsumed = (!mIsConsumed && (Math.abs(deltaX) > mTouchSlop) && (Math.abs(deltaX) > Math.abs(deltaY))) ||
                ((Math.abs(xVelocity) > SNAP_VELOCITY) && (Math.abs(xVelocity) > Math.abs(yVelocity)));

        if (mIsConsumed) {
            int rightMovedX = mLastTouchX - (int) motionEvent.getX();
            // 左侧即将滑出屏幕
            if (getScrollX() + rightMovedX >= 0) {
                scrollTo(0, 0);
            } else {
                scrollBy(rightMovedX, 0);
            }
        }
        mLastTouchX = x;
        mLastTouchY = y;
    }

    private void executeSetPosition(int x, int y) {
        mLastInterceptX = x;
        mLastInterceptY = y;
    }

    private void executeCheckSlideState(int x, int y) {
        mVelocityTracker.computeCurrentVelocity(1000);
        float xVelocity = mVelocityTracker.getXVelocity();
        float yVelocity = mVelocityTracker.getYVelocity();

        int deltaX = x - mLastInterceptX;
        int deltaY = y - mLastInterceptY;
        // 当横向滑动距离大于纵向，或横向滑动速度大于纵向滑动速度时判定为横向滑动
        if (Math.abs(xVelocity) > SNAP_VELOCITY && Math.abs(xVelocity) > Math.abs(yVelocity)
                || Math.abs(deltaX) > mTouchSlop && Math.abs(deltaX) > Math.abs(deltaY)) {
            mIsSlide = true;
        } else {
            mIsSlide = false;
        }
        mLastInterceptX = x;
        mLastInterceptY = y;
    }

    private void executeScrollBack() {
        int startX = getScrollX();
        int dx = -getScrollX();
        mScroller.startScroll(startX, 0, dx, 0, 300);
        invalidate();
    }

    private void executeScrollClose() {
        int startX = getScrollX();
        int dx = -getScrollX() - getWidth();
        mScroller.startScroll(startX, 0, dx, 0, 300);
        invalidate();
    }

    private void executeDrawShadow(Canvas canvas) {
        mLeftShadow.setBounds(0, 0, mShadowWidth, getHeight());
        canvas.save();
        canvas.translate(-mShadowWidth, 0);
        mLeftShadow.draw(canvas);
        canvas.restore();
    }

    private void executeObtainVelocity(MotionEvent motionEvent) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(motionEvent);
    }

    private void executeReleaseParameter() {
        mIsSlide = false;
        mLastInterceptX = mLastInterceptY = 0;
    }

    private void executeReleaseVelocity() {
        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }
}
