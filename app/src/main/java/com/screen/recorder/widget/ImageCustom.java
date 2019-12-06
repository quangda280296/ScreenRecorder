package com.screen.recorder.widget;

import android.content.Context;
import android.util.AttributeSet;

public class ImageCustom extends android.support.v7.widget.AppCompatImageView {

    public ImageCustom(Context context) {
        super(context);
    }

    public ImageCustom(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageCustom(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int width, int height) {
        super.onMeasure(width, height);
        int measuredWidth = getMeasuredWidth();
        setMeasuredDimension(measuredWidth, measuredWidth * 5 / 4);
    }
}