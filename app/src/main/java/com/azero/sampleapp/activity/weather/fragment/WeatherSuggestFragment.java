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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.azero.sampleapp.R;
import com.bumptech.glide.Glide;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A simple {@link } subclass.
 * Use the {@link WeatherSuggestFragment} factory method to
 * create an instance of this fragment.
 */
public class WeatherSuggestFragment extends WeatherFragmentBase {
    private static final String ARG_WEATHER_INFO = "ARG_WEATHER_INFO";
    TextView carInfo;
    TextView umbrellaInfo;
    TextView clothInfo;
    ImageView carIcon;
    ImageView umbrellaIcon;
    ImageView clothIcon;
    public WeatherSuggestFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.weather_fragment_weather_suggest, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        updateView();
    }

    public void initView(View view) {
        carInfo = view.findViewById(R.id.car_info);
        umbrellaInfo = view.findViewById(R.id.umbrella_info);
        clothInfo = view.findViewById(R.id.cloth_info);
        carIcon = view.findViewById(R.id.car_info_icon);
        umbrellaIcon =view.findViewById(R.id.umbrella_icon);
        clothIcon = view.findViewById(R.id.cloth_icon);
    }

    protected void updateView() {
        if (weather == null) return;


        if (weather.getSuggestion().getDressing() != null) {
            carInfo.setText(weather.getSuggestion().getDressing().getSuggestion());
            Glide.with(this).load(weather.getSuggestion().getDressing().getIconUrl()).into(carIcon);
        }
        if (weather.getSuggestion().getUmbrella() != null) {
            umbrellaInfo.setText(weather.getSuggestion().getUmbrella().getSuggestion());
            Glide.with(this).load(weather.getSuggestion().getUmbrella().getIconUrl()).into(umbrellaIcon);
        }
        if (weather.getSuggestion().getCarWashing() != null) {
            clothInfo.setText(weather.getSuggestion().getCarWashing().getSuggestion());
            Glide.with(this).load(weather.getSuggestion().getCarWashing().getIconUrl()).into(clothIcon);
        }

    }
}
