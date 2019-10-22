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

package com.azero.sampleapp.manager;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.azero.sampleapp.activity.launcher.LauncherActivity;
import com.azero.sampleapp.activity.playerinfo.BasePlayerInfoActivity;
import com.azero.sampleapp.activity.template.BaseDisplayCardActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * 负责整个程序的生命周期管理
 */
public class ActivityLifecycleManager implements Application.ActivityLifecycleCallbacks {
    private static Stack<Activity> activityStack = new Stack<>();
    private static List<Activity> templateList = new ArrayList<>();
    private static List<Activity> playerInfoList = new ArrayList<>();
    private int mActivityCount = 0;

    private static class Holder {
        private static ActivityLifecycleManager INSTANCE = new ActivityLifecycleManager();
    }

    public static ActivityLifecycleManager getInstance() {
        return Holder.INSTANCE;
    }


    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        activityStack.add(activity);
        if (activity instanceof BaseDisplayCardActivity) {
            templateList.add(activity);
        } else if (activity instanceof BasePlayerInfoActivity) {
            clearChannel(ChannelName.PLAYER_INFO);
            playerInfoList.add(activity);
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
        mActivityCount++;
    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        mActivityCount--;
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        activityStack.remove(activity);
        if (activity instanceof BaseDisplayCardActivity) {
            templateList.remove(activity);
        } else if (activity instanceof BasePlayerInfoActivity) {
            playerInfoList.remove(activity);
        }
    }

    public Activity getCurActivity() {
        return activityStack.lastElement();
    }

    /**
     * 关闭指定类型的界面
     *
     * @param channelName
     */
    public void clearChannel(ChannelName channelName) {
        switch (channelName) {
            case TEMPLATE:
                for (Activity activity : templateList) {
                    activity.finish();
                    activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
                templateList.clear();
                break;
            case PLAYER_INFO:
                for (Activity activity : playerInfoList) {
                    activity.finish();
                    activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
                playerInfoList.clear();
                break;
            default:
                break;
        }
    }

    /**
     * 获取栈顶Activity
     *
     * @return
     */
    public Activity getTopActivity() {
        if (activityStack.size() > 0) {
            return activityStack.get(activityStack.size() - 1);
        }
        return null;
    }

    /**
     * 判断当前是否在首页
     *
     * @return
     */
    public boolean topIsLauncher() {
        if (activityStack.size() == 1) {
            Activity activity = activityStack.get(0);
            if (activity instanceof LauncherActivity) {
                return ((LauncherActivity) activity).isForeground();
            }
        }
        return false;
    }

    public enum ChannelName {
        TEMPLATE,
        PLAYER_INFO
    }

    /**
     * 应用是否在前台
     *
     * @return
     */
    public boolean isAppForeground() {
        return mActivityCount > 0;
    }
}
