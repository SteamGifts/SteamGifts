package net.mabako.steamgifts.adapters;

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

public abstract class EndlessAdapter<ItemType, HolderType extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = EndlessAdapter.class.getSimpleName();

    private static final int ACTUAL_VIEW = 1;
    private static final int PROGRESS_VIEW = 2;
    private static final int END_VIEW = 3;

    private List<ItemType> items = new ArrayList<>();
    private boolean loading = false;
    private OnLoadListener loadListener;
    private boolean reachedTheEnd;
    private int page = 1;

    public EndlessAdapter(RecyclerView view, OnLoadListener listener) {
        final LinearLayoutManager layout = (LinearLayoutManager) view.getLayoutManager();

        if (listener != null) {
            loadListener = listener;

            view.addOnScrollListener(new OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    int lastVisibleItem = layout.findLastVisibleItemPosition();

                    if (!loading && layout.getItemCount() <= (lastVisibleItem + 5)) {
                        startLoading();
                    }
                }
            });
        }
    }

    private void startLoading() {
        if (reachedTheEnd)
            return;

        loading = true;

        // Insert bogus item for the progress bar.
        items.add(null);
        notifyItemInserted(items.size() - 1);

        Log.v(TAG, "Starting to load more content on page " + page);
        loadListener.onLoad(page);
    }

    public void finishLoading(List<ItemType> addedItems) {
        Log.d(TAG, "Finished loading - " + loading);
        if (loading) {
            // remove loading item for the progress bar
            if (items.size() > 0) {
                items.remove(items.size() - 1);
                notifyItemRemoved(items.size());
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
            finishLoading(new ArrayList<ItemType>());

        reachedTheEnd = true;

        items.add(null);
        notifyItemInserted(items.size() - 1);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void addAll(List<ItemType> items) {
        if (items.size() > 0) {
            items.removeAll(this.items);
            this.items.addAll(items);
            this.notifyItemRangeInserted(this.items.size() - items.size(), items.size());
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

    public ItemType getItem(int position) {
        return items.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        return position < getItemCount() && getItem(position) != null ? ACTUAL_VIEW : reachedTheEnd ? END_VIEW : PROGRESS_VIEW;
    }

    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ACTUAL_VIEW) {
            return onCreateActualViewHolder(parent);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(reachedTheEnd ? R.layout.endless_scroll_end : R.layout.endless_progress_bar, parent, false);
            return new EmptyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder != null && !(holder instanceof EmptyViewHolder)) {
            onBindActualViewHolder((HolderType) holder, position);
        }
    }

    protected abstract HolderType onCreateActualViewHolder(ViewGroup parent);

    protected abstract void onBindActualViewHolder(HolderType holder, int position);

    public static class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(View v) {
            super(v);
        }
    }

    public interface OnLoadListener {
        void onLoad(int page);
    }
}
