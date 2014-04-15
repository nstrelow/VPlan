package de.nilsstrelow.vplan.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import de.nilsstrelow.vplan.activities.VertretungsplanActivity;
import de.nilsstrelow.vplan.fragments.ClassDayViewFragment;
import de.nilsstrelow.vplan.helpers.SchoolClass;
import de.nilsstrelow.vplan.helpers.SchoolDay;

/**
 * Created by djnilse on 08.04.2014.
 */
public class DaysPagerAdapter extends FragmentStatePagerAdapter {

    SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

    private SchoolClass schoolClass;

    public DaysPagerAdapter(FragmentManager fm, SchoolClass schoolClass) {
        super(fm);
        this.schoolClass = schoolClass;
    }

    @Override
    public Fragment getItem(int dayIndex) {
        SchoolDay schoolDay = schoolClass.getDay(dayIndex);
        return new ClassDayViewFragment(schoolClass.getDay(dayIndex));
    }

    @Override
    public int getCount() {
        return VertretungsplanActivity.NUM_PAGES;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }
}
