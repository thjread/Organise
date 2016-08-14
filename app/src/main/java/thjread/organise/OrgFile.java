package thjread.organise;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class OrgFile {
    File f;
    ArrayList<String> file;
    String title;
    Date lastWrite = null;

    File write_file;

    public boolean deleted = false;

    public OrgFile(String filePath, Context context) throws IOException {
        file = new ArrayList<>();

        File doc_dir = context.getDir("doc_dir", Context.MODE_PRIVATE);
        File write_dir = context.getDir("write_dir", Context.MODE_PRIVATE);
        f = new File(doc_dir, filePath);
        write_file = new File(write_dir, filePath);
        if (!f.exists()) {
            f.getParentFile().mkdirs();
            f.createNewFile();
        }
        BufferedReader reader = new BufferedReader(new FileReader(f));
        String str = "";
        while ((str = reader.readLine()) != null) {
            file.add(str);
        }
        reader.close();

        this.title = filePath;

        if (write_file.exists()) {
            lastWrite = new Date(f.lastModified());
        }
    }

    public OrgFile(File f, Context context) throws IOException {
        this.f = f;
        file = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new FileReader(f));
        String str;
        while ((str = reader.readLine()) != null) {
            file.add(str);
        }
        reader.close();

        this.title = f.getName();

        File write_dir = context.getDir("write_dir", Context.MODE_PRIVATE);
        write_file = new File(write_dir, title);
        if (write_file.exists()) {
            lastWrite = new Date(f.lastModified());
        }
    }

    public void write(Org org) {
        OrgWriteTask task = new OrgWriteTask(org);
        task.execute();
    }

    public void delete(Org org) {
        OrgDeleteTask task = new OrgDeleteTask(org);
        task.execute();
        this.deleted = true;
    }

    class OrgWriteTask extends AsyncTask<Void, Void, Void> {
        Org org;

        OrgWriteTask(Org org) {
            this.org = org;
        }

        protected Void doInBackground(Void... params) {
            GlobalState.getWriteLock();
            try {
                String s = org.serialise();
                String path = f.getAbsolutePath();
                f.delete();
                FileOutputStream fo = new FileOutputStream(path, true);
                fo.write(s.getBytes());
                fo.close();
                lastWrite = new Date();
                write_file.getParentFile().mkdirs();
                write_file.createNewFile();
            } catch (Exception e) {
                Log.e("thjread.organise", e.toString());
            }
            GlobalState.returnWriteLock();
            return null;
        }
    }

    class OrgDeleteTask extends AsyncTask<Void, Void, Void> {
        Org org;

        OrgDeleteTask(Org org) {
            this.org = org;
        }

        protected Void doInBackground(Void... params) {
            GlobalState.getWriteLock();
            try {
                f.delete();
                lastWrite = new Date();
                write_file.getParentFile().mkdirs();
                write_file.createNewFile();
            } catch (Exception e) {
                Log.e("thjread.organise", e.toString());
            }
            GlobalState.returnWriteLock();
            return null;
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
