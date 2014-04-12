package de.nilsstrelow.vplan.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;

import java.io.File;

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
public class DownloadVPlanTask extends AsyncTask<String, Integer, Integer> {

    private static final int UPDATED_VPLAN = 0;
    private static final int NOT_UPDATED_VPLAN = 1;
    private final Context context;
    public File timestamp;
    private String vplanPath;
    private String[] classes;

    public DownloadVPlanTask(Context context, String path) {
        this.context = context;
        this.vplanPath = path;
        timestamp = new File(Device.TIMESTAMP_PATH);
        classes = context.getResources().getStringArray(R.array.zs_classes);
    }

    @Override
    protected Integer doInBackground(String... fileStrings) {
        String onlineTimeStamp = "";
        String localTimeStamp = "";
        String localTimeStampPath = "";
        String localDirPath = "";
        String newPlanServerUrl = "";
        String newPlanDevicePath = "";
        for (String schoolClass : classes) {
            onlineTimeStamp = NetworkUtils.getFile(Server.ZS_PLAN_URL + schoolClass + "/timestamp");
            //Log.e("onlineTimeStamp", onlineTimeStamp);
            localDirPath = Device.VPLAN_PATH + schoolClass;
            localTimeStampPath = localDirPath + "/timestamp";
            File dir = new File(localDirPath);
            if (!dir.exists()) {
                dir.mkdirs();
                localTimeStamp = "";
            } else {
                localTimeStamp = FileUtils.readFile(localTimeStampPath);
            }

            // TODO : make it so only changed files are downloaded
            if (!onlineTimeStamp.equals(localTimeStamp)) {
                String[] lines = onlineTimeStamp.split("\n", -1);
                for (String line : lines) {
                    if (!line.equals("")) {
                        newPlanServerUrl = Server.ZS_PLAN_URL + schoolClass + "/" + line.substring(0, line.indexOf("'"));
                        newPlanDevicePath = localDirPath + "/" + line.substring(0, line.indexOf("'"));
                        NetworkUtils.saveFile(newPlanServerUrl, newPlanDevicePath);
                    }
                }
                NetworkUtils.saveFile(Server.ZS_PLAN_URL + schoolClass + "/timestamp", localTimeStampPath);
            }
        }
        String onlineTimestamps = NetworkUtils.getFile(Server.TIMESTAMP_URL);
        String localTimestamps = timestamp.exists() ? FileUtils.readFile(Device.TIMESTAMP_PATH) : "";

        if ((!timestamp.exists() || !localTimestamps.equals(onlineTimestamps)) && !this.isCancelled()) {
            onProgressUpdate(0);
            String[] lines = onlineTimestamps.split("\\r?\\n");
            for (String line : lines) {
                String fileName = line.substring(4, 12) + ".txt";
                NetworkUtils.saveFile(Server.VPLAN_URL + fileName, Device.VPLAN_PATH + "/" + fileName);
            }
            NetworkUtils.saveFile(Server.TIMESTAMP_URL, Device.TIMESTAMP_PATH);
            return UPDATED_VPLAN;
        } else {
            Log.v("TIMESTAMP", "SAME, NO UPDATE NEEDED");
            return NOT_UPDATED_VPLAN;
        }
    }

    @Override
    protected void onPreExecute() {
        Message msg = new Message();
        if (NetworkUtils.isInternetAvailable(context)) {
            msg.obj = context.getResources().getString(R.string.check_update_plan_msg);
            msg.what = HandlerMsg.LOADING;
            VertretungsplanActivity.handler.sendMessage(msg);
            new File(vplanPath).mkdir();
        } else {
            msg.obj = context.getResources().getString(R.string.no_connection_msg);
            msg.what = HandlerMsg.CROUTON_ALERT;
            VertretungsplanActivity.handler.sendMessage(msg);
            msg = new Message();
            msg.what = HandlerMsg.LOAD_STARTPAGE_MSG;
            VertretungsplanActivity.handler.sendMessage(msg);
            msg = new Message();
            msg.what = HandlerMsg.FINISHED_LOADING;
            VertretungsplanActivity.handler.sendMessage(msg);
            cancel(true);
        }

    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        Message msg = new Message();
        msg.obj = context.getResources().getString(R.string.update_plan_msg);
        msg.what = HandlerMsg.DOWNLOADING;
        VertretungsplanActivity.handler.sendMessage(msg);
    }

    @Override
    protected void onPostExecute(Integer result) {
        Message msg;
        switch (result) {
            case UPDATED_VPLAN:
                msg = new Message();
                msg.obj = context.getResources().getString(R.string.plan_was_updated_msg);
                msg.what = HandlerMsg.CROUTON_CONFIRM;
                VertretungsplanActivity.handler.sendMessage(msg);
                VertretungsplanActivity.handler.sendEmptyMessage(HandlerMsg.LOAD_VPLAN_MSG);
                break;
            case NOT_UPDATED_VPLAN:
                msg = new Message();
                msg.obj = context.getResources().getString(R.string.no_updates_msg);
                msg.what = HandlerMsg.CROUTON_INFO;
                VertretungsplanActivity.handler.sendMessage(msg);
                VertretungsplanActivity.handler.sendEmptyMessage(HandlerMsg.FINISHED_LOADING);
                VertretungsplanActivity.handler.sendEmptyMessage(HandlerMsg.NOT_UPDATED);
                break;
        }

    }
}
