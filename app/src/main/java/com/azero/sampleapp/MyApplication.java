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

package com.azero.sampleapp;

import android.support.multidex.MultiDexApplication;

import com.azero.platforms.iface.config.AzeroConfiguration;
import com.azero.sampleapp.activity.controller.LocalViewController;
import com.azero.sampleapp.impl.audioinput.record.SystemRecord;
import com.azero.sampleapp.manager.ActivityLifecycleManager;
import com.azero.sampleapp.widget.DisconnectStatusBar;
import com.azero.sampleapp.impl.azeroexpress.navigation.NavigationHandler;
import com.azero.sampleapp.impl.phonecallcontroller.PhoneCallControllerHandler;
import com.azero.sampleapp.impl.audioinput.AudioInputManager;
import com.azero.sampleapp.impl.audioinput.SpeechRecognizerHandler;
import com.azero.sampleapp.impl.andlink.AndLinkViewHandler;
import com.azero.sampleapp.impl.azeroexpress.AzeroExpressHandler;
import com.azero.sampleapp.impl.andcallcontroller.AndCallViewControllerHandler;
import com.azero.sampleapp.impl.audioinput.record.BasexRecord;
import com.azero.sampleapp.util.Utils;
import com.azero.platforms.iface.AlexaClient;
import com.azero.sdk.AndLinkConfig;
import com.azero.sdk.Config;

import com.azero.sdk.AzeroManager;
import com.azero.sdk.HandlerContainer;
import com.azero.sdk.HandlerContainerBuilder;
import com.azero.sdk.event.Command;
import com.azero.sdk.impl.Alerts.AlertsHandler;
import com.azero.sdk.impl.AndLink.AndLinkManager;
import com.azero.sdk.impl.AzeroClient.AzeroClientHandler;
import com.azero.sdk.util.executors.AppExecutors;
import com.azero.sdk.util.log;

