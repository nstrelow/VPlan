package de.nilsstrelow.vplan.helpers;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by djnilse on 12.04.2014.
 */
public class Plan {
    /*
     * First element is 5a, last Element is 13
     */
    public List<SchoolWeek> schoolClasses = new ArrayList<SchoolWeek>();

    public SchoolWeek getClass(int i) {
        return schoolClasses.get(i);
    }
}
