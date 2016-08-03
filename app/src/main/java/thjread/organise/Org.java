package thjread.organise;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class Org {
    public ArrayList<OrgItem> rootItems;
    public ArrayList<OrgItem> items;
    public String title;
    public OrgFile file;

    public class Keyword {
        public ArrayList<String> todoKeywords;
        public ArrayList<String> startedKeywords;
        public ArrayList<String> doneKeywords;

        public static final int NO_KEYWORD_TYPE=0;
        public static final int TODO_KEYWORD_TYPE=1;
        public static final int STARTED_KEYWORD_TYPE=2;
        public static final int DONE_KEYWORD_TYPE=3;

        public Keyword(ArrayList<String> todoKeywords, ArrayList<String> startedKeywords,
                       ArrayList<String> doneKeywords) {
            this.todoKeywords = todoKeywords;
            this.startedKeywords = startedKeywords;
            this.doneKeywords = doneKeywords;
        }

        int getMaxKeyword() {
            return todoKeywords.size() + startedKeywords.size() + doneKeywords.size();
        }

        int keywordType (String keyword) {
            if (todoKeywords.contains(keyword)) {
                return TODO_KEYWORD_TYPE;
            } else if (startedKeywords.contains(keyword)) {
                return STARTED_KEYWORD_TYPE;
            } else if (doneKeywords.contains(keyword)) {
                return DONE_KEYWORD_TYPE;
            } else {
                return NO_KEYWORD_TYPE;
            }
        }

        int keywordType (int n) {
            if (n == 0) {
                return NO_KEYWORD_TYPE;
            } else {
                n -= 1;
            }

            if (n < todoKeywords.size()) {
                return TODO_KEYWORD_TYPE;
            }

            n -= todoKeywords.size();
            if (n < startedKeywords.size()) {
                return STARTED_KEYWORD_TYPE;
            }

            n -= startedKeywords.size();
            if (n < doneKeywords.size()) {
                return DONE_KEYWORD_TYPE;
            }

            return NO_KEYWORD_TYPE;
        }

        int keywordToInt (String keyword) {
            int todo = todoKeywords.indexOf(keyword);
            if (todo != -1) return todo + 1;

            int started = startedKeywords.indexOf(keyword);
            if (started != -1) return started + todoKeywords.size() + 1;

            int done = doneKeywords.indexOf(keyword);
            if (done != -1) return done + todoKeywords.size() + startedKeywords.size() + 1;

            return 0;
        }

        String intToKeyword (int n) {
            if (n <= 0) {
                return "";
            } else {
                n -= 1;
            }

            if (n < todoKeywords.size()) {
                return todoKeywords.get(n);
            }

            n -= todoKeywords.size();
            if (n < startedKeywords.size()) {
                return startedKeywords.get(n);
            }

            n -= startedKeywords.size();
            if (n < doneKeywords.size()) {
                return doneKeywords.get(n);
            }

            return "";
        }
    }

    public Org(OrgFile file) {
        this.file = file;

        ArrayList<String> todoKeywords = new ArrayList<>(); todoKeywords.add("TODO");
        ArrayList<String> startedKeywords = new ArrayList<>(); startedKeywords.add("STARTED");
        ArrayList<String> doneKeywords = new ArrayList<>(); doneKeywords.add("DONE");
        Keyword keyword = new Keyword(todoKeywords, startedKeywords, doneKeywords);

        rootItems = new ArrayList<>();
        items = new ArrayList<>();

        while (!file.isEmpty()) {
            OrgItem item = new OrgItem(keyword, items, null, rootItems.size(), this);
            if (item.parse(file)) {
                rootItems.add(item);
                items.add(item);
            }
        }

        title = file.title;
    }

    public String serialise() {
        String s = "";
        for (int i = 0; i < rootItems.size(); ++i) {
            s += rootItems.get(i).serialise();
        }
        return s;
    }

    public void resetExpanded() {
        for (int i=0; i<items.size(); ++i) {
            items.get(i).expandState = 0;
        }
    }
}