package de.nilsstrelow.vplan.utils;

import android.annotation.SuppressLint;
import android.app.Activity;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import de.nilsstrelow.vplan.R;

/**
 * Utils for Croutons
 * Created by djnilse on 13.04.2014.
 */
@SuppressLint("ResourceAsColor")
public class CroutonUtils {

    public static final int CROUTON_CONFIRM = 0;
    public static final int CROUTON_INFO = 1;
    public static final int CROUTON_ALERT = 2;

    static Configuration config
            = new Configuration.Builder()
            .setDuration(2000).build();

    static Style mCONFIRM = new Style.Builder()
            .setBackgroundColor(R.color.holo_green_crouton)
            .setConfiguration(config).build();

    static Style mAlert = new Style.Builder()
            .setBackgroundColor(R.color.holo_red_crouton)
            .setConfiguration(config).build();

    static Style mInfo = new Style.Builder()
            .setBackgroundColor(R.color.holo_blue_crouton)
            .setConfiguration(config).build();

    public static void makeCrouton(Activity activity, String message, int color) {
        switch (color) {
            case CROUTON_CONFIRM:
                Crouton.makeText(activity, message, mCONFIRM).show();
                break;
            case CROUTON_INFO:
                Crouton.makeText(activity, message, mInfo).show();
                break;
            case CROUTON_ALERT:
                Crouton.makeText(activity, message, mAlert).show();
                break;
        }

    }
}
