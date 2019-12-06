package com.screen.recorder.activity;

import android.animation.Animator;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.WindowManager;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.OnCompositionLoadedListener;
import com.screen.recorder.R;
import com.screen.recorder.service.ScreenRecorderService;
import com.vmb.ads_in_app.BaseActivity;

public class SplashActivity extends BaseActivity {

    private LottieAnimationView lottieAnimationView;
    private LottieDrawable drawable;

    @Override
    protected int getResLayout() {
        return R.layout.activity_splash;
    }

    @Override
    protected void initView() {
        lottieAnimationView = findViewById(R.id.lottie);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    @Override
    protected void initData() {
        LottieComposition.Factory.fromAssetFileName(this, "wave-loading.json", (new OnCompositionLoadedListener() {
            @Override
            public void onCompositionLoaded(LottieComposition composition) {
                drawable = new LottieDrawable();
                drawable.setComposition(composition);
                drawable.playAnimation();
                drawable.addAnimatorListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        finish();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                lottieAnimationView.setImageDrawable(drawable);
            }
        }));

        //GetConfig.callAPI(getApplicationContext(), Config.CODE_CONTROL_APP, Config.VERSION_APP, Config.PACKAGE_NAME);

        new Thread(new Runnable() {
            @Override
            public void run() {
                /*((MainApplication) getApplication()).registerReceiver();
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (!wifiManager.isWifiEnabled())
                    wifiManager.setWifiEnabled(true);*/

                Intent intent = new Intent(getApplicationContext(), ScreenRecorderService.class);
                if (Build.VERSION.SDK_INT >= 26) {
                    startForegroundService(intent);
                } else {
                    startService(intent);
                }
            }
        }).start();

        new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                drawable.stop();
            }
        }.start();

        /*AdsHandler.getInstance().displayPopupOpenApp(SplashActivity.this,
                new Intent(SplashActivity.this, MainActivity.class));*/
    }

    @Override
    public void onBackPressed() {
        Log.i("SplashActivity", "onBackPressed()");
    }
}