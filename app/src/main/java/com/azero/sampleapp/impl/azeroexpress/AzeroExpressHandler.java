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

package com.azero.sampleapp.impl.azeroexpress;

import android.content.Context;

import com.azero.sampleapp.Setting;
import com.azero.sampleapp.widget.GlobalBottomBar;
import com.azero.platforms.iface.AzeroExpress;
import com.azero.sampleapp.impl.andcallcontroller.AndCallViewControllerHandler;
import com.azero.sdk.impl.AndLink.AndLinkManager;
import com.azero.sampleapp.impl.azeroexpress.navigation.NavigationHandler;
import com.azero.sdk.util.Constant;
import com.azero.sdk.util.executors.AppExecutors;
import com.azero.sdk.util.log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 对一些自定义的Directive接收和处理，可用于开发自定义技能
 * <p>
 * 1.AndLink
 * 2.ASR 结果
 * 3.ReportLog Andlink上报日志
 * 4.Navigation 页面跳转
 */
public class AzeroExpressHandler extends AzeroExpress {
    private AppExecutors mExecutors;
    private Context mContext;
    private AndLinkManager mAndLinkManager;
    private NavigationHandler navigationHandler;

    public AzeroExpressHandler(AppExecutors executors, Context context) {
        mExecutors = executors;
        mContext = context;
    }

    public void setAndLinkHandler(AndLinkManager andLinkManager) {
        mAndLinkManager = andLinkManager;
    }

    @Override
    public void handleExpressDirective(String name, String payload) {
        log.d("payload:" + payload);
        try {
            JSONObject expressDirective = new JSONObject(payload);
            switch (name) {
                case "AndLink":
                    if (Setting.enableHjghAndAndLink) {
                        mAndLinkManager.loginAndLink(expressDirective);
                    }
                    break;
                case "ASRText":
                    if (expressDirective.has("text")) {
                        String text = expressDirective.getString("text");
                        mExecutors.mainThread().execute(() -> GlobalBottomBar.getInstance(mContext).append(text));
                    }
                    break;
                case "ReportLog":
                    if (expressDirective.has("type")) {
                        if (Setting.enableHjghAndAndLink) {
                            if ("andLink".equals(expressDirective.getString("type"))) {
                                AndCallViewControllerHandler.getInstance().onUpLoadLog();
                            }
                        }
                    }
                    break;
                case "Navigation":
                    if (navigationHandler != null) {
                        navigationHandler.handleDirective(expressDirective);
                    }
                    break;
                case "AndCallState":
                    if (Setting.enableHjghAndAndLink) {
                        if (expressDirective.has("andCallStateNum")) {
                            String andCallStateNum = expressDirective.getString("andCallStateNum");
                            switch (andCallStateNum) {
                                case Constant.AndCallStateNum.ANDHOME_ENABLE:
                                    log.d("启用和家固话");
                                    AndCallViewControllerHandler.getInstance().onLoginIms();
                                    break;
                                case Constant.AndCallStateNum.ANDHOME_DISCONNECT:
                                    log.d("停用和家固话");
                                    break;
                                case Constant.AndCallStateNum.ANDHOME_CANCELLATION:
                                    log.d("注销和家固话");
                                    break;
                                case Constant.AndCallStateNum.ANDHOME_SIGN_IN:
                                    log.d("登录和家固话");
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                default:
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public NavigationHandler getNavigationHandler() {
        return navigationHandler;
    }

    public void setNavigationHandler(NavigationHandler navigationHandler) {
        this.navigationHandler = navigationHandler;
    }
}
