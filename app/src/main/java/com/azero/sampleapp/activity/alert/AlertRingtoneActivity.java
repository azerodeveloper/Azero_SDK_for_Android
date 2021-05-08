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

package com.azero.sampleapp.activity.alert;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.azero.sampleapp.R;
import com.azero.sampleapp.activity.template.BaseDisplayCardActivity;
import com.azero.sampleapp.activity.template.ConfigureTemplateView;
import com.azero.sampleapp.widget.SlidingView;
import com.azero.sdk.AzeroManager;
import com.azero.sdk.impl.Alerts.AlertsHandler;
import com.azero.sdk.util.Constant;
import com.azero.sdk.util.log;

import org.json.JSONException;
import org.json.JSONObject;

public class AlertRingtoneActivity extends BaseDisplayCardActivity {

    private TextView mAlertTimeTextView;
    private TextView mAlertEventTextView;

    private AlertsHandler alerts;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);

        SlidingView rootView = new SlidingView(this);
        rootView.onBindActivity(this);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.card_alert_ringtone_template;
    }

    @Override
    protected void initView() {
        mAlertTimeTextView = findViewById(R.id.alert_time);
        mAlertEventTextView = findViewById(R.id.alert_event);
        ConstraintLayout alertLayout = findViewById(R.id.alert_ringtone_layout);
        ImageView alertClockIcon = findViewById(R.id.alert_clock_icon);
        ImageView alertImage = findViewById(R.id.alert_image);
        TextView alertText = findViewById(R.id.alert_text_1);

        alertText.setText(R.string.alert_text_1);
        alertLayout.setBackgroundResource(R.drawable.soundai_alert_ringtone_background);
        alertClockIcon.setImageResource(R.drawable.soundai_alert_ringtone_clock_icon);
        alertImage.setImageResource(R.drawable.soundai_alert_ringtone_icon_right_n);

        alerts = (AlertsHandler) AzeroManager.getInstance().getHandler(AzeroManager.ALERT_HANDLER);
    }

    @Override
    protected void initData(Intent intent) {
        String payload = intent.getStringExtra(Constant.EXTRA_TEMPLATE);
        log.d("payload: " + payload);
        try {
            JSONObject template = new JSONObject(payload);
            ConfigureTemplateView.configureAlertRingtoneTemplate(this, template);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        alerts.localStop();
    }

    public TextView getAlertTimeTextView() {
        return mAlertTimeTextView;
    }

    public TextView getAlertEventTextView() {
        return mAlertEventTextView;
    }
}
