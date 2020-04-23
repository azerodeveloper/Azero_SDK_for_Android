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

import com.azero.sampleapp.R;
import com.azero.sampleapp.activity.playerinfo.bean.PushMessage;
import com.azero.sampleapp.widget.LyricView;
import com.azero.sdk.util.Constant;
import com.azero.sdk.util.log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class PlayerInfoLyricFragment extends BasePlayerInfoFragment {
    private LyricView mLyricView;

    public PlayerInfoLyricFragment() {
    }

    public static PlayerInfoLyricFragment newInstance(String template) {
        PlayerInfoLyricFragment fragment = new PlayerInfoLyricFragment();
        Bundle args = new Bundle();
        args.putString(Constant.EXTRA_TEMPLATE, template);
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
        mLyricView = rootView.findViewById(R.id.lyricView);
        try {
            Bundle bundle = getArguments();
            JSONObject template = new JSONObject(bundle.getString(Constant.EXTRA_TEMPLATE));
            configurePlayerInfo(template);
        } catch (JSONException e) {
            log.e(e.getMessage());
            getActivity().finish();
        }
    }

    public void configurePlayerInfo(JSONObject template) {
        try {
            if (template.has("provider")) {
                // Set header subtext to provider name if no header subtext given
                JSONObject provider = template.getJSONObject("provider");
            }
        } catch (JSONException e) {
            log.e(e.getMessage());
        }
    }

    @Override
    protected void lazyLoad() {
        log.e("lazyLoad***");
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void receiverUpdate(PushMessage pushMessage) {
        switch (pushMessage.type) {
            case PushMessage.UPDATE_PROGRESS:
                mLyricView.setCurrentTimeMillis(pushMessage.getPosition());
                break;
            case PushMessage.SETUP_LYRIC:
            case PushMessage.MEDIASTATE_PLAYING:
                File file = pushMessage.getFile();
                setupLyric(file);
                break;
            case PushMessage.UPDATE_LYRIC:
                break;
            case PushMessage.CLEAR_LYRIC:
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void setupLyric(File file) {
        if (mLyricView != null) {
            mLyricView.setLyricFile(file);
        }
    }
}
