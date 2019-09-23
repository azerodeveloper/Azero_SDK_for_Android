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

package com.azero.sampleapp.impl.phonecallcontroller;

import com.azero.sdk.AzeroManager;
import com.azero.sdk.impl.PhoneCallController.AbstractPhoneCallDispatcher;
import com.azero.sdk.util.log;

/**
 * @Author: sjy
 * @Date: 2019/9/3
 */
public class PhoneCallControllerHandler {

    private com.azero.sdk.impl.PhoneCallController.PhoneCallControllerHandler mPhoneCallControllerHandler;

    public PhoneCallControllerHandler() {
        if (mPhoneCallControllerHandler == null) {
            mPhoneCallControllerHandler = (com.azero.sdk.impl.PhoneCallController.PhoneCallControllerHandler) AzeroManager.getInstance().getHandler(AzeroManager.PHONECALL_HANDLER);
        }
        register();
    }

    private void register() {
        mPhoneCallControllerHandler.registerPhoneCallControllerDispatchedListener(new AbstractPhoneCallDispatcher() {
            @Override
            public boolean onDial(String payload) {
                log.d(payload);
                return super.onDial(payload);
            }

            @Override
            public boolean onReDial(String payload) {
                log.d(payload);
                return super.onReDial(payload);
            }

            @Override
            public void onAnswer(String payload) {
                log.d(payload);
                super.onAnswer(payload);
            }

            @Override
            public void onStop(String payload) {
                log.d(payload);
                super.onStop(payload);
            }

            @Override
            public void onSendDTMF(String payload) {
                log.d(payload);
                super.onSendDTMF(payload);
            }
        });
    }

}

