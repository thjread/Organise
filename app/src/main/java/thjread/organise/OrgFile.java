package thjread.organise;

<<<<<<< HEAD
import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Queue;

public class OrgFile {
    ArrayList<String> file;

    public OrgFile(int id, Context context) throws IOException {
        file = new ArrayList<String>();

        InputStream is = context.getResources().openRawResource(id);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        if (is != null) {
            String str = "";
            while ((str = reader.readLine()) != null) {
                file.add(str);
            }
        }
        is.close();
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
=======
/**
 * Created by tread on 08/06/16.
 */
public class OrgFile {
>>>>>>> fda365c97e91fac4ef2099ef4d7b0b3803358063
}
