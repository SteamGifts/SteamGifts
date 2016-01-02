package net.mabako.steamgifts.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import net.mabako.steamgifts.adapters.viewholder.DiscussionListItemViewHolder;
import net.mabako.steamgifts.data.Discussion;

import java.util.List;

public class DiscussionAdapter extends EndlessAdapter {
    /**
     * Discussions that are shown per page.
     */
    private static final int ITEMS_PER_PAGE = 100;

    /**
     * Context of this adapter.
     */
    private final Activity context;

    public DiscussionAdapter(Activity context, @NonNull RecyclerView view, @NonNull OnLoadListener listener) {
        super(view, listener);
        this.context = context;
    }

    @Override
    protected RecyclerView.ViewHolder onCreateActualViewHolder(View view, int viewType) {
        return new DiscussionListItemViewHolder(view, context);
    }

    @Override
    protected void onBindActualViewHolder(RecyclerView.ViewHolder h, int position) {
        if (h instanceof DiscussionListItemViewHolder) {
            DiscussionListItemViewHolder holder = (DiscussionListItemViewHolder) h;
            Discussion discussion = (Discussion) getItem(position);

            holder.setFrom(discussion);
        }
    }

    @Override
    protected boolean hasEnoughItems(List<IEndlessAdaptable> items) {
        return items.size() == ITEMS_PER_PAGE;
    }
}
