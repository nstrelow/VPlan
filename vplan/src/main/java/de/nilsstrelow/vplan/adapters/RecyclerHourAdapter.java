package de.nilsstrelow.vplan.adapters;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;

import de.nilsstrelow.vplan.R;
import de.nilsstrelow.vplan.activities.AddReminderActivity;
import de.nilsstrelow.vplan.activities.VertretungsplanActivity;
import de.nilsstrelow.vplan.helpers.Entry;
import de.nilsstrelow.vplan.helpers.SchoolDay;
import de.nilsstrelow.vplan.utils.UIUtils;

/**
 * RecyclerView.Adapter implementation, not ready yet, waiting for better RecyclerView
 * using old HourAdapter instead
 * Created by Nils on 01.07.2014.
 */
public class RecyclerHourAdapter extends RecyclerView.Adapter<RecyclerHourAdapter.ViewHolder> {

    //private static LayoutInflater inflater = null;
    private ActionBarActivity activity;
    private SchoolDay schoolDay;


    public RecyclerHourAdapter(ActionBarActivity activity, SchoolDay schoolDay) {
        this.activity = activity;
        this.schoolDay = schoolDay;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View rowView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hour_row, parent, false);

        ViewHolder vh = new ViewHolder(rowView);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerHourAdapter.ViewHolder viewHolder, int position) {
        
        final Entry entry = schoolDay.getEntry(position);


        if (entry != null) {
            if (entry.isNormalEntry()) {
                // make ConvertView Clickable and give color
                viewHolder.itemView.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.rectangle_generic));
                // special colors for different remarks
                if (entry.bemerkung.contains("Entfall") || entry.bemerkung.contains("eigenv.Arb.") || entry.bemerkung.contains("entfällt")) {
                    viewHolder.itemView.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.rectangle_entfall));
                }
                if (entry.bemerkung.contains("andr. Raum") || entry.bemerkung.contains("Raumaenderung")) {
                    viewHolder.itemView.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.rectangle_andraum));
                }
                if (entry.bemerkung.contains("Vertretung")) {
                    viewHolder.itemView.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.rectangle_vertretung));
                }
                if (entry.bemerkung.contains("Betreuung")) {
                    viewHolder.itemView.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.rectangle_betreuung));
                }
                if (entry.bemerkung.contains("Verlegung")) {
                    viewHolder.itemView.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.rectangle_verlegung));
                }
                if (entry.bemerkung.contains("Klausur")) {
                    viewHolder.itemView.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.rectangle_klausur));
                }

                final LinearLayout some = (LinearLayout) viewHolder.itemView;

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {

                    final Handler handler = new Handler();

                    @Override
                    public void onClick(View v) {

                        if (!entry.bemerkung.equals("")) {

                            final TextView txt = (TextView) some.findViewById(R.id.bemerkung);
                            txt.setText(entry.bemerkung);
                            txt.setTypeface(VertretungsplanActivity.robotoBold);

                            if (txt.getVisibility() == TextView.GONE) {
                                expand(txt, txt.getText().toString(), some.getWidth());
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        collapse(txt);
                                    }
                                }, 6000);
                            } else {
                                collapse(txt);
                                handler.removeCallbacks(null);
                            }
                        }
                    }
                });

                viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {

                    View view;

                    @Override
                    public boolean onLongClick(View v) {

                        view = v;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                            view.setAlpha(0.7f);
                        activity.startSupportActionMode(new ActionMode.Callback() {
                            @Override
                            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                                switch (item.getItemId()) {

                                    case R.id.action_add_reminder:
                                        activity.getSupportFragmentManager();
                                        Intent reminderIntent = new Intent(activity, AddReminderActivity.class);
                                        reminderIntent.putExtra(AddReminderActivity.ENTRY_KEY, entry);
                                        activity.startActivity(reminderIntent);
                                        mode.finish();
                                        break;


                                }
                                return true;
                            }

                            @Override
                            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                                mode.getMenuInflater().inflate(R.menu.contextual_menu, menu);
                                return true;
                            }

                            @Override
                            public void onDestroyActionMode(ActionMode mode) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                                    view.setAlpha(1);
                            }

                            @Override
                            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                                mode.setTitle("Eintragsauswahl");
                                return false;
                            }
                        });
                        return true;
                    }
                });
            }
        }

        viewHolder.txtStunde.setText(entry.stunde);
        viewHolder.txtStunde.setTypeface(VertretungsplanActivity.robotoBold);
        viewHolder.vertreter.setText(entry.vertreter);
        viewHolder.vertreter.setTypeface(VertretungsplanActivity.robotoBold);
        viewHolder.fach.setText(entry.fach);
        viewHolder.fach.setTypeface(VertretungsplanActivity.robotoBold);
        viewHolder.raum.setText(entry.raum);
        viewHolder.raum.setTypeface(VertretungsplanActivity.robotoBold);
        viewHolder.stattLehrer.setText(entry.stattLehrer);
        viewHolder.stattLehrer.setTypeface(VertretungsplanActivity.robotoBold);
        viewHolder.stattFach.setText(entry.stattFach);
        viewHolder.stattFach.setTypeface(VertretungsplanActivity.robotoBold);
        viewHolder.stattRaum.setText(entry.stattRaum);
        viewHolder.stattRaum.setTypeface(VertretungsplanActivity.robotoBold);
    }

    @Override
    public int getItemCount() {
        return schoolDay.getSize();
    }

    private void expand(View view, String text, int width) {
        //set Visible
        view.setVisibility(View.VISIBLE);

        ValueAnimator mAnimator = slideAnimator(0, UIUtils.getHeight(activity, text, 14, width, VertretungsplanActivity.robotoBold, activity.getResources().getDimensionPixelSize(R.dimen.view_padding)), view);
        mAnimator.start();
    }

    private void collapse(View view) {
        int finalHeight = view.getHeight();

        ValueAnimator mAnimator = slideAnimator(finalHeight, 0, view);

        final View v = view;

        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                //Height=0, but it set visibility to GONE
                v.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mAnimator.start();
    }

    private ValueAnimator slideAnimator(int start, int end, View view) {

        ValueAnimator animator = ValueAnimator.ofInt(start, end);

        final View v = view;

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //Update Height
                int value = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
                layoutParams.height = value;
                v.setLayoutParams(layoutParams);
            }
        });
        return animator;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtStunde;
        TextView vertreter;
        TextView fach;
        TextView raum;
        TextView stattLehrer;
        TextView stattFach;
        TextView stattRaum;

        public ViewHolder(View rowView) {
            super(rowView);
            txtStunde = (TextView) rowView.findViewById(R.id.std);
            vertreter = (TextView) rowView.findViewById(R.id.vertreter);
            fach = (TextView) rowView.findViewById(R.id.fach);
            raum = (TextView) rowView.findViewById(R.id.raum);
            stattLehrer = (TextView) rowView.findViewById(R.id.statt_lehrer);
            stattFach = (TextView) rowView.findViewById(R.id.statt_fach);
            stattRaum = (TextView) rowView.findViewById(R.id.statt_raum);
        }
    }
}
