package thjread.organise;

<<<<<<< HEAD
import android.util.Log;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;

public class Org {
    ArrayList<OrgItem> rootItems;

    public Org (OrgFile file) {
        HashMap<String, Integer> keywords = new HashMap<String, Integer>();
        keywords.put("TODO", 0);
        keywords.put("STARTED", 1);
        keywords.put("DONE", 2);

        rootItems = new ArrayList<OrgItem>();

        while (!file.isEmpty()) {
            OrgItem item = new OrgItem(keywords);
            if (item.parse(file)) {
                rootItems.add(item);
            }
        }
    }
=======
/**
 * Created by tread on 08/06/16.
 */
public class Org {
>>>>>>> fda365c97e91fac4ef2099ef4d7b0b3803358063
}