package de.nilsstrelow.vplan.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import de.nilsstrelow.vplan.R;
import de.nilsstrelow.vplan.activities.Settings;
import de.nilsstrelow.vplan.activities.VertretungsplanActivity;
import de.nilsstrelow.vplan.constants.Device;
import de.nilsstrelow.vplan.constants.Server;

/**
 * Service to check for update
 * Created by djnilse on 12/2/13.
 */
public class CheckForPlanService extends Service {

    public static final int NEED_UPDATE = 0;
    public static final int NOT_NEED_UPDATE = 1;

    public static Handler handler;

    public static boolean isInternetAvailable(Context context) {

        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();

        if (info == null || !info.isConnected() || info.isRoaming()) {
            return false;
        }
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case NEED_UPDATE:
                        //Toast.makeText(getApplicationContext(), "Needs update", Toast.LENGTH_SHORT).show();

                        Intent resultIntent = new Intent(getApplicationContext(), VertretungsplanActivity.class);
                        // Because clicking the notification opens a new ("special") activity, there's
                        // no need to create an artificial back stack.
                        PendingIntent resultPendingIntent =
                                PendingIntent.getActivity(
                                        getApplicationContext(),
                                        0,
                                        resultIntent,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                );

                        NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(getApplicationContext())
                                        .setSmallIcon(R.drawable.ic_zs_drawer_white)
                                        .setContentTitle("ZS-Plan")
                                        .setContentText("Neuer Vertretungsplan")
                                        .setAutoCancel(true)
                                        .setContentIntent(resultPendingIntent);

                        // Sets an ID for the notification
                        int mNotificationId = 8361;
                        // Gets an instance of the NotificationManager service
                        NotificationManager mNotifyMgr =
                                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        // Builds the notification and issues it.
                        mNotifyMgr.notify(mNotificationId, mBuilder.build());

                        break;
                    case NOT_NEED_UPDATE:
                        //Toast.makeText(getApplicationContext(), "Does not need an update", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        boolean checkForUpdate = sharedPref.getBoolean(Settings.CHECK_FOR_UPDATE, true);

        if (checkForUpdate) {
            if (isInternetAvailable(this)) {
                new CheckForUpdateTask(this).execute();
            }
        }
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static class CheckForUpdateTask extends AsyncTask<String, Integer, Integer> {

        private static final int UPDATE_VPLAN = 0;
        private static final int NO_UPDATE_VPLAN = 1;
        public static File timestamp;
        //private final Context context;
        public String localTimestamps;

        public CheckForUpdateTask(Context context) {
            //this.context = context;
            timestamp = new File(Device.TIMESTAMP_PATH);

        }

        private String getLocalTimestamps() {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(timestamp));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            StringBuilder sb = null;

            try {
                sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append('\n');
                    line = br.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (sb != null) {
                return sb.toString();
            } else {
                Log.e(CheckForPlanService.class.getName(), "getLocalTimestamps() is null");
                cancel(true);
                return "";
            }
        }

        public String getVPSTimestamps(String sUrl) {
            StringBuilder sb = null;
            try {
                URL url = new URL(sUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setDoOutput(true);
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(30000);
                connection.connect();

                InputStream in = connection.getInputStream();

                BufferedReader rd = new BufferedReader(new InputStreamReader(in));
                String line;
                sb = new StringBuilder();
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                    sb.append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (sb != null) {
                return sb.toString();
            } else {
                Log.e(CheckForPlanService.class.getName(), "getVPSTimestamps() is not returning something");
                cancel(true);
                return localTimestamps; // return localTimestamps in case getVPSTimestamps() fails, so ZS Plan doesn't crash
            }

        }

        @Override
        protected Integer doInBackground(String... fileStrings) {

            localTimestamps = timestamp.exists() ? getLocalTimestamps() : "";
            String onlineTimestamps = getVPSTimestamps(Server.TIMESTAMP_URL_OLD);

            if (timestamp.exists() && localTimestamps.equals(onlineTimestamps)) {
                Log.v("CheckForPlanService", "SAME, NO UPDATED NEEDED");
                return NO_UPDATE_VPLAN;
            } else {
                return UPDATE_VPLAN;
            }
        }

        @Override
        protected void onPreExecute() {
            Log.v("CheckForPlanService", "start Service to check");
        }

        @Override
        protected void onPostExecute(Integer result) {
            switch (result) {
                case UPDATE_VPLAN:
                    handler.sendEmptyMessage(NEED_UPDATE);
                    break;
                case NO_UPDATE_VPLAN:
                    handler.sendEmptyMessage(NOT_NEED_UPDATE);
                    break;
            }

        }
    }
}
