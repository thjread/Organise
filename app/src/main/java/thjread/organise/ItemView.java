package thjread.organise;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
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
    private static Integer base_color;

    public static View getView(OrgItem item, View convertView, ViewGroup parent, boolean indent, boolean do_colors) {
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
        }
        if (base_color == null) {
            base_color = ContextCompat.getColor(convertView.getContext(), R.color.basecard);
        }

        TextView headline = (TextView) convertView.findViewById(R.id.headline);
        TextView keyword = (TextView) convertView.findViewById(R.id.keyword);
        String key = "";
        switch(item.keyword) {
            case 0: key = "TODO: ";
                break;
            case 1: key = "STARTED: ";
                break;
            case 2: key = "DONE: ";
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
            int paddingStart = Math.round(30 * scale * (item.treeLevel - 1));
            LinearLayout container = (LinearLayout) convertView.findViewById(R.id.item_container);
            container.setPaddingRelative(paddingStart, container.getPaddingTop(),
                    container.getPaddingEnd(), container.getPaddingBottom());
        }

        int color = getColor(item, do_colors);
        convertView.findViewById(R.id.item_cardview).setBackgroundColor(color);

        return convertView;
    }

    public static int getColor(OrgItem item, boolean do_colors) { //Must call getView first to populate colors
        int color;
        if (do_colors) {
            color = colors.get((item.treeLevel - 1) % 8);
        } else {
            color = base_color;
        }
        return color;
    }
}
