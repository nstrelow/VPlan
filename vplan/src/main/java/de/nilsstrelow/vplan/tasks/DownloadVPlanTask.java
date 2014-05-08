package de.nilsstrelow.vplan.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import de.nilsstrelow.vplan.R;
import de.nilsstrelow.vplan.activities.VertretungsplanActivity;
import de.nilsstrelow.vplan.constants.Device;
import de.nilsstrelow.vplan.constants.HandlerMsg;
import de.nilsstrelow.vplan.constants.Server;
import de.nilsstrelow.vplan.utils.FileUtils;
import de.nilsstrelow.vplan.utils.NetworkUtils;

/**
 * AsyncTask to download VPlans
 * Created by djnilse on 30.03.2014.
 */
public class DownloadVPlanTask extends AsyncTask<String, String, Boolean> {

    private static boolean UPDATED;
    private final String TAG = DownloadVPlanTask.class.getSimpleName();
    private final Context context;
    public File timestamp;
    private String vplanPath;

    public DownloadVPlanTask(Context context, String path) {
        this.context = context;
        this.vplanPath = path;
        timestamp = new File(Device.TIMESTAMP_PATH);
        UPDATED = false;
    }

    @Override
    protected Boolean doInBackground(String... schoolClassName) {
        updateGeneric();
        return updateMyClass(schoolClassName[0]);
    }

    private boolean updateAllClasses() {
        String onlineTimestamp = NetworkUtils.getFile(Server.ZS_TIMESTAMP_URL);
        String localTimestamp = FileUtils.readFile(Device.TIMESTAMP_PATH);

        if (!onlineTimestamp.equals(localTimestamp)) {

            UPDATED = true;

            String[] onlineLines = onlineTimestamp.split("\n", -1);
            String[] localLines = localTimestamp.split("\n", -1);

            Set<String> classesToBeUpdated = new HashSet<String>(Arrays.asList(onlineLines));
            classesToBeUpdated.removeAll(new HashSet<String>(Arrays.asList(localLines)));

            for (String schoolClassToBeUpdated : classesToBeUpdated) {
                String schoolClass = schoolClassToBeUpdated.substring(1, schoolClassToBeUpdated.lastIndexOf("/"));
                updateSchoolClass(schoolClass);
                Log.i(TAG, "Class updated: " + schoolClass);
            }
            FileUtils.saveFile(onlineTimestamp, Device.TIMESTAMP_PATH);
        }
        return UPDATED;
    }

    private boolean updateMyClass(String schoolClassName) {

        String onlineTimestamp = NetworkUtils.getFile(Server.ZS_PLAN_URL + schoolClassName + "/timestamp");
        String localTimestamp = FileUtils.readFile(Device.VPLAN_PATH + schoolClassName + "/timestamp");

        if (!onlineTimestamp.equals(localTimestamp)) {

            UPDATED = true;
            updateSchoolClass(schoolClassName);
            Log.i(TAG, "Class updated: " + schoolClassName);
        }
        return UPDATED;
    }

    /* Update von Allgemeinen Bemerkungen */
    private boolean updateGeneric() {

        String schoolClassName = "generic";

        String onlineTimestamp = NetworkUtils.getFile(Server.ZS_PLAN_URL + schoolClassName + "/timestamp");
        String localTimestamp = FileUtils.readFile(Device.VPLAN_PATH + schoolClassName + "/timestamp");

        if (!onlineTimestamp.equals(localTimestamp)) {

            UPDATED = true;
            updateSchoolClass(schoolClassName);
            Log.i(TAG, "Class updated: " + schoolClassName);
        }
        return UPDATED;
    }

    private void updateSchoolClass(String schoolClass) {

        /* update Subtitle */
        publishProgress(schoolClass);

        String localDirPath = Device.VPLAN_PATH + schoolClass;
        String localTimestampPath = localDirPath + "/timestamp";

        String onlineClassTimestamp = NetworkUtils.getFile(Server.ZS_PLAN_URL + schoolClass + "/timestamp");
        String localClassTimestamp;

        File dir = new File(localDirPath);
        if (!dir.exists()) {
            dir.mkdirs();
            localClassTimestamp = "";
        } else {
            localClassTimestamp = FileUtils.readFile(localTimestampPath);
        }

        String newPlanServerUrl;
        String newPlanDevicePath;

        if (!onlineClassTimestamp.equals(localClassTimestamp)) {

            String[] onlineLines = onlineClassTimestamp.split("\n", -1);
            String[] localLines = localClassTimestamp.split("\n", -1);

            Set<String> plansToBeUpdated = new HashSet<String>(Arrays.asList(onlineLines));
            plansToBeUpdated.removeAll(new HashSet<String>(Arrays.asList(localLines)));

            for (String planToBeUpdated : plansToBeUpdated) {
                String filename = planToBeUpdated.substring(0, planToBeUpdated.indexOf("'"));
                newPlanServerUrl = Server.ZS_PLAN_URL + schoolClass + "/" + filename;
                newPlanDevicePath = localDirPath + "/" + filename;
                NetworkUtils.downloadFileTo(newPlanServerUrl, newPlanDevicePath);
                Log.i(TAG, "Plan updated: " + filename);
            }
        }
        /* Update class timestamp */
        FileUtils.saveFile(onlineClassTimestamp, Device.VPLAN_PATH + schoolClass + "/timestamp");
    }

    @Override
    protected void onPreExecute() {
        Message msg = new Message();
        if (NetworkUtils.isInternetAvailable(context)) {
            msg.obj = context.getResources().getString(R.string.check_update_plan_msg);
            msg.what = HandlerMsg.STARTING_DOWNLOADING_PLAN;
            VertretungsplanActivity.handler.sendMessage(msg);
            new File(vplanPath).mkdir();
        } else {
            msg.obj = context.getResources().getString(R.string.no_connection_msg);
            msg.what = HandlerMsg.CROUTON_ALERT;
            VertretungsplanActivity.handler.sendMessage(msg);
            msg = new Message();
            msg.what = HandlerMsg.FINISHED_DOWNLOADING_PLAN;
            VertretungsplanActivity.handler.sendMessage(msg);
            cancel(true);
        }

    }

    @Override
    protected void onProgressUpdate(String... schoolClass) {
        Message msg = new Message();
        msg.obj = schoolClass[0];
        msg.what = HandlerMsg.UPDATING;
        VertretungsplanActivity.handler.sendMessage(msg);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        Message msg;
        if (UPDATED) {
            VertretungsplanActivity.handler.sendEmptyMessage(HandlerMsg.FINISHED_DOWNLOADING_PLAN);
            VertretungsplanActivity.handler.sendEmptyMessage(HandlerMsg.UPDATED);
            msg = new Message();
            msg.obj = context.getResources().getString(R.string.plan_was_updated_msg);
            msg.what = HandlerMsg.CROUTON_CONFIRM;
            VertretungsplanActivity.handler.sendMessage(msg);
        } else {
            VertretungsplanActivity.handler.sendEmptyMessage(HandlerMsg.FINISHED_DOWNLOADING_PLAN);
            msg = new Message();
            msg.obj = context.getResources().getString(R.string.no_updates_msg);
            msg.what = HandlerMsg.CROUTON_INFO;
            VertretungsplanActivity.handler.sendMessage(msg);
        }

    }
}
