package com.vmb.ads_in_app.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;
import com.vmb.ads_in_app.Interface.IAPISendToken;
import com.vmb.ads_in_app.LibrayData;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TokenNotiUtil {

    public static void sendTokenToServer(final Context context, String token, String code, String version, String packg) {
        final String TAG = "sendTokenToServer";

        if (context == null) {
            Log.i(TAG, "context == null");
            return;
        }

        // TODO: Implement this method to send token to your app server.
        final String deviceID = DeviceUtil.getDeviceId(context);
        String timeRegister = TimeRegUtil.getTimeRegister(context);
        String os_version = DeviceUtil.getDeviceOS();
        String phone_name = DeviceUtil.getDeviceName();
        final String country = CountryCodeUtil.getCountryCode(context);
        if (TextUtils.isEmpty(country)) {
            Log.i(TAG, "country empty");
            RefreshToken.getInstance().setCountryNull(true);
        }

        Log.i(TAG, "deviceID = " + deviceID);
        Log.i(TAG, "code = " + code);
        Log.i(TAG, "version = " + version);
        Log.i(TAG, "package = " + packg);
        Log.i(TAG, "os_version = " + os_version);
        Log.i(TAG, "phone_name = " + phone_name);
        Log.i(TAG, "country = " + country);
        Log.i(TAG, "timeRegister = " + timeRegister);
        Log.i(TAG, "token_id = " + token);

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
                .addFormDataPart("token_id", token)
                .build();

        IAPISendToken api = RetrofitInitiator.createService(IAPISendToken.class, LibrayData.Url.URL_BASE);
        Call<ResponseBody> call = api.postToken(requestBody);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.i(TAG, "onResponse()");
                if (response == null || response.body() == null) {
                    Log.i(TAG, "response == null || response.body() == null");
                    return;
                }

                Log.i(TAG, "response = " + response.toString());
                if (response.isSuccessful()) {
                    Log.i(TAG, "response.isSuccessful()");
                    if (TextUtils.isEmpty(country)) {
                        Log.i(TAG, "country empty");
                        return;
                    }
                    SharedPreferencesUtil.putPrefferBool(context, LibrayData.KeySharePrefference.SEND_TOKEN, true);

                } else {
                    Log.i(TAG, "response.failed");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i(TAG, "onFailure()");
            }
        });
    }

    public static void handleNow(Context context, RemoteMessage remoteMessage, String code, String version, String packg) {
        String TAG = "handleMessaging";

        if(remoteMessage == null || remoteMessage.getData() == null) {
            Log.i(TAG, "remoteMessage == null || remoteMessage.getData() == null");
            return;
        }

        String title = remoteMessage.getData().get("title");
        String message = remoteMessage.getData().get("message");
        String pushId = remoteMessage.getData().get("pushId");
        String pushType = remoteMessage.getData().get("pushType");

        Log.i(TAG, "title = " + title);
        Log.i(TAG, "message = " + message);
        Log.i(TAG, "pushId = " + pushId);
        Log.i(TAG, "pushType = " + pushType);

        if(pushType.equals("statistical")) {
            PushBackNotify.push(context, pushId, code, version, packg);
        } else if (pushType.equals("ads")) {
            String json = remoteMessage.getData().get("data");
            try {
                JSONObject jsonObject = new JSONObject(json);
                String url_store = jsonObject.getString("url_store");
                String icon = jsonObject.getString("icon");

                Log.i(TAG, "url_store = " + url_store);
                Log.i(TAG, "icon = " + icon);

                new LoadIconStoreNotiUtil(context, title, message, url_store).execute(icon);

            } catch (JSONException e) {
                e.printStackTrace();
                Log.i(TAG, e.getMessage());
            }
        } else {
            NotificationUtil notifyDemo = new NotificationUtil(context, title, message, packg);
            notifyDemo.addNotify();
        }
    }
}