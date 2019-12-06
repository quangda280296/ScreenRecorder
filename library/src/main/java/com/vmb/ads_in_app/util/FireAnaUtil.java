package com.vmb.ads_in_app.util;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.firebase.analytics.FirebaseAnalytics;

public class FireAnaUtil {

    public static void logEvent(Context context, String eventName) {
        logEvent(context, eventName, null);
    }

    public static void logEvent(Context context, String eventName, String type) {
        // Obtain the FirebaseAnalytics instance.
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        //CustomEvent customEvent = new CustomEvent(eventName);

        Bundle params = new Bundle();
        if (TextUtils.isEmpty(type)) {
            params.putString(eventName, eventName);
            //customEvent.putCustomAttribute(eventName, eventName);
        } else {
            params.putString("type", type);
            //customEvent.putCustomAttribute("type", type);
        }

        if (mFirebaseAnalytics != null)
            mFirebaseAnalytics.logEvent(eventName, params);

        //Answers.getInstance().logCustom(customEvent);
    }

    public static void setProperty(Context context, String propertyName, int value) {
        // Obtain the FirebaseAnalytics instance.
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        if (mFirebaseAnalytics != null)
            mFirebaseAnalytics.setUserProperty(propertyName, value + "");
    }
}