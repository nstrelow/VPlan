package de.nilsstrelow.vplan.helpers;

/**
 * SchoolDay data structure
 * Created by djnilse on 10/31/13.
 */

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SchoolClass {

    private List<SchoolDay> schoolDays = new ArrayList<SchoolDay>();

    public SchoolDay getDay(int i) {
        if(schoolDays.size() > i)
            return schoolDays.get(i);
        else
            return new SchoolDay();
    }

    public void addDay(SchoolDay schoolDay) {
        schoolDays.add(schoolDay);
    }

    public int getSize() {
        return schoolDays.size();
    }

    public void sortDays() {
        int lenD = schoolDays.size();
        int j;
        SchoolDay sTmp;
        for (int i = 0; i < lenD; i++) {
            j = i;
            for (int k = i; k < lenD; k++) {
                if (schoolDays.get(j) != null && schoolDays.get(k) != null) { // fix strange NullPointer Bug
                    if (schoolDays.get(j).day.compareTo(schoolDays.get(k).day) > 0) {
                        j = k;
                    }
                }
            }
            sTmp = schoolDays.get(i);
            schoolDays.set(i, schoolDays.get(j));
            schoolDays.set(j, sTmp);
        }
    }
}