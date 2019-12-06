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

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSettings;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.facebook.ads.RewardedVideoAd;
import com.facebook.ads.RewardedVideoAdListener;
import com.vmb.ads_in_app.Interface.BannerLoaderListener;
import com.vmb.ads_in_app.Interface.IRewardedListener;
import com.vmb.ads_in_app.LibrayData;
import com.vmb.ads_in_app.R;
import com.vmb.ads_in_app.model.AdsConfig;
import com.vmb.ads_in_app.util.FireAnaUtil;
import com.vmb.ads_in_app.util.NativeUtil;

public class FBAdsHandler {
    private static FBAdsHandler fbAdsUtils;

    private InterstitialAd interstitialAd;
    private boolean isInterstitialLoading = false;

    private RewardedVideoAd rewardedVideoAd;
    private boolean isRewardedVideoLoading = false;

    private IRewardedListener iRewardedListener;

    public static FBAdsHandler getInstance() {
        synchronized (FBAdsHandler.class) {
            if (fbAdsUtils == null) {
                fbAdsUtils = new FBAdsHandler();
            }
            return fbAdsUtils;
        }
    }

    public void setInstance(FBAdsHandler fbAdsUtils) {
        FBAdsHandler.fbAdsUtils = fbAdsUtils;
    }

    public InterstitialAd getInterstitialAd() {
        return this.interstitialAd;
    }

    public boolean isRewardedVideoLoading() {
        return isRewardedVideoLoading;
    }

    public void initBannerFB(final Context context, final ViewGroup banner_layout, final String adSize) {
        initBannerFB(context, banner_layout, adSize, null);
    }

    public void initBannerFB(final Context context, final ViewGroup banner_layout, final String adSize,
                             final BannerLoaderListener bannerLoaderListener) {
        final String TAG_BANNER = "initBannerFacebook";
        Log.i(TAG_BANNER, "initBannerFB()");

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

            final NativeAd nativeAd = new NativeAd(context, nativeId);
            nativeAd.setAdListener(new NativeAdListener() {
                @Override
                public void onMediaDownloaded(Ad ad) {
                    Log.i(TAG_BANNER, "onMediaDownloaded");
                }

                @Override
                public void onError(Ad ad, AdError adError) {
                    Log.i(TAG_BANNER, "onError(): " + adError.getErrorMessage());

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

                @Override
                public void onAdLoaded(Ad ad) {
                    Log.i(TAG_BANNER, "onAdLoaded");
                    NativeAdLayout adView = (NativeAdLayout) LayoutInflater
                            .from(context)
                            .inflate(R.layout.fb_native_ad_view, null);
                    NativeUtil.inflateAd(context, nativeAd, adView);

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

                @Override
                public void onAdClicked(Ad ad) {
                    Log.i(TAG_BANNER, "onAdClicked");
                }

                @Override
                public void onLoggingImpression(Ad ad) {
                    Log.i(TAG_BANNER, "onLoggingImpression");
                }
            });
            // Request an ad
            nativeAd.loadAd();

        } else {
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

            AdView adView = null;
            if (adSize.equals(LibrayData.AdsSize.BANNER)) {
                DisplayMetrics displayMetrics = new DisplayMetrics();

                WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                wm.getDefaultDisplay().getMetrics(displayMetrics);

                int widthPixels = displayMetrics.widthPixels;
                int densityDpi = displayMetrics.densityDpi;
                Log.i(TAG_BANNER, "widthPixels = " + widthPixels);

                int widthDP = (widthPixels * DisplayMetrics.DENSITY_DEFAULT) / densityDpi;
                Log.i(TAG_BANNER, "widthDP = " + widthDP);

                if (widthDP >= 600) {
                    adView = new AdView(context, bannerId, AdSize.BANNER_HEIGHT_90);
                    Log.i(TAG_BANNER, "AdSize = LARGE_BANNER");
                } else {
                    adView = new AdView(context, bannerId, AdSize.BANNER_HEIGHT_50);
                    Log.i(TAG_BANNER, "AdSize = BANNER");
                }
            }

            final AdView finalAdView = adView;
            adView.setAdListener(new AdListener() {
                @Override
                public void onError(Ad ad, AdError adError) {
                    Log.i(TAG_BANNER, "onError(): " + adError.getErrorMessage());

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
                public void onAdLoaded(Ad ad) {
                    Log.i(TAG_BANNER, "onAdLoaded()");
                    if (banner_layout != null) {
                        if (finalAdView != null) {
                            ViewParent parent = finalAdView.getParent();
                            if (parent != null)
                                if (parent instanceof ViewGroup)
                                    ((ViewGroup) parent).removeView(finalAdView);
                        }
                        banner_layout.addView(finalAdView);
                        banner_layout.setVisibility(View.VISIBLE);
                    } else {
                        AdsHandler.getInstance().setAdview(finalAdView);
                        AdsHandler.BannerListener listener = AdsHandler.getInstance().getBannerListener();
                        if (listener != null)
                            listener.onBannerLoaded();
                    }
                }

                @Override
                public void onAdClicked(Ad ad) {
                    Log.i(TAG_BANNER, "onAdClicked()");
                }

                @Override
                public void onLoggingImpression(Ad ad) {
                    Log.i(TAG_BANNER, "onLoggingImpression()");
                }
            });

            adView.loadAd();
        }
    }

    public void initInterstitialFB(final Context context) {
        final String TAG_POPUP = "initInterstitialFB";
        Log.i(TAG_POPUP, "initInterstitialFB()");

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

        interstitialAd = new InterstitialAd(context, popupId);
        interstitialAd.setAdListener(new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {
                Log.i(TAG_POPUP, "onInterstitialDisplayed()");
            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                Log.i(TAG_POPUP, "onInterstitialDismissed()");
                loadPopup(context);
            }

            @Override
            public void onAdLoaded(Ad ad) {
                isInterstitialLoading = false;
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
                    AdsHandler.getInstance().setTypeInterstialOpenApp("facebook");
                    AdsHandler.getInstance().setInterstialOpenAppLoaded(true);
                    AdsHandler.InterstialListener listener = AdsHandler.getInstance().getInterstialListener();
                    if (listener != null)
                        listener.onLoaded();
                }
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Ad clicked callback
                Log.i(TAG_POPUP, "onAdClicked()");
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                Log.i(TAG_POPUP, "onLoggingImpression()");
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                isInterstitialLoading = false;
                Log.i(TAG_POPUP, "onError(): " + adError.getErrorMessage());

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
                else if (type.equals("richadx"))
                    DCPublisherHandler.getInstance().initInterstitialDC(context);
            }
        });

        loadPopup(context);
    }

