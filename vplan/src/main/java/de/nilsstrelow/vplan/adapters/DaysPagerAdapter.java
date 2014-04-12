package de.nilsstrelow.vplan.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import de.nilsstrelow.vplan.activities.VertretungsplanActivity;
import de.nilsstrelow.vplan.fragments.ClassDayViewFragment;
import de.nilsstrelow.vplan.utils.SchoolClassUtils;

/**
* Created by djnilse on 08.04.2014.
*/
public class DaysPagerAdapter extends FragmentStatePagerAdapter {

    SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

    public DaysPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        int classIndex = SchoolClassUtils.getClassIndex(VertretungsplanActivity.schoolDays.get(position), VertretungsplanActivity.getCurrentSchoolClassName());
        return selectFragment(position, classIndex);
    }

    /**
     * Swaps fragments in the main content view
     */
    private Fragment selectFragment(int group, int child) {

        // global variable needed to set right day
        VertretungsplanActivity.currentSchoolDay = group;

        if (child == -1) {
            Fragment fragment = new ClassDayViewFragment();
            Bundle args = new Bundle();
            String[] novalue = {"Keine Änderungen an diesem Tag"};
            args.putStringArray("CLASSDATA", novalue);
            args.putString("GENERICMSG", VertretungsplanActivity.schoolDays.get(VertretungsplanActivity.currentSchoolDay).schoolGenericMessage);
            fragment.setArguments(args);
            return fragment;
        }

        // global variable needed to set right class
        VertretungsplanActivity.currentSchoolClassName = SchoolClassUtils.getClassName(VertretungsplanActivity.schoolDays, group, child);


        Fragment fragment = new ClassDayViewFragment();
        Bundle args = new Bundle();
        String value;
        try {
            final int classIndex = SchoolClassUtils.getClassIndex(VertretungsplanActivity.schoolDays.get(VertretungsplanActivity.currentSchoolDay), VertretungsplanActivity.currentSchoolClassName);
            value = VertretungsplanActivity.schoolDays.get(VertretungsplanActivity.currentSchoolDay).schoolClassData.get(classIndex); // classIndex greater, but schoolClassData wrong
            args.putStringArray("CLASSDATA", value.split("__"));
        } catch (Exception e) {
            String values[] = {"Es gab Probleme mit dem Einlesen dieses Plans.", "Die Anzeige konnte nicht generiert werden"};
            args.putStringArray("CLASSDATA", values);
        }
        args.putString("GENERICMSG", VertretungsplanActivity.schoolDays.get(VertretungsplanActivity.currentSchoolDay).schoolGenericMessage);
        fragment.setArguments(args);

        return fragment;
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
