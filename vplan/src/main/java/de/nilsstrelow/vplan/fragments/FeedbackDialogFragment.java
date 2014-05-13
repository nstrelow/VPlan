package de.nilsstrelow.vplan.fragments;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import de.nilsstrelow.vplan.R;

/**
 * DialogFragment for feedback
 * Created by djnilse on 02.03.14.
 */
public class FeedbackDialogFragment extends DialogFragment {

    public static final String EMAIL = "funceptionapps@gmail.com";

    public static FeedbackDialogFragment newInstance() {
        return new FeedbackDialogFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle(getResources().getString(R.string.feedback_dialog_title));

        View v = inflater.inflate(R.layout.feedback_dialog, container, false);

        final EditText feedbackMsgEd = (EditText) v.findViewById(R.id.feedback_msg);

        Button sendFeedback = (Button) v.findViewById(R.id.bt_send_feedback);
        Button abortFeedback = (Button) v.findViewById(R.id.bt_abort_feedback);

        abortFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        sendFeedback.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                CharSequence message = feedbackMsgEd.getText();

                // Check for message's length
                int messageMinLength = 15;
                if (message == null || message.length() < messageMinLength) {
                    Toast.makeText(getActivity(), getString(
                            R.string.feedback_error_msg_too_short,
                            messageMinLength), Toast.LENGTH_SHORT)
                            .show();
                    return; // Don't dismiss dialog
                }

                PackageInfo pi;
                try {
                    //noinspection ConstantConditions
                    pi = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    return;
                }

                CharSequence body = createBodyMessage(pi, message);

                Intent intent = new Intent(Intent.ACTION_SEND)
                        .setType("message/rfc822")
                        .putExtra(Intent.EXTRA_EMAIL, new String[]{EMAIL})
                        .putExtra(Intent.EXTRA_SUBJECT, "Feedback zu V Plan")
                        .putExtra(Intent.EXTRA_TEXT, body);

                try {
                    startActivity(Intent.createChooser(intent,
                            getString(R.string.feedback_send_via)));

                    // Dismiss current dialog once everything is ok
                    getDialog().dismiss();
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getActivity(), getString(R.string.feedback_error_no_app),
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        return v;
    }

    /*
    * Copyright (C) 2013-2014 AChep@xda <artemchep@gmail.com>
    */
    private CharSequence createBodyMessage(PackageInfo pi, CharSequence msg) {
        return "" + msg +
                '\n' +
                '\n' +
                "- - - - - - - - - - - - -" + '\n' +
                "app_version:" + pi.versionName + '(' + pi.versionCode + ")\n" +
                "android_version:" + Build.VERSION.RELEASE + '(' + Build.VERSION.SDK_INT + ")\n" +
                "build_display:" + Build.DISPLAY + '\n' +
                "build_brand:" + Build.BRAND + '\n' +
                "build_model:" + Build.MODEL;
    }

}
