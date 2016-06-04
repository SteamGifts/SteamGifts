package net.mabako.steamgifts.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.mabako.steamgifts.core.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An adapter for loading pseudo-endless lists of giveaways, discussions, games, comments and so forth.
 */
public abstract class EndlessAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Serializable {
    private static final String TAG = EndlessAdapter.class.getSimpleName();

    /**
     * First page to ever be seen on a list.
     */
    public static final int FIRST_PAGE = 1;

    /**
     * Last page we should reasonably expect.
     */
    public static final int LAST_PAGE = 11223344;

    /**
     * View ID for "Loading..."
     */
    private static final int PROGRESS_VIEW = -1;

    /**
     * View ID for "This is the end."
     */
    private static final int END_VIEW = -2;

    private static final long serialVersionUID = 95216226584860610L;

    /**
     * Sticky items, for example when using cards.
     */
    private List<IEndlessAdaptable> stickyItems = new ArrayList<>();

    /**
     * The list of items this adapter holds.
     */
    private final List<IEndlessAdaptable> items = new ArrayList<>();

    /**
     * Are we currently loading?
     */
    private boolean loading = false;

    /**
     * Upon reaching the end of the current list, we'd want to execute the listener.
     */
    private transient OnLoadListener loadListener;

    /**
     * Are we at the end of the list yet?
     */
    private boolean reachedTheEnd;

    /**
     * What page are we currently on?
     */
    private int page = FIRST_PAGE;

    /**
     * If set to true, we start from the bottom instead of the top.
     */
    private boolean viewInReverse = false;

    /**
     * If set to true, you've reached the end if all loaded items have appeared on a previous page.
     */
    protected boolean alternativeEnd = false;

    /**
     * XSRF-Token to use for calls upon this adapter.
     */
    private String xsrfToken = null;

    private RecyclerView.OnScrollListener scrollListener = new ScrollListener();

    public EndlessAdapter() {

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

        if (loadListener != null) {
            Log.d(TAG, "Starting to load more content on page " + page);
            loadListener.onLoad(page);
        } else {
            Log.w(TAG, "want to scroll more, but no load listener found");
        }
    }

    public void finishLoading(List<IEndlessAdaptable> addedItems) {
        boolean loadNextPage;

        Log.v(TAG, "Finished loading - " + loading);
        if (loading) {
            // remove loading item for the progress bar
            if (items.size() > 0) {
                items.remove(items.size() - 1);
                notifyItemRemoved(getItemCount());
            }

            loading = false;
            loadNextPage = addAll(addedItems);
        } else {
            boolean wasEmpty = items.size() == 0;
            loadNextPage = addAll(addedItems);

            if (!wasEmpty)
                loadNextPage = false;
        }

        // Have we reached the last page yet?
        if (viewInReverse) {
            --page;
            if (page < FIRST_PAGE && !reachedTheEnd)
                reachedTheEnd();
        } else
            ++page;

        if (loadNextPage) {
            // We presume to have filtered some items, and automatically load more since the onScrollListener doesn't quite handle this edge case yet.
            startLoading();
        }
    }

    public void cancelLoading() {
        loading = false;
    }

    public void reachedTheEnd() {
        reachedTheEnd(true);
    }

    /**
     * @param addEndItem if this is set to false, we do not add the "You've reached the end" text.
     */
    public void reachedTheEnd(boolean addEndItem) {
        if (reachedTheEnd)
            return;

        Log.d(TAG, "Reached the end");

        // Make sure we're not loading anymore...
        if (loading)
            finishLoading(new ArrayList<IEndlessAdaptable>());

        reachedTheEnd = true;

        if (addEndItem) {
            items.add(null);
            notifyItemInserted(getItemCount() - 1);
        }
    }

    protected List<IEndlessAdaptable> getItems() {
        return items;
    }

    /**
     * How many items do we currently show?
     *
     * @return number of items currently shown
     */
    public int getItemCount() {
        return items.size() + stickyItems.size();
    }

