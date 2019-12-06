package com.screen.recorder.activity;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.screen.recorder.Config;
import com.screen.recorder.R;
import com.screen.recorder.base.BaseActivity;

public class WebviewActivity extends BaseActivity {

    private WebView webView;

    @Override
    protected int getResLayout() {
        return R.layout.activity_webview;
    }

    @Override
    protected void initView() {
        webView = findViewById(R.id.webView);
    }

    @Override
    protected void initData() {
        String url = Config.URL_WEB;
        if (TextUtils.isEmpty(url)) {
            return;
        }

        webView.loadUrl(url);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
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