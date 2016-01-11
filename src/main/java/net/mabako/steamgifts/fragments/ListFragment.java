package net.mabako.steamgifts.fragments;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.activities.CommonActivity;
import net.mabako.steamgifts.activities.MainActivity;
import net.mabako.steamgifts.adapters.EndlessAdapter;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class ListFragment<AdapterType extends EndlessAdapter> extends Fragment {
    protected boolean loadItemsInitially = true;
    protected boolean allowSearch = true;

    protected AdapterType adapter;
    private RecyclerView listView;

    private SwipeRefreshLayout swipeContainer;
    private ProgressBar progressBar;

    /**
     * What are we searching for?
     */
    protected String searchQuery = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout layout = (RelativeLayout) inflater.inflate(getLayoutResource(), container, false);

        listView = (RecyclerView) layout.findViewById(R.id.list);
        swipeContainer = (SwipeRefreshLayout) layout.findViewById(R.id.swipeContainer);
        progressBar = (ProgressBar) layout.findViewById(R.id.progressBar);

        setupListViewAdapter();
        setupSwipeContainer();

        setHasOptionsMenu(true);
        if (loadItemsInitially)
            fetchItems(1);

        return layout;

    }

    private void setupListViewAdapter() {
        listView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = createAdapter(listView);
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

    protected void showSnack(String message, int length) {
        if (getView() == null)
            Log.e(ListFragment.class.getSimpleName(), "List not loaded yet...");

        try {
            Snackbar.make(swipeContainer != null ? swipeContainer : getView(), message, length).show();
        } catch (NullPointerException e) {
            Log.w(ListFragment.class.getSimpleName(), "Could not show snack for " + message);
        }
    }


    public void addItems(List<? extends IEndlessAdaptable> items, boolean clearExistingItems) {
        if (items != null) {
            if (clearExistingItems)
                adapter.clear();

            adapter.finishLoading(new ArrayList<>(items));
        } else {
            showSnack("Failed to fetch items", Snackbar.LENGTH_LONG);
        }

        progressBar.setVisibility(View.GONE);
        swipeContainer.setVisibility(View.VISIBLE);
        swipeContainer.setRefreshing(false);
    }


    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        if (!allowSearch)
            return;

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
                MenuItemCompat.collapseActionView(searchMenu);

                startActivityForQuery(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
    }

    protected void startActivityForQuery(String query) {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.ARG_QUERY, query);
        bundle.putSerializable(MainActivity.ARG_TYPE, getType());
        intent.putExtras(bundle);

        getActivity().startActivityForResult(intent, CommonActivity.REQUEST_LOGIN_PASSIVE);
        if (searchQuery != null && !searchQuery.isEmpty())
            getActivity().finish();
    }

    /**
     * Load a tab's items only if the user is on that tab.
     *
     * @param isVisibleToUser
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && !loadItemsInitially) {
            loadItemsInitially = true;
            fetchItems(1);
        }
    }

    protected abstract AdapterType createAdapter(RecyclerView listView);

    /**
     * Load all items from a particular page.
     *
     * @param page page to load items from
     */
    protected abstract void fetchItems(int page);

    protected int getLayoutResource() {
        return R.layout.fragment_list;
    }

    protected abstract Serializable getType();
}
