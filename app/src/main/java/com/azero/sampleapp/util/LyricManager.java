
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

package com.azero.sampleapp.util;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import com.azero.sampleapp.activity.playerinfo.bean.LineBean;
import com.azero.sampleapp.activity.playerinfo.bean.LyricBean;
import com.azero.sdk.util.log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

public class LyricManager {

    private OnProgressChangedListener progressChangedListener;
    private int selectedColor = Color.parseColor("#1E90FF");
    private int normalColor = Color.parseColor("#FFFFFF");
    private LyricBean lyricBean = null;

    private boolean flag_refresh = false;
    private int flag_position = 0;

    private LyricManager(Context context) {
    }

    public static LyricManager getInstance(Context context) {
        return new LyricManager(context);
    }

    /**
     * 设置歌词流文件
     *
     * @param inputStream 歌词文件的输入流
     */
    public void setFileStream(final InputStream inputStream) {
        if (null != inputStream) {
            new Thread(() -> decode(inputStream)).start();
        } else {
            lyricBean = null;
        }
    }

    /**
     * 设置歌词文件
     *
     * @param file 歌词文件
     */
    public void setLyricFile(File file) {
        try {
            setFileStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据输入流解析转码成歌词
     */
    private void decode(InputStream inputStream) {
        try {
            String line;
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            BufferedReader bufferedReader = getReader(bufferedInputStream);
            lyricBean = new LyricBean();
            while ((line = bufferedReader.readLine()) != null) {
                analyzeLyric(line);
            }
            bufferedReader.close();
            inputStream.close();
            if(lyricBean.getLines().size()>0){
                Collections.sort(lyricBean.getLines());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        setCurrentTimeMillis(0);
    }

    /**
     * 动态识别歌词编码格式
     *  防止出现乱码问题
     */
    private BufferedReader getReader(BufferedInputStream bufferedInputStream)throws IOException{
        String charset = "GBK";
        byte[] first3bytes = new byte[3];
        boolean checked = false;
        try {
           bufferedInputStream.mark(-1);
            int read = bufferedInputStream.read(first3bytes);
            if(read==-1){
                bufferedInputStream.reset();
                return new BufferedReader(new InputStreamReader(bufferedInputStream, charset));
            }
            if (first3bytes[0] == (byte) 0xEF && first3bytes[1] == (byte) 0xBB
                    && first3bytes[2] == (byte) 0xBF) {// utf-8
                log.e("Lyric text type = utf-8");
                checked = true;
                charset = "UTF-8";

            } else if (first3bytes[0] == (byte) 0xFF
                    && first3bytes[1] == (byte) 0xFE) {
                log.e("Lyric text type = utf-unicode");
                charset = "unicode";
                checked = true;
            } else if (first3bytes[0] == (byte) 0xFE
                    && first3bytes[1] == (byte) 0xFF) {
                log.e("Lyric text type = utf-16be");
                charset = "UTF-16BE";
                checked = true;
            } else if (first3bytes[0] == (byte) 0xFF
                    && first3bytes[1] == (byte) 0xFF) {
                charset = "UTF-16LE";
                checked = true;
            }
            bufferedInputStream.reset();
            if(!checked){
                while((read = bufferedInputStream.read())!=-1){
                    if(read>= 0xF0){
                        break;
                    }
                    if (0x80 <= read && read <= 0xBF)
                        break;
                    if (0xC0 <= read && read <= 0xDF) {
                        read = bufferedInputStream.read();
                        if (0x80 <= read && read <= 0xBF)
                            continue;
                        else
                            break;
                    }else if (0xE0 <= read) {
                        read = bufferedInputStream.read();
                        if (0x80 <= read && read <= 0xBF) {
                            read = bufferedInputStream.read();
                            if (0x80 <= read && read <= 0xBF) {
                                charset = "UTF-8";
                                break;
                            } else
                                break;
                        } else
                            break;
                    }
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }
        log.e("lyric charset ***********"+charset);
        bufferedInputStream.reset();
        return new BufferedReader(new InputStreamReader(bufferedInputStream, charset));
    }
    /**
     * 逐行解析歌词文件
     */
    private void analyzeLyric(String line) {
        log.e("Lyric line"+line);
        try {
            if(line==null){
                return;
            }
            LineBean lineBean = new LineBean();
            //[04:05.02][03:43.28][01:46.88]在春天里摇摆
            String[] timestampStr = line.split("]");//判断时间戳的个数
            int index = line.lastIndexOf("]");
            if(index==-1){//兼容没有时间戳的歌词
                lineBean.setContent(line);
                lineBean.setStart(1000);
                lyricBean.getLines().add(lineBean);
                return;
            }
            if (line.startsWith("[offset:")) {
                // 时间偏移量
                String string = line.substring(8, index).trim();
                lyricBean.setOffset(Long.parseLong(string));
                return;
            }
            if (line.startsWith("[ti:")) {
                // title 标题
                String string = line.substring(4, index).trim();
                lyricBean.setTitle(string);
                return;
            }
            if (line.startsWith("[ar:")) {
                // artist 作者
                String string = line.substring(4, index).trim();
                lyricBean.setArtist(string);
                return;
            }
            if (line.startsWith("[al:")) {
                // album 所属专辑
                String string = line.substring(4, index).trim();
                lyricBean.setAlbum(string);
                return;
            }
            if (line.startsWith("[by:")) {
                return;
            }

            if(timestampStr.length>1){
                for(int i=0;i<timestampStr.length-1;i++){
                    LineBean lineBeans = new LineBean();
                    String temp = timestampStr[i];
                    String[] time = temp.split("\\[");
                    lineBeans.setStart(analyzeStartTimeMillis(time[1]));
                    if((index+1)<=line.length()){
                        lineBeans.setContent(line.substring(index+1));
                    }
                    lyricBean.getLines().add(lineBeans);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 从字符串中获得时间值
     */
    private long analyzeStartTimeMillis(String str) {
        try {
            String[] s1 = str.split(":");
            long minute = Long.parseLong(s1[0]);
            String[] s2 = s1[1].split("\\.");
            long second = Long.parseLong(s2[0]);
            long millisecond = 0;
            if(s2.length>1){
                millisecond = Long.parseLong(s2[1]);
            }
            return millisecond + second * 1000 + minute * 60 * 1000;
        }catch (Exception e){
            return -1;
        }
    }

    /**
     * 根据当前时间戳值获得歌词
     *
     * @param timeMillis long值时间戳
     */
    public void setCurrentTimeMillis(final long timeMillis) {
        try {
            final List<LineBean> lines = lyricBean != null ? lyricBean.getLines() : null;
            if (lines != null) {
                new Thread(() -> {
                    try {
                        int position = 0;
                        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
                        for (int i = 0, size = lines.size(); i < size; i++) {
                            if (lines.get(i).getStart() < timeMillis) {
                                position = i;
                            } else {
                                break;
                            }
                        }
                        if (position == flag_position && !flag_refresh) {
                            return;
                        }
                        flag_position = position;
                        for (int i = 0, size = lines.size(); i < size; i++) {
                            if (i != position) {
                                ForegroundColorSpan span = new ForegroundColorSpan(normalColor);
                                String line = lines.get(i).getContent();
                                SpannableString spannableString = new SpannableString(line + "\n");
                                spannableString.setSpan(span, 0, spannableString.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                                stringBuilder.append(spannableString);
                            } else {
                                ForegroundColorSpan span = new ForegroundColorSpan(selectedColor);
                                String line = lines.get(i).getContent();
                                SpannableString spannableString = new SpannableString(line + "\n");
                                spannableString.setSpan(span, 0, spannableString.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                                stringBuilder.append(spannableString);
                            }
                        }
                        Message message = new Message();
                        message.what = 0x159;
                        DataHolder dataHolder = new DataHolder();
                        dataHolder.builder = stringBuilder;
                        dataHolder.position = position;
                        dataHolder.refresh = flag_refresh;
                        dataHolder.lines = lines;
                        message.obj = dataHolder;
                        handler.sendMessage(message);

                        if (flag_refresh) {
                            flag_refresh = false;
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }).start();
            } else {
                Message message = new Message();
                message.what = 0x159;
                DataHolder dataHolder = new DataHolder();
                dataHolder.builder = null;
                dataHolder.position = -1;
                message.obj = dataHolder;
                handler.sendMessage(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Object object = msg.obj;
            if (object != null && object instanceof DataHolder) {
                DataHolder dataHolder = (DataHolder) object;
                switch (msg.what) {
                    case 0x159:
                        if (null != progressChangedListener) {
                            progressChangedListener.onProgressChanged(dataHolder.builder, dataHolder.position, dataHolder.refresh);
                            if (dataHolder.lines != null&&dataHolder.position<dataHolder.lines.size()) {
                                progressChangedListener.onProgressChanged(dataHolder.lines.get(dataHolder.position).getContent(), dataHolder.refresh);
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    };

    /**
     * 数据缓存类
     */
    class DataHolder {
        SpannableStringBuilder builder;
        List<LineBean> lines;
        boolean refresh;
        int position;
    }

    /**
     * 注册监听事件
     */
    public void setOnProgressChangedListener(OnProgressChangedListener progressChangedListener) {
        this.progressChangedListener = progressChangedListener;
    }

    /**
     *
     * */
    public interface OnProgressChangedListener {
        void onProgressChanged(String singleLine, boolean refresh);

        void onProgressChanged(SpannableStringBuilder stringBuilder, int lineNumber, boolean refresh);
    }

    /**
     * 设置选中文本颜色
     */
    public void setSelectedTextColor(int color) {
        if (color != selectedColor) {
            selectedColor = color;
            flag_refresh = true;
        }
    }

    /**
     * 设置正常文本颜色
     */
    public void setNormalTextColor(int color) {
        if (color != normalColor) {
            normalColor = color;
        }
    }
}
