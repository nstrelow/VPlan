package de.nilsstrelow.vplan.activities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

import de.nilsstrelow.vplan.R;
import de.nilsstrelow.vplan.constants.Hours;
import de.nilsstrelow.vplan.helpers.Entry;
import de.nilsstrelow.vplan.services.ReminderService;
import de.nilsstrelow.vplan.utils.DateUtils;

public class AddReminderActivity extends ActionBarActivity {

    public static final String TAG = AddReminderActivity.class.getSimpleName();
    public static final String ENTRY_KEY = "entry_key";
    private boolean DEBUG = false;
    private Entry selectedEntry;
    private Calendar timeToNotify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_reminder_activity);

        try {

            selectedEntry = (Entry) getIntent().getSerializableExtra(ENTRY_KEY);
            timeToNotify = Calendar.getInstance();
            timeToNotify.setTime(selectedEntry.day);

            if (DEBUG)
                Log.e(TAG, timeToNotify.toString());

            final TimePicker timePicker = (TimePicker) findViewById(R.id.timePicker);
            timePicker.setIs24HourView(true);

            final DatePicker datePicker = (DatePicker) findViewById(R.id.datePicker);

        /* Add predefined time */
            String stunde = selectedEntry.stunde;
            final String title = "Erinnerung für " + stunde + ". Stunde";
            //getDialog().setTitle(title);
            int hour = Integer.valueOf((stunde.matches("[1][0-2].*") ? stunde.substring(0, 2) : stunde.substring(0, 1)));
            String[] times = Hours.getHour(hour).split(":");
            timePicker.setCurrentHour(Integer.valueOf(times[0]));
            timePicker.setCurrentMinute(Integer.valueOf(times[1]));

            datePicker.init(timeToNotify.get(Calendar.YEAR), timeToNotify.get(Calendar.MONTH), timeToNotify.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
                @Override
                public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                }
            });

            View actionBarButtons = getLayoutInflater().inflate(R.layout.done_cancel_bar,
                    new LinearLayout(this), false);
            View cancelActionView = actionBarButtons.findViewById(R.id.action_cancel);
            cancelActionView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            View doneActionView = actionBarButtons.findViewById(R.id.action_done);
            doneActionView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int year = datePicker.getYear();
                    int month = datePicker.getMonth();
                    int day = datePicker.getDayOfMonth();
                    int hour = timePicker.getCurrentHour();
                    int min = timePicker.getCurrentMinute();

                    timeToNotify.set(year, month, day, hour, min);


                    Log.e(TAG, timeToNotify.getTime().toString());

                    if (DateUtils.isFuture(timeToNotify.getTime())) {
                        addReminder();
                        Toast.makeText(AddReminderActivity.this, title + " erstellt", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(AddReminderActivity.this, "Hmmm... Woher hast du die Zeitmaschine ?", Toast.LENGTH_LONG).show();
                    }
                }
            });

            final ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(actionBarButtons);

            TextView txtVertreter = (TextView) findViewById(R.id.rVertreter);
            txtVertreter.setText(selectedEntry.vertreter);
            TextView txtFach = (TextView) findViewById(R.id.rFach);
            txtFach.setText(selectedEntry.fach);
            TextView txtRaum = (TextView) findViewById(R.id.rRaum);
            txtRaum.setText(selectedEntry.raum);
            TextView txtStattLehrer = (TextView) findViewById(R.id.rStattLehrer);
            txtStattLehrer.setText(selectedEntry.stattLehrer);
            TextView txtStattFach = (TextView) findViewById(R.id.rStattFach);
            txtStattFach.setText(selectedEntry.stattFach);
            TextView txtStattRaum = (TextView) findViewById(R.id.rStattRaum);
            txtStattRaum.setText(selectedEntry.stattRaum);
            TextView txtBemerkung = (TextView) findViewById(R.id.rBemerkung);
            txtBemerkung.setText(selectedEntry.bemerkung);
        } catch (Exception e) {
            Toast.makeText(this, "Dies funktioniert nicht auf deinem Gerät. Sende mir ein Feedback!", Toast.LENGTH_LONG).show();
            finish();
        }

    }

    private void addReminder() {

        Intent i = new Intent(this, ReminderService.class);
        i.putExtra(ReminderService.SELECTED_ENTRY_KEY, selectedEntry);
        PendingIntent pendingIntent = PendingIntent.getService(this,
                5546, i, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);

        Log.e(TAG, timeToNotify.getTime().toString());

        if (DEBUG)
            am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 2000, pendingIntent);
        else
            am.set(AlarmManager.RTC_WAKEUP, timeToNotify.getTime().getTime(), pendingIntent);

    }
}
