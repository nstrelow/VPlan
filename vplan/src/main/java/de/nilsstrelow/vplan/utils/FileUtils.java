package de.nilsstrelow.vplan.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by djnilse on 10.04.2014.
 */
public class FileUtils {

    /**
     * @param path path to file
     * @return string content of file
     */
    public static String readFile(String path) {
        try {
            BufferedReader br = null;
            br = new BufferedReader(new FileReader(path));

            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append('\n');
                line = br.readLine();
            }
            br.close();

            return sb.toString();
        } catch (FileNotFoundException e) {
            Log.e("readFile()", "FileNotFound");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

}
