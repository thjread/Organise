package thjread.organise;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
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

import java.util.ArrayList;
import java.util.List;

public class DocumentActivity extends AppCompatActivity {
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

        if (listItems.size() == 0) {
            recyclerView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    launchItemActionActivity(null);
                    return true;
                }
            });
        }

        adapter = new ItemAdapter(this, listItems, id, new ItemAdapter.LongTapListener() {
            @Override
            public void onLongTap(OrgItem item, ItemViewHolder vH, MotionEvent e) {
                int screenLoc[] = {0, 0};
                vH.container.getLocationOnScreen(screenLoc);
                launchItemActionActivity(item);
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

        if (listItems.size() == 0) {
            recyclerView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    launchItemActionActivity(null);
                    return true;
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ITEM_ACTION_RESULT) {
            if (resultCode == RESULT_OK) {
                ArrayList<String> path = data.getStringArrayListExtra(ItemAction.RESULT_ITEM_PATH);
                OrgItem item = GlobalState.getFiles().getItem(path);
                Boolean isEdit = data.getBooleanExtra(ItemAction.RESULT_IS_EDIT, false);
                Boolean isDelete = data.getBooleanExtra(ItemAction.RESULT_IS_DELETE, false);
                onItemChange(item, isEdit, isDelete);

                if (isDelete) {
                    item.document.deleteItem(item);
                }
            }
        }
    }

    private void onItemChange(OrgItem item, boolean isEdit, boolean deleted) {
        if (deleted) {
            deleteItem(item);
        } else if (isEdit) {
            adapter.notifyItemChanged(adapter.getPosition(item));
        } else {
            if (item.parent == null) {
                int index;
                ArrayList<OrgItem> children = item.document.rootItems;
                if (item.child_number < children.size() - 1) {
                    index = listItems.indexOf(children.get(item.child_number + 1));
                } else {
                    index = listItems.indexOf(children.get(item.child_number - 1));
                    while (index < listItems.size() && listItems.get(index).treeLevel >= item.treeLevel) {
                        index++;
                    }
                }
                adapter.add(index, item);
            } else {
                if (item.parent.expandState == 0) {
                    item.parent.setExpanded(1, listItems, adapter, listItems.indexOf(item.parent));
                    adapter.notifyItemChanged(listItems.indexOf(item.parent));
                } else {
                    int index;
                    if (item.child_number == 0) {
                        index = listItems.indexOf(item.parent) + 1;
                    } else {
                        if (item.child_number < item.parent.children.size() - 1) {
                            index = listItems.indexOf(item.parent.children.get(item.child_number + 1));
                        } else {
                            index = listItems.indexOf(item.parent.children.get(item.child_number - 1));
                            while (index < listItems.size() && listItems.get(index).treeLevel >= item.treeLevel) {
                                index++;
                            }
                        }
                    }
                    adapter.add(index, item);
                }
            }

            if (recyclerView.getVisibility() == View.GONE) {
                recyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    private static final int ITEM_ACTION_RESULT = 1;

    private void launchItemActionActivity(OrgItem item) {
        Intent i = ItemAction.newInstance(this, item, org);
        startActivityForResult(i, ITEM_ACTION_RESULT);
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
