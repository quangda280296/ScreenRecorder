package com.vmb.ads_in_app.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.provider.Settings;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.vmb.ads_in_app.LibrayData;
import com.vmb.ads_in_app.R;

import java.io.File;
import java.util.Arrays;

import okhttp3.OkHttpClient;
import okhttp3.Request;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by jacky on 11/21/17.
 */

public class ShareRateUtil {

    public final String TAG = "Share";
    public static final String MIME_TYPE_IMAGE = "image/*";

    // Share FB
    public static void shareApp(Activity activity) {
        try {
            String appPackageName = activity.getPackageName();
            String shareBody = "https://play.google.com/store/apps/details?id=" + appPackageName;
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
            activity.startActivityForResult(Intent.createChooser(sharingIntent, "Share App"),
                    LibrayData.RequestCode.REQUEST_CODE_SHARE_APP);
            FireAnaUtil.logEvent(activity, LibrayData.Event.SHARE_APP);
        } catch (Exception e) {

        }
    }

    public static void rateApp(Activity activity) {
        try {
            String appPackageName = activity.getPackageName();
            try {
                activity.startActivityForResult(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + appPackageName)), LibrayData.RequestCode.REQUEST_CODE_RATE_APP);
            } catch (Exception e) {
                activity.startActivityForResult(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)),
                        LibrayData.RequestCode.REQUEST_CODE_RATE_APP);
            }
        } catch (Exception e) {

        }
    }

    public static void shareFB(final Activity activity, final String path, final CallbackManager callbackManager,
                               final String code, final String version, final String packg) {
        final String TAG = "shareFB()";

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        if (isLoggedIn) {
            showShareFB(activity, path, callbackManager, code, version, packg);
            return;
        }

        LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList(""));
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(final LoginResult loginResult) {
                        Log.i(TAG, "onSuccess");
                        showShareFB(activity, path, callbackManager, code, version, packg);
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(activity, R.string.login_cancel, Toast.LENGTH_SHORT).show();
                        Log.i("facebookLogin()", "onCancel");
                    }

                    @Override
                    public void onError(FacebookException e) {
                        Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.i("facebookLogin()", "onError: " + e.getMessage());
                    }
                });
    }

    public static void showShareFB(final Activity activity, String path, CallbackManager callbackManager,
                                   final String code, final String version, final String packg) {
        final String TAG = "showShareFB()";

        ShareDialog shareDialog = new ShareDialog(activity);
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                ToastUtil.shortToast(activity, activity.getString(R.string.share_success));
                Log.i(TAG, "onSuccess");

                final String deviceId = DeviceUtil.getDeviceId(activity);
                final String country_code = CountryCodeUtil.getCountryCode(activity);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String timeRegister = TimeRegUtil.getTimeRegister(activity);
                            if (TextUtils.isEmpty(timeRegister))
                                timeRegister = String.valueOf(System.currentTimeMillis() / 1000);

                            String url = "http://gamemobileglobal.com/api/log_share_app.php?"
                                    + "deviceID=" + deviceId
                                    + "&code=" + code
                                    + "&version=" + version
                                    + "&country=" + country_code
                                    + "&timereg=" + timeRegister
                                    + "&package=" + packg;

                            Log.i(TAG, "url_control.php = " + url);
                            OkHttpClient client = new OkHttpClient();
                            Request request = new Request.Builder()
                                    .url(url)
                                    .build();
                            client.newCall(request).execute();

                        } catch (Exception e) {
                            Log.i(TAG, "catch Exception");
                        }
                    }
                }).start();
            }

            @Override
            public void onCancel() {
                ToastUtil.shortToast(activity, activity.getString(R.string.share_cancel));
                Log.i(TAG, "onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                ToastUtil.shortToast(activity, activity.getString(R.string.share_error) + "\n" + error.getMessage());
                Log.i(TAG, "onError: " + error.getMessage());
            }
        });

        if (ShareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent content = new ShareLinkContent.Builder()
                    .setContentUrl(Uri.parse(path))
                    /*.setShareHashtag(new ShareHashtag.Builder()
                            .setHashtag(activity.getString(R.string.app_name))
                            .build())
                    .setQuote(activity.getString(R.string.app_name))*/
                    .build();
            shareDialog.show(content, ShareDialog.Mode.AUTOMATIC);
        }
    }

    public static void shareFB(final Activity activity, final CallbackManager callbackManager,
                               final String code, final String version, final String packg) {
        final String TAG = "shareFB()";

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        if (isLoggedIn) {
            showShareFB(activity, callbackManager, code, version, packg);
            return;
        }

        LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList(""));
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(final LoginResult loginResult) {
                        Log.i(TAG, "onSuccess");
                        showShareFB(activity, callbackManager, code, version, packg);
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(activity, R.string.login_cancel, Toast.LENGTH_SHORT).show();
                        Log.i("facebookLogin()", "onCancel");
                    }

                    @Override
                    public void onError(FacebookException e) {
                        Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.i("facebookLogin()", "onError: " + e.getMessage());
                    }
                });
    }

    public static void showShareFB(final Activity activity, CallbackManager callbackManager,
                                   final String code, final String version, final String packg) {
        final String TAG = "showShareFB()";

        ShareDialog shareDialog = new ShareDialog(activity);
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                ToastUtil.shortToast(activity, activity.getString(R.string.share_success));
                Log.i(TAG, "onSuccess");

                final String deviceId = DeviceUtil.getDeviceId(activity);
                final String country_code = CountryCodeUtil.getCountryCode(activity);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String timeRegister = TimeRegUtil.getTimeRegister(activity);
                            if (TextUtils.isEmpty(timeRegister))
                                timeRegister = String.valueOf(System.currentTimeMillis() / 1000);

                            String url = "http://gamemobileglobal.com/api/log_share_app.php?"
                                    + "deviceID=" + deviceId
                                    + "&code=" + code
                                    + "&version=" + version
                                    + "&country=" + country_code
                                    + "&timereg=" + timeRegister
                                    + "&package=" + packg;

                            Log.i(TAG, "url_control.php = " + url);
                            OkHttpClient client = new OkHttpClient();
                            Request request = new Request.Builder()
                                    .url(url)
                                    .build();
                            client.newCall(request).execute();

                        } catch (Exception e) {
                            Log.i(TAG, "catch Exception");
                        }
                    }
                }).start();
            }

            @Override
            public void onCancel() {
                ToastUtil.shortToast(activity, activity.getString(R.string.share_cancel));
                Log.i(TAG, "onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                ToastUtil.shortToast(activity, activity.getString(R.string.share_error) + "\n" + error.getMessage());
                Log.i(TAG, "onError: " + error.getMessage());
            }
        });

        if (ShareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent content = new ShareLinkContent.Builder()
                    .setContentUrl(Uri.parse("https://play.google.com/store/apps/details?id=" + packg))
                    /*.setShareHashtag(new ShareHashtag.Builder()
                            .setHashtag(activity.getString(R.string.app_name) + " - Brightest Flashlight App")
                            .build())
                    .setQuote(activity.getString(R.string.app_name) + " - Brightest Flashlight App")*/
                    .build();
            shareDialog.show(content, ShareDialog.Mode.AUTOMATIC);
        }
    }

    // Share others
    public static void shareFacebook(Activity activity, Uri uri) {
        FireAnaUtil.logEvent(activity, LibrayData.Event.SHARE_IMG, "facebook");
        disableExposure();
        try {
            Intent intent = new Intent("android.intent.action.SEND");
            intent.setType(MIME_TYPE_IMAGE);
            intent.putExtra("android.intent.extra.STREAM", uri);
            intent.putExtra("android.intent.extra.TEXT", activity.getString(R.string.some_text_to_share));
            intent.setPackage("com.facebook.katana");
            activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.share_photo_to_fb)));
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.longToast(activity, activity.getString(R.string.install_fb_app_to_use));
        }
    }

    public static void shareMessengerFB(Activity activity, Uri uri, String app_name) {
        FireAnaUtil.logEvent(activity, LibrayData.Event.SHARE_IMG, "messenger");
        disableExposure();
        try {
            Intent intent = new Intent("android.intent.action.SEND");
            intent.setPackage("com.facebook.orca");
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("https://play.google.com/store/apps/details?id=");
            stringBuilder2.append(activity.getPackageName());
            String stringBuilder3 = stringBuilder2.toString();
            StringBuilder stringBuilder4 = new StringBuilder();
            stringBuilder4.append(activity.getString(R.string.created_by));
            stringBuilder4.append(app_name);
            stringBuilder4.append(" :");
            stringBuilder4.append(stringBuilder3);
            intent.putExtra("android.intent.extra.TEXT", stringBuilder4.toString());
            intent.putExtra("android.intent.extra.STREAM", uri);
            intent.setType(MIME_TYPE_IMAGE);
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.share_photo_to_fb_mess)));
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.longToast(activity, activity.getString(R.string.install_fb_mess_app_to_use));
        }
    }

    public static void shareInstagram(Activity activity, String str, String app_name) {
        FireAnaUtil.logEvent(activity, LibrayData.Event.SHARE_IMG, "instagram");
        disableExposure();
        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("https://play.google.com/store/apps/details?id=");
            stringBuilder.append(activity.getPackageName());
            String stringBuilder2 = stringBuilder.toString();
            Intent intent = new Intent("android.intent.action.SEND");
            intent.setType(MIME_TYPE_IMAGE);
            intent.putExtra("android.intent.extra.STREAM", Uri.parse(str));
            StringBuilder stringBuilder3 = new StringBuilder();
            stringBuilder3.append(activity.getString(R.string.created_by));
            stringBuilder3.append(app_name);
            stringBuilder3.append(" :");
            stringBuilder3.append(stringBuilder2);
            intent.putExtra("android.intent.extra.TEXT", stringBuilder3.toString());
            intent.setPackage("com.instagram.android");
            activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.share_photo_to_insta)));
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.longToast(activity, activity.getString(R.string.install_insta_app_to_use));
        }
    }

    public static void shareZalo(Activity activity, String str, String app_name) {
        FireAnaUtil.logEvent(activity, LibrayData.Event.SHARE_IMG, "zalo");
        disableExposure();
        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("PATH FILE: ");
            stringBuilder.append(str);
            Log.e("Share", stringBuilder.toString());
            Intent intent = new Intent("android.intent.action.SEND");
            intent.setPackage("com.zing.zalo");
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("https://play.google.com/store/apps/details?id=");
            stringBuilder2.append(activity.getPackageName());
            String stringBuilder3 = stringBuilder2.toString();
            StringBuilder stringBuilder4 = new StringBuilder();
            stringBuilder4.append(activity.getString(R.string.created_by));
            stringBuilder4.append(app_name);
            stringBuilder4.append(" :");
            stringBuilder4.append(stringBuilder3);
            intent.putExtra("android.intent.extra.TEXT", stringBuilder4.toString());
            intent.putExtra("android.intent.extra.STREAM", Uri.parse(str));
            intent.setType(MIME_TYPE_IMAGE);
            //intent.setFlags(268435456);
            activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.share_to_zalo)));
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.longToast(activity, activity.getString(R.string.install_zalo_app_to_use));
        }
    }

    public static void shareWeChat(Activity activity, String str, String app_name) {
        FireAnaUtil.logEvent(activity, LibrayData.Event.SHARE_IMG, "wechat");
        disableExposure();
        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("PATH FILE: ");
            stringBuilder.append(str);
            Log.e("Share", stringBuilder.toString());
            Intent intent = new Intent("android.intent.action.SEND");
            intent.setPackage("com.tencent.mm");
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("https://play.google.com/store/apps/details?id=");
            stringBuilder2.append(activity.getPackageName());
            String stringBuilder3 = stringBuilder2.toString();
            StringBuilder stringBuilder4 = new StringBuilder();
            stringBuilder4.append(activity.getString(R.string.created_by));
            stringBuilder4.append(app_name);
            stringBuilder4.append(" :");
            stringBuilder4.append(stringBuilder3);
            intent.putExtra("android.intent.extra.TEXT", stringBuilder4.toString());
            intent.putExtra("android.intent.extra.STREAM", Uri.parse(str));
            intent.setType(MIME_TYPE_IMAGE);
            //intent.setFlags(268435456);
            activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.share_to_wechat)));
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.longToast(activity, activity.getString(R.string.install_wechat_app_to_use));
        }
    }

    public static void shareWhatsApp(Activity activity, String str, String app_name) {
        FireAnaUtil.logEvent(activity, LibrayData.Event.SHARE_IMG, "whatsapp");
        disableExposure();
        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("PATH FILE: ");
            stringBuilder.append(str);
            Log.e("Share", stringBuilder.toString());
            Intent intent = new Intent("android.intent.action.SEND");
            intent.setPackage("com.whatsapp");
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("https://play.google.com/store/apps/details?id=");
            stringBuilder2.append(activity.getPackageName());
            String stringBuilder3 = stringBuilder2.toString();
            StringBuilder stringBuilder4 = new StringBuilder();
            stringBuilder4.append(activity.getString(R.string.created_by));
            stringBuilder4.append(app_name);
            stringBuilder4.append(" :");
            stringBuilder4.append(stringBuilder3);
            intent.putExtra("android.intent.extra.TEXT", stringBuilder4.toString());
            intent.putExtra("android.intent.extra.STREAM", Uri.parse(str));
            intent.setType(MIME_TYPE_IMAGE);
            //intent.setFlags(268435456);
            activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.share_to_whatsapp)));
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.longToast(activity, activity.getString(R.string.install_whatsapp_app_to_use));
        }
    }

    public static void shareTwitter(Activity activity, String str, String app_name) {
        FireAnaUtil.logEvent(activity, LibrayData.Event.SHARE_IMG, "twitter");
        disableExposure();
        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("PATH FILE: ");
            stringBuilder.append(str);
            Log.e("Share", stringBuilder.toString());
            Intent intent = new Intent("android.intent.action.SEND");
            intent.setPackage("com.twitter.android");
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("https://play.google.com/store/apps/details?id=");
            stringBuilder2.append(activity.getPackageName());
            String stringBuilder3 = stringBuilder2.toString();
            StringBuilder stringBuilder4 = new StringBuilder();
            stringBuilder4.append(activity.getString(R.string.created_by));
            stringBuilder4.append(app_name);
            stringBuilder4.append(" :");
            stringBuilder4.append(stringBuilder3);
            intent.putExtra("android.intent.extra.TEXT", stringBuilder4.toString());
            intent.putExtra("android.intent.extra.STREAM", Uri.parse(str));
            intent.setType(MIME_TYPE_IMAGE);
            //intent.setFlags(268435456);
            activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.share_to_twiter)));
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.longToast(activity, activity.getString(R.string.install_twitter_app_to_use));
        }
    }

    public static void shareMore(Context context, String str, String app_name) {
        FireAnaUtil.logEvent(context, LibrayData.Event.SHARE_IMG, "more");
        StringBuilder stringBuilder;
        String str2;
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("https://play.google.com/store/apps/details?id=");
        stringBuilder2.append(context.getPackageName());
        String stringBuilder3 = stringBuilder2.toString();
        stringBuilder = new StringBuilder();
        str2 = context.getString(R.string.photo_created_by);
        stringBuilder.append(str2);
        stringBuilder.append(stringBuilder3);
        shareImageAndText(context, str, app_name, stringBuilder.toString());
    }

    public static void shareImageAndText(Context context, String str, String str2, String str3) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                StrictMode.class.getMethod("disableDeathOnFileUriExposure", new Class[0]).invoke(null, new Object[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            File file = new File(str);
            str = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.getName().substring(file.getName().lastIndexOf(".") + 1));
            Intent intent = new Intent();
            intent.setAction("android.intent.action.SEND");
            intent.setType(str);
            intent.putExtra("android.intent.extra.STREAM", Uri.fromFile(file));
            intent.putExtra("android.intent.extra.SUBJECT", str2);
            intent.putExtra("android.intent.extra.TEXT", str3);
            context.startActivity(Intent.createChooser(intent, context.getResources().getString(R.string.title_share_file)));

        } catch (Exception e) {
            ToastUtil.shortToast(context, context.getString(R.string.try_again_later));
            StringBuilder s = new StringBuilder();
            s.append("shareImageAndText error = ");
            s.append(e.toString());
            Log.e("", str2.toString());
        }
    }

    @SuppressLint({"NewApi"})
    private String getDefaultSmsPackage(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return Telephony.Sms.getDefaultSmsPackage(context);
        }
        String string = Settings.Secure.getString(context.getContentResolver(), "sms_default_application");
        PackageManager manager = context.getPackageManager();
        return manager.resolveActivity(manager.getLaunchIntentForPackage(string), 0).activityInfo.packageName;
    }

    public void shareSMS(Activity activity, String str, String app_name) {
        disableExposure();
        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("PATH FILE: ");
            stringBuilder.append(str);
            Log.e("Share", stringBuilder.toString());
            Intent intent = new Intent("android.intent.action.SEND");
            intent.setPackage(getDefaultSmsPackage(activity));
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("https://play.google.com/store/apps/details?id=");
            stringBuilder2.append(activity.getPackageName());
            String stringBuilder3 = stringBuilder2.toString();
            StringBuilder stringBuilder4 = new StringBuilder();
            stringBuilder4.append(activity.getString(R.string.created_by));
            stringBuilder4.append(app_name);
            stringBuilder4.append(" :");
            stringBuilder4.append(stringBuilder3);
            intent.putExtra("sms_body", stringBuilder4.toString());
            stringBuilder4 = new StringBuilder();
            stringBuilder4.append(activity.getString(R.string.created_by));
            stringBuilder4.append(app_name);
            stringBuilder4.append(" :");
            stringBuilder4.append(stringBuilder3);
            intent.putExtra("android.intent.extra.TEXT", stringBuilder4.toString());
            intent.putExtra("android.intent.extra.STREAM", Uri.parse(str));
            intent.setType(MIME_TYPE_IMAGE);
            //intent.setFlags(268435456);
            activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.share_to_sms)));
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.longToast(activity, activity.getString(R.string.install_sms_app_to_use));
        }
    }

    public static void disableExposure() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                StrictMode.class.getMethod("disableDeathOnFileUriExposure", new Class[0]).invoke(null, new Object[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}