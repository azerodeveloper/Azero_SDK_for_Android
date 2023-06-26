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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextClock;

import com.azero.sampleapp.MyApplication;
import com.azero.sampleapp.R;
import com.azero.sampleapp.activity.controller.TemplateViewController;
import com.azero.sampleapp.activity.launcher.viewmodel.LauncherViewModel;
import com.azero.sampleapp.impl.azeroexpress.navigation.NavigationHandler;
import com.azero.sampleapp.util.AzeroConstants;
import com.azero.sampleapp.util.Utils;
import com.azero.sampleapp.widget.AzeroOSListenerImpl;
import com.azero.sampleapp.widget.GlobalBottomBar;
import com.azero.sampleapp.widget.UIListenerImpl;
import com.azero.sdk.GlobalContext;
import com.azero.sdk.event.AzeroEvent;
import com.azero.sdk.impl.TemplateRuntime.TemplateDispatcher;
import com.azero.sdk.impl.TemplateRuntime.TemplateRuntimeHandler;
import com.azero.sdk.interfa.AzeroOSListener;
import com.azero.sdk.interfa.IAzeroExpressCallback;
import com.azero.sdk.manager.AzeroManager;
import com.azero.sdk.util.Constant;
import com.azero.sdk.util.executors.TaskExecutor;
import com.azero.sdk.util.log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

/**
 * 首页
 */
public class LauncherActivity extends AppCompatActivity implements AzeroOSListener {
    private static final String TAG = AzeroConstants.TAG + "LauncherActivity";
    private ViewPager viewPager;
    private LauncherPagerAdapter launcherPagerAdapter;
    private LauncherViewModel launcherViewModel;
    private static String mCurrentAudioItemId;
    private TemplateViewController mTemplateViewController;
    private TemplateRuntimeHandler mTemplateRuntimeHandler;
    private TemplateDispatcher mTemplateDispatch;
    private boolean isForeground = false;
    private NavigationHandler mNavigation;
    private UIListenerImpl mUIListenerImpl;

