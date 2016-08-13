package thjread.organise;

import android.app.Application;

import java.util.Arrays;
import java.util.List;

public class GlobalState extends Application {
    private static GlobalState instance = null;
    private static OrgFiles files = null;
    private static Org currentOrg = null;

    public static GlobalState getInstance() {
        checkInstance();
        return instance;
    }

    public static OrgFiles getFiles() {
        if (files == null) {
            files = new OrgFiles();
        }
        return files;
    }

    public static Org getCurrentOrg() {
        return currentOrg;
    }

    public static void setCurrentOrg(Org org) {
        currentOrg = org;
    }

    private static void checkInstance() {
        if (instance == null)
            throw new IllegalStateException("Application not created yet!");
    }

    private static boolean writeLock = false;

    public static void getWriteLock() {
        while (writeLock == true) {}
        writeLock = true;
    }

    public static void returnWriteLock() {
        writeLock = false;
    }

    private static List<String> listFromString(String string) {
        return Arrays.asList(string.split("(\\s*)?,(\\s*)?"));
    }

    public static List<String> getLocations() {
        String location_string = PrefUtils.readPref(getInstance(), "quick_add_locations", "Todo.org/Tasks, Homework.org");
        return listFromString(location_string);
    }

    public static String getDropboxPath() {
        String path = PrefUtils.readPref(getInstance(), "dropbox_path", "/org");
        if (path.charAt(path.length()-1) == '/') {
            return path.substring(0, path.length()-1);
        } else {
            return path;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
