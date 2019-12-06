package com.screen.recorder.handler;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;

import com.screen.recorder.Config;
import com.screen.recorder.R;
import com.screen.recorder.service.NotificationIntentService;
import com.screen.recorder.service.ScreenRecorderService;

/**
 * Created by keban on 6/15/2018.
 */

public class NotificationHandler {

    private Context context;

    public NotificationHandler(Context context) {
        this.context = context;
    }

    public Notification addNotify() {
        return addNotify(true);
    }

    public Notification addNotify(boolean isNotify) {
        String TAG = "NotificationHandler";

        Notification.Builder builder = new Notification.Builder(context);
        builder.setSmallIcon(R.drawable.ic_header_menu)
                .setWhen(System.currentTimeMillis())
                .setOngoing(true)
                .setCategory(NotificationCompat.CATEGORY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setVisibility(Notification.VISIBILITY_SECRET);
        }

        RemoteViews contentView;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            contentView = new RemoteViews(context.getPackageName(), R.layout.layout_interactive_notificartion);
            builder.setCustomContentView(contentView);
        } else {
            contentView = new RemoteViews(context.getPackageName(), R.layout.layout_interactive_notificartion_below);
            builder.setContent(contentView);
        }

        if (ScreenRecorderService.getRecorder().isStart()) {
            contentView.setViewVisibility(R.id.layout_record, View.GONE);
            contentView.setViewVisibility(R.id.layout_home, View.GONE);
            contentView.setViewVisibility(R.id.layout_screen_shot, View.VISIBLE);
            contentView.setViewVisibility(R.id.layout_close, View.GONE);
            contentView.setViewVisibility(R.id.layout_pause, View.VISIBLE);
            contentView.setViewVisibility(R.id.layout_stop, View.VISIBLE);
            if (ScreenRecorderService.getRecorder().isPause())
                contentView.setImageViewResource(R.id.img_pause, R.drawable.ic_resume);
            else
                contentView.setImageViewResource(R.id.img_pause, R.drawable.ic_pause);
        } else {
            contentView.setViewVisibility(R.id.layout_record, View.VISIBLE);
            contentView.setViewVisibility(R.id.layout_home, View.VISIBLE);
            contentView.setViewVisibility(R.id.layout_screen_shot, View.VISIBLE);
            contentView.setViewVisibility(R.id.layout_close, View.VISIBLE);
            contentView.setViewVisibility(R.id.layout_pause, View.GONE);
            contentView.setViewVisibility(R.id.layout_stop, View.GONE);
        }

        // adding action to the notification button
        Intent record = new Intent(context, NotificationIntentService.class);
        record.setAction(Config.Action.ACTION_RECORD);
        contentView.setOnClickPendingIntent(R.id.layout_record, PendingIntent.getService(context,
                Config.RequestCode.ICON_INTERACT_RECORD, record, PendingIntent.FLAG_UPDATE_CURRENT));

        // adding action to the notification button
        Intent home = new Intent(context, NotificationIntentService.class);
        home.setAction(Config.Action.ACTION_HOME);
        contentView.setOnClickPendingIntent(R.id.layout_home, PendingIntent.getService(context,
                Config.RequestCode.ICON_INTERACT_HOME, home, PendingIntent.FLAG_UPDATE_CURRENT));
        contentView.setOnClickPendingIntent(R.id.layout_app_noti, PendingIntent.getService(context,
                Config.RequestCode.ICON_INTERACT_HOME, home, PendingIntent.FLAG_UPDATE_CURRENT));

        // adding action to the notification button
        Intent screenshot = new Intent(context, NotificationIntentService.class);
        screenshot.setAction(Config.Action.ACTION_SCREENSHOT);
        contentView.setOnClickPendingIntent(R.id.layout_screen_shot, PendingIntent.getService(context,
                Config.RequestCode.ICON_INTERACT_SCREENSHOT, screenshot, PendingIntent.FLAG_UPDATE_CURRENT));

        // adding action to the notification button
        Intent close = new Intent(context, NotificationIntentService.class);
        close.setAction(Config.Action.ACTION_CLOSE);
        contentView.setOnClickPendingIntent(R.id.layout_close, PendingIntent.getService(context,
                Config.RequestCode.ICON_INTERACT_CLOSE, close, PendingIntent.FLAG_UPDATE_CURRENT));

        // adding action to the notification button
        Intent pause = new Intent(context, NotificationIntentService.class);
        pause.setAction(Config.Action.ACTION_PAUSE);
        contentView.setOnClickPendingIntent(R.id.layout_pause, PendingIntent.getService(context,
                Config.RequestCode.ICON_INTERACT_PAUSE, pause, PendingIntent.FLAG_UPDATE_CURRENT));

        // adding action to the notification button
        Intent stop = new Intent(context, NotificationIntentService.class);
        stop.setAction(Config.Action.ACTION_STOP);
        contentView.setOnClickPendingIntent(R.id.layout_stop, PendingIntent.getService(context,
                Config.RequestCode.ICON_INTERACT_STOP, stop, PendingIntent.FLAG_UPDATE_CURRENT));

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channel_Id = Config.PACKAGE_NAME + "_interactive_notification";

            NotificationChannel notificationChannel =
                    new NotificationChannel(channel_Id, channel_Id, NotificationManager.IMPORTANCE_LOW);
            notificationChannel.setDescription("ScreenRecorder");

            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableLights(false);

            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            notificationChannel.enableVibration(false);

            notificationManager.createNotificationChannel(notificationChannel);
            builder.setChannelId(channel_Id);
        }

        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notification.flags |= Notification.FLAG_ONGOING_EVENT;

        if (isNotify)
            notificationManager.notify(Config.Notification.ID_INTERACTIVE, notification);
        return notification;
    }
}