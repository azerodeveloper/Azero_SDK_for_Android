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

package com.azero.sampleapp.activity.template;

import android.content.Intent;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.azero.platforms.iface.MediaPlayer;
import com.azero.platforms.iface.MediaPlayer.MediaState;
import com.azero.sampleapp.R;
import com.azero.sampleapp.widget.HighlightTextView;
import com.azero.sdk.AzeroManager;
import com.azero.sdk.impl.MediaPlayer.RawSpeakAudioMediaPlayerHandler;
import com.azero.sdk.util.Constant;
import com.azero.sdk.util.log;

import org.json.JSONException;
import org.json.JSONObject;

public class BodyTemplate1Activity extends BaseDisplayCardActivity implements MediaPlayer.OnMediaStateChangeListener {
    private TextView mMainTitle;
    private TextView mSubTitle;
    private ScrollView mScrollView;
    private HighlightTextView mTextField;
    private ConstraintLayout background;

    @Override
    protected int getLayoutResId() {
        return R.layout.card_body_template1;
    }

    @Override
    protected void initView() {
        mMainTitle = findViewById(R.id.mainTitle);
        mSubTitle = findViewById(R.id.subTitle);
        mTextField = findViewById(R.id.textField);
        background = findViewById(R.id.BodyTemplate1);
        mScrollView = findViewById(R.id.scrollView);
    }

    @Override
    protected void initData(Intent intent) {
        try {
            JSONObject template = new JSONObject(intent.getStringExtra(Constant.EXTRA_TEMPLATE));
            ConfigureTemplateView.configureBodyTemplate1(this, template);
        } catch (JSONException e) {
            log.e(e.getMessage());
            finish();
        } catch (Exception e) {
        }

        RawSpeakAudioMediaPlayerHandler speaker = (RawSpeakAudioMediaPlayerHandler)
                AzeroManager.getInstance().getHandler(AzeroManager.SPEAKER_HANDLER);
        speaker.addOnMediaStateChangeListener(this);
        mTextField.setOnHighlightChangeListener((view, line, offset) -> {
            if (offset > (mScrollView.getHeight() / 2 - 80)) {
                if (mScrollView.getScrollY() != view.getHeight()) {
                    log.d("getHeight" + mScrollView.getHeight());
                    mScrollView.smoothScrollTo(0, offset - (mScrollView.getHeight() / 2 - 80));
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        RawSpeakAudioMediaPlayerHandler speaker = (RawSpeakAudioMediaPlayerHandler)
                AzeroManager.getInstance().getHandler(AzeroManager.SPEAKER_HANDLER);
        speaker.removeOnMediaStateChangeListener(this);
    }

    public TextView getMainTitle() {
        return mMainTitle;
    }

    public TextView getSubTitle() {
        return mSubTitle;
    }

    public HighlightTextView getTextField() {
        return mTextField;
    }

    public ConstraintLayout getBackground() {
        return background;
    }

    @Override
    public void onMediaError(String name, String s1, MediaPlayer.MediaError mediaError) {

    }

    @Override
    public void onMediaStateChange(String name, MediaState mediaState) {
        switch (mediaState) {
            case STOPPED:
                mTextField.stopAnimation();
                break;
            case PLAYING:
                break;
            case BUFFERING:
                break;
        }
    }

    @Override
    public void onPositionChange(String name, long position, long duration) {
        log.d("position" + position);
        mTextField.updatePosition(position);
    }
}
