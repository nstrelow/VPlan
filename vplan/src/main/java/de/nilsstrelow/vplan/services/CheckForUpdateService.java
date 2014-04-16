package de.nilsstrelow.vplan.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import de.nilsstrelow.vplan.R;
import de.nilsstrelow.vplan.activities.Settings;
import de.nilsstrelow.vplan.activities.VertretungsplanActivity;
import de.nilsstrelow.vplan.constants.Device;
import de.nilsstrelow.vplan.constants.Server;
import de.nilsstrelow.vplan.utils.FileUtils;
import de.nilsstrelow.vplan.utils.NetworkUtils;

/**
 * Service to check for update
 * Created by djnilse on 12/2/13.
 */
public class CheckForUpdateService extends Service {

    public static final String TAG = CheckForUpdateService.class.getSimpleName();

    public static final int NEED_UPDATE = 0;
    public static final int NOT_NEED_UPDATE = 1;
    public static Handler handler;
    private String schoolClassName;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {

                    case NEED_UPDATE:
                        Intent resultIntent = new Intent(getApplicationContext(), VertretungsplanActivity.class);
                        PendingIntent resultPendingIntent =
                                PendingIntent.getActivity(
                                        getApplicationContext(),
                                        0,
                                        resultIntent,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                );

                        long[] pattern = {0, 300, 100, 300, 100};

                        NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(getApplicationContext())
                                        .setSmallIcon(R.drawable.ic_zs_drawer_white)
                                        .setContentTitle("V-Plan")
                                        .setContentText("Neuer Plan für Klasse " + schoolClassName)
                                        .setVibrate(pattern)
                                        .setOnlyAlertOnce(true)
                                        .setAutoCancel(true)
                                        .setContentIntent(resultPendingIntent);

                        int mNotificationId = 8361;
                        NotificationManager mNotifyMgr =
                                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        mNotifyMgr.notify(mNotificationId, mBuilder.build());
                        break;

                    case NOT_NEED_UPDATE:
                        break;
                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        boolean checkForUpdate = sharedPref.getBoolean(Settings.CHECK_FOR_UPDATE, true);

        schoolClassName = sharedPref.getString(Settings.MY_SCHOOL_CLASS_PREF, "5a");

        if (checkForUpdate) {
            if (NetworkUtils.isInternetAvailable(this)) {
                new CheckForUpdateTask(schoolClassName).execute();
            }
        }
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static class CheckForUpdateTask extends AsyncTask<String, Integer, Boolean> {

        private String schoolClassName;

        public CheckForUpdateTask(String schoolClassName) {
            this.schoolClassName = schoolClassName;
        }

        @Override
        protected Boolean doInBackground(String... fileStrings) {
            String localTimestampPath = Device.VPLAN_PATH + schoolClassName + Device.TIMESTAMP;
            String onlineTimestampPath = Server.ZS_PLAN_URL + schoolClassName + Server.TIMESTAMP;
            String localTimestamp = FileUtils.readFile(localTimestampPath);
            String onlineTimestamp = NetworkUtils.getFile(onlineTimestampPath);
            return !onlineTimestamp.equals(localTimestamp);
        }

        @Override
        protected void onPreExecute() {
            Log.v(TAG, "starting");
        }

        @Override
        protected void onPostExecute(Boolean needsUpdate) {
            handler.sendEmptyMessage(needsUpdate ? NEED_UPDATE : NOT_NEED_UPDATE);
        }
    }
}
