package de.nilsstrelow.vplan.helpers;

/**
 * SchoolDay data structure
 * Created by djnilse on 10/31/13.
 */

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SchoolDay {

    public Date day;
    private List<Entry> entries = new ArrayList<Entry>();
    private String genericMsg;

    public SchoolDay() {
    }

    public SchoolDay(Date date) {
        day = date;
    }

    public Entry getEntry(int i) {
        return entries.get(i);
    }

    public void addEntry(Entry entry) {
        entries.add(entry);
    }

    public int getSize() {
        return entries.size();
    }

    public String getGenericMessage() {
        return genericMsg;
    }

    public void setGenericMessage(String genericMsg) {
        this.genericMsg = genericMsg;
    }
}