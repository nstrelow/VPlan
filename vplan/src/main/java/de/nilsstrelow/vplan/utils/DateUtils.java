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

    public static Date parseString(String schoolDayName) {

        DateFormat df = new SimpleDateFormat("EEddMMyy", Locale.GERMAN);
        Date result = null;
        try {
            result = df.parse(schoolDayName.substring(0, 8));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String parseDate(Date date) {
        if (date != null)
            return new SimpleDateFormat("EEdd.MM.yy", Locale.GERMANY).format(date);
        else
            return "Keine Pläne";
    }
}
