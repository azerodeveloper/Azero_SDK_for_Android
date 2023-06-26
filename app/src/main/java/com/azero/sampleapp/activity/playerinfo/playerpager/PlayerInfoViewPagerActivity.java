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
package com.azero.sampleapp.activity.playerinfo.playerpager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.azero.platforms.iface.MediaPlayer;
import com.azero.sampleapp.R;
import com.azero.sampleapp.activity.playerinfo.BasePlayerInfoActivity;
import com.azero.sampleapp.activity.playerinfo.bean.PushMessage;
import com.azero.sampleapp.util.DownloadHelper;
import com.azero.sampleapp.util.FileUtils;
import com.azero.sampleapp.util.GlideManager;
import com.azero.sdk.impl.MediaPlayer.MediaPlayerHandler;
import com.azero.sdk.impl.TemplateRuntime.TemplateDispatcher;
import com.azero.sdk.impl.TemplateRuntime.TemplateRuntimeHandler;
import com.azero.sdk.interfa.IAzeroExpressCallback;
import com.azero.sdk.manager.AzeroManager;
import com.azero.sdk.manager.AzeroManagerImpl;
import com.azero.sdk.util.Constant;
import com.azero.sdk.util.log;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class PlayerInfoViewPagerActivity extends BasePlayerInfoActivity implements DownloadHelper.OnDownloadListener, View.OnClickListener {
    private String title;
    private File lyricFile;
    private ImageView ivBg, ivLogo;
    private TextView mHeader;
    private ViewPager mViewPager;
    private PushMessage pushMessage;
    private boolean hasLyrics = false;
    private DownloadHelper downloadHelper;
    private MediaPlayerHandler mMediaPlayer;
    private TemplateDispatcher templateDispatcher;
    private TemplateRuntimeHandler templateRuntimeHandler;
    private PlayerInfoPagerAdapter playerInfoPagerAdapter;
    private MediaPlayer.OnMediaStateChangeListener mediaStateChangeListener;
    private static final int EXTERNAL_STORAGE_REQ_CODE = 101;
    private CompositeDisposable compositeDisposable;

    private IAzeroExpressCallback mAZeroCallback =  new IAzeroExpressCallback() {
        @Override
        public void handleExpressDirective(String messageId,String type, String payload) {
            log.d("handleExpressDirective: type:" + type + ", payload: " + payload);
            try {
                if ("renderPlayerInfo".equals(type)) {
                    PushMessage pushMessage = new PushMessage(PushMessage.UPDATE_PLAYERINFO);
                    pushMessage.setPayload(payload);
                }
            } catch (Exception e) {

            }
        }
    };

    @Override
    protected int getLayoutResId() {
        return R.layout.card_audio_main_template;
    }

    @Override
    protected void initView() {
        super.initView();
        downloadHelper = DownloadHelper.getInstance();
        ImageButton mBtnBack = findViewById(R.id.ibtn_back);
        mHeader = findViewById(R.id.header);
        ivBg = findViewById(R.id.iv_bg);
        ivLogo = findViewById(R.id.iv_logo);
        mViewPager = findViewById(R.id.viewPager);
        requestPermission();
        initViewPager();
        initMediaState();
        //registerTemplate();
        mBtnBack.setOnClickListener(this);
        compositeDisposable = new CompositeDisposable();
        GlideManager.loadImgWithBlur(this, R.drawable.img_default, ivBg);
    }



    private void requestPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    private void initViewPager() {
        playerInfoPagerAdapter = new PlayerInfoPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(playerInfoPagerAdapter);
        log.d("init view pager!");
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                clearShowLyric();
                if (position == 1) {
                    EventBus.getDefault().post(new PushMessage(PushMessage.UPDATE_LYRIC));
                    mHeader.setVisibility(View.VISIBLE);
                } else {
                    mHeader.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void registerTemplate() {
        //各技能回调注册，如asr、navigation
        List<String> services = new ArrayList<>();
        services.add("renderPlayerInfo");
        AzeroManager.getInstance().registerAzeroExpressCallback(services,mAZeroCallback);



//        templateRuntimeHandler = (TemplateRuntimeHandler) AzeroManager.getInstance().getHandler(Constant.TEMPLATE_HANDLER);
//        templateDispatcher = new TemplateDispatcher() {
//            @Override
//            public void renderTemplate(String payload, String type) {
//                super.renderTemplate(payload, type);
//                log.e("registerTemplateDispatchedListener renderTemplate" + payload + "  type=" + type);
//            }
//
//            @Override
//            public void renderPlayerInfo(String payload) {
//                log.e("registerTemplateDispatchedListener");
//                PushMessage pushMessage = new PushMessage(PushMessage.UPDATE_PLAYERINFO);
//                pushMessage.setPayload(payload);
//                //EventBus.getDefault().post(pushMessage);
//            }
//        };
//
//        templateRuntimeHandler.registerTemplateDispatchedListener(templateDispatcher);
    }

    private void initMediaState() {
        mMediaPlayer = (MediaPlayerHandler) AzeroManager.getInstance().getHandler(Constant.AUDIO_HANDLER);
        log.e("v: " + mMediaPlayer.toString());
        mediaStateChangeListener = new MediaPlayer.OnMediaStateChangeListener() {
            @Override
            public void onMediaError(String playerName, String msg, MediaPlayer.MediaError mediaError) {
                log.d("playerName: " + playerName + " reason: " + msg + " Error:" + mediaError.toString());
            }

            @Override
            public void onMediaStateChange(String playerName, MediaPlayer.MediaState mediaState) {
                log.d("onMediaStateChange*********" + mediaState);
                if (mediaState.equals(MediaPlayer.MediaState.BUFFERING)) {
                    PushMessage pushMessage = new PushMessage(PushMessage.MEDIASTATE_PLAYING);
                    pushMessage.setFile(lyricFile);
                    EventBus.getDefault().post(pushMessage);
                }
            }

            @Override
            public void onPositionChange(String playerName, long position, long duration) {
                log.d(" onPositionChange playerName===>"+playerName+",   position===>"+position+",   duration===>"+duration);
                PushMessage pushMessage = new PushMessage(PushMessage.UPDATE_PROGRESS);
                pushMessage.setPosition(position);
                EventBus.getDefault().post(pushMessage);
            }
        };
        mMediaPlayer.addOnMediaStateChangeListener(mediaStateChangeListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerTemplate();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == EXTERNAL_STORAGE_REQ_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                log.e("Manifest.permission.WRITE_EXTERNAL_STORAGE success！");
            } else {
                log.e("Manifest.permission.WRITE_EXTERNAL_STORAGE failed！");
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void initData(Intent intent) {
        clearShowLyric();
        mViewPager.setCurrentItem(0);
        mHeader.setVisibility(View.GONE);
        String data = intent.getStringExtra(Constant.EXTRA_TEMPLATE);
        configTemplate(data);
        if (hasLyrics) {
            mHeader.setText(title);
            autoShowLyric();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        AzeroManager.getInstance().unRegisterAzeroExpressCallback(mAZeroCallback);
    }

    /**
     * 10s后自动跳转到歌词
     */
    private void autoShowLyric() {
        Disposable disposable = Observable.timer(10, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> mViewPager.setCurrentItem(1));
        compositeDisposable.add(disposable);
    }

    private void configTemplate(String data) {
        try {
            log.e("template" + data);
            playerInfoPagerAdapter.setTemplate(data);
            JSONObject template = new JSONObject(data);
            String lyricUrl;
            if (template.has("provider")) {
                // Set header subtext to provider name if no header subtext given
                JSONObject provider = template.getJSONObject("provider");
                if (provider.has("logo")) {
                    JSONObject logo = provider.getJSONObject("logo");
                    String url = getImageUrl(logo);
                    GlideManager.loadImg(this, url, ivLogo, 100, 100);
                }

                title = template.has("title")
                        ? template.getString("title") : "";
                if (provider.has("lyric")) {
                    lyricUrl = provider.getString("lyric");
                    String fileName = lyricUrl.substring(lyricUrl.lastIndexOf("/"));
                    hasLyrics = true;
                    playerInfoPagerAdapter.setPageCount(2);
                    String pathName = FileUtils.getExternalStorageDirectory();
                    downloadHelper.download(lyricUrl, pathName, fileName, this);

                } else {
                    sendClearLyricMsg();
                }
                playerInfoPagerAdapter.notifyDataSetChanged();
                if (template.has("art")) {
                    JSONObject art = template.getJSONObject("art");
                    String url = getImageUrl(art);
                    GlideManager.loadImgWithBlur(this, url, ivBg);
                }
            }
            PushMessage pushMessage = new PushMessage(PushMessage.UPDATE_TEMPLATE);
            pushMessage.setTemplate(data);
            EventBus.getDefault().post(pushMessage);
        } catch (JSONException e) {
            log.e(e.getMessage());
            finish();
        }
    }

    private void sendClearLyricMsg() {
        hasLyrics = false;
        playerInfoPagerAdapter.setPageCount(1);
        playerInfoPagerAdapter.notifyDataSetChanged();
        EventBus.getDefault().post(new PushMessage(PushMessage.CLEAR_LYRIC));
    }

    private static String getImageUrl(JSONObject image) {
        String url = null;

        try {
            JSONArray sources = image.getJSONArray("sources");
            HashMap<String, String> imageMap = new HashMap<>();

            for (int j = 0; j < sources.length(); j++) {
                JSONObject next = sources.getJSONObject(j);
                String size;
                if (next.has("size")) {
                    size = next.getString("size").toUpperCase();
                } else {
                    size = "DEFAULT";
                }
                imageMap.put(size, next.getString("url"));
            }

            if (imageMap.containsKey("DEFAULT")) {
                url = imageMap.get("DEFAULT");
            } else if (imageMap.containsKey("X-LARGE")) {
                url = imageMap.get("X-LARGE");
            } else if (imageMap.containsKey("LARGE")) {
                url = imageMap.get("LARGE");
            } else if (imageMap.containsKey("MEDIUM")) {
                url = imageMap.get("MEDIUM");
            } else if (imageMap.containsKey("SMALL")) {
                url = imageMap.get("SMALL");
            } else if (imageMap.containsKey("X-SMALL")) {
                url = imageMap.get("X-SMALL");
            }
        } catch (JSONException e) {
            log.e(e.getMessage());
        }
        return url;
    }

    @Override
    public void onDownloadSuccess(File file) {
        log.e("onDownloadSuccess****" + file.getAbsolutePath());
        lyricFile = file;
        pushMessage = new PushMessage(PushMessage.SETUP_LYRIC);
        pushMessage.setFile(file);
        EventBus.getDefault().postSticky(pushMessage);
    }

    @Override
    public void onDownloading(float progress) {

    }

    @Override
    public void onDownloadFailed(Exception e) {
        EventBus.getDefault().post(new PushMessage(PushMessage.CLEAR_LYRIC));
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.ibtn_back) {
            finish();
        }
    }

    public void clearShowLyric() {
        if (compositeDisposable != null) {
            compositeDisposable.clear();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pushMessage != null) {
            EventBus.getDefault().removeStickyEvent(pushMessage);
        }
//        if (templateRuntimeHandler != null && templateDispatcher != null) {
//            templateRuntimeHandler.unregisterTemplateDispatchedListener(templateDispatcher);
//        }
        if (mMediaPlayer != null && mediaStateChangeListener != null) {
            mMediaPlayer.removeOnMediaStateChangeListener(mediaStateChangeListener);
        }
        clearShowLyric();
    }
}
