package net.mabako.steamgifts.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import net.mabako.steamgifts.adapters.viewholder.GiveawayGroupViewHolder;
import net.mabako.steamgifts.data.GiveawayGroup;

import java.util.List;

public class GiveawayGroupAdapter extends EndlessAdapter {
    /**
     * Groups that are shown per page.
     */
    private static final int ITEMS_PER_PAGE = 25; // TODO actual number?

    /**
     * Context of this adapter.
     */
    private final Context context;

    public GiveawayGroupAdapter(Context context, RecyclerView view, OnLoadListener listener) {
        super(view, listener);
        this.context = context;
        this.alternativeEnd = true;
    }

    @Override
    protected RecyclerView.ViewHolder onCreateActualViewHolder(View view, int viewType) {
        return new GiveawayGroupViewHolder(view, context);
    }

    @Override
    protected void onBindActualViewHolder(RecyclerView.ViewHolder h, int position) {
        if (h instanceof GiveawayGroupViewHolder) {
            GiveawayGroupViewHolder holder = (GiveawayGroupViewHolder) h;
            GiveawayGroup group = (GiveawayGroup) getItem(position);
            holder.setFrom(group);
        }
    }

    @Override
    protected boolean hasEnoughItems(List<IEndlessAdaptable> items) {
        return items.size() == ITEMS_PER_PAGE;
    }
}
