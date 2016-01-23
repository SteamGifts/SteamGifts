package net.mabako.steamgifts.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import net.mabako.steamgifts.adapters.viewholder.GiveawayListItemViewHolder;
import net.mabako.steamgifts.data.Game;
import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.fragments.interfaces.IHasEnterableGiveaways;

import java.util.List;

public class GiveawayAdapter extends EndlessAdapter {
    /**
     * Giveaways that are shown per page.
     */
    private final int itemsPerPage;

    /**
     * Context of this adapter.
     */
    private final Activity context;

    /**
     * Fragment this is shown in.
     */
    private final IHasEnterableGiveaways fragment;

    public GiveawayAdapter(Activity context, OnLoadListener listener, IHasEnterableGiveaways fragment, int itemsPerPage) {
        super(listener);
        this.context = context;
        this.fragment = fragment;
        this.itemsPerPage = itemsPerPage;
    }

    @Override
    protected RecyclerView.ViewHolder onCreateActualViewHolder(View view, int viewType) {
        return new GiveawayListItemViewHolder(view, context, this, fragment);
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
        return items.size() == itemsPerPage;
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

    public void removeGiveaway(String giveawayId) {
        for (int position = getItems().size() - 1; position >= 0; --position) {
            if (giveawayId.equals(((Giveaway) getItem(position)).getGiveawayId())) {
                removeItem(position);
            }
        }
    }

    public void removeHiddenGame(int internalGameId) {
        if (internalGameId == Game.NO_APP_ID)
            throw new IllegalStateException();

        for (int position = getItems().size() - 1; position >= 0; --position) {
            Giveaway giveaway = (Giveaway) getItem(position);

            if (giveaway != null && giveaway.getInternalGameId() == internalGameId) {
                removeItem(position);
            }
        }
    }
}
