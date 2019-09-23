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

package com.azero.sampleapp.activity.template;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.azero.sampleapp.R;
import com.azero.sdk.util.Constant;
import com.azero.sdk.util.log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 天气展示界面
 */
public class WeatherActivity extends BaseDisplayCardActivity {
    private TextView mMainTitle;
    private TextView mSubTitle;
    private ImageView mCurrentWeatherIcon;
    private TextView mCurrentWeather;
    private TextView mHighTemp;
    private TextView mLowTemp;
    private View[] mForecasts = new View[5];

    @Override
    protected int getLayoutResId() {
        return R.layout.card_weather_template;
    }

    @Override
    protected void initData(Intent intent) {
        try {
            JSONObject template = new JSONObject(intent.getStringExtra(Constant.EXTRA_TEMPLATE));
            ConfigureTemplateView.configureWeatherTemplate(this, template);
        } catch (JSONException e) {
            log.e(e.getMessage());
            finish();
        }
    }

    @Override
    protected void initView() {
        mMainTitle = findViewById(R.id.mainTitle);
        mSubTitle = findViewById(R.id.subTitle);
        mCurrentWeatherIcon = findViewById(R.id.currentWeatherIcon);
        mCurrentWeather = findViewById(R.id.currentWeather);
        mHighTemp = findViewById(R.id.highTempCurrent);
        mLowTemp = findViewById(R.id.lowTempCurrent);
        mForecasts[0] = findViewById(R.id.forecast0);
        mForecasts[1] = findViewById(R.id.forecast1);
        mForecasts[2] = findViewById(R.id.forecast2);
        mForecasts[3] = findViewById(R.id.forecast3);
        mForecasts[4] = findViewById(R.id.forecast4);
    }

    public TextView getMainTitle() {
        return mMainTitle;
    }

    public TextView getSubTitle() {
        return mSubTitle;
    }

    public ImageView getCurrentWeatherIcon() {
        return mCurrentWeatherIcon;
    }

    public TextView getCurrentWeather() {
        return mCurrentWeather;
    }

    public TextView getHighTemp() {
        return mHighTemp;
    }

    public TextView getLowTemp() {
        return mLowTemp;
    }

    public View getForecasts(int i) {
        return mForecasts[i];
    }
}
