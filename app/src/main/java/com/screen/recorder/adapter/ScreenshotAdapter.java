package com.screen.recorder.adapter;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.screen.recorder.Config;
import com.screen.recorder.R;
import com.screen.recorder.activity.ZoomablePhotoActivity;
import com.screen.recorder.adapter.holder.ScreenshotViewHolder;
import com.screen.recorder.base.adapter.BaseAdapter;
import com.screen.recorder.base.adapter.holder.BaseViewHolder;
import com.screen.recorder.fragment.ScreenshotsFragment;
import com.screen.recorder.service.ScreenRecorderService;
import com.screen.recorder.utils.Utils;
import com.vmb.ads_in_app.util.PermissionUtil;
import com.vmb.ads_in_app.util.ShareRateUtil;
import com.vmb.ads_in_app.util.ToastUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ScreenshotAdapter extends BaseAdapter {

    private List<File> listScreenshots;
    private String[] menu;

    public ScreenshotAdapter(Context context, List list) {
        super(context, list);
        this.listScreenshots = list;
        this.menu = context.getResources().getStringArray(R.array.menu);
    }

    @Override
    public void updateList(List listData) {
        this.listScreenshots = listData;
        notifyDataSetChanged();
    }

    @Override
    protected int getResLayout() {
        return R.layout.row_screenshot;
    }

    @Override
    protected BaseViewHolder getViewHolder(ViewGroup viewGroup) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return new ScreenshotViewHolder(inflater.inflate(getResLayout(), viewGroup, false));
    }

    @Override
    protected void bindView(RecyclerView.ViewHolder viewHolder, final int position) {

        if (viewHolder instanceof ScreenshotViewHolder) {
            ScreenshotViewHolder holder = (ScreenshotViewHolder) viewHolder;

            holder.lbl_screenshot.setText(listScreenshots.get(position).getName());
            holder.layout_screenshot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ZoomablePhotoActivity.class);
                    intent.putExtra(Config.URL_FILE, listScreenshots.get(position).getPath());
                    context.startActivity(intent);
                }
            });

            holder.layout_screenshot.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!(context instanceof Activity))
                        return false;

                    new AlertDialog.Builder(context)
                            .setTitle(R.string.menu)
                            .setItems(menu, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, final int po) {
                                    switch (po) {
                                        case 0:
                                            Intent intent = new Intent(context, ZoomablePhotoActivity.class);
                                            intent.putExtra(Config.URL_FILE, listScreenshots.get(position).getPath());
                                            context.startActivity(intent);
                                            break;

                                        case 1:
                                            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

                                            String[] words = listScreenshots.get(position).getName().split("\\.");
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

                                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
                                                    boolean result = Utils.changeName(listScreenshots.get(position), newName.toString());
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
                                                    dialog.cancel();
                                                }
                                            });

                                            AlertDialog alertDialog = builder.create();
                                            alertDialog.show();
                                            break;

                                        case 2:
                                            ScreenRecorderService.getRecorder().setFileName(listScreenshots.get(position).getPath());

                                            if (PermissionUtil.requestPermission((Activity) context,
                                                    Config.RequestCode.REQUEST_CODE_PERMISSION_DELETE,
                                                    Manifest.permission.WRITE_EXTERNAL_STORAGE))
                                                deletePhoto(new File(listScreenshots.get(position).getPath()));
                                            break;

                                        case 3:
                                            ShareRateUtil.shareMore(context, listScreenshots.get(position).getPath(),
                                                    context.getString(R.string.app_name));
                                            break;
                                    }
                                }
                            })
                            .create()
                            .show();
                    return false;
                }
            });

            Glide.with(context)
                    .load(Uri.fromFile(listScreenshots.get(position)))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .override(640, 720)
                    .placeholder(R.drawable.img_empty_photo)
                    .into(holder.img_screenshot);
        }
    }

    private void deletePhoto(final File file) {
        if (file != null) {
            if (file.exists()) {
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
                builder.setCancelable(true);
                builder.setMessage(R.string.confirm_delete_screenshots);
                builder.setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (file.delete()) {
                            ToastUtil.longToast(context, context.getString(R.string.screenshots_deleted));
                            FragmentManager manager = ((Activity) context).getFragmentManager();
                            ScreenshotsFragment fragment = (ScreenshotsFragment) manager.findFragmentByTag(Config.FragmentTag.Fragment_Screenshot);
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
        if (listScreenshots == null)
            listScreenshots = new ArrayList<>();
        return listScreenshots.size();
    }
}