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
import android.os.Bundle;

import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.azero.sampleapp.R;
import com.azero.sampleapp.widget.SlidingView;
import com.azero.sdk.util.log;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class QrCodeActivity extends AppCompatActivity {

    private TextView qrCodeTitle;
    private TextView qrCodeText1;
    private TextView qrCodeText2;
    private TextView qrCodeText3;
    private TextView qrCodeText4;

    private ImageView qrCodeImage;

    private JSONObject template;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(getLayoutResId());
        initView();
        initData(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        initData(intent);
    }

    int getLayoutResId() {
        return R.layout.card_andlink_qr_code;
    }

    void initData(Intent intent) {
        SlidingView rootView = new SlidingView(this);
        try {
            template = new JSONObject(intent.getStringExtra("EXTRA_TEMPLATE"));
            log.d("initData: " + template.toString());
            ConfigureTemplateView.configureAndLink(this, template);

            if (template.has("type")) {
                String type = template.getString("type");
                switch (type) {
                    case "andLink_qrCode":
                        rootView.onBindActivity(this);
                        break;
                    default:
                        break;
                }
            }

        } catch (JSONException e) {
            log.e(e.getMessage());
            finish();
        }
    }

    void initView() {
        qrCodeImage = findViewById(R.id.qrCodeImage);

        qrCodeTitle = findViewById(R.id.qrCodeTitle);
        qrCodeText1 = findViewById(R.id.qrCodeText1);
        qrCodeText2 = findViewById(R.id.qrCodeText2);
        qrCodeText3 = findViewById(R.id.qrCodeText3);
        qrCodeText4 = findViewById(R.id.qrCodeText4);
    }

    public ImageView getQrCodeImage() {
        return qrCodeImage;
    }

    public TextView getQrCodeTitle() {
        return qrCodeTitle;
    }

    public TextView getQrCodeText1() {
        return qrCodeText1;
    }

    public TextView getQrCodeText2() {
        return qrCodeText2;
    }

    public TextView getQrCodeText3() {
        return qrCodeText3;
    }

    public TextView getQrCodeText4() {
        return qrCodeText4;
    }

}
