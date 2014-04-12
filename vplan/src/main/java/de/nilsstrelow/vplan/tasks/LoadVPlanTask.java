package de.nilsstrelow.vplan.tasks;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Message;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.nilsstrelow.vplan.R;
import de.nilsstrelow.vplan.activities.VertretungsplanActivity;
import de.nilsstrelow.vplan.constants.HandlerMsg;
import de.nilsstrelow.vplan.helpers.ErrorMessage;
import de.nilsstrelow.vplan.helpers.SchoolDay;
import de.nilsstrelow.vplan.utils.DateUtils;
import de.nilsstrelow.vplan.utils.SchoolClassUtils;

/**
 * Created by djnilse on 08.04.2014.
 */
public class LoadVPlanTask extends AsyncTask<File, Integer, Boolean> {

    // just one return value, so define it local to LoadVPlanTask
    List<String> classes;
    private Activity activity;

    public LoadVPlanTask(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected Boolean doInBackground(File... files) {
        prepareSchoolPlanView(files);
        return true;
    }

    @Override
    protected void onPreExecute() {
        Message msg = new Message();
        msg.obj = activity.getResources().getString(R.string.load_plan_msg);
        msg.what = HandlerMsg.LOADING;
        VertretungsplanActivity.handler.sendMessage(msg);
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        VertretungsplanActivity.handler.sendEmptyMessage(HandlerMsg.FINISHED_LOADING);
        VertretungsplanActivity.handler.sendEmptyMessage(HandlerMsg.LOAD_STARTPAGE_MSG);
    }

    public void prepareSchoolPlanView(File[] files) {
        int length = files.length;
        VertretungsplanActivity.dates = new Date[length];
        for (int i = 0; i < length; i++) {
            final String date = SchoolClassUtils.parseSchoolDay(files[i].getName());
            VertretungsplanActivity.dates[i] = DateUtils.parseDate(date);
            SchoolDay day = new SchoolDay(date);
            day.schoolClassData.addAll(parseSchoolClassData(files[i].getAbsolutePath()));
            day.schoolClasses.addAll(classes);
            day.schoolGenericMessage = loadGenericMessage(files[i].getAbsolutePath());
            VertretungsplanActivity.schoolDays.append(i, day);
        }
        sortDays(VertretungsplanActivity.dates);
    }

    // gotta make this into one loop
    public String loadGenericMessage(String pathToSchoolDayFile) {
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(pathToSchoolDayFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader r = new BufferedReader(fileReader);
        String genericMessage = "";
        String line;
        try {
            while ((line = r.readLine()) != null) {
                if (!line.equals("")) {
                    if (line.contains("statt") && line.contains("verlegt")) {
                        if (genericMessage.length() != 0) {
                            if (genericMessage.charAt(genericMessage.length() - 1) == '\n') {
                                fileReader.close();
                                return genericMessage.substring(0, genericMessage.length() - 1);
                            }
                        }
                        fileReader.close();
                        return genericMessage;
                    }
                    if (!(line.contains("Vertretungsplan") || line.contains("Josephskirchstr.9") || line.contains("D-60433"))) {
                        genericMessage += line + "\n";
                    }
                }
            }
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // selection sort
    /*public void sortDaysOld(Date[] dates) {
        int lenD = dates.length;
        int j;
        Date tmp;
        SchoolDay sTmp;
        for (int i = 0; i < lenD; i++) {
            j = i;
            for (int k = i; k < lenD; k++) {
                if (dates[j] != null && dates[k] != null) { // fix strange NullPointer Bug
                    if (dates[j].compareTo(dates[k]) > 0) {
                        j = k;
                    }
                }
            }
            sTmp = schoolDays.get(i);
            tmp = dates[i];
            schoolDays.setValueAt(i, schoolDays.get(j));
            dates[i] = dates[j];
            schoolDays.setValueAt(j, sTmp);
            dates[j] = tmp;
        }
    }*/

    // bubble sort
    public void sortDays(Date[] dates) {
        int lenD = dates.length;
        Date tmp;
        SchoolDay sTmp;
        for (int i = 0; i < lenD; i++) {
            for (int j = (lenD - 1); j >= (i + 1); j--) {
                if (dates[j] != null && dates[j - 1] != null) { // fix strange NullPointer Bug
                    if (dates[j].compareTo(dates[j - 1]) < 0) {
                        tmp = dates[j];
                        sTmp = VertretungsplanActivity.schoolDays.get(j);
                        dates[j] = dates[j - 1];
                        VertretungsplanActivity.schoolDays.setValueAt(j, VertretungsplanActivity.schoolDays.get(j - 1));
                        dates[j - 1] = tmp;
                        VertretungsplanActivity.schoolDays.setValueAt(j - 1, sTmp);
                    }
                }
            }
        }
    }

    public List<String> parseSchoolClassData(String pathToSchoolDayFile) {
        List<String> classDataList = new ArrayList<String>();
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(pathToSchoolDayFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            BufferedReader r = new BufferedReader(fileReader);
            classes = new ArrayList<String>();

            String line;
            String classData = "";

            // single data
            String stunde;
            String vertreter = "-_- ";
            String fach = "-_- ";
            String raum = "-_- ";
            String stattLehrer = "-_- ";
            String stattFach = "-_- ";
            String stattRaum = "-_- ";
            String bemerkung;
            int currentClass = 0;
            try {
                while ((line = r.readLine()) != null) {
                    //Log.e(getApplicationInfo().name, line);
                    //com.google.analytics.tracking.android.Log.e(line);

                    if (line.length() == 3 || line.length() == 2) {
                        currentClass++;
                        classes.add(line.substring(1));
                        if (!classData.equals("")) {
                            classDataList.add(classData);
                            classData = "";
                        }
                    } else {
                        if (currentClass != 0 && !line.contains(" -- ")) {
                            String data[] = line.replaceFirst("\\s", "").split("\\s+", -1); // prevent leading emptry string in array
                            int i = 0;
                            if (data[i].matches("1?[0-9](\\s?-\\s?1?[0-9])?")) {   // then the line is an entry, parses numbers from 0-19
                                stunde = data[i] + "-_-";
                                if (data[i + 1].equals("-") && data[i + 2].matches("1?[0-9]")) {
                                    stunde = data[i] + data[i + 1] + data[i + 2] + "-_-";
                                    i += 2;
                                }
                                i++; // i = 1
                                // only proceed with parsing if we are sure it is an actual entry
                                if (data[i].matches("[A-Z][a-zA-Zäöü]{0,3}|---|\\?{3}")) {   // then data[i] is a teacher, parses 4 letters
                                    vertreter = data[i] + "-_-";
                                    i++; // i = 2

                                    // only parse this if vertreter != +
                                    if (data[i].matches("[A-Z][\\w]?[a-zA-Z]{0,2}|Sp3|Sp4|---|\\?{3}") && !data[i].equals("NT") && !data[i].equals("TE")) {   // then the line is a class, parses 4 letters
                                        fach = data[i] + "-_-";
                                        i++; // i = 3
                                    }
                                } else if (data[i].equals("+")) { // in diesem Fall "meistens" kein Fach, Vertreter ist +
                                    vertreter = "+-_-";
                                    i++; // i = 3
                                    fach = " -_-";
                                    if (data[i + 1].matches("[0-4K][0-9][0-9]|Aula|NT|TE|---|-|\\?{3}")) {        // if next item is a room, then parse date[i+1] as fach
                                        fach = data[i] + "-_-";
                                        i++;
                                    }

                                }
                                if (data[i].matches("[0-4K][0-9][0-9]|Aula|NT|TE|---|-|\\?{3}")) {   // then the line is a room, parses 3 digits
                                    raum = data[i] + "-_-";
                                    i++; // i = 4
                                }
                                if (data[i].matches("[A-Z][a-zA-zäöü]{0,3}")) {   // then data[i] is a teacher, parses 4 letters
                                    stattLehrer = data[i] + "-_-";
                                    i++; // i = 5
                                }
                                if (data[i].matches("[A-Z][\\w]?[a-zA-Z]{0,2}|Sp3|Sp4") && !data[i].equals("NT") && !data[i].equals("TE")) {   // then the line is a class, parses 4 letters
                                    stattFach = data[i] + "-_-";
                                    i++; // i = 6
                                }
                                if (data[i].matches("[0-4K][0-9][0-9]|Aula|NT|TE")) {   // then the line is a room, parses 3 digits
                                    stattRaum = data[i] + "-_-";
                                    i++; // i = 7
                                }

                                //for (int n = i; n < data.length; n++) {
                                //    bemerkung += data[n] + " ";
                                //}
                                bemerkung = line.substring(line.indexOf(data[i]));
                                bemerkung = bemerkung.replaceAll("\\s+", " ");

                                classData += stunde + vertreter + fach + raum + stattLehrer + stattFach + stattRaum + bemerkung + "__";
                                vertreter = fach = raum = stattLehrer = stattFach = stattRaum = "-_- ";

                            } else {
                                if (line.contains("Std. SV-Stunde") || line.contains("Std. SV-Std.") || line.contains("SV-Stunde")) {
                                    classData += line + "__";
                                }
                                if (line.length() > 50) {
                                    if (!classData.equals("") && line.substring(0, 50).contains("                                                ")) {
                                        String[] words = line.split("\\s+", -1);
                                        String bemerkung2 = "";
                                        for (String word : words) {
                                            bemerkung2 += word + " ";
                                        }
                                        classData = classData.substring(0, classData.length() - 3) + bemerkung2 + "__";
                                    } else if (!line.contains("D-60433") && !line.contains("Josephskirchstr.") && !line.contains("     statt") && !line.contains("Fch.") && !line.contains("Vertretungsplan ")) { // exclude header
                                        // if is nothing OR it doesn't contains a bunch of spaces, just add everything to a line
                                        classData += line + "__";
                                    }

                                }

                            }
                        }
                    }
                }
                if (!classData.equals("")) {
                    classDataList.add(classData);
                }

                fileReader.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return classDataList;
        } catch (Exception e) {
            com.google.analytics.tracking.android.Log.e(e);
            File errorPlan = new File(pathToSchoolDayFile);
            String s = errorPlan.getName().substring(0, errorPlan.getName().length() - 4);
            if (VertretungsplanActivity.sharedPref.getBoolean(s, true)) {
                Message msg = new Message();
                msg.what = HandlerMsg.ERROR;
                msg.obj = new ErrorMessage(true, "Einlesefehler", "Der Plan " + s + " konnte nicht richtig eingelesen werden. Die Korrektheit der für diesen Tag angezeigten Einträge ist nicht gewährleistet, da es beim Einlesen der Pläne zu Verschiebungen unter den Klassen kommen kann. Ich bitte um dein Verständnis.");
                VertretungsplanActivity.handler.sendMessage(msg);

                // Save Error Log on Plan, to prevent showing Dialog again
                SharedPreferences.Editor editor = VertretungsplanActivity.sharedPref.edit();
                editor.putBoolean(s, false);
                editor.commit();
            }
        }
        try {
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classDataList;
    }
}
