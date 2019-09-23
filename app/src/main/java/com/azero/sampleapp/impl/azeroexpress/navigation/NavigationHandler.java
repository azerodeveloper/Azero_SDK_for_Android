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

package com.azero.sampleapp.impl.azeroexpress.navigation;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import com.azero.platforms.iface.LocalMediaSource;
import com.azero.sampleapp.manager.ActivityLifecycleManager;
import com.azero.sampleapp.activity.playerinfo.BasePlayerInfoActivity;
import com.azero.sampleapp.activity.launcher.LauncherActivity;
import com.azero.sampleapp.activity.template.BaseTemplateActivity;
import com.azero.sampleapp.impl.azeroexpress.AzeroExpressInterface;
import com.azero.sampleapp.util.Utils;
import com.azero.sdk.AzeroManager;
import com.azero.sdk.event.Command;
import com.azero.sdk.util.log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 导航模块
 * 执行语音指令"退出"、"返回"、"返回首页"的动作
 */
public class NavigationHandler implements AzeroExpressInterface {

    private Context applicationContext;

    public NavigationHandler(Context context) {
        applicationContext = context.getApplicationContext();
    }

    @Override
    public void handleDirective(JSONObject expressDirective) {
        try {
            String action = expressDirective.getString("action");
            switch (action) {
                //退出
                case "Exit":
                    handleExit();
                    break;
                //返回首页
                case "goHome":
                    handleGoHome();
                    break;
                //返回
                case "goBack":
                    handleGoBack();
                    break;
                default:
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleGoBack() {
        log.d("handleGoBack");
        if (Utils.isApplicationForeground()) {
            Instrumentation inst = new Instrumentation();
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
        } else {
            exitExternalMediaSource(LocalMediaSource.PlayControlType.BACK);
        }
    }

    private void handleExit() {
        log.d("handleExit");
        Activity activity = ActivityLifecycleManager.getInstance().getTopActivity();
        //在播放页面或者首页说退出时，同时暂停播放器
        if (activity instanceof BasePlayerInfoActivity) {
            if (((BaseTemplateActivity) activity).isForeground()) {
                AzeroManager.getInstance().executeCommand(Command.CMD_PLAY_PAUSE);
            }
        } else if (activity instanceof LauncherActivity) {
            if (((LauncherActivity) activity).isForeground()) {
                AzeroManager.getInstance().executeCommand(Command.CMD_PLAY_PAUSE);
            }
        }
        //执行退出命令
        if (Utils.isApplicationForeground()) {
            //模拟点击返回按钮
            Instrumentation inst = new Instrumentation();
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
        } else {
            //调用第三方退出接口
            exitExternalMediaSource(LocalMediaSource.PlayControlType.EXIT);
        }
    }

    private void exitExternalMediaSource(LocalMediaSource.PlayControlType controlType) {
        LocalMediaSource musicMediaSource = AzeroManager.getInstance().getMusicMediaSource();
        if (musicMediaSource != null) {
            musicMediaSource.playControl(controlType);
        }
        exitExternalMediaVideoSource(controlType);
    }

    /**
     * 退出视频
     * 返回首页的时候直接退出
     *
     * @param controlType
     */
    private void exitExternalMediaVideoSource(LocalMediaSource.PlayControlType controlType) {
        LocalMediaSource videoMediaSource = AzeroManager.getInstance().getVideoMediaSource();
        if (videoMediaSource != null) {
            videoMediaSource.playControl(controlType);
        }
    }

    private void handleGoHome() {
        log.d("handleGoHome");
        Intent intent = new Intent(applicationContext, LauncherActivity.class);
        applicationContext.startActivity(intent);
        if (!Utils.isApplicationForeground()) {
            log.d("handleGoHome exit");
            exitExternalMediaVideoSource(LocalMediaSource.PlayControlType.EXIT);
        }
    }
}
