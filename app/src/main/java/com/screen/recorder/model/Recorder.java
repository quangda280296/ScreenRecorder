package com.screen.recorder.model;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.screen.recorder.Config;
import com.screen.recorder.Interface.IRecordListener;
import com.screen.recorder.R;
import com.screen.recorder.service.ScreenRecorderService;
import com.screen.recorder.utils.Compare;
import com.screen.recorder.utils.Utils;
import com.vmb.ads_in_app.util.SharedPreferencesUtil;
import com.vmb.ads_in_app.util.ToastUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Recorder {
    private boolean isStart;
    private boolean isPause;

    public boolean isStart() {
        return isStart;
    }

    public boolean isPause() {
        return isPause;
    }

    public void setStart(boolean start) {
        isStart = start;
    }

    public void setPause(boolean pause) {
        isPause = pause;
    }

    private String resolution;
    private String quality;
    private String fps;
    private String orientation;
    private boolean audio;

    private int resultCode;
    private Intent resultData;

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public void setResultData(Intent resultData) {
        this.resultData = resultData;
    }

    public int getResultCode() {
        return resultCode;
    }

    public Intent getResultData() {
        return resultData;
    }

    private String fileName;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String file_name) {
        fileName = file_name;
    }

    public String getResolution() {
        return resolution;
    }

    public String getQuality() {
        return quality;
    }

    public String getFps() {
        return fps;
    }

    public String getOrientation() {
        return orientation;
    }

    public boolean isAudio() {
        return audio;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public void setFps(String fps) {
        this.fps = fps;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    public void setAudio(boolean audio) {
        this.audio = audio;
    }

    public void setResolution(Context context, String resolution) {
        this.resolution = resolution;
        SharedPreferencesUtil.putPrefferString(context, Config.KeySharePrefference.RESOLUTION, resolution);
    }

    public void setQuality(Context context, String quality) {
        this.quality = quality;
        SharedPreferencesUtil.putPrefferString(context, Config.KeySharePrefference.QUALITY, quality);
    }

    public void setFps(Context context, String fps) {
        this.fps = fps;
        SharedPreferencesUtil.putPrefferString(context, Config.KeySharePrefference.FPS, fps);
    }

    public void setOrientation(Context context, String orientation) {
        this.orientation = orientation;
        SharedPreferencesUtil.putPrefferString(context, Config.KeySharePrefference.ORIENTATION, orientation);
    }

    public void setAudio(Context context, boolean audio) {
        this.audio = audio;
        SharedPreferencesUtil.putPrefferBool(context, Config.KeySharePrefference.AUDIO, audio);
    }

    private IRecordListener listener;

    public void setListener(IRecordListener listener) {
        this.listener = listener;
    }

    private List<File> listVideos;
    private List<File> listScreenshots;

    public List<File> getListVideos() {
        return listVideos;
    }

    public void setListVideos(List<File> listVideos) {
        if (listVideos == null)
            this.listVideos = new ArrayList<>();
        else {
            Comparator comparator = new Compare();
            Utils.sortList(listVideos, comparator);
            this.listVideos = listVideos;
        }
    }

    public List<File> getListScreenshots() {
        return listScreenshots;
    }

    public void setListScreenshots(List<File> listScreenshots) {
        if (listScreenshots == null)
            this.listScreenshots = new ArrayList<>();
        else {
            Comparator comparator = new Compare();
            Utils.sortList(listScreenshots, comparator);
            this.listScreenshots = listScreenshots;
        }
    }

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

                    if (listener != null)
                        listener.onRecording(minutes, seconds);
                }
            });

            if (handler != null)
                handler.postDelayed(this, 1000);
        }
    };

    public void resume(Context context) {
        if (handler != null)
            handler.post(runnable);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (listener != null)
                    listener.onResumeRecord();
            }
        });

        ToastUtil.longToast(context, context.getString(R.string.resumed));
    }

    public void pause(Context context) {
        if (handler != null)
            handler.removeCallbacks(runnable);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (listener != null)
                    listener.onPauseRecord();
            }
        });

        ToastUtil.longToast(context, context.getString(R.string.paused));
    }

    public void startRecording(Context context) {
        time = 0;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (listener != null)
                    listener.onStartRecording();
            }
        });

        handler = new Handler();
        if (handler != null)
            handler.post(runnable);

        ToastUtil.longToast(context, context.getString(R.string.recording));
    }

    public void stopRecording(Context context) {
        if (handler != null)
            handler.removeCallbacks(runnable);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (listener != null)
                    listener.onStopRecording();
            }
        });

        ToastUtil.longToast(context, context.getString(R.string.recorded_video_saved_to) + " " + fileName);
        Utils.addPhotoToGallery(context, fileName);
    }

    public void screenshotCaptured(Context context) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (listener != null)
                    listener.onCaptured();
            }
        });

        ToastUtil.longToast(context, context.getString(R.string.screenshot_saved_to) + " " + fileName);
    }
}