    /**
     * Add a whole range of items to this adapter, and check if we've reached the end.
     *
     * @param items items to add.
     * @return true if any items were filtered and we should just try to load another page, false otherwise
     */
    private boolean addAll(List<IEndlessAdaptable> items) {
        if (items.size() > 0) {
            boolean enoughItems = hasEnoughItems(items);
            // remove all things we already have
            items.removeAll(this.items);

            // How many items were on the page?
            int realItemCount = items.size();

            // And how many items did we add after filtering?
            int insertedItems = addFiltered(items);

            notifyItemRangeInserted(getItemCount() - insertedItems, insertedItems);

            if (enoughItems && realItemCount == 0 && alternativeEnd) {
                enoughItems = false;
                Log.v(TAG, "Not Enough items for the next page [1]");
            }

            if (viewInReverse && page > FIRST_PAGE) {
                enoughItems = true;
                Log.v(TAG, "Not Enough items for the next page [2]");
            }

            // Did we have enough items and have not reached the end?
            if (!enoughItems && !reachedTheEnd) {
                reachedTheEnd();
                return false;
            }

            // Have we filtered out all items, while we should have had some items?
            if (realItemCount > 0) {
                // We didn't even get a single item out of this
                if (insertedItems == 0)
                    return true;

                // Have we filtered any items, and are we still at the start of reasonable suspicion of this view not being fully filled?
                return this.items.size() <= 25 && insertedItems != realItemCount;
            }
            return false;
        } else {
            Log.v(TAG, "Got no items on the current page");
            reachedTheEnd();
            return false;
        }
    }

    protected int addFiltered(List<IEndlessAdaptable> items) {
        this.items.addAll(items);
        return items.size();
    }

    public void clear() {
        items.clear();
        reachedTheEnd = false;
        page = viewInReverse ? LAST_PAGE : FIRST_PAGE;

        notifyDataSetChanged();
    }

    public IEndlessAdaptable getItem(int position) {
        if (position == RecyclerView.NO_POSITION)
            return null;

        if (position < stickyItems.size())
            return stickyItems.get(position);

        return items.get(position - stickyItems.size());
    }

    public List<IEndlessAdaptable> getStickyItems() {
        return stickyItems;
    }

    public void setStickyItems(List<IEndlessAdaptable> stickyItems) {
        int oldSize = this.stickyItems.size(),
                newSize = stickyItems.size();

        this.stickyItems.clear();
        this.stickyItems.addAll(stickyItems);

        if (oldSize >= newSize) {
            notifyItemRangeChanged(0, newSize);

            if (oldSize > newSize)
                // some item was actually removed
                notifyItemRangeRemoved(newSize, oldSize - newSize);
        } else /* oldSize < newSize */ {
            // new items inserted
            notifyItemRangeChanged(0, oldSize);
            notifyItemRangeInserted(oldSize, newSize - oldSize);
        }
    }

    public IEndlessAdaptable getStickyItem() {
        return stickyItems.isEmpty() ? null : stickyItems.get(0);
    }

    public void setStickyItem(IEndlessAdaptable stickyItem) {
        setStickyItems(Arrays.asList(stickyItem));
    }

    /**
     * Return the view type for the item at a specific position.
     *
     * @param position position of the item
     * @return the layout if it's an actual item, {@link #PROGRESS_VIEW} or {@link #END_VIEW} if it's a progress spinner or the end.
     */
    public int getItemViewType(int position) {
        return position < getItemCount() && getItem(position) != null ? getItem(position).getLayout() : reachedTheEnd ? END_VIEW : PROGRESS_VIEW;
    }

