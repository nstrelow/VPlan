package de.nilsstrelow.vplan.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A class to parse and work with Dates
 * Created by djnilse on 08.04.2014.
 */
public class DateUtils {

    public static Date parseString(String target) {
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);
        Date result = null;
        try {
            result = df.parse(target.substring(3));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String parseSchoolDay(String schoolDayName) {
        // final String --> then switch to Mo --> Montag
        String dayWord = schoolDayName.substring(0, 2);
        String day = schoolDayName.substring(2, 4);
        String month = schoolDayName.substring(4, 6);
        String year = schoolDayName.substring(6, 8);
        return dayWord + " " + day + "." + month + "." + "20" + year;
    }

    public static String parseDate(Date date) {
        if (date != null)
            return new SimpleDateFormat("EEdd.MM.yy", Locale.GERMANY).format(date);
        else
            return "Keine Pläne";
    }
}
