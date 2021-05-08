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

import android.os.Bundle;
import androidx.constraintlayout.widget.Group;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.azero.platforms.iface.MediaPlayer;
import com.azero.sampleapp.R;
import com.azero.sampleapp.activity.playerinfo.bean.PushMessage;
import com.azero.sampleapp.activity.template.ConfigureTemplateView;
import com.azero.sdk.AzeroManager;
import com.azero.sdk.event.Command;
import com.azero.sdk.impl.MediaPlayer.MediaPlayerHandler;
import com.azero.sdk.util.Constant;
import com.azero.sdk.util.log;
import com.jakewharton.rxbinding3.view.RxView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import static com.azero.sampleapp.util.Utils.stringForTime;

public class PlayerInfoFragment extends BasePlayerInfoFragment {
    private static final String MODEL_LOOP = "LOOP";
    private static final String MODEL_SHUFFLE = "SHUFFLE";
    private static final String MODEL_SINGLE = "SINGLE_LOOP";
    protected ImageButton mControlPrev, mControlNext;
    protected ToggleButton mControlPlayPause, playModelToggle;
    protected TextView mProgressTime, mEndTime;
    protected SeekBar mProgress;
    private TextView mTitle;
    private TextView mTitleSubtext1;
    private TextView mTitleSubtext2;
    private ImageView mArt;
    private Group progressGroup;
    private MediaPlayerHandler mMediaPlayer;

    public PlayerInfoFragment() {
    }