    private void loadPopup(Context context) {
        isInterstitialLoading = true;
        // load interstitial ads
        interstitialAd.loadAd();
        //FireAnaUtil.logEvent(context, LibrayData.Event.LOAD_POPUP_ADS, "facebook");
        //ToastUtil.shortToast(context, "load popup Facebook");
    }

    public void displayInterstitial(Context context) {
        displayInterstitial(context, true);
    }

    public void displayInterstitial(Context context, boolean isRestartCoundDown) {
        String TAG = "displayFB";

        if (interstitialAd == null) {
            Log.i(TAG, "interstitialAd == null");
            return;
        }

        Log.i(TAG, "displayInterstitial()");

        if (interstitialAd.isAdLoaded()) {
            interstitialAd.show();
            //FireAnaUtil.logEvent(context, LibrayData.Event.SHOW_POPUP_ADS, "facebook");
            if (isRestartCoundDown)
                AdsHandler.getInstance().restartCountDown();
            Log.i(TAG, "displayInterstitial() = true");
        } else if (isInterstitialLoading) {
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

        if (interstitialAd.isAdLoaded())
            return true;
        return false;
    }

    public void initRewardedVideo(final Context context, final boolean isRequired) {
        final String TAG_POPUP = "initRewardedVideoFB";
        Log.i(TAG_POPUP, "initRewardedVideo()");

        if (!isRequired)
            if (rewardedVideoAd != null)
                return;

        int index = AdsHandler.getInstance().getAdsIndexReward();
        Log.i(TAG_POPUP, "index = " + index);
        if (AdsConfig.getInstance().getAds() == null || index >= AdsConfig.getInstance().getAds().size()
                || AdsConfig.getInstance().getAds().get(index) == null
                || AdsConfig.getInstance().getAds().get(index).getKey() == null) {
            Log.i(TAG_POPUP, "Invalid");
            return;
        }

        final String rewardId = AdsConfig.getInstance().getAds().get(index).getKey().getVideo();
        Log.i(TAG_POPUP, "rewardId = " + rewardId);

        rewardedVideoAd = new RewardedVideoAd(context, rewardId);

        rewardedVideoAd.setAdListener(new RewardedVideoAdListener() {
            @Override
            public void onAdLoaded(Ad ad) {
                Log.i(TAG_POPUP, "onAdLoaded()");
                isRewardedVideoLoading = false;
                AdsHandler.RewardedVideoListener listener = AdsHandler.getInstance().getRewardedVideoListener();
                if (listener != null)
                    listener.onLoaded();
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Rewarded Video ad impression - the event will fire when the
                // video starts playing
                Log.d(TAG_POPUP, "Rewarded video ad impression logged!");
            }

            @Override
            public void onRewardedVideoClosed() {
                Log.i(TAG_POPUP, "onRewardedVideoClosed()");
            }

            @Override
            public void onAdClicked(Ad ad) {
                Log.i(TAG_POPUP, "onAdClicked()");
            }

            @Override
            public void onError(Ad ad, AdError error) {
                isRewardedVideoLoading = false;
                Log.i(TAG_POPUP, "Rewarded video ad failed to load: " + error.getErrorMessage());

                AdsHandler.getInstance().increseAdsIndexReward();

                int index = AdsHandler.getInstance().getAdsIndexReward();
                if (AdsConfig.getInstance().getAds() == null || index >= AdsConfig.getInstance().getAds().size()
                        || AdsConfig.getInstance().getAds().get(index) == null) {
                    Log.i(TAG_POPUP, "Invalid");
                    AdsHandler.RewardedVideoListener listener = AdsHandler.getInstance().getRewardedVideoListener();
                    if (listener != null)
                        listener.onLoadFailed();
                    return;
                }

                String type = AdsConfig.getInstance().getAds().get(index).getType();
                if (TextUtils.isEmpty(type)) {
                    Log.i(TAG_POPUP, "TextUtils.isEmpty(type)");
                    AdsHandler.RewardedVideoListener listener = AdsHandler.getInstance().getRewardedVideoListener();
                    if (listener != null)
                        listener.onLoadFailed();
                    return;
                }

                if (type.equals("facebook"))
                    FBAdsHandler.getInstance().initRewardedVideo(context, isRequired);
                else if (type.equals("admob"))
                    AdmobHandler.getInstance().initRewardedVideo(context, isRequired);
            }

            @Override
            public void onRewardedVideoCompleted() {
                Log.i(TAG_POPUP, "onRewardedVideoCompleted()");
                if (iRewardedListener != null)
                    iRewardedListener.onRewarded();
                loadRewardedVideo();
            }
        });

        loadRewardedVideo();
    }

    private void loadRewardedVideo() {
        isRewardedVideoLoading = true;
        // Begin loading your interstitial.
        AdSettings.addTestDevice("0e1df4dc-487c-43f3-9941-4f687f1e6364");
        rewardedVideoAd.loadAd();
    }

    public boolean isRewardedVideoLoaded() {
        String TAG = "isRewardedVideoLoaded";
        if (rewardedVideoAd == null) {
            Log.i(TAG, "rewardedVideoAd == null");
            return false;
        }

        if (rewardedVideoAd.isAdLoaded())
            return true;
        return false;
    }

    public void displayRewardedVideo(IRewardedListener listener) {
        String TAG = "displayRewardedFB";
        Log.i(TAG, "displayRewardedVideo");
        this.iRewardedListener = listener;

        if (rewardedVideoAd == null) {
            Log.i(TAG, "rewardedVideoAd == null");
            return;
        }

        if (rewardedVideoAd.isAdLoaded()) {
            rewardedVideoAd.show();
            Log.i(TAG, "displayRewardedVideo()");
        } else {
            Log.i(TAG, "displayRewardedVideo.loadFailed()");
        }
    }
}