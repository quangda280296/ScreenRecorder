package com.screen.recorder.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.screen.recorder.Config;
import com.screen.recorder.R;
import com.screen.recorder.base.fragment.BaseFragment;
import com.screen.recorder.service.ScreenRecorderService;

public class SettingFragment extends BaseFragment {

    private String[] resolution = {"1080P", "720P", "640P", "480P", "320P", "240P"};
    private String[] quality = {"Ultra", "High", "Medium", "Low"};
    private String[] fps = {"60FPS", "30FPS", "15FPS"};
    private String[] orientation = {"Auto", "Portrait", "Landscape"};

    private TextView lbl_pathSave;
    private TextView lbl_resolution;
    private TextView lbl_quality;
    private TextView lbl_fps;
    private TextView lbl_orientation;

    private View layout_resolution;
    private View layout_quality;
    private View layout_fps;
    private View layout_orientation;
    private View layout_record_audio;

    private SwitchCompat switch_record;

    @Override
    protected int getResLayout() {
        return R.layout.fragment_setting;
    }

    @Override
    protected void initView(View view) {
        lbl_pathSave = view.findViewById(R.id.lbl_pathSave);

        layout_resolution = view.findViewById(R.id.layout_resolution);
        layout_quality = view.findViewById(R.id.layout_quality);
        layout_fps = view.findViewById(R.id.layout_fps);
        layout_orientation = view.findViewById(R.id.layout_orientation);

        lbl_resolution = view.findViewById(R.id.lbl_resolution);
        lbl_quality = view.findViewById(R.id.lbl_quality);
        lbl_fps = view.findViewById(R.id.lbl_fps);
        lbl_orientation = view.findViewById(R.id.lbl_orientation);
        layout_record_audio = view.findViewById(R.id.layout_record_audio);

        switch_record = view.findViewById(R.id.switch_record);
    }

    @Override
    protected void initData() {
        lbl_pathSave.append(Config.Directory.ROOT_DIRECTORY + "/" + Config.Directory.VIDEO_DIRECTORY);

        layout_resolution.setOnClickListener(this);
        layout_quality.setOnClickListener(this);
        layout_fps.setOnClickListener(this);
        layout_orientation.setOnClickListener(this);
        layout_record_audio.setOnClickListener(this);

        lbl_resolution.setText(ScreenRecorderService.getRecorder().getResolution());
        lbl_quality.setText(ScreenRecorderService.getRecorder().getQuality());
        lbl_fps.setText(ScreenRecorderService.getRecorder().getFps());
        lbl_orientation.setText(ScreenRecorderService.getRecorder().getOrientation());

        switch_record.setChecked(ScreenRecorderService.getRecorder().isAudio());
        switch_record.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ScreenRecorderService.getRecorder().setAudio(getActivity(), isChecked);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_resolution:
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.resolution)
                        .setItems(resolution, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int position) {
                                lbl_resolution.setText(resolution[position]);
                                ScreenRecorderService.getRecorder().setResolution(getActivity(), resolution[position]);
                            }
                        })
                        .create()
                        .show();
                break;

            case R.id.layout_quality:
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.quality)
                        .setItems(quality, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int position) {
                                lbl_quality.setText(quality[position]);
                                ScreenRecorderService.getRecorder().setQuality(getActivity(), quality[position]);
                            }
                        })
                        .create()
                        .show();
                break;

            case R.id.layout_fps:
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.fps)
                        .setItems(fps, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int position) {
                                lbl_fps.setText(fps[position]);
                                ScreenRecorderService.getRecorder().setFps(getActivity(), fps[position]);
                            }
                        })
                        .create()
                        .show();
                break;

            case R.id.layout_orientation:
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.orientation)
                        .setItems(orientation, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int position) {
                                lbl_orientation.setText(orientation[position]);
                                ScreenRecorderService.getRecorder().setOrientation(getActivity(), orientation[position]);
                            }
                        })
                        .create()
                        .show();
                break;

            case R.id.layout_record_audio:
                switch_record.toggle();
                break;

            default:
                break;
        }
    }
}