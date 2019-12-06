package com.vmb.ads_in_app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getResLayout());
        initView();
        initData();
    }

    protected abstract int getResLayout();

    protected abstract void initView();

    protected abstract void initData();
}