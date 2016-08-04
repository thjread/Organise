package thjread.organise;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;

public class OrgFile {
    File f;
    ArrayList<String> file;
    String title;
    Date lastWrite = null;

    public OrgFile(String filePath, Context context) throws IOException {
        file = new ArrayList<>();

        File doc_dir = context.getDir("doc_dir", Context.MODE_PRIVATE);
        f = new File(doc_dir, filePath);
        BufferedReader reader = new BufferedReader(new FileReader(f));
        String str = "";
        while ((str = reader.readLine()) != null) {
            file.add(str);
        }
        reader.close();

        this.title = filePath;
    }

    public OrgFile(File f) throws IOException {
        this.f = f;
        file = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new FileReader(f));
        String str = "";
        while ((str = reader.readLine()) != null) {
            file.add(str);
        }
        reader.close();

        this.title = f.getName();
    }

    public void write(Org org) {
        String s = org.serialise();
        try {
            String path = f.getAbsolutePath();
            f.delete();
            FileOutputStream fo = new FileOutputStream(path, true);
            fo.write(s.getBytes());
            fo.close();
            lastWrite = new Date();
        } catch (IOException e) {
            Log.d("thjread.organise", e.toString());
        }
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
