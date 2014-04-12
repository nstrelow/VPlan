package de.nilsstrelow.vplan.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;

import de.nilsstrelow.vplan.R;
import de.nilsstrelow.vplan.activities.VertretungsplanActivity;

/**
 * BaseAdapter for hour entries in ListView
 * Created by djnilse on 11/16/13.
 */
public class HourAdapter extends BaseAdapter {

    private static LayoutInflater inflater = null;
    Activity activity;
    String[] data;

    public HourAdapter(Activity activity, String[] data) {
        this.activity = activity;
        this.data = data;
        inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public static int getHeight(Context context, CharSequence text, int textSize, int deviceWidth, Typeface typeface, int padding) {
        TextView textView = new TextView(context);
        textView.setPadding(padding, padding, padding, padding);
        textView.setTypeface(typeface);
        textView.setText(text, TextView.BufferType.SPANNABLE);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(deviceWidth, View.MeasureSpec.AT_MOST);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        textView.measure(widthMeasureSpec, heightMeasureSpec);
        return textView.getMeasuredHeight();
    }

    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public Object getItem(int position) {
        return data[position];
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
        final String[] row = data[position].split("-_-");
        if (row != null) {
            // make ConvertView Clickable and give color
            rowView.setBackgroundDrawable(parent.getResources().getDrawable(R.drawable.rectangle_generic));
            final String bemerkung = (row.length > 7) ? row[7] : "";
            // special colors for different remarks
            if (bemerkung.contains("Entfall") || bemerkung.contains("eigenv.Arb.")) {
                rowView.setBackgroundDrawable(parent.getResources().getDrawable(R.drawable.rectangle_entfall));
            }
            if (bemerkung.contains("andr. Raum")) {
                rowView.setBackgroundDrawable(parent.getResources().getDrawable(R.drawable.rectangle_andraum));
            }
            if (bemerkung.contains("Vertretung")) {
                rowView.setBackgroundDrawable(parent.getResources().getDrawable(R.drawable.rectangle_vertretung));
            }
            if (bemerkung.contains("Betreuung")) {
                rowView.setBackgroundDrawable(parent.getResources().getDrawable(R.drawable.rectangle_betreuung));
            }
            if (bemerkung.contains("Verlegung")) {
                rowView.setBackgroundDrawable(parent.getResources().getDrawable(R.drawable.rectangle_verlegung));
            }
            if (bemerkung.contains("Klausur")) {
                rowView.setBackgroundDrawable(parent.getResources().getDrawable(R.drawable.rectangle_klausur));
            }

            final LinearLayout some = (LinearLayout) rowView;

            rowView.setOnClickListener(new View.OnClickListener() {

                final Handler handler = new Handler();

                @Override
                public void onClick(View v) {
                    if (!bemerkung.equals("")) {

                        final TextView txt = (TextView) some.findViewById(R.id.bemerkung);
                        txt.setText(bemerkung);
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

            if (row.length > 6) {
                holder.txtStunde.setText(row[0]);
                holder.txtStunde.setTypeface(VertretungsplanActivity.robotoBold);
                holder.vertreter.setText(row[1]);
                holder.vertreter.setTypeface(VertretungsplanActivity.robotoBold);
                holder.fach.setText(row[2]);
                holder.fach.setTypeface(VertretungsplanActivity.robotoBold);
                holder.raum.setText(row[3]);
                holder.raum.setTypeface(VertretungsplanActivity.robotoBold);
                holder.stattLehrer.setText(row[4]);
                holder.stattLehrer.setTypeface(VertretungsplanActivity.robotoBold);
                holder.stattFach.setText(row[5]);
                holder.stattFach.setTypeface(VertretungsplanActivity.robotoBold);
                holder.stattRaum.setText(row[6]);
                holder.stattRaum.setTypeface(VertretungsplanActivity.robotoBold);
            } else {
                //LinearLayout root = new LinearLayout(activity);
                TextView special = new TextView(activity);
                special.setText(row[0]);
                special.setTypeface(VertretungsplanActivity.robotoBold);
                special.setHeight((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, activity.getResources().getDisplayMetrics()));
                special.setGravity(Gravity.CENTER);
                //root.addView(special);
                special.setBackgroundDrawable(parent.getResources().getDrawable(R.drawable.rectangle_special));
                rowView = special;
            }
        }

        return rowView;
    }

    private void expand(View view, String text, int width) {
        //set Visible
        view.setVisibility(View.VISIBLE);

        ValueAnimator mAnimator = slideAnimator(0, getHeight(activity, text, 14, width, VertretungsplanActivity.robotoBold, activity.getResources().getDimensionPixelSize(R.dimen.standard_padding)), view);
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