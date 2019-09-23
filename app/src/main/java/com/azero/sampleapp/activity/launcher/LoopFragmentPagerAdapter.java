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

    protected ViewPager viewPager;
    protected List<RecommendationFragment> fragments;
    private int FIRST_ITEM_POSITION;
    private int LAST_ITEM_POSITION;
    private boolean isChanged = false;
    private int currentPos;
    private int lastPos;
    //用于判断文字滑动方向
    // left to right  0
    // right to left 1
    private int direction = 0;
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

    public void initFragments(List<Recommendation> recommendations) {
        if (!(recommendations != null && recommendations.size() > 0))
            throw new NullPointerException("Couldn't init Fragments");
        sortData(recommendations);
        notifyDataSetChanged();
        viewPager.setCurrentItem(1);
        FIRST_ITEM_POSITION = 1;
        LAST_ITEM_POSITION = fragments.size() - 2;
        fragments.get(FIRST_ITEM_POSITION).startFadeInAnimate(1);
    }

    private void sortData(List<Recommendation> recommendations) {
        fragments.add(RecommendationFragment.newInstance(recommendations.get(recommendations.size() - 1)));
        for (Recommendation recommendation : recommendations) {
            RecommendationFragment recommendationFragment = RecommendationFragment.newInstance(recommendation);
            fragments.add(recommendationFragment);
        }
        fragments.add(RecommendationFragment.newInstance(recommendations.get(0)));
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }


    @Override
    public void onPageSelected(int position) {
        currentPos = position;
        if (position > LAST_ITEM_POSITION) {
            isChanged = true;
            currentPos = FIRST_ITEM_POSITION;
        } else if (position < FIRST_ITEM_POSITION) {
            isChanged = true;
            currentPos = LAST_ITEM_POSITION;
        }
        if (isChanged) {
            isChanged = false;
            viewPageHelper.setCurrentItem(currentPos, true);
            return;
        }
        //文字滑动方向判断
        if (currentPos == LAST_ITEM_POSITION && lastPos == FIRST_ITEM_POSITION) {
            //从第一页滑到最后一页
            direction = 1;
        } else if (currentPos == FIRST_ITEM_POSITION && lastPos == LAST_ITEM_POSITION) {
            //最后一页滑到第一页
            direction = 0;
        } else if (currentPos - lastPos > 0) {
            direction = 0;
        } else {
            direction = 1;
        }
        fragments.get(currentPos).startFadeInAnimate(direction);
        lastPos = currentPos;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
}
