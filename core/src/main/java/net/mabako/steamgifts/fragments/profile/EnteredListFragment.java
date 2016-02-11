package net.mabako.steamgifts.fragments.profile;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import net.mabako.steamgifts.adapters.GiveawayAdapter;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.fragments.GiveawayDetailFragment;
import net.mabako.steamgifts.fragments.ListFragment;
import net.mabako.steamgifts.fragments.interfaces.IActivityTitle;
import net.mabako.steamgifts.fragments.interfaces.IHasEnterableGiveaways;
import net.mabako.steamgifts.fragments.util.GiveawayListFragmentStack;
import net.mabako.steamgifts.tasks.EnterLeaveGiveawayTask;

import java.io.Serializable;

public class EnteredListFragment extends ListFragment<GiveawayAdapter> implements IHasEnterableGiveaways, IActivityTitle {
    private final static String TAG = EnteredListFragment.class.getSimpleName();
    private EnterLeaveGiveawayTask enterLeaveTask;

    @Override
    public int getTitleResource() {
        return R.string.user_tab_entered;
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
        return new GiveawayAdapter(LoadEnteredGameListTask.ENTRIES_PER_PAGE, PreferenceManager.getDefaultSharedPreferences(getContext()));
    }

    @Override
    protected AsyncTask<Void, Void, ?> getFetchItemsTask(int page) {
        return new LoadEnteredGameListTask(this, page);
    }

    @Override
    protected Serializable getType() {
        return null;
    }

    @Override
    public void requestEnterLeave(String giveawayId, String what, String xsrfToken) {
        // Probably not...
        // if (enterLeaveTask != null)
        // enterLeaveTask.cancel(true);

        enterLeaveTask = new EnterLeaveGiveawayTask(this, getContext(), giveawayId, xsrfToken, what);
        enterLeaveTask.execute();
    }

    @Override
    public void onEnterLeaveResult(String giveawayId, String what, Boolean success, boolean propagate) {
        if (success == Boolean.TRUE && GiveawayDetailFragment.ENTRY_DELETE.equals(what)) {
            adapter.removeGiveaway(giveawayId);
        } else {
            Log.e(TAG, "Probably an error catching the result...");
        }

        if (propagate)
            GiveawayListFragmentStack.onEnterLeaveResult(giveawayId, what, success);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (enterLeaveTask != null)
            enterLeaveTask.cancel(true);
    }

}
