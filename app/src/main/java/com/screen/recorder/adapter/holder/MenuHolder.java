package com.screen.recorder.adapter.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.screen.recorder.R;
import com.screen.recorder.base.adapter.holder.BaseViewHolder;

public class MenuHolder extends BaseViewHolder {

    public View layout_menu;
    public ImageView img_icon;
    public TextView lbl_title;

    public MenuHolder(View itemView) {
        super(itemView);
        layout_menu = itemView.findViewById(R.id.layout_menu);
        img_icon = itemView.findViewById(R.id.img_icon);
        lbl_title = itemView.findViewById(R.id.lbl_title);
    }
}