package thjread.organise;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.ChangeBounds;
import android.transition.Explode;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.BounceInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.RunnableFuture;

public class DocumentActivity extends AppCompatActivity {
    private ArrayList<OrgItem> listItems;
    private ItemAdapter adapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setExitTransition(null);
            getWindow().setSharedElementExitTransition(null);
            //getWindow().setEnterTransition(new Explode());
            ActivityCompat.postponeEnterTransition(this);
        }

        Bundle b = getIntent().getExtras();
        int id = -1;
        if (b != null) {
            id = b.getInt("id");
        }

        setContentView(R.layout.activity_document);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Todo.org");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listView = (ListView) findViewById(R.id.listview);
        listItems = new ArrayList<>();

        Org org = GlobalState.getCurrentOrg();
        org.resetExpanded();

        for (int i=0; i<org.rootItems.size(); ++i) {
            OrgItem item = org.rootItems.get(i);
            listItems.add(item);
        }

        adapter = new ItemAdapter(this, listItems, id);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                OrgItem item = (OrgItem) parent.getItemAtPosition(position);
                item.toggleExpanded(listItems, adapter, position);
            }
        });

        if (id != -1) {
            final OrgItem item = expandItemWithId(id, org.rootItems);
            if (item != null) {
                adapter.notifyDataSetChanged();
                int pos = adapter.getPosition(item);
                final Activity thisActivity = this;
                listView.setSelection(pos);
                listView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            if (adapter.hasSetTransitionName) {
                                ActivityCompat.startPostponedEnterTransition(thisActivity);
                                adapter.transitionView.setTransitionName("");
                            }
                        }
                    }
                });
            }
        }
    }

    private OrgItem expandItemWithId(int id, ArrayList<OrgItem> list) {
        for (int i=0; i<list.size(); ++i) {
            OrgItem item = list.get(i);
            if (item.id == id) {
                return item;
            } else if (item.all_child_ids.contains(id)) {
                item.setExpanded(1, listItems, adapter, adapter.getPosition(item));
                return expandItemWithId(id, item.children);
            }
        }
        return null;

    }

}
