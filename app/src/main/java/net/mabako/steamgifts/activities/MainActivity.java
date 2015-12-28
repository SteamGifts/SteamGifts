package net.mabako.steamgifts.activities;

import android.os.PersistableBundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.adapters.GiveawayAdapter;
import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.tasks.LoadGiveawaysFromUrlTask;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private SwipeRefreshLayout swipeContainer;
    private ListView listView;
    private static final String TAG = MainActivity.class.getSimpleName();

    private GiveawayAdapter adapter;
    private ArrayList<Giveaway> giveaways;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                // Your code to refresh the list here.
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
                        new ProfileDrawerItem().withName("kt").withIcon(R.drawable.guy)
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean current) {
                        return false;
                    }
                })
                .build();

        Drawer result = new DrawerBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(false)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
                .withAccountHeader(header)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.navigation_giveaways),
                        new PrimaryDrawerItem().withName(R.string.navigation_discussions)
                )
                .build();
    }

    public void fetchGiveaways(int page) {
        Log.d(TAG, "Fetching giveaways on page " + page);
        new LoadGiveawaysFromUrlTask(this, page).execute();
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
