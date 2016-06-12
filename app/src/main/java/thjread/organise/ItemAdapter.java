package thjread.organise;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
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

    public ItemAdapter(Context context, ArrayList<OrgItem> items, int animateId) {
        super(context, 0, items);
        this.animateId = animateId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        OrgItem item = getItem(position);

        convertView = ItemView.getView(item, convertView, parent, true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (animateId != null) {
                if (item.id == animateId) {
                    convertView.findViewById(R.id.item_cardview)
                            .setTransitionName(convertView.getContext().getString(R.string.item_transition));
                    hasSetTransitionName = true;
                } else {
                    convertView.findViewById(R.id.item_cardview).setTransitionName("");
                }
            }
        }
        return convertView;
    }
}