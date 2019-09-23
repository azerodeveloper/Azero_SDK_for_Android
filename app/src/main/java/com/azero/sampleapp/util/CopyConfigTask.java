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

package com.azero.sampleapp.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 复制配置文件到data目录的Task
 */
public final class CopyConfigTask {

    private static final String TAG = "SoundAI";

    private static final String CONFIG_DIR_NAME = "sai_config";

    private static final int BUFFER_SIZE = 8192;

    private AssetManager assetManager;
    private String configPath;
    private String assetsDirName;
    private ConfigListener configListener;
    private Handler handler = new Handler(Looper.getMainLooper());

    public CopyConfigTask(Context context, String assetsDirName) {
        assetManager = context.getAssets();
        configPath = context.getFilesDir().toString() + File.separator + CONFIG_DIR_NAME;
        this.assetsDirName = assetsDirName;
    }

    /**
     * Task完成后从此回调反馈
     *
     * @param listener 回调接口
     * @return
     */
    public CopyConfigTask setConfigListener(ConfigListener listener) {
        this.configListener = listener;
        return this;
    }

    public void execute() {
        this.execute(configPath, true);
    }

    public void execute(boolean isOverride) {
        execute(configPath, isOverride);
    }

    public void execute(String destPath) {
        execute(destPath, true);
    }

    public void execute(String destPath, boolean isOverride) {
        new CopyThread(destPath, isOverride).start();
    }

    /**
     * 复制文件线程
     */
    private final class CopyThread extends Thread {

        private String destPath;
        private boolean isOverride;

        CopyThread(String destPath, boolean isOverride) {
            this.destPath = destPath;
            this.isOverride = isOverride;
        }

        @Override
        public void run() {
            Log.i(TAG, "CopyConfigTask start.");
            try {
                final File destDir = new File(destPath);
                if (!destDir.exists()) {
                    if (!destDir.mkdirs()) {
                        handleError("mkdirs 'sai_config' failed.");
                    }
                }
                String[] fileNames = assetManager.list(assetsDirName);
                if (fileNames.length > 0) {
                    for (String fileName : fileNames) {
                        copyFilesFromAssets(isOverride,
                                destDir.getAbsolutePath() + File.separator + fileName,
                                assetsDirName + File.separator + fileName);
                    }
                }
                if (configListener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            configListener.onSuccess(destPath);
                        }
                    });
                }
            } catch (final IOException e) {
                e.printStackTrace();
                handleError(e.getMessage());
            }
        }

        /**
         * 从Assets复制配置文件
         *
         * @param isOverride    是否覆盖
         * @param destPath      目标地址
         * @param assetFilename 文件名
         * @throws IOException
         */
        private void copyFilesFromAssets(boolean isOverride, String destPath, String assetFilename) throws IOException {
            File outFile = new File(destPath);
            if (outFile.exists()) {
                if (isOverride) {
                    outFile.delete();
                    Log.i(TAG, "overriding file " + destPath);
                } else {
                    Log.i(TAG, "file " + destPath + " already exists. No isOverride.\n");
                    return;
                }
            }
            InputStream is = assetManager.open(assetFilename);
            FileOutputStream fos = new FileOutputStream(destPath);
            try {
                byte[] buffer = new byte[BUFFER_SIZE];
                int readBytes;
                while ((readBytes = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, readBytes);
                }
                fos.flush();
            } finally {
                closeStream(fos, is);
            }
        }

        /**
         * 错误捕捉
         *
         * @param errorMsg 错误信息
         */
        private void handleError(final String errorMsg) {
            if (configListener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        configListener.onFailed(errorMsg);
                    }
                });
            }
        }
    }

    /**
     * 结果回调
     */
    public interface ConfigListener {
        /**
         * Task Finish
         *
         * @param configPath 存储路径
         */
        void onSuccess(String configPath);

        /**
         * 任务执行失败
         *
         * @param errorMsg 错误信息
         */
        void onFailed(String errorMsg);
    }

    public static void closeStream(Closeable... closeables) {
        for (Closeable c : closeables) {
            try {
                if (c != null) {
                    c.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
