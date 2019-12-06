package com.vmb.ads_in_app.handler;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.Button;

import com.vmb.ads_in_app.Interface.BannerLoaderListener;
import com.vmb.ads_in_app.Interface.IRewardedListener;
import com.vmb.ads_in_app.LibrayData;
import com.vmb.ads_in_app.R;
import com.vmb.ads_in_app.activity.AdsActivity;
import com.vmb.ads_in_app.model.AdsConfig;

import java.util.concurrent.TimeUnit;

public class AdsHandler {
    private String TAG = "AdsHandler";

    private static AdsHandler adsHandler;

    public static AdsHandler getInstance() {
        synchronized (AdsHandler.class) {
            if (adsHandler == null) {
                adsHandler = new AdsHandler();
            }
            return adsHandler;
        }
    }

    private boolean isCallAPI = false;

    public boolean isCallAPI() {
        return isCallAPI;
    }

    public void setCallAPI(boolean callAPI) {
        isCallAPI = callAPI;
    }

    private RewardedVideoListener rewardedVideoListener;

    public RewardedVideoListener getRewardedVideoListener() {
        return rewardedVideoListener;
    }

    public interface RewardedVideoListener {
        void onLoaded();

        void onLoadFailed();
    }

    private InterstialListener interstialListener;

    public InterstialListener getInterstialListener() {
        return interstialListener;
    }

    public interface InterstialListener {
        void onLoaded();
    }

    private BannerListener bannerListener;

    public BannerListener getBannerListener() {
        return bannerListener;
    }

    public interface BannerListener {
        void onBannerLoaded();
    }

    private ExitDialogListener exitDialogListener;

    public void setListener(ExitDialogListener exitDialogListener) {
        this.exitDialogListener = exitDialogListener;
    }

    public interface ExitDialogListener {
        void onClickButton(boolean yes);
    }

    private GetConfigListener getConfigListener;

    public GetConfigListener getGetConfigListener() {
        return getConfigListener;
    }

    public interface GetConfigListener {
        void onGetConfig();
    }

    private UpdateListener updateListener;

    public UpdateListener getUpdateListener() {
        return updateListener;
    }

    public void setUpdateListener(UpdateListener updateListener) {
        this.updateListener = updateListener;
    }

    public interface UpdateListener {
        void onGetConfig();
    }

    private AlertDialog closeAppDialog;

    private int adsIndexBanner = 0;
    private int adsIndexRectangle = 0;
    private int adsIndexPopup = 0;
    private int adsIndexReward = 0;

    private CountDownTimer countDownTimer;
    private boolean canShowPopup = true;

    private boolean isShowPopupOpenApp = false;

    private boolean isShowLoadingScreenOpenApp;
    private String typeInterstialOpenApp;
    private boolean isInterstialOpenAppLoaded = false;

    private View adview;

    // get - set
    public void setInstance(AdsHandler adsUtils) {
        AdsHandler.adsHandler = adsUtils;
    }

    public void setAdview(View adview) {
        this.adview = adview;
    }

    public void setTypeInterstialOpenApp(String typeInterstialOpenApp) {
        this.typeInterstialOpenApp = typeInterstialOpenApp;
    }

    public void setInterstialOpenAppLoaded(boolean interstialOpenAppLoaded) {
        isInterstialOpenAppLoaded = interstialOpenAppLoaded;
    }

    public boolean isCanShowPopup() {
        return canShowPopup;
    }

    public int getAdsIndexBanner() {
        return this.adsIndexBanner;
    }

    public void increseAdsIndexBanner() {
        this.adsIndexBanner += 1;
    }

    public int getAdsIndexPopup() {
        return this.adsIndexPopup;
    }

    public void increseAdsIndexPopup() {
        this.adsIndexPopup += 1;
    }

    public int getAdsIndexReward() {
        return adsIndexReward;
    }

    public void increseAdsIndexReward() {
        this.adsIndexReward += 1;
    }

    public int getAdsIndexRectangle() {
        return this.adsIndexRectangle;
    }

    public void increseAdsIndexRectangle() {
        this.adsIndexRectangle += 1;
    }

    public boolean isShowPopupOpenApp() {
        return this.isShowPopupOpenApp;
    }

    public void setShowPopupOpenApp(boolean showPopupOpenApp) {
        this.isShowPopupOpenApp = showPopupOpenApp;
    }

