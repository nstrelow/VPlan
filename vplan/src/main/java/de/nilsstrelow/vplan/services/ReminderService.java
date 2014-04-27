package de.nilsstrelow.vplan.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;

import java.security.InvalidParameterException;

import de.nilsstrelow.vplan.R;
import de.nilsstrelow.vplan.activities.VertretungsplanActivity;
import de.nilsstrelow.vplan.helpers.Entry;

/**
 * Service to check for update
 * Created by djnilse on 12/2/13.
 */
public class ReminderService extends Service {

    public static final String TAG = ReminderService.class.getSimpleName();

    public static final String SELECTED_ENTRY_KEY = "selected_entry_key";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "Notify");

        Entry selectedEntry = (Entry) intent.getSerializableExtra(SELECTED_ENTRY_KEY);

        String compactTitle = "Erinnerung für " + selectedEntry.stunde + ". Stunde";
        String expandedTitle = selectedEntry.stunde + ". Stunde Details:";

        long[] pattern = {0, 300, 100, 300, 100};

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplication())
                        .setSmallIcon(R.drawable.ic_zs_drawer_white)
                        .setVibrate(pattern)
                        .setAutoCancel(true)
                        .setContentTitle(compactTitle)
                        .setContentText(selectedEntry.bemerkung);

        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();

        inboxStyle.setBigContentTitle(expandedTitle);

        String[] lines = new String[4];
        lines[0] = getString(R.string.notification_line_vertreter, selectedEntry.vertreter);
        lines[1] = getString(R.string.notification_line_fach, selectedEntry.fach);
        lines[2] = getString(R.string.notification_line_raum, selectedEntry.raum);
        lines[3] = getString(R.string.notification_line_bemerkung, selectedEntry.bemerkung);

        String[] linesUnformatted = new String[4];
        linesUnformatted[0] = getString(R.string.notification_line_vertreter);
        linesUnformatted[1] = getString(R.string.notification_line_fach);
        linesUnformatted[2] = getString(R.string.notification_line_raum);
        linesUnformatted[3] = getString(R.string.notification_line_bemerkung);

        for (int i = 0; i < lines.length; i++) {
            int titleLength = linesUnformatted[i].indexOf("%1$s");
            if (titleLength < 0) {
                throw new InvalidParameterException("Something's wrong with your string! LINT could have caught that.");
            }
            Spannable sb = new SpannableString(lines[i]);
            sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, titleLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            inboxStyle.addLine(sb);
        }
        mBuilder.setStyle(inboxStyle);

        Intent resultIntent = new Intent(getApplication(), VertretungsplanActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplication());
        stackBuilder.addParentStack(VertretungsplanActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(4561, mBuilder.build());


        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
