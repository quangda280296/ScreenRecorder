package com.screen.recorder.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import com.screen.recorder.Config;
import com.screen.recorder.activity.MainActivity;
import com.screen.recorder.activity.RequestActivity;

public class NotificationIntentService extends IntentService {
    String TAG = "NotificationIntentService";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public NotificationIntentService() {
        super("notificationIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable final Intent intent) {
        Log.i(TAG, "onHandleIntent");

        switch (intent.getAction()) {
            case Config.Action.ACTION_RECORD:
                Log.i(TAG, Config.Action.ACTION_RECORD);
                Handler record = new Handler(Looper.getMainLooper());
                record.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, Config.Action.ACTION_RECORD);
                        if (ScreenRecorderService.getRecorder().getResultData() == null) {
                            Intent launchIntent = new Intent(getApplicationContext(), RequestActivity.class);
                            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            launchIntent.setAction(Config.Action.ACTION_RECORD);
                            startActivity(launchIntent);
                        } else {
                            Intent start = new Intent(getApplicationContext(), ScreenRecorderService.class);
                            start.setAction(Config.Action.ACTION_RECORD);
                            if (Build.VERSION.SDK_INT >= 26) {
                                startForegroundService(start);
                            } else {
                                startService(start);
                            }
                        }

                        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                        sendBroadcast(it);
                    }
                });
                break;

            case Config.Action.ACTION_HOME:
                Log.i(TAG, Config.Action.ACTION_HOME);
                Handler icon = new Handler(Looper.getMainLooper());
                icon.post(new Runnable() {
                    @Override
                    public void run() {
                        Intent launchIntent = new Intent(getApplicationContext(), MainActivity.class);
                        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(launchIntent);

                        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                        sendBroadcast(it);
                    }
                });
                break;

            case Config.Action.ACTION_SCREENSHOT:
                Log.i(TAG, Config.Action.ACTION_SCREENSHOT);
                Handler shot = new Handler(Looper.getMainLooper());
                shot.post(new Runnable() {
                    @Override
                    public void run() {
                        if (ScreenRecorderService.getRecorder().getResultData() == null) {
                            Intent launchIntent = new Intent(getApplicationContext(), RequestActivity.class);
                            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            launchIntent.setAction(Config.Action.ACTION_SCREENSHOT);
                            startActivity(launchIntent);
                        } else {
                            if (ScreenRecorderService.getRecorder().isStart()) {
                                Intent launchIntent = new Intent(getApplicationContext(), RequestActivity.class);
                                launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                launchIntent.setAction(Config.Action.ACTION_SCREENSHOT);
                                startActivity(launchIntent);
                                return;
                            }

                            Intent intent = new Intent(getApplicationContext(), ScreenRecorderService.class);
                            intent.setAction(Config.Action.ACTION_SCREENSHOT);
                            if (Build.VERSION.SDK_INT >= 26) {
                                startForegroundService(intent);
                            } else {
                                startService(intent);
                            }
                        }

                        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                        sendBroadcast(it);
                    }
                });
                break;

            case Config.Action.ACTION_CLOSE:
                Log.i(TAG, Config.Action.ACTION_CLOSE);
                Intent close = new Intent(getApplicationContext(), ScreenRecorderService.class);
                close.setAction(Config.Action.ACTION_CLOSE);
                if (Build.VERSION.SDK_INT >= 26) {
                    startForegroundService(close);
                } else {
                    startService(close);
                }
                break;

            case Config.Action.ACTION_PAUSE:
                Log.i(TAG, Config.Action.ACTION_PAUSE);
                if (ScreenRecorderService.getRecorder().isPause()) {
                    Intent resume = new Intent(getApplicationContext(), ScreenRecorderService.class);
                    resume.setAction(Config.Action.ACTION_RESUME);
                    if (Build.VERSION.SDK_INT >= 26) {
                        startForegroundService(resume);
                    } else {
                        startService(resume);
                    }
                } else {
                    Intent pause = new Intent(getApplicationContext(), ScreenRecorderService.class);
                    pause.setAction(Config.Action.ACTION_PAUSE);
                    if (Build.VERSION.SDK_INT >= 26) {
                        startForegroundService(pause);
                    } else {
                        startService(pause);
                    }
                }
                break;

            case Config.Action.ACTION_STOP:
                Log.i(TAG, Config.Action.ACTION_STOP);
                Intent stop = new Intent(getApplicationContext(), ScreenRecorderService.class);
                stop.setAction(Config.Action.ACTION_STOP);
                if (Build.VERSION.SDK_INT >= 26) {
                    startForegroundService(stop);
                } else {
                    startService(stop);
                }
                break;

            default:
                break;
        }
    }
}