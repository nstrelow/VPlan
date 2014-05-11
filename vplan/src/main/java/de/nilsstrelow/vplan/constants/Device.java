package de.nilsstrelow.vplan.constants;

import android.content.Context;
import android.os.Environment;
import android.preference.PreferenceManager;

import de.nilsstrelow.vplan.activities.Settings;

/**
 * constant device local paths
 * Created by djnilse on 09.04.2014.
 */
public class Device {
    public static final String VPLAN_PATH = Environment.getExternalStorageDirectory() + "/" + "vplan/";
    public static final String ZS_VPLAN_PATH = VPLAN_PATH + "zs/";
    public static final String ERS_VPLAN_PATH = VPLAN_PATH + "ers/";
    public static final String TIMESTAMP_PATH = VPLAN_PATH + "timestamp";
    public static String GENERIC_MSG_PATH;

    public static final String TIMESTAMP = "/timestamp";

    public static String getDevicePath(Context context) {
        int schoolIndex = PreferenceManager.getDefaultSharedPreferences(context).getInt(Settings.MY_SCHOOL_PREF, 0);
        switch (schoolIndex) {
            case Schools.ZS:
                return ZS_VPLAN_PATH;
            case Schools.ERS:
                return ERS_VPLAN_PATH;
            default:
                return ZS_VPLAN_PATH;
        }
    }

    public static void initGenericPath(Context context) {
        int schoolIndex = PreferenceManager.getDefaultSharedPreferences(context).getInt(Settings.MY_SCHOOL_PREF, 0);
        switch (schoolIndex) {
            case Schools.ZS:
                GENERIC_MSG_PATH = ZS_VPLAN_PATH + "generic/";
                break;
            case Schools.ERS:
                GENERIC_MSG_PATH = ERS_VPLAN_PATH + "generic/";
                break;
            default:
                GENERIC_MSG_PATH = ZS_VPLAN_PATH + "generic/";
        }
    }
}
