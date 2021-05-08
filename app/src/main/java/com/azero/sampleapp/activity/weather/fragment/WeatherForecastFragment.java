
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.azero.sampleapp.R;
import com.azero.sampleapp.activity.weather.adapter.WeatherForecastAdapter;

public class WeatherForecastFragment extends WeatherFragmentBase {
    private static final String ARG_WEATHER_INFO = "ARG_WEATHER_INFO";
    private RecyclerView recyclerView;
    private WeatherForecastAdapter weatherForecastAdapter;
    private TextView titleDayView;
    private TextView titleWeekDayView;
    private TextView titleLocationView;
    private ImageView back;
    private ImageView bg;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.weather_fragment_weather_frecast_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recy_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        weatherForecastAdapter = new WeatherForecastAdapter();

        recyclerView.setAdapter(weatherForecastAdapter);
        // recyclerView.addItemDecoration(new ShortDecoration(getContext(),R.style.ShortLineDecoration));

        initView(view);
        updateView();
        if (weather != null) {
            if (weather.getDaily() != null) {
                weatherForecastAdapter.setNewData(weather.getDaily().getDaily());
            }
        }


    }

    public void initView(View view) {
        titleDayView = view.findViewById(R.id.main_title);
        titleWeekDayView = view.findViewById(R.id.week_day);
        titleLocationView = view.findViewById(R.id.city);
        back = view.findViewById(R.id.back_icon);
        bg = view.findViewById(R.id.bg);
    }

    protected void updateView() {
     }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void clearData() {
        if (weatherForecastAdapter.getData() != null) {
            weatherForecastAdapter.getData().clear();
        }
    }
}
