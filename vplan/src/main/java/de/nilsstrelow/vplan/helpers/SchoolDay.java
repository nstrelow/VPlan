package de.nilsstrelow.vplan.helpers;

/**
 * SchoolDay data structure
 * Created by djnilse on 10/31/13.
 */

import java.util.ArrayList;
import java.util.List;

public class SchoolDay {

    public final List<String> schoolClasses = new ArrayList<String>();
    public final List<String> schoolClassData = new ArrayList<String>();
    public String string;
    public String schoolGenericMessage = "";

    public SchoolDay(String string) {
        this.string = string;
    }

}