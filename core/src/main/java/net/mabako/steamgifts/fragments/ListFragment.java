package net.mabako.steamgifts.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import net.mabako.steamgifts.adapters.EndlessAdapter;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.fragments.interfaces.ILoadItemsListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// TODO make EndlessAdapter's viewInReverse more easily handled within here.
public abstract class ListFragment<AdapterType extends EndlessAdapter> extends Fragment implements EndlessAdapter.OnLoadListener, ILoadItemsListener {
    private static final String TAG = ListFragment.class.getSimpleName();

    private static final String SAVED_ADAPTER = "listadapter";

    private boolean loadItemsInitially = true;

    protected AdapterType adapter;
    private RecyclerView listView;

    private View rootView;
    protected SwipeRefreshLayout swipeContainer;
    private ProgressBar progressBar;

    @Nullable
    private FloatingActionButton scrollToTopButton;

    private AsyncTask<Void, Void, ?> taskToFetchItems = null;

    @Override
    @SuppressWarnings("unchecked")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            adapter = createAdapter();
        } else {
            adapter = (AdapterType) savedInstanceState.getSerializable(SAVED_ADAPTER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(getLayoutResource(), container, false);
        rootView = container.getRootView();
        loadItemsInitially = isCurrentFragmentTheActiveFragment();

        listView = (RecyclerView) layout.findViewById(R.id.list);
        swipeContainer = (SwipeRefreshLayout) layout.findViewById(R.id.swipeContainer);
        progressBar = (ProgressBar) layout.findViewById(R.id.progressBar);
        scrollToTopButton = (FloatingActionButton) container.getRootView().findViewById(R.id.scroll_to_top_button);

        setupListViewAdapter();
        setupSwipeContainer();
        setupScrollToTopButton();

        if (loadItemsInitially) {
            initializeListView();
        }

        return layout;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cancelFetch();
    }

    private void setupListViewAdapter() {
        listView.setLayoutManager(new LinearLayoutManager(getContext()));
        listView.addOnScrollListener(adapter.getScrollListener());
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
                if (adapter.isViewInReverse()) {
                    // We'd basically want to clear out the whole view, since view in reverse is a bit different to handle
                    // TODO can we just call fetchItems(EndlessAdapter.LAST_PAGE)?
                    refresh();
                } else {
                    fetchItems(EndlessAdapter.FIRST_PAGE);
                }
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(R.color.colorPrimary);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SAVED_ADAPTER, adapter);
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

        showNormalListView();

        taskToFetchItems = null;
    }


    public void addItems(List<? extends IEndlessAdaptable> items, boolean clearExistingItems, String xsrfToken) {
        addItems(items, clearExistingItems);

        if (xsrfToken != null)
            adapter.setXsrfToken(xsrfToken);
    }

    private void showNormalListView() {
        progressBar.setVisibility(View.GONE);
        swipeContainer.setVisibility(View.VISIBLE);
        swipeContainer.setRefreshing(false);
    }

    protected void refresh() {
        adapter.cancelLoading();
        adapter.clear();
        progressBar.setVisibility(View.VISIBLE);
        swipeContainer.setVisibility(View.GONE);
        swipeContainer.setRefreshing(false);

        if (scrollToTopButton != null)
            scrollToTopButton.setVisibility(View.GONE);

        // TODO reverse pages?
        fetchItems(adapter.isViewInReverse() ? EndlessAdapter.LAST_PAGE : EndlessAdapter.FIRST_PAGE);
    }

    /**
     * Load a tab's items only if the user is on that tab.
     *
     * @param isVisibleToUser
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            setupScrollToTopButton();

            if (!loadItemsInitially) {
                loadItemsInitially = true;

                if (getView() == null) {
                    // Fallback for a rare circumstance in which setUserVisibleHint would be called before onCreateView,
                    // in which case getView() returns null.
                } else
                    initializeListView();
            }
        }
    }

    protected void initializeListView() {
        if (adapter.isEmpty()) {
            fetchItems(1);
        } else {
            showNormalListView();
        }
    }

    @NonNull
    protected abstract AdapterType createAdapter();

    /**
     * Load all items from a particular page.
     *
     * @param page page to load items from
     */
    protected void fetchItems(int page) {
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

    public void setupScrollToTopButton() {
        if (scrollToTopButton != null) {
            if (isCurrentFragmentTheActiveFragment()) {
                scrollToTopButton.hide();
                scrollToTopButton.setTag("clickable");
                scrollToTopButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listView != null) {
                            ((LinearLayoutManager) listView.getLayoutManager()).scrollToPositionWithOffset(0, 0);
                            scrollToTopButton.hide();
                        }
                    }
                });
            } else {
                Log.v(TAG, "setupScrollToTopButton: not the current activity");
            }
        } else {
            Log.v(TAG, "setupScrollToTopButton: no FAB");
        }
    }

    @Override
    public final void onLoad(int page) {
        fetchItems(page);
    }

    public AdapterType getAdapter() {
        return adapter;
    }

    protected abstract AsyncTask<Void, Void, ?> getFetchItemsTask(int page);

    protected int getLayoutResource() {
        return R.layout.fragment_list;
    }

    protected abstract Serializable getType();

    public boolean isCurrentFragmentTheActiveFragment() {
        if (rootView == null)
            return false;

        ViewPager viewPager = (ViewPager) rootView.findViewById(R.id.viewPager);
        if (viewPager != null) {
            PagerAdapter adapter = viewPager.getAdapter();
            if (adapter instanceof FragmentAdapter) {
                Fragment currentItem = ((FragmentAdapter) adapter).getItem(viewPager.getCurrentItem());
                if (!(currentItem instanceof ListFragment))
                    return false;

                ListFragment listFragment = (ListFragment) currentItem;
                /// TODO separate two instances of the same fragment
                return listFragment.getClass() == getClass() && BundleEquality.equalBundles(listFragment.getArguments(), getArguments());
            }

            Log.w(TAG, getClass().getSimpleName() + " does not have a FragmentAdapter!");
            return true;
        } else {
            // not a paged view, so there's no real way for this not to be active.
            return true;
        }
    }

    private static class BundleEquality {
        public static boolean equalBundles(Bundle one, Bundle two) {
            if (one == null)
                return two == null;
            if (one.size() != two.size())
                return false;

            Set<String> setOne = one.keySet();
            Object valueOne;
            Object valueTwo;

            for (String key : setOne) {
                valueOne = one.get(key);
                valueTwo = two.get(key);
                if (valueOne instanceof Bundle && valueTwo instanceof Bundle &&
                        !equalBundles((Bundle) valueOne, (Bundle) valueTwo)) {
                    return false;
                } else if (valueOne == null) {
                    if (valueTwo != null || !two.containsKey(key))
                        return false;
                } else if (!valueOne.equals(valueTwo))
                    return false;
            }

            return true;
        }
    }
}
