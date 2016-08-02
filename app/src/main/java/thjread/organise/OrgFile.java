package thjread.organise;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class OrgFile {
    ArrayList<String> file;
    String title;

    public OrgFile(int id, String title, Context context) throws IOException {
        file = new ArrayList<String>();

        InputStream is = context.getResources().openRawResource(id);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        if (is != null) {
            String str = "";
            while ((str = reader.readLine()) != null) {
                file.add(str);
            }
        }
        is.close();

        this.title = title;
    }

    public OrgFile(String filePath, Context context) throws IOException {
        file = new ArrayList<String>();

        File f = new File(context.getFilesDir() + filePath);
        BufferedReader reader = new BufferedReader(new FileReader(f));
        String str = "";
        while ((str = reader.readLine()) != null) {
            file.add(str);
        }
        reader.close();

        this.title = filePath;
    }

    public boolean isEmpty() {
        return file.isEmpty();
    }

    public String peekLine() {
        return file.get(0);
    }

    public String removeLine() {
        return file.remove(0);
    }
}
