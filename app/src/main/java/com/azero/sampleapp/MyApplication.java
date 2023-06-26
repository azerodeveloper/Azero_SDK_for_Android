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

package com.azero.sampleapp;


import com.azero.sampleapp.manager.ActivityLifecycleManager;
import com.azero.sdk.BaseApplication;
import com.azero.sdk.config.Config;
import com.azero.sdk.config.ConfigSetting;
import com.azero.sdk.impl.AudioInput.record.SystemRecord;
import com.azero.sdk.manager.AzeroManager;

public class MyApplication extends BaseApplication{
    private static MyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        registerActivityLifecycleCallbacks(ActivityLifecycleManager.getInstance());
    }

    public static MyApplication getInstance() {
        return instance;
    }


    public void initAzero() throws RuntimeException {

        ConfigSetting config = new ConfigSetting.ConfigBuilder()
                .setProductID("speaker_azero_test")
                .setClientID("5da580abda66010006f7e6c4")
                .setServerType(Config.SERVER.PRO)
                .create();

        AzeroManager.getInstance().startEngine(new SystemRecord(),null,config);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        AzeroManager.getInstance().release();
    }

    public void exit() {
        try {
            ActivityLifecycleManager.getInstance().finishAllActivity();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
