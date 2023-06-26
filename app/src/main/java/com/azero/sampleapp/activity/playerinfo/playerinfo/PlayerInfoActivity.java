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

package com.azero.sampleapp.activity.playerinfo.playerinfo;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.azero.platforms.iface.MediaPlayer;
import com.azero.sampleapp.R;
import com.azero.sampleapp.activity.playerinfo.BasePlayerInfoActivity;
import com.azero.sampleapp.activity.template.ConfigureTemplateView;
import com.azero.sdk.event.Command;
import com.azero.sdk.impl.MediaPlayer.MediaPlayerHandler;
import com.azero.sdk.impl.TemplateRuntime.TemplateDispatcher;
import com.azero.sdk.interfa.IAzeroExpressCallback;
import com.azero.sdk.manager.AzeroManager;
import com.azero.sdk.util.Constant;
import com.azero.sdk.util.log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.azero.sampleapp.util.Utils.stringForTime;

/**
 * 音频播放界面
 */
public class PlayerInfoActivity extends BasePlayerInfoActivity {
    private TextView mHeader;
    private TextView mHeaderSubtext1;
    private TextView mTitle;
    private TextView mTitleSubtext1;
    private TextView mTitleSubtext2;
    private ImageView mPartnerLogo;
    private ImageView mArt;
    private boolean seeking = false;

    private MediaPlayer.OnMediaStateChangeListener mMediaStateChangeListener;
    private TemplateDispatcher mTemplateDispatcher;

