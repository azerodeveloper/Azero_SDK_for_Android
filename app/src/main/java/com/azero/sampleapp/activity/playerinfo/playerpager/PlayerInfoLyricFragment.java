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

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import com.azero.sampleapp.R;
import com.azero.sampleapp.activity.playerinfo.bean.PushMessage;
import com.azero.sampleapp.util.LyricManager;
import com.azero.sampleapp.widget.LyricView;
import com.azero.sdk.util.Constant;
import com.azero.sdk.util.log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PlayerInfoLyricFragment extends BasePlayerInfoFragment{
    private LyricView mLyricView;
    private LyricManager lyricManager;
    private int lineNum = -1;
    private String name = "";
    public PlayerInfoLyricFragment() { }

    public static PlayerInfoLyricFragment newInstance(String template) {
        PlayerInfoLyricFragment fragment = new PlayerInfoLyricFragment();
        Bundle args = new Bundle();
        args.putString(Constant.EXTRA_TEMPLATE,template);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.card_audio_lyric_template;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        lyricManager = LyricManager.getInstance(getActivity().getApplicationContext());
        mLyricView = rootView.findViewById(R.id.lyricView);
        lyricManager.setOnProgressChangedListener(new LyricManager.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(String singleLine, boolean refresh) {
            }

            @Override
            public void onProgressChanged(SpannableStringBuilder stringBuilder, int lineNumber, boolean refresh) {
               log.e("onProgressChanged****lineNumber:"+lineNumber+" refresh:"+refresh+"  stringBuilder:"+stringBuilder);
                if (lineNum != lineNumber || refresh) {
                    lineNum = lineNumber;
                    mLyricView.setText(stringBuilder);
                    mLyricView.setCurrentPosition(lineNumber);
                }
            }
        });
        try {
            Bundle bundle = getArguments();
            JSONObject template = new JSONObject(bundle.getString(Constant.EXTRA_TEMPLATE));
            configurePlayerInfo(template);
        } catch (JSONException e) {
            log.e(e.getMessage());
            getActivity().finish();
        }
    }

    public  void configurePlayerInfo( JSONObject template) {
        try {
            if (template.has("provider")) {
                // Set header subtext to provider name if no header subtext given
                JSONObject provider = template.getJSONObject("provider");
                if (provider.has("name")) {
                    name = provider.getString("name");
                }
            }
        } catch (JSONException e) {
            log.e(e.getMessage());
        }
    }

    @Override
    protected void lazyLoad() {
        log.e("lazyLoad***");
    }

    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void receiverUpdate(PushMessage pushMessage){
        switch (pushMessage.type){
            case PushMessage.UPDATE_PROGRESS:
                lyricManager.setCurrentTimeMillis(pushMessage.getPosition());
                break;
            case PushMessage.SETUP_LYRIC:
            case PushMessage.MEDIASTATE_PLAYING:
                log.e("receiverUpdate***.setupLyric");
                File file = pushMessage.getFile();
                setupLyric(file);
                break;
            case PushMessage.UPDATE_LYRIC:
                log.e("receiverUpdate***PushMessage.UPDATE_LYRIC");
                mLyricView.scrollRequestFocus();
                mLyricView.callOnClick();
                mLyricView.startAnimator();
                mLyricView.setCurrentPosition(lineNum);
                break;
            case PushMessage.CLEAR_LYRIC:
                log.e("receiverUpdate***PushMessage.CLEAR_LYRIC");
                clearLyricText();
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        lyricManager.setOnProgressChangedListener(null);
        EventBus.getDefault().unregister(this);
    }

    private boolean setupLyric(File file) {
        log.d("setupLyric***");
        if (file==null||!file.exists()) {
            log.e("setupLyric: lyric File not exists!");
            return false;
        }
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            log.e("setupLyric: load lyric success" + file.getAbsolutePath());
        } catch (IOException e) {
            log.e("setupLyric: error when load lyric");
            e.printStackTrace();
        } finally {
            if(lyricManager!=null){
                lyricManager.setFileStream(inputStream);
            }
        }
        return true;
    }


    private void setLyricText(File file){
        log.d("clearLyricText***");
        boolean isResult = setupLyric(file);
        if (!isResult) {
            String str = "暂无歌词";
            SpannableStringBuilder stringBuilder = new SpannableStringBuilder(str);
            ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.WHITE);
            stringBuilder.setSpan(foregroundColorSpan, 0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mLyricView.setText(stringBuilder);
        }
    }

    private void clearLyricText(){
        log.d("clearLyricText***");
        String str = "暂无歌词";
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder(str);
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.WHITE);
        stringBuilder.setSpan(foregroundColorSpan, 0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mLyricView.setText(stringBuilder);
    }
}
