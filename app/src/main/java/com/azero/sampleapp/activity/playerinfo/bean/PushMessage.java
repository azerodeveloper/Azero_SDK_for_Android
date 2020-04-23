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
package com.azero.sampleapp.activity.playerinfo.bean;

import java.io.File;

public class PushMessage {
    public static final int UPDATE_TEMPLATE = 1001;//更新模板
    public static final int UPDATE_PROGRESS = 1002;//更新进度
    public static final int UPDATE_PLAYERINFO = 1003;//更新信息
    public static final int SETUP_LYRIC = 1004;//设置歌词
    public static final int UPDATE_LYRIC = 1005;//更新歌词
    public static final int CLEAR_LYRIC = 1006;//清空歌词
    public static final int MEDIASTATE_PLAYING = 1007;//播放状态
    public int type;
    private String template;
    private File file;
    private String payload;
    private long position = -1;//进度

    public PushMessage(int type){
        this.type = type;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
