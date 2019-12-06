package com.vmb.ads_in_app.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.vmb.ads_in_app.LibrayData;
import com.vmb.ads_in_app.util.FireAnaUtil;
import com.vmb.ads_in_app.util.TimeRegUtil;

import java.text.DateFormat;
import java.util.Calendar;

public class AlarmUtil {

    public static void setAlarm(Context context) {
        if (context == null)
            return;

        putTimeUsed(context);
        long time_install = Long.parseLong(TimeRegUtil.getTimeRegister(context));
        long time_now = System.currentTimeMillis() / 1000;

        int offset = 60;
        if (time_now - time_install <= 1 * 60 * 60)
            offset = 10;

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, offset);

        // Turn on alarm
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent receiver = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                LibrayData.RequestCode.REQUEST_CODE_ALARM, receiver, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), offset * 60 * 1000, pendingIntent);

        DateFormat format = DateFormat.getInstance();
        Log.i("setAlarmUtil", format.format(cal.getTime()));
    }

    public static void putTimeUsed(Context context) {
        long time_install = Long.parseLong(TimeRegUtil.getTimeRegister(context));
        long time_now = System.currentTimeMillis() / 1000;

        long TIME_USED = (time_now - time_install) / 60;
        FireAnaUtil.setProperty(context, LibrayData.UserProperties.TIME_USED, (int) TIME_USED);
        Log.i("setAlarmUtil", "time_used = " + TIME_USED + " minutes");
    }
}