package de.nilsstrelow.vplan.constants;

import android.os.Environment;

/**
 * constant device local paths
 * Created by djnilse on 09.04.2014.
 */
public class Device {
    public static final String VPLAN_PATH = Environment.getExternalStorageDirectory() + "/" + ".zs-vertretungsplan/";
    public static final String TIMESTAMP_PATH = VPLAN_PATH + "timestamp";
    public static final String GENERIC_MSG_PATH = VPLAN_PATH + "generic/";
}
