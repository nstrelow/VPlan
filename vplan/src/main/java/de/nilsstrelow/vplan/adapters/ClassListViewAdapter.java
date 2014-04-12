package de.nilsstrelow.vplan.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import de.nilsstrelow.vplan.R;
import de.nilsstrelow.vplan.activities.VertretungsplanActivity;


/**
 * ListViewAdapter for school classes
 * Created by djnilse on 10/31/13.
 */
public class ClassListViewAdapter extends ArrayAdapter<String> {

    private final Activity activity;
    private final String[] schoolClasses;

    public ClassListViewAdapter(Activity activity, String[] schoolClasses) {
        super(activity, R.layout.listrow_class, schoolClasses);
        this.activity = activity;
        this.schoolClasses = schoolClasses;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final String className = schoolClasses[position];

        View rowView = convertView;

        if (rowView == null) {
            ViewHolder viewHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.listrow_class, parent, false);
            viewHolder.entry = (TextView) rowView.findViewById(R.id.child_item);
            rowView.setTag(viewHolder);
        }

        ViewHolder holder = (ViewHolder) rowView.getTag();

        holder.entry.setTypeface(VertretungsplanActivity.robotoBlack);
        if (className.startsWith("5") || className.startsWith("6") || className.startsWith("7")) {
            //txtListChild.setBackground(activity.getResources().getDrawable(R.drawable.rectangle_orange));
            holder.entry.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.rectangle_orange));
        }
        if (className.startsWith("8") || className.startsWith("9")) {
            //txtListChild.setBackground(activity.getResources().getDrawable(R.drawable.rectangle_orange));
            holder.entry.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.rectangle_green));
        }
        if (className.startsWith("1")) {
            //txtListChild.setBackground(activity.getResources().getDrawable(R.drawable.rectangle_orange));
            holder.entry.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.rectangle_purple));
        }
        holder.entry.setText(className);
        return rowView;
    }

    static class ViewHolder {
        TextView entry;
    }
}