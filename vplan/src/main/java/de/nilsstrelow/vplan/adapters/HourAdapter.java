package de.nilsstrelow.vplan.adapters;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;

import de.nilsstrelow.vplan.R;
import de.nilsstrelow.vplan.activities.VertretungsplanActivity;
import de.nilsstrelow.vplan.helpers.Entry;
import de.nilsstrelow.vplan.helpers.SchoolDay;
import de.nilsstrelow.vplan.utils.UIUtils;

/**
 * BaseAdapter for hour entries in ListView
 * Created by djnilse on 11/16/13.
 */
public class HourAdapter extends BaseAdapter {

    private static LayoutInflater inflater = null;
    Activity activity;
    SchoolDay schoolDay;

    public HourAdapter(Activity activity, SchoolDay schoolDay) {
        this.activity = activity;
        this.schoolDay = schoolDay;
        inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return schoolDay.getSize();
    }

    @Override
    public Object getItem(int position) {
        return schoolDay.getEntry(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        // reuse views
        if (rowView == null || rowView instanceof TextView) {
            rowView = inflater.inflate(R.layout.hour_row, parent, false);

            EntryHolder viewHolder = new EntryHolder();
            viewHolder.txtStunde = (TextView) rowView.findViewById(R.id.std);
            viewHolder.vertreter = (TextView) rowView.findViewById(R.id.vertreter);
            viewHolder.fach = (TextView) rowView.findViewById(R.id.fach);
            viewHolder.raum = (TextView) rowView.findViewById(R.id.raum);
            viewHolder.stattLehrer = (TextView) rowView.findViewById(R.id.statt_lehrer);
            viewHolder.stattFach = (TextView) rowView.findViewById(R.id.statt_fach);
            viewHolder.stattRaum = (TextView) rowView.findViewById(R.id.statt_raum);
            rowView.setTag(viewHolder);
        }

        EntryHolder holder = (EntryHolder) rowView.getTag();

        final Entry entry = schoolDay.getEntry(position);

        if (entry != null) {
            if (entry.isNormalEntry()) {
                // make ConvertView Clickable and give color
                rowView.setBackgroundDrawable(parent.getResources().getDrawable(R.drawable.rectangle_generic));
                //final String bemerkung = (row.length > 7) ? row[7] : "";
                // special colors for different remarks
                if (entry.bemerkung.contains("Entfall") || entry.bemerkung.contains("eigenv.Arb.")) {
                    rowView.setBackgroundDrawable(parent.getResources().getDrawable(R.drawable.rectangle_entfall));
                }
                if (entry.bemerkung.contains("andr. Raum")) {
                    rowView.setBackgroundDrawable(parent.getResources().getDrawable(R.drawable.rectangle_andraum));
                }
                if (entry.bemerkung.contains("Vertretung")) {
                    rowView.setBackgroundDrawable(parent.getResources().getDrawable(R.drawable.rectangle_vertretung));
                }
                if (entry.bemerkung.contains("Betreuung")) {
                    rowView.setBackgroundDrawable(parent.getResources().getDrawable(R.drawable.rectangle_betreuung));
                }
                if (entry.bemerkung.contains("Verlegung")) {
                    rowView.setBackgroundDrawable(parent.getResources().getDrawable(R.drawable.rectangle_verlegung));
                }
                if (entry.bemerkung.contains("Klausur")) {
                    rowView.setBackgroundDrawable(parent.getResources().getDrawable(R.drawable.rectangle_klausur));
                }

                final LinearLayout some = (LinearLayout) rowView;

                rowView.setOnClickListener(new View.OnClickListener() {

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

                holder.txtStunde.setText(entry.stunde);
                holder.txtStunde.setTypeface(VertretungsplanActivity.robotoBold);
                holder.vertreter.setText(entry.vertreter);
                holder.vertreter.setTypeface(VertretungsplanActivity.robotoBold);
                holder.fach.setText(entry.fach);
                holder.fach.setTypeface(VertretungsplanActivity.robotoBold);
                holder.raum.setText(entry.raum);
                holder.raum.setTypeface(VertretungsplanActivity.robotoBold);
                holder.stattLehrer.setText(entry.stattLehrer);
                holder.stattLehrer.setTypeface(VertretungsplanActivity.robotoBold);
                holder.stattFach.setText(entry.stattFach);
                holder.stattFach.setTypeface(VertretungsplanActivity.robotoBold);
                holder.stattRaum.setText(entry.stattRaum);
                holder.stattRaum.setTypeface(VertretungsplanActivity.robotoBold);
            } else {
                TextView special = new TextView(activity);
                special.setText(entry.line);
                special.setTypeface(VertretungsplanActivity.robotoBold);
                special.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT));
                int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, activity.getResources().getDisplayMetrics());
                special.setPadding(padding, padding, padding, padding);
                special.setGravity(Gravity.CENTER);
                special.setBackgroundDrawable(parent.getResources().getDrawable(R.drawable.rectangle_special));
                rowView = special;
            }
        }

        return rowView;
    }

    private void expand(View view, String text, int width) {
        //set Visible
        view.setVisibility(View.VISIBLE);

        ValueAnimator mAnimator = slideAnimator(0, UIUtils.getHeight(activity, text, 14, width, VertretungsplanActivity.robotoBold, activity.getResources().getDimensionPixelSize(R.dimen.standard_padding)), view);
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

    static class EntryHolder {
        TextView txtStunde;
        TextView vertreter;
        TextView fach;
        TextView raum;
        TextView stattLehrer;
        TextView stattFach;
        TextView stattRaum;
    }
}