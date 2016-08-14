package thjread.organise;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.widget.Space;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ItemAdapter extends RecyclerView.Adapter {

    public interface LongTapListener {
        void onLongTap(OrgItem item);
    }

    private Integer animateId;
    public boolean hasSetTransitionName = false;
    public CardView transitionView;
    private ArrayList<OrgItem> items;
    private LongTapListener longTapListener;

    public ItemAdapter(Context context, ArrayList<OrgItem> items, int animateId, LongTapListener longTapListener) {
        super();
        this.animateId = animateId;
        this.items = items;
        this.longTapListener = longTapListener;
    }

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

    public void add(int position, OrgItem item) {
        items.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(OrgItem item) {
        remove(items.indexOf(item));
    }

    public void remove(int position) {
        items.remove(position);
        notifyItemRemoved(position);
    }

    public int getPosition(OrgItem item) {
        return items.indexOf(item);
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview, parent, false);
        return new ItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder h, int position) {
        final ItemViewHolder holder = (ItemViewHolder) h;
        final OrgItem item = items.get(position);
        ItemView.bindViewHolder(item, holder, true, true, (item.child_number%2) == 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (animateId != null && !hasSetTransitionName) {
                if (item.id == animateId) {
                    holder.card_view.setTransitionName(holder.context.getString(R.string.item_transition));
                    hasSetTransitionName = true;
                    transitionView = holder.card_view;
                    ValueAnimator anim = new ValueAnimator();
                    boolean isDone = item.keywords.keywordType(item.keyword) == Org.Keyword.DONE_KEYWORD_TYPE;
                    anim.setIntValues(ItemView.getColor(item, false, false, isDone),
                            ItemView.getColor(item, true, (item.child_number%2) == 0, isDone));
                    anim.setEvaluator(new ArgbEvaluator());
                    anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            int color = (Integer) animation.getAnimatedValue();
                            holder.card_view.setBackgroundColor(color);
                        }
                    });
                    anim.setDuration(350);
                    anim.start();
                } else {
                    holder.card_view.setTransitionName("");
                }
            }
        }

        final ItemAdapter adapter = this;
        holder.container.setOnTouchListener(new OnSwipeTouchListener(holder.context, holder.container, true) {
            public void onSwipeRight() {
                item.nextKeyword();
                notifyItemChanged(holder.getAdapterPosition());
            }

            public void onTap() {
                item.toggleExpanded(items, adapter, holder.getAdapterPosition());
                notifyItemChanged(holder.getAdapterPosition());
            }

            public void onLongTap() {
                longTapListener.onLongTap(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}