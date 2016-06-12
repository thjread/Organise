package thjread.organise;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by tread on 11/06/16.
 */
public class ItemView {

    public static View getView(OrgItem item, View convertView, ViewGroup parent, boolean indent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview, parent, false);
        }

        TextView headline = (TextView) convertView.findViewById(R.id.headline);
        String head = "* " + item.title;
        if (!item.children.isEmpty() && item.expandState == 0) {
            head += " ...";
        }
        headline.setText(head);


        TextView deadline = (TextView) convertView.findViewById(R.id.deadline);
        String deadlineText = "";
        if (item.deadline != null) {
            deadlineText += "DEADLINE: " + DateFormatter.format(item.deadline, true);
        }
        if (item.scheduled != null) {
            if (!deadlineText.equals("")) deadlineText += " ";
            deadlineText += "SCHEDULED: " + DateFormatter.format(item.scheduled, false);
        }

        if (!deadlineText.equals("")) {
            deadline.setVisibility(View.VISIBLE);
            deadline.setText(deadlineText);
        } else {
            deadline.setVisibility(View.GONE);
        }

        if (indent) {
            Context context = convertView.getContext();
            final float scale = context.getResources().getDisplayMetrics().density;
            int paddingStart = Math.round(30 * scale * (item.treeLevel - 1));
            LinearLayout container = (LinearLayout) convertView.findViewById(R.id.item_container);
            container.setPaddingRelative(paddingStart, container.getPaddingTop(),
                    container.getPaddingEnd(), container.getPaddingBottom());
        }

        return convertView;
    }
}
