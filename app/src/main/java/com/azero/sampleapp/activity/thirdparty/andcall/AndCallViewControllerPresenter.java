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

package com.azero.sampleapp.activity.thirdparty.andcall;

import android.view.SurfaceView;

import com.azero.platforms.phonecontrol.PhoneCallController;
import com.azero.sdk.AzeroManager;
import com.azero.sdk.impl.AndCallController.AbstractAndCallDispatcher;
import com.azero.sdk.impl.AndCallController.AndCallControllerHandler;
import com.azero.sdk.util.log;
import com.mobile.voip.sdk.constants.VoIPConstant;

import org.json.JSONException;
import org.json.JSONObject;

import static com.azero.platforms.phonecontrol.PhoneCallController.CallState.ACTIVE;
import static com.azero.platforms.phonecontrol.PhoneCallController.CallState.CALL_RECEIVED;
import static com.azero.platforms.phonecontrol.PhoneCallController.CallState.DIALING;
import static com.azero.platforms.phonecontrol.PhoneCallController.CallState.IDLE;
import static com.azero.platforms.phonecontrol.PhoneCallController.CallState.INBOUND_RINGING;
import static com.azero.platforms.phonecontrol.PhoneCallController.CallState.OUTBOUND_RINGING;
import static com.azero.sdk.impl.AndCallController.AndCallErrorCode.QUERY_NAME_FAILED;

/**
 * @Author: sjy
 * @Date: 2019/8/15
 */
public class AndCallViewControllerPresenter extends AbstractAndCallDispatcher implements AndCallContract.Presenter {

    private AndCallControllerHandler mAndCallControllerHandler;
    private AndCallContract.View view;

    private int mSession;
    private String mCurrentCallNumber;
    private String mCallType;
    private String mCallName;

    private PhoneCallController.CallState mCallState;
    private boolean muteFlag = false;//判断是否静音
    private boolean videoFlag = false;//判断是否为视频通话
    private boolean isComingEncodeErrorMsg = false;

    private static final String CONTACT_ID = "contact_id";
    private static final String PHONE_NUMBER = "phone_number";

    public AndCallViewControllerPresenter() {
        if (mAndCallControllerHandler == null) {
            mAndCallControllerHandler = (AndCallControllerHandler) AzeroManager.getInstance().getHandler(AzeroManager.ANDCALL_HANDLER);
        }
        mCurrentCallNumber = "";
        registerListener();
    }

    @Override
    public void unregisterListener() {
        mAndCallControllerHandler.unregisterAndCallControllerDispatchedListener(this);
    }

    private void registerListener() {
        mAndCallControllerHandler.registerAndCallControllerDispatchedListener(this);
    }

