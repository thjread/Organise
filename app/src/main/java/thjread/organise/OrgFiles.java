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
    ArrayList<Org> files;

    OrgFiles () {
        files = new ArrayList<>();
    }

    public void addDocument(Org document) {
        for (int i=0; i<files.size(); ++i) {
            if (files.get(i).title.equals(document.title)) {
                files.set(i, document);
                return;
            }
        }

        files.add(document);
    }

    public void loadFiles(Context context) throws IOException {
        addDocument(new Org(new OrgFile("Todo.org", context)));
    }

    public void syncFiles(Context context, DropboxAPI<AndroidAuthSession> mDBApi, SyncFilesCallback callback) {
        syncFile(context, mDBApi, callback, "Todo.org");
        syncFile(context, mDBApi, callback, "Test.org");
    }

    public void syncFile(Context context, DropboxAPI<AndroidAuthSession> mDBApi, SyncFilesCallback callback,
                         String filePath) {
        try {
            File file = new File(context.getFilesDir() + filePath);
            FileOutputStream outputStream = new FileOutputStream(file);

            DropboxDownloadTask task = new DropboxDownloadTask(mDBApi, outputStream, filePath, context,
                    callback, this);
            task.execute();

        } catch (FileNotFoundException e) {//TODO deal with properly
            Log.d("thjread.organise", e.getMessage());
        }
    }

    class DropboxDownloadTask extends AsyncTask<Void, Void, Void> {
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

        protected Void doInBackground(Void... params) {
            try {
                DropboxAPI.Entry meta = mDBApi.metadata("/org", 100, null, true, null);
                for (int i=0; i<meta.contents.size(); ++i) {
                    String path = meta.contents.get(i).path;
                    String filename = meta.contents.get(i).fileName();

                    File file = new File(context.getFilesDir() + filename);
                    FileOutputStream outputStream = new FileOutputStream(file);

                    mDBApi.getFile(path, null, outputStream, null);
                    addDocument(new Org(new OrgFile(filename, context)));
                }
            } catch (DropboxException e) {//TODO
                Log.d("thjread.organise", e.toString());
            } catch (IOException e) {

            }

            callback.syncFilesCallback(orgfiles);
            return null;
        }
    }

    public List<Org> getFiles() {
        return files;
    }
}

interface SyncFilesCallback {
    void syncFilesCallback(OrgFiles files);
}

