package net.mabako.steamgifts.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.adapters.EndlessAdapter;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class ListFragment<AdapterType extends EndlessAdapter> extends Fragment {
    protected boolean loadItemsInitially = true;

    protected AdapterType adapter;
    private RecyclerView listView;

    private SwipeRefreshLayout swipeContainer;
    private ProgressBar progressBar;

    private AsyncTask<Void, Void, ?> taskToFetchItems = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout layout = (RelativeLayout) inflater.inflate(getLayoutResource(), container, false);

        listView = (RecyclerView) layout.findViewById(R.id.list);
        swipeContainer = (SwipeRefreshLayout) layout.findViewById(R.id.swipeContainer);
        progressBar = (ProgressBar) layout.findViewById(R.id.progressBar);

        setupListViewAdapter();
        setupSwipeContainer();

        if (loadItemsInitially)
            fetchItems(1);

        return layout;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cancelFetch();
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

        taskToFetchItems = null;
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
    protected final void fetchItems(int page) {
        if (taskToFetchItems != null)
            taskToFetchItems.cancel(true);

        taskToFetchItems = getFetchItemsTask(page);
        taskToFetchItems.execute();
    }

    protected final void cancelFetch() {
        if (taskToFetchItems != null)
            taskToFetchItems.cancel(true);

        taskToFetchItems = null;
        adapter.cancelLoading();
        swipeContainer.setRefreshing(false);
    }

    protected abstract AsyncTask<Void, Void, ?> getFetchItemsTask(int page);

    protected int getLayoutResource() {
        return R.layout.fragment_list;
    }

    protected abstract Serializable getType();
}
