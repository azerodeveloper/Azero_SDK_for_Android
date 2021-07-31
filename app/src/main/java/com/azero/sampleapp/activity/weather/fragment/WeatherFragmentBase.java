
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

import android.content.Context;
import android.os.Bundle;


import com.azero.sampleapp.activity.weather.data.Weather;

import androidx.fragment.app.Fragment;


public class WeatherFragmentBase extends Fragment {
    protected Weather weather;
    private WeatherFragmentInterface weatherFragmentInterface;

    public void updateData(Weather weather) {
        if(this.weather!=weather){
            this.weather = weather;
            if(isResumed()||isAdded()) {
                updateView();
            }
        }
    }
    protected void updateView() {

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(weatherFragmentInterface!=null){
            weather = weatherFragmentInterface.getWeather();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(weatherFragmentInterface!=null){
            updateData(weatherFragmentInterface.getWeather());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof WeatherFragmentInterface){
            weatherFragmentInterface = (WeatherFragmentInterface) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        weatherFragmentInterface = null;
    }
}
