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

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Point;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.azero.sampleapp.MyApplication;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 获取屏幕信息类
 */
public class DisplayUtils {

    private static WindowManager wm = null;
    private static DisplayMetrics dm = null;

    /**
     * 获取屏幕宽度
     *
     * @return
     */
    public static int getDisplayWidth(Context context) {
        if (wm == null) {//用于与窗口管理器交互
            wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }

        if (dm == null) {
            dm = new DisplayMetrics();
        }
        //先通过wm获取宽高纬度，然后将其放在displaymetrics类中
        wm.getDefaultDisplay().getMetrics(dm);

        return dm.widthPixels;
    }

    /**
     * 获取屏幕高度
     *
     * @return
     */
    public static int getDisplayHeight(Context context) {
        if (wm == null) {
            wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }

        if (dm == null) {
            dm = new DisplayMetrics();
        }

        wm.getDefaultDisplay().getMetrics(dm);

        return dm.heightPixels;
    }

    public static int getDisplayRealHeight(Activity activity){
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        int height = size.y;
        return height;
    }

    public static int getDisplayRealWidth(Activity activity){
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        int width = size.x;
        return width;
    }

    /**
     * MethodName: 打开软键盘
     * Description: 延迟加载防止界面没有加载完全，无法弹出键盘的情况
     */
    public static void openKeyboard(final EditText editText) {
        Timer timer = new Timer();
        timer.schedule(
                new TimerTask() {
                    public void run() {
                        InputMethodManager inputManager =
                                (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.showSoftInput(editText, 0);
                    }

                },
                200);
    }

    /**
     * MethodName: 关闭软键盘
     */
    public static void closeKeyboard(Context context, EditText editTextField) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editTextField.getWindowToken(), 0);
    }

    /**
     * MethodName:  setText
     * Description: 超过4个字长度就显示...
     */
    public static void setText(TextView view, String text){
        if(TextUtils.isEmpty(text)){
            return;
        }
        if(text.length()>3){
            text=text.substring(0,3).concat("...");
        }

        view.setText(text);
    }


    /**
     * dp转px
     *
     * @param dpValue dpValue
     * @return pxValue
     */
    public static int dp2px(float dpValue) {
        final float scale = MyApplication.getInstance().getApplicationContext()
                .getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}
