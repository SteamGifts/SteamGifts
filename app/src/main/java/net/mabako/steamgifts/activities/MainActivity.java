package net.mabako.steamgifts.activities;

import android.os.PersistableBundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.adapters.GiveawayAdapter;
import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.tasks.LoadGiveawaysFromUrlTask;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private SwipeRefreshLayout swipeContainer;
    private ListView listView;

    private GiveawayAdapter adapter;
    private ArrayList<Giveaway> giveaways;
    private Drawer drawer;

    public enum Type {
        ALL, NEW
    }
    private Type type = Type.ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        setupNavBar();
        setupListViewAdapter();
        setupSwipeContainer();

        fetchGiveaways(1);
    }

    private void setupListViewAdapter() {
        listView = (ListView) findViewById(R.id.list);

        giveaways = new ArrayList<>();
        adapter = new GiveawayAdapter(this, R.layout.item_listview, R.id.giveaway_name, giveaways);

        listView.setAdapter(adapter);
    }

    private void setupSwipeContainer() {
        // Swipe to Refresh
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                fetchGiveaways(1);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(R.color.colorPrimary);
    }

    private void setupNavBar() {
        AccountHeader header = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(
                        new ProfileDrawerItem().withName("kt").withEmail("Level 9 Wizard").withIcon(R.drawable.guy)
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean current) {
                        return false;
                    }
                })
                .build();

        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar((Toolbar) findViewById(R.id.toolbar))
                .withTranslucentStatusBar(false)
                .withActionBarDrawerToggle(true)
                .withAccountHeader(header)
                .addDrawerItems(
                        new SectionDrawerItem().withName(R.string.navigation_giveaways).withDivider(false),
                        new PrimaryDrawerItem().withName(R.string.navigation_giveaways_all).withSetSelected(true).withIdentifier(R.string.navigation_giveaways_all),
                        new PrimaryDrawerItem().withName(R.string.navigation_giveaways_new).withIdentifier(R.string.navigation_giveaways_new),

                        new SectionDrawerItem().withName(R.string.navigation_discussions)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        switch (drawerItem.getIdentifier()) {
                            case R.string.navigation_giveaways_all:
                                type = Type.ALL;
                                fetchGiveaways(1);
                                getSupportActionBar().setTitle(R.string.navigation_giveaways_all_title);
                                break;

                            case R.string.navigation_giveaways_new:
                                type = Type.NEW;
                                fetchGiveaways(1);
                                getSupportActionBar().setTitle(R.string.navigation_giveaways_new_title);
                                break;

                            default:
                                return false;
                        }

                        drawer.closeDrawer();
                        return true;
                    }
                })
                .build();
    }

    public void fetchGiveaways(int page) {
        Log.d(TAG, "Fetching giveaways on page " + page);
        new LoadGiveawaysFromUrlTask(this, page, type).execute();
    }

    public void addGiveaways(List<Giveaway> giveaways1, boolean clearExistingItems) {
        Log.d(TAG, "Adding " + giveaways1.size() + " giveaways, " + clearExistingItems);

        if(clearExistingItems)
            giveaways.clear();

        giveaways.addAll(giveaways1);
        adapter.notifyDataSetChanged();

        swipeContainer.setRefreshing(false);
    }
}
