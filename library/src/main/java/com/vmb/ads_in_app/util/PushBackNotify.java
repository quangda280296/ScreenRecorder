package com.vmb.ads_in_app.util;

import android.content.Context;
import android.util.Log;

import com.vmb.ads_in_app.Interface.IAPIPushBackNotify;
import com.vmb.ads_in_app.LibrayData;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PushBackNotify {

    public static void push(final Context context, String pushId, String code, String version, String packg) {
        final String TAG = "PushBackNotify";

        String deviceID = DeviceUtil.getDeviceId(context);
        String timeRegister = TimeRegUtil.getTimeRegister(context);
        String os_version = DeviceUtil.getDeviceOS();
        String phone_name = DeviceUtil.getDeviceName();
        String country = CountryCodeUtil.getCountryCode(context);

        Log.i(TAG, "deviceID = " + deviceID);
        Log.i(TAG, "code = " + code);
        Log.i(TAG, "version = " + version);
        Log.i(TAG, "package = " + packg);
        Log.i(TAG, "os_version = " + os_version);
        Log.i(TAG, "phone_name = " + phone_name);
        Log.i(TAG, "country = " + country);
        Log.i(TAG, "timeRegister = " + timeRegister);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("deviceID", deviceID)
                .addFormDataPart("code", code)
                .addFormDataPart("version", version)
                .addFormDataPart("package", packg)
                .addFormDataPart("os_version", os_version)
                .addFormDataPart("phone_name", phone_name)
                .addFormDataPart("country", country)
                .addFormDataPart("timeRegister", timeRegister)
                .addFormDataPart("pushId", pushId)
                .build();

        IAPIPushBackNotify api = RetrofitInitiator.createService(IAPIPushBackNotify.class, LibrayData.Url.URL_BASE);
        Call<ResponseBody> call = api.pushBack(requestBody);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.i(TAG, "onResponse()");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i(TAG, "onFailure()");
            }
        });
    }
}