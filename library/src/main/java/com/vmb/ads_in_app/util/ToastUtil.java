package com.vmb.ads_in_app.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ToastUtil {

    public static void shortToast(Context context, String text) {
        if(context == null)
            return;

        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public static void longToast(Context context, String text) {
        if(context == null)
            return;

        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    public static void shortSnackbar(Activity activity, String text) {
        if(activity == null)
            return;

        Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), text, Snackbar.LENGTH_SHORT);

        View view = snackbar.getView();
        view.setBackgroundColor(Color.WHITE);
        ((TextView) view.findViewById(android.support.design.R.id.snackbar_text)).setTextColor(Color.BLACK);
        ((TextView) view.findViewById(android.support.design.R.id.snackbar_action)).setTextColor(Color.GREEN);
        snackbar.show();
    }

    public static void longSnackbar(Activity activity, String text) {
        if(activity == null)
            return;

        Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG);

        View view = snackbar.getView();
        view.setBackgroundColor(Color.WHITE);
        ((TextView) view.findViewById(android.support.design.R.id.snackbar_text)).setTextColor(Color.BLACK);
        ((TextView) view.findViewById(android.support.design.R.id.snackbar_action)).setTextColor(Color.GREEN);
        snackbar.show();
    }

    public static void customShortSnackbar(Activity activity, String text, String click, View.OnClickListener onClickListener) {
        if(activity == null)
            return;

        Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), text, Snackbar.LENGTH_SHORT)
                .setAction(click, onClickListener);

        View view = snackbar.getView();
        view.setBackgroundColor(Color.BLACK);
        ((TextView) view.findViewById(android.support.design.R.id.snackbar_text)).setTextColor(Color.WHITE);
        ((TextView) view.findViewById(android.support.design.R.id.snackbar_action)).setTextColor(Color.GREEN);
        snackbar.show();
    }

    public static void customLongSnackbar(Activity activity, String text, String click, View.OnClickListener onClickListener) {
        if(activity == null)
            return;

        Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG)
                .setAction(click, onClickListener);

        View view = snackbar.getView();
        view.setBackgroundColor(Color.BLACK);
        ((TextView) view.findViewById(android.support.design.R.id.snackbar_text)).setTextColor(Color.WHITE);
        ((TextView) view.findViewById(android.support.design.R.id.snackbar_action)).setTextColor(Color.GREEN);
        snackbar.show();
    }
}