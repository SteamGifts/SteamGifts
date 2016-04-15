package net.mabako.steamgifts.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.mikepenz.actionitembadge.library.ActionItemBadge;
import com.mikepenz.actionitembadge.library.utils.BadgeStyle;

import net.mabako.steamgifts.adapters.EndlessAdapter;
import net.mabako.steamgifts.adapters.GiveawayAdapter;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.fragments.interfaces.IActivityTitle;
import net.mabako.steamgifts.fragments.interfaces.IFilterUpdatedListener;
import net.mabako.steamgifts.fragments.interfaces.IHasEnterableGiveaways;
import net.mabako.steamgifts.fragments.interfaces.IHasHideableGiveaways;
import net.mabako.steamgifts.fragments.util.GiveawayListFragmentStack;
import net.mabako.steamgifts.persistentdata.FilterData;
import net.mabako.steamgifts.persistentdata.SavedGiveaways;
import net.mabako.steamgifts.tasks.EnterLeaveGiveawayTask;
import net.mabako.steamgifts.tasks.LoadGiveawayListTask;
import net.mabako.steamgifts.tasks.UpdateGiveawayFilterTask;

import java.io.Serializable;
import java.util.List;

/**
 * List of all giveaways.
 */
public class GiveawayListFragment extends SearchableListFragment<GiveawayAdapter> implements IHasEnterableGiveaways, IHasHideableGiveaways, IActivityTitle, IFilterUpdatedListener {
    private static final String TAG = GiveawayListFragment.class.getSimpleName();
    private static final String SAVED_TYPE = "type";
    private static final String SAVED_LAST_REMOVED = "last-removed-game";

    private EnterLeaveGiveawayTask enterLeaveTask;
    private SavedGiveaways savedGiveaways;

    /**
     * Type of items to show.
     */
    private Type type = Type.ALL;

    /**
     * Any game we might have removed from the giveaway list.
     */
    private LastRemovedGame lastRemovedGame;

    public static GiveawayListFragment newInstance(Type type, String query, boolean finishActivityOnSearchStopped) {
        GiveawayListFragment g = new GiveawayListFragment();

        Bundle args = new Bundle();
        args.putSerializable(SAVED_TYPE, type);
        args.putString(SAVED_QUERY, query);
        args.putBoolean(SAVED_FINISH_ON_STOP, finishActivityOnSearchStopped);
        g.setArguments(args);

        g.type = type;

        return g;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GiveawayListFragmentStack.addFragment(this);

        if (savedInstanceState == null) {
            type = (Type) getArguments().getSerializable(SAVED_TYPE);
            lastRemovedGame = null;
        } else {
            type = (Type) savedInstanceState.getSerializable(SAVED_TYPE);
            lastRemovedGame = (LastRemovedGame) savedInstanceState.getSerializable(SAVED_LAST_REMOVED);
        }

        adapter.setFragmentValues(getActivity(), this, savedGiveaways);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(SAVED_TYPE, type);
        outState.putSerializable(SAVED_LAST_REMOVED, lastRemovedGame);
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

        if (enterLeaveTask != null)
            enterLeaveTask.cancel(true);
    }

    @Override
    public void onDestroy() {
        GiveawayListFragmentStack.removeFragment(this);
        super.onDestroy();
    }

    @NonNull
    @Override
    protected GiveawayAdapter createAdapter() {
        return new GiveawayAdapter(50, true, PreferenceManager.getDefaultSharedPreferences(getContext()));
    }

    @Override
    protected AsyncTask<Void, Void, ?> getFetchItemsTask(int page) {
        return new LoadGiveawayListTask(this, page, type, getSearchQuery(), PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("preference_giveaway_show_pinned", false));
    }

    /**
     * Returns the resource of what to show in the title.
     *
     * @return resource to show in the title
     */
    @Override
    public int getTitleResource() {
        return type.getTitleResource();
    }

    /**
     * Return extra content for a title.
     *
     * @return extra content (dynamic)
     */
    @Override
    public String getExtraTitle() {
        return null;
    }

    @Override
    public Type getType() {
        return type;
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
        if (success == Boolean.TRUE) {
            Giveaway giveaway = adapter.findItem(giveawayId);
            if (giveaway != null) {
                giveaway.setEntered(GiveawayDetailFragment.ENTRY_INSERT.equals(what));
                if (GiveawayDetailFragment.ENTRY_INSERT.equals(what) && FilterData.getCurrent(getContext()).isHideEntered()) {
                    // we want to hide entered giveaways
                    adapter.removeGiveaway(giveawayId);
                } else {
                    adapter.notifyItemChanged(giveaway);
                }
            }
        } else {
            Log.e(TAG, "Probably an error catching the result...");
        }

        if (propagate)
            GiveawayListFragmentStack.onEnterLeaveResult(giveawayId, what, success);
    }

