package com.screen.recorder.fragment;

import android.os.Environment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.screen.recorder.Config;
import com.screen.recorder.R;
import com.screen.recorder.adapter.ScreenshotAdapter;
import com.screen.recorder.base.fragment.BaseFragment;
import com.screen.recorder.service.ScreenRecorderService;
import com.screen.recorder.utils.Utils;

import java.io.File;
import java.util.List;

public class ScreenshotsFragment extends BaseFragment {

    private RecyclerView recycler_content;
    private TextView lbl_no_data;
    private ScreenshotAdapter adapter;

    @Override
    protected int getResLayout() {
        return R.layout.fragment_screenshot;
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
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        recycler_content.setLayoutManager(gridLayoutManager);
        List<File> listScreenshots = ScreenRecorderService.getRecorder().getListScreenshots();
        adapter = new ScreenshotAdapter(getActivity(), listScreenshots);
        recycler_content.setAdapter(adapter);
        if (listScreenshots == null || listScreenshots.size() <= 0) {
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
                Config.Directory.ROOT_DIRECTORY + "/" + Config.Directory.SCREENSHOT_DIRECTORY;
        List<File> listScreenshots = Utils.getPhotoInDirectory(pathFolder);
        ScreenRecorderService.getRecorder().setListScreenshots(listScreenshots);
        List<File> list = ScreenRecorderService.getRecorder().getListScreenshots();
        adapter.updateList(list);
        if (list != null && list.size() > 0)
            lbl_no_data.setVisibility(View.GONE);
        else
            lbl_no_data.setVisibility(View.VISIBLE);
    }
}