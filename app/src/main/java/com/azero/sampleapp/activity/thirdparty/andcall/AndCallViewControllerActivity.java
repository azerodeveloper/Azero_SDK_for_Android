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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.azero.sampleapp.R;
import com.azero.sdk.util.Constant;
import com.azero.sdk.util.DisplayUtils;
import com.azero.sdk.util.log;

public class AndCallViewControllerActivity extends AppCompatActivity implements AndCallContract.View {

    private RelativeLayout mLocalView;
    private RelativeLayout mRemoteView;

    private TextView mContactName;
    private TextView mContactState;
    private TextView mHideKeyboard;
    private TextView mContactNumber;

    private View[] mPhoneButtonViews = new View[5];
    private View[] mPhoneSmallButtonViews = new View[3];
    private View[] mPhoneNumberButton = new View[12];

    private ConstraintLayout mPhoneCallControlView;
    private ConstraintLayout mPhoneCallKeyboardView;

    private AndCallContract.Presenter mPresenter;

    private View svRemote;  //远端view

    private RelativeLayout.LayoutParams mLayoutParams = null;
    private TextView cameraErrorText;
    private Context mContext;

    /**
     * 视频通话使用到的部分参数
     *
     * @param remoteWidth 正常远端宽
     * @param remoteHeight 正常远端高
     * @param rotateRemoteWidth 旋转宽
     * @param rotateRemoteHeight 旋转高
     */

    private int screenWidth, screenHeight;
    private int remoteWidth = 0;
    private int remoteHeight = 0;
    private int rotateRemoteWidth = 0;
    private int rotateRemoteHeight = 0;
    private int mLocalWidth = 0;
    private int mLocalHeight = 0;

    /**
     * openGL创建失败的标志，用于判断是否重新添加本地视频
     */
    private boolean isComingEncodeErrorMsg = false;

    private boolean muteFlag = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(getLayoutResId());
        initView();

        mPresenter = new AndCallViewControllerPresenter();
        Intent intent = getIntent();
        String data = intent.getStringExtra(Constant.EXTRA_TEMPLATE);
        screenWidth = DisplayUtils.getDisplayRealWidth(this);
        screenHeight = DisplayUtils.getDisplayRealHeight(this);
        initGui();
        setOnTouchListener();
        setOnClickListener();

        mPhoneButtonViews[0].setVisibility(View.GONE);
        mPhoneButtonViews[1].setVisibility(View.GONE);
        mPhoneButtonViews[2].setVisibility(View.GONE);
        mPhoneButtonViews[3].setVisibility(View.GONE);
        mPresenter.activityStart(this, data);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    int getLayoutResId() {
        return R.layout.card_phone_call_template;
    }

    void initView() {
        mPhoneButtonViews[0] = findViewById(R.id.phone_call_button_cast0);
        mPhoneButtonViews[1] = findViewById(R.id.phone_call_button_cast1);
        mPhoneButtonViews[2] = findViewById(R.id.phone_call_button_cast2);
        mPhoneButtonViews[3] = findViewById(R.id.phone_call_button_cast3);
        mPhoneButtonViews[4] = findViewById(R.id.hang_up);

        mPhoneSmallButtonViews[0] = findViewById(R.id.phone_call_small_button_cast0);
        mPhoneSmallButtonViews[1] = findViewById(R.id.phone_call_small_button_cast1);
        mPhoneSmallButtonViews[2] = findViewById(R.id.phone_call_small_button_cast2);

        mPhoneNumberButton[0] = findViewById(R.id.num_one);
        mPhoneNumberButton[1] = findViewById(R.id.num_two);
        mPhoneNumberButton[2] = findViewById(R.id.num_three);
        mPhoneNumberButton[3] = findViewById(R.id.num_four);
        mPhoneNumberButton[4] = findViewById(R.id.num_five);
        mPhoneNumberButton[5] = findViewById(R.id.num_six);
        mPhoneNumberButton[6] = findViewById(R.id.num_seven);
        mPhoneNumberButton[7] = findViewById(R.id.num_eight);
        mPhoneNumberButton[8] = findViewById(R.id.num_nine);
        mPhoneNumberButton[9] = findViewById(R.id.num_zero);
        mPhoneNumberButton[10] = findViewById(R.id.num_star);
        mPhoneNumberButton[11] = findViewById(R.id.num_pound);

        mPhoneCallControlView = findViewById(R.id.phone_call_control_view);
        mPhoneCallKeyboardView = findViewById(R.id.phone_call_keyboard_view);

        mHideKeyboard = findViewById(R.id.hideKeyBoard);
        mLocalView = findViewById(R.id.phone_call_view);
        mRemoteView = findViewById(R.id.rl_camera_remote);
        mContactName = findViewById(R.id.phone_call_contact_name);
        mContactState = findViewById(R.id.phone_call_contact_state);
        mContactNumber = findViewById(R.id.phone_call_contact_number);

        log.d("start Activity");
    }

