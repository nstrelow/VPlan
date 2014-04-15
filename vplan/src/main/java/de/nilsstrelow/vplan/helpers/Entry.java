package de.nilsstrelow.vplan.helpers;

/**
 * Created by djnilse on 12.04.2014.
 */
public class Entry {
    public String stunde;
    public String vertreter;
    public String fach;
    public String raum;
    public String stattLehrer;
    public String stattFach;
    public String stattRaum;
    public String bemerkung;

    public String line;

    private boolean isNormalEntry;

    public Entry(String[] data) {
        line = "";
        if(data.length == 8) {
            isNormalEntry = true;
            stunde = data[0];
            vertreter = data[1];
            fach = data[2];
            raum = data[3];
            stattLehrer = data[4];
            stattFach = data[5];
            stattRaum = data[6];
            bemerkung = data[7];
        } else {
            isNormalEntry = false;
            for(int i = 0; i < data.length; i++) {
                line = line + data[0];
            }
        }
    }

    public boolean isNormalEntry() {
        return isNormalEntry;
    }
}
