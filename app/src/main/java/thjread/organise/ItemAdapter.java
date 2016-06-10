package thjread.organise;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tread on 07/06/16.
 */
public class ItemAdapter extends ArrayAdapter<OrgItem> {
    public ItemAdapter(Context context, ArrayList<OrgItem> items) {
        super(context, 0, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        OrgItem item = getItem(position);

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
            deadlineText += "DEADLINE: " + DateFormatter.format(item.deadline);
        }
        if (item.scheduled != null) {
            if (deadlineText != "") deadlineText += " ";
            deadlineText += "SCHEDULED: " + DateFormatter.format(item.scheduled);
        }

        if (deadlineText != "") {
            deadline.setVisibility(View.VISIBLE);
            deadline.setText(deadlineText);
        } else {
            deadline.setVisibility(View.GONE);
        }

        Context context = convertView.getContext();
        final float scale = context.getResources().getDisplayMetrics().density;
        int paddingStart = Math.round(30*scale*(item.treeLevel-1));
        LinearLayout container = (LinearLayout) convertView.findViewById(R.id.item_container);
        container.setPaddingRelative(paddingStart, container.getPaddingTop(),
                container.getPaddingEnd(), container.getPaddingBottom());

        return convertView;
    }
}