    private IAzeroExpressCallback mAzeroCallback = new IAzeroExpressCallback() {
        @Override
        public void handleExpressDirective(String messageId,String s, String s1) {
            runOnUiThread(() -> {
                try {
                    executeRenderPlayerInfo(s1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        }
    };

    @Override
    protected int getLayoutResId() {
        return R.layout.card_render_player_info;
    }

    @Override
    protected void initView() {
        super.initView();
        mHeader = findViewById(R.id.header);
        mHeaderSubtext1 = findViewById(R.id.headerSubtext1);
        mTitle = findViewById(R.id.title);
        mTitleSubtext1 = findViewById(R.id.titleSubtext1);
        mTitleSubtext2 = findViewById(R.id.titleSubtext2);
        mPartnerLogo = findViewById(R.id.partnerLogo);
        mArt = findViewById(R.id.art);

        mControlPrev.setOnClickListener((View view) -> AzeroManager.getInstance().executeCommand(Command.CMD_PLAY_PREVIOUS));
        mControlNext.setOnClickListener((View view) -> AzeroManager.getInstance().executeCommand(Command.CMD_PLAY_NEXT));
        mControlPlayPause.setOnClickListener((View view) -> {
            if (mControlPlayPause.isChecked()) {
                AzeroManager.getInstance().executeCommand(Command.CMD_PLAY_PLAY);
            } else {
                AzeroManager.getInstance().executeCommand(Command.CMD_PLAY_PAUSE);
            }
        });

        registerListeners();

        start();
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerListeners();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterListeners();
    }

    @Override
    protected void initData(Intent intent) {
        try {
            JSONObject template = new JSONObject(intent.getStringExtra(Constant.EXTRA_TEMPLATE));
            ConfigureTemplateView.configurePlayerInfo(this, template);
        } catch (JSONException e) {
            log.e(e.getMessage());
            finish();
        }
    }

    private void registerListeners() {
        mMediaStateChangeListener = new MediaPlayer.OnMediaStateChangeListener() {
            @Override
            public void onMediaError(String playerName, String msg, MediaPlayer.MediaError mediaError) {
                log.e("playerName: " + playerName + " reason: " + msg + " Error:" + mediaError.toString());
            }

            @Override
            public void onMediaStateChange(String playerName, MediaPlayer.MediaState mediaState) {
                log.d("onMediaStateChange: playerName===>"+playerName+",     mediaState===>"+mediaState);
                switch (mediaState) {
                    case STOPPED:
                        runOnUiThread(() -> stop());
                        break;
                    case PLAYING:
                        runOnUiThread(() -> start());
                        break;
                    case BUFFERING:
                        break;
                }
            }

            @Override
            public void onPositionChange(String playerName, long position, long duration) {
                log.d("playerName===>"+playerName+",   position===>"+position+",   duration===>"+duration);
                long pos = 0;
                if (duration != 0) {
                    pos = 1000L * position / duration;
                }
                mProgress.setProgress((int) pos);
                mEndTime.setText(stringForTime((int) duration));
                mProgressTime.setText(stringForTime((int) position));
            }
        };
        MediaPlayerHandler mMediaPlayer = (MediaPlayerHandler) AzeroManager.getInstance().getHandler(Constant.AUDIO_HANDLER);
        mMediaPlayer.addOnMediaStateChangeListener(mMediaStateChangeListener);

        mProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (seeking) {
                    long duration = mMediaPlayer.getDuration();
                    long position = seekBar.getProgress() * duration / 1000L;
                    mEndTime.setText(stringForTime((int) duration));
                    mProgressTime.setText(stringForTime((int) position));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seeking = false;
                long duration = mMediaPlayer.getDuration();
                int progress = 0;
                //直接拖到头状态会有问题
                if ((progress = seekBar.getProgress()) == 1000) {
                    progress = 999;
                }
                long position = progress * duration / 1000L;
                log.d("progress:" + seekBar.getProgress());
                mMediaPlayer.setPosition(position);
            }
        });

//        mTemplateDispatcher = new TemplateDispatcher() {
//            @Override
//            public void renderPlayerInfo(String payload) {
//                runOnUiThread(() -> {
//                    try {
//                        executeRenderPlayerInfo(payload);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                });
//            }
//        };
//        TemplateRuntimeHandler templateRuntimeHandler = (TemplateRuntimeHandler) AzeroManager.getInstance().getHandler(Constant.TEMPLATE_HANDLER);
//        templateRuntimeHandler.registerTemplateDispatchedListener(mTemplateDispatcher);
        List<String> services = new ArrayList<>();
        services.add("renderPlayerInfo");
        AzeroManager.getInstance().registerAzeroExpressCallback(services,mAzeroCallback);
    }

    private void unregisterListeners() {
        MediaPlayerHandler mMediaPlayer = (MediaPlayerHandler) AzeroManager.getInstance().getHandler(Constant.AUDIO_HANDLER);
        //TemplateRuntimeHandler templateRuntimeHandler = (TemplateRuntimeHandler) AzeroManager.getInstance().getHandler(Constant.TEMPLATE_HANDLER);
        mMediaPlayer.removeOnMediaStateChangeListener(mMediaStateChangeListener);
        //templateRuntimeHandler.unregisterTemplateDispatchedListener(mTemplateDispatcher);
        AzeroManager.getInstance().unRegisterAzeroExpressCallback(mAzeroCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregisterListeners();
    }

    private void executeRenderPlayerInfo(String payload) throws JSONException {
        log.d("payload: " + payload);
        JSONObject playerInfo = new JSONObject(payload);
        String type = playerInfo.getString("type");
        if (!"RenderPlayerInfo".equals(type)) {
            return;
        }
        String audioItemId = playerInfo.getString("audioItemId");
        JSONObject content = playerInfo.getJSONObject("content");
        JSONObject audioProvider = content.getJSONObject("provider");
        String providerName = audioProvider.getString("name");
        //更新按钮状态和播放信息
        JSONArray controls = playerInfo.getJSONArray("controls");
        for (int j = 0; j < controls.length(); j++) {
            JSONObject control = controls.getJSONObject(j);
            if (control.getString("type").equals("BUTTON")) {
                final boolean enabled = control.getBoolean("enabled");
                final String name = control.getString("name");
                updateControlButton(name, enabled);
            } else if (control.getString("type").equals("TOGGLE")) {
                final boolean selected = control.getBoolean("selected");
                final boolean enabled = control.getBoolean("enabled");
                final String name = control.getString("name");
                updateControlToggle(name, enabled, selected);
            }
        }

        String title = content.has("title") ? content.getString("title") : "";
        String artist = content.has("titleSubtext1") ? content.getString("titleSubtext1") : "";
        JSONObject provider = content.getJSONObject("provider");
        String name = provider.has("name") ? provider.getString("name") : "";
        setPlayerInfo(title, artist, name);
    }

    public void updateControlButton(final String name, final boolean enabled) {
        switch (name) {
            case "PREVIOUS":
                mControlPrev.setEnabled(enabled);
                break;
            case "PLAY_PAUSE":
                mControlPlayPause.setEnabled(enabled);
                break;
            case "NEXT":
                mControlNext.setEnabled(enabled);
                break;
        }
    }

    public void updateControlToggle(final String name, final boolean enabled, final boolean selected) {
    }

    public void setPlayerInfo(final String title, final String artist, final String provider) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTitle.setText(title);
            }
        });
    }

    public void start() {
        mControlPrev.setEnabled(true);
        mControlPlayPause.setEnabled(true);
        mControlPlayPause.setChecked(true);
        mControlNext.setEnabled(true);
        mProgress.setMax(1000);
    }

    public void stop() {
        mControlPlayPause.setChecked(false);
    }


    public TextView getHeader() {
        return mHeader;
    }

    public TextView getHeaderSubtext1() {
        return mHeaderSubtext1;
    }

    public TextView getmTitle() {
        return mTitle;
    }

    public TextView getTitleSubtext1() {
        return mTitleSubtext1;
    }

    public TextView getTitleSubtext2() {
        return mTitleSubtext2;
    }

    public ImageView getPartnerLogo() {
        return mPartnerLogo;
    }

    public ImageView getArt() {
        return mArt;
    }
}
