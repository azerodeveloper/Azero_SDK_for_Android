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

package com.azero.sampleapp.impl.audioinput;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.azero.sampleapp.MyApplication;
import com.azero.sampleapp.widget.GlobalBottomBar;
import com.azero.sdk.impl.Common.InputManager;
import com.azero.sdk.impl.SpeechRecognizer.AbsSpeechRecognizer;
import com.azero.sdk.util.executors.AppExecutors;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 语音识别数据管理模块
 * 唤醒时 调用{@link #onTapToTalk()} 或 {@link #onHoldToTalk()} 请求唤醒
 * 成功后会回调{@link #startAudioInput()}
 * 然后通过{@link #write(byte[], long)}接口灌入数据
 * <p>
 * {@link #onTapToTalk()} 唤醒后由云端下发识别结束事件，被动停止识别。
 * {@link #onHoldToTalk()} 按下按钮识别内容，松开后停止识别。由本地控制识别内容长度，主动停止识别。
 * <p>
 * 识别停止后回调{@link #stopAudioInput()} 停止灌入数据
 */
public class SpeechRecognizerHandler extends AbsSpeechRecognizer
        implements AudioInputManager.AudioInputConsumer, AudioInputManager.LocalVadListener {

    private static final String TAG = "SpeechRecognizerHandler";

    private final InputManager mAudioInputManager;
    // Only true if holdToTalk() returned true
    private AtomicBoolean mAllowStopCapture = new AtomicBoolean(false);
    private AtomicBoolean mReceiveVadEnd = new AtomicBoolean(false);
    private boolean mVadTimeoutStart = false;

    private long beginTime;
    private AppExecutors mAppExecutors;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private final static int VAD_IS_ENDED = 0;
    private final static int VAD_NOT_ENDED = 1;

    private Context mContext;
    private CountDownTimer vadTimeOutTimer = new CountDownTimer(300, 300) {
        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            Log.d(TAG, "onFinish: vad time out");
            onReleaseHoldToTalk();
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case VAD_IS_ENDED:
                    Log.d(TAG, "handleMessage: vad end.");
                    mReceiveVadEnd.set(true);
                    if (mVadTimeoutStart) {
                        cancelVadTimeOutTimer();
                        onReleaseHoldToTalk();
                    }
                    break;
                case VAD_NOT_ENDED:
                    mReceiveVadEnd.set(false);
                    break;
                default:
                    break;
            }
        }
    };

    public SpeechRecognizerHandler(AppExecutors executors,
                                   Context context,
                                   InputManager audioInputManager,
                                   boolean wakeWordSupported,
                                   boolean wakeWordEnabled) {
        super(wakeWordSupported && wakeWordEnabled);
        mContext = context;
        mAudioInputManager = audioInputManager;
        mAppExecutors = executors;
        ((AudioInputManager) mAudioInputManager).setLocalVadListener(this);
    }

    /**
     * SDK回调，要求开始灌入数据
     *
     * @return 是否可以灌入数据
     */
    @Override
    public boolean startAudioInput() {
        Log.d(TAG, "startAudioInput: ");
        MyApplication.getInstance().isAudioInputting = true;
        beginTime = System.currentTimeMillis();
        mAllowStopCapture.set(true);
        showWakeupDialogByAudioInput();
        return mAudioInputManager.startAudioInput(this);
    }

    /**
     * SDK回调，要求停止灌入数据
     *
     * @return 停止灌入是否成功
     */
    @Override
    public boolean stopAudioInput() {
        Log.d(TAG, "stopAudioInput: mVadTimeoutStart: " + mVadTimeoutStart +
                ", mReceiveVadEnd: " + mReceiveVadEnd +
                ", mAllowStopCapture: " + mAllowStopCapture);
        if (mVadTimeoutStart) {
            Log.d(TAG, "stopAudioInput: audio input is stopped.");
            cancelVadTimeOutTimer();
        }
        if (mAllowStopCapture.get()) {
            mAllowStopCapture.set(false);
        }
        MyApplication.getInstance().isAudioInputting = false;
        hideWakeupDialog();
        return mAudioInputManager.stopAudioInput(this);
    }

    /**
     * 唤醒成功
     *
     * @param wakeWord 唤醒词
     */
    @Override
    public boolean wakewordDetected(String wakeWord) {
        mAudioCueObservable.playAudioCue(AudioCueState.START_VOICE);
        return true;
    }

    /**
     * 唤醒过程结束
     */
    @Override
    public void endOfSpeechDetected() {
        Log.d(TAG, "endOfSpeechDetected: ");
        mAudioCueObservable.playAudioCue(AudioCueState.END);
    }


    /**
     * TapToTalk唤醒模式，唤醒后由云端下发识别结束事件，被动停止识别。
     */
    @Override
    public void onTapToTalk() {
        Log.d(TAG, "onTapToTalk: ");
        showWakeupDialogByAudioInput();
        if (tapToTalk()) {
            Log.d(TAG, "onTapToTalk: tap to talk success");
            mAudioCueObservable.playAudioCue(AudioCueState.START_TOUCH);
        }
    }

    /**
     * HoldToTalk唤醒模式，按下按钮识别内容，松开后停止识别。由本地控制识别内容长度，主动停止识别。
     */
    @Override
    public void onHoldToTalk() {
        showWakeupDialogByWakeup();
        if (holdToTalk()) {
            mAllowStopCapture.set(true);
            Log.d(TAG, "onHoldToTalk: hold to talk");
            mAudioCueObservable.playAudioCue(AudioCueState.START_TOUCH);
        }
    }

    /**
     * 与{@link #onHoldToTalk()}配套，用于主动结束识别。
     */
    @Override
    public void onReleaseHoldToTalk() {
        Log.d(TAG, "onReleaseHoldToTalk: ");
        mExecutor.execute(new StopCaptureRunnable());
    }

    /**
     * SDK回调，获取模块名称
     *
     * @return 模块名称
     */
    @Override
    public String getAudioInputConsumerName() {
        return "SpeechRecognizer";
    }


    /**
     * 回调音频数据
     *
     * @param buffer 数据buffer
     * @param size   数据长度
     * @name AudioInputConsumer Functions
     */
    @Override
    public void onAudioInputAvailable(byte[] buffer, int size) {
        // Write audio samples to engine
        write(buffer, size);
    }


    /**
     * 通过唤醒弹出唤醒提示框
     */
    private void showWakeupDialogByWakeup() {
        mAppExecutors.mainThread().execute(() -> GlobalBottomBar.getInstance(mContext).show("", 0, true));
    }

    /**
     * 多轮中弹出唤醒提示框
     */
    private void showWakeupDialogByAudioInput() {
        mAppExecutors.mainThread().execute(() -> GlobalBottomBar.getInstance(mContext).show("", 0, false));
    }

    /**
     * 收回唤醒提示框
     */
    private void hideWakeupDialog() {
        Log.d(TAG, "hideWakeupDialog: ");
        mAppExecutors.mainThread().execute(() -> GlobalBottomBar.getInstance(mContext).hide(2000));
    }

    /**
     * 本地VAD事件，收到后主动停止识别
     */
    @Override
    public void onLocalVadEnd() {
        Log.d(TAG, "onLocalVadEnd: receive vad end");
        if (mAllowStopCapture.get()) {
            long diffTime = System.currentTimeMillis() - beginTime;
            //VAD截断时间过短，忽略此次事件
            if (diffTime < 500) {
                Log.d(TAG, "onLocalVadEnd: diffTime < 1000");
            } else {
                Log.d(TAG, "onLocalVadEnd: local vad end, diffTime: " + diffTime + "mReceiveVadEnd: " + mReceiveVadEnd.get() + ", mAllowStopCapture: " + mAllowStopCapture.get());
                if (mReceiveVadEnd.get()) {
                    onReleaseHoldToTalk();
                } else {
                    vadTimeOutTimer.start();
                    mReceiveVadEnd.set(false);
                    mVadTimeoutStart = true;
                }
            }
        }
    }

    @Override
    public void onLocalVadBegin() {
        Log.d(TAG, "onLocalVadBegin: mVadTimeoutStart: " + mVadTimeoutStart);
        if (mVadTimeoutStart) {
            cancelVadTimeOutTimer();
        }
    }

    public void onReceivedAsrText(JSONObject payloadObject) {
        Log.d(TAG, "onReceivedAsrText: payload: " + payloadObject.toString());
        Message message = new Message();

        if (payloadObject.has("vadEnded")) {
            try {
                boolean vadEnded = payloadObject.getBoolean("vadEnded");
                if (vadEnded) {
                    message.what = VAD_IS_ENDED;
                    mHandler.sendMessage(message);
                    return;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (mReceiveVadEnd.get()) {
            message.what = VAD_NOT_ENDED;
            mHandler.sendMessage(message);
        }
    }

    private void cancelVadTimeOutTimer() {
        Log.d(TAG, "cancelVadTimeOutTimer: mReceiveVadEnd: " + mReceiveVadEnd +
                ", mVadTimeoutStart: " + mVadTimeoutStart);
        vadTimeOutTimer.cancel();
        mReceiveVadEnd.set(false);
        mVadTimeoutStart = false;
    }

    private class StopCaptureRunnable implements Runnable {

        @Override
        public void run() {
            if (mAllowStopCapture.get()) {
                Log.d(TAG, "StopCaptureRunnable run: mVadTimeoutStart: " + mVadTimeoutStart);
                boolean stopCapture = stopCapture();
                Log.d(TAG, "StopCaptureRunnable run: stopCapture: " + stopCapture);
                if (stopCapture) {
                    mAllowStopCapture.set(false);
                    if (mVadTimeoutStart) {
                        cancelVadTimeOutTimer();
                    }
                }
            }
        }
    }

}