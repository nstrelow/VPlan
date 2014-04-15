package de.nilsstrelow.vplan.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Utils to read and save files
 * Created by djnilse on 10.04.2014.
 */
public class FileUtils {

    /**
     * @param path path to file
     * @return string content of file
     */
    public static String readFile(String path) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));

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

    public static void saveFile(String content, String path) {
        File myFile = new File(path);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(myFile);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            outputStreamWriter.write(content);
            outputStreamWriter.close();
            fileOutputStream.close();
        } catch (Exception ignored) {
        }
    }

}
