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

import android.content.Context;
import android.os.CountDownTimer;

import com.azero.sampleapp.widget.GlobalBottomBar;
import com.azero.sdk.impl.Common.InputManager;
import com.azero.sdk.impl.SpeechRecognizer.AbsSpeechRecognizer;
import com.azero.sdk.util.executors.AppExecutors;
import com.azero.sdk.util.log;

import org.json.JSONException;
import org.json.JSONObject;

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
    private final InputManager mAudioInputManager;
    // Only true if holdToTalk() returned true
    private boolean mAllowStopCapture = false;
    private boolean mVadTimeoutStart = false;
    private boolean mReceiveVadEnd = false;
    private long beginTime;
    private AppExecutors appExecutors;
    private Context mContext;
    private CountDownTimer vadTimeOutTimer = new CountDownTimer(300, 300) {
        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            log.d("VAD TIMEOUT TIMER TEST: time out");
            onReleaseHoldToTalk();
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
        appExecutors = executors;
        ((AudioInputManager) mAudioInputManager).setLocalVadListener(this);
    }

    /**
     * SDK回调，要求开始灌入数据
     *
     * @return 是否可以灌入数据
     */
    @Override
    public boolean startAudioInput() {
        log.d("startAudioInput");
        beginTime = System.currentTimeMillis();
        mAllowStopCapture = true;
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
        log.d("stopAudioInput");
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
        mAudioCueObservable.playAudioCue(AudioCueState.END);
    }


    /**
     * TapToTalk唤醒模式，唤醒后由云端下发识别结束事件，被动停止识别。
     */
    @Override
    public void onTapToTalk() {
        log.d("speech on tap to talk");
        showWakeupDialogByAudioInput();
        if (tapToTalk()) {
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
            mAllowStopCapture = true;
            mAudioCueObservable.playAudioCue(AudioCueState.START_TOUCH);
        }
    }

    /**
     * 与{@link #onHoldToTalk()}配套，用于主动结束识别。
     */
    @Override
    public void onReleaseHoldToTalk() {
        if (mAllowStopCapture) {
            log.d("VAD TIMEOUT TIMER TEST: onReleaseHoldToTalk");
            if (stopCapture()) {
                mAllowStopCapture = false;
                cancelVadTimeOutTimer();
            }
        }
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
        write(buffer, size); // Write audio samples to engine
    }


    /**
     * 通过唤醒弹出唤醒提示框
     */
    private void showWakeupDialogByWakeup() {
        appExecutors.mainThread().execute(() -> GlobalBottomBar.getInstance(mContext).show("", 0, true));
    }

    /**
     * 多轮中弹出唤醒提示框
     */
    private void showWakeupDialogByAudioInput() {
        appExecutors.mainThread().execute(() -> GlobalBottomBar.getInstance(mContext).show("", 0, false));
    }

    /**
     * 收回唤醒提示框
     */
    private void hideWakeupDialog() {
        appExecutors.mainThread().execute(() -> GlobalBottomBar.getInstance(mContext).hide(2000));
    }

    /**
     * 本地VAD事件，收到后主动停止识别
     */
    @Override
    public void onLocalVadEnd() {
        long diffTime = System.currentTimeMillis() - beginTime;
        //VAD截断时间过短，忽略此次事件
        if (diffTime < 500) {
            log.d("diffTime < 1000 :" + diffTime);
        } else {
            log.d("VAD TIMEOUT TIMER TEST: local vad end");
            startVadTimeOutTimer();
        }
    }

    @Override
    public void onLocalVadBegin() {
        cancelVadTimeOutTimer();
    }

    public void onReceivedAsrText(JSONObject payloadObject) {
        log.d("VAD TIMEOUT TIMER TEST: asr text payload: " + payloadObject.toString());
        if (payloadObject.has("vadEnded")) {
            try {
                boolean vadEnded = payloadObject.getBoolean("vadEnded");
                if (vadEnded) {
                    mReceiveVadEnd = true;
                    if (mVadTimeoutStart) {
                        cancelVadTimeOutTimer();
                        onReleaseHoldToTalk();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void startVadTimeOutTimer() {
        log.d("VAD TIMEOUT TIMER TEST: start, mReceiveVadEnd: " + mReceiveVadEnd);
        if (mReceiveVadEnd) {
            onReleaseHoldToTalk();
        } else {
            vadTimeOutTimer.start();
            mReceiveVadEnd = false;
            mVadTimeoutStart = true;
        }
    }

    private void cancelVadTimeOutTimer() {
        log.d("VAD TIMEOUT TIMER TEST: cancel");
        vadTimeOutTimer.cancel();
        mReceiveVadEnd = false;
        mVadTimeoutStart = false;
    }

}