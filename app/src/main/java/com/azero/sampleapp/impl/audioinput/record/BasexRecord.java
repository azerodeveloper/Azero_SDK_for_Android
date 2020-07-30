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

import com.soundai.basex.lib.BasexEventListener;
import com.soundai.basex.lib.BasexManager;

/**
 * SoundAI 提供的数据读取工具 Basex
 * 从TinyCap读取数据，与硬件的MIC强相关，需要调整参数，已达到最优唤醒体验效果
 * 由于使用时会占用硬件设备，会导致AudioRecord无法获取数据，如有其他应用需要使用MIC
 * 请临时关闭Basex或选用其他数据读取方式。
 */
public class BasexRecord extends Record {
    private static final int RUNTIME = -1;                           // 录音时长，单位帧，-1：一直录音
    private static final String HW = "hw:0,0";                       // 录音设备，hw:[card],[device]
    private static final int MIC_NUM = 4;                            // mic 数量
    private static final int CHANNEL_NUM = 6;                        // channel 数量
    private static final String CHANNEL_MAP = "0,1,2,3,4,5";         // 通道顺序
    private static final int BIT = 32;                               // 位深
    private static final int SAMPLE_RATE = 16000;                    // 采样率
    private static final int MIC_SHIFT = 16;                         // mic移位，默认不移位，值为16
    private static final int REF_SHIFT = 16;                         // 回采移位，默认不移位，值为16
    private static final int MODE = 0;                               // 是否加密，默认0
    private static final int PERIOD_SIZE = 256;                      // 缓存大小，对应tinycap 的-p属性
    private static final int BUFFER_SIZE = 4;                        // 缓存数量，默认4

    @Override
    public void start() {
        BasexManager.getInstance().setListener(new BasexEventListener() {
            @Override
            public void onSaveData(byte[] data, int size) {
                //如需测试工具读取数据是否正常，请打开如下注释，并赋予“空间存储”权限
//                FileUtils.writeFile(data,"/sdcard/basexdata.pcm",true);
                if (listener != null) {
                    listener.onData(data, size);
                }
            }

            @Override
            public void onRecordStart() {
            }

            @Override
            public void onRecordEnd() {
            }
        });

        BasexManager.getInstance().initBasex(
                RUNTIME,
                HW,
                MIC_NUM,
                CHANNEL_NUM,
                CHANNEL_MAP,
                BIT,
                SAMPLE_RATE,
                MIC_SHIFT,
                REF_SHIFT,
                MODE,
                PERIOD_SIZE,
                BUFFER_SIZE,
                null
        );
    }

    @Override
    public void stop() {
        BasexManager.getInstance().releaseBase();
    }
}
