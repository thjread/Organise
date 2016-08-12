package thjread.organise;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.ListView;

import java.util.ArrayList;

public class DocumentActivity extends AppCompatActivity implements AddTaskCallbackInterface {
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

        Org org = GlobalState.getCurrentOrg();

        setContentView(R.layout.activity_document);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(org.title);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listView = (ListView) findViewById(R.id.listview);
        listItems = new ArrayList<>();

        org.resetExpanded();

        for (int i=0; i<org.rootItems.size(); ++i) {
            OrgItem item = org.rootItems.get(i);
            listItems.add(item);
        }

        adapter = new ItemAdapter(this, listItems, id, new ItemAdapter.LongTapListener() {
            @Override
            public void onLongTap(OrgItem item) {
                launchItemActionFragment(item);
            }
        });
        listView.setAdapter(adapter);

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
                                listView.removeOnLayoutChangeListener(this);
                            }
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void onItemChange(OrgItem item, boolean isEdit, boolean deleted) {
        if (deleted) {
            listItems.remove(item);
            adapter.notifyDataSetChanged();
            return;
        }

        if (!isEdit) {
            int index;
            if (item.child_number == 0) {
                index = listItems.indexOf(item.parent)+1;
            } else {
                if (item.child_number < item.parent.children.size()-1) {
                    index = listItems.indexOf(item.parent.children.get(item.child_number+1));
                } else {
                    index = listItems.indexOf(item.parent.children.get(item.child_number-1));
                    while(index < listItems.size() && listItems.get(index).treeLevel >= item.treeLevel) {
                        index++;
                    }
                }
            }
            if (item.parent.getExpanded() == 0) {
                item.parent.expandState = 1;
            }
            listItems.add(index, item);
        }

        adapter.notifyDataSetChanged();
    }

    private void launchItemActionFragment(OrgItem item) {
        Fragment itemAction = ItemAction.newInstance(item);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(itemAction, null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
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
