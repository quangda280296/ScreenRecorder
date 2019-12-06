package com.vmb.ads_in_app.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.vmb.ads_in_app.LibrayData;
import com.vmb.ads_in_app.R;

/**
 * Created by keban on 6/15/2018.
 */

public class NotificationUtil {

    private Context context;
    private String title;
    private String message;
    private String packg;

    public NotificationUtil(Context context, String title, String message, String packg) {
        this.context = context;
        this.title = title;
        this.message = message;
        this.packg = packg;
    }

    public void addNotify() {
        thread.run();
    }

    // Handle notification
    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            PackageManager pm = context.getPackageManager();
            Intent intent = pm.getLaunchIntentForPackage(packg);
            intent.setAction(LibrayData.ACTION_NOTI);
            PendingIntent launchIntent =
                    PendingIntent.getActivity(context, LibrayData.RequestCode.REQUEST_CODE_NOTIFICATON, intent, 0);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Notification.Builder builder = new Notification.Builder(context);

            int logo = context.getApplicationInfo().logo;
            if (logo == 0)
                logo = R.drawable.ic_notification_normal;

            builder.setSmallIcon(logo)
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(launchIntent)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setPriority(Notification.PRIORITY_MAX);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.setVisibility(Notification.VISIBILITY_PUBLIC);
            }

            RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.layout_notificartion);
            contentView.setImageViewResource(R.id.notification_item_image, context.getApplicationInfo().icon);
            //set title
            if (!TextUtils.isEmpty(title))
                contentView.setTextViewText(R.id.notification_item_title, title);
            else
                contentView.setTextViewText(R.id.notification_item_title, context.getString(context.getApplicationInfo().labelRes));
            //set message
            if (!TextUtils.isEmpty(message))
                contentView.setTextViewText(R.id.notification_item_message, message);
            builder.setContent(contentView);

            //define a notification manager
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String channel_Id = context.getString(R.string.default_notification_channel_id);

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
            notificationManager.notify(LibrayData.ID_NOTIFICATION, notification);
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