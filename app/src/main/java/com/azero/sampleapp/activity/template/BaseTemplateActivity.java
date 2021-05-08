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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.view.WindowManager;

import com.azero.platforms.core.PlatformInterface;
import com.azero.sdk.AzeroManager;
import com.azero.sdk.impl.TemplateRuntime.TemplateRuntimeHandler;

public abstract class BaseTemplateActivity extends AppCompatActivity {
    private boolean foreground = false;
    protected TemplateRuntimeHandler templateRuntimeHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(getLayoutResId());
        initView();
        initData(getIntent());

        PlatformInterface templateInterface = AzeroManager.getInstance()
                .getHandler(AzeroManager.TEMPLATE_HANDLER);
        if (templateInterface != null)
            templateRuntimeHandler = (TemplateRuntimeHandler) templateInterface;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        initData(intent);
    }

    protected abstract int getLayoutResId();

    protected abstract void initView();

    protected abstract void initData(Intent intent);

    @Override
    protected void onResume() {
        super.onResume();
        foreground = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        foreground = false;
    }

    public boolean isForeground() {
        return foreground;
    }
}
