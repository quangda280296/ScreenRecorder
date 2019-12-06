package com.vmb.ads_in_app;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.vmb.ads_in_app.Interface.IAPIControl;
import com.vmb.ads_in_app.Interface.IUpdateNewVersion;
import com.vmb.ads_in_app.handler.AdsHandler;
import com.vmb.ads_in_app.model.AdsConfig;
import com.vmb.ads_in_app.util.CountryCodeUtil;
import com.vmb.ads_in_app.util.DeviceUtil;
import com.vmb.ads_in_app.util.FireAnaUtil;
import com.vmb.ads_in_app.util.NetworkUtil;
import com.vmb.ads_in_app.util.RetrofitInitiator;
import com.vmb.ads_in_app.util.SharedPreferencesUtil;
import com.vmb.ads_in_app.util.TimeRegUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetConfig {

    public static void callAPI(final Context context, String code, String version, String packg) {
        callAPI(context, LibrayData.AdsSize.BANNER, code, version, packg);
    }

    public static synchronized void callAPI(final Context context, final String adSize, String code, String version, String packg) {
        final String TAG = "callAPI()";
        //final List<String> app_package_list = GetPackages.getAll(activity);

        if (AdsHandler.getInstance().isCallAPI()) {
            Log.i(TAG, "isCallAPI = true");
            return;
        } else
            Log.i(TAG, "isCallAPI = false");

        if (!NetworkUtil.isNetworkAvailable(context)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!NetworkUtil.isNetworkAvailable(context)) {
                        FireAnaUtil.logEvent(context, LibrayData.Event.OPEN_APP, "open_app_not_internet");
                        //ToastUtil.longToast(context, "push log: no internet");
                    }
                }
            }, 10000);

        } else {
            AdsHandler.getInstance().setCallAPI(true);

            //ToastUtil.longToast(context, "push log: internet connected");

            String deviceID = DeviceUtil.getDeviceId(context);
            String timereg = TimeRegUtil.getTimeRegister(context);
            String country_code = CountryCodeUtil.getCountryCode(context);
            String phone_name = DeviceUtil.getDeviceName();
            String os_version = DeviceUtil.getDeviceOS();

            /*Log.i(TAG, "deviceID = " + deviceID);
            Log.i(TAG, "code = " + code);
            Log.i(TAG, "version = " + version);
            Log.i(TAG, "timereg = " + timereg);
            Log.i(TAG, "packg = " + packg);
            Log.i(TAG, "country_code = " + country_code);
            Log.i(TAG, "phone_name = " + phone_name);
            Log.i(TAG, "os_version = " + os_version);*/

            IAPIControl api = RetrofitInitiator.createService(IAPIControl.class, LibrayData.Url.URL_BASE);
            Call<AdsConfig> call = api.getAds(deviceID, code, version, country_code, timereg, packg, phone_name, os_version);
            call.enqueue(new Callback<AdsConfig>() {
                @Override
                public void onResponse(Call<AdsConfig> call, Response<AdsConfig> response) {
                    Log.i(TAG, "onResponse()");
                    if (response == null) {
                        Log.i(TAG, "response == null");
                        initAdsWhileServerDown(context, adSize);
                        return;
                    }

                    if (response.isSuccessful()) {
                        Log.i(TAG, "response.isSuccessful()");

                        if (response.body() == null) {
                            Log.i(TAG, "response.body() null || activity.isFinishing()");
                            return;
                        }

                        Log.i("test", "count = ");

                        AdsConfig.setInstance(response.body());
                        handle(context, adSize);

                        Gson gson = new Gson();
                        String json = gson.toJson(response.body());
                        Log.i(TAG, "json = " + json);

                        // Write to a file
                        try {
                            FileOutputStream fout = context.openFileOutput(LibrayData.FileName.FILE_ADS_CONFIG, Activity.MODE_PRIVATE);
                            OutputStreamWriter osw = new OutputStreamWriter(fout);
                            osw.write(json);
                            osw.flush();
                            osw.close();

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            Log.i(TAG, e.getMessage());
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.i(TAG, e.getMessage());
                        }

                    } else {
                        Log.i(TAG, "response.failed");
                        initAdsWhileServerDown(context, adSize);
                    }
                }

                @Override
                public void onFailure(Call<AdsConfig> call, Throwable t) {
                    Log.i(TAG, "onFailure()");
                    initAdsWhileServerDown(context, adSize);
                }
            });

            FireAnaUtil.logEvent(context, LibrayData.Event.OPEN_APP, "open_app_have_internet");
        }

        int NUMBER_OPEN_APP = SharedPreferencesUtil.getPrefferInt(context, LibrayData.UserProperties.NUMBER_OPEN_APP, 0);
        NUMBER_OPEN_APP += 1;
        SharedPreferencesUtil.putPrefferInt(context, LibrayData.UserProperties.NUMBER_OPEN_APP, NUMBER_OPEN_APP);
        Log.i("NUMBER_OPEN_APP", NUMBER_OPEN_APP + "");
        FireAnaUtil.setProperty(context, LibrayData.UserProperties.NUMBER_OPEN_APP, NUMBER_OPEN_APP);
    }

    private static void initAdsWhileServerDown(Context context, String adSize) {
        String TAG = "callAPI()";
        Log.i(TAG, "initAdsWhileServerDown()");

        // Read from a file
        try {
            FileInputStream fin = context.openFileInput(LibrayData.FileName.FILE_ADS_CONFIG);
            InputStreamReader isr = new InputStreamReader(fin);

            char[] inputBuffer = new char[10];
            int charRead;
            String readString = "";

            //---Start reading from file---
            while ((charRead = isr.read(inputBuffer)) > 0) {
                readString += String.copyValueOf(inputBuffer, 0, charRead);
                inputBuffer = new char[10];
            }

            Log.i(TAG, "readString = " + readString);

            Gson gson = new Gson();
            AdsConfig config = gson.fromJson(readString, AdsConfig.class);

            AdsConfig.setInstance(config);
            handle(context, adSize);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i(TAG, e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, e.getMessage());
        }
    }

    private static void handle(Context context, String adSize) {
        // init Countdown
        AdsHandler.getInstance().initInterstital(context);
        //AdsHandler.getInstance().initBanner(context, adSize, null);
        AdsHandler.getInstance().initCountDown();

        AdsHandler.GetConfigListener getConfigListener = AdsHandler.getInstance().getGetConfigListener();
        if (getConfigListener != null)
            getConfigListener.onGetConfig();

        AdsHandler.UpdateListener updateListener = AdsHandler.getInstance().getUpdateListener();
        if (updateListener != null)
            updateListener.onGetConfig();
    }

    public static void init(final IUpdateNewVersion listener, final Activity activity) {
        init(listener, activity, (ViewGroup) activity.findViewById(R.id.banner));
    }

    public static void init(final IUpdateNewVersion listener, final Activity activity, final ViewGroup banner_layout) {
        AdsHandler.getInstance().addBannerStartApp(banner_layout);
        AdsHandler.getInstance().initConfirmDialog(activity);

        if (AdsConfig.getInstance().getConfig() == null) {
            AdsHandler.getInstance().setUpdateListener(new AdsHandler.UpdateListener() {
                @Override
                public void onGetConfig() {
                    onUpdate(listener);
                }
            });
            return;
        }

        onUpdate(listener);
    }

    public static void onUpdate(IUpdateNewVersion listener) {
        if (AdsConfig.getInstance().getUpdate_status() != 0) {
            if (AdsConfig.getInstance().getUpdate_status() == 2)
                listener.onGetConfig(true);
            else if (AdsConfig.getInstance().getUpdate_status() == 1)
                listener.onGetConfig(false);
        }
    }
}