    private void initGui() {
        ((TextView) mPhoneButtonViews[0].findViewById(R.id.phone_call_button_text)).setText("视频通话");
        ((ImageButton) mPhoneButtonViews[0].findViewById(R.id.phone_call_button_image)).setImageResource(R.drawable.soundai_video_button_n);

        ((TextView) mPhoneButtonViews[1].findViewById(R.id.phone_call_button_text)).setText("转到普通电话");
        ((ImageButton) mPhoneButtonViews[1].findViewById(R.id.phone_call_button_image)).setImageResource(R.drawable.soundai_audio_button_n);

        ((TextView) mPhoneButtonViews[2].findViewById(R.id.phone_call_button_text)).setText("接通电话");
        ((ImageButton) mPhoneButtonViews[2].findViewById(R.id.phone_call_button_image)).setImageResource(R.drawable.soundai_pick_up_n);

        ((TextView) mPhoneButtonViews[3].findViewById(R.id.phone_call_button_text)).setText("挂断电话");
        ((ImageButton) mPhoneButtonViews[3].findViewById(R.id.phone_call_button_image)).setImageResource(R.drawable.soundai_cancle_button_n);

        ((TextView) mPhoneSmallButtonViews[0].findViewById(R.id.phone_call_button_text)).setText("禁麦");
        ((ImageButton) mPhoneSmallButtonViews[0].findViewById(R.id.phone_call_button_image)).setImageResource(R.drawable.soundai_mute_icon_n);

        ((TextView) mPhoneSmallButtonViews[1].findViewById(R.id.phone_call_button_text)).setText("拨号键盘");
        ((ImageButton) mPhoneSmallButtonViews[1].findViewById(R.id.phone_call_button_image)).setImageResource(R.drawable.soundai_dialpad_icon_n);

        ((TextView) mPhoneButtonViews[4].findViewById(R.id.phone_call_button_text)).setText("挂断");
        ((ImageButton) mPhoneButtonViews[4].findViewById(R.id.phone_call_button_image)).setImageResource(R.drawable.soundai_cancle_button_n);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mLocalView.setBackground(getResources().getDrawable(R.drawable.soundai_phone_call_view, null));
        }

