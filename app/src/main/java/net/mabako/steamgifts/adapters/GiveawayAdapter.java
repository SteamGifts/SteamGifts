package net.mabako.steamgifts.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import net.mabako.steamgifts.adapters.viewholder.GiveawayListItemViewHolder;
import net.mabako.steamgifts.data.Giveaway;

import java.util.List;

public class GiveawayAdapter extends EndlessAdapter {
    private static final int ITEMS_PER_PAGE = 50;
    private final Activity context;

    public GiveawayAdapter(Activity context, RecyclerView view, OnLoadListener listener) {
        super(view, listener);
        this.context = context;
    }

    @Override
    protected RecyclerView.ViewHolder onCreateActualViewHolder(View view, int viewType) {
        return new GiveawayListItemViewHolder(view, context, this);
    }

    @Override
    public void onBindActualViewHolder(RecyclerView.ViewHolder h, int position) {
        if (h instanceof GiveawayListItemViewHolder) {
            GiveawayListItemViewHolder holder = (GiveawayListItemViewHolder) h;
            Giveaway giveaway = (Giveaway) getItem(position);

            holder.setFrom(giveaway);
        }
    }

    @Override
    protected boolean hasEnoughItems(List<IEndlessAdaptable> items) {
        return items.size() == ITEMS_PER_PAGE;
    }

    public Giveaway findItem(@NonNull String giveawayId) {
        for (IEndlessAdaptable adaptable : getItems()) {
            Giveaway giveaway = (Giveaway) adaptable;
            if (giveaway != null && giveawayId.equals(giveaway.getGiveawayId()))
                return giveaway;
        }
        return null;
    }

    public void notifyItemChanged(Giveaway item) {
        int index = getItems().indexOf(item);
        if (index >= 0)
            notifyItemChanged(index);
    }
}