    // init Banner
    public void initBanner(Activity activity) {
        initBanner(activity, LibrayData.AdsSize.BANNER, (ViewGroup) activity.findViewById(R.id.banner), null);
    }

    public void initBanner(Context context, String adSize, ViewGroup banner_layout) {
        initBanner(context, adSize, banner_layout, null);
    }

    public void initBanner(Context context, String adSize, ViewGroup banner_layout, BannerLoaderListener listener) {
        Log.i(TAG, "initBanner()");

        if (AdsConfig.getInstance().getConfig() == null) {
            Log.i(TAG, "config == null");
            return;
        }

        if (AdsConfig.getInstance().getConfig().getShow_banner_ads() == 0) {
            Log.i(TAG, "show_banner_ads == 0");
            return;
        }

        int index;
        if (adSize.equals(LibrayData.AdsSize.NATIVE_ADS))
            index = adsIndexRectangle;
        else
            index = adsIndexBanner;

        if (AdsConfig.getInstance().getAds().size() <= 0) {
            Log.i(TAG, "size <= 0");
            return;
        }

        if (index >= AdsConfig.getInstance().getAds().size()) {
            if (adSize.equals(LibrayData.AdsSize.NATIVE_ADS)) {
                adsIndexRectangle = 0;
                index = adsIndexRectangle;
            } else {
                adsIndexBanner = 0;
                index = adsIndexBanner;
            }
            Log.i(TAG, "index >= size");
        }

        if (AdsConfig.getInstance().getAds().get(index) == null) {
            Log.i(TAG, "Invalid");
            return;
        }

        String type = AdsConfig.getInstance().getAds().get(index).getType();
        if (TextUtils.isEmpty(type)) {
            Log.i(TAG, "typeBanner == null");
            return;
        }

        if (type.equals("facebook")) {
            FBAdsHandler.getInstance().initBannerFB(context, banner_layout, adSize, listener);
            Log.i(TAG, "initBanner == facebook");
        } else if (type.equals("admob")) {
            AdmobHandler.getInstance().initBannerAdmob(context, banner_layout, adSize, listener);
            Log.i(TAG, "initBanner == admob");
        } else if (type.equals("richadx")) {
            DCPublisherHandler.getInstance().initBannerPublisher(context, banner_layout, adSize, listener);
            Log.i(TAG, "initBanner == richadx");
        }
    }

    public void addBannerStartApp(final ViewGroup banner_layout) {
        Log.i(TAG, "addBannerStartApp()");
        if (adview != null) {
            ViewParent parent = adview.getParent();
            if (parent != null)
                if (parent instanceof ViewGroup)
                    ((ViewGroup) parent).removeView(adview);
            banner_layout.addView(adview);
            banner_layout.setVisibility(View.VISIBLE);
        } else {
            bannerListener = new BannerListener() {
                @Override
                public void onBannerLoaded() {
                    if (adview != null) {
                        ViewParent parent = adview.getParent();
                        if (parent != null)
                            if (parent instanceof ViewGroup)
                                ((ViewGroup) parent).removeView(adview);
                    }
                    banner_layout.addView(adview);
                    banner_layout.setVisibility(View.VISIBLE);
                }
            };
        }
    }

    public void displayPopupOpenApp(final Activity activity, final Intent intent) {
        Log.i(TAG, "displayPopupOpenApp()");

        /*if (isInterstialOpenAppLoaded) {
         *//*if (isShowLoadingScreenOpenApp) {
                Intent intent = new Intent(activity, AdsActivity.class);
                intent.putExtra(LibrayData.KeyIntentData.KEY_ADS_ACTIVITY, typeInterstialOpenApp);
                intent.putExtra(LibrayData.KeyIntentData.IS_RESTART_COUNTDOWN, false);
                activity.startActivity(intent);
            } else {*//*
            displayPopup(activity, typeInterstialOpenApp, false);
            //}
            AdsHandler.getInstance().setShowPopupOpenApp(true);
        } else {*/
        interstialListener = new InterstialListener() {
            @Override
            public void onLoaded() {
                    /*if (isShowLoadingScreenOpenApp) {
                        Intent intent = new Intent(activity, AdsActivity.class);
                        intent.putExtra(LibrayData.KeyIntentData.KEY_ADS_ACTIVITY, typeInterstialOpenApp);
                        intent.putExtra(LibrayData.KeyIntentData.IS_RESTART_COUNTDOWN, false);
                        activity.startActivity(intent);
                    } else {*/

                AdsHandler.getInstance().setShowPopupOpenApp(true);

                activity.startActivity(intent);
                activity.finish();

                displayPopup(activity, typeInterstialOpenApp, false);
                //}
            }
        };
        //}

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isShowPopupOpenApp)
                    return;

