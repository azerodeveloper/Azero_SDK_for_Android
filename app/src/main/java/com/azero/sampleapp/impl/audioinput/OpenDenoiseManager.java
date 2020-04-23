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

import com.azero.sampleapp.util.CopyConfigTask;
import com.azero.sampleapp.impl.audioinput.record.BasexRecord;
import com.azero.sampleapp.impl.audioinput.record.Record;
import com.azero.sdk.AzeroManager;
import com.azero.sdk.util.log;
import com.soundai.open_denoise.denoise.SaiClient;

import java.io.File;

/**
 * SoundAI算法库模块
 * 用于处理语音数据，可提供ASR数据及VoIP数据
 */
public class OpenDenoiseManager {

    private Context context;
    //core
    private SaiClient saiClient;
    //是否处于唤醒状态
    private boolean wakeUp = false;
    //过滤首个VAD事件
    private boolean filterFirstVAD = false;
    //过滤唤醒事件
    private boolean filterWakeup = false;
    //是否开启本地VAD
    private boolean mEnableLocalVad = false;
    //数据源
    private Record mRecord;
    //唤醒角度
    private float angle = 0;
    //前次VAD事件
    private int lastVAD = -1;
    //算法库回调
    private DenoiseCallback denoiseCallback;
    //唤醒后未检测到人声，超时时间
    private final int VADBEGINTIMEOUT = 4 * 1000;
    //唤醒后检测到人声，截断超时时间
    private final int VADENDTIMEOUT = 10 * 1000;
    //静态变量
    private final int VAD_BEGIN = 0;
    private final int VAD_ENG = 1;

    private CountDownTimer vadbeginTimer = new CountDownTimer(VADBEGINTIMEOUT, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            if (!mEnableLocalVad) {
                return;
            }
            lastVAD = VAD_ENG;
            if (denoiseCallback != null) {
                denoiseCallback.onVadCallback(VAD_ENG);
            }
            log.e("VAD Begin Timeout!");
        }
    };

    private CountDownTimer vadEndTimer = new CountDownTimer(VADENDTIMEOUT, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            if (!mEnableLocalVad) {
                return;
            }
            lastVAD = VAD_ENG;
            if (denoiseCallback != null) {
                denoiseCallback.onVadCallback(VAD_ENG);
            }
            log.e("VAD End Timeout!");
        }
    };


    public OpenDenoiseManager(Context context, Record record, boolean enableLocalVad, final DenoiseCallback denoiseCallback) {
        saiClient = SaiClient.getInstance();
        this.context = context;
        this.denoiseCallback = denoiseCallback;
        this.mRecord = record;
        mEnableLocalVad = enableLocalVad;
        String assertsDirName;
        if (mRecord instanceof BasexRecord) {
            assertsDirName = "config";
        } else {
            assertsDirName = "config2";
        }
        new CopyConfigTask(context, assertsDirName).setConfigListener(new CopyConfigTask.ConfigListener() {
            @Override
            public void onSuccess(String configPath) {
                if (initSaiClient(configPath, denoiseCallback) > 0) {
                    log.e("OpenDenoise init Error!");
                } else {
                    log.d("OpenDenoise init Succeed");
                    startAudioInput();
                }
            }

            @Override
            public void onFailed(String errorMsg) {
                log.d("onFailed: " + errorMsg);
            }
        }).execute();

        //读取的数据灌入到算法库中
        mRecord.setDataListener((data, size) -> {
            saiClient.feedData(data);
        });
    }

    private int initSaiClient(String configPath, final DenoiseCallback denoiseCallback) {
        return saiClient.init(
                context,
                true,
                configPath,
                "ViewPageHelper",
                AzeroManager.getInstance().generateToken(context),
                new SaiClient.Callback() {
                    @Override
                    public void onAsrDataCallback(byte[] data, int size) {
//                        FileUtils.writeFile(data,"/sdcard/asrdata.pcm",true);
                        if (denoiseCallback != null) {
                            denoiseCallback.onAsrData(data, size);
                        }
                    }

                    @Override
                    public void onVoipDataCallback(byte[] bytes, int size) {
                        if (denoiseCallback != null) {
                            denoiseCallback.onVoIpData(bytes, size);
                        }
                    }

                    @Override
                    public void onWakeupCallback(float wakeup_angle, String wakeup_word, float score, byte[] data) {
                        if (filterWakeup) {
                            filterWakeup = false;
                            return;
                        }
                        log.d("Wake up!angle:" + wakeup_angle + "word:" + wakeup_word);
                        angle = wakeup_angle;
                        wakeUp = true;
                        filterFirstVAD = false;
                        if (denoiseCallback != null) {
                            denoiseCallback.onWakeUp(wakeup_word);
                        }
                        if (lastVAD != VAD_BEGIN) {
                            vadEndTimer.cancel();
                            vadbeginTimer.cancel();
                            vadbeginTimer.start();
                        }
                    }

                    @Override
                    public void onVadCallback(int vadResult) {
                        if (!mEnableLocalVad) {
                            return;
                        }
                        log.d("localVad:" + vadResult);
                        if (filterFirstVAD) {
                            log.e("filter");
                            filterFirstVAD = false;
                            return;
                        }
                        if (denoiseCallback != null) {
                            denoiseCallback.onVadCallback(vadResult);
                        }
                        if (vadResult == VAD_BEGIN) {
                            log.d("lastVAD: " + lastVAD);
                            //重复收到vadBegin事件，避免重新计时
                            if (lastVAD != VAD_BEGIN) {
                                vadbeginTimer.cancel();
                                vadEndTimer.start();
                            }
                        } else if (vadResult == VAD_ENG) {
                            vadEndTimer.cancel();
                        }
                        lastVAD = vadResult;
                    }
                }
        );
    }

    public boolean isWakeUp() {
        return wakeUp;
    }

    public interface DenoiseCallback {
        void onAsrData(byte[] data, int size);

        void onWakeUp(String wakeWord);

        void onVoIpData(byte[] data, int size);

        /**
         * return local VAD result
         * 0 vadBegin
         * 1 vadEnd
         */
        void onVadCallback(int result);
    }

    public void startVoip() {
        saiClient.startVoip();
    }

    public void stopVoip() {
        saiClient.stopVoip();
    }

    public void startRecognize() {
        log.d("recognize start!");
        saiClient.startBeam(angle);
        filterFirstVAD = true;
        //只需要数据回调，阻止唤醒事件
        filterWakeup = true;
    }

    public void stopRecognize() {
        log.d("recognize stop!");
        saiClient.stopBeam();
        wakeUp = false;
    }

    public void startAudioInput() {
        mRecord.start();
    }

    public void stopAudioInput() {
        mRecord.stop();
    }
}
