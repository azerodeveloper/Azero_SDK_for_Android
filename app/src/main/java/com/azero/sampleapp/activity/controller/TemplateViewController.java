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

package com.azero.sampleapp.activity.controller;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.azero.sampleapp.activity.playerinfo.playerpager.PlayerInfoViewPagerActivity;
import com.azero.sampleapp.activity.weather.WeatherActivity;
import com.azero.sampleapp.manager.ActivityLifecycleManager;
import com.azero.sampleapp.activity.alert.AlertRingtoneActivity;
import com.azero.sampleapp.activity.alert.AlertsActivity;
import com.azero.sampleapp.activity.playerinfo.news.NewsActivity;
import com.azero.sampleapp.activity.template.BodyTemplate1Activity;
import com.azero.sampleapp.activity.template.BodyTemplate2Activity;
import com.azero.sampleapp.activity.template.ListTemplate1Activity;
import com.azero.sampleapp.activity.template.PhotoprathyActivity;
import com.azero.sdk.util.Constant;
import com.azero.sdk.util.log;

import org.json.JSONObject;

/**
 * Template类型界面控制
 */
public class TemplateViewController {
    public static final int BODY_TEMPLATE1 = 1, BODY_TEMPLATE2 = 2, LIST_TEMPLATE1 = 3,
            WEATHER_TEMPLATE = 4, SET_DESTINATION_TEMPLATE = 5, LOCAL_SEARCH_LIST_TEMPLATE1 = 6,
            RENDER_PLAYER_INFO = 7, SHOW_ALERTS_INFO = 8, SHOW_NEWS_INFO = 9, SHOW_ALERT_RINGING_INFO = 10,BODY_TEMPLATE3 = 11;
    private Context context;

    public TemplateViewController(Context context) {
        this.context = context;
    }

    public void showDisplayCard(@NonNull JSONObject template, int type) {
        showDisplayCard(context, template, type);
    }

    private void showDisplayCard(Context context, @NonNull JSONObject template, int type) {
        Intent intent = new Intent();
        intent.putExtra(Constant.EXTRA_TEMPLATE, template.toString());
        switch (type) {
            case BODY_TEMPLATE1:
                intent.setClass(context, BodyTemplate1Activity.class);
                break;
            case BODY_TEMPLATE2:
                intent.setClass(context, BodyTemplate2Activity.class);
                break;
            case BODY_TEMPLATE3:
                intent.setClass(context, PhotoprathyActivity.class);
                break;
            case LIST_TEMPLATE1:
                intent.setClass(context, ListTemplate1Activity.class);
                break;
            case WEATHER_TEMPLATE:
                intent.setClass(context, WeatherActivity.class);
                break;
            case SET_DESTINATION_TEMPLATE:
            case LOCAL_SEARCH_LIST_TEMPLATE1:
                return;
            case RENDER_PLAYER_INFO:
                intent.setClass(context, PlayerInfoViewPagerActivity.class);
                break;
            case SHOW_ALERTS_INFO:
                intent.setClass(context, AlertsActivity.class);
                break;
            case SHOW_NEWS_INFO:
                intent.setClass(context, NewsActivity.class);
                break;
            case SHOW_ALERT_RINGING_INFO:
                try {
                    intent.setClass(context, AlertRingtoneActivity.class);
                } catch (Exception e) {
                    log.e(e.getMessage());
                }
                break;
            default:
                break;
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void clearTemplate() {
        ActivityLifecycleManager.getInstance().clearChannel(ActivityLifecycleManager.ChannelName.TEMPLATE);
    }

    public void clearPlayerInfo() {
        ActivityLifecycleManager.getInstance().clearChannel(ActivityLifecycleManager.ChannelName.PLAYER_INFO);
    }
}
