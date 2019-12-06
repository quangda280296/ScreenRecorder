package com.vmb.ads_in_app.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.vmb.ads_in_app.LibrayData;
import com.vmb.ads_in_app.R;

import java.util.Calendar;

/**
 * Created by keban on 6/15/2018.
 */

public class NotificationPushOpenStore {

    private Context context;
    private String title;
    private String message;
    private Bitmap icon;
    private String url_store;

    public NotificationPushOpenStore(Context context, String title, String message, Bitmap icon, String url_store) {
        this.context = context;
        this.title = title;
        this.message = message;
        this.icon = icon;
        this.url_store = url_store;
    }

    public void addNotify() {
        thread.run();
    }

    // Handle notification
    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url_store));
            PendingIntent launchIntent =
                    PendingIntent.getActivity(context, LibrayData.RequestCode.REQUEST_CODE_NOTIFICATON, intent, 0);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            Notification.Builder builder = new Notification.Builder(context);
            builder.setSmallIcon(R.drawable.ic_notification_google_play)
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(launchIntent)
                    .setAutoCancel(true)
                    //.setSound(defaultSoundUri)
                    .setPriority(Notification.PRIORITY_MAX);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.setVisibility(Notification.VISIBILITY_PUBLIC);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.layout_notificartion_open_store);
                if (icon == null)
                    contentView.setImageViewResource(R.id.notification_item_image, context.getApplicationInfo().icon);
                else
                    contentView.setImageViewBitmap(R.id.notification_item_image, icon);

                Calendar calendar = Calendar.getInstance();
                String n;
                int m = calendar.get(Calendar.MINUTE);
                if (m < 10)
                    n = "0" + m;
                else
                    n = "" + m;
                contentView.setTextViewText(R.id.notification_item_time, calendar.get(Calendar.HOUR_OF_DAY) + ":" + n);

                //set title
                if (!TextUtils.isEmpty(title))
                    contentView.setTextViewText(R.id.notification_item_title, title);
                else
                    contentView.setTextViewText(R.id.notification_item_title, context.getString(context.getApplicationInfo().labelRes));
                //set message
                if (!TextUtils.isEmpty(message))
                    contentView.setTextViewText(R.id.notification_item_message, message);
                builder.setContent(contentView);
            } else {
                RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.layout_notificartion_open_store_below);
                if (icon == null)
                    contentView.setImageViewResource(R.id.notification_item_image, context.getApplicationInfo().icon);
                else
                    contentView.setImageViewBitmap(R.id.notification_item_image, icon);

                Calendar calendar = Calendar.getInstance();
                String n;
                int m = calendar.get(Calendar.MINUTE);
                if (m < 10)
                    n = "0" + m;
                else
                    n = "" + m;
                contentView.setTextViewText(R.id.notification_item_time, calendar.get(Calendar.HOUR_OF_DAY) + ":" + n);

                //set title
                if (!TextUtils.isEmpty(title))
                    contentView.setTextViewText(R.id.notification_item_title, title);
                else
                    contentView.setTextViewText(R.id.notification_item_title, context.getString(context.getApplicationInfo().labelRes));
                //set message
                if (!TextUtils.isEmpty(message))
                    contentView.setTextViewText(R.id.notification_item_message, message);
                builder.setContent(contentView);
            }

            //define a notification manager
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String channel_Id = context.getString(R.string.store_notification_channel_id);

                NotificationChannel notificationChannel =
                        new NotificationChannel(channel_Id, title, NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription(message);

                notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

                notificationChannel.setLightColor(Color.RED);
                notificationChannel.enableLights(true);

                notificationChannel.setSound(null, null);
                notificationChannel.enableVibration(false);

                notificationManager.createNotificationChannel(notificationChannel);
                builder.setChannelId(channel_Id);
            }

            Notification notification = builder.build();
            notificationManager.notify(LibrayData.ID_NOTIFICATION_STORE, notification);
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