        ((TextView) mPhoneNumberButton[0].findViewById(R.id.phone_call_button)).setText("1");
        ((TextView) mPhoneNumberButton[1].findViewById(R.id.phone_call_button)).setText("2");
        ((TextView) mPhoneNumberButton[2].findViewById(R.id.phone_call_button)).setText("3");
        ((TextView) mPhoneNumberButton[3].findViewById(R.id.phone_call_button)).setText("4");
        ((TextView) mPhoneNumberButton[4].findViewById(R.id.phone_call_button)).setText("5");
        ((TextView) mPhoneNumberButton[5].findViewById(R.id.phone_call_button)).setText("6");
        ((TextView) mPhoneNumberButton[6].findViewById(R.id.phone_call_button)).setText("7");
        ((TextView) mPhoneNumberButton[7].findViewById(R.id.phone_call_button)).setText("8");
        ((TextView) mPhoneNumberButton[8].findViewById(R.id.phone_call_button)).setText("9");
        ((TextView) mPhoneNumberButton[9].findViewById(R.id.phone_call_button)).setText("0");
        ((TextView) mPhoneNumberButton[10].findViewById(R.id.phone_call_button)).setText("*");
        ((TextView) mPhoneNumberButton[11].findViewById(R.id.phone_call_button)).setText("#");
    }

    private void setOnClickListener() {
        mPhoneButtonViews[0].findViewById(R.id.phone_call_button_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mPhoneButtonViews[1].findViewById(R.id.phone_call_button_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mPhoneButtonViews[2].findViewById(R.id.phone_call_button_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.onClickPickUp();
            }
        });

        mPhoneButtonViews[3].findViewById(R.id.phone_call_button_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.onClickDecline();
            }
        });

        mPhoneButtonViews[4].findViewById(R.id.phone_call_button_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.onClickDecline();
            }
        });

        mPhoneSmallButtonViews[0].findViewById(R.id.phone_call_button_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.onClickMute();
            }
        });

        mPhoneSmallButtonViews[1].findViewById(R.id.phone_call_button_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPhoneCallControlView.setVisibility(View.GONE);
                mPhoneCallKeyboardView.setVisibility(View.VISIBLE);
            }
        });

        mHideKeyboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPhoneCallControlView.setVisibility(View.VISIBLE);
                mPhoneCallKeyboardView.setVisibility(View.GONE);
            }
        });

        mPhoneNumberButton[0].findViewById(R.id.phone_call_button).setOnClickListener(v -> mPresenter.onClickKeyboard(KeyEvent.KEYCODE_1));
        mPhoneNumberButton[1].findViewById(R.id.phone_call_button).setOnClickListener(v -> mPresenter.onClickKeyboard(KeyEvent.KEYCODE_2));
        mPhoneNumberButton[2].findViewById(R.id.phone_call_button).setOnClickListener(v -> mPresenter.onClickKeyboard(KeyEvent.KEYCODE_3));
        mPhoneNumberButton[3].findViewById(R.id.phone_call_button).setOnClickListener(v -> mPresenter.onClickKeyboard(KeyEvent.KEYCODE_4));
        mPhoneNumberButton[4].findViewById(R.id.phone_call_button).setOnClickListener(v -> mPresenter.onClickKeyboard(KeyEvent.KEYCODE_5));
        mPhoneNumberButton[5].findViewById(R.id.phone_call_button).setOnClickListener(v -> mPresenter.onClickKeyboard(KeyEvent.KEYCODE_6));
        mPhoneNumberButton[6].findViewById(R.id.phone_call_button).setOnClickListener(v -> mPresenter.onClickKeyboard(KeyEvent.KEYCODE_7));
        mPhoneNumberButton[7].findViewById(R.id.phone_call_button).setOnClickListener(v -> mPresenter.onClickKeyboard(KeyEvent.KEYCODE_8));
        mPhoneNumberButton[8].findViewById(R.id.phone_call_button).setOnClickListener(v -> mPresenter.onClickKeyboard(KeyEvent.KEYCODE_9));
        mPhoneNumberButton[9].findViewById(R.id.phone_call_button).setOnClickListener(v -> mPresenter.onClickKeyboard(KeyEvent.KEYCODE_0));
        mPhoneNumberButton[10].findViewById(R.id.phone_call_button).setOnClickListener(v -> mPresenter.onClickKeyboard(KeyEvent.KEYCODE_POUND));
        mPhoneNumberButton[11].findViewById(R.id.phone_call_button).setOnClickListener(v -> mPresenter.onClickKeyboard(KeyEvent.KEYCODE_STAR));
    }

    private void setOnTouchListener() {
        log.d("AndCallControllerView setOnTouchListener");
        mPhoneButtonViews[0].findViewById(R.id.phone_call_button_image).setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    ((ImageButton) mPhoneButtonViews[0].findViewById(R.id.phone_call_button_image)).setImageResource(R.drawable.soundai_video_button_h);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    ((ImageButton) mPhoneButtonViews[0].findViewById(R.id.phone_call_button_image)).setImageResource(R.drawable.soundai_video_button_n);
                }
                return false;
            }
        });

        mPhoneButtonViews[1].findViewById(R.id.phone_call_button_image).setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    ((ImageButton) mPhoneButtonViews[1].findViewById(R.id.phone_call_button_image)).setImageResource(R.drawable.soundai_audio_button_h);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    ((ImageButton) mPhoneButtonViews[1].findViewById(R.id.phone_call_button_image)).setImageResource(R.drawable.soundai_audio_button_n);
                }
                return false;
            }
        });

        mPhoneButtonViews[2].findViewById(R.id.phone_call_button_image).setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    ((ImageButton) mPhoneButtonViews[2].findViewById(R.id.phone_call_button_image)).setImageResource(R.drawable.soundai_pick_up_h);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    ((ImageButton) mPhoneButtonViews[2].findViewById(R.id.phone_call_button_image)).setImageResource(R.drawable.soundai_pick_up_n);
                }
                return false;
            }
        });

        mPhoneButtonViews[3].findViewById(R.id.phone_call_button_image).setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    ((ImageButton) mPhoneButtonViews[3].findViewById(R.id.phone_call_button_image)).setImageResource(R.drawable.soundai_cancle_button_h);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    ((ImageButton) mPhoneButtonViews[3].findViewById(R.id.phone_call_button_image)).setImageResource(R.drawable.soundai_cancle_button_n);
                }
                return false;
            }
        });

        mPhoneButtonViews[4].findViewById(R.id.phone_call_button_image).setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    ((ImageButton) mPhoneButtonViews[4].findViewById(R.id.phone_call_button_image)).setImageResource(R.drawable.soundai_cancle_button_h);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    ((ImageButton) mPhoneButtonViews[4].findViewById(R.id.phone_call_button_image)).setImageResource(R.drawable.soundai_cancle_button_n);
                }
                return false;
            }
        });
    }

    private void showLocalLayout(int localWidth, int localHeight) {
        SurfaceView surfaceView = mPresenter.getLocalPreviewSurfaceView();
        if (surfaceView != null) {
            if (localWidth == 0 || localHeight == 0) {
                localWidth = (int) (0.35 * DisplayUtils.getDisplayRealWidth(this));
                localHeight = (int) (0.35 * DisplayUtils.getDisplayRealHeight(this));
            }
            ViewGroup svLocalParent = (ViewGroup) surfaceView.getParent();

            mLocalView.removeAllViews();
            if (svLocalParent != null) {
                svLocalParent.removeAllViews();
            }

            mLayoutParams = new RelativeLayout.LayoutParams(localWidth, localHeight);
            mLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

            // TODO: 2019/7/31 if (callState == VoIPConstant.CALL_STATE_ANSWERED)
            //  mLayoutParams.setMargins(0, DisplayUtils.getDisplayRealHeight(mActivityWR.get()) - localHeight, 0, 0);

            mLocalView.addView(surfaceView, 0, mLayoutParams);
            surfaceView.setZOrderMediaOverlay(true);
        }
    }

    private void showRemoteLayout(int remoteWidth, int remoteHeight) {
        svRemote = mPresenter.getRemotePreviewSurfaceView();

        if (remoteWidth == 0 || remoteHeight == 0) {
            remoteWidth = 720;
            remoteHeight = 1080;
        }

        ViewGroup svRemoteParent = (ViewGroup) svRemote.getParent();
        mRemoteView.removeAllViews();
        if (svRemoteParent != null) {
            svRemoteParent.removeAllViews();
        }
        mRemoteView.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(remoteWidth, remoteHeight);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        mRemoteView.setLayoutParams(layoutParams);
        mRemoteView.addView(svRemote);
        if (svRemote instanceof SurfaceView) {
            ((SurfaceView) svRemote).setZOrderMediaOverlay(false);
        }
    }

    @Override
    public void changeContactState(String str) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mContactState.setText(str);
            }
        });
    }

    @Override
    public void updateName(String string) {
        mContactName.setText(string);
    }

    @Override
    public void callOutAudio() {
        log.d("callOutAudio");

        mPhoneButtonViews[0].setVisibility(View.VISIBLE);
        mPhoneButtonViews[1].setVisibility(View.GONE);
        mPhoneButtonViews[2].setVisibility(View.GONE);
        mPhoneButtonViews[3].setVisibility(View.VISIBLE);

    }

    @Override
    public void callOutVideo() {
        mPhoneButtonViews[0].setVisibility(View.GONE);
        mPhoneButtonViews[1].setVisibility(View.VISIBLE);
        mPhoneButtonViews[2].setVisibility(View.GONE);
        mPhoneButtonViews[3].setVisibility(View.VISIBLE);
    }

    @Override
    public void callAlertingAudio() {

        mPhoneButtonViews[0].setVisibility(View.VISIBLE);
        mPhoneButtonViews[1].setVisibility(View.GONE);
        mPhoneButtonViews[2].setVisibility(View.VISIBLE);
        mPhoneButtonViews[3].setVisibility(View.VISIBLE);

    }

    @Override
    public void callAlertingVideo() {
        mPhoneButtonViews[0].setVisibility(View.GONE);
        mPhoneButtonViews[1].setVisibility(View.VISIBLE);
        mPhoneButtonViews[2].setVisibility(View.GONE);
        mPhoneButtonViews[3].setVisibility(View.VISIBLE);
    }

    @Override
    public void pickUpAudio() {
        mPhoneButtonViews[0].setVisibility(View.VISIBLE);
        mPhoneButtonViews[1].setVisibility(View.GONE);
        mPhoneButtonViews[2].setVisibility(View.GONE);
        mPhoneButtonViews[3].setVisibility(View.VISIBLE);
    }

    @Override
    public void pickUpVideo() {
        mPhoneButtonViews[0].setVisibility(View.GONE);
        mPhoneButtonViews[1].setVisibility(View.VISIBLE);
        mPhoneButtonViews[2].setVisibility(View.GONE);
        mPhoneButtonViews[3].setVisibility(View.VISIBLE);

    }

    @Override
    public void hangUp() {
        this.finish();
    }

    @Override
    public void changeMuteButton(boolean mute) {
        muteFlag = mute;
        if (muteFlag) {
            ((ImageButton) mPhoneSmallButtonViews[0].findViewById(R.id.phone_call_button_image)).setImageResource(R.drawable.soundai_mute_icon_h);
        } else {
            ((ImageButton) mPhoneSmallButtonViews[0].findViewById(R.id.phone_call_button_image)).setImageResource(R.drawable.soundai_mute_icon_n);
        }
    }

    @Override
    public void setContactNumber(String number) {
        mContactNumber.setText(number);
    }

    @Override
    public void receiveOpenCameraError() {
        if (cameraErrorText == null) {
            cameraErrorText = new TextView(this);
            cameraErrorText.setBackgroundColor(mContext.getResources().getColor(R.color.mCardBackground));
            cameraErrorText.setGravity(Gravity.CENTER);
            cameraErrorText.setTextColor(mContext.getResources().getColor(R.color.white));
            cameraErrorText.setTextSize(20);
            cameraErrorText.setText("您的摄像头未安装或存在异常");

            if (mLayoutParams == null) {
                mLayoutParams = new RelativeLayout.LayoutParams(mLocalWidth, mLocalHeight);
            }
            mLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT | RelativeLayout.ALIGN_PARENT_BOTTOM);
            mLocalView.addView(cameraErrorText, 0, mLayoutParams);
        }
    }

    @Override
    public void calculateView(int width, int height) {
        rotateRemoteWidth = remoteWidth = screenWidth;
        rotateRemoteHeight = remoteHeight = screenHeight;
        float dataBack = (float) height / width;
        float dataRotate = (float) width / height;  //调换宽高，计算宽屏显示，用于远端图像旋转动画
        float dataLocal = (float) screenHeight / screenWidth;
        if (dataBack < dataLocal) {
            remoteHeight = remoteWidth * height / width;
        } else {
            remoteWidth = remoteHeight * width / height;
        }
        if (dataRotate < dataLocal) {
            rotateRemoteHeight = rotateRemoteWidth * width / height;
        } else {
            rotateRemoteWidth = rotateRemoteHeight * height / width;
        }
    }

    @Override
    public void loadLocalView(int width, int height) {
        mLocalWidth = (int) (0.35 * DisplayUtils.getDisplayRealWidth(this));
        mLocalHeight = (int) (0.35 * DisplayUtils.getDisplayRealHeight(this));
        float dataBack = (float) height / width;
        float dataLocal = (float) mLocalHeight / mLocalWidth;
        if (dataBack < dataLocal) {
            mLocalHeight = mLocalWidth * height / width;
        } else {
            mLocalWidth = mLocalHeight * width / height;
        }
        showLocalLayout(mLocalWidth, mLocalHeight);
    }

    @Override
    public void loadRemoteView() {
        if (isComingEncodeErrorMsg) {
            isComingEncodeErrorMsg = false;
            showLocalLayout(mLocalWidth, mLocalHeight);
        }
        showRemoteLayout(remoteWidth, remoteHeight);
    }

    @Override
    public void setComingEncodeErrorMsg(boolean isComingEncodeErrorMsg) {
        this.isComingEncodeErrorMsg = isComingEncodeErrorMsg;
    }

    @Override
    public void finish() {
        mPresenter.unregisterListener();
        super.finish();
    }
}
