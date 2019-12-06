package com.vmb.ads_in_app.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PrintKeyHash {

    public static String print(Context context) {
        String TAG = "printKeyHash";
        String key = "KeyHash empty";

        if (context == null)
            return key;
        try {
            //getting application package name, as defined in manifest
            String packageName = context.getPackageName();

            //Retriving package info
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);

            Log.i(TAG, "package_name = " + context.getPackageName());

            for (android.content.pm.Signature signature : packageInfo.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                key = new String(Base64.encode(md.digest(), 0));

                // String key = new String(Base64.encodeBytes(md.digest()));
                Log.i(TAG, "key_hash = " + key);
            }
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e(TAG, "Name not found" + e1.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "No such an algorithm" + e.toString());
        } catch (Exception e) {
            Log.e(TAG, "Exception" + e.toString());
        }

        return key;
    }
}
