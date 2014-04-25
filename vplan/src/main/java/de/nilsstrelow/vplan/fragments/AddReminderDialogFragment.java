package de.nilsstrelow.vplan.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;

import de.nilsstrelow.vplan.R;
import de.nilsstrelow.vplan.constants.Hours;
import de.nilsstrelow.vplan.helpers.Entry;

/**
 * DialogFragment for feedback
 * Created by djnilse on 02.03.14.
 */
public class AddReminderDialogFragment extends DialogFragment {

    public static final String ENTRY_KEY = "entry_key";

    private Entry selectedEntry;

    public static AddReminderDialogFragment newInstance(Entry entry) {

        AddReminderDialogFragment addReminderDialogFragment = new AddReminderDialogFragment();

        final Bundle args = new Bundle(1);
        args.putSerializable(ENTRY_KEY, entry);
        addReminderDialogFragment.setArguments(args);

        return addReminderDialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedEntry = (Entry) getArguments().getSerializable(ENTRY_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.add_reminder_dialog, container, false);

        TimePicker timePicker = (TimePicker) v.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);

        /* Add predefined time */
        String stunde = selectedEntry.stunde;
        getDialog().setTitle("Erinnerung für " + stunde + ". Stunde");
        int hour = Integer.valueOf((stunde.matches("[1][0-2].*") ? stunde.substring(0,2) : stunde.substring(0,1)));
        String[] times = Hours.getHour(hour).split(":");
        timePicker.setCurrentHour(Integer.valueOf(times[0]));
        timePicker.setCurrentMinute(Integer.valueOf(times[1]));

        View cancelActionView = v.findViewById(R.id.action_cancel);
        cancelActionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
        View doneActionView = v.findViewById(R.id.action_done);
        doneActionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addReminder();
            }
        });

        //TextView txtHour = (TextView) v.findViewById(R.id.rHour);
        //txtHour.setText(selectedEntry.stunde + ". Stunde");
        TextView txtVertreter = (TextView) v.findViewById(R.id.rVertreter);
        txtVertreter.setText(selectedEntry.vertreter);
        TextView txtFach = (TextView) v.findViewById(R.id.rFach);
        txtFach.setText(selectedEntry.fach);
        TextView txtRaum = (TextView) v.findViewById(R.id.rRaum);
        txtRaum.setText(selectedEntry.raum);
        TextView txtStattLehrer = (TextView) v.findViewById(R.id.rStattLehrer);
        txtStattLehrer.setText(selectedEntry.stattLehrer);
        TextView txtStattFach = (TextView) v.findViewById(R.id.rStattFach);
        txtStattFach.setText(selectedEntry.stattFach);
        TextView txtStattRaum = (TextView) v.findViewById(R.id.rStattRaum);
        txtStattRaum.setText(selectedEntry.stattRaum);
        TextView txtBemerkung = (TextView) v.findViewById(R.id.rBemerkung);
        txtBemerkung.setText(selectedEntry.bemerkung);

        return v;
    }

    private void addReminder() {

    }

}
