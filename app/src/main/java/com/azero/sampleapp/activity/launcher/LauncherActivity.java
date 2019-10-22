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

package com.azero.sampleapp.activity.launcher;

import android.Manifest;
import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextClock;
import android.widget.Toast;

import com.azero.sampleapp.R;
import com.azero.sampleapp.Setting;
import com.azero.sampleapp.activity.controller.LocalViewController;
import com.azero.sampleapp.activity.controller.TemplateViewController;
import com.azero.sampleapp.activity.launcher.viewmodel.LauncherViewModel;
import com.azero.sampleapp.impl.andcallcontroller.AndCallViewControllerHandler;
import com.azero.sampleapp.util.Utils;
import com.azero.sdk.AzeroEvent;
import com.azero.sdk.AzeroManager;
import com.azero.sdk.impl.MediaPlayer.MediaPlayerHandler;
import com.azero.sdk.impl.TemplateRuntime.TemplateDispatcher;
import com.azero.sdk.impl.TemplateRuntime.TemplateRuntimeHandler;
import com.azero.sdk.util.log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 首页
 */
public class LauncherActivity extends AppCompatActivity implements AzeroManager.AzeroOSListener {
    private ViewPager viewPager;
    private LauncherPagerAdapter launcherPagerAdapter;
    private LauncherViewModel launcherViewModel;
    private MediaPlayerHandler mMediaPlayer;
    private static String mCurrentAudioItemId;
    private boolean isForeground = false;

    /* Shared Preferences */
    private boolean mIsTalkButtonLongPressed = false;

    private static final String sDeviceConfigFile = "app_config.json";
    private static final int sPermissionRequestCode = 0;
    private static final String[] sRequiredPermissions = {Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if permissions are missing and must be requested
        ArrayList<String> requests = new ArrayList<>();

        for (String permission : sRequiredPermissions) {
            if (ActivityCompat.checkSelfPermission(this, permission)
                    == PackageManager.PERMISSION_DENIED) {
                requests.add(permission);
            }
        }

        // Request necessary permissions if not already granted, else start app
        if (requests.size() > 0) {
            ActivityCompat.requestPermissions(this,
                    requests.toArray(new String[requests.size()]), sPermissionRequestCode);
        } else {
            create();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void create() {
        log.d("imei: " + Utils.getimei(this));
        setContentView(R.layout.activity_launcher);
        initViewModel();
        TextClock textClock = findViewById(R.id.clock);
        viewPager = findViewById(R.id.container);
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/clock.ttf");
        textClock.setTypeface(typeFace);
        LocalViewController localViewController = new LocalViewController(this);
        AndCallViewControllerHandler mAndCallViewControllerHandler = AndCallViewControllerHandler.getInstance();

        renderView();

        if (Setting.enableHjghAndAndLink) {
            initPhoneCallButtonView();
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 101);
        }

        AzeroManager.getInstance().addAzeroOSListener(this);
        if (AzeroManager.getInstance().isEngineInitComplete()) {
            initAzero();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == sPermissionRequestCode) {
            if (grantResults.length > 0) {
                for (int grantResult : grantResults) {
                    if (grantResult == PackageManager.PERMISSION_DENIED) {
                        // Permission request was denied
                        Toast.makeText(this, "Permissions required",
                                Toast.LENGTH_LONG).show();
                    }
                }
                // Permissions have been granted. Start app
                create();
            } else {
                // Permission request was denied
                Toast.makeText(this, "Permissions required", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void initViewModel() {
        launcherViewModel = ViewModelProviders.of(this).get(LauncherViewModel.class);
        launcherViewModel.getRecommendationList().observe(this, recommendations -> {
            if (recommendations != null && recommendations.size() > 0) {
                log.d("recommendation size" + recommendations.size());
                launcherPagerAdapter.initFragments(recommendations);
            }
        });
    }

    private void renderView() {
        viewPager.setPageTransformer(true, new LauncherPageTransformer());
        launcherPagerAdapter = new LauncherPagerAdapter(viewPager, getSupportFragmentManager());
        launcherPagerAdapter.setLoopInterval(10000);
        viewPager.setAdapter(launcherPagerAdapter);
        launcherPagerAdapter.startLoop();
    }

    @Override
    protected void onResume() {
        super.onResume();
		if(launcherPagerAdapter != null){
			launcherPagerAdapter.startLoop();
		}
        isForeground = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(launcherPagerAdapter != null){
            launcherPagerAdapter.stopLoop();
        }
        isForeground = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (launcherPagerAdapter != null) {
            launcherPagerAdapter.onDestroy();
        }
    }

    @Override
    public void onEvent(AzeroEvent azeroEvent, String s) {
        log.d("AzeroEvent" + azeroEvent);
        switch (azeroEvent) {
            case EVENT_ENGINE_INITIALIZATION_COMPLETE:
                initAzero();
                break;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initPhoneCallButtonView() {
        ImageButton phone = findViewById(R.id.phone);

        phone.setImageResource(R.drawable.soundai_phone_h);
        phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AndCallViewControllerHandler.getInstance().onLoginIms();
            }
        });

        phone.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    phone.setImageResource(R.drawable.soundai_phone_n);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    phone.setImageResource(R.drawable.soundai_phone_h);
                }
                return false;
            }
        });

    }

