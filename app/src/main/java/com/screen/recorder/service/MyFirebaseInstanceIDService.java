package com.screen.recorder.service;

import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.vmb.ads_in_app.util.RefreshToken;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIDService";

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        if (TextUtils.isEmpty(refreshedToken))
            return;

        Log.i(TAG, "onTokenRefresh: " + refreshedToken);
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.

        RefreshToken.RefreshTokenListener listener = RefreshToken.getInstance().getListener();
        if (listener != null) {
            if (!RefreshToken.getInstance().isRefreshed()) {
                RefreshToken.getInstance().setRefreshed(true);
                listener.onRefreshed(refreshedToken);
            }
        }
    }
}