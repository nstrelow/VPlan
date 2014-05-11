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
public class SchoolListViewAdapter extends ArrayAdapter<String> {

    private final Activity activity;
    private final String[] schools;

    public SchoolListViewAdapter(Activity activity, String[] schools) {
        super(activity, R.layout.listrow_class_drawer, schools);
        this.activity = activity;
        this.schools = schools;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final String school = schools[position];

        View rowView = convertView;

        if (rowView == null) {
            ViewHolder viewHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.listrow_class_drawer, parent, false);
            viewHolder.entry = (TextView) rowView.findViewById(R.id.child_item);
            rowView.setTag(viewHolder);
        }

        ViewHolder holder = (ViewHolder) rowView.getTag();

        holder.entry.setTypeface(VertretungsplanActivity.robotoBold);
        holder.entry.setText(school);
        return rowView;
    }

    static class ViewHolder {
        TextView entry;
    }
}