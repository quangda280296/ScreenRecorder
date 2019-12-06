package com.screen.recorder.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.github.chrisbanes.photoview.PhotoView;
import com.screen.recorder.Config;
import com.screen.recorder.R;
import com.screen.recorder.base.BaseActivity;
import com.vmb.ads_in_app.util.SharedPreferencesUtil;
import com.vmb.ads_in_app.util.ToastUtil;

public class ZoomablePhotoActivity extends BaseActivity {

    private PhotoView imageView;

    @Override
    protected int getResLayout() {
        return R.layout.activity_zoomable_photo;
    }

    @Override
    protected void initView() {
        imageView = findViewById(R.id.iv_zoomable);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    @Override
    protected void initData() {
        String path = getIntent().getStringExtra(Config.URL_FILE);
        if (!TextUtils.isEmpty(path)) {
            Log.i("setImageURI", "path from list");
            setPhoto(path);
            return;
        }

        path = SharedPreferencesUtil.getPrefferString(getApplicationContext(), Config.URL_FILE, "");
        if (TextUtils.isEmpty(path)) {
            ToastUtil.longToast(getApplicationContext(), getString(R.string.error));
            finish();
            return;
        }

        Log.i("setImageURI", "path from shared preference");
        setPhoto(path);
    }

    private void setPhoto(String path) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Log.i("getBitmap", "width = " + width);
        Log.i("getBitmap", "height = " + height);

        if (width > height) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            Log.i("getBitmap", "Switch to landscape");
        }

        imageView.setImageBitmap(bitmap);
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