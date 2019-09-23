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

package com.azero.sampleapp.impl.andcallcontroller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.azero.platforms.phonecontrol.PhoneCallController;
import com.azero.sampleapp.activity.controller.LocalViewController;
import com.azero.sdk.AndLinkConfig;
import com.azero.sdk.AzeroManager;
import com.azero.sdk.impl.AndCallController.AbstractAndCallDispatcher;
import com.azero.sdk.impl.AndCallController.AndCallControllerHandler;
import com.azero.sdk.impl.AndLink.AndLinkManager;
import com.azero.sdk.impl.Common.InputManager;
import com.azero.sdk.util.executors.AppExecutors;
import com.azero.sdk.util.log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.azero.sdk.impl.AndCallController.AndCallErrorCode.IMS_LOGIN_FAILED;
import static com.azero.sdk.impl.AndCallController.AndCallErrorCode.NOT_FOUND_ANDLINK_DEVICE_ID;
import static com.azero.sdk.impl.AndCallController.AndCallErrorCode.NOT_LOGIN_IMS;
import static com.azero.sdk.impl.AndCallController.AndCallErrorCode.SIP_ERROR;
import static com.azero.sdk.impl.AndCallController.AndCallErrorCode.UP_LOAD_LOG_FAILED;

/**
 * @Author: sjy
 * @Date: 2019/8/15
 */
public class AndCallViewControllerHandler {

    private Context mContext;
    private LocalViewController mLocalViewController;

    private PhoneCallController.CallState mCallState;
    private AndLinkConfig mAndLinkConfig;

    private String mCurrentCallNumber;
    private String mCallType;
    private String mCallName;
    private AndCallControllerHandler mAndCallControllerHandler;

    @SuppressLint("StaticFieldLeak")
    private static AndCallViewControllerHandler INSTANCE;

    private AppExecutors appExecutors;
    private static final String CONTACT_ID = "contact_id";
    private static final String PHONE_NUMBER = "phone_number";

    public static AndCallViewControllerHandler getInstance() {
        if (INSTANCE == null) {
            synchronized (AndCallViewControllerHandler.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AndCallViewControllerHandler();
                }
            }
        }
        return INSTANCE;
    }

    private AndCallViewControllerHandler() {
        mCallState = PhoneCallController.CallState.IDLE;
        appExecutors = new AppExecutors();

        if (mAndCallControllerHandler == null) {
            mAndCallControllerHandler = (AndCallControllerHandler) AzeroManager.getInstance().getHandler(AzeroManager.ANDCALL_HANDLER);
        }
    }

    public void init(Context context, AndLinkConfig andLinkConfig, InputManager inputManager) {
        mContext = context;
        mAndLinkConfig = andLinkConfig;

        mAndCallControllerHandler.initDataInput(inputManager);
        registerListener();
    }

    private void registerListener() {

        mAndCallControllerHandler.registerAndCallControllerDispatchedListener(new AbstractAndCallDispatcher() {

            @Override
            public void onFailed(int errorCode, String errorMessage) {
                switch (errorCode) {
                    case SIP_ERROR:
                    case IMS_LOGIN_FAILED:
                    case UP_LOAD_LOG_FAILED:
                        showToast(errorMessage);
                        break;
                    case NOT_LOGIN_IMS:
                        showToast(errorMessage);
                        onLoginIms();
                        break;
                    case NOT_FOUND_ANDLINK_DEVICE_ID:
                    case 100003:
                        AndLinkManager.getInstance(mContext).requestAndLinkAddress();
                        showToast("您还没有绑定AndLink,请绑定后再试吧");
                        break;
                    default:
                        log.e("errorCode: " + errorCode + ", errorMessage: " + errorMessage);
                        break;
                }
            }

            @Override
            public void onCallStateChanged(PhoneCallController.CallState callState) {
                mCallState = callState;
            }

            @Override
            public void onInComingCall(String phoneNumber, int callType, int session) {
                mCurrentCallNumber = phoneNumber;
                mCallType = PHONE_NUMBER;
                showDisplayCard("InComingCall");
            }

            @Override
            public void onLoginSucceed(int i) {
                log.d("login success");
                appExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "和家固话登录成功", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onLogout() {
                showToast("和家固话已登出");
            }

            @Override
            public void onCallOutSuccess(int session, String callType, String phoneNumber, String callName) {
                log.d("onCallOutSuccess");
                mCurrentCallNumber = phoneNumber;
                mCallType = callType;
                mCallName = callName;
                showDisplayCard("CallOut");
            }

            @Override
            public void onOpenCameraError() {
                log.e("您的摄像头未安装或存在异常");
            }

            @Override
            public void onVideoCallOutSuccess(int session, String callType, String callName) {
                showDisplayCard("VideoCallOut");
                mCallType = callType;
                mCallName = callName;
                showDisplayCard("VideoCallOut");
            }
        });
    }

    private void showToast(String msg) {
        appExecutors.mainThread().execute(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void onUpLoadLog() {
        mAndCallControllerHandler.onUpLoadLog();
    }

    public void setLocalViewController(LocalViewController localViewController) {
        mLocalViewController = localViewController;
    }

    public void showDisplayCard(String type) {
        log.d("show display card type: " + type);
        try {
            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("type", type);
            jsonMap.put("callState", mCallState.toString());
            jsonMap.put("phoneNumber", mCurrentCallNumber);
            jsonMap.put("callType", mCallType);
            jsonMap.put("callName", mCallName);
            String json = JSON.toJSONString(jsonMap);
            JSONObject jsonObject = new JSONObject(json);
            mLocalViewController.showDisplayCard(jsonObject, LocalViewController.AND_CALL_ACTIVITY);
        } catch (JSONException json) {
            log.d("error json: " + json);
        }
    }

    public void onLogoutIms() {
        mAndCallControllerHandler.onLogoutIms();
    }

    public void onLoginIms() {
        //可用于自定义来电铃声,注释掉则默认播放来电号码提示音
//        AzeroManager.getInstance().getLocalMediaPlayerHandler().addRingtonePlayer(R.raw.ringtone);
        mAndCallControllerHandler.onLoginIms(mAndLinkConfig);
    }
}
