package com.screen.recorder.adapter.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.screen.recorder.R;
import com.screen.recorder.base.adapter.holder.BaseViewHolder;

public class VideoViewHolder extends BaseViewHolder {

    public View layout_item;
    public View layout_menu;

    public ImageView img_thumbnail;

    public TextView lbl_name;
    public TextView lbl_duration;
    public TextView lbl_size;

    public VideoViewHolder(View itemView) {
        super(itemView);
        layout_item = itemView.findViewById(R.id.layout_item);
        layout_menu = itemView.findViewById(R.id.layout_menu);

        img_thumbnail = itemView.findViewById(R.id.img_thumbnail);

        lbl_name = itemView.findViewById(R.id.lbl_name);
        lbl_duration = itemView.findViewById(R.id.lbl_duration);
        lbl_size = itemView.findViewById(R.id.lbl_size);
    }
}