
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

package com.azero.sampleapp.activity.weather.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.azero.sampleapp.R;
import com.azero.sampleapp.activity.weather.widget.SquareTextView;
import com.bumptech.glide.Glide;

public class WeatherAirInfoFragment extends WeatherFragmentBase {
    private static final String ARG_WEATHER_INFO = "ARG_WEATHER_INFO";
    private SquareTextView airQualityInfo;
    private TextView airQualityValue;
    private SquareTextView ultravioletRayInfo;
    private TextView ultravioletRayValue;
    private ImageView airQualityIcon;
    private ImageView ultravioletRayIcon;
    private ViewGroup multiContainer;
    private ViewGroup singleContainer;
    private TextView singleInfoName;
    private SquareTextView singleInfoLevel;
    private TextView singleInfoValue;

    public WeatherAirInfoFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.weather_fragment_weather_air_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        updateView();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();
    }

    public void initView(View view) {
        airQualityInfo = view.findViewById(R.id.air_quality_info);
        airQualityValue = view.findViewById(R.id.air_quality_value);
        ultravioletRayInfo = view.findViewById(R.id.ultraviolet_ray_info);
        ultravioletRayValue = view.findViewById(R.id.ultraviolet_ray_value);
        airQualityIcon = view.findViewById(R.id.air_quality_icon);
        ultravioletRayIcon = view.findViewById(R.id.ultraviolet_ray_icon);
        multiContainer = view.findViewById(R.id.multi_info_container);
        singleContainer = view.findViewById(R.id.single_info_container);
        singleInfoName = view.findViewById(R.id.single_info_name);
        singleInfoLevel = view.findViewById(R.id.single_info_level);
        singleInfoValue = view.findViewById(R.id.single_info_value);
    }

    protected void updateView() {
        if (weather == null) return;
        if (weather.getAir() != null && weather.getUv() != null) {
            singleContainer.setVisibility(View.GONE);
            multiContainer.setVisibility(View.VISIBLE);
            ultravioletRayInfo.setText(weather.getUv().getBrief());
            ultravioletRayInfo.setSweepAngle(360f * weather.getUv().getLevel() / weather.getUv().getMaxLevel());
            ultravioletRayValue.setText(weather.getUv().getSuggest());
            airQualityInfo.setText(weather.getAir().getQuality());
            airQualityInfo.setSweepAngle(360f * weather.getAir().getLevel() / weather.getAir().getMaxLevel());
            airQualityValue.setText(weather.getAir().getPm25());
            Glide.with(this).load(weather.getAir().getIconUrl()).into(airQualityIcon);
            Glide.with(this).load(weather.getUv().getIconUrl()).into(ultravioletRayIcon);
        } else if (weather.getAir() != null) {
            singleContainer.setVisibility(View.VISIBLE);
            multiContainer.setVisibility(View.GONE);
            singleInfoName.setText(getActivity().getResources().getString(R.string.weather_air_quality));
            singleInfoName.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.weather_kongqizhishu, 0, 0, 0);
            singleInfoLevel.setText(weather.getAir().getQuality());
            singleInfoLevel.setSweepAngle(360f * weather.getAir().getLevel() / weather.getAir().getMaxLevel());
            singleInfoValue.setText(weather.getAir().getPm25());
        } else if (weather.getUv() != null) {
            singleContainer.setVisibility(View.VISIBLE);
            multiContainer.setVisibility(View.GONE);
            singleInfoName.setText(getActivity().getResources().getString(R.string.weather_ultraviolet_ray));
            singleInfoName.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.weather_ziwaixian, 0, 0, 0);
            singleInfoLevel.setText(weather.getUv().getBrief());
            singleInfoLevel.setSweepAngle(360f * weather.getUv().getLevel() / weather.getUv().getMaxLevel());
            singleInfoValue.setText(weather.getUv().getSuggest());
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

}
