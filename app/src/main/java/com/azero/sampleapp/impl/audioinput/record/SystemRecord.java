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

package com.azero.sampleapp.impl.audioinput.record;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.azero.sdk.util.log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

/**
 *  默认的数据读取工具，使用AudioTrack读取单路数据，可运行在任何设备上
 *  由于获取不到回采数据，在播放音频时不易唤醒，仅供体验使用
 */
public class SystemRecord extends Record {
    // All audio input consumers expect PCM 16 data @ 16 Khz. We divide this consumption into 10 ms
    // chunks. It comes out at 160 samples every 10 ms to reach 16000 samples (in a second).
    private static final int sSamplesToCollectInOneCycle = 160;
    private static final int sBytesInEachSample = 2; // PCM 16 = 2 bytes per sample
    private static final int sSampleRateInHz = 16000; //16 khz
    private static final int sAudioFramesInBuffer = 5; // Create large enough buffer for 5 audio frames.
    private AudioRecord mAudioInput;
    private AudioReaderRunnable mReaderRunnable;

    private final ExecutorService mExecutor = Executors.newFixedThreadPool(1);

    public SystemRecord() {
        mAudioInput = createAudioInput();
    }

    @Override
    public void start() {
        try {
            mAudioInput.startRecording();
        } catch (IllegalStateException e) {
            log.e("AudioRecord cannot start recording. Error: "
                    + e.getMessage());
        }
        try {
            mExecutor.submit(mReaderRunnable = new AudioReaderRunnable()); // Submit the audio reader thread
        } catch (RejectedExecutionException e) {
            log.e("Audio reader task cannot be scheduled for execution. Error: "
                    + e.getMessage());
        }
    }

    @Override
    public void stop() {
        if (mReaderRunnable != null) mReaderRunnable.cancel();
        try {
            mAudioInput.stop();
        } catch (IllegalStateException e) {
            log.e("AudioRecord cannot stop recording. Error: "
                    + e.getMessage());
        }
    }

    private AudioRecord createAudioInput() {
        AudioRecord audioRecord = null;
        try {
            int minBufferSize = AudioRecord.getMinBufferSize(
                    sSampleRateInHz,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            int bufferSize = minBufferSize + (
                    sAudioFramesInBuffer * sSamplesToCollectInOneCycle * sBytesInEachSample);
            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC, sSampleRateInHz,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize);
        } catch (IllegalArgumentException e) {
            log.e("Cannot create audio input. Error: "
                    + e.getMessage());
        }
        return audioRecord;
    }

    //
    // AudioReader class
    //
    private class AudioReaderRunnable implements Runnable {

        private boolean mRunning = true;
        private byte[] mBuffer = new byte[sSamplesToCollectInOneCycle * sBytesInEachSample];
        private byte[] mBuffer2 = new byte[sSamplesToCollectInOneCycle * sBytesInEachSample * 2];

        void cancel() {
            mRunning = false;
        }

        boolean isRunning() {
            return mRunning;
        }

        @Override
        public void run() {
            int size;

            while (mRunning) {
                size = mAudioInput.read(mBuffer, 0, mBuffer.length);
                if (size > 0 && mRunning && listener != null) {
                    // 算法库至少需要一路回采数据
                    // 为单通道数据添加一个空的回采通道
                    for (int i = 0; i < size * 2; i += 2) {
                        if (i % 4 == 0) {
                            mBuffer2[i] = mBuffer[i / 2];
                            mBuffer2[i + 1] = mBuffer[i / 2 + 1];
                        }
                    }

                    listener.onData(mBuffer2, size * 2);
                }
            }
        }
    }
}
