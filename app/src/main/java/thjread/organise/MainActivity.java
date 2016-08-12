package thjread.organise;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
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
import android.widget.LinearLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
            SyncFilesCallback,
            SwipeRefreshLayout.OnRefreshListener,
            AddTaskCallbackInterface {

    private ArrayList<OrgItem> scheduledToday;
    private ArrayList<OrgItem> deadlineSoon;

    private ArrayList<Pair<OrgItem, Pair<View, ViewGroup>>> views;

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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Fragment f = new AddTask();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(f, null);
                ft.commit();
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

        Dropbox.init(this, swipeRefresh, this);
        is_syncing = true;
    }

    public void onRefresh() {
        Dropbox.syncFiles(this);
        is_syncing = true;
    }

    public void populateViews(OrgFiles files) {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        menu.clear();
        for (int i=0; i<files.getFiles().size(); ++i) {
            final Org doc = files.getFiles().get(i);
            menu.add(doc.title).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    GlobalState.setCurrentOrg(doc);
                    if (doc.rootItems.size() > 1) {
                        launchDocumentActivity(null, doc.rootItems.get(1));//Avoid colour snap
                    } else {
                        launchDocumentActivity(null, doc.rootItems.get(0));
                    }
                    return true;
                };
            });
        }

        final LinearLayout scheduledContainer = (LinearLayout) findViewById(R.id.scheduledtoday);
        final LinearLayout deadlineContainer = (LinearLayout) findViewById(R.id.deadlinesoon);
        scheduledContainer.removeViewsInLayout(1, scheduledContainer.getChildCount()-1);
        scheduledToday = new ArrayList<>();
        deadlineContainer.removeViewsInLayout(1, deadlineContainer.getChildCount()-1);
        deadlineSoon = new ArrayList<>();

        for (int j=0; j<files.getFiles().size(); ++j) {
            Org org = files.getFiles().get(j);
            for (int i = 0; i < org.items.size(); ++i) {
                OrgItem item = org.items.get(i);
                if (item.deadline != null &&
                        item.keywords.keywordType(item.keyword) != Org.Keyword.DONE_KEYWORD_TYPE) {
                    if (DateFormatter.days(item.deadline) < 10) {
                        deadlineSoon.add(item);
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
                    }
                }
            }
        }

        class ScheduledComparator implements Comparator<OrgItem> {
            @Override
            public int compare(OrgItem o1, OrgItem o2) {
                return o1.scheduled.compareTo(o2.scheduled);
            }
        }
        Collections.sort(scheduledToday, new ScheduledComparator());

        views = new ArrayList<>();
        for (int i=0; i<scheduledToday.size(); ++i) {
            final OrgItem item = scheduledToday.get(i);
            final View itemView = ItemView.getView(item, null,
                    scheduledContainer, false, false, false);
            views.add(new Pair<>(item, new Pair<>(itemView, (ViewGroup) scheduledContainer)));
            scheduledContainer.addView(itemView);

            itemView.setOnTouchListener(new OnSwipeTouchListener(itemView.getContext()) {
                public void onSwipeRight() {
                    item.nextKeyword();
                    item.expandState = 0;
                    ItemView.getView(item, itemView, scheduledContainer, false, false, false);
                }

                public void onTap() {
                    launchDocumentActivity(itemView, item);
                }
            });
        }

        class DeadlineComparator implements Comparator<OrgItem> {
            @Override
            public int compare(OrgItem o1, OrgItem o2) {
                return o1.deadline.compareTo(o2.deadline);
            }
        }
        Collections.sort(deadlineSoon, new DeadlineComparator());
        for (int i=0; i<deadlineSoon.size(); ++i) {
            final OrgItem item = deadlineSoon.get(i);
            final View itemView = ItemView.getView(item, null,
                    deadlineContainer, false, false, false);
            views.add(new Pair<>(item, new Pair<>(itemView, (ViewGroup) deadlineContainer)));
            deadlineContainer.addView(itemView);
            itemView.setOnTouchListener(new OnSwipeTouchListener(itemView.getContext()) {
                public void onSwipeRight() {
                    item.nextKeyword();
                    item.expandState = 0;
                    ItemView.getView(item, itemView, deadlineContainer, false, false, false);
                }

                public void onTap() {
                    launchDocumentActivity(itemView, item);
                }
            });
        }

        scheduledContainer.requestLayout();
        deadlineContainer.requestLayout();
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
            Pair<OrgItem, Pair<View, ViewGroup>> it = views.get(i);
            OrgItem item = it.t;
            View itemView = it.u.t;
            ViewGroup container = it.u.u;
            item.expandState = 0;
            ItemView.getView(item, itemView, container, false, false, false);
        }
    }

    public void onItemChange(OrgItem item, boolean isEdit, boolean isDelete) {
        refreshViews();
    }

    private void launchDocumentActivity(View v, OrgItem item) {
        if (is_syncing) {
            return;
        }
        GlobalState.setCurrentOrg(item.document);
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
        b.putInt("id", item.id);
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

        Dropbox.resumeAuth(this);

        refreshViews();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
