package com.vmb.ads_in_app.util;

import android.content.Context;
import android.content.pm.PackageManager;

public class FanpageUtil {

    //method to get the right URL to use in the intent
    public static String getFacebookPageURL(Context context, String url_fanpage) {
        PackageManager packageManager = context.getPackageManager();
        try {
            int versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode;
            if (versionCode >= 3002850) { //newer versions of fb app
                return "fb://facewebmodal/f?href=" + url_fanpage;
            } else { //older versions of fb app
                return "fb://page/" + url_fanpage;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return url_fanpage; //normal web url
        }
    }
}