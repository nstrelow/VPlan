package de.nilsstrelow.vplan.helpers;

/**
 * SchoolDay data structure
 * Created by djnilse on 10/31/13.
 */

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.nilsstrelow.vplan.constants.Device;
import de.nilsstrelow.vplan.utils.FileUtils;

public class SchoolDay {

    public Date day;
    public String dayName;
    private List<Entry> entries = new ArrayList<Entry>();
    private String genericMsg;

    public SchoolDay() {
    }

    public SchoolDay(Date day, String dayName) {
        this.day = day;
        this.dayName = dayName;
        genericMsg = loadGenericMsg();
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

    private String loadGenericMsg() {
        String s = FileUtils.readFile(Device.GENERIC_MSG_PATH + dayName + "_generic.txt");
        return (s.length() > 0) ? s.substring(0, s.length() - 1) : "";
    }
}