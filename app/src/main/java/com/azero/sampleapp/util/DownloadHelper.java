package com.azero.sampleapp.util;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadHelper {

    private static final String TAG = "Sound.DownloadHelper";

    private static DownloadHelper instance = new DownloadHelper();

    private OkHttpClient okHttpClient;

    private DownloadHelper() {
        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build();
        }
    }

    public static DownloadHelper getInstance() {
        return instance;
    }

    /**
     * @param url      下载连接
     * @param dir      下载文件存储目录
     * @param filename 文件名
     * @param listener 下载状态
     */

    public void download(final String url, final String dir, final String filename, final DownloadHelper.OnDownloadListener listener) {
        if (url == null) return;
        File tmp = new File(dir, filename);
        if (tmp.exists()) {
            Log.d("Download", "文件存在");
            listener.onDownloadSuccess(tmp);
            return;
        }
        Request request = new Request.Builder().url(url).build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.onDownloadFailed(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                InputStream is = null;
                byte[] buffer = new byte[1024];
                int len = 0;
                FileOutputStream fos = null;
                File file = new File(dir);

                try {
                    if (!file.exists()) {
                        if (!file.mkdir()) {
                            Log.d("Download", "onResponse: 文件创建失败");
                        } else {
                            Log.d("Download", "onResponse: 文件创建成功");
                        }
                    } else {
                        Log.d("Download", "onResponse: 文件存在");
                    }

                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    File file1 = new File(file, filename);
                    fos = new FileOutputStream(file1);
                    long sum = 0;
                    while ((len = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                        sum += len;
                        float progress = ((sum * 0.01f) / (total * 0.01f));
                        listener.onDownloading(progress);
                    }
                    fos.flush();
                    listener.onDownloadSuccess(file1);
                } catch (Exception e) {
                    listener.onDownloadFailed(e);
                } finally {
                    if (is != null) {
                        is.close();
                    }
                    if (fos != null) {
                        fos.close();
                    }
                }
            }
        });
    }

    public interface OnDownloadListener {
        void onDownloadSuccess(File file);

        void onDownloading(float progress);

        void onDownloadFailed(Exception e);
    }
}
