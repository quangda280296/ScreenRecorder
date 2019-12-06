package com.screen.recorder.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.screen.recorder.Config;
import com.screen.recorder.activity.ZoomablePhotoActivity;
import com.vmb.ads_in_app.LibrayData;
import com.vmb.ads_in_app.R;
import com.vmb.ads_in_app.util.SharedPreferencesUtil;

/**
 * Created by keban on 6/15/2018.
 */

public class NotificationScreenshot {

    private Context context;
    private String path;

    public NotificationScreenshot(Context context, String path) {
        this.context = context;
        this.path = path;
    }

    public void addNotify() {
        thread.run();
    }

    // Handle notification
    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(context, ZoomablePhotoActivity.class);
            SharedPreferencesUtil.putPrefferString(context, Config.URL_FILE, path);
            PendingIntent launchIntent = PendingIntent.getActivity(context, 958, intent, 0);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            //set title
            String title = context.getString(context.getApplicationInfo().labelRes);

            //set message
            String message = "Tap to view your screenshot";

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setSmallIcon(context.getApplicationInfo().logo)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(launchIntent)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setPriority(Notification.PRIORITY_MAX);
                    /*.setStyle(new NotificationCompat.BigPictureStyle()
                            .bigPicture(BitmapFactory.decodeFile(ScreenRecorderService.getFileName()))
                            .bigLargeIcon(null));*/

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                builder.setVisibility(Notification.VISIBILITY_PUBLIC);

            //define a notification manager
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String channel_Id = Config.PACKAGE_NAME + "_screenshot_notification";

                NotificationChannel notificationChannel =
                        new NotificationChannel(channel_Id, title, NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription(message);

                notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

                AudioAttributes att = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();
                notificationChannel.setSound(defaultSoundUri, att);

                notificationChannel.setLightColor(Color.RED);
                notificationChannel.enableLights(true);

                notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                notificationChannel.enableVibration(true);

                notificationManager.createNotificationChannel(notificationChannel);
                builder.setChannelId(channel_Id);
            }

            Notification notification = builder.build();
            notificationManager.notify(Config.Notification.ID_SCREENSHOT, notification);
        }
    });

    private Bitmap getBitmapIcon(Bitmap bitmap) {
        int width = getSizeImage();
        return Bitmap.createScaledBitmap(bitmap, width, width, true);
    }

    private int getSizeImage() {
        return (int) context.getResources().getDimension(R.dimen.image_size);
    }
}