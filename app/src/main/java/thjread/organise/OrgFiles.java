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
        File doc_dir = context.getDir("doc_dir", Context.MODE_PRIVATE);
        File[] files = doc_dir.listFiles();
        for (int i=0; i<files.length; ++i) {
            addDocument(new Org(new OrgFile(files[i])));
        }
    }

    public void syncFiles(Context context, DropboxAPI<AndroidAuthSession> mDBApi, SyncFilesCallback callback) {
        DropboxDownloadTask task = new DropboxDownloadTask(mDBApi, context,
                callback, this);
        task.execute();
    }

    class DropboxDownloadTask extends AsyncTask<Void, Void, Void> {
        private DropboxAPI<AndroidAuthSession> mDBApi;
        private Context context;
        private SyncFilesCallback callback;
        private OrgFiles orgfiles;

        DropboxDownloadTask(DropboxAPI<AndroidAuthSession> mDBApi,
                            Context context,
                            SyncFilesCallback callback,
                            OrgFiles orgfiles) {
            this.mDBApi = mDBApi;
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

                    File doc_dir = context.getDir("doc_dir", Context.MODE_PRIVATE);
                    File file = new File(doc_dir, filename);
                    FileOutputStream outputStream = new FileOutputStream(file);

                    mDBApi.getFile(path, null, outputStream, null);
                    outputStream.close();
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

