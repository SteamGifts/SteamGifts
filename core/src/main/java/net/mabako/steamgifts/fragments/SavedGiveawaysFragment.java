package net.mabako.steamgifts.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import net.mabako.steamgifts.adapters.GiveawayAdapter;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.fragments.interfaces.IActivityTitle;
import net.mabako.steamgifts.fragments.interfaces.IHasEnterableGiveaways;
import net.mabako.steamgifts.fragments.profile.LoadEnteredGameListTask;
import net.mabako.steamgifts.fragments.profile.ProfileGiveaway;
import net.mabako.steamgifts.fragments.util.GiveawayListFragmentStack;
import net.mabako.steamgifts.persistentdata.SavedGiveaways;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;
import net.mabako.steamgifts.tasks.EnterLeaveGiveawayTask;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Show a list of saved giveaways.
 */
public class SavedGiveawaysFragment extends ListFragment<SavedGiveawaysFragment.SavedGiveawaysAdapter> implements IActivityTitle, IHasEnterableGiveaways {
    private static final String TAG = SavedGiveawaysFragment.class.getSimpleName();

    private SavedGiveaways savedGiveaways;

    private LoadEnteredGameListTask enteredGameListTask;
    private EnterLeaveGiveawayTask enterLeaveTask;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        setHasOptionsMenu(true);

        return view;
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
    public void onDestroy() {
        super.onDestroy();

        if (enteredGameListTask != null) {
            enteredGameListTask.cancel(true);
            enteredGameListTask = null;
        }

        if (enterLeaveTask != null) {
            enterLeaveTask.cancel(true);
            enterLeaveTask = null;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.saved_giveaways_menu, menu);
        menu.findItem(R.id.remove_all_entered_saved).setVisible(adapter.getEnteredItemCount() > 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.remove_all_entered_saved) {
            for (Giveaway enteredGiveaway : adapter.getEnteredItems()) {
                savedGiveaways.remove(enteredGiveaway.getGiveawayId());
                adapter.removeGiveaway(enteredGiveaway.getGiveawayId());
            }

            if (getActivity() != null)
                getActivity().invalidateOptionsMenu();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @NonNull
    @Override
    protected SavedGiveawaysAdapter createAdapter() {
        return new SavedGiveawaysAdapter(-1, false, PreferenceManager.getDefaultSharedPreferences(getContext()));
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

        super.addItems(savedGiveaways.all(), true);
        adapter.reachedTheEnd();

        // Load all entered giveaways
        if (enteredGameListTask != null)
            enteredGameListTask.cancel(true);

        if (SteamGiftsUserData.getCurrent(getContext()).isLoggedIn()) {
            enteredGameListTask = new LoadEnteredGameListTask(this, 1);
            enteredGameListTask.execute();
        }
    }

    @Override
    public int getTitleResource() {
        return R.string.saved_giveaways_title;
    }

    @Override
    public String getExtraTitle() {
        return null;
    }

    public void onRemoveSavedGiveaway(String giveawayId) {
        adapter.removeGiveaway(giveawayId);
    }

    /**
     * Callback for {@link #enteredGameListTask}
     * <p>Note: do NOT call this from within this class.</p>
     */
    @Override
    public void addItems(List<? extends IEndlessAdaptable> items, boolean clearExistingItems) {
        if (items != null) {
            // closed or not deleted
            boolean foundAnyClosedGiveaways = false;

            // do nothing much except update the status of existing giveaways.
            for (IEndlessAdaptable endlessAdaptable : items) {
                ProfileGiveaway giveaway = (ProfileGiveaway) endlessAdaptable;
                if (!giveaway.isOpen() && !giveaway.isDeleted()) {
                    foundAnyClosedGiveaways = true;
                    break;
                }

                Giveaway existingGiveaway = adapter.findItem(giveaway.getGiveawayId());
                if (existingGiveaway != null) {
                    existingGiveaway.setEntries(giveaway.getEntries());
                    existingGiveaway.setEntered(true);
                    adapter.notifyItemChanged(existingGiveaway);
                }
            }

            FragmentActivity activity = getActivity();
            if (activity != null)
                activity.supportInvalidateOptionsMenu();

            // have we found any non-closed giveaways?
            if (foundAnyClosedGiveaways) {
                enteredGameListTask = null;
            } else {
                enteredGameListTask = new LoadEnteredGameListTask(this, enteredGameListTask.getPage() + 1);
                enteredGameListTask.execute();
            }
        } else {
            showSnack("Failed to update entered giveaways", Snackbar.LENGTH_LONG);
        }
    }

    @Override
    public void requestEnterLeave(String giveawayId, String enterOrDelete, String xsrfToken) {
        if (!SteamGiftsUserData.getCurrent(getContext()).isLoggedIn()) {
            Log.w(TAG, "Could not request enter/leave giveaway, since we're not logged in");
            return;
        }

        if (enterLeaveTask != null)
            enterLeaveTask.cancel(true);

        enterLeaveTask = new EnterLeaveGiveawayTask(this, getContext(), giveawayId, xsrfToken, enterOrDelete);
        enterLeaveTask.execute();
    }

    @Override
    public void onEnterLeaveResult(String giveawayId, String what, Boolean success, boolean propagate) {
        if (success == Boolean.TRUE) {
            Giveaway giveaway = adapter.findItem(giveawayId);
            if (giveaway != null) {
                boolean currentlyEnteredAny = adapter.getEnteredItemCount() > 0;

                giveaway.setEntered(GiveawayDetailFragment.ENTRY_INSERT.equals(what));
                adapter.notifyItemChanged(giveaway);

                boolean nowEnteredAny = adapter.getEnteredItemCount() > 0;
                if (currentlyEnteredAny != nowEnteredAny && getActivity() != null)
                    getActivity().supportInvalidateOptionsMenu();
            }
        } else {
            Log.e(TAG, "Probably an error catching the result...");
        }

        if (propagate)
            GiveawayListFragmentStack.onEnterLeaveResult(giveawayId, what, success);
    }

    /**
     * Adapter with some useful functions for saved items.
     */
    public static class SavedGiveawaysAdapter extends GiveawayAdapter {
        private static final long serialVersionUID = -6841859269105451683L;

        private SavedGiveawaysAdapter(int itemsPerPage, boolean filterItems, SharedPreferences sharedPreferences) {
            super(itemsPerPage, filterItems, sharedPreferences);
        }

        public int getEnteredItemCount() {
            int entered = 0;

            for (IEndlessAdaptable item : getItems()) {
                if (item instanceof Giveaway && ((Giveaway) item).isEntered())
                    ++entered;
            }

            return entered;
        }

        public List<Giveaway> getEnteredItems() {
            List<Giveaway> entered = new ArrayList<>();
            for (IEndlessAdaptable item : getItems()) {
                if (item instanceof Giveaway && ((Giveaway) item).isEntered())
                    entered.add((Giveaway) item);
            }

            return entered;
        }
    }
}
