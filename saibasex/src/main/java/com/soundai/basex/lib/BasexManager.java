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

package com.soundai.basex.lib;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BasexManager {
    public static final String TAG = "SoundAI_open_basex";
    public static final int FLAG_SAVE_DATA = 1;
    public static final int FLAG_RECORD_START = 2;
    public static final int FLAG_RECORD_END = 3;
    private static BasexManager mInstance;

    private BasexJni mJni;
    private BasexEventListener mListener;
    ExecutorService basexThreadPool;
    ExecutorService saveDataThreadPool;

    private Handler mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case FLAG_SAVE_DATA:
                    break;
                case FLAG_RECORD_START:
                    if (mListener != null) mListener.onRecordStart();
                    break;
                case FLAG_RECORD_END:
                    if (mListener != null) mListener.onRecordEnd();
                    break;
            }
            return false;
        }
    });

    private BasexManager() {
        try{
            System.loadLibrary("open_basex_lib");
            System.loadLibrary("sai_micbasex");
            mJni = new BasexJni();
            basexThreadPool = Executors.newSingleThreadExecutor();
            saveDataThreadPool = Executors.newSingleThreadExecutor();
        }catch (Exception ex){
            ex.getStackTrace();
            Log.e(TAG,ex.getMessage());
        }
    }

    public synchronized static BasexManager getInstance() {
        if (mInstance == null) {
            mInstance = new BasexManager();
        }
        return mInstance;
    }

    public void initBasex(final int runtime, final String hw, final int micNum, final int channelNum, final String channelMap,
                          final int bit, final int sampleRate, final int micShift, final int refShift, final int mode,
                          final int periodSize, final int bufferSize, final BasexEventListener listener) {

        if (listener != null) setListener(listener);
        final BasexJniCallback callback = new BasexJniCallback() {
            @Override
            public void onSaveData(final byte[] data, final int size) {
                saveDataThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (mListener != null) mListener.onSaveData(data, size);
                    }
                });
            }

            @Override
            public void onRecordStart() {
                mHandler.sendEmptyMessage(FLAG_RECORD_START);
            }

            @Override
            public void onRecordEnd() {
                mHandler.sendEmptyMessage(FLAG_RECORD_END);
            }
        };

        Runnable basexRunnable = new Runnable() {
            @Override
            public void run() {
                mJni.initBaseX(runtime, hw, micNum, channelNum, channelMap, bit,
                        sampleRate, micShift, refShift, mode, periodSize, bufferSize, callback);
            }
        };
        basexThreadPool.execute(basexRunnable);
    }

    public void releaseBase() {
        if (mJni != null) {
            mJni.releaseBaseX();
        }
    }

    public BasexEventListener getListener() {
        return mListener;
    }

    public void setListener(BasexEventListener mListener) {
        this.mListener = mListener;
    }
}
