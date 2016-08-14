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
import android.widget.Space;
import android.widget.TextView;

import java.util.ArrayList;

public class ItemView {

    private static ArrayList<Integer> colors;
    private static int base_color;
    private static int text_color;

    private static final float INDENT = 24;

    public static void bindViewHolder(OrgItem item, ItemAdapter.ItemViewHolder holder, boolean indent,
                                      boolean do_colors, boolean light) {
        Context context = holder.context;
        if (colors == null) {
            colors = new ArrayList<>();
            colors.add(ContextCompat.getColor(context, R.color.hierarchy1));
            colors.add(ContextCompat.getColor(context, R.color.hierarchy2));
            colors.add(ContextCompat.getColor(context, R.color.hierarchy3));
            colors.add(ContextCompat.getColor(context, R.color.hierarchy4));
            colors.add(ContextCompat.getColor(context, R.color.hierarchy5));
            colors.add(ContextCompat.getColor(context, R.color.hierarchy6));
            colors.add(ContextCompat.getColor(context, R.color.hierarchy7));
            colors.add(ContextCompat.getColor(context, R.color.hierarchy8));
            base_color = ContextCompat.getColor(context, R.color.basecard);
            text_color = ContextCompat.getColor(context, R.color.itemview_textcolor);
        }

        String key = item.keywords.intToKeyword(item.keyword) + ": ";
        if (item.keyword == 0) {
            key = "";
        }
        int keywordType = item.keywords.keywordType(item.keyword);
        switch(keywordType) {
            case Org.Keyword.TODO_KEYWORD_TYPE:
                if (item.treeLevel == 4 || item.treeLevel == 5 && do_colors) {
                    holder.keyword.setTextColor(Color.parseColor("#FFCCBC"));
                } else {
                    holder.keyword.setTextColor(Color.parseColor("#FF5722"));
                }
                break;
            case Org.Keyword.STARTED_KEYWORD_TYPE:
                holder.keyword.setTextColor(Color.parseColor("#FFD54F"));
                break;
        }
        String head = item.title;
        if (!item.children.isEmpty() && item.expandState == 0) {
            head += " ...";
        }
        holder.keyword.setText(key);
        holder.headline.setText(head);

        if (item.deadline != null) {
            holder.deadline.setText("Deadline: ");
            holder.deadlineText.setText(DateFormatter.format(item.deadline, true));
            holder.deadline.setVisibility(View.VISIBLE);
            holder.deadlineText.setVisibility(View.VISIBLE);
        } else {
            holder.deadline.setVisibility(View.GONE);
            holder.deadlineText.setVisibility(View.GONE);
        }
        if (item.scheduled != null) {
            holder.scheduled.setText("Scheduled: ");
            holder.scheduledText.setText(DateFormatter.format(item.scheduled, false));
            holder.scheduled.setVisibility(View.VISIBLE);
            holder.scheduledText.setVisibility(View.VISIBLE);
        } else {
            holder.scheduled.setVisibility(View.GONE);
            holder.scheduledText.setVisibility(View.GONE);
        }

        if (item.scheduled != null && item.deadline != null) {
            holder.spacer.setVisibility(View.VISIBLE);
        } else {
            holder.spacer.setVisibility(View.GONE);
        }

        if (indent) {
            final float scale = context.getResources().getDisplayMetrics().density;
            int paddingStart = Math.round(INDENT * scale * (item.treeLevel - 1));
            holder.container.setPaddingRelative(paddingStart, holder.container.getPaddingTop(),
                    holder.container.getPaddingEnd(), holder.container.getPaddingBottom());
        }

        int color = getColor(item, do_colors, light, keywordType == Org.Keyword.DONE_KEYWORD_TYPE);
        holder.card_view.setBackgroundColor(color);

        if (keywordType == Org.Keyword.DONE_KEYWORD_TYPE) {
            holder.headline.setTextColor(lightenColor(color, 0.5f));
            holder.deadline.setTextColor(lightenColor(color, 0.5f));
            holder.deadlineText.setTextColor(lightenColor(color, 0.5f));
            holder.scheduled.setTextColor(lightenColor(color, 0.5f));
            holder.scheduledText.setTextColor(lightenColor(color, 0.5f));
            int key_color = Color.parseColor("#00C853");
            holder.keyword.setTextColor(ColorUtils.blendARGB(key_color, color, 0.3f));
        } else {
            holder.headline.setTextColor(text_color);
            holder.deadline.setTextColor(text_color);
            holder.deadlineText.setTextColor(text_color);
            holder.scheduled.setTextColor(text_color);
            holder.scheduledText.setTextColor(text_color);
        }
    }

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
        TextView scheduled = (TextView) convertView.findViewById(R.id.scheduled);
        TextView scheduledText = (TextView) convertView.findViewById(R.id.scheduled_text);
        Space spacer = (Space) convertView.findViewById(R.id.spacer);
        if (item.deadline != null) {
            deadline.setText("Deadline: ");
            deadlineText.setText(DateFormatter.format(item.deadline, true));
            deadline.setVisibility(View.VISIBLE);
            deadlineText.setVisibility(View.VISIBLE);
        } else {
            deadline.setVisibility(View.GONE);
            deadlineText.setVisibility(View.GONE);
        }
        if (item.scheduled != null) {
            scheduled.setText("Scheduled: ");
            scheduledText.setText(DateFormatter.format(item.scheduled, false));
            scheduled.setVisibility(View.VISIBLE);
            scheduledText.setVisibility(View.VISIBLE);
        } else {
            scheduled.setVisibility(View.GONE);
            scheduledText.setVisibility(View.GONE);
        }

        if (item.scheduled != null && item.deadline != null) {
            spacer.setVisibility(View.VISIBLE);
        } else {
            spacer.setVisibility(View.GONE);
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
            scheduled.setTextColor(lightenColor(color, 0.5f));
            scheduledText.setTextColor(lightenColor(color, 0.5f));
            int key_color = Color.parseColor("#00C853");
            keyword.setTextColor(ColorUtils.blendARGB(key_color, color, 0.3f));
        } else {
            headline.setTextColor(text_color);
            deadline.setTextColor(text_color);
            deadlineText.setTextColor(text_color);
            scheduled.setTextColor(text_color);
            scheduledText.setTextColor(text_color);
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
