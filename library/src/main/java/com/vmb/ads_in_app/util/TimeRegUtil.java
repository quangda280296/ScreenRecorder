package com.vmb.ads_in_app.util;

import android.content.Context;
import android.text.TextUtils;

public class TimeRegUtil {

    public static void setTimeRegister(Context context) {
        if(context == null)
            return;

        String timeRegister = getTimeRegister(context);

        if (TextUtils.isEmpty(timeRegister)) {
            SharedPreferencesUtil.putPrefferString(context, "time_register", String.valueOf(System.currentTimeMillis() / 1000));
        }
    }

    public static String getTimeRegister(Context context) {
        if(context == null)
            return "";

        String timeRegister = SharedPreferencesUtil.getPrefferString(context, "time_register", "");
        return timeRegister;
    }
}