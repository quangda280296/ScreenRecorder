package com.screen.recorder.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.screen.recorder.Config;
import com.screen.recorder.R;
import com.screen.recorder.service.ScreenRecorderService;
import com.vmb.ads_in_app.util.PermissionUtil;
import com.vmb.ads_in_app.util.SharedPreferencesUtil;
import com.vmb.ads_in_app.util.ToastUtil;

import java.io.File;

public class RequestActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(getResources().getColor(android.R.color.transparent));
        }

        Intent intent = getIntent();
        if (intent == null) {
            Log.i("onCreateActivity", "intent == null");
            return;
        }

        switch (intent.getAction()) {
            case Config.Action.ACTION_SCREENSHOT:
                if (PermissionUtil.requestPermission(RequestActivity.this,
                        Config.RequestCode.REQUEST_CODE_PERMISSION_WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    requestCapturing();
                break;

            case Config.Action.ACTION_RECORD:
                if (PermissionUtil.requestPermission(RequestActivity.this,
                        Config.RequestCode.REQUEST_CODE_PERMISSION_AUDIO_AND_WRITE,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}))
                    requestRecording();
                break;

            default:
                break;
        }
    }

    public void requestCapturing() {
        // Create a screenshot folder
        String pathScreenshots = Environment.getExternalStorageDirectory() + "/" +
                Config.Directory.ROOT_DIRECTORY + "/" + Config.Directory.SCREENSHOT_DIRECTORY + "/";
        File screenshot_folder = new File(pathScreenshots);
        if (!screenshot_folder.exists())
            screenshot_folder.mkdirs();

        MediaProjectionManager manager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent permissionIntent = manager.createScreenCaptureIntent();
        startActivityForResult(permissionIntent, Config.RequestCode.REQUEST_CODE_CAPTURE_PERMISSION);
    }

    private void startScreenCapturer(final int resultCode, final Intent data) {
        Intent intent = new Intent(this, ScreenRecorderService.class);
        intent.setAction(Config.Action.ACTION_SCREENSHOT);
        intent.putExtra(Config.EXTRA_RESULT_CODE, resultCode);
        intent.putExtra(Config.EXTRA_RESULT_INTENT, data);
        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    public void requestRecording() {
        // Create a video folder
        String pathVideos = Environment.getExternalStorageDirectory() + "/" +
                Config.Directory.ROOT_DIRECTORY + "/" + Config.Directory.VIDEO_DIRECTORY + "/";
        File video_folder = new File(pathVideos);
        if (!video_folder.exists())
            video_folder.mkdirs();

        MediaProjectionManager manager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent permissionIntent = manager.createScreenCaptureIntent();
        startActivityForResult(permissionIntent, Config.RequestCode.REQUEST_CODE_RECORD_PERMISSION);
    }

    private void startScreenRecorder(final int resultCode, final Intent data) {
        Intent intent = new Intent(this, ScreenRecorderService.class);
        intent.setAction(Config.Action.ACTION_RECORD);
        intent.putExtra(Config.EXTRA_RESULT_CODE, resultCode);
        intent.putExtra(Config.EXTRA_RESULT_INTENT, data);
        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Config.RequestCode.REQUEST_CODE_PERMISSION_WRITE_EXTERNAL_STORAGE:
                if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        AlertDialog dialog = new AlertDialog.Builder(RequestActivity.this)
                                .setMessage(R.string.permissions_write_storage)
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent();
                                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                                        intent.setData(Uri.parse("package:" + getPackageName()));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                        startActivity(intent);
                                        dialog.cancel();
                                    }
                                }).create();
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                finish();
                            }
                        });
                        dialog.show();

                    } else {
                        ToastUtil.shortToast(getApplicationContext(), getString(R.string.permissions_write_storage));
                        finish();
                    }
                } else
                    requestCapturing();
                break;

            case Config.RequestCode.REQUEST_CODE_PERMISSION_AUDIO_AND_WRITE:
                if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    if (ScreenRecorderService.getRecorder().isAudio()) {
                        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
                            check();
                        else {
                            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                                AlertDialog dialog = new AlertDialog.Builder(RequestActivity.this)
                                        .setMessage(R.string.permissions_accept)
                                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        })
                                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent intent = new Intent();
                                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                                intent.addCategory(Intent.CATEGORY_DEFAULT);
                                                intent.setData(Uri.parse("package:" + getPackageName()));
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                                startActivity(intent);
                                                dialog.cancel();
                                            }
                                        }).create();
                                dialog.setCanceledOnTouchOutside(false);
                                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        finish();
                                    }
                                });
                                dialog.show();
                            } else {
                                ToastUtil.shortToast(getApplicationContext(), getString(R.string.permissions_record_audio));
                                finish();
                            }
                        }
                    } else
                        check();
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        AlertDialog dialog = new AlertDialog.Builder(RequestActivity.this)
                                .setMessage(R.string.permissions_accept)
                                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                })
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent();
                                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                                        intent.setData(Uri.parse("package:" + getPackageName()));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                        startActivity(intent);
                                        dialog.cancel();
                                    }
                                }).create();
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                finish();
                            }
                        });
                        dialog.show();
                    } else {
                        ToastUtil.shortToast(getApplicationContext(), getString(R.string.permissions_write_storage));
                        finish();
                    }
                }
                break;

            default:
                break;
        }
    }

    private void check() {
        boolean check = SharedPreferencesUtil.getPrefferBool(getApplicationContext(),
                Config.KeySharePrefference.OVERLAY, false);

        if (!check) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(RequestActivity.this)
                        .setMessage(getString(R.string.accept_to_use_count_down))
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:" + Config.PACKAGE_NAME));
                                startActivityForResult(intent, Config.RequestCode.REQUEST_CODE_PERMISSION_OVERLAY);
                                dialog.cancel();
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestRecording();
                                dialog.cancel();
                            }
                        })
                        .create();

                dialog.setCancelable(false);
                dialog.show();
                SharedPreferencesUtil.putPrefferBool(getApplicationContext(),
                        Config.KeySharePrefference.OVERLAY, true);
            }
        } else
            requestRecording();
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, final Intent data) {
        if (Config.RequestCode.REQUEST_CODE_RECORD_PERMISSION == requestCode) {
            if (resultCode == RESULT_OK)
                startScreenRecorder(resultCode, data);
            else
                ToastUtil.shortToast(getApplicationContext(), getString(R.string.permissions_recording));
            finish();
        } else if (Config.RequestCode.REQUEST_CODE_CAPTURE_PERMISSION == requestCode) {
            if (resultCode == RESULT_OK)
                startScreenCapturer(resultCode, data);
            else
                ToastUtil.shortToast(getApplicationContext(), getString(R.string.permissions_screenshot));
            finish();
        } else if (Config.RequestCode.REQUEST_CODE_PERMISSION_OVERLAY == requestCode) {
            requestRecording();
        }
    }

    @Override
    public void onClick(View v) {

    }
}