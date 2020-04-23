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
import android.support.annotation.NonNull;

import com.azero.sampleapp.Setting;
import com.azero.sampleapp.impl.audioinput.record.Record;
import com.azero.sdk.impl.Common.InputManager;
import com.azero.sdk.util.log;

import java.util.ArrayList;
import java.util.List;

/**
 * 算法库处理后的数据输送模块
 *   提供降噪数据和VoIP数据
 *   提供唤醒回调
 *   提供本地VAD回调（需要enableLocalVAD）
 */
public class AudioInputManager extends InputManager {
    //降噪音频数据回调
    private List<AudioInputConsumer> mAudioInputConsumers;
    //唤醒回调
    private List<WakeUpConsumer> mWakeUpConsumers;
    //VoIP数据回调
    private List<VoipInputConsumer> mVoipInputConsumers;
    //算法库模块
    private OpenDenoiseManager openDenoiseManager;
    //Vad回调
    private LocalVadListener localVadListener;

    public interface WakeUpConsumer {
        void onWakewordDetected(String wakeWord);
    }

    public AudioInputManager(Context context,Record record) {
        mAudioInputConsumers = new ArrayList<AudioInputConsumer>();
        mWakeUpConsumers = new ArrayList<WakeUpConsumer>();
        mVoipInputConsumers = new ArrayList<VoipInputConsumer>();
        openDenoiseManager = new OpenDenoiseManager(context, record, Setting.enableLocalVAD, new OpenDenoiseManager.DenoiseCallback() {
            @Override
            public void onAsrData(byte[] data, int size) {
                notifyDataAvailableToAudioInputConsumers(data, size);
            }

            @Override
            public void onWakeUp(String wakeWord) {
                notifyWakeUpToWakeUpConsumer(wakeWord);
            }

            @Override
            public void onVadCallback(int result) {
                log.d("vad: " + result);
                if (result == 1 && localVadListener != null) {
                    localVadListener.onLocalVadEnd();
                } else if (localVadListener != null && result == 0) {
                    localVadListener.onLocalVadBegin();
                }
            }

            @Override
            public void onVoIpData(byte[] data, int size) {
                notifyDataAvailableToVoIpInputConsumers(data, size);
            }
        });
    }

    @Override
    public boolean stopAudioInput(@NonNull AudioInputConsumer consumer) {
        log.i("Stop recording request received from " + consumer.getAudioInputConsumerName());

        int consumersLeft = 0;
        synchronized (mAudioInputConsumers) {
            mAudioInputConsumers.remove(consumer);
            consumersLeft = mAudioInputConsumers.size();
        }

        if (consumersLeft == 0) {
            log.i("Stopping recording for the last client " + consumer.getAudioInputConsumerName());
            openDenoiseManager.stopRecognize();
            return true;
        }

        log.i("Audio recording wouldnt be stopped on account of remaining clients");
        return true;
    }

    @Override
    public void startvoip(@NonNull InputManager.VoipInputConsumer voipInputConsumer) {
        onStartVoIp(voipInputConsumer);
    }

    @Override
    public void stopvoip(@NonNull InputManager.VoipInputConsumer voipInputConsumer) {
        onStopVoIp(voipInputConsumer);
    }

    @Override
    public boolean startAudioInput(@NonNull AudioInputConsumer consumer) {
        log.i("Start recording request received from " + consumer.getAudioInputConsumerName());

        synchronized (mAudioInputConsumers) {
            mAudioInputConsumers.add(consumer);
        }

        if (openDenoiseManager.isWakeUp()) {
            log.i("Audio recording already in progress");
            return true;
        }
        openDenoiseManager.startRecognize();
        return true;
    }

    public void onStopVoIp(@NonNull InputManager.VoipInputConsumer consumer) {
        log.i("Stop recording request received from " + consumer.getVoIpInputConsumerName());
        int consumersLeft = 0;
        synchronized (mVoipInputConsumers) {
            mVoipInputConsumers.remove(consumer);
            consumersLeft = mVoipInputConsumers.size();
        }

        if (consumersLeft == 0) {
            log.i("Stopping recording for the last client " + consumer.getVoIpInputConsumerName());
            openDenoiseManager.stopVoip();
        }
        log.i("Audio recording wouldnt be stopped on account of remaining clients");
    }

    public void onStartVoIp(@NonNull InputManager.VoipInputConsumer consumer) {
        log.i("Start recording request received from " + consumer.getVoIpInputConsumerName());
        synchronized (mVoipInputConsumers) {
            mVoipInputConsumers.add(consumer);
        }
        openDenoiseManager.startVoip();
    }

    public void addWakeUpObserver(@NonNull WakeUpConsumer consumer) {
        synchronized (mWakeUpConsumers) {
            mWakeUpConsumers.add(consumer);
        }
    }

    public void removeWakeUpObserver(@NonNull WakeUpConsumer consumer) {
        synchronized (mWakeUpConsumers) {
            mWakeUpConsumers.remove(consumer);
        }
    }


    private void notifyDataAvailableToAudioInputConsumers(byte[] buffer, int size) {
        synchronized (mAudioInputConsumers) {
            for (AudioInputConsumer consumer : mAudioInputConsumers) {
                consumer.onAudioInputAvailable(buffer, size);
            }
        }
    }

    private void notifyWakeUpToWakeUpConsumer(String wakeWord) {
        synchronized (mAudioInputConsumers) {
            for (WakeUpConsumer consumer : mWakeUpConsumers) {
                consumer.onWakewordDetected(wakeWord);
            }
        }
    }

    public void setLocalVadListener(LocalVadListener localVadListener) {
        this.localVadListener = localVadListener;
    }

    public interface LocalVadListener {
        void onLocalVadEnd();
        void onLocalVadBegin();
    }

    private void notifyDataAvailableToVoIpInputConsumers(byte[] data, int size) {
        synchronized (mVoipInputConsumers) {
            for (InputManager.VoipInputConsumer consumer : mVoipInputConsumers) {
                consumer.onVoIpInputAvailable(data, size);
            }
        }
    }
}
