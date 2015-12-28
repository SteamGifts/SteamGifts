package net.mabako.steamgifts.fragments;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.adapters.GiveawayAdapter;
import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.tasks.LoadGiveawaysFromUrlTask;

import java.util.ArrayList;
import java.util.List;

public class GiveawaysFragment extends Fragment implements IFragmentNotifications {
    private static final String TAG = GiveawaysFragment.class.getSimpleName();

    private SwipeRefreshLayout swipeContainer;
    private ProgressBar progressBar;
    private ListView listView;

    private GiveawayAdapter adapter;
    private ArrayList<Giveaway> giveaways;
    private Type type = Type.ALL;

    public static GiveawaysFragment newInstance(Type type)
    {
        GiveawaysFragment g = new GiveawaysFragment();
        g.type = type;
        return g;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.fragment_giveaways_list, container, false);

        listView = (ListView) layout.findViewById(R.id.list);
        swipeContainer = (SwipeRefreshLayout) layout.findViewById(R.id.swipeContainer);
        progressBar = (ProgressBar) layout.findViewById(R.id.progressBar);

        setupListViewAdapter();
        setupSwipeContainer();

        fetchItems(1);

        return layout;
    }

    private void setupListViewAdapter() {
        giveaways = new ArrayList<>();
        adapter = new GiveawayAdapter(getActivity(), R.layout.item_listview, R.id.giveaway_name, giveaways);

        listView.setAdapter(adapter);
    }

    private void setupSwipeContainer() {
        // Swipe to Refresh

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
    public int getTitleResource() {
        return type.getTitleResource();
    }

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
            Snackbar.make(swipeContainer, "Failed to fetch giveaways", Snackbar.LENGTH_LONG).show();
        }


        progressBar.setVisibility(View.GONE);
        swipeContainer.setVisibility(View.VISIBLE);
        swipeContainer.setRefreshing(false);
    }

    @Override
    public void onAccountChange() {
        fetchItems(1);
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
