package net.mabako.steamgifts.activities;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.adapters.GiveawayAdapter;
import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.tasks.LoadGiveawaysFromUrlTask;

import java.util.ArrayList;
import java.util.List;

public class GiveawaysActivity extends BaseActivity {
    private static final String TAG = net.mabako.steamgifts.activities.GiveawaysActivity.class.getSimpleName();

    private SwipeRefreshLayout swipeContainer;
    private ListView listView;

    private GiveawayAdapter adapter;
    private ArrayList<Giveaway> giveaways;
    private Type type = Type.ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_giveaways);

        type = (Type) getIntent().getSerializableExtra("type");

        setupListViewAdapter();
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        setupNavBar();
        setupSwipeContainer();

        fetchItems(1);
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
                fetchItems(1);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(R.color.colorPrimary);
    }

    @Override
    protected void setupNavBar() {
        super.setupNavBar();

        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null)
            actionbar.setTitle(type.getTitleResource());
    }

    @Override
    public void onAccountChange() {
        super.onAccountChange();
        fetchItems(1);
    }

    @Override
    public void fetchItems(int page) {
        Log.d(TAG, "Fetching giveaways on page " + page);
        new LoadGiveawaysFromUrlTask(this, page, type).execute();
    }

    public void addGiveaways(List<Giveaway> giveaways1, boolean clearExistingItems) {
        if (giveaways1 != null) {
            Log.d(TAG, "Adding " + giveaways1.size() + " giveaways, " + clearExistingItems);

            if (clearExistingItems)
                giveaways.clear();

            giveaways.addAll(giveaways1);
            adapter.notifyDataSetChanged();
        } else {
            Snackbar.make(findViewById(R.id.swipeContainer), "Failed to fetch giveaways", Snackbar.LENGTH_LONG).show();
        }


        findViewById(R.id.progressBar).setVisibility(View.GONE);
        swipeContainer.setVisibility(View.VISIBLE);
        swipeContainer.setRefreshing(false);
    }

    public enum Type {
        ALL(R.string.navigation_giveaways_all, R.string.navigation_giveaways_all_title),
        GROUP(R.string.navigation_giveaways_group, R.string.navigation_giveaways_group_title),
        WISHLIST(R.string.navigation_giveaways_wishlist, R.string.navigation_giveaways_wishlist_title),
        NEW(R.string.navigation_giveaways_new, R.string.navigation_giveaways_new_title);

        private int titleResource;
        private int navbarResource;

        Type(int navbarResource, int titleResource) {
            this.navbarResource = navbarResource;
            this.titleResource = titleResource;
        }

        public static Type find(int identifier) {
            for (Type t : values())
                if (identifier == t.getNavbarResource())
                    return t;

            throw new IllegalStateException();
        }

        public int getTitleResource() {
            return titleResource;
        }

        public int getNavbarResource() {
            return navbarResource;
        }
    }
}
