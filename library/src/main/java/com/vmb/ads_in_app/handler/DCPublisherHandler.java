package com.vmb.ads_in_app.handler;

import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.vmb.ads_in_app.Interface.BannerLoaderListener;
import com.vmb.ads_in_app.LibrayData;
import com.vmb.ads_in_app.R;
import com.vmb.ads_in_app.model.AdsConfig;
import com.vmb.ads_in_app.util.FireAnaUtil;
import com.vmb.ads_in_app.util.NativeUtil;

public class DCPublisherHandler {
    private static DCPublisherHandler publisherUtil;

    private PublisherInterstitialAd interstitialAd;

    public static DCPublisherHandler getInstance() {
        synchronized (DCPublisherHandler.class) {
            if (publisherUtil == null) {
                publisherUtil = new DCPublisherHandler();
            }
            return publisherUtil;
        }
    }

    public void setInstance(DCPublisherHandler publisherUtil) {
        DCPublisherHandler.publisherUtil = publisherUtil;
    }

    public PublisherInterstitialAd getInterstitialAd() {
        return this.interstitialAd;
    }

    public void initBannerPublisher(final Context context, final ViewGroup banner_layout, final String adSize) {
        initBannerPublisher(context, banner_layout, adSize, null);
    }

    public void initBannerPublisher(final Context context, final ViewGroup banner_layout, final String adSize,
                                    final BannerLoaderListener bannerLoaderListener) {
        final String TAG_BANNER = "initBannerPublisher";
        Log.i(TAG_BANNER, "initBannerPublisher()");

        if (adSize.equals(LibrayData.AdsSize.NATIVE_ADS)) {
            Log.i(TAG_BANNER, "AdSize = NATIVE_ADS");

            int index_native = AdsHandler.getInstance().getAdsIndexRectangle();
            Log.i(TAG_BANNER, "index_banner = " + index_native);
            if (AdsConfig.getInstance().getAds() == null || index_native >= AdsConfig.getInstance().getAds().size()
                    || AdsConfig.getInstance().getAds().get(index_native) == null
                    || AdsConfig.getInstance().getAds().get(index_native).getKey() == null) {
                Log.i(TAG_BANNER, "Invalid");
                return;
            }

            String nativeId = AdsConfig.getInstance().getAds().get(index_native).getKey().getThumbai();
            Log.i(TAG_BANNER, "nativeId = " + nativeId);

            AdLoader adLoader = new AdLoader.Builder(context, nativeId)
                    .forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                        @Override
                        public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                            Log.i(TAG_BANNER, "onUnifiedNativeAdLoaded()");
                            // Show the ad

                            /*TemplateView template = (TemplateView) LayoutInflater
                                    .from(activity)
                                    .inflate(R.layout.layout_ad_unified, null);
                            template.setNativeAd(unifiedNativeAd);
                            banner_layout.addView(template);
                            banner_layout.setVisibility(View.VISIBLE);*/

                            final UnifiedNativeAdView adView = (UnifiedNativeAdView) LayoutInflater
                                    .from(context)
                                    .inflate(R.layout.ad_unified, null);
                            NativeUtil.populateUnifiedNativeAdView(unifiedNativeAd, adView);

                            if (banner_layout != null) {
                                if (adView != null) {
                                    ViewParent parent = adView.getParent();
                                    if (parent != null)
                                        if (parent instanceof ViewGroup)
                                            ((ViewGroup) parent).removeView(adView);
                                }
                                banner_layout.addView(adView);
                                banner_layout.setVisibility(View.VISIBLE);
                            } else {
                                AdsHandler.getInstance().setAdview(adView);
                                AdsHandler.BannerListener listener = AdsHandler.getInstance().getBannerListener();
                                if (listener != null)
                                    listener.onBannerLoaded();
                            }

                            if (bannerLoaderListener != null)
                                bannerLoaderListener.onLoaded();
                        }
                    })
                    .withAdListener(new AdListener() {
                        @Override
                        public void onAdFailedToLoad(int errorCode) {
                            // Handle the failure by logging, altering the UI, and so on.
                            switch (errorCode) {
                                case PublisherAdRequest.ERROR_CODE_INTERNAL_ERROR:
                                    Log.i(TAG_BANNER, "onAdFailedToLoad(): ERROR_CODE_INTERNAL_ERROR");
                                    break;
                                case PublisherAdRequest.ERROR_CODE_INVALID_REQUEST:
                                    Log.i(TAG_BANNER, "onAdFailedToLoad(): ERROR_CODE_INVALID_REQUEST");
                                    break;
                                case PublisherAdRequest.ERROR_CODE_NETWORK_ERROR:
                                    Log.i(TAG_BANNER, "onAdFailedToLoad(): ERROR_CODE_NETWORK_ERROR");
                                    break;
                                case PublisherAdRequest.ERROR_CODE_NO_FILL:
                                    Log.i(TAG_BANNER, "onAdFailedToLoad(): ERROR_CODE_NO_FILL");
                                    break;
                            }

                            AdsHandler.getInstance().increseAdsIndexRectangle();
                            int index = AdsHandler.getInstance().getAdsIndexRectangle();

                            if (AdsConfig.getInstance().getAds() == null || index >= AdsConfig.getInstance().getAds().size()
                                    || AdsConfig.getInstance().getAds().get(index) == null) {
                                Log.i(TAG_BANNER, "Invalid");
                                return;
                            }

                            String type = AdsConfig.getInstance().getAds().get(index).getType();
                            if (TextUtils.isEmpty(type)) {
                                Log.i(TAG_BANNER, "TextUtils.isEmpty(type)");
                                return;
                            }

                            if (type.equals("facebook"))
                                FBAdsHandler.getInstance().initBannerFB(context, banner_layout, adSize);
                            else if (type.equals("admob"))
                                AdmobHandler.getInstance().initBannerAdmob(context, banner_layout, adSize);
                            else if (type.equals("richadx"))
                                DCPublisherHandler.getInstance().initBannerPublisher(context, banner_layout, adSize);
                        }
                    })
                    .withNativeAdOptions(new NativeAdOptions.Builder()
                            // Methods in the NativeAdOptions.Builder class can be
                            // used here to specify individual options settings.
                            .build())
                    .build();

            PublisherAdRequest adRequest = new PublisherAdRequest.Builder().build();
            adLoader.loadAd(adRequest);

        } else {
            final PublisherAdView adView = new PublisherAdView(context);

            int index_banner = AdsHandler.getInstance().getAdsIndexBanner();
            Log.i(TAG_BANNER, "index_banner = " + index_banner);
            if (AdsConfig.getInstance().getAds() == null || index_banner >= AdsConfig.getInstance().getAds().size()
                    || AdsConfig.getInstance().getAds().get(index_banner) == null
                    || AdsConfig.getInstance().getAds().get(index_banner).getKey() == null) {
                Log.i(TAG_BANNER, "Invalid");
                return;
            }

            String bannerId = AdsConfig.getInstance().getAds().get(index_banner).getKey().getBanner();
            Log.i(TAG_BANNER, "bannerId = " + bannerId);

            if (adSize.equals(LibrayData.AdsSize.BANNER)) {
                adView.setAdUnitId(bannerId);
                DisplayMetrics displayMetrics = new DisplayMetrics();

                WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                wm.getDefaultDisplay().getMetrics(displayMetrics);

                int widthPixels = displayMetrics.widthPixels;
                int densityDpi = displayMetrics.densityDpi;
                Log.i(TAG_BANNER, "widthPixels = " + widthPixels);

                int widthDP = (widthPixels * DisplayMetrics.DENSITY_DEFAULT) / densityDpi;
                Log.i(TAG_BANNER, "widthDP = " + widthDP);

                if (widthDP >= 600) {
                    adView.setAdSizes(AdSize.LEADERBOARD);
                    Log.i(TAG_BANNER, "AdSize = LEADERBOARD");
                } else {
                    adView.setAdSizes(AdSize.BANNER);
                    Log.i(TAG_BANNER, "AdSize = BANNER");
                }
            }

            adView.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    Log.i(TAG_BANNER, "onAdClosed()");
                }

                @Override
                public void onAdFailedToLoad(int i) {
                    switch (i) {
                        case PublisherAdRequest.ERROR_CODE_INTERNAL_ERROR:
                            Log.i(TAG_BANNER, "onAdFailedToLoad(): ERROR_CODE_INTERNAL_ERROR");
                            break;
                        case PublisherAdRequest.ERROR_CODE_INVALID_REQUEST:
                            Log.i(TAG_BANNER, "onAdFailedToLoad(): ERROR_CODE_INVALID_REQUEST");
                            break;
                        case PublisherAdRequest.ERROR_CODE_NETWORK_ERROR:
                            Log.i(TAG_BANNER, "onAdFailedToLoad(): ERROR_CODE_NETWORK_ERROR");
                            break;
                        case PublisherAdRequest.ERROR_CODE_NO_FILL:
                            Log.i(TAG_BANNER, "onAdFailedToLoad(): ERROR_CODE_NO_FILL");
                            break;
                    }

                    AdsHandler.getInstance().increseAdsIndexBanner();
                    int index = AdsHandler.getInstance().getAdsIndexBanner();

                    if (AdsConfig.getInstance().getAds() == null || index >= AdsConfig.getInstance().getAds().size()
                            || AdsConfig.getInstance().getAds().get(index) == null) {
                        Log.i(TAG_BANNER, "Invalid");
                        return;
                    }

                    String type = AdsConfig.getInstance().getAds().get(index).getType();
                    if (TextUtils.isEmpty(type)) {
                        Log.i(TAG_BANNER, "TextUtils.isEmpty(type)");
                        return;
                    }

                    if (type.equals("facebook"))
                        FBAdsHandler.getInstance().initBannerFB(context, banner_layout, adSize);
                    else if (type.equals("admob"))
                        AdmobHandler.getInstance().initBannerAdmob(context, banner_layout, adSize);
                    else if (type.equals("richadx"))
                        DCPublisherHandler.getInstance().initBannerPublisher(context, banner_layout, adSize);
                }

                @Override
                public void onAdLeftApplication() {
                    Log.i(TAG_BANNER, "onAdLeftApplication()");
                }

                @Override
                public void onAdOpened() {
                    Log.i(TAG_BANNER, "onAdOpened()");
                }

                @Override
                public void onAdLoaded() {
                    Log.i(TAG_BANNER, "onAdLoaded()");
                    if (banner_layout != null) {
                        if (adView != null) {
                            ViewParent parent = adView.getParent();
                            if (parent != null)
                                if (parent instanceof ViewGroup)
                                    ((ViewGroup) parent).removeView(adView);
                        }
                        banner_layout.addView(adView);
                        banner_layout.setVisibility(View.VISIBLE);
                    } else {
                        AdsHandler.getInstance().setAdview(adView);
                        AdsHandler.BannerListener listener = AdsHandler.getInstance().getBannerListener();
                        if (listener != null)
                            listener.onBannerLoaded();
                    }
                }
            });

            PublisherAdRequest adRequest = new PublisherAdRequest.Builder().build();
            adView.loadAd(adRequest);
        }
    }

    public void initInterstitialDC(final Context context) {
        final String TAG_POPUP = "initInterstitialDC";
        Log.i(TAG_POPUP, "initInterstitialDC()");

        int index = AdsHandler.getInstance().getAdsIndexPopup();
        Log.i(TAG_POPUP, "index = " + index);
        if (AdsConfig.getInstance().getAds() == null || index >= AdsConfig.getInstance().getAds().size()
                || AdsConfig.getInstance().getAds().get(index) == null
                || AdsConfig.getInstance().getAds().get(index).getKey() == null) {
            Log.i(TAG_POPUP, "Invalid");
            return;
        }

        String popupId = AdsConfig.getInstance().getAds().get(index).getKey().getPopup();
        Log.i(TAG_POPUP, "popupId = " + popupId);

        interstitialAd = new PublisherInterstitialAd(context);
        interstitialAd.setAdUnitId(popupId);

        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLeftApplication() {
                Log.i(TAG_POPUP, "onAdLeftApplication()");
            }

            @Override
            public void onAdClosed() {
                Log.i(TAG_POPUP, "onAdClosed()");
                loadPopup(context);
            }

            @Override
            public void onAdLoaded() {
                Log.i(TAG_POPUP, "onAdLoaded()");

                if (AdsConfig.getInstance().getConfig() == null) {
                    Log.i(TAG_POPUP, "AdsConfig.getInstance().getConfig() == null");
                    return;
                }

                if (AdsConfig.getInstance().getConfig().getOpen_app_show_popup() == 0) {
                    Log.i(TAG_POPUP, "show_open_app == 0");
                    return;
                }

                if (!AdsHandler.getInstance().isShowPopupOpenApp()) {
                    AdsHandler.getInstance().setTypeInterstialOpenApp("richadx");
                    AdsHandler.getInstance().setInterstialOpenAppLoaded(true);
                    AdsHandler.InterstialListener listener = AdsHandler.getInstance().getInterstialListener();
                    if (listener != null)
                        listener.onLoaded();
                }
            }

            @Override
            public void onAdFailedToLoad(int i) {
                switch (i) {
                    case PublisherAdRequest.ERROR_CODE_INTERNAL_ERROR:
                        Log.i(TAG_POPUP, "onAdFailedToLoad(): ERROR_CODE_INTERNAL_ERROR");
                        break;
                    case PublisherAdRequest.ERROR_CODE_INVALID_REQUEST:
                        Log.i(TAG_POPUP, "onAdFailedToLoad(): ERROR_CODE_INVALID_REQUEST");
                        break;
                    case PublisherAdRequest.ERROR_CODE_NETWORK_ERROR:
                        Log.i(TAG_POPUP, "onAdFailedToLoad(): ERROR_CODE_NETWORK_ERROR");
                        break;
                    case PublisherAdRequest.ERROR_CODE_NO_FILL:
                        Log.i(TAG_POPUP, "onAdFailedToLoad(): ERROR_CODE_NO_FILL");
                        break;
                }

                AdsHandler.getInstance().increseAdsIndexPopup();

                int index = AdsHandler.getInstance().getAdsIndexPopup();
                if (AdsConfig.getInstance().getAds() == null || index >= AdsConfig.getInstance().getAds().size()
                        || AdsConfig.getInstance().getAds().get(index) == null) {
                    Log.i(TAG_POPUP, "Invalid");
                    return;
                }

                String type = AdsConfig.getInstance().getAds().get(index).getType();
                if (TextUtils.isEmpty(type)) {
                    Log.i(TAG_POPUP, "TextUtils.isEmpty(type)");
                    return;
                }

                if (type.equals("facebook"))
                    FBAdsHandler.getInstance().initInterstitialFB(context);
                else if (type.equals("admob"))
                    AdmobHandler.getInstance().initInterstitialAdmob(context);
                else
                    DCPublisherHandler.getInstance().initInterstitialDC(context);
            }
        });

        loadPopup(context);
    }

    private void loadPopup(Context context) {
        // Create ad request.
        PublisherAdRequest adRequestFull = new PublisherAdRequest.Builder().build();
        // Begin loading your interstitial.
        interstitialAd.loadAd(adRequestFull);
        //FireAnaUtil.logEvent(context, LibrayData.Event.LOAD_POPUP_ADS, "richadx");
        //ToastUtil.shortToast(context, "load popup Richadx");
    }

    public void displayInterstitial(Context context) {
        displayInterstitial(context, true);
    }

    public void displayInterstitial(Context context, boolean isRestartCoundDown) {
        String TAG = "displayRichadx";

        if (interstitialAd == null) {
            Log.i(TAG, "interstitialAd == null");
            return;
        }

        Log.i(TAG, "displayInterstitial()");

        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
            //FireAnaUtil.logEvent(context, LibrayData.Event.SHOW_POPUP_ADS, "richadx");
            if (isRestartCoundDown)
                AdsHandler.getInstance().restartCountDown();
            Log.i(TAG, "displayInterstitial() = true");
        } else if (interstitialAd.isLoading()) {
            Log.i(TAG, "interstitialAd.loading()");
        } else
            AdsHandler.getInstance().initInterstital(context);
    }

    /*public void setVisibility(boolean isShow) {
        if (adView != null) {
            adView.setVisibility(isShow ? View.VISIBLE : View.GONE);
        }
    }*/

    public boolean isPopupLoaded() {
        String TAG = "isPopupLoaded";
        if (interstitialAd == null) {
            Log.i(TAG, "interstitialAd == null");
            return false;
        }

        if (interstitialAd.isLoaded())
            return true;
        return false;
    }
}