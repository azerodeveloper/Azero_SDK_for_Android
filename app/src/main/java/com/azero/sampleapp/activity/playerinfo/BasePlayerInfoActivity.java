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

package com.azero.sampleapp.activity.playerinfo;

import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.azero.sampleapp.R;
import com.azero.sampleapp.activity.template.BaseTemplateActivity;

public abstract class BasePlayerInfoActivity extends BaseTemplateActivity {
    protected ImageButton mControlPrev, mControlNext;
    protected ToggleButton mControlPlayPause;
    protected TextView mProgressTime, mEndTime;
    protected SeekBar mProgress;

    @Override
    protected void initView() {
        mControlPrev = findViewById( R.id.prevControlButton );
        mControlPlayPause = findViewById( R.id.playControlButton );
        mControlNext = findViewById( R.id.nextControlButton );
        
        mProgress = findViewById( R.id.mediaProgressBar );
        mProgressTime = findViewById( R.id.mediaProgressTime );
        mEndTime = findViewById( R.id.mediaEndTime );
    }
}
