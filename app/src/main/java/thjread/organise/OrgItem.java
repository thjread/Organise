package thjread.organise;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    Date closed;

    SimpleDateFormat format;

    HashMap<String, Integer> keywords;

    int treeLevel;

    public OrgItem(HashMap<String, Integer> keywords) {
        this.keywords = keywords;
        children = new ArrayList<OrgItem>();
        scheduled = null;
        deadline = null;
        closed = null;
        format = new SimpleDateFormat("yyyy-MM-dd");
    }
    
    public OrgItem(HashMap<String, Integer> keywords, String title, Date scheduled,
                   Date deadline, Date closed, int keyword, int treeLevel) {
        this.keywords = keywords;
        this.title = title;
        this.scheduled = scheduled;
        this.deadline = deadline;
        this.closed = closed;
        this.keyword = keyword;
        this.treeLevel = treeLevel;
        children = new ArrayList<OrgItem>();
        format = new SimpleDateFormat("yyyy-MM-dd");
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
            tokenPointer = parsePlanning(tokens, tokenPointer);
            if (tokenPointer >= tokens.length) break;

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
            int tokenPointer = 0;
            while (tokenPointer < tokens.length) {
                tokenPointer = parsePlanning(tokens, tokenPointer);
                if (tokenPointer >= tokens.length) break;

                section += tokens[tokenPointer] + " ";
                tokenPointer++;
            }
            if (section.length() > 0) {
                section = section.substring(0, section.length() - 1) + "\n";
            }
            return true;
        }
    }

    private int parsePlanning(String[] tokens, int tokenPointer) {
        if (tokens.length <= tokenPointer+1) {
            return tokenPointer;
        }
        Pair<Date, Integer> p = readTimestamp(tokens, tokenPointer+1);
        if (p == null) {
            return tokenPointer;
        }
        int returnPointer = p.u;
        Date d = p.t;

        switch (tokens[tokenPointer]) {
            case "DEADLINE:":
                deadline = d;
                break;
            case "SCHEDULED:":
                scheduled = d;
                break;
            case "CLOSED:":
                closed = d;
                break;
            default:
                return tokenPointer;
        }

        Log.d("thjread.organise", this.title + " " + d.toString());

        return returnPointer;
    }

    private Pair<Date, Integer> readTimestamp(String[] tokens, int tokenPointer) {
        if (tokens[tokenPointer].toCharArray()[0] != '<') {//TODO inactive dates
            return null;
        }
        ArrayList<String> dateTokens = new ArrayList<String>();
        boolean success = false;
        int finalPointer = tokenPointer;
        for (int i=tokenPointer; (!success) && (i<tokens.length); ++i) {
            String token = tokens[i];
            if (i == tokenPointer) {
                token = token.substring(1);
            }
            if (token.toCharArray()[token.length()-1] == '>') {
                token = token.substring(0, token.length()-1);
                finalPointer = i;
                success = true;
            }
            dateTokens.add(token);
        }
        if (!success) return null;

        Date d;
        try {
            d = format.parse(dateTokens.get(0));
        } catch (ParseException e) {
            return null;
        }

        //TODO times, repeats

        return new Pair<Date, Integer>(d, finalPointer+1);
    }
}

class Pair<T, U> {
    public final T t;
    public final U u;

    public Pair(T t, U u) {
        this.t= t;
        this.u= u;
    }
}