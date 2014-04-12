package de.nilsstrelow.vplan.helpers;

/**
 * SchoolDay data structure
 * Created by djnilse on 10/31/13.
 */

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SchoolDay2 {

    private Date day;
    public List<Entry> entries = new ArrayList<Entry>();

    public SchoolDay2(Date date) {
        day = date;
    }

    public Entry getEntry(int i) {
        return entries.get(i);
    }
}