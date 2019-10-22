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
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.azero.sampleapp.R;
import com.bumptech.glide.Glide;
import com.azero.sampleapp.activity.weather.data.WeatherNow;;


public class WeatherNowInfoFragment extends WeatherFragmentBase {
    private static final String ARG_WEATHER_INFO = "ARG_WEATHER_INFO";
    private TextView currentTmpView;
    private TextView weatherView;
    private TextView dayTemptureView;
    private TextView windView;
    private ImageView weatherIcon;
    private ImageView windIcon;
    private ImageView backgroundView;
    public WeatherNowInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.weather_fragment_weather_now_info_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        updateView();
    }

    public void initView(View view) {
        currentTmpView = view.findViewById(R.id.current_tempture);
        weatherView = view.findViewById(R.id.weither);
        windView = view.findViewById(R.id.wind);
        weatherIcon = view.findViewById(R.id.weather_icon);
        windIcon = view.findViewById(R.id.wind_icon);
        //backgroundView = view.findViewById(R.id.bg);
        dayTemptureView = view.findViewById(R.id.day_tempture);
    }

    protected void updateView() {
        if (weather == null) return;
        WeatherNow now = weather.getNow();
        currentTmpView.setText(getString(R.string.weather_temperature,now.getTemperature()));
        weatherView.setText(now.getText());
        windView.setText(getString(R.string.weather_wind, now.getWindDirection()));
        dayTemptureView.setText(getString(R.string.weather_day_temprure, weather.getWeather().getLow(), weather.getWeather().getHigh()));
        Glide.with(this).load(now.getIconUrl()).into(weatherIcon);
        //Glide.with(this).load(now.getBackgroundUrl()).into(backgroundView);
        Glide.with(this).load(now.getWindDirectionIcon()).into(windIcon);

    }


}
