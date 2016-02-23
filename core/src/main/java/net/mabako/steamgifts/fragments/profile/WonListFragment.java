package net.mabako.steamgifts.fragments.profile;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import net.mabako.steamgifts.adapters.GiveawayAdapter;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.fragments.ListFragment;
import net.mabako.steamgifts.fragments.interfaces.IActivityTitle;
import net.mabako.steamgifts.receivers.CheckForWonGiveaways;
import net.mabako.steamgifts.tasks.LoadWonGameListTask;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WonListFragment extends ListFragment<GiveawayAdapter> implements IActivityTitle {
    @Override
    public int getTitleResource() {
        return R.string.user_giveaway_won;
    }

    @Override
    public String getExtraTitle() {
        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter.setFragmentValues(getActivity(), this, null);
    }

    @NonNull
    @Override
    protected GiveawayAdapter createAdapter() {
        return new GiveawayAdapter(50, PreferenceManager.getDefaultSharedPreferences(getContext()));
    }

    @Override
    public void addItems(List<? extends IEndlessAdaptable> items, boolean clearExistingItems) {
        super.addItems(items, clearExistingItems);
        if (clearExistingItems && items != null) {
            List<String> ids = new ArrayList<>();
            for (IEndlessAdaptable item : items) {
                if (item instanceof Giveaway) {
                    ids.add(((Giveaway) item).getGiveawayId());
                }
            }
            CheckForWonGiveaways.setLastDismissedGiveawayIds(getContext(), ids);
        }
    }

    @Override
    protected AsyncTask<Void, Void, ?> getFetchItemsTask(int page) {
        return new LoadWonGameListTask(this, getContext(), page);
    }

    @Override
    protected Serializable getType() {
        return null;
    }

}
