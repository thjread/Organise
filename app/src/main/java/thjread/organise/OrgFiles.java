package thjread.organise;

import android.content.Context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OrgFiles {
    ArrayList<OrgFile> files;

    public void loadFiles(Context context) throws IOException {
        files = new ArrayList<OrgFile>();
        files.add(new OrgFile(R.raw.test, context));
    }

    public List<OrgFile> getFiles() {
        return files;
    }
}

