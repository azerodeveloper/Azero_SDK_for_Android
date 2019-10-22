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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.azero.sampleapp.manager.ActivityLifecycleManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Utils {

    public static String getMac(Context context) {
        String mac = "";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mac = getLocalMacFromformWifi(context);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mac = getMacFromFile();
        } else {
            mac = getMacFromHardWare();
        }
        return mac;
    }

    private static String getMacFromHardWare() {
        try {
            ArrayList<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface interfaces : all) {
                if (!interfaces.getName().equalsIgnoreCase("wlan0")) continue;
                byte[] macbytes = interfaces.getHardwareAddress();
                if (macbytes == null)
                    return "02:00:00:00:00:00";
                StringBuilder builder = new StringBuilder();
                for (byte b : macbytes) {
                    builder.append(String.format("%02X:", b));
                }
                if (builder.length() > 0)
                    builder.deleteCharAt(builder.length() - 1);
                return builder.toString();
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "02:00:00:00:00:00";
    }

    private static String getMacFromFile() {
        String mac = "02:00:00:00:00:00";
        try {
            mac = new BufferedReader(new FileReader(new File("/sys/class/net/wlan0/address"))).readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mac;
    }

    private static String getLocalMacFromformWifi(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        String macAddress = wifiManager.getConnectionInfo().getMacAddress();
        return macAddress;
    }


    public static String getIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        return String.valueOf(ipAddress);
    }

    public static String getSoftVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            if (packageInfo != null)
                return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getimei(@NonNull Context context) {
        Objects.requireNonNull(context);
        String defaultSN = "12345678";
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager == null)
            return null;

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        String imei = defaultSN;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    imei = telephonyManager.getImei();
                } else {
                    imei = telephonyManager.getDeviceId();
                }
                imei = (imei == null) ? defaultSN :imei;
                return imei;
            } catch (Exception e) {
                e.printStackTrace();
                return imei;
            }
        } else {
            try {
                imei = telephonyManager.getImei();
                imei = (imei == null) ? defaultSN :imei;
                return imei;
            } catch (Exception e) {
                e.printStackTrace();
                return imei;
            }
        }
    }

    @SuppressLint("HardwareIds")
    public static String getSn() {
        String serial = null;
        try {
            @SuppressLint("PrivateApi") Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serial;
    }

    public static void wakeUpAndUnlock(Context context) {
        // 获取电源管理器对象
        PowerManager pm = (PowerManager) context.getApplicationContext()
                .getSystemService(Context.POWER_SERVICE);
        boolean screenOn = pm.isScreenOn();
        Log.d("WakeScreen0", "screenOn: " + screenOn);
        if (!screenOn) {
            // 获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
            @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl = pm.newWakeLock(
                    PowerManager.ACQUIRE_CAUSES_WAKEUP |
                            PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
            wl.acquire(10000); // 点亮屏幕
            wl.release(); // 释放
        }
    }

    public static String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        StringBuilder mStringBuilder = new StringBuilder();
        Formatter mFormatter = new Formatter(mStringBuilder, Locale.CHINA);
        mStringBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    /**
     * 判断当前应用是否在前台
     *
     * @return
     */
    public static boolean isApplicationForeground() {
        return ActivityLifecycleManager.getInstance().isAppForeground();
    }

    /**
     * first app user
     */
    public static final int AID_APP = 10000;
    /**
     * offset for uid ranges for each user
     */
    public static final int AID_USER = 100000;

    public static String getForegroundApp() {
        Log.e("PKG", "VersionCode:" + Build.VERSION.SDK_INT);
        File[] files = new File("/proc").listFiles();
        int lowestOomScore = Integer.MAX_VALUE;
        String foregroundProcess = null;
        for (File file : files) {
            if (!file.isDirectory()) {
                continue;
            }
            int pid;
            try {
                pid = Integer.parseInt(file.getName());
            } catch (NumberFormatException e) {
                continue;
            }
            try {
                String cgroup = read(String.format("/proc/%d/cgroup", pid));
                String[] lines = cgroup.split("\n");
                String cpuSubsystem;
                String cpuaccctSubsystem;


                for (int i = 0; i < lines.length; i++) {

                    Log.e("PKG", lines[i]);

                }


                if (lines.length == 2) {//有的手机里cgroup包含2行或者3行，我们取cpu和cpuacct两行数据
                    cpuSubsystem = lines[0];
                    cpuaccctSubsystem = lines[1];
                } else if (lines.length == 3) {
                    cpuSubsystem = lines[0];
                    cpuaccctSubsystem = lines[2];
                } else if (lines.length == 5) {//6.0系统
                    cpuSubsystem = lines[2];
                    cpuaccctSubsystem = lines[4];
                } else {
                    continue;
                }
                if (!cpuaccctSubsystem.endsWith(Integer.toString(pid))) {
                    // not an application process
                    continue;
                }
                if (cpuSubsystem.endsWith("bg_non_interactive")) {
                    // background policy
                    continue;
                }
                String cmdline = read(String.format("/proc/%d/cmdline", pid));
                if (cmdline.contains("com.android.systemui")) {
                    continue;
                }
                int uid = Integer.parseInt(
                        cpuaccctSubsystem.split(":")[2].split("/")[1].replace("uid_", ""));
                if (uid >= 1000 && uid <= 1038) {
                    // system process
                    continue;
                }
                int appId = uid - AID_APP;
                int userId = 0;
                // loop until we get the correct user id.
                // 100000 is the offset for each user.
                while (appId > AID_USER) {
                    appId -= AID_USER;
                    userId++;
                }
                if (appId < 0) {
                    continue;
                }
                // u{user_id}_a{app_id} is used on API 17+ for multiple user account support.
                // String uidName = String.format("u%d_a%d", userId, appId);
                File oomScoreAdj = new File(String.format("/proc/%d/oom_score_adj", pid));
                if (oomScoreAdj.canRead()) {
                    int oomAdj = Integer.parseInt(read(oomScoreAdj.getAbsolutePath()));
                    if (oomAdj != 0) {
                        continue;
                    }
                }
                int oomscore = Integer.parseInt(read(String.format("/proc/%d/oom_score", pid)));
                if (oomscore < lowestOomScore) {
                    lowestOomScore = oomscore;
                    foregroundProcess = cmdline;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return foregroundProcess;
    }

    private static String read(String path) throws IOException {
        StringBuilder output = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(path));
        output.append(reader.readLine());
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            output.append('\n').append(line);
        }
        reader.close();
        return output.toString().trim();
    }

    public static boolean isForeground(Context context, String className) {
        if (context == null || TextUtils.isEmpty(className)) {
            return false;
        }
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            return className.equals(cpn.getClassName());
        }
        return false;
    }
}
