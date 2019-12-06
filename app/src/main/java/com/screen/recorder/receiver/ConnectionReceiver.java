package com.screen.recorder.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.screen.recorder.Config;
import com.vmb.ads_in_app.GetConfig;
import com.vmb.ads_in_app.util.NetworkUtil;

/**
 * Created by keban on 9/1/2017.
 */

public class ConnectionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (NetworkUtil.isNetworkAvailable(context)) {
            GetConfig.callAPI(context, Config.CODE_CONTROL_APP, Config.VERSION_APP, Config.PACKAGE_NAME);
            Log.i("ConnectionReceiver", "receive internet connection");
        } else
            Log.i("ConnectionReceiver", "no internet connection");
    }
}