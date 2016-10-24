package thjread.organise;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class ItemAdapter extends RecyclerView.Adapter {

    public interface LongTapListener {
        void onLongTap(OrgItem item, ItemViewHolder vh, MotionEvent e);
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
                    holder.card_view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
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
                    anim.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            holder.card_view.setLayerType(View.LAYER_TYPE_NONE, null);
                        }
                    });
                    anim.start();
                } else {
                    holder.card_view.setTransitionName("");
                }
            }
        }

        final ItemAdapter adapter = this;
        holder.card_view.setOnTouchListener(new OnSwipeTouchListener(holder.context, holder.container, true) {
            public void onSwipeRight() {
                item.nextKeyword();
                notifyItemChanged(holder.getAdapterPosition());
            }

            public void onTap() {
                item.toggleExpanded(items, adapter, holder.getAdapterPosition());
                if (item.expandState != 0) {
                    notifyItemChanged(getPosition(item));//breaks animation for fold but fixes double tap
                }/* else {
                    String title = item.title;
                    if (!item.children.isEmpty() && item.expandState == 0) {
                        title += " ...";
                    }
                    holder.headline.setText(title);// Using notifyItemChanged breaks animation
                }*/
            }

            public void onLongTap(MotionEvent e) {
                longTapListener.onLongTap(item, holder, e);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}