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

package com.azero.sampleapp.activity.alert;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.azero.sdk.util.log;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class AlertsRecyclerView extends RecyclerView {

    /**
     * @param INVALID_POSITION 触摸到的点不在子View范围内
     * @param INVALID_CHILD_WIDTH 子ItemView不含两个子View
     * @param SNAP_VELOCITY 最小滑动速度
     * @param MAX_ALPHA 最大不透明度
     * @param CHILD_VIEW_NUM ViewGroup中子View数量
     */

    private static final int INVALID_POSITION = -1;
    private static final int INVALID_CHILD_WIDTH = -1;
    private static final int SNAP_VELOCITY = 600;
    private static final int MAX_ALPHA = 100;
    private static final int CHILD_VIEW_NUM = 2;

    private VelocityTracker mVelocityTracker;
    private Rect mTouchFrame;
    private Scroller mScroller;
    private ViewGroup mFlingView;

    /**
     * @param mLastTouchX 滑动过程中记录上次触碰点X
     * @param mFirstX 首次触碰范围X
     * @param mFirstY 首次触碰范围Y
     */

    private float mLastTouchX;
    private float mFirstX;
    private float mFirstY;

    private boolean mIsSlide;

    /**
     * @param mPosition 触碰的View的位置
     * @param mMenuViewWidth 菜单按钮宽度
     * @param mTouchSlop 滑动最小距离
     */

    private int mPosition;
    private int mMenuViewWidth;
    private int mTouchSlop;


    public AlertsRecyclerView(Context context) {
        this(context, null);
    }

    public AlertsRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AlertsRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mScroller = new Scroller(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        int x = (int) motionEvent.getX();
        int y = (int) motionEvent.getY();
        executeObtainVelocity(motionEvent);
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                executeSetPosition(x, y);
                executeCloseUnClickedMenu();
                break;
            case MotionEvent.ACTION_MOVE:
                if (executeViewScroll(x, y)) {
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                executeReleaseVelocity();
                break;
            default:
                break;
        }
        return super.onInterceptTouchEvent(motionEvent);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (mIsSlide && mPosition != INVALID_POSITION) {
            float x = motionEvent.getX();
            float y = motionEvent.getY();
            executeObtainVelocity(motionEvent);

            mVelocityTracker.computeCurrentVelocity(1000);
            float xVelocity = mVelocityTracker.getXVelocity();
            float yVelocity = mVelocityTracker.getYVelocity();

            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mLastTouchX = x;
                    break;
                case MotionEvent.ACTION_MOVE:
                    executeMenuScroll(x);
                    break;
                case MotionEvent.ACTION_UP:
                    executeMenuViewController(xVelocity);
                    executeReleaseParameter();
                    executeReleaseVelocity();
                    break;
                default:
                    break;
            }
            return true;
        } else {
            executeCloseMenu();
            executeReleaseVelocity();
        }
        return super.onTouchEvent(motionEvent);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mFlingView.scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
    }

    private void executeCloseUnClickedMenu() {
        if (mPosition != INVALID_POSITION) {
            ViewGroup view = mFlingView;
            mFlingView = (ViewGroup) getChildAt(mPosition - ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition());

            if (view != null && mFlingView != view && view.getScrollX() != 0) {
                view.scrollTo(0, 0);
                view.getChildAt(0).getBackground().setAlpha(0);
            }

            executeSetMenuViewWidth();
        }
    }

    private void executeSetMenuViewWidth() {
        if (mFlingView.getChildCount() == CHILD_VIEW_NUM) {
            mMenuViewWidth = mFlingView.getChildAt(1).getWidth();
        } else {
            mMenuViewWidth = INVALID_CHILD_WIDTH;
        }
    }

    private void executeSetPosition(int x, int y) {
        mFirstX = mLastTouchX = x;
        mFirstY = y;
        mPosition = executePointToPosition(x, y);
    }

    private boolean executeViewScroll(int x, int y) {
        mVelocityTracker.computeCurrentVelocity(1000);
        float xVelocity = mVelocityTracker.getXVelocity();
        float yVelocity = mVelocityTracker.getYVelocity();

        boolean isSlide = (((Math.abs(xVelocity) > SNAP_VELOCITY) && (Math.abs(xVelocity) > Math.abs(yVelocity))) ||
                ((Math.abs(x - mFirstX) >= mTouchSlop) && (Math.abs(x - mFirstX) > Math.abs(y - mFirstY))));
        if (isSlide) {
            mIsSlide = true;
        }
        return isSlide;
    }

    private void executeMenuScroll(float x) {
        if (mMenuViewWidth != INVALID_CHILD_WIDTH) {
            float deltaX = mLastTouchX - x;
            float viewX = mFlingView.getScrollX();
            boolean mIsConsumed = (viewX + deltaX <= mMenuViewWidth && viewX + deltaX >= 0);

            if (mIsConsumed) {
                mFlingView.scrollBy((int) deltaX, 0);
                int alpha = (int) (MAX_ALPHA * (mFlingView.getScrollX() + Math.abs(deltaX)) / mMenuViewWidth);
                mFlingView.getChildAt(0).getBackground().setAlpha(alpha);
            }
            mLastTouchX = x;
        }
    }

    private void executeMenuViewController(float xVelocity) {
        if (mMenuViewWidth != INVALID_CHILD_WIDTH) {
            int scrollX = mFlingView.getScrollX();
            int halfMenuViewWidth = mMenuViewWidth / 2;

            if (xVelocity < -SNAP_VELOCITY) {
                executeOpenMenu(scrollX);
            } else if (xVelocity >= SNAP_VELOCITY) {
                executeCloseMenu(scrollX);
            } else if (scrollX >= halfMenuViewWidth) {
                executeOpenMenu(scrollX);
            } else {
                executeCloseMenu(scrollX);
            }
            invalidate();
        }
    }

    private void executeCloseMenu(int scrollX) {
        mScroller.startScroll(scrollX, 0, -scrollX, 0, Math.abs(scrollX));
        mFlingView.getChildAt(0).getBackground().setAlpha(0);
    }

    private void executeOpenMenu(int scrollX) {
        mScroller.startScroll(scrollX, 0, mMenuViewWidth - scrollX, 0, Math.abs(mMenuViewWidth - scrollX));
        mFlingView.getChildAt(0).getBackground().setAlpha(MAX_ALPHA);
    }

    private void executeReleaseParameter() {
        mIsSlide = false;
        mLastTouchX = 0;
        mMenuViewWidth = INVALID_CHILD_WIDTH;
        mPosition = INVALID_POSITION;
    }

    private void executeReleaseVelocity() {
        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void executeObtainVelocity(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    public int executePointToPosition(int x, int y) {
        int firstPosition = ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
        Rect frame = mTouchFrame;
        if (frame == null) {
            mTouchFrame = new Rect();
            frame = mTouchFrame;
        }
        final int count = getChildCount();
        for (int i = count - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            if (child.getVisibility() == View.VISIBLE) {
                child.getHitRect(frame);
                if (frame.contains(x, y)) {
                    log.d("firstPosition: " + firstPosition + ", i: " + i);
                    return firstPosition + i;
                }
            }
        }
        return INVALID_POSITION;
    }

    private void executeCloseMenu() {
        if (mFlingView != null && mFlingView.getScrollX() != 0) {
            mFlingView.scrollTo(0, 0);
            mFlingView.getChildAt(0).getBackground().setAlpha(0);
        }
    }
}
