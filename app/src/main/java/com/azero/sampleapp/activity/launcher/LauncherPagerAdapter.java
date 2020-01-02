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

package com.azero.sampleapp.activity.launcher;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;

import com.azero.sampleapp.activity.launcher.recommendation.Recommendation;
import com.azero.sampleapp.activity.launcher.recommendation.RecommendationFragment;
import com.azero.sampleapp.activity.launcher.viewmodel.LauncherViewModel;
import com.google.android.gms.common.util.CollectionUtils;


/**
 * Description:
 * Created by WangXin 19-1-18
 */
public class LauncherPagerAdapter extends LoopFragmentPagerAdapter {

    private Handler handler;
    private LoopRunnable loopRunnable;
    private SwitchRunnable switchRunnable;
    private LauncherViewModel launcherViewModel;

    private boolean mRunning = false;

    private long interval = 3000;

    public LauncherPagerAdapter(ViewPager vp, FragmentManager fm) {
        super(vp, fm);
        handler = new Handler();
        loopRunnable = new LoopRunnable();
        switchRunnable = new SwitchRunnable();
        registerTouchListener();
        initListener();
    }

    private void initListener() {
        ViewPager.OnPageChangeListener listener = new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // do nothing
            }

            @Override
            public void onPageSelected(int position) {
                if (launcherViewModel == null || CollectionUtils.isEmpty(fragments)) {
                    return;
                }
                // 滑动到倒数第二个，拉取下一组Launcher（为了实现无限轮播，会在最后添一个item，所以倒数第二个是 size - 3）
                if (position != POSITION_UNCHANGED && position != POSITION_NONE && position == fragments.size() - 3) {
                    Bundle arguments = fragments.get(fragments.size() - 2).getArguments();
                    Recommendation recommendation = arguments.getParcelable(RecommendationFragment.ARG_RECOMMENDATION);
                    if (recommendation != null) {
                        launcherViewModel.updateRecommendations(recommendation.getContentId());
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // do nothing
            }
        };
        viewPager.addOnPageChangeListener(listener);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void registerTouchListener() {
        viewPager.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    if (loopRunnable != null) {
                        loopRunnable.stop();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (loopRunnable != null) {
                        loopRunnable.start();
                    }
                    break;
                default:
                    break;
            }
            return false;
        });

    }

    public void setLauncherViewModel(LauncherViewModel launcherViewModel) {
        this.launcherViewModel = launcherViewModel;
    }

    public void setLoopInterval(long interval) {
        this.interval = interval;
    }

    public void startLoop() {
        if (loopRunnable != null) {
            if (!mRunning) {
                loopRunnable.start();
                mRunning = true;
            }
        }
    }

    public void stopLoop() {
        if (loopRunnable != null) {
            if (mRunning) {
                loopRunnable.stop();
                mRunning = false;
            }
        }
    }

    public void onDestroy() {
        if (loopRunnable != null) {
            loopRunnable.destroy();
            mRunning = false;
        }
    }

    class LoopRunnable implements Runnable {
        public void start() {
            handler.postDelayed(this, interval);
        }

        public void stop() {
            handler.removeCallbacksAndMessages(null);
        }

        public void destroy() {
            handler.removeCallbacksAndMessages(null);
        }

        @Override
        public void run() {
            if (CollectionUtils.isEmpty(fragments) || fragments.size() == 1) {
                return;
            }
            int currentItem = viewPager.getCurrentItem();
            fragments.get(currentItem).startFadeOutAnimate();
            handler.postDelayed(switchRunnable, 1000);
        }
    }

    class SwitchRunnable implements Runnable {
        @Override
        public void run() {
            int currentItem = viewPager.getCurrentItem();
            viewPager.setCurrentItem(currentItem + 1);
            handler.postDelayed(loopRunnable, interval);
        }
    }
}