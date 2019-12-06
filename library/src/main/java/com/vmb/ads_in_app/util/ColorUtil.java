package com.vmb.ads_in_app.util;

import android.content.Context;
import android.os.Build.VERSION;

public class ColorUtil {

    public static int getColor(Context context, int i) {
        if (VERSION.SDK_INT >= 23) {
            return context.getResources().getColor(i, context.getTheme());
        }
        return context.getResources().getColor(i);
    }
}