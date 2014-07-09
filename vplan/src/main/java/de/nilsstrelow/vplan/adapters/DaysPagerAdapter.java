package de.nilsstrelow.vplan.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import de.nilsstrelow.vplan.activities.VertretungsplanActivity;
import de.nilsstrelow.vplan.fragments.ClassDayViewFragment;
import de.nilsstrelow.vplan.helpers.SchoolClass;

/**
 * PagerAdapter to show substitution plan of one class
 * Created by djnilse on 08.04.2014.
 */
public class DaysPagerAdapter extends FragmentPagerAdapter {

    private SchoolClass schoolClass;

    public DaysPagerAdapter(FragmentManager fm, SchoolClass schoolClass) {
        super(fm);
        this.schoolClass = schoolClass;
    }

    @Override
    public Fragment getItem(int dayIndex) {
        return ClassDayViewFragment.newInstance(schoolClass.getDay(dayIndex));
    }

    @Override
    public int getCount() {
        return VertretungsplanActivity.NUM_PAGES;
    }
}
