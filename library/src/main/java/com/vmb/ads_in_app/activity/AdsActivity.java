package com.vmb.ads_in_app.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.vmb.ads_in_app.LibrayData;
import com.vmb.ads_in_app.R;
import com.vmb.ads_in_app.handler.AdsHandler;

public class AdsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getResLayout());
        initView();
        initData();
    }

    protected int getResLayout() {
        return R.layout.fragment_loading_dialog;
    }

    protected void initView() {

    }

    protected void initData() {
        final String type = getIntent().getStringExtra(LibrayData.KeyIntentData.KEY_ADS_ACTIVITY);
        final boolean isRestartCoundDown = getIntent().getBooleanExtra(LibrayData.KeyIntentData.IS_RESTART_COUNTDOWN, true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                AdsHandler.getInstance().displayPopup(getApplicationContext(), type, isRestartCoundDown);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!isFinishing())
                            finish();
                    }
                }, 200);
            }
        }, 1000);
    }
}