package com.azero.sampleapp.widget;

import android.util.Log;

import com.azero.sdk.GlobalContext;
import com.azero.sdk.config.ConfigSetting;
import com.azero.sdk.impl.SpeechRecognizer.AbsSpeechRecognizer;
import com.azero.sdk.manager.AzeroManager;
import com.azero.sdk.uievent.IUIListener;
import com.azero.sdk.util.executors.TaskExecutor;


/**
 * Created by weijianqiang
 * On 2021/6/25
 * Description:
 */
public class UIListenerImpl implements IUIListener {
    private static final String TAG = "UIListenerImpl";
    
    @Override
    public void onWakeUp() {
        Log.d(TAG, "onWakeUp: ");
        if(!ConfigSetting.enableNoNeedWakeup()){
            AzeroManager.getInstance().playTip(AbsSpeechRecognizer.AudioCueState.START_TOUCH);
        }
        TaskExecutor.executeOnMainTnread(new Runnable() {
            @Override
            public void run() {
                GlobalBottomBar.getInstance(GlobalContext.getContext()).show("", 0, false);
            }
        });

        //AppExecutors.getInstance().mainThread().execute(() -> GlobalBottomBarManager.getInstance(GlobalContext.getContext()).getGlobalBottomBar().show("", 0, false));

    }

    @Override
    public void onRecordStart() {
        Log.d(TAG, "onRecordStart: ");
    }

    @Override
    public void onRecording(int volume, String asrText) {
        Log.d(TAG, "onRecording: ");
//        TaskExecutor.executeOnMainTnread(new Runnable() {
//            @Override
//            public void run() {
//                GlobalBottomBar.getInstance(GlobalContext.getContext()).append(asrText);
//            }
//        });
        //AppExecutors.getInstance().mainThread().execute(() -> GlobalBottomBarManager.getInstance(GlobalContext.getContext()).getGlobalBottomBar().append(asrText));
    }

    @Override
    public void onRecordStop() {
        Log.d(TAG, "onRecordStop: ");
        if(!ConfigSetting.enableNoNeedWakeup()){
            AzeroManager.getInstance().playTip(AbsSpeechRecognizer.AudioCueState.END);
        }

        TaskExecutor.executeOnMainTnread(new Runnable() {
            @Override
            public void run() {
                GlobalBottomBar.getInstance(GlobalContext.getContext()).hide(2000);
            }
        });
       // AppExecutors.getInstance().mainThread().execute(() -> GlobalBottomBarManager.getInstance(GlobalContext.getContext()).getGlobalBottomBar().hide(2000));
    }

    @Override
    public void onRecognizeStart() {
        Log.d(TAG, "onRecognizeStart: ");
    }

    @Override
    public void onRecognizeEnd() {
        Log.d(TAG, "onRecognizeEnd: ");
    }
}
