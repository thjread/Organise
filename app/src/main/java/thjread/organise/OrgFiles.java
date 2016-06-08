package thjread.organise;

<<<<<<< HEAD
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

=======
/**
 * Created by tread on 08/06/16.
 */
public class OrgFiles {
}
>>>>>>> fda365c97e91fac4ef2099ef4d7b0b3803358063
