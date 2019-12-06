package com.screen.recorder.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.VideoView;

import com.screen.recorder.Config;
import com.screen.recorder.R;
import com.screen.recorder.base.BaseActivity;
import com.screen.recorder.widget.VideoController;
import com.vmb.ads_in_app.util.SharedPreferencesUtil;
import com.vmb.ads_in_app.util.ToastUtil;

public class WatchVideoActivity extends BaseActivity {

    private VideoView videoView;

    @Override
    protected int getResLayout() {
        return R.layout.activity_watch_video;
    }

    @Override
    protected void initView() {
        videoView = findViewById(R.id.videoView);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    @Override
    protected void initData() {
        String path = getIntent().getStringExtra(Config.URL_FILE);
        if (!TextUtils.isEmpty(path)) {
            Log.i("setVideoURI", "path from list");
            init(path);
            return;
        }

        path = SharedPreferencesUtil.getPrefferString(getApplicationContext(), Config.URL_FILE, "");
        if (TextUtils.isEmpty(path)) {
            ToastUtil.longToast(getApplicationContext(), getString(R.string.error));
            finish();
            return;
        }

        Log.i("setVideoURI", "path from shared preference");
        init(path);
    }

    public void init(String path) {
        MediaMetadataRetriever m = new MediaMetadataRetriever();
        try {
            m.setDataSource(path);
        } catch (Exception e) {
            ToastUtil.shortToast(getApplicationContext(), "Cannot play this video");
            finish();
        }
        String height = m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        String width = m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);

        Log.i("getDimensionVideo", "width = " + width);
        Log.i("getDimensionVideo", "height = " + height);

        try {
            if (Integer.parseInt(width) > Integer.parseInt(height)) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                Log.i("getDimensionVideo", "Switch to landscape");
            }
        } catch (Exception e) {
            Log.i("getDimensionVideo", e.getMessage());
        }

        Uri video = Uri.parse(path);
        videoView.setVideoURI(video);

        VideoController mc = new VideoController(this, true);
        mc.setMediaPlayer(videoView);
        videoView.setMediaController(mc);
        videoView.requestFocus();
        mc.show();

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoView.start();
            }
        });
    }

    @Override
    protected void onActivity_Result(int requestCode, int resultCode, Intent data) {

    }

    @Override
    protected void onRequestPermissions_Result(int requestCode, String[] permissions, int[] grantResults) {

    }

    @Override
    public void onClick(View v) {

    }
}