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

package com.azero.sampleapp.activity.launcher.recommendation;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.azero.sampleapp.R;
import com.azero.sampleapp.widget.GradientView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

/**
 * Description:
 * Created by WangXin 19-1-18
 */
public class RecommendationFragment extends Fragment {

    public static final String ARG_RECOMMENDATION = "arg_recommendation";
    private static final int ANIMATION_MOVEMENT = 50;
    private static final int ANIMATION_MOVE_INTERVAL = 1000;

    RelativeLayout recommendationLayout;
    TextView mainTitle;
    TextView subTitle;
    RelativeLayout background;
    LinearLayout titleLayout;
    GradientView shadeView;
    Animation fadeinAnimation;
    Animation fadeoutAnimation;
    Animation moveRightAnimation;
    Animation moveLeftAnimation;
    Animation moveOutAnimation;


    private OnClickListener listener;

    public static RecommendationFragment newInstance(Recommendation recommendation) {
        RecommendationFragment fragment = new RecommendationFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_RECOMMENDATION, recommendation);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recommendation, container, false);
        recommendationLayout = rootView.findViewById(R.id.recommendation_layout);
        mainTitle = rootView.findViewById(R.id.title);
        background = rootView.findViewById(R.id.background);
        subTitle = rootView.findViewById(R.id.subTitle);
        shadeView = rootView.findViewById(R.id.shadeView);
        titleLayout = rootView.findViewById(R.id.title_layout);
        fadeinAnimation = new AlphaAnimation(0.0f, 1.0f);
        fadeinAnimation.setDuration(1000);
        fadeinAnimation.setFillAfter(true);
        fadeoutAnimation = new AlphaAnimation(1.0f, 0.0f);
        fadeoutAnimation.setDuration(1000);
        fadeoutAnimation.setFillAfter(true);
        moveRightAnimation = new TranslateAnimation(titleLayout.getTranslationX() - ANIMATION_MOVEMENT, titleLayout.getTranslationX(), titleLayout.getTranslationY(), titleLayout.getTranslationY());
        moveRightAnimation.setDuration(ANIMATION_MOVE_INTERVAL);
        moveLeftAnimation = new TranslateAnimation(titleLayout.getTranslationX() + ANIMATION_MOVEMENT, titleLayout.getTranslationX(), titleLayout.getTranslationY(), titleLayout.getTranslationY());
        moveLeftAnimation.setDuration(ANIMATION_MOVE_INTERVAL);
        moveOutAnimation = new TranslateAnimation(titleLayout.getTranslationX(), titleLayout.getTranslationX() - ANIMATION_MOVEMENT, titleLayout.getTranslationY(), titleLayout.getTranslationY());
        moveOutAnimation.setDuration(ANIMATION_MOVE_INTERVAL);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null) {
            Recommendation recommendation = arguments.getParcelable(ARG_RECOMMENDATION);
            if (recommendation != null) {
                if (recommendation.getType() == Recommendation.Type.TEMPLATE1) {
                    RequestOptions options = new RequestOptions().override(1920, 1080).centerCrop();
                    Glide
                            .with(this)
                            .load(recommendation.getBgUrl())
                            .apply(options)
                            .into(new SimpleTarget<Drawable>() {
                                @Override
                                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                    background.setBackground(resource);
                                }
                            });
                } else {
                    background.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.mCardBackground));
                    RequestOptions options = new RequestOptions().override(1920, 1080).centerCrop();
                    Glide
                            .with(background)
                            .load(recommendation.getBgUrl())
                            .apply(options)
                            .into(new SimpleTarget<Drawable>() {
                                @Override
                                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                    recommendationLayout.setBackground(resource);
                                }
                            });
                }

                if (!TextUtils.isEmpty(recommendation.getTitle())) {
                    shadeView.setVisibility(View.VISIBLE);
                    titleLayout.setVisibility(View.VISIBLE);
                    mainTitle.setText(recommendation.getTitle());
                    subTitle.setText(recommendation.getIntroduce());
                }
                recommendationLayout.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onClick(recommendation);
                    }
                });
            }
        }
    }

    public void setOnClickListener(OnClickListener listener) {
        this.listener = listener;
    }

    public void startFadeInAnimate(int direction) {
        if (recommendationLayout != null) {
            recommendationLayout.setVisibility(View.VISIBLE);
            recommendationLayout.startAnimation(fadeinAnimation);
            if (direction == 1) {
                titleLayout.startAnimation(moveRightAnimation);
            } else {
                titleLayout.startAnimation(moveLeftAnimation);
            }
        }
    }

    public void startFadeOutAnimate() {
        if (recommendationLayout != null) {
            recommendationLayout.startAnimation(fadeoutAnimation);
            titleLayout.startAnimation(moveOutAnimation);
        }
    }

    public interface OnClickListener {
        void onClick(Recommendation recommendation);
    }
}