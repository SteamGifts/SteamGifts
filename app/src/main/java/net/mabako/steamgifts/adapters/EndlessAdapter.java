package net.mabako.steamgifts.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.mabako.steamgifts.R;

import java.util.ArrayList;
import java.util.List;

public abstract class EndlessAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = EndlessAdapter.class.getSimpleName();

    private static final int PROGRESS_VIEW = -1;
    private static final int END_VIEW = -2;

    private IEndlessAdaptable stickyItem = null;
    private final List<IEndlessAdaptable> items = new ArrayList<>();

    private boolean loading = false;
    private OnLoadListener loadListener;
    private boolean reachedTheEnd;
    private int page = 1;

    /**
     * If set to true, you've reached the end if all loaded items have appeared on a previous page.
     */
    protected boolean alternativeEnd = false;

    public EndlessAdapter(@NonNull RecyclerView view, @NonNull OnLoadListener listener) {
        final LinearLayoutManager layoutManager = (LinearLayoutManager) view.getLayoutManager();
        if (layoutManager == null)
            throw new IllegalStateException("No layout manager");

        loadListener = listener;

        view.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();

                if (!loading && layoutManager.getItemCount() <= (lastVisibleItem + 5)) {
                    startLoading();
                }
            }
        });
    }

    /**
     * Start loading by insert a progress bar item.
     */
    private void startLoading() {
        if (reachedTheEnd)
            return;

        loading = true;

        // Insert bogus item for the progress bar.
        items.add(null);
        notifyItemInserted(getItemCount() - 1);

        Log.v(TAG, "Starting to load more content on page " + page);
        loadListener.onLoad(page);
    }

    public void finishLoading(List<IEndlessAdaptable> addedItems) {
        Log.d(TAG, "Finished loading - " + loading);
        if (loading) {
            // remove loading item for the progress bar
            if (items.size() > 0) {
                items.remove(items.size() - 1);
                notifyItemRemoved(getItemCount());
            }

            loading = false;
            addAll(addedItems);
        } else {
            addAll(addedItems);
        }
        ++page;
    }

    public void reachedTheEnd() {
        Log.d(TAG, "Reached the end");

        // Make sure we're not loading anymore...
        if (loading)
            finishLoading(new ArrayList<IEndlessAdaptable>());

        reachedTheEnd = true;

        items.add(null);
        notifyItemInserted(getItemCount() - 1);
    }

    protected List<IEndlessAdaptable> getItems() {
        return items;
    }

    @Override
    public int getItemCount() {
        int itemCount = items.size();
        if (stickyItem != null)
            itemCount++;
        return itemCount;
    }

    private void addAll(List<IEndlessAdaptable> items) {
        if (items.size() > 0) {
            boolean enoughItems = hasEnoughItems(items);
            // remove all things we already have
            items.removeAll(this.items);

            this.items.addAll(items);
            this.notifyItemRangeInserted(getItemCount() - items.size(), items.size());

            if (enoughItems && items.size() == 0 && alternativeEnd) {
                enoughItems = false;
            }

            // Did we have enough items and have not reached the end?
            if (!enoughItems && !reachedTheEnd)
                reachedTheEnd();
        } else {
            reachedTheEnd();
        }
    }

    public void clear() {
        Log.d(TAG, "Clearing list");

        items.clear();
        reachedTheEnd = false;
        page = 1;
        notifyDataSetChanged();
    }

    public IEndlessAdaptable getItem(int position) {
        if (stickyItem != null) {
            return position == 0 ? stickyItem : items.get(position - 1);
        } else {
            return items.get(position);
        }
    }

    public IEndlessAdaptable getStickyItem() {
        return stickyItem;
    }

    public void setStickyItem(IEndlessAdaptable stickyItem) {
        if (this.stickyItem == null) {
            this.stickyItem = stickyItem;
            notifyItemInserted(0);
        } else {
            this.stickyItem = stickyItem;
            notifyItemChanged(0);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position < getItemCount() && getItem(position) != null ? getItem(position).getLayout() : reachedTheEnd ? END_VIEW : PROGRESS_VIEW;
    }

    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == PROGRESS_VIEW || viewType == END_VIEW) {
            View view = LayoutInflater.from(parent.getContext()).inflate(viewType == PROGRESS_VIEW ? R.layout.endless_progress_bar : R.layout.endless_scroll_end, parent, false);
            return new EmptyViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);

            RecyclerView.ViewHolder holder = onCreateActualViewHolder(view, viewType);
            if (holder == null)
                throw new IllegalStateException("Got no giveaway holder for " + viewType);
            return holder;
        }
    }

    /**
     * Proxy binding a viewholder to the item. In particular, if this is not a custom item, but a progress/end view, nothing will be called upon.
     *
     * @param holder   view holder instance
     * @param position position of the item
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder != null && !(holder instanceof EmptyViewHolder)) {
            onBindActualViewHolder(holder, position);
        }
    }

    /**
     * Create a view holder for item.
     *
     * @param view     the instantiated view
     * @param viewType the view's layout id
     * @return viewholder for the view
     */
    protected abstract RecyclerView.ViewHolder onCreateActualViewHolder(View view, int viewType);

    /**
     * Bind a viewholder to a particular item
     *
     * @param holder   view holder instance
     * @param position position of the item
     */
    protected abstract void onBindActualViewHolder(RecyclerView.ViewHolder holder, int position);

    /**
     * Check whether or not we have enough items to load more (i.e. page is full)
     *
     * @param items
     * @return {@code true} if more items can be loaded, {@code false} otherwise
     */
    protected abstract boolean hasEnoughItems(List<IEndlessAdaptable> items);

    /**
     * View holder with no interactions.
     * <p/>
     * This is the case for the progress bar and the "You've reached the end" text.
     */
    public static class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(View v) {
            super(v);
        }
    }

    /**
     * Listener called upon scrolling down to load "more" items.
     */
    public interface OnLoadListener {
        void onLoad(int page);
    }
}
