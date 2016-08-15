package thjread.organise;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

public class ItemViewHolder extends RecyclerView.ViewHolder {
    public TextView deadline;
    public TextView deadlineText;
    public TextView scheduled;
    public TextView scheduledText;
    public TextView headline;
    public TextView keyword;
    public Space spacer;
    public Context context;
    public LinearLayout container;
    public CardView card_view;

    public ItemViewHolder(View v) {
        super(v);
        context = v.getContext();
        deadline = (TextView) v.findViewById(R.id.deadline);
        deadlineText = (TextView) v.findViewById(R.id.deadline_text);
        scheduled = (TextView) v.findViewById(R.id.scheduled);
        scheduledText = (TextView) v.findViewById(R.id.scheduled_text);
        spacer = (Space) v.findViewById(R.id.spacer);
        headline = (TextView) v.findViewById(R.id.headline);
        keyword = (TextView) v.findViewById(R.id.keyword);
        container = (LinearLayout) v.findViewById(R.id.item_container);
        card_view = (CardView) v.findViewById(R.id.item_cardview);
    }
}
