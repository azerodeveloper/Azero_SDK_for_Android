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

package com.azero.sampleapp.activity.thirdparty.andcall;

import android.view.SurfaceView;


public interface AndCallContract {
    interface View {
        void changeContactState(String str);

        void updateName(String string);

        void hangUp();

        void callOutAudio();

        void callOutVideo();

        void callAlertingAudio();

        void callAlertingVideo();

        void pickUpAudio();

        void pickUpVideo();

        void changeMuteButton(boolean mute);

        void setContactNumber(String number);

        void receiveOpenCameraError();

        void calculateView(int width, int height);

        void loadLocalView(int width, int height);

        void loadRemoteView();

        void setComingEncodeErrorMsg(boolean isComingEncodeErrorMsg);
    }

    interface Presenter {
        void activityStart(View view, String data);

        void unregisterListener();

        void onClickPickUp();

        void onClickDecline();

        void onClickMute();

        void onClickKeyboard(int num);

        SurfaceView getLocalPreviewSurfaceView();

        SurfaceView getRemotePreviewSurfaceView();
    }
}
