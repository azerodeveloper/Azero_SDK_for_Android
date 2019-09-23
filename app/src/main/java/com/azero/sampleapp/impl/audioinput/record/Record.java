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

/**
 * Record基类，创建读取数据实例，插入到{@link com.azero.sampleapp.impl.audioinput.SpeechRecognizerHandler}中
 * 负责为识别模块提供数据
 */
public abstract class Record {
    /**
     * 开始读取数据
     */
    public abstract void start();

    /**
     * 停止读取数据
     */
    public abstract void stop();


    protected Listener listener;

    public void setDataListener(Listener listener) {
        this.listener = listener;
    }

    /**
     * 数据回调
     */
    public interface Listener {
        void onData(byte[] data, int size);
    }
}
