package thjread.organise;

import android.app.Application;

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

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
