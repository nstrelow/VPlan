package de.nilsstrelow.vplan.constants;

/**
 * Created by djnilse on 16.04.2014.
 */
public class Hours {
    public static final String HOUR_1 = "7:45";
    public static final String HOUR_2 = "8:35";
    public static final String HOUR_3 = "9:35";
    public static final String HOUR_4 = "10:25";
    public static final String HOUR_5 = "11:30";
    public static final String HOUR_6 = "12:20";
    public static final String HOUR_7 = "13:15";
    public static final String HOUR_8 = "14:05";
    public static final String HOUR_9 = "14:55";
    public static final String HOUR_10 = "15:40";
    public static final String HOUR_11 = "16:30";
    public static final String HOUR_12 = "17:15";

    public static String getHour(int hour) {
        switch (hour) {
            case 1:
                return HOUR_1;
            case 2:
                return HOUR_2;
            case 3:
                return HOUR_3;
            case 4:
                return HOUR_4;
            case 5:
                return HOUR_5;
            case 6:
                return HOUR_6;
            case 7:
                return HOUR_7;
            case 8:
                return HOUR_8;
            case 9:
                return HOUR_9;
            case 10:
                return HOUR_10;
            case 11:
                return HOUR_11;
            case 12:
                return HOUR_12;
        }
        return HOUR_1;
    }
}
