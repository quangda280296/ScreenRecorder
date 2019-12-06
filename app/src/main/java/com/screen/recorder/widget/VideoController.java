package com.screen.recorder.widget;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;

import com.screen.recorder.Config;
import com.screen.recorder.R;
import com.screen.recorder.activity.RequestActivity;
import com.screen.recorder.service.ScreenRecorderService;

public class VideoController extends MediaController {

    private Context context;
    private ImageView screenshots;

    public VideoController(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public VideoController(Context context, boolean useFastForward) {
        super(context, useFastForward);
        this.context = context;
    }

    public VideoController(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public void setAnchorView(View view) {
        super.setAnchorView(view);

        screenshots = new ImageView(context);
        screenshots.setImageResource(R.drawable.ic_screenshot_white);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        int widthPixels = displayMetrics.widthPixels;
        int heightPixels = displayMetrics.heightPixels;

        Log.i("setAnchorView()", "widthPixels = " + widthPixels);
        Log.i("setAnchorView()", "heightPixels = " + heightPixels);

        FrameLayout.LayoutParams params;
        if (widthPixels > heightPixels) {
            params = new FrameLayout.LayoutParams(heightPixels / 14, heightPixels / 14);
            params.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
            params.rightMargin = heightPixels / 5;
            params.bottomMargin = heightPixels / 24;
        } else {
            params = new FrameLayout.LayoutParams(widthPixels / 14, widthPixels / 14);
            params.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
            params.rightMargin = widthPixels / 15;
            params.bottomMargin = widthPixels / 24;
        }

        screenshots.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
                if (ScreenRecorderService.getRecorder().getResultData() == null) {
                    Intent launchIntent = new Intent(context, RequestActivity.class);
                    launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    launchIntent.setAction(Config.Action.ACTION_SCREENSHOT);
                    context.startActivity(launchIntent);
                } else {
                    if (ScreenRecorderService.getRecorder().isStart()) {
                        Intent launchIntent = new Intent(context, RequestActivity.class);
                        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        launchIntent.setAction(Config.Action.ACTION_SCREENSHOT);
                        context.startActivity(launchIntent);
                        return;
                    }

                    Intent intent = new Intent(context, ScreenRecorderService.class);
                    intent.setAction(Config.Action.ACTION_SCREENSHOT);
                    if (Build.VERSION.SDK_INT >= 26) {
                        context.startForegroundService(intent);
                    } else {
                        context.startService(intent);
                    }
                }
            }
        });

        addView(screenshots, params);
    }
}