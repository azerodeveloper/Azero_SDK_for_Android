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

package com.azero.sampleapp.activity.weather;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.azero.sampleapp.R;
import com.azero.sampleapp.activity.template.BaseDisplayCardActivity;
import com.azero.sampleapp.activity.weather.adapter.MainAdapter;
import com.azero.sampleapp.activity.weather.data.Weather;
import com.azero.sampleapp.activity.weather.data.WeatherAir;
import com.azero.sampleapp.activity.weather.fragment.WeatherAirInfoFragment;
import com.azero.sampleapp.activity.weather.fragment.WeatherForecastFragment;
import com.azero.sampleapp.activity.weather.fragment.WeatherForecastInfoFragment;
import com.azero.sampleapp.activity.weather.fragment.WeatherFragmentInterface;
import com.azero.sampleapp.activity.weather.fragment.WeatherNotSupportFragment;
import com.azero.sampleapp.activity.weather.fragment.WeatherNowInfoFragment;
import com.azero.sampleapp.activity.weather.fragment.WeatherSuggestFragment;
import com.azero.sampleapp.activity.weather.widget.ViewPagerScroller;
import com.azero.sampleapp.util.DateUtils;
import com.azero.sdk.AzeroManager;
import com.azero.sdk.impl.TemplateRuntime.TemplateRuntimeHandler;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;


