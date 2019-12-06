package com.vmb.ads_in_app.util;

import android.content.Context;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.vmb.ads_in_app.R;

/**
 * Created by Manh Dang on 03/05/2018.
 */

public class OnTouchClickListener implements View.OnTouchListener {
    /**
     * min movement to detect as a click action.
     */
    private int minMove = 20;
    private float startX;
    private float startY;
    private View.OnClickListener mListener;
    private Context context;

    public OnTouchClickListener(View.OnClickListener mListener, Context context) {
        this.mListener = mListener;
        this.context = context;
    }

    private boolean isAClick(float startX, float endX, float startY, float endY) {
        float differenceX = Math.abs(startX - endX);
        float differenceY = Math.abs(startY - endY);
        if (differenceX > minMove || differenceY > minMove) {
            return false;
        }
        return true;
    }

    @Override
    public boolean onTouch(final View v, MotionEvent event) {
        if (this.context == null)
            return false;

        Animation zoom_in = AnimationUtils.loadAnimation(context, R.anim.zoom_in);
        Animation zoom_out = AnimationUtils.loadAnimation(context, R.anim.zoom_out);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                v.startAnimation(zoom_out);
                return true;

            case MotionEvent.ACTION_UP:
                float endX = event.getX();
                float endY = event.getY();
                v.startAnimation(zoom_in);
                if (isAClick(startX, endX, startY, endY)) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mListener.onClick(v);
                        }
                    }, 50);
                }
                break;

            case MotionEvent.ACTION_CANCEL:
                v.startAnimation(zoom_in);
                break;
        }

        return false;
    }
}