                activity.startActivity(new Intent(intent));
                activity.finish();
            }
        }, 4000);
    }

    // init Interstital
    public void initInterstital(Context context) {
        initInterstital(context, true);
    }

    public void initInterstital(final Context context, boolean isShowLoadingScreenOpenApp) {
        Log.i(TAG, "initInterstital()");
        this.isShowLoadingScreenOpenApp = isShowLoadingScreenOpenApp;

        if (AdsConfig.getInstance().getAds() == null) {
            Log.i(TAG, "Invalid");
            return;
        }

        if (AdsConfig.getInstance().getAds().size() <= 0) {
            Log.i(TAG, "size <= 0");
            return;
        }

        if (adsIndexPopup >= AdsConfig.getInstance().getAds().size()) {
            adsIndexPopup = 0;
            Log.i(TAG, "index >= size");
        }

        if (AdsConfig.getInstance().getAds().get(adsIndexPopup) == null) {
            Log.i(TAG, "Invalid");
            return;
        }

        String type = AdsConfig.getInstance().getAds().get(adsIndexPopup).getType();
        if (TextUtils.isEmpty(type)) {
            Log.i(TAG, "typePopup == null");
            return;
        }

        if (type.equals("facebook")) {
            FBAdsHandler.getInstance().initInterstitialFB(context);
            Log.i(TAG, "initInterstital == facebook");
        } else if (type.equals("admob")) {
            AdmobHandler.getInstance().initInterstitialAdmob(context);
            Log.i(TAG, "initInterstital == admob");
        } else if (type.equals("richadx")) {
            DCPublisherHandler.getInstance().initInterstitialDC(context);
            Log.i(TAG, "initInterstital == richadx");
        }
    }

    // display Interstitial
    public void displayInterstitial(Activity activity) {
        displayInterstitial(activity, true);
    }

    public void displayInterstitial(Activity activity, boolean showLoadingScreen) {
        String TAG = "displayInterstitial";
        Log.i(TAG, "displayInterstitial()");

        if (AdsConfig.getInstance().getAds() == null) {
            Log.i(TAG, "Invalid");
            return;
        }

        if (AdsConfig.getInstance().getAds().size() <= 0) {
            Log.i(TAG, "size <= 0");
            return;
        }

        if (adsIndexPopup >= AdsConfig.getInstance().getAds().size()) {
            adsIndexPopup = 0;
            Log.i(TAG, "index >= size");
            initInterstital(activity, isShowLoadingScreenOpenApp);
        }

        if (AdsConfig.getInstance().getAds().get(adsIndexPopup) == null) {
            Log.i(TAG, "Invalid");
            return;
        }

        String type = AdsConfig.getInstance().getAds().get(adsIndexPopup).getType();
        if (TextUtils.isEmpty(type)) {
            Log.i(TAG, "typePopup == null");
            return;
        }

        if (type.equals("facebook")) {
            Log.i(TAG, "typeDisplayPopup == facebook");
            if (!FBAdsHandler.getInstance().isPopupLoaded() || !canShowPopup) {
                Log.i(TAG, "FBAdsPopupLoaded == false  || canShowPopup == false");
            } else
                checkShowLoadingScreen(activity, type, showLoadingScreen);
        } else if (type.equals("admob")) {
            Log.i(TAG, "typeDisplayPopup == admob");
            if (!AdmobHandler.getInstance().isPopupLoaded() || !canShowPopup) {
                Log.i(TAG, "AdmobAdsPopupLoaded == false  || canShowPopup == false");
            } else
                checkShowLoadingScreen(activity, type, showLoadingScreen);
        } else if (type.equals("richadx")) {
            Log.i(TAG, "typeDisplayPopup == richadx");
            if (!DCPublisherHandler.getInstance().isPopupLoaded() || !canShowPopup) {
                Log.i(TAG, "!DCAdsPopupLoaded == false  || canShowPopup == false");
            } else
                checkShowLoadingScreen(activity, type, showLoadingScreen);
        }
    }

    private void checkShowLoadingScreen(Activity activity, String type, boolean showLoadingScreen) {
        String TAG = "displayInterstitial";
        if (showLoadingScreen) {
            Log.i(TAG, "showLoadingScreen == true");
            Intent intent = new Intent(activity, AdsActivity.class);
            intent.putExtra(LibrayData.KeyIntentData.KEY_ADS_ACTIVITY, type);
            activity.startActivity(intent);
        } else {
            Log.i(TAG, "showLoadingScreen == false");
            AdsHandler.getInstance().displayPopup(activity, type);
        }
    }

    public void displayPopup(Context context, String type) {
        displayPopup(context, type, true);
    }

    public void displayPopup(Context context, String type, boolean isRestartCoundDown) {
        String TAG = "displayInterstitial";
        Log.i(TAG, "displayPopup() type = " + type);

        if (type.equals("facebook"))
            FBAdsHandler.getInstance().displayInterstitial(context, isRestartCoundDown);
        else if (type.equals("admob"))
            AdmobHandler.getInstance().displayInterstitial(context, isRestartCoundDown);
        else if (type.equals("richadx"))
            DCPublisherHandler.getInstance().displayInterstitial(context, isRestartCoundDown);
    }

    // Count Down
    public void initCountDown() {
        final String TAG = "initCountDown";
        isCallAPI = true;

        Log.i(TAG, "initCountDown()");
        if (AdsConfig.getInstance().getConfig() == null) {
            Log.i(TAG, "AdsConfig.getInstance().getConfig() == null");
            return;
        }

        int time_start_show_popup = AdsConfig.getInstance().getConfig().getTime_start_show_popup();
        countDownTimer = new CountDownTimer(TimeUnit.SECONDS.toMillis(time_start_show_popup), TimeUnit.SECONDS.toMillis(1)) {
            public void onTick(long millisUntilFinished) {
                Log.i(TAG, "millisUntilFinished == " + millisUntilFinished);
            }

            public void onFinish() {
                Log.i(TAG, "onFinish()");
                canShowPopup = true;
            }
        };

        canShowPopup = false;
        countDownTimer.start();
    }

    public void restartCountDown() {
        final String TAG = "restartCountDown";

        Log.i(TAG, "restartCountDown()");
        if (AdsConfig.getInstance().getConfig() == null) {
            Log.i(TAG, "AdsConfig.getInstance().getConfig() == null");
            return;
        }

        int offset_time_show_popup = AdsConfig.getInstance().getConfig().getOffset_time_show_popup();
        countDownTimer = new CountDownTimer(TimeUnit.SECONDS.toMillis(offset_time_show_popup), TimeUnit.SECONDS.toMillis(1)) {
            public void onTick(long millisUntilFinished) {
                Log.i(TAG, "millisUntilFinished == " + millisUntilFinished);
            }

            public void onFinish() {
                Log.i(TAG, "onFinish()");
                canShowPopup = true;
            }
        };

        canShowPopup = false;
        countDownTimer.start();
    }

    private void cancelDownCount() {
        if (countDownTimer == null) {
            Log.i(TAG, "countDownTimer == null");
            return;
        }
        canShowPopup = true;
        countDownTimer.cancel();
    }

    /*// others
    public void setVisibility(boolean isShow) {
        final String TAG = "setVisibility()";

        if (AdsConfig.getInstance().getAds() == null || adsIndexPopup >= AdsConfig.getInstance().getAds().size()
                || AdsConfig.getInstance().getAds().get(adsIndexPopup) == null) {
            Log.i(TAG, "Invalid");
            return;
        }

        String type = AdsConfig.getInstance().getAds().get(adsIndexPopup).getType();
        if (TextUtils.isEmpty(type)) {
            Log.i(TAG, "TextUtils.isEmpty(type)");
            return;
        }

        if (type.equals("facebook"))
            FBAdsHandler.getInstance().setVisibility(isShow);
        else if (type.equals("admob"))
            AdmobHandler.getInstance().setVisibility(isShow);
        else
            DCPublisherHandler.getInstance().setVisibility(isShow);
    }*/

    public void initConfirmDialog(Activity activity) {
        initConfirmDialog(activity, LibrayData.AdsSize.NATIVE_ADS);
    }

    public void initConfirmDialog(final Activity activity, final String adSize) {
        Log.i(TAG, "initConfirmDialog()");

        LayoutInflater inflater = LayoutInflater.from(activity);
        final View alertLayout = inflater.inflate(R.layout.dialog_confirm_exit, null);

        final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setView(alertLayout);
        closeAppDialog = alert.create();

        Button btn_yes = alertLayout.findViewById(R.id.btn_yes);
        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (exitDialogListener != null)
                    exitDialogListener.onClickButton(true);
                else
                    activity.finish();
                closeAppDialog.dismiss();
            }
        });

        Button btn_no = alertLayout.findViewById(R.id.btn_no);
        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (exitDialogListener != null)
                    exitDialogListener.onClickButton(false);
                closeAppDialog.dismiss();
            }
        });

        closeAppDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                // Initialize a new window manager layout parameters
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

                // Copy the alert dialog window attributes to new layout parameter instance
                layoutParams.copyFrom(closeAppDialog.getWindow().getAttributes());

                // Set the width and height for the layout parameters
                // This will bet the width and height of alert dialog
                layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;

                // Apply the newly created layout parameters to the alert dialog window
                closeAppDialog.getWindow().setAttributes(layoutParams);
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (AdsConfig.getInstance().getConfig() == null) {
                    getConfigListener = new GetConfigListener() {
                        @Override
                        public void onGetConfig() {
                            ViewGroup rectangle = alertLayout.findViewById(R.id.rectangle);
                            initAdsExitDialog(activity, adSize, rectangle);
                        }
                    };
                    return;
                }

                ViewGroup rectangle = alertLayout.findViewById(R.id.rectangle);
                initAdsExitDialog(activity, adSize, rectangle);
            }
        }, 4000);
    }

    public void initAdsExitDialog(Activity activity, String adSize, ViewGroup rectangle) {
        if (AdsConfig.getInstance().getConfig() == null) {
            Log.i(TAG, "config == null");
            return;
        }

        if (AdsConfig.getInstance().getConfig().getClose_app_show_popup() == 0) {
            Log.i(TAG, "close_app_show_popup == 0");
            return;
        }

        AdsHandler.getInstance().initBanner(activity, adSize, rectangle);
    }

    public void showCofirmDialog(Activity activity) {
        showCofirmDialog(activity, null);
    }

    public void showCofirmDialog(Activity activity, ExitDialogListener exitDialogListener) {
        this.exitDialogListener = exitDialogListener;
        if (closeAppDialog == null) {
            Log.i(TAG, "closeAppDialog == null");
            activity.finish();
            return;
        }

        closeAppDialog.show();
    }

    public void destroyInstance() {
        AdsHandler.getInstance().cancelDownCount();
        AdsHandler.getInstance().setInstance(null);

        AdmobHandler.getInstance().setInstance(null);
        FBAdsHandler.getInstance().setInstance(null);
        DCPublisherHandler.getInstance().setInstance(null);
    }

    public void initRewardedVideo(Context context) {
        initRewardedVideo(context, false);
    }

    public void initRewardedVideo(Context context, boolean isRequired) {
        Log.i(TAG, "initRewardedVideo()");

        if (AdsConfig.getInstance().getAds() == null) {
            Log.i(TAG, "Invalid");
            return;
        }

        if (AdsConfig.getInstance().getAds().size() <= 0) {
            Log.i(TAG, "size <= 0");
            return;
        }

        if (adsIndexReward >= AdsConfig.getInstance().getAds().size()) {
            adsIndexReward = 0;
            Log.i(TAG, "index >= size");
        }

        if (AdsConfig.getInstance().getAds().get(adsIndexReward) == null) {
            Log.i(TAG, "Invalid");
            return;
        }

        String type = AdsConfig.getInstance().getAds().get(adsIndexReward).getType();
        if (TextUtils.isEmpty(type)) {
            Log.i(TAG, "typePopup == null");
            return;
        }

        if (type.equals("admob")) {
            AdmobHandler.getInstance().initRewardedVideo(context, isRequired);
            Log.i(TAG, "initRewardedVideo == admob");
        } else if (type.equals("facebook")) {
            FBAdsHandler.getInstance().initRewardedVideo(context, isRequired);
            Log.i(TAG, "initRewardedVideo == facebook");
        }
    }

    public boolean checkRewardedVideo(Context context) {
        String TAG = "checkRewardedVideo";
        Log.i(TAG, "checkRewardedVideo()");

        if (AdsConfig.getInstance().getAds() == null) {
            Log.i(TAG, "Invalid");
            return false;
        }

        if (AdsConfig.getInstance().getAds().size() <= 0) {
            Log.i(TAG, "size <= 0");
            return false;
        }

        if (adsIndexReward >= AdsConfig.getInstance().getAds().size()) {
            adsIndexReward = 0;
            Log.i(TAG, "index >= size");
            initRewardedVideo(context, true);
        }

        if (AdsConfig.getInstance().getAds().get(adsIndexReward) == null) {
            Log.i(TAG, "Invalid");
            return false;
        }

        String type = AdsConfig.getInstance().getAds().get(adsIndexReward).getType();
        if (TextUtils.isEmpty(type)) {
            Log.i(TAG, "typePopup == null");
            return false;
        }

        if (type.equals("admob")) {
            Log.i(TAG, "checkRewardedVideo == admob");
            if (AdmobHandler.getInstance().isRewardedVideoLoaded() || AdmobHandler.getInstance().isRewardedVideoLoading())
                return true;
        } else if (type.equals("facebook")) {
            Log.i(TAG, "checkRewardedVideo == facebook");
            if (FBAdsHandler.getInstance().isRewardedVideoLoaded() || FBAdsHandler.getInstance().isRewardedVideoLoading())
                return true;
        }

        return false;
    }

    public void displayRewardedVideo(final Context context, final IRewardedListener listener) {
        Log.i(TAG, "displayRewardedVideo()");

        if (AdsConfig.getInstance().getAds() == null) {
            Log.i(TAG, "Invalid");
            if (listener != null)
                listener.onFailed();
            return;
        }

        if (AdsConfig.getInstance().getAds().size() <= 0) {
            Log.i(TAG, "size <= 0");
            return;
        }

        if (adsIndexReward >= AdsConfig.getInstance().getAds().size()) {
            adsIndexReward = 0;
            Log.i(TAG, "index >= size");
            initRewardedVideo(context, true);
        }

        if (AdsConfig.getInstance().getAds().get(adsIndexReward) == null) {
            Log.i(TAG, "Invalid");
            if (listener != null)
                listener.onFailed();
            return;
        }

        String type = AdsConfig.getInstance().getAds().get(adsIndexReward).getType();
        if (TextUtils.isEmpty(type)) {
            Log.i(TAG, "typePopup == null");
            if (listener != null)
                listener.onFailed();
            return;
        }

        if (type.equals("admob")) {
            Log.i(TAG, "displayRewardedVideo == admob");
            if (AdmobHandler.getInstance().isRewardedVideoLoaded())
                AdmobHandler.getInstance().displayRewardedVideo(listener);
            else {
                if (!AdmobHandler.getInstance().isRewardedVideoLoading()) {
                    if (listener != null)
                        listener.onFailed();
                    return;
                }

                showLoading(context, listener);
            }
        } else if (type.equals("facebook")) {
            Log.i(TAG, "displayRewardedVideo == facebook");
            if (FBAdsHandler.getInstance().isRewardedVideoLoaded())
                FBAdsHandler.getInstance().displayRewardedVideo(listener);
            else {
                if (!FBAdsHandler.getInstance().isRewardedVideoLoading()) {
                    if (listener != null)
                        listener.onFailed();
                    return;
                }

                showLoading(context, listener);
            }
        }
    }

    public void showLoading(Context context, final IRewardedListener listener) {
        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(context.getString(R.string.loading_ads));
        dialog.show();
        rewardedVideoListener = new RewardedVideoListener() {
            @Override
            public void onLoaded() {
                rewardedVideoListener = null;
                dialog.dismiss();
                AdmobHandler.getInstance().displayRewardedVideo(listener);
            }

            @Override
            public void onLoadFailed() {
                rewardedVideoListener = null;
                dialog.dismiss();
                if (listener != null)
                    listener.onFailed();
            }
        };
    }
}