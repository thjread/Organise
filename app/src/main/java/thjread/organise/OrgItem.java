package thjread.organise;

import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class OrgItem {
    String title;
    String section;
    ArrayList<OrgItem> children;
    int keyword;
    Date scheduled;
    Date deadline;
    Date done;

    HashMap<String, Integer> keywords;

    int treeLevel;

    public OrgItem(HashMap<String, Integer> keywords) {
        this.keywords = keywords;
        children = new ArrayList<OrgItem>();
    }
    
    public OrgItem(HashMap<String, Integer> keywords, String title, Date scheduled,
                   Date deadline, Date done, int keyword, int treeLevel) {
        this.keywords = keywords;
        this.title = title;
        this.scheduled = scheduled;
        this.deadline = deadline;
        this.done = done;
        this.keyword = keyword;
        this.treeLevel = treeLevel;
        children = new ArrayList<OrgItem>();
    }

    public void addChild(OrgItem child) {
        children.add(child);
    }

    public boolean parse(OrgFile file) {
        boolean success = false;
        while (!success) {
            if (file.isEmpty()) {
                return false;
            }
            success = parseFirstLine(file.removeLine());
        }

        section = "";

        while (!file.isEmpty() && parseSection(file.peekLine())) {
            file.removeLine();
        }

        while (!file.isEmpty()) {
            String[] tokens = tokenise(file.peekLine());
            if (tokens.length != 0) {
                int stars = starNum(tokens[0]);
                if (stars > treeLevel) {
                    OrgItem item = new OrgItem(keywords);
                    item.parse(file);
                    this.addChild(item);
                } else {
                    return true;
                }
            } else {
                file.removeLine();
            }
        }

        return true;
    }

    private boolean parseFirstLine(String line) {
        String[] tokens = tokenise(line);
        int tokenPointer = 0;
        if (tokens.length == 0) {
            return false;
        }

        if (parseStars(tokens[tokenPointer], true)) {
            tokenPointer++;
            if (tokenPointer >= tokens.length) return false;
        } else {
            return false;
        }

        if (parseKeyword(tokens[tokenPointer])) {
            tokenPointer++;
            if (tokenPointer >= tokens.length) return false;
        } else {
            keyword = -1;
        }

        title = "";
        for (; tokenPointer < tokens.length; ++tokenPointer) {
            title += tokens[tokenPointer];
            if (tokenPointer != tokens.length-1) {
                title += " ";
            }
        }

        //TODO add tag parsing

        return true;
    }

    private String[] tokenise(String line) {
        return line.split("\\s+");
    }

    private int starNum(String stars) {
        int numStars = 0;

        char[] chars = stars.toCharArray();
        for (int i=0; i<chars.length; ++i) {
            if (chars[i] != '*') {
                return 0;
            }
            numStars++;
        }

        return numStars;
    }

    private boolean parseStars(String stars, boolean store) {
        treeLevel = starNum(stars);

        if (treeLevel == 0) {
            return false;
        } else {
            return true;
        }
    }

    private boolean parseKeyword(String keyword) {
        Integer key = keywords.get(keyword);
        if (key != null) {
            this.keyword = key;
            return true;
        } else {
            return false;
        }
    }

    private boolean parsePriority(String priority) {
        char[] chars = priority.toCharArray();
        if (chars[0] == '#' && chars[1] == '[' && chars[chars.length-1] == ']') {
            return true;
        } else {
            return false;
        }
    }

    private boolean parseSection(String line) {
        String[] tokens = tokenise(line);
        if (tokens.length == 0) {
            return false;
        }

        if (starNum(tokens[0]) != 0) {
            return false;
        } else {
            section += line + "\n";
            return true;
        }
    }
}
