package net.mabako.steamgifts.fragments;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.adapters.EndlessAdapter;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;

import java.util.ArrayList;
import java.util.List;

public abstract class ListFragment<AdapterType extends EndlessAdapter> extends Fragment implements IFragmentNotifications {
    private static final String TAG = ListFragment.class.getSimpleName();

    protected AdapterType adapter;
    private RecyclerView listView;

    private SwipeRefreshLayout swipeContainer;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout layout = (RelativeLayout) inflater.inflate(getLayoutResource(), container, false);

        listView = (RecyclerView) layout.findViewById(R.id.list);
        swipeContainer = (SwipeRefreshLayout) layout.findViewById(R.id.swipeContainer);
        progressBar = (ProgressBar) layout.findViewById(R.id.progressBar);

        setupListViewAdapter();
        setupSwipeContainer();

        setHasOptionsMenu(true);
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
        Snackbar.make(swipeContainer, message, length).show();
    }


    public void addGiveaways(List<? extends IEndlessAdaptable> items, boolean clearExistingItems) {
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

    protected abstract AdapterType createAdapter(RecyclerView listView);

    /**
     * Load all items from a particular page.
     *
     * @param page page to load items from
     */
    protected abstract void fetchItems(int page);

    protected abstract int getLayoutResource();
}