    public static PlayerInfoFragment newInstance(String template) {
        PlayerInfoFragment fragment = new PlayerInfoFragment();
        Bundle args = new Bundle();
        args.putString(Constant.EXTRA_TEMPLATE, template);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.card_audio_info_template;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        mMediaPlayer = (MediaPlayerHandler) AzeroManager.getInstance().getHandler(AzeroManager.AUDIO_HANDLER);
        EventBus.getDefault().register(this);
        mTitle = rootView.findViewById(R.id.title);
        mTitleSubtext1 = rootView.findViewById(R.id.titleSubtext1);
        mTitleSubtext2 = rootView.findViewById(R.id.titleSubtext2);
        mArt = rootView.findViewById(R.id.art);
        mControlPrev = rootView.findViewById(R.id.prevControlButton);
        mControlPlayPause = rootView.findViewById(R.id.playControlButton);
        playModelToggle = rootView.findViewById(R.id.playModelToggle);
        mControlNext = rootView.findViewById(R.id.nextControlButton);

        mProgress = rootView.findViewById(R.id.mediaProgressBar);
        mProgressTime = rootView.findViewById(R.id.mediaProgressTime);
        mEndTime = rootView.findViewById(R.id.mediaEndTime);
        progressGroup = rootView.findViewById(R.id.progressGroup);

        mControlPlayPause.setEnabled(false);
        mControlPlayPause.setChecked(false);
        initClicks();
        try {
            Bundle bundle = getArguments();
            JSONObject template = new JSONObject(bundle.getString(Constant.EXTRA_TEMPLATE));
            ConfigureTemplateView.configurePlayerInfo(this, template);
        } catch (JSONException e) {
            log.e(e.getMessage());
            getActivity().finish();
        }
        mProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    //直接拖到头状态会有问题
                    if ((progress = seekBar.getProgress()) == 1000) {
                        progress = 999;
                    }
                    long duration = mMediaPlayer.getDuration();
                    long position = progress * duration / 1000L;
                    mEndTime.setText(stringForTime((int) duration));
                    mProgressTime.setText(stringForTime((int) position));
                    mMediaPlayer.setPosition(position);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @Override
    protected void lazyLoad() {
        log.e("lazyLoad*********");
    }

    private void initClicks() {
        addDisposable(RxView.clicks(mControlPrev)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(unit -> {
                    log.e("mControlPrev click event");
                    AzeroManager.getInstance().executeCommand(Command.CMD_PLAY_PREVIOUS);
                }));
        addDisposable(RxView.clicks(mControlNext)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(unit -> {
                    log.e("mControlNext click event");
                    AzeroManager.getInstance().executeCommand(Command.CMD_PLAY_NEXT);
                }));
        addDisposable(RxView.clicks(mControlPlayPause)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(unit -> {
                    log.e("mControlPlayPause click event");
                    if (mControlPlayPause.isChecked()) {
                        AzeroManager.getInstance().executeCommand(Command.CMD_PLAY_PLAY);
                    } else {
                        AzeroManager.getInstance().executeCommand(Command.CMD_PLAY_PAUSE);
                    }
                }));
        addDisposable(RxView.clicks(playModelToggle)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(unit -> {
                    log.e("playModelToggle click event");
                    changePlayModel(playModelToggle.getTag().toString());
                }));
    }

    private void executeRenderPlayerInfo(String payload) throws JSONException {
        log.e("executeRenderPlayerInfo payload: " + payload);
        JSONObject playerInfo = new JSONObject(payload);
        String type = playerInfo.getString("type");
        if (!"RenderPlayerInfo".equals(type)) {
            return;
        }
        if (playerInfo.has("content")) {
            JSONObject content = playerInfo.getJSONObject("content");
            String title = content.has("title") ? content.getString("title") : "";
            String artist = "";
            String album = "";
            if (content.has("provider")) {
                JSONObject provider = content.getJSONObject("provider");
                artist = provider.has("name") ? provider.getString("name") : "";
                album = provider.has("album") ? provider.getString("album") : "";
            }
            setPlayerInfo(title, artist, album);
        }
        //更新按钮状态和播放信息
        JSONArray controls = playerInfo.getJSONArray("controls");
        for (int j = 0; j < controls.length(); j++) {
            JSONObject control = controls.getJSONObject(j);
            if (control.getString("type").equals("BUTTON")) {
                final boolean enabled = control.getBoolean("enabled");
                final boolean selected = control.getBoolean("selected");
                final String name = control.getString("name");
                updateControlButton(name, enabled, selected);
            } else if (control.getString("type").equals("TOGGLE")) {
                final boolean selected = control.getBoolean("selected");
                final boolean enabled = control.getBoolean("enabled");
                final String name = control.getString("name");
                updateControlToggle(name, enabled, selected);
            }
        }
    }

    public void updateControlButton(final String name, final boolean enabled, final boolean selected) {
        log.e("updateControlButton:" + name + "=" + enabled + " selected:" + selected);
        switch (name) {
            case "PREVIOUS":
                mControlPrev.setEnabled(enabled);
                mControlPrev.setSelected(selected);
                break;
            case "PLAY_PAUSE":
                mControlPlayPause.setEnabled(enabled);
                break;
            case "NEXT":
                mControlNext.setEnabled(enabled);
                mControlNext.setSelected(selected);
                break;
        }
    }

    public void updateControlToggle(final String name, final boolean enabled, final boolean selected) {
        switch (name) {
            case "SHUFFLE":
                if (selected) {
                    playModelToggle.setEnabled(enabled);
                    playModelToggle.setSelected(selected);
                    playModelToggle.setTag(MODEL_SHUFFLE);
                }
                break;
            case "LOOP":
                if (selected) {
                    playModelToggle.setEnabled(enabled);
                    playModelToggle.setSelected(selected);
                    playModelToggle.setTag(MODEL_LOOP);
                }
                break;
            case "SINGLE_LOOP":
                if (selected) {
                    playModelToggle.setEnabled(enabled);
                    playModelToggle.setSelected(selected);
                    playModelToggle.setTag(MODEL_SINGLE);
                }
                break;
        }
        setPlayModel(playModelToggle.getTag().toString());
    }


    private void setPlayModel(String playModel) {
        int playModelRes = R.drawable.icon_music_loop;
        switch (playModel) {
            case MODEL_LOOP:
                playModelRes = R.drawable.icon_music_loop;
                break;
            case MODEL_SHUFFLE:
                playModelRes = R.drawable.icon_music_random;
                break;
            case MODEL_SINGLE:
                playModelRes = R.drawable.icon_music_single;
                break;
        }
        playModelToggle.setButtonDrawable(getResources().getDrawable(playModelRes));
    }

    public void setPlayerInfo(final String title, final String artist, String album) {
        mTitle.setText(title);
        mTitleSubtext1.setText(artist);
        mTitleSubtext2.setText(String.format(getString(R.string.album_info), album));
    }

    public void start() {
        mControlPlayPause.setEnabled(true);
        mControlPlayPause.setChecked(true);
    }

    public void stop() {
        mControlPlayPause.setChecked(false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receiverUpdate(PushMessage pushMessage) {
        switch (pushMessage.type) {
            case PushMessage.UPDATE_TEMPLATE:
                log.e("receiverUpdate==UPDATE_TEMPLATE");
                try {
                    JSONObject template = new JSONObject(pushMessage.getTemplate());
                    ConfigureTemplateView.configurePlayerInfo(this, template);
                } catch (JSONException e) {
                    log.e(e.getMessage());
                    getActivity().finish();
                }
                break;
            case PushMessage.UPDATE_PLAYERINFO:
                log.d("receiverUpdate==UPDATE_PLAYERINFO");
                try {
                    executeRenderPlayerInfo(pushMessage.getPayload());
                } catch (JSONException e) {
                    log.e(e.getMessage());
                    getActivity().finish();
                }
                break;
            case PushMessage.UPDATE_PROGRESS:
                long position = pushMessage.getPosition();
                long pos = 0;
                if (mMediaPlayer.getDuration() != 0) {
                    pos = 1000L * position / mMediaPlayer.getDuration();
                }
                mProgress.setProgress((int) pos);
                mEndTime.setText(stringForTime((int) mMediaPlayer.getDuration()));
                mProgressTime.setText(stringForTime((int) position));
                break;
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void mediaStateChange(MediaPlayer.MediaState mediaState) {
        log.d("mediaStateChange***" + mediaState);
        switch (mediaState) {
            case STOPPED:
                stop();
                break;
            case PLAYING:
                start();
                break;
            case BUFFERING:
                break;
        }
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


    public ImageView getArt() {
        return mArt;
    }

    public Group getProgressGroup() {
        return progressGroup;
    }


    public void changePlayModel(String playModel) {
        Command command;
        switch (playModel) {
            case MODEL_LOOP:
                playModelToggle.setTag(MODEL_SINGLE);
                command = Command.CMD_PLAY_REPEAT;
                break;
            case MODEL_SINGLE:
                playModelToggle.setTag(MODEL_SHUFFLE);
                command = Command.CMD_PLAY_SHUFFLE;
                break;
            case MODEL_SHUFFLE:
                playModelToggle.setTag(MODEL_LOOP);
                command = Command.CMD_PLAY_LOOP;
                break;
            default:
                playModelToggle.setTag(MODEL_LOOP);
                command = Command.CMD_PLAY_LOOP;
                break;
        }
        // setPlayModel(playModelToggle.getTag().toString());
        AzeroManager.getInstance().executeCommand(command);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        clearDisposable();
        EventBus.getDefault().unregister(this);
    }
}
