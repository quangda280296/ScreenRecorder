package com.vmb.ads_in_app.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class GetPackages {

    public static List getAll(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent main = new Intent(Intent.ACTION_MAIN, null);
        main.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> packages = pm.queryIntentActivities(main, 0);

        ArrayList<String> app_name_list = new ArrayList<String>();
        ArrayList<String> app_package_list = new ArrayList<String>();

        for (ResolveInfo resolve_info : packages) {
            try {
                String package_name = resolve_info.activityInfo.packageName;
                String app_name = (String) pm.getApplicationLabel(pm.getApplicationInfo(package_name, PackageManager.GET_META_DATA));

                Log.i("GetAllPackage", app_name);

                boolean same = false;
                for (int i = 0; i < app_name_list.size(); i++) {
                    if (package_name.equals(app_package_list.get(i)))
                        same = true;
                }
                if (!same) {
                    app_name_list.add(app_name);
                    app_package_list.add(package_name);
                }

            } catch (Exception e) {

            }
        }

        return app_package_list;
    }

    public boolean checkPackageExist(Context context, String packgName) {
        PackageManager pm = context.getPackageManager();
        Intent main = new Intent(Intent.ACTION_MAIN, null);
        main.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> packages = pm.queryIntentActivities(main, 0);

        ArrayList<String> app_name_list = new ArrayList<String>();
        ArrayList<String> app_package_list = new ArrayList<String>();

        for (ResolveInfo resolve_info : packages) {
            try {
                String package_name = resolve_info.activityInfo.packageName;
                String app_name = (String) pm.getApplicationLabel(pm.getApplicationInfo(package_name, PackageManager.GET_META_DATA));

                boolean same = false;
                for (int i = 0; i < app_name_list.size(); i++) {
                    if (package_name.equals(app_package_list.get(i)))
                        same = true;
                }
                if (!same) {
                    app_name_list.add(app_name);
                    app_package_list.add(package_name);
                }

            } catch (Exception e) {

            }
        }

        for (String pkg : app_package_list) {
            if(pkg.equals(packgName))
                return true;
        }

        return false;
    }
}