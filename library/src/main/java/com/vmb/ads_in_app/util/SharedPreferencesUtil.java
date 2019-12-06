package com.vmb.ads_in_app.util;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class SharedPreferencesUtil {

    public static boolean getPrefferBool(Context context, String key, boolean value) {
        if(context == null)
            return false;

        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, value);
    }

    public static void putPrefferBool(Context context, String key, boolean value) {
        if(context == null)
            return;

        Editor sharedata = PreferenceManager.getDefaultSharedPreferences(context).edit();
        sharedata.putBoolean(key, value);
        sharedata.commit();
    }

    public static int getPrefferInt(Context context, String key, int value) {
        if(context == null)
            return 0;

        return PreferenceManager.getDefaultSharedPreferences(context).getInt(key, value);
    }

    public static void putPrefferInt(Context context, String key, int value) {
        if(context == null)
            return;

        Editor sharedata = PreferenceManager.getDefaultSharedPreferences(context).edit();
        sharedata.putInt(key, value);
        sharedata.commit();
    }

    public static long getPrefferLong(Context context, String key, long value) {
        if(context == null)
            return 0;

        return PreferenceManager.getDefaultSharedPreferences(context).getLong(key, value);
    }

    public static void putPrefferLong(Context context, String key, long value) {
        if(context == null)
            return;

        Editor sharedata = PreferenceManager.getDefaultSharedPreferences(context).edit();
        sharedata.putLong(key, value);
        sharedata.commit();
    }

    public static String getPrefferString(Context context, String key, String value) {
        if(context == null)
            return "";

        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, value);
    }

    public static void putPrefferString(Context context, String key, String value) {
        if(context == null)
            return;

        Editor sharedata = PreferenceManager.getDefaultSharedPreferences(context).edit();
        sharedata.putString(key, value);
        sharedata.commit();
    }

    public static <T> void put(Context context, String key, T value) {
        if (value == null || context == null) {
            return;
        }
        Type type = new TypeToken<T>() {
        }.getType();
        String json = new Gson().toJson(value, type);
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(key, json);
        editor.commit();
    }

    public static <T> T get(Context context, String key, Class<T> type) {
        if (context == null) return null;
        String json = PreferenceManager.getDefaultSharedPreferences(context).getString(key, "");
        if (!TextUtils.isEmpty(json)) {
            return new Gson().fromJson(json, type);
        } else {
            return null;
        }
    }
}