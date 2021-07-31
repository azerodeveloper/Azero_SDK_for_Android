
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
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.azero.sampleapp.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class WeatherNotSupportFragment extends WeatherFragmentBase {
    private static final String ARG_WEATHER_INFO = "ARG_WEATHER_INFO";

    private TextView titleDayView;
    private TextView titleWeekDayView;
    private TextView titleLocationView;
    private ImageView back;
    private TextView info;

    public WeatherNotSupportFragment() {
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
        return inflater.inflate(R.layout.weather_fragment_weather_not_support, container, false);
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
        Log.e("sss", "onResume");
        updateView();
    }

    public void initView(View view) {
        titleDayView = view.findViewById(R.id.main_title);
        titleWeekDayView = view.findViewById(R.id.week_day);
        titleLocationView = view.findViewById(R.id.city);
        back = view.findViewById(R.id.back_icon);
        info = view.findViewById(R.id.not_support_bg);
    }

    protected void updateView() {
        if (weather == null) return;

        info.setMovementMethod(ScrollingMovementMethod.getInstance());
        if(TextUtils.isEmpty(weather.getNotSupport().getMessage())){
            info.setText(weather.getNotSupport().getMessage());
        }else{
            info.setText(weather.getAnswer());
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
