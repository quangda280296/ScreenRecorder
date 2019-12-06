package com.screen.recorder.adapter;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupMenu;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.screen.recorder.Config;
import com.screen.recorder.R;
import com.screen.recorder.activity.WatchVideoActivity;
import com.screen.recorder.adapter.holder.VideoViewHolder;
import com.screen.recorder.base.adapter.BaseAdapter;
import com.screen.recorder.base.adapter.holder.BaseViewHolder;
import com.screen.recorder.fragment.ScreenshotsFragment;
import com.screen.recorder.fragment.VideosFragment;
import com.screen.recorder.service.ScreenRecorderService;
import com.screen.recorder.utils.Utils;
import com.vmb.ads_in_app.util.OnTouchClickListener;
import com.vmb.ads_in_app.util.PermissionUtil;
import com.vmb.ads_in_app.util.ShareRateUtil;
import com.vmb.ads_in_app.util.ToastUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VideoAdapter extends BaseAdapter {

    private List<File> listVideos;

    public VideoAdapter(Context context, List list) {
        super(context, list);
        this.listVideos = list;
    }

    @Override
    public void updateList(List listData) {
        this.listVideos = listData;
        notifyDataSetChanged();
    }

    @Override
    protected int getResLayout() {
        return R.layout.row_video;
    }

    @Override
    protected BaseViewHolder getViewHolder(ViewGroup viewGroup) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return new VideoViewHolder(inflater.inflate(getResLayout(), viewGroup, false));
    }

    @Override
    protected void bindView(RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder instanceof VideoViewHolder) {
            VideoViewHolder holder = (VideoViewHolder) viewHolder;

            Glide.with(context)
                    .load(listVideos.get(position).getPath())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .override(160, 80)
                    .placeholder(R.drawable.img_empty_photo)
                    .into(holder.img_thumbnail);

            holder.lbl_name.setText(listVideos.get(position).getName());
            holder.lbl_size.setText(String.format("%.2f", listVideos.get(position).length() / Math.pow(1024, 2)) + " MB");

            holder.layout_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, WatchVideoActivity.class);
                    intent.putExtra(Config.URL_FILE, listVideos.get(position).getPath());
                    context.startActivity(intent);
                }
            });

            holder.layout_menu.setOnTouchListener(new OnTouchClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu pm = new PopupMenu(context, v);
                    pm.getMenuInflater().inflate(R.menu.popup_menu, pm.getMenu());
                    pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.play:
                                    Intent intent = new Intent(context, WatchVideoActivity.class);
                                    intent.putExtra(Config.URL_FILE, listVideos.get(position).getPath());
                                    context.startActivity(intent);
                                    break;

                                case R.id.edit_name:
                                    final InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

                                    String[] words = listVideos.get(position).getName().split("\\.");
                                    int length = words.length;

                                    String extension = "";
                                    String name = "";
                                    if (length >= 2) {
                                        extension = words[length - 1];
                                        name = words[length - 2];
                                    }

                                    LayoutInflater inflater = LayoutInflater.from(context);
                                    View layout_edit_name = inflater.inflate(R.layout.layout_edit_name, null);

                                    final EditText txt_edit_name = layout_edit_name.findViewById(R.id.txt_edit_name);
                                    txt_edit_name.setText(name);
                                    txt_edit_name.setSelectAllOnFocus(true);

                                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
                                    builder.setTitle(context.getString(R.string.change_name));
                                    builder.setView(layout_edit_name);

                                    final StringBuilder newName = new StringBuilder();
                                    newName.append(".");
                                    newName.append(extension);

                                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String nameEdit = txt_edit_name.getText().toString();
                                            if (TextUtils.isEmpty(nameEdit))
                                                return;

                                            newName.insert(0, nameEdit);
                                            boolean result = Utils.changeName(listVideos.get(position), newName.toString());
                                            if (result) {
                                                ToastUtil.shortToast(context, context.getString(R.string.change_name_successfully));
                                                FragmentManager manager = ((Activity) context).getFragmentManager();
                                                ScreenshotsFragment fragment = (ScreenshotsFragment) manager.findFragmentByTag(Config.FragmentTag.Fragment_Screenshot);
                                                fragment.updateData();
                                                dialog.dismiss();
                                            } else {
                                                ToastUtil.longToast(context, "Error occur! Please try again later");
                                            }
                                            dialog.cancel();
                                        }
                                    });

                                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                            dialog.cancel();
                                        }
                                    });

                                    android.app.AlertDialog alertDialog = builder.create();
                                    alertDialog.show();
                                    break;

                                case R.id.delete:
                                    ScreenRecorderService.getRecorder().setFileName(listVideos.get(position).getPath());

                                    if (PermissionUtil.requestPermission((Activity) context,
                                            Config.RequestCode.REQUEST_CODE_PERMISSION_DELETE,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE))
                                        deleteVideo(new File(listVideos.get(position).getPath()));
                                    break;

                                case R.id.share:
                                    ShareRateUtil.shareMore(context, listVideos.get(position).getPath(),
                                            context.getString(R.string.app_name));
                                    break;

                                default:
                                    break;
                            }

                            return false;
                        }
                    });
                    pm.show();
                }
            }, context));

            long time = Utils.getDurationFile(listVideos.get(position).getPath());
            long m = time / 60;
            long s = time % 60;
            String minutes = m + "";

            String seconds = s + "";
            if (s < 10)
                seconds = "0" + seconds;
            holder.lbl_duration.setText(minutes + ":" + seconds);
        }
    }

    private void deleteVideo(final File file) {
        if (file != null) {
            if (file.exists()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setCancelable(true);
                builder.setMessage(R.string.confirm_delete_video);
                builder.setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (file.delete()) {
                            ToastUtil.longToast(context, context.getString(R.string.video_deleted));
                            FragmentManager manager = ((Activity) context).getFragmentManager();
                            VideosFragment fragment = (VideosFragment) manager.findFragmentByTag(Config.FragmentTag.Fragment_Video);
                            fragment.updateData();
                            dialog.dismiss();
                        } else
                            ToastUtil.longToast(context, context.getString(R.string.delete_fail));
                    }
                });
                builder.setPositiveButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        }
    }

    @Override
    public int getItemCount() {
        if (listVideos == null)
            listVideos = new ArrayList<>();
        return listVideos.size();
    }
}