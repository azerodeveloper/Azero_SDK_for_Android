package com.azero.sampleapp.activity.weather.adapter;

/*
https://www.jianshu.com/p/8f8ccdcf3580

Copyright (c) $today.year SoundAI. All Rights Reserved

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
specific language governing permissions and limitations under the License.
*/


import android.widget.ImageView;

import com.azero.sampleapp.R;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.azero.sampleapp.activity.weather.data.WeatherDaily;

import java.text.SimpleDateFormat;

public class WeatherForecastAdapter extends BaseQuickAdapter<WeatherDaily, BaseViewHolder> {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public WeatherForecastAdapter() {
        super(R.layout.weather_item_weather_forecast_layout);
    }

    @Override
    protected void convert(BaseViewHolder helper, WeatherDaily item) {
        helper.setText(R.id.weekDay, item.getWeek());
        helper.setText(R.id.day, item.getMonthDay());

        helper.setText(R.id.tempeture_high, item.getHigh() + "℃");
        helper.setText(R.id.tempeture_low, item.getLow() + "℃");
        ImageView imageView = helper.getView(R.id.icon);
        Glide.with(imageView).load(item.getIconUrl()).into(imageView);
    }
}
