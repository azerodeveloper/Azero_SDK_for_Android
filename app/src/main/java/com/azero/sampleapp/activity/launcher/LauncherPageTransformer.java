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

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.RelativeLayout;

import com.azero.sampleapp.R;
import com.azero.sdk.util.log;


/**
 * Description:
 * Created by WangXin 19-1-22
 */
public class LauncherPageTransformer implements ViewPager.PageTransformer {

    @Override
    public void transformPage(@NonNull View page, float position) {
        ViewHolder viewHolder;

        if (page.getTag() == null) {
            viewHolder = new ViewHolder();
            viewHolder.recommendationLayout = page.findViewById(R.id.recommendation_layout);
            page.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) page.getTag();
        }

        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            handleInvisiblePage(page, position);
        } else if (position <= 0) { // [-1,0]
            // Use the default slide transition when moving to the left page
            handleLeftPage(page, position, viewHolder);
        } else if (position <= 1) { // (0,1]
            // Fade the page out.
            handleRightPage(page, position, viewHolder);
        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
            handleInvisiblePage(page, position);
        }
    }

    private void handleInvisiblePage(View page, float position) {
        page.setAlpha(0);
    }

    private void handleLeftPage(View page, float position, ViewHolder viewHolder) {
        int pageWidth = page.getWidth();

        if (viewHolder.recommendationLayout != null) {
            viewHolder.recommendationLayout.setAlpha(1 + position);

            // Counteract the default slide transition
            viewHolder.recommendationLayout.setTranslationX(pageWidth * (position * 0.08f));
        }

        page.setTranslationX(-pageWidth * position);

        page.setPivotX(page.getWidth() * 0.5f);
        page.setPivotY(page.getHeight() * 0.5f);

        page.setAlpha(1 + position);
    }

    private void handleRightPage(View page, float position, ViewHolder viewHolder) {
        int pageWidth = page.getWidth();

        if (viewHolder.recommendationLayout != null) {
            // Fade the page out.
            viewHolder.recommendationLayout.setAlpha(1 - position);

            // Counteract the default slide transition
            viewHolder.recommendationLayout.setTranslationX(pageWidth * (position * 0.08f));
        }

        page.setTranslationX(-pageWidth * position);

        page.setPivotX(page.getWidth() * 0.5f);
        page.setPivotY(page.getHeight() * 0.5f);

        page.setAlpha(1 - position);
    }

    static class ViewHolder {
        RelativeLayout recommendationLayout;
    }
}
