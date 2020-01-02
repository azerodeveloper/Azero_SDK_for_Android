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

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import com.azero.sampleapp.activity.launcher.recommendation.Recommendation;
import com.azero.sampleapp.activity.launcher.recommendation.RecommendationFragment;
import com.azero.sdk.util.log;

import java.util.ArrayList;
import java.util.List;


/**
 * Description:
 * Created by WangXin 19-1-26
 */
public abstract class LoopFragmentPagerAdapter extends FragmentStatePagerAdapter implements ViewPager.OnPageChangeListener {

    private static final int LEFT_TO_RIGHT = 0;

    private static final int RIGHT_TO_LEFT = 1;

    private static final int DEFAULT_POSITION = 1;

    protected ViewPager viewPager;
    protected List<RecommendationFragment> fragments;
    private int firstItemPosition;
    private int lastItemPosition;
    private int currentPos;
    private boolean isChanged = false;
    private int lastPos;
    private ViewPageHelper viewPageHelper;

    public LoopFragmentPagerAdapter(ViewPager vp, FragmentManager fm) {
        super(fm);
        viewPager = vp;
        viewPager.addOnPageChangeListener(this);
        fragments = new ArrayList<>();
        viewPageHelper = new ViewPageHelper(vp);
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    public int getCurrentPos() {
        return currentPos;
    }

    public void updateFragments(List<Recommendation> recommendations) {
        updateFragments(recommendations, DEFAULT_POSITION);
    }

    public void updateFragments(List<Recommendation> recommendations, int position) {
        if (recommendations == null || recommendations.isEmpty()) {
            log.e("updateFragments: recommendation list is empty");
            return;
        }
        if (position < firstItemPosition) {
            position = firstItemPosition;
        } else if (position > lastItemPosition) {
            position = lastItemPosition;
        }
        sortData(recommendations);
        notifyDataSetChanged();
        viewPager.setCurrentItem(position);
        if (recommendations.size() > 1) {
            firstItemPosition = 1;
            lastItemPosition = fragments.size() - 2;
        }
        fragments.get(position).startFadeInAnimate(1);
    }

    private void sortData(List<Recommendation> recommendations) {
        fragments.clear();
        for (Recommendation recommendation : recommendations) {
            RecommendationFragment recommendationFragment = RecommendationFragment.newInstance(recommendation);
            fragments.add(recommendationFragment);
        }
        if (recommendations.size() > 1) {
            fragments.add(0, RecommendationFragment.newInstance(recommendations.get(recommendations.size() - 1)));
            fragments.add(RecommendationFragment.newInstance(recommendations.get(0)));
        }

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }


    @Override
    public void onPageSelected(int position) {
        currentPos = position;
        if (position > lastItemPosition) {
            isChanged = true;
            currentPos = firstItemPosition;
        } else if (position < firstItemPosition) {
            isChanged = true;
            currentPos = lastItemPosition;
        }
        if (isChanged) {
            isChanged = false;
            viewPageHelper.setCurrentItem(currentPos, true);
            return;
        }
        //文字滑动方向判断
        int direction;
        if (currentPos == lastItemPosition && lastPos == firstItemPosition) {
            //从第一页滑到最后一页
            direction = RIGHT_TO_LEFT;
        } else if (currentPos == firstItemPosition && lastPos == lastItemPosition) {
            //最后一页滑到第一页
            direction = LEFT_TO_RIGHT;
        } else if (currentPos - lastPos > 0) {
            direction = LEFT_TO_RIGHT;
        } else {
            direction = RIGHT_TO_LEFT;
        }
        fragments.get(currentPos).startFadeInAnimate(direction);
        lastPos = currentPos;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
}
