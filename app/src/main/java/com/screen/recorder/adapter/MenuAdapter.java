package com.screen.recorder.adapter;

import android.app.Activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.screen.recorder.Config;
import com.screen.recorder.R;
import com.screen.recorder.activity.WebviewActivity;
import com.screen.recorder.adapter.holder.MenuHolder;
import com.screen.recorder.base.adapter.BaseAdapter;
import com.screen.recorder.base.adapter.holder.BaseViewHolder;
import com.screen.recorder.fragment.SettingFragment;
import com.vmb.ads_in_app.model.AdsConfig;
import com.vmb.ads_in_app.util.ShareRateUtil;

import java.util.List;

public class MenuAdapter extends BaseAdapter {

    public MenuAdapter(Context context, List list) {
        super(context, list);
    }

    @Override
    protected int getResLayout() {
        return R.layout.row_menu;
    }

    @Override
    protected BaseViewHolder getViewHolder(ViewGroup viewGroup) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return new MenuHolder(inflater.inflate(getResLayout(), viewGroup, false));
    }

    @Override
    protected void bindView(RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder instanceof MenuHolder) {
            MenuHolder holder = (MenuHolder) viewHolder;

            holder.lbl_title.setText(context.getString(Config.resource_title_menu[position]));
            holder.img_icon.setImageResource(Config.resource_icon_menu[position]);
            holder.layout_menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (context instanceof Activity) {
                        Activity activity = (Activity) context;

                        switch (position) {
                            case 0:
                                ShareRateUtil.shareApp(activity);
                                break;

                            case 1:
                                ShareRateUtil.rateApp(activity);
                                break;

                            case 2:
                                String uri = AdsConfig.getInstance().getLink_more_apps();
                                if (TextUtils.isEmpty(uri))
                                    uri = "https://play.google.com/store/apps/developer?id=Fruits+Studio";

                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(uri));
                                activity.startActivity(intent);
                                break;

                            case 3:
                                FragmentManager manager = activity.getFragmentManager();
                                FragmentTransaction transaction = manager.beginTransaction()
                                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                                transaction.replace(R.id.main_content, new SettingFragment(), Config.FragmentTag.Fragment_Setting);
                                transaction.addToBackStack(Config.FragmentTag.Fragment_Setting);
                                transaction.commit();
                                DrawerLayout drawer = activity.findViewById(R.id.drawer_layout);
                                if (drawer.isDrawerOpen(GravityCompat.START))
                                    drawer.closeDrawer(GravityCompat.START, true);
                                break;

                            case 4:
                                Intent web = new Intent(context, WebviewActivity.class);
                                context.startActivity(web);
                                break;

                            default:
                                break;
                        }
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return getList().size();
    }
}