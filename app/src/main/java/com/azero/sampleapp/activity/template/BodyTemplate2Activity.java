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
import android.support.constraint.ConstraintLayout;
import android.widget.ImageView;
import android.widget.TextView;


import com.azero.sampleapp.R;
import com.azero.sdk.util.Constant;
import com.azero.sdk.util.log;

import org.json.JSONException;
import org.json.JSONObject;

public class BodyTemplate2Activity extends BaseDisplayCardActivity{
    private TextView mMainTitle;
    private TextView mSubTitle;
    private TextView mTextField;
    private ImageView mImage;
    private ConstraintLayout background;


    @Override
    protected int getLayoutResId() {
        return R.layout.card_body_template2;
    }

    @Override
    protected void initView() {
        mMainTitle = findViewById(R.id.mainTitle);
        mSubTitle = findViewById(R.id.subTitle);
        mTextField = findViewById(R.id.textField);
        mImage = findViewById( R.id.image );
        background = findViewById(R.id.BodyTemplate2);
    }

    @Override
    protected void initData(Intent intent) {
        try {
            JSONObject template = new JSONObject(intent.getStringExtra(Constant.EXTRA_TEMPLATE));
            ConfigureTemplateView.configureBodyTemplate2(this, template);
        } catch (JSONException e) {
            log.e(e.getMessage());
            finish();
        }
    }

    public TextView getMainTitle() {
        return mMainTitle;
    }

    public TextView getSubTitle() {
        return mSubTitle;
    }

    public TextView getTextField() {
        return mTextField;
    }

    public ImageView getImage() {
        return mImage;
    }

    public ConstraintLayout getBackground() {
        return background;
    }
}
