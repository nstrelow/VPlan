package de.nilsstrelow.vplan.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * A class to parse and work with Dates
 * Created by djnilse on 08.04.2014.
 */
public class DateUtils {

    public static Date parseString(String schoolDayName) {

        DateFormat df = new SimpleDateFormat("ddMMyy", Locale.US);
        Date result = null;
        try {
            result = df.parse(schoolDayName.substring(2, 8));
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

    public static int getYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }

    public static int getMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MONTH);
    }

    public static int getDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    public static boolean isFuture(Date date) {
        final Calendar now = Calendar.getInstance();
        return now.getTime().before(date);
    }
}
