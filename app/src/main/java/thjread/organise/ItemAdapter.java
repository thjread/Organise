package thjread.organise;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.CardView;
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
    private Integer animateId;
    public boolean hasSetTransitionName = false;
    public CardView transitionView;
    private ArrayList<OrgItem> items;

    public ItemAdapter(Context context, ArrayList<OrgItem> items, int animateId) {
        super(context, 0, items);
        this.animateId = animateId;
        this.items = items;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final OrgItem item = getItem(position);

        convertView = ItemView.getView(item, convertView, parent, true, true, (item.child_number%2) == 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (animateId != null && !hasSetTransitionName) {
                if (item.id == animateId) {
                    final CardView cardview = (CardView) convertView.findViewById(R.id.item_cardview);
                    cardview.setTransitionName(convertView.getContext().getString(R.string.item_transition));
                    hasSetTransitionName = true;
                    transitionView = cardview;
                    ValueAnimator anim = new ValueAnimator();
                    boolean isDone = item.keywords.keywordType(item.keyword) == Org.Keyword.DONE_KEYWORD_TYPE;
                    anim.setIntValues(ItemView.getColor(item, false, false, isDone),
                            ItemView.getColor(item, true, (item.child_number%2) == 0, isDone));
                    anim.setEvaluator(new ArgbEvaluator());
                    anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            int color = (Integer) animation.getAnimatedValue();
                            cardview.setBackgroundColor(color);
                        }
                    });
                    anim.setDuration(350);
                    anim.start();
                } else {
                    convertView.findViewById(R.id.item_cardview).setTransitionName("");
                }
            }
        }

        final ArrayAdapter<OrgItem> adapter = this;
        final View v = convertView;
        convertView.setOnTouchListener(new OnSwipeTouchListener(convertView.getContext()) {
            public void onSwipeRight() {
                item.nextKeyword();
                adapter.notifyDataSetChanged();
            }

            public void onTap() {
                item.toggleExpanded(items, adapter, position);
                adapter.notifyDataSetChanged();
            }
        });

        return convertView;
    }
}