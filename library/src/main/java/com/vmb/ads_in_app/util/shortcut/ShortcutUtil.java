package com.vmb.ads_in_app.util.shortcut;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.vmb.ads_in_app.util.SharedPreferencesUtil;

import java.util.Arrays;

public class ShortcutUtil {

    public static void addShortcut(final Context context, String name, Bitmap bitmap, String url) {
        if (context == null || bitmap == null) {
            Log.i("ShortcutUtil", "null");
            return;
        }

        bitmap = Bitmap.createScaledBitmap(bitmap, 256, 256, true);

        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.setData(Uri.parse(url));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);

            // creare Dynamic Shortcut
            ShortcutInfo shortcut = new ShortcutInfo.Builder(context, name)
                    .setShortLabel(name)
                    .setLongLabel(name)
                    .setIcon(Icon.createWithBitmap(bitmap))
                    .setIntent(intent)
                    .build();

            shortcutManager.setDynamicShortcuts(Arrays.asList(shortcut));

            /*// create Pin Shortcut
            if (shortcutManager.isRequestPinShortcutSupported()) {
                List<ShortcutInfo> list = shortcutManager.getPinnedShortcuts();
                for (ShortcutInfo si : list) {
                    if (si.getId().equals(name))
                        return;
                }

                Intent pinnedShortcutCallbackIntent =
                        shortcutManager.createShortcutResultIntent(shortcut);

                int defaulfRequestCode = 909;
                PendingIntent successCallback =
                        PendingIntent.getBroadcast(context, defaulfRequestCode, pinnedShortcutCallbackIntent, 0);

                shortcutManager.requestPinShortcut(shortcut,
                        successCallback.getIntentSender());
            }*/

        } else {
            final Intent extra = new Intent();
            extra.putExtra("android.intent.extra.shortcut.INTENT", intent);
            extra.putExtra("android.intent.extra.shortcut.NAME", name);
            extra.putExtra("android.intent.extra.shortcut.ICON", bitmap);
            extra.putExtra("duplicate", false);
            extra.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            context.sendBroadcast(extra);
            SharedPreferencesUtil.putPrefferString(context, name, name);
        }
    }
}