    public void requestHideGame(int internalGameId, String title) {
        new UpdateGiveawayFilterTask<>(this, adapter.getXsrfToken(), UpdateGiveawayFilterTask.HIDE, internalGameId, title).execute();
    }

    @Override
    public void onHideGame(final int internalGameId, boolean propagate, final String gameTitle) {
        Log.v(TAG, "onHideGame/" + this.toString() + " ~~ " + propagate);
        if (propagate) {
            GiveawayListFragmentStack.onHideGame(internalGameId);
        } else {
            List<EndlessAdapter.RemovedElement> removedGiveaways = adapter.removeHiddenGame(internalGameId);
            lastRemovedGame = new LastRemovedGame(removedGiveaways, internalGameId);
        }

        if (gameTitle != null) {
            // If we're propagating, this means we're visible instance
            Snackbar.make(swipeContainer, String.format(getString(R.string.game_was_hidden), gameTitle), Snackbar.LENGTH_LONG).setAction(R.string.undo, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new UpdateGiveawayFilterTask<>(GiveawayListFragment.this, adapter.getXsrfToken(), UpdateGiveawayFilterTask.UNHIDE, internalGameId, gameTitle).execute();
                }
            }).show();
        }
    }

    public void onShowGame(int internalGameId, boolean propagate) {
        Log.v(TAG, "onShowGame/" + this + " ~~ " + propagate);
        if (propagate) {
            GiveawayListFragmentStack.onShowGame(internalGameId);
        } else if (lastRemovedGame != null) {
            if (lastRemovedGame.internalGameId == internalGameId) {
                adapter.restore(lastRemovedGame.removedGiveaways);
                lastRemovedGame = null;
            } else {
                Log.w(TAG, "onShowGame(" + internalGameId + ") expected " + lastRemovedGame.internalGameId + ", not restoring game(s)");
            }
        } else {
            Log.w(TAG, "onShowGame called without a lastRemovedGame");
        }
    }

    @Override
    public Fragment newSearchingInstance(String query) {
        return newInstance(type, query, false);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem filterMenu = menu.findItem(R.id.filter);
        filterMenu.setVisible(true);

        ActionItemBadge.update(getActivity(), filterMenu, getResources().getDrawable(R.drawable.ic_filter_variant), (BadgeStyle) null, FilterData.getCurrent(getContext()).isAnyActive() ? "\n\n{faw-check-circle}" : null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.filter) {
            FragmentManager fm = getActivity().getSupportFragmentManager();

            FilterGiveawayDialogFragment dialog = new FilterGiveawayDialogFragment();
            dialog.setListener(this);
            dialog.show(fm, dialog.getClass().getSimpleName());

            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

    // TODO This does not handle propagation up the call stack.
    @Override
    public void onFilterUpdated() {
        refresh();

        FragmentActivity activity = getActivity();
        if (activity != null)
            activity.supportInvalidateOptionsMenu();
    }

    @Override
    protected void refresh() {
        super.refresh();
        lastRemovedGame = null;
    }

    /**
     * Different types of Giveaway lists.
     */
    public enum Type {
        /**
         * All giveaways.
         */
        ALL(R.string.navigation_giveaways_all, R.string.navigation_giveaways_all_title),

        /**
         * Group giveaways.
         */
        GROUP(R.string.navigation_giveaways_group, R.string.navigation_giveaways_group_title),

        /**
         * Giveaways with games from your wishlist.
         */
        WISHLIST(R.string.navigation_giveaways_wishlist, R.string.navigation_giveaways_wishlist_title),

        /**
         * Recommended Giveaways.
         */
        RECOMMENDED(R.string.navigation_giveaways_recommended, R.string.navigation_giveaways_recommended_title),

        /**
         * New giveaways.
         */
        NEW(R.string.navigation_giveaways_new, R.string.navigation_giveaways_new_title);

        private final int titleResource;
        private final int navbarResource;

        Type(int navbarResource, int titleResource) {
            this.navbarResource = navbarResource;
            this.titleResource = titleResource;
        }

        public int getTitleResource() {
            return titleResource;
        }

        public int getNavbarResource() {
            return navbarResource;
        }
    }

    private static class LastRemovedGame implements Serializable {
        private static final long serialVersionUID = -7112241651196581480L;

        private final List<EndlessAdapter.RemovedElement> removedGiveaways;
        private final int internalGameId;

        private LastRemovedGame(List<EndlessAdapter.RemovedElement> removedGiveaways, int internalGameId) {
            this.removedGiveaways = removedGiveaways;
            this.internalGameId = internalGameId;
        }
    }
}