    private IAzeroExpressCallback mAzeroCallBack = new IAzeroExpressCallback() {
        @Override
        public void handleExpressDirective(String messageId,String type, String payload) {
            Log.d(TAG, "handleExpressDirective: type:" + type + ", payload: " + payload);
            try {
                JSONObject template = new JSONObject(payload);
                if (Constant.SKILL.ASR.equals(type) && template.has("text") && template.has("finished") ) {
                    if (!TextUtils.isEmpty(template.optString("text")) && template.optBoolean("finished")) {
                        if (template.optString("text").contains("上传唤醒音频")){
                            int result = AzeroManager.getInstance().uploadFile(Constant.PCMTYPE.WAKEUP);
                            log.e("上传唤醒音频成功：====》"+result);
                        } else if (template.optString("text").contains("上传原始音频")){
                            int result = AzeroManager.getInstance().uploadFile(Constant.PCMTYPE.RAW);
                            log.e("上传原始音频成功：====》"+result);
                        }  else if (template.optString("text").contains("上传识别音频")){
                            int result = AzeroManager.getInstance().uploadFile(Constant.PCMTYPE.ASR);
                            log.e("上传识别音频成功：====》"+result);
                        }

                    }
                    TaskExecutor.executeOnMainTnread(new Runnable() {
                        @Override
                        public void run() {
                            GlobalBottomBar.getInstance(GlobalContext.getContext()).append(template.optString("text"));
                        }
                    });
                } else if ("renderPlayerInfo".equals(type)) {
                    try {
                        JSONObject playerInfo = new JSONObject(payload);
                        String audioItemId = playerInfo.getString("audioItemId");
                        JSONObject content = playerInfo.getJSONObject("content");
                        String typeImpl = playerInfo.getString("type");

                        if (!audioItemId.equals(mCurrentAudioItemId)) {
                            // Log only if audio item has changed
                            mCurrentAudioItemId = audioItemId;
                            if (typeImpl.equals("NewsTemplate")) {
                                mTemplateViewController.showDisplayCard(playerInfo, TemplateViewController.SHOW_NEWS_INFO);
                            } else {
                                mTemplateViewController.showDisplayCard(content, TemplateViewController.RENDER_PLAYER_INFO);
                            }
                        }
                    } catch (Exception e) {
                        log.e(e.getMessage());
                    }
                } else if (Constant.SKILL.CLEARTEMPLATE.equals(type)) {
                    mTemplateViewController.clearTemplate();
                } else if (Constant.SKILL.CLEARPLAYERINFO.equals(type)) {
                    mTemplateViewController.clearPlayerInfo();
                } else {
                    if (mTemplateViewController == null) {
                        return;
                    }

                    // Log payload
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
                        case "LauncherTemplate1":
                            launcherViewModel.loadRecommendations(template);
                            break;
                        case Constant.SKILL.NAVIGATION:
                            mNavigation.handleDirective(template);
                            break;
                        default:
                            break;
                    }
                }
            } catch (Exception e) {

            }
        }
    };


    private static final int sPermissionRequestCode = 0;
    private static final String[] sRequiredPermissions = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mNavigation = new NavigationHandler(GlobalContext.getContext());
        mUIListenerImpl = new UIListenerImpl();
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
            try {
                if (!AzeroManager.getInstance().isEngineInitComplete()) {
                    AzeroManager.getInstance().addAzeroOSListener(this);
                    MyApplication.getInstance().initAzero();
                }
                create();
            } catch (RuntimeException e) {
                Utils.showAlertDialog(this, getString(R.string.alert_azero_init_failure), "Could not start engine. Reason: \n"
                        + e.getMessage());
                log.e(e.toString());
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void create() {
        log.d("========create");
        setContentView(R.layout.activity_launcher);
        TextClock textClock = findViewById(R.id.clock);
        viewPager = findViewById(R.id.container);
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/clock.ttf");
        textClock.setTypeface(typeFace);
        renderView();
        initViewModel();
//        textClock.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                AzeroManager.getInstance().acquireTts("窗前明月光，意识地上霜。举头望明月，低头思故乡。");
//            }
//        });

//        renderView();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 101);
        }

        if (AzeroManager.getInstance().isEngineInitComplete()) {
            //initAzero();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == sPermissionRequestCode) {
            boolean any_denied = false;
            if (grantResults.length > 0) {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        any_denied = true;
                        Utils.showAlertDialog(this, getString(R.string.alert_azero_init_failure), permissions[i] + getString(R.string.alert_azero_no_permission));
                    }
                }
                if (!any_denied) {
                    if (!AzeroManager.getInstance().isEngineInitComplete()) {
                        AzeroManager.getInstance().addAzeroOSListener(this);
                        MyApplication.getInstance().initAzero();
                    }
                    create();
                }
            } else {
                Utils.showAlertDialog(this, getString(R.string.alert_azero_init_failure), getString(R.string.alert_azero_no_permission));
            }
        }
    }

    private void initViewModel() {
        launcherViewModel = ViewModelProviders.of(this).get(LauncherViewModel.class);
        launcherViewModel.getRecommendationList().observe(this, recommendations -> {
            if (recommendations != null && recommendations.size() > 0) {
                log.d("initViewModel  launcherPagerAdapter==" + launcherPagerAdapter);
                launcherPagerAdapter.updateFragments(recommendations, launcherPagerAdapter.getCurrentPos());
            }
        });
        launcherViewModel.initDefaultRecommendation();
    }

    private void renderView() {
        log.d("renderView");
        viewPager.setPageTransformer(true, new LauncherPageTransformer());
        launcherPagerAdapter = new LauncherPagerAdapter(viewPager, getSupportFragmentManager());
        launcherPagerAdapter.setLoopInterval(10000);
        viewPager.setAdapter(launcherPagerAdapter);
        launcherPagerAdapter.startLoop();
        launcherPagerAdapter.setLauncherViewModel(launcherViewModel);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AzeroManager.getInstance().registerUIListener(mUIListenerImpl);
        log.e("start===");
        //initAzero();
        log.e("stop===");
        if (launcherPagerAdapter != null) {
            launcherPagerAdapter.startLoop();
        }
        isForeground = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (launcherPagerAdapter != null) {
            launcherPagerAdapter.stopLoop();
        }
        isForeground = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        log.e("onStop=====");
        //       AzeroManager.getInstance().registerUIListener(mUIListenerImpl);
//        boolean result = AzeroManager.getInstance().unRegisterAzeroExpressCallback(mAzeroCallBack);
//        log.e("result=====>"+result);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (mTemplateRuntimeHandler != null && mTemplateDispatch != null) {
//            mTemplateRuntimeHandler.unregisterTemplateDispatchedListener(mTemplateDispatch);
//        }
        AzeroManager.getInstance().removeAzeroOSListener(this);
        if (launcherPagerAdapter != null) {
            launcherPagerAdapter.onDestroy();
        }
    }

    @Override
    public void onEvent(AzeroEvent azeroEvent, String s) {
        log.d("AzeroOSListenerImpl:   AzeroEvent ===>" + azeroEvent + ",     msg===>" + s);
        switch (azeroEvent) {
            case EVENT_ENGINE_INITIALIZATION_COMPLETE:
                initAzero();
                break;
        }
    }


    private void initAzero() {
        log.d("initAzero");

        mTemplateViewController = new TemplateViewController(this.getApplicationContext());

        //各技能回调注册，如asr、navigation
        List<String> services = new ArrayList<>();
        services.add(Constant.SKILL.ASR);
        services.add(Constant.SKILL.NAVIGATION);
        services.add("renderPlayerInfo");
        services.add(Constant.SKILL.WEATHERTEMPLATE);
        services.add("BodyTemplate1");
        services.add("BodyTemplate2");

        services.add("ListTemplate1");
        services.add("DefaultTemplate1");
        services.add("DefaultTemplate2");
        services.add("DefaultTemplate3");

        services.add("LocalSearchListTemplate1");

        services.add("AlertsListTemplate");
        services.add("AlertRingtoneTemplate");
        services.add("LauncherTemplate1");
        AzeroManager.getInstance().registerAzeroExpressCallback(services, mAzeroCallBack);
        //各状态回调注册
        //AzeroManager.getInstance().addAzeroOSListener(new AzeroOSListenerImpl());
    }


    @Override
    public void onBackPressed() {
        //屏蔽返回事件
    }

    public boolean isForeground() {
        return isForeground;
    }
}
