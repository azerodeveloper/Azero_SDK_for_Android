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

import org.json.JSONObject;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.azero.sampleapp.activity.template.QrCodeActivity;
import com.azero.sampleapp.activity.thirdparty.andcall.AndCallViewControllerActivity;
import com.azero.sdk.util.Constant;

public class LocalViewController {
    public static final int QR_CORD_ACTIVITY = 0, AND_CALL_ACTIVITY = 1;

    private Context mContext;

    public LocalViewController(Context context) {
        mContext = context;
    }

    public void showDisplayCard(@NonNull JSONObject json, int type) {
        showDisplayCard(mContext, json, type);
    }

    private void showDisplayCard(Context context, @NonNull JSONObject template, int type) {
        Intent intent = new Intent();
        intent.putExtra(Constant.EXTRA_TEMPLATE, template.toString());
        switch (type) {
            case QR_CORD_ACTIVITY:
                intent.setClass(context, QrCodeActivity.class);
                break;
            case AND_CALL_ACTIVITY:
                intent.setClass(context, AndCallViewControllerActivity.class);
                break;
            default:
                break;
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
