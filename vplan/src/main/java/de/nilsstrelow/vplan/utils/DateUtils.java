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

    public static Date parseDate(String target) {
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);
        Date result = null;
        try {
            result = df.parse(target.substring(3));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }
}
