package net.mabako.steamgifts.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.adapters.EndlessAdapter;
import net.mabako.steamgifts.adapters.GiveawayAdapter;
import net.mabako.steamgifts.fragments.interfaces.IActivityTitle;
import net.mabako.steamgifts.fragments.util.GiveawayListFragmentStack;
import net.mabako.steamgifts.persistentdata.SavedGiveaways;

import java.io.Serializable;

/**
 * Show a list of saved giveaways.
 */
// TODO implements IHasEnterableGiveaways?
public class SavedGiveawaysFragment extends ListFragment<GiveawayAdapter> implements IActivityTitle {
    private SavedGiveaways savedGiveaways;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GiveawayListFragmentStack.addFragment(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        savedGiveaways = new SavedGiveaways(context);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        GiveawayListFragmentStack.removeFragment(this);
    }

    @Override
    protected GiveawayAdapter createAdapter() {
        return new GiveawayAdapter(getActivity(), new EndlessAdapter.OnLoadListener() {
            @Override
            public void onLoad(int page) {
                fetchItems(page);
            }
        }, null, -1);
    }

    @Override
    protected AsyncTask<Void, Void, ?> getFetchItemsTask(int page) {
        return null;
    }

    @Override
    protected Serializable getType() {
        return null;
    }

    @Override
    protected void fetchItems(int page) {
        if (page != 1)
            return;

        addItems(savedGiveaways.getGiveaways(), true);
        adapter.reachedTheEnd();
    }

    @Override
    public int getTitleResource() {
        return R.string.navigation_giveaways_saved_title;
    }

    @Override
    public String getExtraTitle() {
        return null;
    }
}
