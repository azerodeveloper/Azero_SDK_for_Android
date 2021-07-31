package com.azero.sampleapp.widget;

import com.azero.sdk.event.AzeroEvent;
import com.azero.sdk.interfa.AzeroOSListener;
import com.azero.sdk.util.log;

/**
 * Created by weijianqiang
 * On 2021/6/24
 * Description: 系统状态回调类，以及各种技能（包含自定义技能）回调
 */
public class AzeroOSListenerImpl implements AzeroOSListener {
    private static final String TAG = "AzeroOSListenerImpl";

    public AzeroOSListenerImpl() {
    }


    @Override
    public void onEvent(AzeroEvent event, String msg) {
        log.d("AzeroOSListenerImpl:   AzeroEvent ===>" + event + ",     msg===>" + msg);
        switch (event) {
            case EVENT_ENGINE_INITIALIZATION_COMPLETE:
                break;
            case EVENT_CONNECTION_STATUS_CHANGED:
                break;

        }
    }
}
