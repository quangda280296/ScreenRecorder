package com.vmb.ads_in_app.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.vmb.ads_in_app.LibrayData;

public class RefreshToken {

    private static RefreshToken refreshToken;

    private RefreshTokenListener listener;
    private boolean isRefreshed = false;
    private boolean isCountryNull;

    public static RefreshToken getInstance() {
        if (refreshToken == null) {
            synchronized (RefreshToken.class) {
                refreshToken = new RefreshToken();
            }
        }
        return refreshToken;
    }

    public void destroy() {
        this.refreshToken = null;
    }

    public interface RefreshTokenListener {
        void onRefreshed(String token);
    }

    public RefreshTokenListener getListener() {
        return listener;
    }

    public void setListener(RefreshTokenListener listener) {
        this.listener = listener;
    }

    public boolean isRefreshed() {
        return isRefreshed;
    }

    public void setRefreshed(boolean refreshed) {
        isRefreshed = refreshed;
    }

    public boolean isCountryNull() {
        return isCountryNull;
    }

    public void setCountryNull(boolean countryNull) {
        isCountryNull = countryNull;
    }

    public void checkSendToken(final Context context, final String code, final String version, final String packg) {
        final String TAG = "MyFirebaseIDService";
        String savedVersion = SharedPreferencesUtil.getPrefferString(context,
                LibrayData.KeySharePrefference.VERSION_TOKEN, "");
        Log.i(TAG, "savedVersion: " + savedVersion);
        if (!savedVersion.equals(version)) {
            SharedPreferencesUtil.putPrefferString(context,
                    LibrayData.KeySharePrefference.VERSION_TOKEN, version);

            SharedPreferencesUtil.putPrefferBool
                    (context, LibrayData.KeySharePrefference.SEND_TOKEN, false);
        }

        boolean check = SharedPreferencesUtil.getPrefferBool
                (context, LibrayData.KeySharePrefference.SEND_TOKEN, false);
        if (check) {
            Log.i(TAG, "check == true");
            return;
        }

        // Get updated InstanceID token.
        final String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        if (TextUtils.isEmpty(refreshedToken)) {
            Log.i(TAG, "refreshedToken empty");
            RefreshToken.getInstance().setListener(new RefreshToken.RefreshTokenListener() {
                @Override
                public void onRefreshed(String token) {
                    Log.i(TAG, "Refreshed token: " + token);
                    TokenNotiUtil.sendTokenToServer(context, token, code, version, packg);
                }
            });
            return;
        }

        Log.i(TAG, "Refreshed token: " + refreshedToken);
        TokenNotiUtil.sendTokenToServer(context, refreshedToken, code, version, packg);
    }
}