package com.vmb.ads_in_app.util;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

public class DeviceUtil {

    public static String getDeviceId(Context context) {
        try {
            String idDevice = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            return idDevice;
        } catch (Exception e) {
            return "Unknown";
        }
    }

    public static String getDeviceName() {
        String deviceName = "";
        try {
            String manufacturer = Build.MANUFACTURER;
            String model = Build.MODEL;
            if (model.startsWith(manufacturer)) {
                return capitalize(model);
            }
            if (manufacturer.equalsIgnoreCase("HTC")) {
                return "HTC " + model;
            }
            deviceName = capitalize(manufacturer) + " " + model;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return deviceName;
    }

    public static String getDeviceOS() {
        try {
            String os_version = Build.VERSION.RELEASE;
            return os_version;
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;
        String phrase = "";
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase += Character.toUpperCase(c);
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase += c;
        }
        return phrase;
    }
}