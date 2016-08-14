package thjread.organise;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrgItem {
    String title;
    String section;
    ArrayList<OrgItem> children;
    int keyword;
    Date scheduled;
    Date deadline;
    Date closed;

    OrgItem parent;
    Org document;

    ArrayList<Integer> all_child_ids;

    int id;
    private static int id_counter = 0;

    SimpleDateFormat format;

    Org.Keyword keywords;

    int treeLevel;
    int expandState  = 0;

    private long lastClick = 0;

    static final int DOUBLE_TAP_TIME = 500;

    ArrayList<OrgItem> items;

    int child_number;

    public OrgItem(Org.Keyword keywords, ArrayList<OrgItem> items, OrgItem parent, int child_number,
                   Org document) {
        this.keywords = keywords;
        this.items = items;
        children = new ArrayList<>();
        scheduled = null;
        deadline = null;
        closed = null;
        format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        this.id = id_counter;
        id_counter++;
        all_child_ids = new ArrayList<>();
        all_child_ids.add(this.id);
        this.parent = parent;
        this.child_number = child_number;
        this.document = document;
        if (parent == null) {
            this.treeLevel = 1;
        } else {
            this.treeLevel = parent.treeLevel+1;
        }
    }

    public ArrayList<String> getPath() {
        ArrayList<String> path;
        if (parent != null) {
            path = parent.getPath();
        } else {
            path = new ArrayList<>();
            path.add(document.title);
        }

        path.add(title);

        return path;
    }

    public void addChild(OrgItem child) {
        children.add(child);
        this.addChildIds(child);
    }

    public void addChildIds(OrgItem child) {
        for (int i=0; i<child.all_child_ids.size(); ++i) {
            all_child_ids.add(child.all_child_ids.get(i));
        }
        if (parent != null) {
            parent.addChildIds(child);
        }
    }

    public int getExpanded() {
        return expandState;
    }

    public void toggleExpanded(List<OrgItem> list, ItemAdapter adapter, int position) {
        if (expandState == 0) {
            setExpanded(1, list, adapter, position);
            lastClick = System.currentTimeMillis();
        } else if (expandState == 1) {
            if (System.currentTimeMillis() - lastClick < DOUBLE_TAP_TIME) {
                setExpanded(2, list, adapter, position);
            } else {
                setExpanded(0, list, adapter, position);
            }
        } else if (expandState == 2) {
            setExpanded(0, list, adapter, position);
        }
    }

    public void setExpanded(int expand, List<OrgItem> list, ItemAdapter adapter, int position) {
        if (expand == 0 && expandState != 0) {
            for (int i=0; i<children.size(); ++i) {
                OrgItem child = children.get(i);
                child.setExpanded(0, list, adapter, position+1);
                adapter.remove(position+1);
            }
        } else if (expand == 1) {
            if (expandState == 0) {
                for (int i = 0; i < children.size(); ++i) {
                    adapter.add(position+i+1, children.get(i));
                }
            }
        } else if (expand == 2) {
            if (expandState == 0) {
                for (int i = 0; i < children.size(); ++i) {
                    adapter.add(position+i+1, children.get(i));
                }
            }
            for (int i=0; i<children.size(); ++i) {
                OrgItem child = children.get(i);
                child.setExpanded(2, list, adapter, list.indexOf(child));
            }
        }

        expandState = expand;
    }

    public void nextKeyword() {
        keyword = (keyword+1) % (keywords.getMaxKeyword() + 1);
        if (keywords.keywordType(keyword) == Org.Keyword.DONE_KEYWORD_TYPE) {
            closed = new Date();
        }
        Log.d("thjread.organise", "writing");
        document.file.write(document);
    }

    public String serialise() {
        String s = "";
        for (int i=0; i<treeLevel; ++i) {
            s += "*";
        }
        s += " ";
        s += keywords.intToKeyword(keyword);
        s += " ";
        s += title;
        s += "\n";
        boolean done_anything = false;
        if (closed != null && keywords.keywordType(keyword) == Org.Keyword.DONE_KEYWORD_TYPE) {
            s += "CLOSED: [" + formatTimestamp(closed) + "] ";
            done_anything = true;
        }
        if (scheduled != null) {
            s += "SCHEDULED: <" + formatTimestamp(scheduled) + "> ";
            done_anything = true;
        }
        if (deadline != null) {
            s += "DEADLINE: <" + formatTimestamp(deadline) + ">";
            done_anything = true;
        }
        if (done_anything) {
            s += "\n";
        }
        for (int i=0; i<children.size(); ++i) {
            s += children.get(i).serialise();
        }
        return s;
    }

    public boolean parse(OrgFile file) { //http://orgmode.org/worg/dev/org-syntax.html
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
                    OrgItem item = new OrgItem(keywords, items, this, this.children.size(), document);
                    item.parse(file);
                    this.addChild(item);
                    items.add(item);
                } else if (stars == 0) {
                    file.removeLine();
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

        if (parseStars(tokens[tokenPointer])) {
            tokenPointer++;
            if (tokenPointer >= tokens.length) return false;
        } else {
            return false;
        }

        if (parseKeyword(tokens[tokenPointer])) {
            tokenPointer++;
            if (tokenPointer >= tokens.length) return false;
        } else {
            keyword = 0;
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
        for (char c : chars) {
            if (c != '*') {
                return 0;
            }
            numStars++;
        }

        return numStars;
    }

    private boolean parseStars(String stars) {
        treeLevel = starNum(stars);

        return (treeLevel != 0);
    }

    private boolean parseKeyword(String keyword) {
        int key = keywords.keywordToInt(keyword);
        if (key != 0) {
            this.keyword = key;
            return true;
        } else {
            return false;
        }
    }

    private boolean parsePriority(String priority) {
        char[] chars = priority.toCharArray();
        return (chars[0] == '#' && chars[1] == '[' && chars[chars.length-1] == ']');
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
            if (section.length() > 0) { //TODO: deal with blank lines properly
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

        return returnPointer;
    }

    private Pair<Date, Integer> readTimestamp(String[] tokens, int tokenPointer) {
        if (tokens[tokenPointer].toCharArray()[0] != '<'
                && tokens[tokenPointer].toCharArray()[0] != '[') {//TODO inactive dates
            return null;
        }
        ArrayList<String> dateTokens = new ArrayList<>();
        boolean success = false;
        int finalPointer = tokenPointer;
        for (int i=tokenPointer; (!success) && (i<tokens.length); ++i) {
            String token = tokens[i];
            if (i == tokenPointer) {
                token = token.substring(1);
            }
            if (token.toCharArray()[token.length()-1] == '>'
                    || token.toCharArray()[token.length()-1] == ']') {
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

        return new Pair<>(d, finalPointer);
    }

    private String formatTimestamp(Date d) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd EEE", Locale.getDefault());
        return format.format(d);
    }
}

