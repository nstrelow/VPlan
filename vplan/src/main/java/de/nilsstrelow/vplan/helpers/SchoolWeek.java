package de.nilsstrelow.vplan.helpers;

/**
 * SchoolDay data structure
 * Created by djnilse on 10/31/13.
 */

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SchoolWeek {

    public List<SchoolDay2> schoolDays = new ArrayList<SchoolDay2>();

    public SchoolDay2 getDay(int i) {
        return schoolDays.get(i);
    }

}