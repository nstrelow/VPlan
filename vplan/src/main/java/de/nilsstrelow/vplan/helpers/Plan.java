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
    public List<SchoolClass> schoolClasses = new ArrayList<SchoolClass>();

    public SchoolClass getClass(int i) {
        return schoolClasses.get(i);
    }

    public void addClass(SchoolClass schoolClass) {
        schoolClasses.add(schoolClass);
    }

}
