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

package com.azero.sampleapp.activity.launcher;

import com.azero.platforms.iface.AlexaClient;
import com.azero.sdk.AzeroManager;
import com.azero.sdk.impl.AzeroClient.AzeroClientHandler;
import com.azero.sdk.util.log;

public class LauncherDataManager implements AzeroClientHandler.ConnectionStatusListener {
    private final static int DEFAULT_REQUEST_COUNT = 5;

    private boolean eventUponConnect = true;

    public LauncherDataManager() {
        AzeroClientHandler azeroClientHandler = (AzeroClientHandler) AzeroManager.getInstance()
                .getHandler(AzeroManager.AZERO_CLIENT_HANDLER);
        azeroClientHandler.addConnectionStatusListener(this);
    }

    public void acquireLauncher() {
        acquireLauncher(DEFAULT_REQUEST_COUNT);
    }

    public void acquireLauncher(int count) {
        log.d("acquire launcher");
        AzeroManager.getInstance().acquireLauncherList(AzeroManager.LAUNCHER_ACQUIRE, null, count);
    }

    public void updateLauncher(String contentId) {
        updateLauncher(contentId, DEFAULT_REQUEST_COUNT);
    }

    public void updateLauncher(String contentId, int count) {
        log.d("update launcher");
        AzeroManager.getInstance().acquireLauncherList(AzeroManager.LAUNCHER_UPDATE, contentId, count);
    }

    @Override
    public void connectionStatusChanged(AlexaClient.ConnectionStatus status, AlexaClient.ConnectionChangedReason reason) {
        if (status != AlexaClient.ConnectionStatus.CONNECTED || !eventUponConnect) {
            return;
        }
        eventUponConnect = false;
        log.d("sendEvent");
        acquireLauncher();
    }
}