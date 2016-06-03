package net.mabako.steamgifts.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import net.mabako.steamgifts.adapters.viewholder.TradeListItemViewHolder;
import net.mabako.steamgifts.data.Trade;

import java.util.List;

public class TradeAdapter extends EndlessAdapter {
    private static final long serialVersionUID = -7990601866101205877L;

    /**
     * Trades that are shown per page.
     */
    private static final int ITEMS_PER_PAGE = 100;

    /**
     * Context of this adapter.
     */
    private transient Activity context;

    public void setFragmentValues(OnLoadListener listener, @NonNull Activity context) {
        setLoadListener(listener);
        this.context = context;
    }

    @Override
    protected RecyclerView.ViewHolder onCreateActualViewHolder(View view, int viewType) {
        if (context == null)
            throw new IllegalStateException("Got no context");

        return new TradeListItemViewHolder(view, context, this);
    }

    @Override
    protected void onBindActualViewHolder(RecyclerView.ViewHolder h, int position) {
        if (h instanceof TradeListItemViewHolder) {
            TradeListItemViewHolder holder = (TradeListItemViewHolder) h;
            Trade trade = (Trade) getItem(position);

            holder.setFrom(trade);
        }
    }

    @Override
    protected boolean hasEnoughItems(List<IEndlessAdaptable> items) {
        return items.size() == ITEMS_PER_PAGE;
    }
}
