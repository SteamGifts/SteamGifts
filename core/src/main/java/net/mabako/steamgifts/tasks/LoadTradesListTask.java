package net.mabako.steamgifts.tasks;

import android.os.AsyncTask;

import net.mabako.steamgifts.data.Discussion;
import net.mabako.steamgifts.data.Trade;
import net.mabako.steamgifts.fragments.TradeListFragment;

import java.util.List;

public class LoadTradesListTask extends AsyncTask<Void, Void, List<Trade>> {
    private final TradeListFragment fragment;
    private final int page;
    private final TradeListFragment.Type type;
    private final String searchQuery;

    public LoadTradesListTask(TradeListFragment fragment, int page, TradeListFragment.Type type, String searchQuery) {
        this.fragment = fragment;
        this.page = page;
        this.type = type;
        this.searchQuery = searchQuery;
    }

    @Override
    protected List<Trade> doInBackground(Void... params) {
        return null;
    }

    @Override
    protected void onPostExecute(List<Trade> result) {
        super.onPostExecute(result);
        fragment.addItems(result, page == 1);
    }
}
