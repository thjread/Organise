package thjread.organise;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class DocumentActivity extends AppCompatActivity implements AddTaskCallbackInterface {
    private Org org;
    private ArrayList<OrgItem> listItems;
    private ItemAdapter adapter;
    private RecyclerView recyclerView;

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

        org = GlobalState.getCurrentOrg();

        setContentView(R.layout.activity_document);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(org.title);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);
        listItems = new ArrayList<>();

        org.resetExpanded();

        for (int i = 0; i < org.rootItems.size(); ++i) {
            OrgItem item = org.rootItems.get(i);
            listItems.add(item);
        }

        LinearLayout container = (LinearLayout) findViewById(R.id.document_linear_layout);
        container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                launchItemActionFragment(null, -1, -1);
                return true;
            }
        });

        recyclerView.setLongClickable(true);

        adapter = new ItemAdapter(this, listItems, id, new ItemAdapter.LongTapListener() {
            @Override
            public void onLongTap(OrgItem item, ItemViewHolder vH, MotionEvent e) {
                int screenLoc[] = {0, 0};
                vH.container.getLocationOnScreen(screenLoc);
                launchItemActionFragment(item, (int) e.getX()+screenLoc[0], (int) e.getY()+screenLoc[1]);
            }
        });
        recyclerView.setAdapter(adapter);

        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(120);
        animator.setRemoveDuration(120);
        animator.setChangeDuration(0);
        recyclerView.setItemAnimator(animator);

        if (id != -1) {
            final OrgItem item = expandItemWithId(id, org.rootItems);
            if (item != null) {
                int pos = adapter.getPosition(item);
                final Activity thisActivity = this;
                recyclerView.scrollToPosition(pos);
                recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            if (adapter.hasSetTransitionName) {
                                ActivityCompat.startPostponedEnterTransition(thisActivity);
                                adapter.transitionView.setTransitionName("");
                                recyclerView.removeOnLayoutChangeListener(this);
                            }
                        }
                    }
                });
            }
        }

        if (listItems.size() == 0) {
            recyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.document, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_delete_document) {
            deleteDocument();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void deleteDocument() {
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle)
                .setTitle("Confirm delete document")
                .setMessage("Delete \"" + org.title + "\"")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        org.file.delete(org);
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void deleteItem(OrgItem item) {
        if (item.expandState != 0) {
            for (OrgItem c : item.children) {
                deleteItem(c);
            }
        }
        adapter.remove(item);
    }

    public void onItemChange(OrgItem item, boolean isEdit, boolean deleted) {
        if (deleted) {
            deleteItem(item);
        } else if (isEdit) {
            adapter.notifyItemChanged(adapter.getPosition(item));
        } else {
            int index;
            if (item.child_number == 0) {
                if (item.parent == null) {
                    index = 0;
                } else {
                    index = listItems.indexOf(item.parent) + 1;
                }
            } else {
                List<OrgItem> children;
                if (item.parent == null) {
                    children = item.document.rootItems;
                } else {
                    children = item.parent.children;
                }
                if (item.child_number < children.size() - 1) {
                    index = listItems.indexOf(children.get(item.child_number + 1));
                } else {
                    index = listItems.indexOf(children.get(item.child_number - 1));
                    while (index < listItems.size() && listItems.get(index).treeLevel >= item.treeLevel) {
                        index++;
                    }
                }
            }
            if (item.parent != null && item.parent.getExpanded() == 0) {
                item.parent.expandState = 1;
            }
            adapter.add(index, item);
            if (recyclerView.getVisibility() == View.GONE) {
                recyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void launchItemActionFragment(OrgItem item, int x, int y) {
        Fragment itemAction = ItemAction.newInstance(item, org, x, y);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(itemAction, null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
    }

    private OrgItem expandItemWithId(int id, ArrayList<OrgItem> list) {
        for (int i = 0; i < list.size(); ++i) {
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
