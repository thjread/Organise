package thjread.organise;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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

    public OrgItem getItem(List<String> path) {
        Org doc = this.getDocument(path.get(0));
        if (path.size() > 1 && doc != null) {
            return doc.getItem(path.subList(1, path.size()));
        } else {
            return null;
        }
    }

    public Org getDocument(String title) {
        return fileMap.get(title);
    }

    public void loadFiles(Context context) throws IOException {
        File doc_dir = context.getDir("doc_dir", Context.MODE_PRIVATE);
        File[] files = doc_dir.listFiles();
        for (int i=0; i<files.length; ++i) {
            Org document = new Org(new OrgFile(files[i], context));
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
            GlobalState.getWriteLock();
            try {
                DropboxAPI.Entry meta = mDBApi.metadata("/org", 100, null, true, null);

                SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");//TODO is this right?

                HashMap<String, Boolean> synced = new HashMap<>();

                for (int i=0; i<meta.contents.size(); ++i) {
                    String path = meta.contents.get(i).path;
                    String filename = meta.contents.get(i).fileName();
                    if (filename.substring(filename.length()-4, filename.length()).equals(".bak")) {
                        continue;
                    }

                    File doc_dir = context.getDir("doc_dir", Context.MODE_PRIVATE);
                    File file = new File(doc_dir, filename);

                    boolean upload = false;
                    Org doc = null;

                    if (fileMap.containsKey(filename)) {
                        try {
                            String modified_string = meta.contents.get(i).modified;
                            Date dropbox_modified = format.parse(modified_string);

                            doc = fileMap.get(filename);
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
                        if (doc.file.deleted) {
                            try {
                                mDBApi.delete(path);
                            } catch (DropboxException e) {
                                Log.d("thjread.organise", e.toString());
                            }
                            files.remove(doc);
                            fileMap.remove(doc.title);
                        } else {
                            FileInputStream inputStream = new FileInputStream(file);
                            try {
                                mDBApi.delete(path + ".bak");
                            } catch (DropboxException e) {
                                //no backups
                            }
                            mDBApi.move(path, path + ".bak");
                            mDBApi.putFile(path, inputStream, file.length(), null, null);
                            inputStream.close();
                        }
                    } else {
                        FileOutputStream outputStream = new FileOutputStream(file);
                        mDBApi.getFile(path, null, outputStream, null);
                        outputStream.close();
                        OrgFile orgFile = new OrgFile(filename, context);
                        orgFile.write_file.delete();
                        addDocument(new Org(orgFile));
                    }
                    synced.put(filename, true);
                }

                for (int i=0; i<files.size(); ++i) {
                    String filename = files.get(i).title;
                    if (!synced.containsKey(filename)) {
                        if (files.get(i).file.deleted) {
                            files.remove(i);
                            fileMap.remove(filename);
                        } else {
                            File doc_dir = context.getDir("doc_dir", Context.MODE_PRIVATE);
                            File file = new File(doc_dir, filename);
                            String path = "/org/" + filename;
                            FileInputStream inputStream = new FileInputStream(file);
                            try {
                                mDBApi.delete(path + ".bak");
                            } catch (DropboxException e) {
                                //no backups
                            }
                            //mDBApi.move(path, path+".bak");
                            mDBApi.putFile(path, inputStream, file.length(), null, null);
                            inputStream.close();
                            Log.d("thjread.organise", "uploading new file");
                        }
                    }
                }

            } catch (Exception e) {//TODO
                Log.d("thjread.organise", e.toString());
            }

            GlobalState.returnWriteLock();

            if (callback != null) {
                callback.syncFilesCallback(orgfiles);
            }
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

