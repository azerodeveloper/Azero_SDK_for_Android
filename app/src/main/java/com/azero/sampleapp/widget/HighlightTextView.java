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

package com.azero.sampleapp.widget;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.UiThread;
import androidx.appcompat.widget.AppCompatTextView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 高亮显示语音播报文字控件
 * TTS引擎 SoundAI
 * 播放器  AudioTrack
 */
public class HighlightTextView extends AppCompatTextView {
    //当前高亮位置（height)
    private float animationCoverTop = 0f;
    //每行文字的时间戳
    private List<Long> timestampArray = new ArrayList<>();
    //高亮行数变化时，反馈控件、行数、行高信息
    private OnHighlightChangeListener onHighlightChangeListener;
    //动画是否停止
    private boolean animationStopped = false;

    public HighlightTextView(Context context) {
        super(context);
    }

    public HighlightTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HighlightTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 初始化动画，计算每行文字的时间戳
     */
    @UiThread
    public void startAnimation() {
        animationStopped = false;
        timestampArray.clear();
        long durationCount = 0L;
        //第一行的时间从零开始
        timestampArray.add(0L);
        for (int i = 0; i < getLineCount(); i++) {
            int lineStart = getLayout().getLineStart(i);
            int lineEnd = getLayout().getLineEnd(i);
            if (lineStart < getText().length() && lineEnd <= getText().length()) {
                String lineText = getText().toString().substring(lineStart, lineEnd);
                durationCount += textToDuration(lineText);
                timestampArray.add(durationCount);
            } else {
                timestampArray.add(0L);
            }
        }
        updatePosition(0L);
    }

    /**
     * 停止动画，再接收到{@link #updatePosition(Long)}信息时不再更新界面
     */
    public void stopAnimation() {
        animationCoverTop = 0f;
        animationStopped = true;
    }

    /**
     * 接收播放进度信息，高亮当前播放的行数
     * @param position 播放新都（AudioTrack）
     */
    public void updatePosition(Long position) {
        if (animationStopped) return;
        for (int index = 0; index < timestampArray.size(); index++) {
            //遍历时间戳，找到首个超过播放进度的时间戳
            Long timestamp = timestampArray.get(index);
            if (position <= timestamp) {
                int target = Math.max(0, index - 1);
                if (getLineHeight() * target != animationCoverTop || target == 0) {
                    //获取需要高亮文字的位置
                    int lineStart = getLayout().getLineStart(target);
                    int lineEnd = getLayout().getLineEnd(target);
                    //高亮显示
                    SpannableStringBuilder builder = new SpannableStringBuilder(getText().toString());
                    builder.setSpan(new ForegroundColorSpan(Color.WHITE), lineStart, lineEnd,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    setText(builder);

                    animationCoverTop = getLineHeight() * target;
                    if (onHighlightChangeListener != null) {
                        //回调当前高亮的行数和位置（height）信息
                        onHighlightChangeListener.onHighlightChange(this, target, (int) animationCoverTop);
                    }
                }
                return;
            }
        }
    }

    /**
     * 计算文字显示间隔（中文字符、标点、英文）
     *
     * @param text
     * @return
     */
    private Long textToDuration(String text) {
        int chineseCount = getChineseCount(text);
        int spCharacterCount = getSPCharCount(text);
        int wordsCount = getWordsCount(text);
        return chineseCount * 3600L + spCharacterCount * 3800L + wordsCount * 3800L;
    }

    public interface OnHighlightChangeListener {
        void onHighlightChange(HighlightTextView view, int line, int offset);
    }

    /**
     * 获取特殊字符和数字个数
     * ，|、|？|！|。|：|；|~|…|0-9
     *
     * @param text
     * @return
     */
    private int getSPCharCount(String text) {
        int count = 0;//汉字数量
        String regEx = "[\uff0c|\u3001|\uff1f|\uff01|\u3002|\uff1a|\uff1b|\u007e|\u2026|\u300a|\u300b|0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(text);
        int len = m.groupCount();
        while (m.find()) {
            for (int i = 0; i <= len; i++) {
                count = count + 1;
            }
        }
        return count;
    }

    /**
     * 获取单词个数
     *
     * @param text
     * @return
     */
    private int getWordsCount(String text) {
        int count = 0;//汉字数量
        String regEx = "\\b[a-zA-Z]+\\b";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(text);
        int len = m.groupCount();
        while (m.find()) {
            for (int i = 0; i <= len; i++) {
                count = count + 1;
            }
        }
        return count;
    }

    /**
     * 获取中文字符个数
     *
     * @param text
     * @return
     */
    private int getChineseCount(String text) {
        int count = 0;//汉字数量
        String regEx = "[\\u4e00-\\u9fa5]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(text);
        int len = m.groupCount();
        while (m.find()) {
            for (int i = 0; i <= len; i++) {
                count = count + 1;
            }
        }
        return count;
    }

    public void setOnHighlightChangeListener(OnHighlightChangeListener onHighlightChangeListener) {
        this.onHighlightChangeListener = onHighlightChangeListener;
    }
}
