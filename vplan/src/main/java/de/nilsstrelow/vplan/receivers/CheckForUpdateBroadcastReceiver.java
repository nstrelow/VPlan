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
import de.nilsstrelow.vplan.services.CheckForUpdateService;

public class CheckForUpdateBroadcastReceiver extends BroadcastReceiver {

    private final String TAG = CheckForUpdateBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        Calendar cal = Calendar.getInstance();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        boolean checkForUpdate = sharedPref.getBoolean(Settings.CHECK_FOR_UPDATE, true);

        if (checkForUpdate) {

            Intent i = new Intent(context, CheckForUpdateService.class);
            PendingIntent pendingIntent = PendingIntent.getService(context,
                    14672, i, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager am = (AlarmManager) context
                    .getSystemService(Activity.ALARM_SERVICE);

            am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                    AlarmManager.INTERVAL_HOUR, pendingIntent);

            Log.v(TAG, "start PendingIntent");
        }
    }
}