    @Override
    public void activityStart(AndCallContract.View view, String data) {
        this.view = view;
        try {
            JSONObject jsonObject = new JSONObject(data);
            String callState = jsonObject.getString("callState");
            String type = jsonObject.getString("type");
            switch (type) {
                case "VideoCallOut":
                    videoFlag = true;
                    break;
                case "CallOut":
                case "InComingCall":
                    videoFlag = false;
                    break;
                default:
                    break;
            }
            mCallState = getState(callState);
            if (jsonObject.has("phoneNumber")) {
                mCurrentCallNumber = jsonObject.getString("phoneNumber");
            }
            if (jsonObject.has("callType")) {
                mCallType = jsonObject.getString("callType");
            }
            if (jsonObject.has("callName")) {
                mCallName = jsonObject.getString("callName");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        log.d("callState: " + mCallState.toString() + ", videoFlag: " + videoFlag);
        switch (mCallState) {
            case DIALING:
                if (!videoFlag) {
                    view.callOutAudio();
                } else {
                    view.callOutVideo();
                }
                break;
            case CALL_RECEIVED:
            case INBOUND_RINGING:
                if (!videoFlag) {
                    view.callAlertingAudio();
                } else {
                    view.callAlertingVideo();
                }
                break;
            default:
                break;
        }
        changeCallState(mCallState);
        mAndCallControllerHandler.onQueryImsContact(mCurrentCallNumber);
    }

    @Override
    public void onClickPickUp() {
        mAndCallControllerHandler.onLocalAnswer();
    }

    @Override
    public void onClickDecline() {
        mAndCallControllerHandler.onLocalDecline();
    }

    @Override
    public void onClickMute() {
        muteFlag = !muteFlag;
        mAndCallControllerHandler.onSetInputMute(muteFlag);
        view.changeMuteButton(muteFlag);
    }

    @Override
    public void onClickKeyboard(int num) {
        log.d("onClickKeyboard: num: " + num + "Session: " + mSession);
        mAndCallControllerHandler.onSendDtmfMessage(num);
    }

    @Override
    public SurfaceView getLocalPreviewSurfaceView() {
        return mAndCallControllerHandler.getLocalPreviewSurfaceView(mSession);
    }

    @Override
    public SurfaceView getRemotePreviewSurfaceView() {
        return null;
    }

    private void changeCallState(PhoneCallController.CallState callState) {
        switch (callState) {
            case DIALING:
                view.changeContactState("正在拨号中，请稍后");
                break;
            case OUTBOUND_RINGING:
                view.changeContactState("正在呼叫，等待对方接听");
                break;
            case ACTIVE:
                view.changeContactState("正在通话中");
                break;
            case CALL_RECEIVED:
            case INBOUND_RINGING:
                view.changeContactState("收到来电");
                break;
            default:
                break;
        }
    }

    private PhoneCallController.CallState getState(String str) {
        switch (str) {
            case "IDLE":
                return IDLE;
            case "DIALING":
                return DIALING;
            case "INBOUND_RINGING":
                return INBOUND_RINGING;
            case "OUTBOUND_RINGING":
                return OUTBOUND_RINGING;
            case "ACTIVE":
                return ACTIVE;
            case "CALL_RECEIVED":
                return CALL_RECEIVED;
            default:
                return IDLE;
        }
    }

    @Override
    public void onFailed(int errorCode, String errorMessage) {
        switch (errorCode) {
            case QUERY_NAME_FAILED:
                view.updateName("");
                view.setContactNumber(mCurrentCallNumber);
                break;
            default:
                break;
        }
    }

    @Override
    public void onCallStateChanged(PhoneCallController.CallState callState) {
        mCallState = callState;
        changeCallState(mCallState);
    }

    @Override
    public void onInComingCall(String phoneNumber, int callType, int session) {
        if (VoIPConstant.CallType.CALLTYPE_1V1_AUDIO_IMS == callType) {
            videoFlag = false;
        } else if (VoIPConstant.CallType.CALLTYPE_1V1_VIDEO_IMS == callType) {
            videoFlag = true;
        }
        mCurrentCallNumber = phoneNumber;
        mCallType = PHONE_NUMBER;
    }

    @Override
    public void onLoginSucceed(int i) {
        log.d("login success");
    }

    @Override
    public void onCallAlerting(int i) {
        changeCallState(mCallState);
    }

    @Override
    public void onStopCallAlerting(int i) {

    }

    @Override
    public void onCallAnswered(int session, int calltype) {
        changeCallState(mCallState);
    }

    @Override
    public void onBeforeReceiveHangup(int session, String reason) {
        mSession = session;
        view.hangUp();
    }

    @Override
    public void onAfterReceiveHangup(int session, String reason) {
        mSession = session;
        view.hangUp();
    }

    @Override
    public void onHangUpCall(int session) {
        mSession = session;
        view.hangUp();
    }

    @Override
    public void onPickupCall(int session) {
        mSession = session;
        changeCallState(mCallState);
        if (videoFlag) {
            view.pickUpVideo();
        } else {
            view.pickUpAudio();
        }
    }

    @Override
    public void onCallOutSuccess(int session, String callType, String phoneNumber, String callName) {
        log.d("onCallOutSuccess");
        mCurrentCallNumber = phoneNumber;
        mSession = session;
        mCallType = callType;
        mCallName = callName;
    }

    @Override
    public void onQuerySuccess(String imsNick) {
        if (CONTACT_ID.equals(mCallType)) {
            view.setContactNumber("");
        } else if (PHONE_NUMBER.equals(mCallType)) {
            mCallName = imsNick;
            view.setContactNumber(mCurrentCallNumber);
        }
        view.updateName(mCallName);
    }

    @Override
    public void onOpenCameraError() {
        log.e("您的摄像头未安装或存在异常");
        view.receiveOpenCameraError();
    }

    @Override
    public void onVideoCallOutSuccess(int session, String callType, String callName) {
        mSession = session;
        videoFlag = true;
        mCallType = callType;
    }

    @Override
    public void incomingCodecChanged(int session, int channel, int width, int height) {
        view.calculateView(width, height);
        if (videoFlag) {
            if (isComingEncodeErrorMsg) {
                isComingEncodeErrorMsg = false;
            }
            view.loadRemoteView();
        } else {
            log.d("当前不是视频通话模式");
        }
    }

    @Override
    public void outgoingCodecChanged(int session, int channel, int width, int height) {
        view.loadLocalView(width, height);
    }

    @Override
    public void videoEncodeModeChanged(boolean isOpenGl) {
        log.d("videoEncodeModeChanged isOpenGL: " + isOpenGl);
        isComingEncodeErrorMsg = true;
        view.setComingEncodeErrorMsg(isComingEncodeErrorMsg);
    }
}
