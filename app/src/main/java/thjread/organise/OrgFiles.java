package thjread.organise;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OrgFiles {
    ArrayList<OrgFile> files;

    public void loadFiles(Context context) throws IOException {
        files = new ArrayList<OrgFile>();
        files.add(new OrgFile("Todo.org", context));
    }

    public void syncFiles(Context context, DropboxAPI<AndroidAuthSession> mDBApi, SyncFilesCallback callback) {
        try {
            File file = new File(context.getFilesDir() + "Todo.org");
            FileOutputStream outputStream = new FileOutputStream(file);

            DropboxDownloadTask task = new DropboxDownloadTask(mDBApi, outputStream, "/Todo.org", context,
                    callback, this);
            task.execute();

            Log.d("thjread.organise", "helloooo");
        } catch (FileNotFoundException e) {//TODO deal with properly
            Log.d("thjread.organise", e.getMessage());
        } catch (IOException e) {

        }

    }

    class DropboxDownloadTask extends AsyncTask<Void, Void, DropboxAPI.DropboxFileInfo> {
        private DropboxAPI<AndroidAuthSession> mDBApi;
        private FileOutputStream fileOutputStream;
        private String filePath;
        private Context context;
        private SyncFilesCallback callback;
        private OrgFiles orgfiles;

        DropboxDownloadTask(DropboxAPI<AndroidAuthSession> mDBApi, FileOutputStream fileOutputStream,
                            String filePath, Context context, SyncFilesCallback callback,
                            OrgFiles orgfiles) {
            this.mDBApi = mDBApi;
            this.fileOutputStream = fileOutputStream;
            this.filePath = filePath;
            this.context = context;
            this.callback = callback;
            this.orgfiles = orgfiles;
        }

        protected DropboxAPI.DropboxFileInfo doInBackground(Void... params) {
            try {
                DropboxAPI.DropboxFileInfo info = mDBApi.getFile(filePath, null, fileOutputStream, null);
                files = new ArrayList<OrgFile>();
                try {
                    files.add(new OrgFile("Todo.org", context));
                } catch (IOException e) {

                }
                callback.syncFilesCallback(orgfiles);
                return info;
            } catch (DropboxException e) {
                Log.d("thjread.organise", e.toString());
            }
            return null;
        }
    };

    public List<OrgFile> getFiles() {
        return files;
    }
}

interface SyncFilesCallback {
    void syncFilesCallback(OrgFiles files);
}