public class MyApplication extends MultiDexApplication implements AudioInputManager.WakeUpConsumer {
    private static MyApplication instance;
    private AppExecutors appExecutors = new AppExecutors();
    private LocalViewController mLocalViewController;
    private AzeroExpressHandler mAzeroExpressHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        registerActivityLifecycleCallbacks(ActivityLifecycleManager.getInstance());
        initAzero();
    }

    public static MyApplication getInstance() {
        return instance;
    }


    private void initAzero() {
        //初始化
        try {
            //第一步 配置参数 注册必要模块 @{
            Config config = new Config(
                    "",         //productID 网站申请
                    "",             //ClientID  网站申请
                    Utils.getimei(this),                    //DeviceSN 传入Mac地址或IMEI号，必须保证设备唯一
                    Config.SERVER.PRO,                              //Server    选择使用的服务器  FAT 测试环境 PRO 正式环境
                    Setting.enableLocalVAD                          //localVAD  是否使用本地VAD
            );
            //定义界面消失时间，不填则使用如下默认值
            config.setTimeoutList(new AzeroConfiguration.TemplateRuntimeTimeout[]{
                    //Template界面在TTS播放完后消失的时间
                    new AzeroConfiguration.TemplateRuntimeTimeout(AzeroConfiguration.TemplateRuntimeTimeoutType.DISPLAY_CARD_TTS_FINISHED_TIMEOUT, 8000),
                    //音频播放完后界面消失时间
                    new AzeroConfiguration.TemplateRuntimeTimeout(AzeroConfiguration.TemplateRuntimeTimeoutType.DISPLAY_CARD_AUDIO_PLAYBACK_FINISHED_TIMEOUT, 300000),
                    //音频播放暂停时界面消失时间
                    new AzeroConfiguration.TemplateRuntimeTimeout(AzeroConfiguration.TemplateRuntimeTimeoutType.DISPLAY_CARD_AUDIO_PLAYBACK_STOPPED_PAUSED_TIMEOUT, 300000)
            });

            //初始化数据读取模块
            AudioInputManager audioInputManager = new AudioInputManager(this, new SystemRecord());
            audioInputManager.addWakeUpObserver(this);
            //识别数据模块
            SpeechRecognizerHandler speechRecognizerHandler = new SpeechRecognizerHandler(
                    appExecutors,
                    this,
                    audioInputManager,
                    true,
                    true
            );

            // 选择是否开启AndLink模块及和家固话模块
            HandlerContainerBuilder.PHONE phone;
            if (Setting.enableHjghAndAndLink) {
                phone = HandlerContainerBuilder.PHONE.HEJIA;
            } else {
                phone = HandlerContainerBuilder.PHONE.PHONE;
            }

            //选择和注册必要模块
            HandlerContainer handlerContainer = new HandlerContainerBuilder(this)
                    .setAudioHandler(HandlerContainerBuilder.AUDIO.SOUNDAI)
                    .setMusicHandler(HandlerContainerBuilder.MUSIC.MIGU)
                    .setVideoHandler(HandlerContainerBuilder.VIDEO.MIFENG)
                    .setPhoneCallHandler(phone)
                    .setSpeechRecognizer(speechRecognizerHandler) //必须注册识别模块
                    .create();
            //@}

            //第二歩 启动引擎 @{
            AzeroManager.getInstance().startEngine(this, config, handlerContainer);
            AzeroManager.getInstance().setDebug(true);
            //@}

            //自定义内容模块
            mAzeroExpressHandler = new AzeroExpressHandler(appExecutors, this);
            mAzeroExpressHandler.setNavigationHandler(new NavigationHandler(this));
            AzeroManager.getInstance().setCustomAgent(mAzeroExpressHandler);

            //初始化本地View控制模块
            mLocalViewController = new LocalViewController(this);

            //Andlink and PhoneCall
            //@{
            if (Setting.enableHjghAndAndLink) {
                enableAndLinkAndHjgh(config, audioInputManager);
            } else {
                PhoneCallControllerHandler phoneCallControllerHandler = new PhoneCallControllerHandler();
            }
            //@}

            // 初始化结束后自动连接和家固话，若未绑定则显示绑定界面
            AndLinkManager.getInstance(this).requestAndLinkAddress();

        } catch (RuntimeException e) {
            log.e("Could not start engine. Reason: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void enableAndLinkAndHjgh(Config config, AudioInputManager audioInputManager) {
        AndLinkConfig andLinkConfig = new AndLinkConfig(
                "500405",           // deviceType AndLink产品Id
                "SaiApp 627b3650d0ed9473:OGZlN2NjMjEzMDZhNjY0MTE0ZTVmYTIwMjY4YzM5M2Q=",  // authorization 身份认证信息，组成方式 SaiApp appId:secretKey
                "SxT1IeQDEd36tG2M",         // productToken 和家固话产品验证码，通过和家固话管理平台获取
                "1.1",      // firmWareVersion 固件版本
                "00ss7wd3ltutzgxo",     // authAppKey 和家固话管理平台获取
                "p8bw2upxfbqssfro"      // appSecret 和家固话管理平台获取
        );

        AndLinkManager mAndLinkManager = AndLinkManager.getInstance(this);

        //初始化AndLink界面控制分发模块
        AndLinkViewHandler mAndLinkViewHandler = new AndLinkViewHandler();

        mAndLinkViewHandler.init(mAndLinkManager, config, andLinkConfig);
        mAndLinkViewHandler.setLocalViewController(mLocalViewController);

        //初始化和家固话界面控制模块
        AndCallViewControllerHandler mAndCallViewControllerHandler = AndCallViewControllerHandler.getInstance();
        mAndCallViewControllerHandler.init(this, andLinkConfig, audioInputManager);

        mAndCallViewControllerHandler.setLocalViewController(mLocalViewController);

        mAzeroExpressHandler.setAndLinkHandler(mAndLinkManager);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        AzeroManager.getInstance().release();
    }

    /**
     * 唤醒时触发
     *
     * @param wakeWord 唤醒词
     */
    @Override
    public void onWakewordDetected(String wakeWord) {
        //如果闹钟正在响，停止闹钟
        stopAlerts();
        AzeroClientHandler mAzeroClient = (AzeroClientHandler) AzeroManager.getInstance().getHandler(AzeroManager.AZERO_CLIENT_HANDLER);
        if (mAzeroClient == null) {
            return;
        }
        //确认已与服务器连接
        if (mAzeroClient.getConnectionStatus()
                == AlexaClient.ConnectionStatus.CONNECTED) {
            //唤醒时解锁屏幕
            Utils.wakeUpAndUnlock(this);
            if (Setting.enableLocalVAD) {
                //本地判断VADEnd
                ((SpeechRecognizerHandler) AzeroManager.getInstance().getHandler(AzeroManager.SPEECH_RECOGNIZER_HANDLER)).onHoldToTalk();
            } else {
                //云端判断VADEnd
                ((SpeechRecognizerHandler) AzeroManager.getInstance().getHandler(AzeroManager.SPEECH_RECOGNIZER_HANDLER)).onTapToTalk();
            }
        } else {
            String message = "Wakeword Detected but AlexaClient not connected. ConnectionStatus: "
                    + mAzeroClient.getConnectionStatus();
            appExecutors.mainThread().execute(() -> {
                //弹窗提示网络错误
                DisconnectStatusBar.getInstance(this).show(DisconnectStatusBar.ErrorType.SERVER_ERROR, 5000);
                if (DisconnectStatusBar.getInstance(this).getErrorType() == DisconnectStatusBar.ErrorType.SERVER_ERROR) {
                    //播放服务器连接错误提示
                    AzeroManager.getInstance().executeCommand(Command.CMD_PLAY_AUDIO_SERVER_ERROR);
                } else {
                    //播放网络连接错误提示
                    AzeroManager.getInstance().executeCommand(Command.CMD_PLAY_AUDIO_NET_ERROR);
                }
            });
            log.w(message);
        }
    }

    private void stopAlerts() {
        AlertsHandler alerts = (AlertsHandler) AzeroManager.getInstance().getHandler(AzeroManager.ALERT_HANDLER);
        if (alerts != null) {
            alerts.localStop();
        }
    }
}
