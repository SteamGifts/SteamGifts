package net.mabako.steamgifts.fragments;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.activities.BaseActivity;
import net.mabako.steamgifts.activities.MainActivity;
import net.mabako.steamgifts.adapters.EndlessAdapter;
import net.mabako.steamgifts.adapters.GiveawayAdapter;
import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.tasks.LoadGiveawayListTask;

/**
 * List of all giveaways.
 */
public class GiveawayListFragment extends ListFragment<GiveawayAdapter> implements IGiveawayUpdateNotification {
    private static final String TAG = GiveawayListFragment.class.getSimpleName();

    /**
     * Type of items to show.
     */
    private Type type = Type.ALL;

    /**
     * What are we searching for?
     */
    private String searchQuery = null;

    public static GiveawayListFragment newInstance(Type type, String query) {
        GiveawayListFragment g = new GiveawayListFragment();
        g.type = type;
        g.searchQuery = query;
        return g;
    }

    @Override
    protected GiveawayAdapter createAdapter(RecyclerView listView) {
        return new GiveawayAdapter(getActivity(), listView, new EndlessAdapter.OnLoadListener() {
            @Override
            public void onLoad(int page) {
                fetchItems(page);
            }
        });
    }

    @Override
    protected void fetchItems(int page) {
        Log.d(TAG, "Fetching giveaways on page " + page);
        new LoadGiveawayListTask(this, page, type, searchQuery).execute();
    }

    /**
     * Callback for a giveaway's status being updated.
     *
     * @param giveawayId ID of the giveaway
     * @param entered    whether or not the giveaway is now entered.
     */
    @Override
    public void onUpdateGiveawayStatus(String giveawayId, boolean entered) {
        Giveaway giveaway = adapter.findItem(giveawayId);
        if (giveaway != null) {
            giveaway.setEntered(entered);
            adapter.notifyItemChanged(giveaway);
        }
    }

    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);

        final MenuItem searchMenu = menu.findItem(R.id.search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenu);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                query = query.trim();
                searchView.setQuery("", false);
                searchMenu.collapseActionView();

                Intent intent = new Intent(getActivity(), MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(MainActivity.ARGS_GIVEAWAY_QUERY, query);
                intent.putExtras(bundle);

                getActivity().startActivityForResult(intent, BaseActivity.REQUEST_LOGIN_PASSIVE);
                if (searchQuery != null && !searchQuery.isEmpty())
                    getActivity().finish();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
    }

    /**
     * Returns the resource of what to show in the title.
     *
     * @return resource to show in the title
     */
    @Override
    public int getTitleResource() {
        return type.getTitleResource();
    }

    /**
     * Return extra content for a title.
     *
     * @return extra content (dynamic)
     */
    @Override
    public String getExtraTitle() {
        return searchQuery;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_giveaway_list;
    }

    /**
     * Different types of Giveaway lists.
     */
    public enum Type {
        /**
         * All giveaways.
         */
        ALL(R.string.navigation_giveaways_all, R.string.navigation_giveaways_all_title),

        /**
         * Group giveaways.
         */
        GROUP(R.string.navigation_giveaways_group, R.string.navigation_giveaways_group_title),

        /**
         * Giveaways with games from your wishlist.
         */
        WISHLIST(R.string.navigation_giveaways_wishlist, R.string.navigation_giveaways_wishlist_title),

        /**
         * New giveaways.
         */
        NEW(R.string.navigation_giveaways_new, R.string.navigation_giveaways_new_title);

        private final int titleResource;
        private final int navbarResource;

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
