package net.mabako.steamgifts.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

import net.mabako.steamgifts.adapters.GiveawayAdapter;
import net.mabako.steamgifts.core.R;
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
        adapter.setFragmentValues(getActivity(), this, savedGiveaways);

        GiveawayListFragmentStack.addFragment(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        savedGiveaways = new SavedGiveaways(getContext());
    }

    @Override
    public void onDetach() {
        super.onDetach();


        if (savedGiveaways != null) {
            savedGiveaways.close();
            savedGiveaways = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        GiveawayListFragmentStack.removeFragment(this);
    }

    @Override
    protected GiveawayAdapter createAdapter() {
        return new GiveawayAdapter(-1, false, PreferenceManager.getDefaultSharedPreferences(getContext()));
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

        addItems(savedGiveaways.all(), true);
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

    public void onRemoveSavedGiveaway(String giveawayId) {
        adapter.removeGiveaway(giveawayId);
    }
}
