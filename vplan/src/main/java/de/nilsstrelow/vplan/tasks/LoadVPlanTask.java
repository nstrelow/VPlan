package de.nilsstrelow.vplan.tasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Message;

import java.io.File;

import de.nilsstrelow.vplan.R;
import de.nilsstrelow.vplan.activities.VertretungsplanActivity;
import de.nilsstrelow.vplan.constants.Device;
import de.nilsstrelow.vplan.constants.HandlerMsg;
import de.nilsstrelow.vplan.helpers.Entry;
import de.nilsstrelow.vplan.helpers.SchoolClass;
import de.nilsstrelow.vplan.helpers.SchoolDay;
import de.nilsstrelow.vplan.utils.FileUtils;

/**
 * Loads the local VPlan files into objects
 * Created by djnilse on 08.04.2014.
 */
public class LoadVPlanTask extends AsyncTask<String, Integer, SchoolClass> {

    private Activity activity;

    public LoadVPlanTask(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected SchoolClass doInBackground(String... schoolClassName) {

        String localTimeStampPath;
        String localSchoolClassPath;

        SchoolClass schoolClass = new SchoolClass();

        SchoolDay schoolDay;

        // paths
        localSchoolClassPath = Device.VPLAN_PATH + schoolClassName[0];
        localTimeStampPath = localSchoolClassPath + "/timestamp";

        File timestamp = new File(localTimeStampPath);

        if (timestamp.exists()) {

            String[] dayFiles = FileUtils.readFile(timestamp.getAbsolutePath()).split("\n");

            for (String dayFile : dayFiles) {

                if (!dayFile.equals("")) {

                    // make a new schoolDay with day
                    schoolDay = new SchoolDay(dayFile.substring(0, 8));

                    // all entries of a day
                    String[] entries = FileUtils.readFile(localSchoolClassPath + "/" + parseTimestamp(dayFile)).split("\n");

                    for (String line : entries) {
                        Entry adding = parseEntry(line);
                        adding.day = schoolDay.day;
                        schoolDay.addEntry(adding);
                    }
                    schoolClass.addDay(schoolDay);
                }
            }
        }
        schoolClass.sortDays();
        return schoolClass;
    }

    private Entry parseEntry(String line) {
        String[] data = line.split(",");
        return new Entry(data);
    }

    private String parseTimestamp(String timeStampLine) {
        return timeStampLine.substring(0, timeStampLine.indexOf("'"));
    }

    @Override
    protected void onPreExecute() {
        Message msg = new Message();
        msg.obj = activity.getResources().getString(R.string.load_plan_msg);
        msg.what = HandlerMsg.STARTING_LOADING_PLAN;
        VertretungsplanActivity.handler.sendMessage(msg);
    }

    @Override
    protected void onPostExecute(SchoolClass schoolClass) {
        Message msg = new Message();
        msg.obj = schoolClass;
        msg.what = HandlerMsg.FINISHED_LOADING_PLAN;
        VertretungsplanActivity.handler.sendMessage(msg);
    }
}
