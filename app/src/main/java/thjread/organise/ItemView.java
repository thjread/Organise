package thjread.organise;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by tread on 11/06/16.
 */
public class ItemView {

    private static ArrayList<Integer> colors;
    private static int base_color;
    private static int text_color;

    private static final float INDENT = 24;

    public static View getView(OrgItem item, View convertView, ViewGroup parent, boolean indent, boolean do_colors,
                               boolean light) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview, parent, false);
        }

        if (colors == null) {
            colors = new ArrayList<>();
            colors.add(ContextCompat.getColor(convertView.getContext(), R.color.hierarchy1));
            colors.add(ContextCompat.getColor(convertView.getContext(), R.color.hierarchy2));
            colors.add(ContextCompat.getColor(convertView.getContext(), R.color.hierarchy3));
            colors.add(ContextCompat.getColor(convertView.getContext(), R.color.hierarchy4));
            colors.add(ContextCompat.getColor(convertView.getContext(), R.color.hierarchy5));
            colors.add(ContextCompat.getColor(convertView.getContext(), R.color.hierarchy6));
            colors.add(ContextCompat.getColor(convertView.getContext(), R.color.hierarchy7));
            colors.add(ContextCompat.getColor(convertView.getContext(), R.color.hierarchy8));
            base_color = ContextCompat.getColor(convertView.getContext(), R.color.basecard);
            text_color = ContextCompat.getColor(convertView.getContext(), R.color.itemview_textcolor);
        }

        TextView headline = (TextView) convertView.findViewById(R.id.headline);
        TextView keyword = (TextView) convertView.findViewById(R.id.keyword);
        String key = item.keywords.intToKeyword(item.keyword) + ": ";
        if (item.keyword == 0) {
            key = "";
        }
        int keywordType = item.keywords.keywordType(item.keyword);
        switch(keywordType) {
            case Org.Keyword.TODO_KEYWORD_TYPE:
                if (item.treeLevel == 4 || item.treeLevel == 5 && do_colors) {
                    keyword.setTextColor(Color.parseColor("#FFCCBC"));
                } else {
                    keyword.setTextColor(Color.parseColor("#FF5722"));
                }
                break;
            case Org.Keyword.STARTED_KEYWORD_TYPE:
                keyword.setTextColor(Color.parseColor("#FFD54F"));
                break;
        }
        String head = item.title;
        if (!item.children.isEmpty() && item.expandState == 0) {
            head += " ...";
        }
        keyword.setText(key);
        headline.setText(head);

        TextView deadline = (TextView) convertView.findViewById(R.id.deadline);
        TextView deadlineText = (TextView) convertView.findViewById(R.id.deadline_text);
        String deadlineString = "";
        String deadlineTextString = "";
        if (item.deadline != null) {
            deadlineString = "Deadline: ";
            deadlineTextString = DateFormatter.format(item.deadline, true);
        }
        if (item.scheduled != null) {
            deadlineString = "Scheduled: ";
            deadlineTextString = DateFormatter.format(item.scheduled, false);
        }

        if (!deadlineString.equals("")) {
            deadline.setVisibility(View.VISIBLE);
            deadlineText.setVisibility(View.VISIBLE);
            deadline.setText(deadlineString);
            deadlineText.setText(deadlineTextString);
        } else {
            deadline.setVisibility(View.GONE);
            deadlineText.setVisibility(View.GONE);
        }

        if (indent) {
            Context context = convertView.getContext();
            final float scale = context.getResources().getDisplayMetrics().density;
            int paddingStart = Math.round(INDENT * scale * (item.treeLevel - 1));
            LinearLayout container = (LinearLayout) convertView.findViewById(R.id.item_container);
            container.setPaddingRelative(paddingStart, container.getPaddingTop(),
                    container.getPaddingEnd(), container.getPaddingBottom());
        }

        int color = getColor(item, do_colors, light, keywordType == Org.Keyword.DONE_KEYWORD_TYPE);
        CardView card_view = (CardView) convertView.findViewById(R.id.item_cardview);
        card_view.setBackgroundColor(color);

        if (keywordType == Org.Keyword.DONE_KEYWORD_TYPE) {
            headline.setTextColor(lightenColor(color, 0.5f));
            deadline.setTextColor(lightenColor(color, 0.5f));
            deadlineText.setTextColor(lightenColor(color, 0.5f));
            int key_color = Color.parseColor("#00C853");
            keyword.setTextColor(ColorUtils.blendARGB(key_color, color, 0.3f));
        } else {
            headline.setTextColor(text_color);
            deadline.setTextColor(text_color);
            deadlineText.setTextColor(text_color);
        }
        return convertView;
    }

    public static int getColor(OrgItem item, boolean do_colors, boolean light, boolean done) { //Must call getView first to populate colors
        int color;
        if (do_colors) {
            color = colors.get((item.treeLevel - 1) % 8);
        } else {
            color = base_color;
        }
        if (light) {
            color = lightColor(color);
        }

        if (done) {
            return lightenColor(color, 0.2f);
        } else {
            return color;
        }
    }

    public static int lightColor(int color) {
        float hsl[] = {0, 0, 0};
        ColorUtils.colorToHSL(color, hsl);
        hsl[2] += 0.05;
        return ColorUtils.HSLToColor(hsl);
    }

    public static int lightenColor(int color, float amount) {
        float hsl[] = {0, 0, 0};
        ColorUtils.colorToHSL(color, hsl);
        hsl[2] = 1-((1-amount)*(1-hsl[2]));
        return ColorUtils.HSLToColor(hsl);
    }
}
