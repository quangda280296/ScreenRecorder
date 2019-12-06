package com.vmb.ads_in_app.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class CountryCodeUtil {

    public static void setCountryCode(final Context context, final String code, final String version, final String packg) {
        final String TAG = "setCountryCode()";
        if (context == null)
            return;

        String country_code = CountryCodeUtil.getCountryCode(context);

        if (TextUtils.isEmpty(country_code)) {
            final OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build();

            new Thread() {
                @Override
                public void run() {
                    try {
                        Request request = new Request.Builder()
                                .url("https://ipinfo.io/json").get().build();

                        okhttp3.Response response = client.newCall(request).execute();
                        String result = response.body().string();
                        Log.i(TAG, "result = " + result);
                        JSONObject jsonObject = new JSONObject(result);
                        String country = jsonObject.getString("country").toUpperCase();

                        /*if (TextUtils.isEmpty(country))
                            country = Locale.getDefault().getCountry().toUpperCase();*/

                        if (country.contains("_")) {
                            String[] words = country.split("_");
                            if (words.length > 1)
                                country = words[1];
                        }

                        SharedPreferencesUtil.putPrefferString(context, "country_code", country);
                        Log.i(TAG, "country = " + country);

                        if (RefreshToken.getInstance().isCountryNull()) {
                            String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                            if (!TextUtils.isEmpty(refreshedToken)) {
                                Log.i(TAG, "refreshedToken: " + refreshedToken);
                                TokenNotiUtil.sendTokenToServer(context, refreshedToken, code, version, packg);
                            }
                        }

                    } catch (Exception e) {
                        Log.i(TAG, "catch: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    public static String getCountryCode(Context context) {
        if (context == null)
            return "";

        String country_code = SharedPreferencesUtil.getPrefferString(context, "country_code", "");
        return country_code;
    }
}