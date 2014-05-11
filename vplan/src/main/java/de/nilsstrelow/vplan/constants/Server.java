package de.nilsstrelow.vplan.constants;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import de.nilsstrelow.vplan.activities.Settings;

/**
 * constant server specific Urls, paths and folders
 * Created by djnilse on 09.04.2014.
 */
public class Server {
    public static final String ZS_WEBSITE_URL = "http://www.ziehenschule-online.de/vplan/";
    public static final String ERS_WEBSITE_URL = "http://www.ers1.de/untis/subst_001.htm";
    public static final String TIMESTAMP = "/timestamp";
    private static final String SERVER_URL = "http://nils.sontg.net/";
    public static final String VPLAN_URL = SERVER_URL + "vplan/";
    public static final String ZS_PLAN_URL = VPLAN_URL + "zs/";
    public static final String ERS_PLAN_URL = VPLAN_URL + "ers/";

    public static String getPlanUrl(Context context) {
        int schoolIndex = PreferenceManager.getDefaultSharedPreferences(context).getInt(Settings.MY_SCHOOL_PREF, 0);
        switch (schoolIndex) {
            case Schools.ZS:
                return ZS_PLAN_URL;
            case Schools.ERS:
                return ERS_PLAN_URL;
            default:
                return ZS_PLAN_URL;
        }
    }
}
