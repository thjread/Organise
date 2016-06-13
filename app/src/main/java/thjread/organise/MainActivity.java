package thjread.organise;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.app.Fragment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.ChangeBounds;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Transition;
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
import android.view.Window;
import android.view.animation.BounceInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ArrayList<OrgItem> scheduledToday;
    private ArrayList<OrgItem> deadlineSoon;
    private ArrayAdapter<OrgItem> adapter;

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
                FragmentTransaction ft = getFragmentManager().beginTransaction();
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

        LinearLayout scheduledContainer = (LinearLayout) findViewById(R.id.scheduledtoday);
        LinearLayout deadlineContainer = (LinearLayout) findViewById(R.id.deadlinesoon);
        scheduledToday = new ArrayList<OrgItem>();
        deadlineSoon = new ArrayList<OrgItem>();

        OrgFiles files = GlobalState.getFiles();

        try {
            files.loadFiles(this);
            Org org = new Org(files.getFiles().get(0));
            GlobalState.setCurrentOrg(org);
            for (int i=0; i<org.items.size(); ++i) {
                OrgItem item = org.items.get(i);
                if (item.deadline != null && item.keyword != 2) {//TODO deal with keywords properly
                    if (DateFormatter.days(item.deadline) < 5) {
                        deadlineSoon.add(item);
                    }
                }
                if (item.scheduled != null && item.keyword != 2) {
                    if (DateFormatter.days(item.scheduled) <= 0) {
                        scheduledToday.add(item);
                    }
                }
            }
        } catch (IOException e) {

        }

        class ScheduledComparator implements Comparator<OrgItem> {
            @Override
            public int compare(OrgItem o1, OrgItem o2) {
                return o1.scheduled.compareTo(o2.scheduled);
            }
        }
        Collections.sort(scheduledToday, new ScheduledComparator());

        for (int i=0; i<scheduledToday.size(); ++i) {
            final OrgItem item = scheduledToday.get(i);
            View itemView = ItemView.getView(item, null,
                    scheduledContainer, false, false);
            scheduledContainer.addView(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchDocumentActivity(v, item);
                }
            }
            );
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
            View itemView = ItemView.getView(item, null,
                    deadlineContainer, false, false);
            deadlineContainer.addView(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchDocumentActivity(v, item);
                }
            }
            );
        }
    }

    private void launchDocumentActivity(View v, OrgItem item) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            v.findViewById(R.id.item_cardview)
                .setTransitionName(getString(R.string.item_transition));
        }
        Intent i = new Intent(v.getContext(), DocumentActivity.class);
        Bundle b;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                    v, getString(R.string.item_transition));
            b = options.toBundle();
        } else {
            b = new Bundle();
        }
        b.putInt("id", item.id);
        i.putExtras(b);
        ActivityCompat.startActivity(this, i, b);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        /*if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
