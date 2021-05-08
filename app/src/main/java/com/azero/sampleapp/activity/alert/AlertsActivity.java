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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.azero.sampleapp.R;
import com.azero.sampleapp.activity.template.BaseDisplayCardActivity;
import com.azero.sampleapp.activity.alert.bean.AlertInfo;
import com.azero.sampleapp.activity.template.ConfigureTemplateView;
import com.azero.sdk.AzeroManager;
import com.azero.sdk.impl.Alerts.AlertsHandler;
import com.azero.sdk.util.Constant;
import com.azero.sdk.util.log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class AlertsActivity extends BaseDisplayCardActivity implements View.OnClickListener {
    private ImageButton imageButtonBack;
    private AlertsAdapter mAlertsAdapter;
    private AlertsRecyclerView mAlertsRecyclerView;
    private AlertsHandler mAlerts;
    private TextView mTextViewTitle;

    @Override
    protected int getLayoutResId() {
        return R.layout.card_alert_template;
    }

    @Override
    protected void initView() {
        mTextViewTitle = findViewById(R.id.textView_title);
        mAlertsRecyclerView = findViewById(R.id.recycleView_alerts);
        mAlertsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        DividerItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.alerts_inset));
        mAlertsRecyclerView.addItemDecoration(itemDecoration);
        mAlerts = (AlertsHandler) AzeroManager.getInstance().getHandler(AzeroManager.ALERT_HANDLER);

        imageButtonBack = findViewById(R.id.imageButton_back);
        imageButtonBack.setOnClickListener(this);
    }

    @Override
    protected void initData(Intent intent) {
        try {
            JSONObject template = new JSONObject(intent.getStringExtra(Constant.EXTRA_TEMPLATE));
            ConfigureTemplateView.configureAlertsListTemplate(this, template);
        } catch (JSONException e) {
            log.e(e.getMessage());
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageButton_back:
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    public void setAlertsAdapter(List<AlertInfo> list) {
        mAlertsAdapter = new AlertsAdapter(this, list);
    }

    public TextView getTextViewTitle() {
        return mTextViewTitle;
    }

    public AlertsHandler getAlerts() {
        return mAlerts;
    }

    public AlertsRecyclerView getAlertsRecyclerView() {
        return mAlertsRecyclerView;
    }

    public AlertsAdapter getAlertsAdapter() {
        return mAlertsAdapter;
    }

}