    /**
     * Create the ViewHolder for the item
     *
     * @param parent
     * @param viewType type of the view
     * @return
     */
    public final RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == PROGRESS_VIEW || viewType == END_VIEW) {
            View view = LayoutInflater.from(parent.getContext()).inflate(viewType == PROGRESS_VIEW ? R.layout.endless_progress_bar : R.layout.endless_scroll_end, parent, false);
            return new EmptyViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);

            RecyclerView.ViewHolder holder = onCreateActualViewHolder(view, viewType);
            if (holder == null)
                throw new IllegalStateException("Got no view holder for " + viewType);
            return holder;
        }
    }

    /**
     * Proxy binding a viewholder to the item. In particular, if this is not a custom item, but a progress/end view, nothing will be called upon.
     *
     * @param holder   view holder instance
     * @param position position of the item
     */
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


    protected RemovedElement removeItem(int position) {
        IEndlessAdaptable current = items.get(position);
        IEndlessAdaptable before = position > 0 ? items.get(position - 1) : null;

        items.remove(position);
        notifyItemRemoved(position + stickyItems.size());

        return new RemovedElement(before, current);
    }

    /**
     * Restore a previously removed item.
     *
     * @param element the item that was removed.
     * @see #removeItem(int)
     * @see #restore(Iterable)
     */
    public void restore(@NonNull RemovedElement element) {
        int position = 0;
        if (element.getElementBefore() != null) {
            position = items.indexOf(element.getElementBefore());
            if (position == -1) {
                Log.w(TAG, "Could not restore element, index not found - " + element.getElement());
                return;
            }

            // We want to place it after that element.
            ++position;
        }

        items.add(position, element.getElement());
        notifyItemInserted(position + stickyItems.size());
    }

    /**
     * Restore a list of previously removed items.
     *
     * @param elements all elements that should be restored
     * @see #removeItem(int)
     * @see #restore(RemovedElement)
     */
    public void restore(@NonNull Iterable<RemovedElement> elements) {
        for (RemovedElement e : elements)
            restore(e);
    }

    /**
     * Clear the list of elements if you're on the first page if {@link #viewInReverse} is not set, or the last page if {@link #viewInReverse} is set.
     *
     * @param page     current page
     * @param lastPage is this page the last page?
     */
    public void notifyPage(int page, boolean lastPage) {
        if (viewInReverse && lastPage) {
            clear();
            this.page = page;
        } else if (!viewInReverse && page == 1)
            clear();
    }

    /**
     * Start from the last page, instead of the first.
     */
    public void setViewInReverse() {
        if (!alternativeEnd)
            throw new UnsupportedOperationException("could not reverse an endless adapter without alternativeEnd set [will have no content on the last pages]");

        viewInReverse = true;
        page = LAST_PAGE;
    }

    /**
     * Is this list viewed in reverse?
     *
     * @return true if the list is viewed in reverse, false otherwise
     */
    public boolean isViewInReverse() {
        return viewInReverse;
    }

    /**
     * Get the scroll listener associated with this adapter.
     *
     * @return scroll listener to bind the view to
     */
    public RecyclerView.OnScrollListener getScrollListener() {
        return scrollListener;
    }

    /**
     * Does this item have any items loaded yet?
     *
     * @return true if any items are loaded, false otherwise
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    public String getXsrfToken() {
        return xsrfToken;
    }

    public void setXsrfToken(String xsrfToken) {
        this.xsrfToken = xsrfToken;
    }

    /**
     * Find the id for the item, and if it exists, trigger a changed notification.
     *
     * @param item item to notify of change
     * @return true if any item was found, false otherwise
     */
    public boolean notifyItemChanged(IEndlessAdaptable item) {
        if (item == null)
            return false;

        int index = items.indexOf(item);
        if (index >= 0) {
            notifyItemChanged(index + stickyItems.size());
            return true;
        }

        index = stickyItems.indexOf(item);
        if (index >= 0) {
            notifyItemChanged(index);
            return true;
        }

        return false;
    }

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
    protected void setLoadListener(OnLoadListener loadListener) {
        this.loadListener = loadListener;
    }

    public interface OnLoadListener {

        void onLoad(int page);
    }

    private class ScrollListener extends RecyclerView.OnScrollListener implements Serializable {
        private static final long serialVersionUID = -9087960089493875144L;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (layoutManager == null)
                throw new IllegalStateException("Can't handle scrolling without a LayoutManager");

            int lastVisibleItem = layoutManager.findLastVisibleItemPosition();

            if (!loading && layoutManager.getItemCount() <= (lastVisibleItem + 5)) {
                startLoading();
            }
        }
    }

    public static class RemovedElement implements Serializable {
        private static final long serialVersionUID = -4246240052789998135L;

        /**
         * The item that was in place before the current removable.
         */
        private final IEndlessAdaptable elementBefore;

        /**
         * The element that was removed.
         */
        private final IEndlessAdaptable element;

        public RemovedElement(IEndlessAdaptable elementBefore, IEndlessAdaptable element) {
            this.elementBefore = elementBefore;
            this.element = element;
        }

        public IEndlessAdaptable getElement() {
            return element;
        }

        public IEndlessAdaptable getElementBefore() {
            return elementBefore;
        }
    }
}
