package net.mabako.steamgifts.fragments;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.activities.CommonActivity;
import net.mabako.steamgifts.adapters.EndlessAdapter;

/**
 * Searchable Discussion or Giveaway List fragment.
 */
public abstract class SearchableListFragment<AdapterType extends EndlessAdapter> extends ListFragment<AdapterType> {
    private static final String TAG = SearchableListFragment.class.getSimpleName();

    /**
     * What are we searching for?
     */
    protected String searchQuery = null;

    protected boolean finishActivityOnSearchStopped = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.main_menu, menu);

        final MenuItem searchMenu = menu.findItem(R.id.search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenu);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                query = query.trim();

                startSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });

        // Stop searching on clicking 'back'
        MenuItemCompat.setOnActionExpandListener(searchMenu, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                stopSearch();
                return true;
            }
        });

        if (searchQuery != null) {
            MenuItemCompat.expandActionView(searchMenu);
            searchView.setQuery(searchQuery, false);
            searchView.clearFocus();
        }
    }

    protected void startSearch(String query) {
        Log.d(TAG, "Starting Search for " + query);

        AppCompatActivity activity = ((AppCompatActivity) getActivity());
        FragmentManager fragmentManager = activity.getSupportFragmentManager();

        if (searchQuery != null)
            stopSearch();

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, newSearchingInstance(query), CommonActivity.FRAGMENT_TAG);
        transaction.addToBackStack(TAG);
        transaction.commit();
    }

    public void stopSearch() {
        if (searchQuery != null) {
            Log.d(TAG, "Stopping Search");

            if (finishActivityOnSearchStopped) {
                getActivity().finish();
            } else {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.popBackStack(TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        }
    }

    /**
     * Creates a new instance of the fragment used exclusively for searching items.
     *
     * @param query
     * @return
     */
    public abstract Fragment newSearchingInstance(String query);
}