public class WeatherActivity extends BaseDisplayCardActivity implements WeatherFragmentInterface {
    public static final int MESSAGE_SCROLL = 100;
    public static final int MESSAGE_DELAY_RECLOCK_TEMPLATE_TIMER = 101;
    public static final int TIME_OUT_TIMES = 5000;
    public static final String EXTRA_TEMPLATE = "EXTRA_TEMPLATE";
    private Weather weatherInfo;
    private WeatherNowInfoFragment weatherInfoFragment;
    private WeatherForecastInfoFragment weatherForecastInfoFragment;
    private WeatherAirInfoFragment weatherAirInfoFragment;
    private WeatherSuggestFragment weatherSuggestFragment;
    private WeatherForecastFragment weatherForecastFragment;
    private WeatherNotSupportFragment weatherNotSupportFragment;
    private MainAdapter mainAdapter;
    private TextView titleDayView;
    private TextView titleWeekDayView;
    private TextView titleLocationView;
    private ImageView back;
    private ImageView backgroundView;
    private ViewPager viewPager;
    private List<Fragment> mList = new ArrayList<>();
    private int seleted = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        updateIntent(intent);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.weather_activity_main;
    }

    public void initFragment() {
        weatherInfoFragment = new WeatherNowInfoFragment();
        weatherForecastInfoFragment = new WeatherForecastInfoFragment();
        weatherAirInfoFragment = new WeatherAirInfoFragment();
        weatherSuggestFragment = new WeatherSuggestFragment();
        weatherForecastFragment = new WeatherForecastFragment();
        weatherNotSupportFragment = new WeatherNotSupportFragment();
        mainAdapter = new MainAdapter(getSupportFragmentManager(), mList);
    }

    public void initView() {
        initFragment();
        titleDayView = findViewById(R.id.main_title);
        titleWeekDayView = findViewById(R.id.week_day);
        titleLocationView = findViewById(R.id.city);
        backgroundView = findViewById(R.id.bg);
        back = findViewById(R.id.back_icon);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        viewPager = findViewById(R.id.view_pager);
        ViewPagerScroller scroller = new ViewPagerScroller(this);
        scroller.setScrollDuration(800);//时间越长，速度越慢。
        scroller.initViewPagerScroll(viewPager);
        viewPager.setAdapter(mainAdapter);
        viewPager.setPageTransformer(true, pageTransformer);
        seleted = 0;
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position != seleted) {
                    seleted = position;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    protected void initData(Intent intent) {
        updateIntent(intent);

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mHandler.removeMessages(MESSAGE_SCROLL);
        return super.dispatchTouchEvent(ev);
    }

    private void updateIntent(Intent intent) {
        String value = intent.getStringExtra(EXTRA_TEMPLATE);
        weatherInfo = new Gson().fromJson(value, Weather.class);
        intent.getParcelableExtra("WEATHER_INFO");
        updateView();
        updateFragmentList();
        mHandler.removeMessages(MESSAGE_SCROLL);
        mHandler.sendEmptyMessageDelayed(MESSAGE_SCROLL, TIME_OUT_TIMES);
        viewPager.setCurrentItem(0);
    }

    private void updateFragmentList() {
        if (weatherInfo == null) return;
        mList.clear();
        if (weatherInfo.getNotSupport() != null) {
            mList.add(weatherNotSupportFragment);
            mainAdapter.notifyDataSetChanged();
            return;
        }
        if (weatherInfo.getNow() != null && DateUtils.isToday(weatherInfo.getDate())) {
            mList.add(weatherInfoFragment);
        } else if (weatherInfo.getWeather() != null) {
            mList.add(weatherForecastInfoFragment);
        }
        WeatherAir air = weatherInfo.getAir() != null ? weatherInfo.getAir() : getSpecificDateAir(weatherInfo);
        if (air != null || weatherInfo.getUv() != null) {
            mList.add(weatherAirInfoFragment);
        }
        if (weatherInfo.getSuggestion() != null) {
            mList.add(weatherSuggestFragment);
        }
        if (weatherInfo.getDaily() != null && weatherInfo.getDaily().getDaily() != null && weatherInfo.getDaily().getDaily().size() > 0) {
            mList.add(weatherForecastFragment);
        }
        mainAdapter.notifyDataSetChanged();
    }

    private void updateView() {
        if (weatherInfo != null) {

            titleDayView.setText(weatherInfo.getDate());
            titleWeekDayView.setText(weatherInfo.getWeek());
            titleLocationView.setText(weatherInfo.getCity());
            String url = null;
            if (weatherInfo.getNow() != null) {
                url = weatherInfo.getNow().getBackgroundUrl();
            } else if (weatherInfo.getWeather() != null) {
                url = weatherInfo.getWeather().getBackgroundUrl();
            }
            if (!TextUtils.isEmpty(url)) {
                Glide.with(this).load(url).into(backgroundView);
            } else {
                backgroundView.setImageResource(R.drawable.weather_fine_bg);
            }
        }
    }

    private WeatherAir getSpecificDateAir(Weather weatherInfo) {
        String date = weatherInfo.getDate();
        List<WeatherAir> airList = weatherInfo.getAirs();
        if (CollectionUtils.isEmpty(airList)) {
            return null;
        }
        for (WeatherAir air : airList) {
            if (TextUtils.equals(air.getDate(), date)) {
                return air;
            }
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    private ViewPager.PageTransformer pageTransformer = new ViewPager.PageTransformer() {

        private static final float MIN_ALPHA = 0.3f;    //最小透明度

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();    //得到view宽
            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left. 出了左边屏幕
                view.setAlpha(0);

            } else if (position <= 1) { // [-1,1]
                if (position < 0) {
                    // Fade the page relative to its size.
                    float alphaFactor = Math.max(MIN_ALPHA, 1 - Math.abs(position));
                    //透明度改变Log
                    view.setAlpha(alphaFactor);
                } else {
                    //出现的页面
                    // Fade the page relative to its size.
                    float alphaFactor = Math.max(MIN_ALPHA, 1 - Math.abs(position));
                    //透明度改变Log
                    view.setAlpha(alphaFactor);
                }

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.    出了右边屏幕
                view.setAlpha(0);
            }
        }
    };
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_SCROLL: {
                    mHandler.removeMessages(MESSAGE_SCROLL);
                    int index = seleted + 1;
                    if (index < mList.size()) {
                        //viewPager.set
                        viewPager.setCurrentItem(index, true);

                        sendEmptyMessageDelayed(MESSAGE_SCROLL, TIME_OUT_TIMES);
                    } else {
                        mHandler.removeMessages(MESSAGE_SCROLL);
                        viewPager.setCurrentItem(0, false);

                    }
                    removeMessages(MESSAGE_DELAY_RECLOCK_TEMPLATE_TIMER);
                    TemplateRuntimeHandler templateRuntimeHandler = (TemplateRuntimeHandler) AzeroManager.getInstance().getHandler(AzeroManager.TEMPLATE_HANDLER);
                    templateRuntimeHandler.reclockTemplateTimer();
                }
                break;

                case MESSAGE_DELAY_RECLOCK_TEMPLATE_TIMER: {
                    removeMessages(MESSAGE_DELAY_RECLOCK_TEMPLATE_TIMER);
                    TemplateRuntimeHandler templateRuntimeHandler = (TemplateRuntimeHandler) AzeroManager.getInstance().getHandler(AzeroManager.TEMPLATE_HANDLER);
                    templateRuntimeHandler.reclockTemplateTimer();
                }
                break;
                default:
                    break;
            }
        }
    };

    @Override
    public Weather getWeather() {
        return weatherInfo;
    }
}
