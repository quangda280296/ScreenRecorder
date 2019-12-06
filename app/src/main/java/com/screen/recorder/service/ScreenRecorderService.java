package com.screen.recorder.service;
/*
 * ScreenRecordingSample
 * Sample project to cature and save audio from internal and video from screen as MPEG4 file.
 *
 * Copyright (c) 2015 saki t_saki@serenegiant.com
 *
 * File name: ScreenRecorderService.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * All files in the folder are under this Apache License, Version 2.0.
 */

import android.Manifest;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.display.VirtualDisplay;
import android.media.AudioManager;
import android.media.MediaScannerConnection;
import android.media.ToneGenerator;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.screen.recorder.Config;
import com.screen.recorder.R;
import com.screen.recorder.activity.MainActivity;
import com.screen.recorder.activity.RequestActivity;
import com.screen.recorder.handler.NotificationHandler;
import com.screen.recorder.media.ImageTransmogrifier;
import com.screen.recorder.media.MediaAudioEncoder;
import com.screen.recorder.media.MediaEncoder;
import com.screen.recorder.media.MediaMuxerWrapper;
import com.screen.recorder.media.MediaScreenEncoder;
import com.screen.recorder.model.Recorder;
import com.screen.recorder.utils.NotificationScreenshot;
import com.screen.recorder.utils.NotificationVideo;
import com.screen.recorder.widget.bubble.BubbleBaseLayout;
import com.screen.recorder.widget.bubble.BubbleLayout;
import com.screen.recorder.widget.bubble.BubbleTrashLayout;
import com.screen.recorder.widget.bubble.BubblesLayoutCoordinator;
import com.screen.recorder.widget.bubble.BubblesManager;
import com.vmb.ads_in_app.util.SharedPreferencesUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ScreenRecorderService extends Service {

    private static final String TAG = "ScreenRecorderService";
    private static final String SCREENSHOT_TYPE = "image/png";

    public static Recorder recorder;

    public static Recorder getRecorder() {
        if (recorder == null) {
            synchronized (Recorder.class) {
                recorder = new Recorder();
            }
        }
        return recorder;
    }

    private Object sSync;
    private MediaMuxerWrapper sMuxer;

    private MediaProjectionManager mMediaProjectionManager;
    private WindowManager mWindowManager;
    private MediaProjection projection;

    private VirtualDisplay mVirtualDisplay;
    private MediaProjection screenshotProjection;

    private ToneGenerator beeper;
    private HandlerThread handlerThread;

    private ImageTransmogrifier it;
    private Handler handle;

    private int width;
    private int height;
    private List<View> listSubButton;

    public WindowManager getWindowManager() {
        if (mWindowManager == null)
            mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        return mWindowManager;
    }

    public Handler getHandle() {
        return (handle);
    }

    private ScreenRecorderServiceBinder binder = new ScreenRecorderServiceBinder();

    public ScreenRecorderServiceBinder getBinder() {
        return binder;
    }

    public class ScreenRecorderServiceBinder extends Binder {
        public ScreenRecorderServiceBinder() {

        }

        public ScreenRecorderService getService() {
            return ScreenRecorderService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "onCreate:");

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationHandler notificationHandler = new NotificationHandler(getApplicationContext());
            startForeground(Config.Notification.ID_INTERACTIVE, notificationHandler.addNotify(false));
        } else {
            NotificationHandler handler = new NotificationHandler(getApplicationContext());
            handler.addNotify();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                sSync = new Object();
                beeper = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
                handlerThread = new HandlerThread(getClass().getSimpleName(),
                        android.os.Process.THREAD_PRIORITY_BACKGROUND);
                mMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
                handlerThread.start();
                handle = new Handler(handlerThread.getLooper());
                ScreenRecorderService.getRecorder().setStart(false);
                ScreenRecorderService.getRecorder().setPause(false);
                setting();
            }
        }).start();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getApplicationContext())) {

        } else {
            addFloatingButton();
        }
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy:");
        if (sMuxer != null) {
            sMuxer.stopRecording();
            sMuxer = null;
        }
        recorder = null;
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "onBind:");
        return binder;
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        Log.v(TAG, "onStartCommand:intent=" + intent);
        int result = START_STICKY;

        if (intent == null) {
            Log.e(TAG, "intent == null");
            return result;
        }

        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            Log.e(TAG, "TextUtils.isEmpty(action)");
            return result;
        }

        int result_code = intent.getIntExtra(Config.EXTRA_RESULT_CODE, -999);
        if (result_code != -999)
            ScreenRecorderService.getRecorder().setResultCode(result_code);

        Intent result_data = intent.getParcelableExtra(Config.EXTRA_RESULT_INTENT);
        if (result_data != null)
            ScreenRecorderService.getRecorder().setResultData(result_data);

        if (Config.Action.ACTION_CLOSE.equals(action)) {
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(Config.Notification.ID_INTERACTIVE);
            stopSelf();
        } else if (Config.Action.ACTION_RECORD.equals(action)) {
            if (ScreenRecorderService.getRecorder().isStart())
                return result;

            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                if (getRecorder().isAudio()) {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
                        startCapturing();
                } else
                    startCapturing();
            } else {
                Handler record = new Handler(Looper.getMainLooper());
                record.post(new Runnable() {
                    @Override
                    public void run() {
                        Intent launchIntent = new Intent(getApplicationContext(), RequestActivity.class);
                        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        launchIntent.setAction(Config.Action.ACTION_RECORD);
                        startActivity(launchIntent);
                    }
                });
            }
            //updateStatus();
        } else if (Config.Action.ACTION_STOP.equals(action) || TextUtils.isEmpty(action)) {
            stopScreenRecord();
            //updateStatus();
            result = START_NOT_STICKY;
        } /*else if (ACTION_QUERY_STATUS.equals(action)) {
            if (!updateStatus()) {
                stopSelf();
                result = START_NOT_STICKY;
            }
        }*/ else if (Config.Action.ACTION_PAUSE.equals(action)) {
            pauseScreenRecord();
        } else if (Config.Action.ACTION_RESUME.equals(action)) {
            resumeScreenRecord();
        } else if (Config.Action.ACTION_SCREENSHOT.equals(action)) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                capturingScreen();
            else {
                Handler cap = new Handler(Looper.getMainLooper());
                cap.post(new Runnable() {
                    @Override
                    public void run() {
                        Intent launchIntent = new Intent(getApplicationContext(), RequestActivity.class);
                        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        launchIntent.setAction(Config.Action.ACTION_SCREENSHOT);
                        startActivity(launchIntent);
                    }
                });
            }
        }
        return result;
    }

    private void setting() {
        Log.v(TAG, "Change Setting");

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        width = metrics.widthPixels;
        height = metrics.heightPixels;

        int min = Math.min(width, height);
        String resolution = "";
        String quality = "";
        if (min >= 1080) {
            resolution = "1080P";
            quality = "High";
        } else if (min < 1080 && min >= 720) {
            resolution = "720P";
            quality = "Medium";
        } else if (min < 720 && min >= 640) {
            resolution = "640P";
            quality = "Medium";
        } else if (min < 640 && min >= 480) {
            resolution = "480P";
            quality = "Low";
        } else if (min < 480 && min >= 320) {
            resolution = "320P";
            quality = "Low";
        } else {
            resolution = "240P";
            quality = "Low";
        }

        ScreenRecorderService.getRecorder().setResolution(SharedPreferencesUtil.getPrefferString(getApplicationContext(),
                Config.KeySharePrefference.RESOLUTION, resolution));
        ScreenRecorderService.getRecorder().setQuality(SharedPreferencesUtil.getPrefferString(getApplicationContext(),
                Config.KeySharePrefference.QUALITY, quality));
        ScreenRecorderService.getRecorder().setFps(SharedPreferencesUtil.getPrefferString(getApplicationContext(),
                Config.KeySharePrefference.FPS, "30FPS"));
        ScreenRecorderService.getRecorder().setOrientation(SharedPreferencesUtil.getPrefferString(getApplicationContext(),
                Config.KeySharePrefference.ORIENTATION, "Auto"));
        ScreenRecorderService.getRecorder().setAudio(SharedPreferencesUtil.getPrefferBool(getApplicationContext(),
                Config.KeySharePrefference.AUDIO, true));
    }

    /**
     * caturing screen as .png file
     */
    private void capturingScreen() {
        // Create a screenshot folder
        String pathScreenshots = Environment.getExternalStorageDirectory() + "/" +
                Config.Directory.ROOT_DIRECTORY + "/" + Config.Directory.SCREENSHOT_DIRECTORY + "/";
        File screenshot_folder = new File(pathScreenshots);
        if (!screenshot_folder.exists())
            screenshot_folder.mkdirs();

        screenshotProjection = mMediaProjectionManager.getMediaProjection(ScreenRecorderService.getRecorder().getResultCode(),
                ScreenRecorderService.getRecorder().getResultData());
        it = new ImageTransmogrifier(ScreenRecorderService.this);

        MediaProjection.Callback cb = new MediaProjection.Callback() {
            @Override
            public void onStop() {
                mVirtualDisplay.release();
            }
        };

        screenshotProjection.registerCallback(cb, handle);

        mVirtualDisplay = screenshotProjection.createVirtualDisplay("Screenshot",
                it.getWidth(), it.getHeight(),
                getResources().getDisplayMetrics().densityDpi,
                Config.VIRT_DISPLAY_FLAGS, it.getSurface(), null, handle);
    }

    public synchronized void processImage(final byte[] png) {
        DateFormat df = new SimpleDateFormat("ddMMyyyyHHmmss");
        Calendar myCalendar = Calendar.getInstance();
        String date = df.format(myCalendar.getTime());
        final String fileName = Environment.getExternalStorageDirectory() + "/" +
                Config.Directory.ROOT_DIRECTORY + "/" + Config.Directory.SCREENSHOT_DIRECTORY
                + "/" + "Scrshot_" + date + ".png";

        new Thread() {
            @Override
            public void run() {
                File output = new File(fileName);
                ScreenRecorderService.getRecorder().setFileName(fileName);

                try {
                    FileOutputStream fos = new FileOutputStream(output);

                    fos.write(png);
                    fos.flush();
                    fos.getFD().sync();
                    fos.close();

                    MediaScannerConnection.scanFile(ScreenRecorderService.this,
                            new String[]{output.getAbsolutePath()},
                            new String[]{SCREENSHOT_TYPE}, null);

                } catch (Exception e) {
                    Log.e(TAG, "Exception writing out screenshot", e);
                }
            }
        }.start();

        beeper.startTone(ToneGenerator.TONE_PROP_ACK);
        ScreenRecorderService.getRecorder().screenshotCaptured(getApplicationContext());

        // you should not wait here
        NotificationScreenshot review = new NotificationScreenshot(getApplicationContext(), fileName);
        review.addNotify();

        if (projection != null) {
            projection.stop();
            projection = null;
        }
        if (screenshotProjection != null) {
            screenshotProjection.stop();
            screenshotProjection = null;
        }
        if (mVirtualDisplay != null)
            mVirtualDisplay.release();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getApplicationContext()))
            return;

        addScreenshot();
    }

    /**
     * start screen recording as .mp4 file
     */
    private void startCapturing() {
        ScreenRecorderService.getRecorder().setStart(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getApplicationContext()))
            recordingScreen();
        else {
            initBubbleView();
            addFloatingButton();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void recordingScreen() {
        Log.v(TAG, "recordingScreen:sMuxer=" + sMuxer);

        // Create a video folder
        String pathVideos = Environment.getExternalStorageDirectory() + "/" +
                Config.Directory.ROOT_DIRECTORY + "/" + Config.Directory.VIDEO_DIRECTORY + "/";
        File video_folder = new File(pathVideos);
        if (!video_folder.exists())
            video_folder.mkdirs();

        synchronized (sSync) {
            if (sMuxer == null) {
                // get MediaProjection
                projection = mMediaProjectionManager.getMediaProjection(ScreenRecorderService.getRecorder().getResultCode(),
                        ScreenRecorderService.getRecorder().getResultData());
                if (projection != null) {
                    // FPS
                    int FPS = 30;
                    switch (ScreenRecorderService.getRecorder().getFps()) {
                        case "60FPS":
                            FPS = 60;
                            break;

                        case "30FPS":
                            FPS = 30;
                            break;

                        case "15FPS":
                            FPS = 15;
                            break;

                        default:
                            break;
                    }

                    // bitrate
                    int bitrate = 6800000;
                    switch (ScreenRecorderService.getRecorder().getQuality()) {
                        case "Ultra":
                            bitrate = 12000000;
                            break;

                        case "High":
                            bitrate = 6800000;
                            break;

                        case "Medium":
                            bitrate = 3500000;
                            break;

                        case "Low":
                            bitrate = 1000000;
                            break;

                        default:
                            break;
                    }

                    // quality
                    final DisplayMetrics metrics = getResources().getDisplayMetrics();
                    int width = metrics.widthPixels;
                    int height = metrics.heightPixels;
                    Log.v(TAG, "width = " + width);
                    Log.v(TAG, "height = " + height);

                    float dimen = 1080f;
                    switch (ScreenRecorderService.getRecorder().getResolution()) {
                        case "1080P":
                            dimen = 1080f;
                            break;

                        case "720P":
                            dimen = 720f;
                            break;

                        case "640P":
                            dimen = 640f;
                            break;

                        case "480P":
                            dimen = 480f;
                            break;

                        case "320P":
                            dimen = 320f;
                            break;

                        case "240P":
                            dimen = 240f;
                            break;

                        default:
                            break;
                    }

                    Log.v(TAG, "dimen = " + dimen);

                    if (width > height) {
                        float scale = (float) height / dimen;
                        width = (int) ((float) width / scale);
                        height = (int) ((float) height / scale);
                        if (width % 2 != 0)
                            width += 1;
                        if (height % 2 != 0)
                            height += 1;
                        Log.v(TAG, "width = " + width);
                        Log.v(TAG, "height = " + height);

                    } else {
                        float scale = (float) width / dimen;
                        width = (int) ((float) width / scale);
                        height = (int) ((float) height / scale);
                        if (width % 2 != 0)
                            width += 1;
                        if (height % 2 != 0)
                            height += 1;
                        Log.v(TAG, "width = " + width);
                        Log.v(TAG, "height = " + height);
                    }

                    // orientation
                    int orientation = 0;
                    switch (ScreenRecorderService.getRecorder().getOrientation()) {
                        /*case "Auto":
                            orientation = 0;
                            break;*/

                        case "Portrait":
                            /*switch (getResources().getConfiguration().orientation) {
                                case Configuration.ORIENTATION_PORTRAIT:
                                    orientation = 0;
                                    break;

                                case Configuration.ORIENTATION_LANDSCAPE:
                                    orientation = 270;
                                    break;
                            }*/
                            if (width > height) {
                                int temp = width;
                                width = height;
                                height = temp;
                            }
                            break;

                        case "Landscape":
                            /*switch (getResources().getConfiguration().orientation) {
                                case Configuration.ORIENTATION_PORTRAIT:
                                    orientation = 90;
                                    break;

                                case Configuration.ORIENTATION_LANDSCAPE:
                                    orientation = 0;
                                    break;
                            }*/
                            if (width < height) {
                                int temp = width;
                                width = height;
                                height = temp;
                            }
                            break;

                        default:
                            break;
                    }

                    Log.v(TAG, String.format("startRecording:(%d,%d)(%d,%d)",
                            metrics.widthPixels, metrics.heightPixels, width, height));
                    try {
                        DateFormat df = new SimpleDateFormat("ddMMyyyyHHmmss");
                        Calendar myCalendar = Calendar.getInstance();
                        String date = df.format(myCalendar.getTime());
                        String fileName = Environment.getExternalStorageDirectory() + "/" +
                                Config.Directory.ROOT_DIRECTORY + "/" + Config.Directory.VIDEO_DIRECTORY
                                + "/" + "ScrRecord_" + date + ".mp4";

                        /*File file = new File(fileName);
                        if (!file.exists())
                            file.createNewFile();*/
                        ScreenRecorderService.getRecorder().setFileName(fileName);

                        sMuxer = new MediaMuxerWrapper(fileName);
                        // if you record audio only, ".m4a" is also OK.
                        // for screen capturing
                        new MediaScreenEncoder(sMuxer, mMediaEncoderListener,
                                projection, width, height, metrics.densityDpi, bitrate, FPS, orientation);
                        if (ScreenRecorderService.getRecorder().isAudio()) {
                            // for audio capturing
                            new MediaAudioEncoder(sMuxer, mMediaEncoderListener);
                        }
                        sMuxer.prepare();
                        sMuxer.startRecording();

                        startRecording();
                        ScreenRecorderService.getRecorder().startRecording(getApplicationContext());

                        if (Build.VERSION.SDK_INT >= 26) {
                            NotificationHandler notificationHandler = new NotificationHandler(getApplicationContext());
                            startForeground(Config.Notification.ID_INTERACTIVE, notificationHandler.addNotify(false));
                        } else {
                            NotificationHandler handler = new NotificationHandler(getApplicationContext());
                            handler.addNotify();
                        }

                    } catch (final IOException e) {
                        Log.e(TAG, "recordingScreen:", e);
                    }
                }
            }
        }
    }

    /**
     * stop screen recording
     */
    private void stopScreenRecord() {
        Log.v(TAG, "stopScreenRecord:sMuxer=" + sMuxer);
        synchronized (sSync) {
            if (sMuxer != null) {
                ScreenRecorderService.getRecorder().setStart(false);
                sMuxer.stopRecording();
                sMuxer = null;
                stopRecording();
                ScreenRecorderService.getRecorder().stopRecording(getApplicationContext());

                if (Build.VERSION.SDK_INT >= 26) {
                    NotificationHandler notificationHandler = new NotificationHandler(getApplicationContext());
                    startForeground(Config.Notification.ID_INTERACTIVE, notificationHandler.addNotify(false));
                } else {
                    NotificationHandler handler = new NotificationHandler(getApplicationContext());
                    handler.addNotify();
                }

                // you should not wait here
                NotificationVideo review = new NotificationVideo(getApplicationContext(),
                        ScreenRecorderService.getRecorder().getFileName());
                review.addNotify();
            }
        }
        /*stopForeground(true*//*removeNotification*//*);
		if (mNotificationManager != null) {
			mNotificationManager.cancel(NOTIFICATION);
			mNotificationManager = null;
		}*/
        //stopSelf();
    }

    private void pauseScreenRecord() {
        synchronized (sSync) {
            if (sMuxer != null) {
                ScreenRecorderService.getRecorder().setPause(true);
                sMuxer.pauseRecording();
                pause();
                ScreenRecorderService.getRecorder().pause(getApplicationContext());

                if (Build.VERSION.SDK_INT >= 26) {
                    NotificationHandler notificationHandler = new NotificationHandler(getApplicationContext());
                    startForeground(Config.Notification.ID_INTERACTIVE, notificationHandler.addNotify(false));
                } else {
                    NotificationHandler handler = new NotificationHandler(getApplicationContext());
                    handler.addNotify();
                }
            }
        }
    }

    private void resumeScreenRecord() {
        synchronized (sSync) {
            if (sMuxer != null) {
                ScreenRecorderService.getRecorder().setPause(false);
                sMuxer.resumeRecording();
                resume();
                ScreenRecorderService.getRecorder().resume(getApplicationContext());

                if (Build.VERSION.SDK_INT >= 26) {
                    NotificationHandler notificationHandler = new NotificationHandler(getApplicationContext());
                    startForeground(Config.Notification.ID_INTERACTIVE, notificationHandler.addNotify(false));
                } else {
                    NotificationHandler handler = new NotificationHandler(getApplicationContext());
                    handler.addNotify();
                }
            }
        }
    }

    /**
     * callback methods from encoder
     */
    private static final MediaEncoder.MediaEncoderListener mMediaEncoderListener = new MediaEncoder.MediaEncoderListener() {
        @Override
        public void onPrepared(final MediaEncoder encoder) {
            Log.v(TAG, "onPrepared:encoder=" + encoder);
        }

        @Override
        public void onStopped(final MediaEncoder encoder) {
            Log.v(TAG, "onStopped:encoder=" + encoder);
        }
    };

    int count_seconds;

    private void initBubbleView() {
        final View mCdView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_countdown, null);
        final TextView lbl_count_down = mCdView.findViewById(R.id.lbl_count_down);

        //Create Layout param
        final WindowManager.LayoutParams params;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params = new WindowManager.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        } else {
            params = new WindowManager.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }

        //Specify position bubble
        params.gravity = Gravity.CENTER;

        //Add bubble view to window
        mWindowManager.addView(mCdView, params);

        count_seconds = 3;
        final Handler test = new Handler();
        test.post(new Runnable() {
            @Override
            public void run() {
                lbl_count_down.setText(count_seconds + "");
                YoYo.with(Techniques.ZoomIn)
                        .duration(500)
                        .playOn(lbl_count_down);
                count_seconds--;
                if (count_seconds < 0) {
                    mWindowManager.removeViewImmediate(mCdView);
                    recordingScreen();
                } else
                    test.postDelayed(this, 1000);
            }
        });
    }

    private void addScreenshot() {
        String path = SharedPreferencesUtil.getPrefferString(getApplicationContext(), Config.URL_FILE, "");
        if (TextUtils.isEmpty(path)) {
            return;
        }

        final View mCdView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_screenshot, null);
        final ImageView img_screenshot = mCdView.findViewById(R.id.img_screenshot);
        img_screenshot.setImageURI(Uri.fromFile(new File(path)));

        //Create Layout param
        final WindowManager.LayoutParams params;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params = new WindowManager.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        } else {
            params = new WindowManager.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }

        //Specify position bubble
        params.gravity = Gravity.CENTER;

        //Add bubble view to window
        mWindowManager.addView(mCdView, params);

        YoYo.with(Techniques.ZoomOut)
                .duration(1400)
                .playOn(img_screenshot);
        Handler test = new Handler();
        test.postDelayed(new Runnable() {
            @Override
            public void run() {
                mWindowManager.removeView(mCdView);
            }
        }, 1200);
    }

    boolean isAddButton = false;

    ImageView img_app;
    TextView lbl_time;

    private int time;

    private Handler handler;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            time += 1;

            final int m = time / 60;
            final int s = time % 60;

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    String minutes = m + "";
                    if (m < 10)
                        minutes = "0" + minutes;

                    String seconds = s + "";
                    if (s < 10)
                        seconds = "0" + seconds;

                    if (lbl_time != null)
                        lbl_time.setText(minutes + " : " + seconds);
                }
            });

            if (handler != null)
                handler.postDelayed(this, 1000);
        }
    };

    public void resume() {
        if (handler != null)
            handler.post(runnable);
    }

    public void pause() {
        if (handler != null)
            handler.removeCallbacks(runnable);
    }

    public void startRecording() {
        time = 0;
        if (img_app != null)
            img_app.setVisibility(View.GONE);
        if (lbl_time != null) {
            lbl_time.setText(getString(R.string._00_00));
            lbl_time.setVisibility(View.VISIBLE);
        }

        handler = new Handler();
        if (handler != null)
            handler.post(runnable);
    }

    public void stopRecording() {
        if (handler != null)
            handler.removeCallbacks(runnable);

        if (lbl_time != null) {
            lbl_time.setText(R.string._00_00);
            lbl_time.setVisibility(View.GONE);
        }
        if (img_app != null)
            img_app.setVisibility(View.VISIBLE);
    }

    boolean isOpen = false;

    public void addFloatingButton() {
        if (isAddButton)
            return;

        isAddButton = true;
        listSubButton = new ArrayList<>();
        BubblesManager bubblesManager = new BubblesManager.Builder(this)
                .setTrashLayout(R.layout.bubble_trash_layout)
                .build();
        bubblesManager.initialize(getBinder());

        bubbleView = (BubbleLayout) LayoutInflater
                .from(getApplicationContext())
                .inflate(R.layout.layout_floating, null);

        img_app = bubbleView.findViewById(R.id.img_app);
        lbl_time = bubbleView.findViewById(R.id.lbl_time);

        bubbleView.setOnBubbleRemoveListener(new BubbleLayout.OnBubbleRemoveListener() {
            @Override
            public void onBubbleRemoved(BubbleLayout bubble) {
                isAddButton = false;
            }
        });

        bubbleView.setOnBubbleClickListener(new BubbleLayout.OnBubbleClickListener() {
            @Override
            public void onBubbleClick(final BubbleLayout bubble) {
                if (isOpen) {
                    removeSubButton();
                } else {
                    isOpen = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (getRecorder().isStart()) {
                                if (getRecorder().isPause()) {
                                    setupPause(bubble);
                                } else {
                                    setupRecording(bubble);
                                }
                            } else {
                                setupNotRecording(bubble);
                            }
                        }
                    }, 500);
                }
            }

            @Override
            public void onTouch() {
                removeSubButton();
            }
        });

        bubblesManager.addBubble(bubbleView, 60, 20);
    }

    public void removeSubButton() {
        isOpen = false;
        for (View v : listSubButton)
            mWindowManager.removeViewImmediate(v);
        listSubButton.clear();
    }

    public void setupPause(BubbleLayout bubble) {
        int x = bubble.getViewParams().x;
        int y = bubble.getViewParams().y;
        if (x <= 0) {
            addSubButton(x + 100, y - 100, R.drawable.btn_resume, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeSubButton();
                    Intent resume = new Intent(getApplicationContext(), ScreenRecorderService.class);
                    resume.setAction(Config.Action.ACTION_RESUME);
                    if (Build.VERSION.SDK_INT >= 26) {
                        startForegroundService(resume);
                    } else {
                        startService(resume);
                    }
                }
            });
            addSubButton(x + 150, y, R.drawable.btn_screenshots, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeSubButton();
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
                        }
                    });
                }
            });
            addSubButton(x + 100, y + 100, R.drawable.btn_stop, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeSubButton();
                    Intent stop = new Intent(getApplicationContext(), ScreenRecorderService.class);
                    stop.setAction(Config.Action.ACTION_STOP);
                    if (Build.VERSION.SDK_INT >= 26) {
                        startForegroundService(stop);
                    } else {
                        startService(stop);
                    }
                }
            });
        } else {
            addSubButton(x - 100, y - 100, R.drawable.btn_resume, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeSubButton();
                    Intent resume = new Intent(getApplicationContext(), ScreenRecorderService.class);
                    resume.setAction(Config.Action.ACTION_RESUME);
                    if (Build.VERSION.SDK_INT >= 26) {
                        startForegroundService(resume);
                    } else {
                        startService(resume);
                    }
                }
            });
            addSubButton(x - 150, y, R.drawable.btn_screenshots, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeSubButton();
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
                        }
                    });
                }
            });
            addSubButton(x - 100, y + 100, R.drawable.btn_stop, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeSubButton();
                    Intent stop = new Intent(getApplicationContext(), ScreenRecorderService.class);
                    stop.setAction(Config.Action.ACTION_STOP);
                    if (Build.VERSION.SDK_INT >= 26) {
                        startForegroundService(stop);
                    } else {
                        startService(stop);
                    }
                }
            });
        }
    }

    public void setupRecording(BubbleLayout bubble) {
        int x = bubble.getViewParams().x;
        int y = bubble.getViewParams().y;
        if (x <= 0) {
            addSubButton(x + 100, y - 100, R.drawable.btn_pause, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeSubButton();
                    Intent pause = new Intent(getApplicationContext(), ScreenRecorderService.class);
                    pause.setAction(Config.Action.ACTION_PAUSE);
                    if (Build.VERSION.SDK_INT >= 26) {
                        startForegroundService(pause);
                    } else {
                        startService(pause);
                    }
                }
            });
            addSubButton(x + 150, y, R.drawable.btn_screenshots, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeSubButton();
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
                }
            });
            addSubButton(x + 100, y + 100, R.drawable.btn_stop, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeSubButton();
                    Intent stop = new Intent(getApplicationContext(), ScreenRecorderService.class);
                    stop.setAction(Config.Action.ACTION_STOP);
                    if (Build.VERSION.SDK_INT >= 26) {
                        startForegroundService(stop);
                    } else {
                        startService(stop);
                    }
                }
            });
        } else {
            addSubButton(x - 100, y - 100, R.drawable.btn_pause, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeSubButton();
                    Intent pause = new Intent(getApplicationContext(), ScreenRecorderService.class);
                    pause.setAction(Config.Action.ACTION_PAUSE);
                    if (Build.VERSION.SDK_INT >= 26) {
                        startForegroundService(pause);
                    } else {
                        startService(pause);
                    }
                }
            });
            addSubButton(x - 150, y, R.drawable.btn_screenshots, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeSubButton();
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
                        }
                    });
                }
            });
            addSubButton(x - 100, y + 100, R.drawable.btn_stop, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeSubButton();
                    Intent stop = new Intent(getApplicationContext(), ScreenRecorderService.class);
                    stop.setAction(Config.Action.ACTION_STOP);
                    if (Build.VERSION.SDK_INT >= 26) {
                        startForegroundService(stop);
                    } else {
                        startService(stop);
                    }
                }
            });
        }
    }

    public void setupNotRecording(BubbleLayout bubble) {
        int x = bubble.getViewParams().x;
        int y = bubble.getViewParams().y;
        if (x <= 0) {
            addSubButton(x + 100, y - 150, R.drawable.btn_record, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeSubButton();
                    Handler record = new Handler(Looper.getMainLooper());
                    record.post(new Runnable() {
                        @Override
                        public void run() {
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
                        }
                    });
                }
            });
            addSubButton(x + 150, y - 50, R.drawable.btn_home, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeSubButton();
                    Handler icon = new Handler(Looper.getMainLooper());
                    icon.post(new Runnable() {
                        @Override
                        public void run() {
                            Intent launchIntent = new Intent(getApplicationContext(), MainActivity.class);
                            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(launchIntent);
                        }
                    });
                }
            });
            addSubButton(x + 150, y + 50, R.drawable.btn_screenshots, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeSubButton();
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
                        }
                    });
                }
            });
            addSubButton(x + 100, y + 150, R.drawable.btn_close, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeSubButton();
                    Handler icon = new Handler(Looper.getMainLooper());
                    icon.post(new Runnable() {
                        @Override
                        public void run() {
                            removeSubButton();
                        }
                    });
                }
            });
        } else {
            addSubButton(x - 100, y - 150, R.drawable.btn_record, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeSubButton();
                    Handler record = new Handler(Looper.getMainLooper());
                    record.post(new Runnable() {
                        @Override
                        public void run() {
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
                        }
                    });
                }
            });
            addSubButton(x - 150, y - 50, R.drawable.btn_home, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeSubButton();
                    Handler icon = new Handler(Looper.getMainLooper());
                    icon.post(new Runnable() {
                        @Override
                        public void run() {
                            Intent launchIntent = new Intent(getApplicationContext(), MainActivity.class);
                            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(launchIntent);
                        }
                    });
                }
            });
            addSubButton(x - 150, y + 50, R.drawable.btn_screenshots, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeSubButton();
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
                        }
                    });
                }
            });
            addSubButton(x - 100, y + 150, R.drawable.btn_close, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Handler icon = new Handler(Looper.getMainLooper());
                    icon.post(new Runnable() {
                        @Override
                        public void run() {
                            removeSubButton();
                        }
                    });
                }
            });
        }
    }

    /*public void initialize() {
        context.bindService(new Intent(context, BubblesService.class),
                bubbleServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    public void recycle() {
        context.unbindService(bubbleServiceConnection);
    }*/

    private BubbleLayout bubbleView;
    private List<BubbleLayout> bubbles = new ArrayList<>();
    private BubbleTrashLayout bubblesTrash;
    private BubblesLayoutCoordinator layoutCoordinator;

    public void addBubble(BubbleLayout bubble, int x, int y) {
        WindowManager.LayoutParams layoutParams = buildLayoutParamsForBubble(x, y);
        bubble.setWindowManager(getWindowManager());
        bubble.setViewParams(layoutParams);
        bubble.setLayoutCoordinator(layoutCoordinator);
        bubbles.add(bubble);
        addViewToWindow(bubble);
    }

    public void addTrash(int trashLayoutResourceId) {
        if (trashLayoutResourceId != 0) {
            bubblesTrash = new BubbleTrashLayout(this);
            bubblesTrash.setWindowManager(mWindowManager);
            bubblesTrash.setViewParams(buildLayoutParamsForTrash());
            bubblesTrash.setVisibility(View.GONE);
            LayoutInflater.from(this).inflate(trashLayoutResourceId, bubblesTrash, true);
            addViewToWindow(bubblesTrash);
            initializeLayoutCoordinator();
        }
    }

    private void initializeLayoutCoordinator() {
        layoutCoordinator = new BubblesLayoutCoordinator.Builder(this)
                .setWindowManager(getWindowManager())
                .setTrashView(bubblesTrash)
                .build();
    }

    private void addViewToWindow(final BubbleBaseLayout view) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                getWindowManager().addView(view, view.getViewParams());
            }
        });
    }

    @Override
    public boolean onUnbind(Intent intent) {
        for (BubbleLayout bubble : bubbles) {
            recycleBubble(bubble);
        }
        bubbles.clear();
        return super.onUnbind(intent);
    }

    private void recycleBubble(final BubbleLayout bubble) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                getWindowManager().removeView(bubble);
                for (BubbleLayout cachedBubble : bubbles) {
                    if (cachedBubble == bubble) {
                        bubble.notifyBubbleRemoved();
                        bubbles.remove(cachedBubble);
                        break;
                    }
                }
            }
        });
    }

    private WindowManager.LayoutParams buildLayoutParamsForBubble(int x, int y) {
        final WindowManager.LayoutParams params;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params = new WindowManager.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        } else {
            params = new WindowManager.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = x;
        params.y = y;
        return params;
    }

    private WindowManager.LayoutParams buildLayoutParamsForTrash() {
        int x = 0;
        int y = 0;
        final WindowManager.LayoutParams params;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params = new WindowManager.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        } else {
            params = new WindowManager.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }
        params.x = x;
        params.y = y;
        return params;
    }

    public void removeBubble(BubbleLayout bubble) {
        recycleBubble(bubble);
    }

    private void addSubButton(int x, int y, int res, View.OnClickListener onClickListener) {
        ImageView mCdView = (ImageView) LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.layout_sub_button, null);
        mCdView.setImageResource(res);
        mCdView.setOnClickListener(onClickListener);

        //Create Layout param
        final WindowManager.LayoutParams params;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params = new WindowManager.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        } else {
            params = new WindowManager.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = x;
        params.y = y;

        //Add bubble view to window
        mWindowManager.addView(mCdView, params);
        listSubButton.add(mCdView);
    }
}