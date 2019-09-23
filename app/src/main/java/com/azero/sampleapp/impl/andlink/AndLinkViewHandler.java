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

package com.azero.sampleapp.impl.andlink;

import com.azero.sampleapp.activity.controller.LocalViewController;
import com.azero.sampleapp.impl.andcallcontroller.AndCallViewControllerHandler;
import com.azero.sdk.AndLinkConfig;
import com.azero.sdk.Config;
import com.azero.sdk.impl.AndLink.AndLinkManager;
import com.azero.sdk.impl.AndLink.AndLinkManagerCallback;
import com.azero.sdk.util.log;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @Author: sjy
 * @Date: 2019/8/14
 */
public class AndLinkViewHandler implements AndLinkManagerCallback {

    private Gson gson;
    private AndLinkManager mAndLinkManager;
    private LocalViewController mLocalViewController;

    public AndLinkViewHandler() {
        gson = new Gson();
    }

    public void setLocalViewController(LocalViewController localViewController) {
        mLocalViewController = localViewController;
    }

    public void init(AndLinkManager andLinkManager, Config config, AndLinkConfig andLinkConfig) {
        mAndLinkManager = andLinkManager;
        log.d("init setCallback");
        mAndLinkManager.initData(config, andLinkConfig);
        mAndLinkManager.setAndLinkHandlerCallback(this);
    }

    @Override
    public void andLinkLoginSuccess() {
        log.d("andLinkLoginSuccess");
        showDisplayCard("finish", "");
        AndCallViewControllerHandler.getInstance().onLoginIms();
    }

    @Override
    public void andLinkOnConnecting() {
        log.d("andLinkOnConnecting");
        showDisplayCard("wait", "");
    }

    @Override
    public void andLinkGetQrCodeUrl(String url) {
        log.d("andLinkGetQrCodeUrl");
        showDisplayCard("andLink_qrCode", url);
    }

    @Override
    public void andLinkUnbind() {
        log.d("解除绑定");
        mAndLinkManager.requestAndLinkAddress();
        AndCallViewControllerHandler.getInstance().onLogoutIms();
    }

    private void showDisplayCard(String type, String message) {
        QrCodeBean qrCodeBean = new QrCodeBean();
        qrCodeBean.setMessage(message);
        qrCodeBean.setType(type);
        String str = gson.toJson(qrCodeBean);
        try {
            JSONObject jsonObject = new JSONObject(str);
            mLocalViewController.showDisplayCard(jsonObject, LocalViewController.QR_CORD_ACTIVITY);
        } catch (JSONException e) {
            log.e(e.toString());
        }
    }
}