    private void initAzero() {
        log.d("initAzero");
        TemplateViewController mTemplateViewController = new TemplateViewController(this.getApplicationContext());
        mMediaPlayer = (MediaPlayerHandler) AzeroManager.getInstance().getHandler(AzeroManager.AUDIO_HANDLER);
        TemplateRuntimeHandler templateRuntimeHandler = (TemplateRuntimeHandler) AzeroManager.getInstance().getHandler(AzeroManager.TEMPLATE_HANDLER);
        templateRuntimeHandler.registerTemplateDispatchedListener(new TemplateDispatcher() {
            @Override
            public void renderTemplate(String payload, String type) {
                try {
                    // Log payload
                    log.d("payload: " + payload);
                    JSONObject template = new JSONObject(payload);
                    switch (type) {
                        case "BodyTemplate1":
                            mTemplateViewController.showDisplayCard(template, TemplateViewController.BODY_TEMPLATE1);
                            break;
                        case "BodyTemplate2":
                            mTemplateViewController.showDisplayCard(template, TemplateViewController.BODY_TEMPLATE2);
                            break;
                        case "DefaultTemplate3":
                            mTemplateViewController.showDisplayCard(template, TemplateViewController.BODY_TEMPLATE3);
                            break;
                        case "ListTemplate1":
                            mTemplateViewController.showDisplayCard(template, TemplateViewController.LIST_TEMPLATE1);
                            break;
                        case "WeatherTemplate":
                            mTemplateViewController.showDisplayCard(template, TemplateViewController.WEATHER_TEMPLATE);
                            break;
                        case "LocalSearchListTemplate1":
                            break;
                        case "DefaultTemplate1":
                            mTemplateViewController.showDisplayCard(template, TemplateViewController.BODY_TEMPLATE1);
                            break;
                        case "DefaultTemplate2":
                            mTemplateViewController.showDisplayCard(template, TemplateViewController.BODY_TEMPLATE2);
                            break;
                        case "AlertsListTemplate":
                            mTemplateViewController.showDisplayCard(template, TemplateViewController.SHOW_ALERTS_INFO);
                            break;
                        case "AlertRingtoneTemplate":
                            mTemplateViewController.showDisplayCard(template, TemplateViewController.SHOW_ALERT_RINGING_INFO);
                            break;
                        default:
                            break;
                    }
                } catch (JSONException e) {
                    log.e(e.getMessage());
                }
            }

            @Override
            public void renderPlayerInfo(String payload) {
                try {
                    JSONObject playerInfo = new JSONObject(payload);
                    String audioItemId = playerInfo.getString("audioItemId");
                    JSONObject content = playerInfo.getJSONObject("content");
                    String type = playerInfo.getString("type");

                    if (!audioItemId.equals(mCurrentAudioItemId)) {
                        // Log only if audio item has changed
                        mCurrentAudioItemId = audioItemId;
                        if (type.equals("NewsTemplate")) {
                            mTemplateViewController.showDisplayCard(playerInfo, TemplateViewController.SHOW_NEWS_INFO);
                        } else {
                            mTemplateViewController.showDisplayCard(content, TemplateViewController.RENDER_PLAYER_INFO);
                        }
                    }
                } catch (JSONException e) {
                    log.e(e.getMessage());
                }
            }

            @Override
            public void clearTemplate() {
                log.d("clearTemplate");
                mTemplateViewController.clearTemplate();
            }

            @Override
            public void clearPlayerInfo() {
                mTemplateViewController.clearPlayerInfo();
            }
        });
    }

    @Override
    public void onBackPressed() {
        //屏蔽返回事件
    }

    public boolean isForeground() {
        return isForeground;
    }
}
