package thjread.organise;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
            SyncFilesCallback,
            SwipeRefreshLayout.OnRefreshListener {

    private ArrayList<OrgItem> scheduledToday;
    private ArrayList<OrgItem> deadlineSoon;
    private ArrayList<OrgItem> todos;

    private ArrayList<Pair<OrgItem, ItemViewHolder>> views;

    private SwipeRefreshLayout swipeRefresh;
    private boolean is_syncing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            //getWindow().setExitTransition(new Explode());
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Upcoming");
        setSupportActionBar(toolbar);

        final Activity activity = this;
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (is_syncing) return;
                Bundle b;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptionsCompat options =
                            ActivityOptionsCompat.makeSceneTransitionAnimation(activity, fab, getString(R.string.add_task_transition));
                    b = options.toBundle();
                } else {
                    b = new Bundle();
                }
                Intent i = AddTask.newInstance(activity, null, null, null, false, true);
                i.putExtras(b);
                ActivityCompat.startActivity(activity, i, b);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeColors(getResources().getColor(R.color.hierarchy1)
                , getResources().getColor(R.color.hierarchy2)
                , getResources().getColor(R.color.hierarchy3)
                , getResources().getColor(R.color.hierarchy4)
                , getResources().getColor(R.color.hierarchy5));
        swipeRefresh.setOnRefreshListener(this);

        OrgFiles files = GlobalState.getFiles();
        try {
            files.loadFiles(this);
            populateViews(files);
        } catch (IOException e) {
            Log.d("thjread.organise", e.toString());
        }

        Dropbox.init(this);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    public void onRefresh() {
        Dropbox.syncFiles(this);
        is_syncing = true;
    }

    public void populateViews(OrgFiles files) {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        menu.removeGroup(R.id.document_group);
        for (int i=0; i<files.getFiles().size(); ++i) {
            final Org doc = files.getFiles().get(i);
            if (!doc.file.deleted) {
                menu.add(R.id.document_group, Menu.NONE, Menu.NONE, doc.title)
                        .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        GlobalState.setCurrentOrg(doc);
                        launchDocumentActivity(null, null, doc);
                        return true;
                    }
                });
            }
        }

        final LinearLayout scheduledContainer = (LinearLayout) findViewById(R.id.scheduledtoday);
        final LinearLayout deadlineContainer = (LinearLayout) findViewById(R.id.deadlinesoon);
        final LinearLayout todoContainer = (LinearLayout) findViewById(R.id.alltodos);
        scheduledContainer.removeViewsInLayout(1, scheduledContainer.getChildCount()-1);
        scheduledToday = new ArrayList<>();
        deadlineContainer.removeViewsInLayout(1, deadlineContainer.getChildCount()-1);
        deadlineSoon = new ArrayList<>();
        todoContainer.removeViewsInLayout(1, todoContainer.getChildCount()-1);
        todos = new ArrayList<>();

        for (int j=0; j<files.getFiles().size(); ++j) {
            Org org = files.getFiles().get(j);
            if (org.file.deleted) continue;

            for (int i = 0; i < org.items.size(); ++i) {
                boolean added = false;
                OrgItem item = org.items.get(i);
                item.expandState = 0;
                if (item.keywords.keywordType(item.keyword) != Org.Keyword.DONE_KEYWORD_TYPE) {
                    if (item.deadline != null && DateFormatter.days(item.deadline) < 10) {
                        deadlineSoon.add(item);
                        added = true;
                    }
                }
                if (item.scheduled != null) {
                    int days = DateFormatter.days(item.scheduled);
                    if (days == 0 ||
                            ((item.keywords.keywordType(item.keyword) != Org.Keyword.DONE_KEYWORD_TYPE
                                    && days <= 0)
                            || (item.closed != null
                                    && item.keywords.keywordType(item.keyword) == Org.Keyword.DONE_KEYWORD_TYPE
                                    && DateFormatter.days(item.closed) == 0))) {
                        scheduledToday.add(item);
                        added = true;
                    }
                }
                if (!added && item.keywords.keywordType(item.keyword) != Org.Keyword.DONE_KEYWORD_TYPE) {
                    if (item.deadline != null || item.scheduled != null) {
                        todos.add(item);
                    }
                }
            }
        }

        views = new ArrayList<>();

        class ScheduledComparator implements Comparator<OrgItem> {
            @Override
            public int compare(OrgItem o1, OrgItem o2) {
                return o1.scheduled.compareTo(o2.scheduled);
            }
        }
        populateItems(new ScheduledComparator(), scheduledContainer, scheduledToday);

        class DeadlineComparator implements Comparator<OrgItem> {
            @Override
            public int compare(OrgItem o1, OrgItem o2) {
                return o1.deadline.compareTo(o2.deadline);
            }
        }
        populateItems(new DeadlineComparator(), deadlineContainer, deadlineSoon);

        class TodoComparator implements Comparator<OrgItem> {
            @Override
            public int compare(OrgItem o1, OrgItem o2) {
                Date o1Date = o1.scheduled;
                Date o2Date = o2.scheduled;
                if (o1Date == null ||
                        (o1.deadline != null && o1.deadline.before(o1Date))) {
                    o1Date = o1.deadline;
                }
                if (o2Date == null ||
                        (o2.deadline != null && o2.deadline.before(o2Date))) {
                    o2Date = o2.deadline;
                }
                if (o1Date != null && o2Date != null) {
                    return o1Date.compareTo(o2Date);
                } else {
                    if (o1Date != null) {
                        return -1;
                    } else if (o2Date != null){
                        return 1;
                    } else {
                        return 0;
                    }
                }
            }
        }
        populateItems(new TodoComparator(), todoContainer, todos);
    }

    public void populateItems(Comparator<OrgItem> comparator, final LinearLayout container, ArrayList<OrgItem> items) {
        Collections.sort(items, comparator);
        for (int i=0; i<items.size(); ++i) {
            final OrgItem item = items.get(i);
            ItemViewHolder viewHolder = ItemView.getView(item, container);
            final View itemView = viewHolder.itemView;
            views.add(new Pair<>(item, viewHolder));
            container.addView(itemView);
            itemView.setOnTouchListener(new OnSwipeTouchListener(itemView.getContext(), itemView, false) {
                public void onSwipeRight() {
                    item.nextKeyword();
                    item.expandState = 0;
                    refreshViews();
                }

                public void onTap() {
                    launchDocumentActivity(itemView, item, item.document);
                }
            });
        }

        container.requestLayout();
    }

    public void syncFilesCallback(final OrgFiles files) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("thjread.organise", "Dropbox synced");
                populateViews(files);
                swipeRefresh.setRefreshing(false);
                is_syncing = false;
            }
        });
    }

    private void refreshViews() {
        if (views == null) {
            return;
        }

        for (int i=0; i<views.size(); ++i) {
            Pair<OrgItem, ItemViewHolder> it = views.get(i);
            OrgItem item = it.t;
            ItemViewHolder viewHolder = it.u;
            item.expandState = 0;
            ItemView.bindViewHolder(item, viewHolder, false, false, false);
        }
    }

    private void launchDocumentActivity(View v, OrgItem item, Org document) {
        if (is_syncing) {
            return;
        }
        GlobalState.setCurrentOrg(document);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && v != null) {
            v.findViewById(R.id.item_cardview)
                    .setTransitionName(getString(R.string.item_transition));
        }
        Intent i = new Intent(this, DocumentActivity.class);
        Bundle b;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && v != null) {
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                    v, getString(R.string.item_transition));
            b = options.toBundle();
        } else {
            b = new Bundle();
        }
        if (item != null) {
            b.putInt("id", item.id);
        }
        i.putExtras(b);
        ActivityCompat.startActivity(this, i, b);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Dropbox.resumeAuth();

        populateViews(GlobalState.getFiles());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void launchSettings() {
        Intent i = new Intent(this, SettingsActivity.class);
        ActivityCompat.startActivity(this, i, null);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }
    public void launchSettings(MenuItem item) { launchSettings(); }

    public void newDocument(MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.new_doc_layout, null);
        final EditText new_doc_name_text = (EditText) view.findViewById(R.id.new_document_name);
        final Context context = this;
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle)
                .setTitle("New document")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        try {
                            Org doc = new Org(new OrgFile(new_doc_name_text.getText().toString(), context));
                            OrgItem orgItem = new OrgItem(doc.keyword, null, null, 0, doc);
                            orgItem.title = "Add new items here";
                            doc.addItem(null, orgItem, null);
                            GlobalState.getFiles().addDocument(doc);
                            launchDocumentActivity(null, null, doc);
                        } catch (IOException e) {
                            Log.d("thjread.organise", e.toString());
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setView(view)
                .create();
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            launchSettings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
