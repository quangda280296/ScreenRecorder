package com.screen.recorder.activity;

import android.Manifest;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.screen.recorder.Config;
import com.screen.recorder.Interface.IRecordListener;
import com.screen.recorder.R;
import com.screen.recorder.adapter.MenuAdapter;
import com.screen.recorder.base.BaseActivity;
import com.screen.recorder.fragment.ScreenshotsFragment;
import com.screen.recorder.fragment.VideosFragment;
import com.screen.recorder.service.ScreenRecorderService;
import com.screen.recorder.utils.Utils;
import com.vmb.ads_in_app.Interface.IUpdateNewVersion;
import com.vmb.ads_in_app.LibrayData;
import com.vmb.ads_in_app.model.AdsConfig;
import com.vmb.ads_in_app.util.CountryCodeUtil;
import com.vmb.ads_in_app.util.NetworkUtil;
import com.vmb.ads_in_app.util.OnTouchClickListener;
import com.vmb.ads_in_app.util.PermissionUtil;
import com.vmb.ads_in_app.util.PrintKeyHash;
import com.vmb.ads_in_app.util.RefreshToken;
import com.vmb.ads_in_app.util.ShareRateUtil;
import com.vmb.ads_in_app.util.SharedPreferencesUtil;
import com.vmb.ads_in_app.util.ToastUtil;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends BaseActivity implements IRecordListener, IUpdateNewVersion {

    private ViewGroup layout_dialog;
    private ImageView img_close;

    private TextView lbl_title;
    private TextView lbl_content;

    private Button btn_a;
    private Button btn_b;
    private Button btn_ok;

    private boolean show_rate = false;
    private boolean require_update = false;

    private ImageView img_video;
    private ImageView img_screenshot;

    private TextView lbl_title_video;
    private TextView lbl_title_screenshot;
    private TextView lbl_time;

    private RecyclerView recycler_menu;
    private View btn_record;

    private int mode = 1;

    @Override
    protected int getResLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        layout_dialog = findViewById(R.id.layout_dialog);
        img_close = findViewById(R.id.img_close);

        lbl_title = findViewById(R.id.lbl_title);
        lbl_content = findViewById(R.id.lbl_content);

        btn_a = findViewById(R.id.btn_a);
        btn_b = findViewById(R.id.btn_b);
        btn_ok = findViewById(R.id.btn_ok);

        btn_record = findViewById(R.id.btn_record);
        lbl_time = findViewById(R.id.lbl_time);

        img_video = findViewById(R.id.img_video);
        img_screenshot = findViewById(R.id.img_screenshot);

        lbl_title_video = findViewById(R.id.lbl_title_video);
        lbl_title_screenshot = findViewById(R.id.lbl_title_screenshot);

        recycler_menu = findViewById(R.id.recycler_menu);
    }

    private boolean isPause = false;
    private boolean isRequestRead = false;

    @Override
    protected void onPause() {
        super.onPause();
        isPause = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPause = false;

        if (!isRequestRead)
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isPause)
                        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            isRequestRead = true;
                            PermissionUtil.requestPermission(MainActivity.this,
                                    Config.RequestCode.REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_EXTERNAL_STORAGE);
                        }
                }
            }, 2000);
    }

    @Override
    protected void initData() {
        //GetConfig.init(MainActivity.this, MainActivity.this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        //NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        btn_record.setOnTouchListener(new OnTouchClickListener(this, getApplicationContext()));
        findViewById(R.id.btn_videos).setOnTouchListener(new OnTouchClickListener(this, getApplicationContext()));
        findViewById(R.id.btn_screenshots).setOnTouchListener(new OnTouchClickListener(this, getApplicationContext()));

        setupMenu();

        if (ScreenRecorderService.getRecorder().isStart()) {
            lbl_time.setVisibility(View.VISIBLE);
            btn_record.setBackgroundResource(R.drawable.img_stop);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                ScreenRecorderService.getRecorder().setListener(MainActivity.this);

                CountryCodeUtil.setCountryCode(getApplicationContext(),
                        Config.CODE_CONTROL_APP, Config.VERSION_APP, Config.PACKAGE_NAME);
                PrintKeyHash.print(getApplicationContext());

                int count_play = SharedPreferencesUtil.getPrefferInt(getApplicationContext(),
                        LibrayData.KeySharePrefference.COUNT_PLAY, 0);
                count_play++;
                SharedPreferencesUtil.putPrefferInt(getApplicationContext(),
                        LibrayData.KeySharePrefference.COUNT_PLAY, count_play);
                boolean rate = SharedPreferencesUtil.getPrefferBool(getApplicationContext(),
                        LibrayData.KeySharePrefference.SHOW_RATE, false);
                if (!rate) {
                    if (count_play >= 5)
                        show_rate = true;
                }

                RefreshToken.getInstance().checkSendToken(getApplicationContext(),
                        Config.CODE_CONTROL_APP, Config.VERSION_APP, Config.PACKAGE_NAME);
            }
        }).start();

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanVideo();
                    //scanPhoto();
                }
            }, 1000);
        }
    }

    private void setupMenu() {
        // If the size of views will change as the data changes.
        recycler_menu.setHasFixedSize(false);
        // Setting the LayoutManager.
        LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext());
        recycler_menu.setLayoutManager(manager);
        MenuAdapter adapter = new MenuAdapter(MainActivity.this, Arrays.asList(Config.action_menu));
        recycler_menu.setAdapter(adapter);
    }

    private void scanVideo() {
        String pathFolder = Environment.getExternalStorageDirectory() + "/" +
                Config.Directory.ROOT_DIRECTORY + "/" + Config.Directory.VIDEO_DIRECTORY;
        List<File> listVideos = Utils.getVideoInDirectory(pathFolder);
        ScreenRecorderService.getRecorder().setListVideos(listVideos);
        if (mode == 1)
            setupVideo();
    }

    private void scanPhoto() {
        String pathFolder = Environment.getExternalStorageDirectory() + "/" +
                Config.Directory.ROOT_DIRECTORY + "/" + Config.Directory.SCREENSHOT_DIRECTORY;
        List<File> listScreenshots = Utils.getPhotoInDirectory(pathFolder);
        ScreenRecorderService.getRecorder().setListScreenshots(listScreenshots);
        if (mode == 2)
            setupScreenshot();
    }

    private void setupVideo() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    FragmentManager manager = getFragmentManager();
                    manager.popBackStack();
                    FragmentTransaction transaction = manager.beginTransaction();
                    VideosFragment fragment = new VideosFragment();
                    transaction.replace(R.id.main_content, fragment, Config.FragmentTag.Fragment_Video);
                    transaction.commit();
                } catch (Exception e) {

                }
            }
        });
    }

    private void setupScreenshot() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    FragmentManager manager = getFragmentManager();
                    manager.popBackStack();
                    FragmentTransaction transaction = manager.beginTransaction();
                    ScreenshotsFragment fragment = new ScreenshotsFragment();
                    transaction.replace(R.id.main_content, fragment, Config.FragmentTag.Fragment_Screenshot);
                    transaction.commit();
                } catch (Exception e) {

                }
            }
        });
    }

    @Override
    protected void onActivity_Result(int requestCode, int resultCode, Intent data) {

    }

    @Override
    protected void onRequestPermissions_Result(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Config.RequestCode.REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE:
                if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        Snackbar.make(findViewById(android.R.id.content), R.string.permissions_read_storage,
                                Snackbar.LENGTH_SHORT).setAction(R.string.enable,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent();
                                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                                        intent.setData(Uri.parse("package:" + getPackageName()));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                        startActivity(intent);
                                    }
                                }).show();
                    } else
                        ToastUtil.shortToast(getApplicationContext(), getString(R.string.permissions_read_storage));
                } else {
                    scanVideo();
                    scanPhoto();
                }
                break;

            case Config.RequestCode.REQUEST_CODE_PERMISSION_DELETE:
                if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                    deletePhoto(new File(ScreenRecorderService.getRecorder().getFileName()));
                else
                    ToastUtil.longToast(getApplicationContext(), getString(R.string.accept_to_delete_image));
                break;

            default:
                break;
        }
    }

    private void deletePhoto(final File file) {
        if (file != null) {
            if (file.exists()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setCancelable(true);
                builder.setMessage(R.string.confirm_delete_video);
                builder.setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (file.delete()) {
                            ToastUtil.longToast(getApplicationContext(), getString(R.string.video_deleted));
                            dialog.dismiss();
                        } else
                            ToastUtil.longToast(getApplicationContext(), getString(R.string.delete_fail));
                    }
                });
                builder.setPositiveButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        }
    }

    public void showRate() {
        show_rate = false;
        SharedPreferencesUtil.putPrefferBool(getApplicationContext(), LibrayData.KeySharePrefference.SHOW_RATE, true);

        lbl_title.setText(R.string.rate_title);
        lbl_content.setText(R.string.rate_content);

        btn_ok.setVisibility(View.GONE);

        btn_a.setText(R.string.share);
        btn_a.setVisibility(View.VISIBLE);
        btn_a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetworkUtil.isNetworkAvailable(getApplicationContext())) {
                    ToastUtil.shortToast(getApplicationContext(), getString(R.string.no_internet));
                    return;
                }
                /*ShareRateUtil.showShareFB(MainActivity.this, callbackManager,
                        Config.CODE_CONTROL_APP, Config.VERSION_APP, Config.PACKAGE_NAME);*/
                ShareRateUtil.shareApp(MainActivity.this);
            }
        });

        btn_b.setText(R.string.rate);
        btn_b.setVisibility(View.VISIBLE);
        btn_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetworkUtil.isNetworkAvailable(getApplicationContext())) {
                    ToastUtil.shortToast(getApplicationContext(), getString(R.string.no_internet));
                    return;
                }
                ShareRateUtil.rateApp(MainActivity.this);
            }
        });

        img_close.setOnTouchListener(new OnTouchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout_dialog.setVisibility(View.GONE);
            }
        }, getApplicationContext()));

        layout_dialog.setVisibility(View.VISIBLE);
    }

    public void showUpdate() {
        String title = AdsConfig.getInstance().getUpdate_title();
        if (TextUtils.isEmpty(title))
            title = "Update";

        String content = AdsConfig.getInstance().getUpdate_message();
        if (TextUtils.isEmpty(content))
            content = "There is a new version, please update soon !";

        lbl_title.setText(title);
        lbl_content.setText(content);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = AdsConfig.getInstance().getUpdate_url();
                if (TextUtils.isEmpty(url))
                    url = "https://play.google.com/store/apps/developer?id=Fruit+Game+Studio";

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivityForResult(intent, LibrayData.RequestCode.REQUEST_CODE_UPDATE);
            }
        });

        img_close.setOnTouchListener(new OnTouchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (require_update)
                    return;

                layout_dialog.setVisibility(View.GONE);
            }
        }, getApplicationContext()));

        layout_dialog.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            FragmentManager manager = getFragmentManager();
            if (manager.getBackStackEntryCount() > 0)
                super.onBackPressed();
            else {
                if (findViewById(R.id.layout_dialog).getVisibility() == View.VISIBLE) {
                    if (require_update)
                        return;

                    findViewById(R.id.layout_dialog).setVisibility(View.GONE);
                    return;
                }

                //AdsHandler.getInstance().showCofirmDialog(MainActivity.this);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_record:
                ScreenRecorderService.getRecorder().setListener(this);
                if (ScreenRecorderService.getRecorder().isStart()) {
                    Intent stop = new Intent(getApplicationContext(), ScreenRecorderService.class);
                    stop.setAction(Config.Action.ACTION_STOP);
                    if (Build.VERSION.SDK_INT >= 26) {
                        startForegroundService(stop);
                    } else {
                        startService(stop);
                    }
                    return;
                }
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
                break;

            case R.id.btn_videos:
                if (mode == 1)
                    return;

                mode = 1;
                scanVideo();

                img_video.setImageResource(R.drawable.img_video_on);
                lbl_title_video.setTextColor(getResources().getColor(R.color.orange_0));
                img_screenshot.setImageResource(R.drawable.img_screenshot_off);
                lbl_title_screenshot.setTextColor(getResources().getColor(R.color.black));
                break;

            case R.id.btn_screenshots:
                if (mode == 2)
                    return;

                mode = 2;
                scanPhoto();

                img_video.setImageResource(R.drawable.img_video_off);
                lbl_title_video.setTextColor(getResources().getColor(R.color.black));
                img_screenshot.setImageResource(R.drawable.img_screenshot_on);
                lbl_title_screenshot.setTextColor(getResources().getColor(R.color.orange_0));
                break;

            default:
                break;
        }
    }

    @Override
    public void onCaptured() {
        //ToastUtil.shortToast(getApplicationContext(), getString(R.string.screenshot_captured));
        if (mode != 2)
            return;

        FragmentManager manager = getFragmentManager();
        ScreenshotsFragment fragment = (ScreenshotsFragment) manager.findFragmentByTag(Config.FragmentTag.Fragment_Screenshot);
        if (fragment != null)
            fragment.updateData();
    }

    @Override
    public void onStartRecording() {
        lbl_time.setText(getString(R.string._00_00));
        lbl_time.setVisibility(View.VISIBLE);
        btn_record.setBackgroundResource(R.drawable.img_stop);
    }

    @Override
    public void onRecording(String minutes, String seconds) {
        lbl_time.setText(minutes + " : " + seconds);
    }

    @Override
    public void onPauseRecord() {
        Log.i("MainActivity", "onPauseRecord()");
    }

    @Override
    public void onResumeRecord() {
        Log.i("MainActivity", "onResumeRecord()");
    }

    @Override
    public void onStopRecording() {
        btn_record.setBackgroundResource(R.drawable.img_record);
        lbl_time.setText(R.string._00_00);
        lbl_time.setVisibility(View.GONE);

        if (mode != 1)
            return;

        FragmentManager manager = getFragmentManager();
        VideosFragment fragment = (VideosFragment) manager.findFragmentByTag(Config.FragmentTag.Fragment_Video);
        if (fragment != null)
            fragment.updateData();
    }

    @Override
    public void onGetConfig(boolean require_update) {
        this.require_update = require_update;
        showUpdate();
    }
}