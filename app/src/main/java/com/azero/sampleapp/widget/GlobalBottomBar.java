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
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.azero.sampleapp.R;

/**
 * Description:
 * Created by WangXin 19-2-25
 */
public final class GlobalBottomBar extends LinearLayout {

    private static final int HIDE = 1;

    @SuppressLint("StaticFieldLeak")
    private static volatile GlobalBottomBar INSTANCE;
    private View rootView;
    private ImageView mOrnament;
    private TextView mBarText;
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;

    private volatile boolean isShow = false;
    private volatile boolean isPlayingHideAnimation = false;
    private volatile boolean cancelHide = false;

    private Handler handler = new Handler(getContext().getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == HIDE && !cancelHide) {
                hide();
            }
        }
    };

    private GlobalBottomBar(Context context) {
        super(context);
        initChildView();
    }

    public static GlobalBottomBar getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (GlobalBottomBar.class) {
                if (INSTANCE == null) {
                    INSTANCE = new GlobalBottomBar(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    private void initChildView() {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.layout_guide_snackbar, this);
        rootView.setBackgroundResource(R.color.snackbar_background);
        mOrnament = rootView.findViewById(R.id.ornament);
        mBarText = rootView.findViewById(R.id.snackbar_text);

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

    public void append(@NonNull String text) {
        append(text, 0);
    }

    public void append(@NonNull String text, long hideDelayMillis) {
        if (hideDelayMillis > 0) {
            cancelHide = true;
            handler.removeCallbacksAndMessages(null);
        }

        if (isShow && !TextUtils.isEmpty(text)) {
            mBarText.setText(text);
            mBarText.setVisibility(VISIBLE);
            if (hideDelayMillis > 0) {
                cancelHide = false;
                handler.sendEmptyMessageDelayed(HIDE, hideDelayMillis);
            }
        }
    }


    public void show() {
        show(null, 0, true);
    }

    public void show(@NonNull String text) {
        show(text, 0, true);
    }

    public void show(long hideDelayMillis) {
        show(null, hideDelayMillis, true);
    }

    public void show(final String text, long hideDelayMillis, boolean showByWakeup) {
        if (!showByWakeup) {
            if (isShow) {
                return;
            }
        }
        cancelHide = true;
        handler.removeCallbacksAndMessages(null);

        if (windowManager == null) {
            return;
        }

        if (isShow) {
            startHideAnimator(() -> startShowAnimator(text));
        } else {
            startShowAnimator(text);
        }
        if (hideDelayMillis > 0) {
            cancelHide = false;
            handler.sendEmptyMessageDelayed(HIDE, hideDelayMillis);
        }
    }

    public void hide() {
        cancelHide = true;
        handler.removeCallbacksAndMessages(null);

        if (windowManager == null) {
            return;
        }

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

    private void startShowAnimator(final String text) {
        ObjectAnimator showAnimator = ObjectAnimator
                .ofFloat(rootView, "translationY", 200f, 0f)
                .setDuration(400);
        showAnimator.setAutoCancel(true);
        showAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isShow = true;
                if (checkOverlayPermission()) {
                    windowManager.addView(rootView, layoutParams);
                } else {

                }

                if (!TextUtils.isEmpty(text)) {
                    mBarText.setText(text);
                    mBarText.setVisibility(VISIBLE);
                } else {
                    mBarText.setVisibility(GONE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                ObjectAnimator alpha = ObjectAnimator
                        .ofFloat(mOrnament, "alpha", 1f, 0.3f)
                        .setDuration(2000);
                alpha.setRepeatCount(-1);
                alpha.setRepeatMode(ValueAnimator.REVERSE);
                alpha.start();
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(getContext());
        } else {
            return true;
        }
    }

    private void startHideAnimator(final HideAnimatorCallback callback) {
        if (isPlayingHideAnimation) {
            return;
        }
        ObjectAnimator hideAnimator = ObjectAnimator
                .ofFloat(rootView, "translationY", 0f, 200f)
                .setDuration(400);
        hideAnimator.setAutoCancel(true);
        hideAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isPlayingHideAnimation = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                try {
                    windowManager.removeViewImmediate(rootView);
                } catch (Exception e) {
                }
                mBarText.setVisibility(GONE);
                mBarText.setText("");
                isShow = false;
                if (callback != null) {
                    callback.finishHide();
                }
                isPlayingHideAnimation = false;
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

}