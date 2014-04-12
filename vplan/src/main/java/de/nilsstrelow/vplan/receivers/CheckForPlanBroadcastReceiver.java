package de.nilsstrelow.vplan.receivers;

/**
 * BroadcastReceiver to start AlarmManager to check for update
 * Created by djnilse on 12/2/13.
 */

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;

import de.nilsstrelow.vplan.activities.Settings;
import de.nilsstrelow.vplan.services.CheckForPlanService;

public class CheckForPlanBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Calendar cal = Calendar.getInstance();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        boolean checkForUpdate = sharedPref.getBoolean(Settings.CHECK_FOR_UPDATE, true);

        // only start hourly update if preference for it is true
        if (checkForUpdate) {

            Intent i = new Intent(context, CheckForPlanService.class);
            PendingIntent pendingIntent = PendingIntent.getService(context,
                    14672, i, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager am = (AlarmManager) context
                    .getSystemService(Activity.ALARM_SERVICE);

            am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                    AlarmManager.INTERVAL_HOUR, pendingIntent);

            Log.v(CheckForPlanBroadcastReceiver.class.getSimpleName(), "start PendingIntent");
        }
    }
    /*
    public void setAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, CheckForPlanService.class);
        PendingIntent pi = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_HOUR, pi);
    }

    public void cancelAlarm(Context context) {
        Intent intent = new Intent(context, CheckForPlanService.class);
        PendingIntent sender = PendingIntent.getService(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
    */
}
