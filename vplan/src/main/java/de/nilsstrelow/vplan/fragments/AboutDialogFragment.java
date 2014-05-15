package de.nilsstrelow.vplan.fragments;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.nilsstrelow.vplan.R;

/**
 * DialogFragment for feedback
 * Created by djnilse on 02.03.14.
 */
public class AboutDialogFragment extends DialogFragment {

    public static final String EMAIL = "funceptionapps@gmail.com";
    private static final String VERSION_UNAVAILABLE = "N/A";

    public static AboutDialogFragment newInstance() {
        return new AboutDialogFragment();
    }

    /*
    * Copyright (C) 2013-2014 AChep@xda <artemchep@gmail.com>
    */
    static CharSequence getVersionTitle(Context context) {
        PackageManager pm = context.getPackageManager();
        String packageName = context.getPackageName();
        String versionName;
        try {
            PackageInfo info = pm.getPackageInfo(packageName, 0);
            versionName = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = VERSION_UNAVAILABLE;
        }

        Resources res = context.getResources();
        return Html.fromHtml(
                res.getString(R.string.about_title,
                        res.getString(R.string.app_name), versionName)
        );
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle(getVersionTitle(getActivity()));

        View v = inflater.inflate(R.layout.about_dialog, container, false);

        TextView aboutTxt = (TextView) v.findViewById(R.id.about_message);
        aboutTxt.setText(Html.fromHtml(getString(R.string.about_message)));
        aboutTxt.setMovementMethod(LinkMovementMethod.getInstance());
        return v;
    }

}
