package com.screen.recorder.fragment;

import android.os.Environment;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.screen.recorder.Config;
import com.screen.recorder.R;
import com.screen.recorder.adapter.VideoAdapter;
import com.screen.recorder.base.fragment.BaseFragment;
import com.screen.recorder.service.ScreenRecorderService;
import com.screen.recorder.utils.Utils;

import java.io.File;
import java.util.List;

public class VideosFragment extends BaseFragment {

    private RecyclerView recycler_content;
    private TextView lbl_no_data;
    private VideoAdapter adapter;

    @Override
    protected int getResLayout() {
        return R.layout.fragment_video;
    }

    @Override
    protected void initView(View view) {
        recycler_content = view.findViewById(R.id.recycler_content);
        lbl_no_data = view.findViewById(R.id.lbl_no_data);
    }

    @Override
    protected void initData() {
        // If the size of views will change as the data changes.
        recycler_content.setHasFixedSize(false);
        // Setting the LayoutManager.
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        recycler_content.setLayoutManager(manager);
        List<File> listVideos = ScreenRecorderService.getRecorder().getListVideos();
        adapter = new VideoAdapter(getActivity(), listVideos);
        recycler_content.setAdapter(adapter);
        if (listVideos == null || listVideos.size() <= 0) {
            lbl_no_data.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
        }
    }

    public void updateData() {
        String pathFolder = Environment.getExternalStorageDirectory() + "/" +
                Config.Directory.ROOT_DIRECTORY + "/" + Config.Directory.VIDEO_DIRECTORY;
        List<File> listVideos = Utils.getVideoInDirectory(pathFolder);
        ScreenRecorderService.getRecorder().setListVideos(listVideos);
        final List<File> list = ScreenRecorderService.getRecorder().getListVideos();
        adapter.updateList(list);
        if (list != null && list.size() > 0)
            lbl_no_data.setVisibility(View.GONE);
        else
            lbl_no_data.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (list != null && list.size() > 0) {
                    adapter.notifyItemChanged(0);
                }
            }
        }, 1000);
    }
}