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

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.azero.sampleapp.R;

/**
 * 网络错误提示状态栏
 */
public class DisconnectStatusBar extends LinearLayout {
    @SuppressLint("StaticFieldLeak")
    private static volatile DisconnectStatusBar INSTANCE;
    private static final int HIDE = 1;

    private View rootView;
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private ErrorType errorType = ErrorType.NONE;

    private volatile boolean isShow = false;
    private volatile boolean cancelHide = false;

    private Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == HIDE && !cancelHide) {
                hide();
            }
        }
    };

    private DisconnectStatusBar(Context context) {
        super(context);
        initChildView();
    }

    public static DisconnectStatusBar getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (GlobalBottomBar.class) {
                if (INSTANCE == null)
                    INSTANCE = new DisconnectStatusBar(context.getApplicationContext());
            }
        }
        return INSTANCE;
    }

    private void initChildView() {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.layout_connection_status_snackbar, this);
        rootView.setBackgroundResource(R.color.server_error);

        windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        // 设置图片格式，效果为背景透明
        layoutParams.format = PixelFormat.RGBA_8888;

        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

//        Point size = new Point();
//        windowManager.getDefaultDisplay().getSize(size);
//        layoutParams.width = size.x;

        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        layoutParams.gravity = Gravity.BOTTOM;
    }

    public void show(ErrorType errorType) {
        show(errorType, 0);
    }

    public void show() {
        show(ErrorType.SERVER_ERROR, 0);
    }

    public void show(ErrorType errorType, long hideDelayMillis) {
        if (this.errorType.ordinal() > errorType.ordinal()) return;
        this.errorType = errorType;
        switch (errorType) {
            case SERVER_ERROR:
                rootView.setBackgroundResource(R.color.server_error);
                break;
            case NET_ERROR:
                rootView.setBackgroundResource(R.color.network_error);
                break;
        }

        cancelHide = true;
        handler.removeCallbacksAndMessages(null);

        if (windowManager == null)
            return;

        if (isShow) {
            startHideAnimator(() -> startShowAnimator());
        } else {
            startShowAnimator();
        }
        if (hideDelayMillis > 0) {
            cancelHide = false;
            handler.sendEmptyMessageDelayed(HIDE, hideDelayMillis);
        }
    }

    public void hide() {
        errorType = ErrorType.NONE;
        cancelHide = true;
        handler.removeCallbacksAndMessages(null);

        if (windowManager == null)
            return;

        if (isShow) {
            startHideAnimator(null);
        }
    }

    public void hide(long hideDelayMillis) {
        handler.removeCallbacksAndMessages(null);
        if (hideDelayMillis >= 0) {
            cancelHide = false;
            handler.sendEmptyMessageDelayed(HIDE, hideDelayMillis);
        }
    }

    private void startShowAnimator() {
        ObjectAnimator showAnimator = ObjectAnimator
                .ofFloat(rootView, "alpha", 0f, 1f)
                .setDuration(500);
        showAnimator.setAutoCancel(true);
        showAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isShow = true;

                if (checkOverlayPermission()) {
                    windowManager.addView(rootView, layoutParams);
                } else {

                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        showAnimator
                .start();
    }

    /**
     * 检查是否有显示先其他app之上权限
     *
     * @return true:有权限； false:无权限
     */
    private boolean checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return Settings.canDrawOverlays(getContext());
        else return true;
    }

    private void startHideAnimator(final HideAnimatorCallback callback) {
        ObjectAnimator hideAnimator = ObjectAnimator
                .ofFloat(rootView, "alpha", 1f, 0f)
                .setDuration(500);
        hideAnimator.setAutoCancel(true);
        hideAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                try {
                    windowManager.removeViewImmediate(rootView);
                } catch (Exception e) {
                }
                if (callback != null)
                    callback.finishHide();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        hideAnimator
                .start();
    }

    interface HideAnimatorCallback {

        void finishHide();
    }

    public enum ErrorType {
        NONE,
        SERVER_ERROR,
        NET_ERROR;
    }

    public ErrorType getErrorType() {
        return errorType;
    }
}
