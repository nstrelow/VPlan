package de.nilsstrelow.vplan.utils;

import android.util.SparseArray;

import de.nilsstrelow.vplan.helpers.SchoolDay;

/**
 * Utils to mess with SchoolClasses
 * Created by djnilse on 08.04.2014.
 */
public class SchoolClassUtils {

    /**
     * searches for the specific class and returns index
     * or returns -1 if not found
     *
     * @param day         Schoolday containing classes
     * @param schoolClass String of the class searching for
     * @return index of schoolClass or -1, if not found
     */
    public static int getClassIndex(SchoolDay day, String schoolClass) {
        try {
            //int size = day.schoolClasses.size();
            for (int i = 0; i < 2/*size*/; i++) {
                //if (day.schoolClasses.get(i).equals(schoolClass))
                return i;
            }
        } catch (NullPointerException e) {
            return -1;
        }
        return -1;
    }

    public static int getClassIndex(String[] schoolClasses, String schoolClassName) {
        int length = schoolClasses.length;
        for (int i = 0; i < length; i++) {
            if (schoolClasses[i].equals(schoolClassName))
                return i;
        }
        return -1;
    }

    /**
     * @param schoolDay   Schoolday containing classes
     * @param schoolClass index of schoolClass
     * @return name of schoolClass
     */
    public static String getClassName(SparseArray<SchoolDay> schoolDays, int schoolDay, int schoolClass) {
        return "";//schoolDays.get(schoolDay).schoolClasses.get(schoolClass);
    }
}
