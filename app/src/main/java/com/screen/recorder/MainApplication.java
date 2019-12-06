package com.screen.recorder;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.multidex.MultiDexApplication;
import android.webkit.WebView;

import com.crashlytics.android.Crashlytics;
import com.facebook.ads.AudienceNetworkAds;
import com.google.firebase.FirebaseApp;
import com.screen.recorder.activity.MainActivity;
import com.screen.recorder.receiver.ConnectionReceiver;
import com.screen.recorder.service.ScreenRecorderService;
import com.vmb.ads_in_app.handler.AdsHandler;
import com.vmb.ads_in_app.util.TimeRegUtil;

import io.fabric.sdk.android.Fabric;

public class MainApplication extends MultiDexApplication {

    ConnectionReceiver receiver = new ConnectionReceiver();

    @Override
    public void onCreate() {
        super.onCreate();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Fabric.with(getApplicationContext(), new Crashlytics());
                TimeRegUtil.setTimeRegister(getApplicationContext());
                FirebaseApp.initializeApp(getApplicationContext());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    String process = getProcessName();
                    if (!getPackageName().equals(process))
                        WebView.setDataDirectorySuffix(process);
                }

                /*try {
                    // Initialize the Audience Network SDK
                    AudienceNetworkAds.initialize(getApplicationContext());
                } catch (Exception e) {
                    e.printStackTrace();
                }*/

                Intent intent = new Intent(getApplicationContext(), ScreenRecorderService.class);
                if (Build.VERSION.SDK_INT >= 26) {
                    startForegroundService(intent);
                } else {
                    startService(intent);
                }
            }
        }).start();

        registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                if (activity instanceof MainActivity) {
                    try {
                        unregisterReceiver(receiver);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    AdsHandler.getInstance().destroyInstance();
                    //stopService(new Intent(getApplicationContext(), ScreenRecorderService.class));
                }
            }
        });

        //initInfoDevice(Config.CODE_CONTROL_APP, Config.VERSION_APP);
    }

    public void registerReceiver() {
        registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }
}