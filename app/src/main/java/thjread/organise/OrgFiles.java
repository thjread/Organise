package thjread.organise;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrgFiles {
    ArrayList<Org> files;
    private Map<String, Org> fileMap;

    OrgFiles () {
        files = new ArrayList<>();
        fileMap = new HashMap<>();
    }


    public void addDocument(Org document) {
        if (fileMap.containsKey(document.title)) {
            int index = files.indexOf(fileMap.get(document.title));
            files.set(index, document);
            fileMap.remove(document.title);
            fileMap.put(document.title, document);
        } else {
            files.add(document);
            fileMap.put(document.title, document);
        }
    }

    public void loadFiles(Context context) throws IOException {
        File doc_dir = context.getDir("doc_dir", Context.MODE_PRIVATE);
        File[] files = doc_dir.listFiles();
        for (int i=0; i<files.length; ++i) {
            Org document = new Org(new OrgFile(files[i]));
            addDocument(document);
            fileMap.put(document.title, document);
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

                SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");//TODO is this right?

                for (int i=0; i<meta.contents.size(); ++i) {
                    String path = meta.contents.get(i).path;
                    String filename = meta.contents.get(i).fileName();
                    if (filename.substring(filename.length()-4, filename.length()).equals(".bak")) {
                        continue;
                    }

                    File doc_dir = context.getDir("doc_dir", Context.MODE_PRIVATE);
                    File file = new File(doc_dir, filename);

                    boolean upload = false;

                    if (fileMap.containsKey(filename)) {
                        try {
                            String modified_string = meta.contents.get(i).modified;
                            Date dropbox_modified = format.parse(modified_string);

                            Org doc = fileMap.get(filename);
                            Date local_modified = doc.file.lastWrite;
                            if (local_modified != null && local_modified.after(dropbox_modified)) {
                                upload = true;
                                Log.d("thjread.organise", "Online modified: " +
                                        dropbox_modified.toString() +
                                        " local modified: " +
                                        local_modified.toString());
                                Log.d("thjread.organise", "uploading");
                            }
                        } catch (ParseException e) {
                            // just download then
                        }
                    }

                    if (upload) {
                        FileInputStream inputStream = new FileInputStream(file);
                        try {
                            mDBApi.delete(path + ".bak");
                        } catch (DropboxException e) {
                            //no backups
                        }
                        mDBApi.move(path, path+".bak");
                        mDBApi.putFile(path, inputStream, file.length(), null, null);
                        inputStream.close();
                    } else {
                        FileOutputStream outputStream = new FileOutputStream(file);
                        mDBApi.getFile(path, null, outputStream, null);
                        outputStream.close();
                        addDocument(new Org(new OrgFile(filename, context)));
                    }
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

