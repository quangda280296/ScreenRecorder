package com.screen.recorder.adapter.holder;

import android.view.View;
import android.widget.TextView;

import com.screen.recorder.R;
import com.screen.recorder.base.adapter.holder.BaseViewHolder;
import com.screen.recorder.widget.ImageCustom;

public class ScreenshotViewHolder extends BaseViewHolder {

    public View layout_screenshot;
    public ImageCustom img_screenshot;
    public TextView lbl_screenshot;

    public ScreenshotViewHolder(View itemView) {
        super(itemView);
        layout_screenshot = itemView.findViewById(R.id.layout_screenshot);
        img_screenshot = itemView.findViewById(R.id.img_screenshot);
        lbl_screenshot = itemView.findViewById(R.id.lbl_screenshot);
    }
}