package de.nilsstrelow.vplan.helpers;

/**
 * SchoolDay data structure
 * Created by djnilse on 10/31/13.
 */

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.nilsstrelow.vplan.constants.Device;
import de.nilsstrelow.vplan.utils.DateUtils;
import de.nilsstrelow.vplan.utils.FileUtils;

public class SchoolDay implements Parcelable {

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public SchoolDay createFromParcel(Parcel in) {
            return new SchoolDay(in);
        }

        public SchoolDay[] newArray(int size) {
            return new SchoolDay[size];
        }
    };
    public Date day;
    public String dayName;
    private String genericMsg;
    private List<Entry> entries = new ArrayList<Entry>();

    public SchoolDay() {
    }

    public SchoolDay(String dayName) {
        this.dayName = dayName;
        this.day = DateUtils.parseString(dayName);
        genericMsg = loadGenericMsg();
    }

    // Parcelling part
    public SchoolDay(Parcel in) {
        String[] data = new String[2];
        in.readStringArray(data);
        this.dayName = data[0];
        this.genericMsg = data[1];
        List<Entry> entries = new ArrayList<Entry>();
        in.readList(entries, null);
        this.entries = entries;
        this.day = (Date) in.readSerializable();
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

    private String loadGenericMsg() {
        String s = FileUtils.readFile(Device.GENERIC_MSG_PATH + dayName + "_generic.txt");
        return (s.length() > 0) ? s.substring(0, s.length() - 1) : "";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{this.dayName, this.genericMsg});
        dest.writeList(this.entries);
        dest.writeSerializable(day);
    }
}