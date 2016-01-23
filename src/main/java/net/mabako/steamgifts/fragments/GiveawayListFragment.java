package net.mabako.steamgifts.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.adapters.EndlessAdapter;
import net.mabako.steamgifts.adapters.GiveawayAdapter;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.fragments.interfaces.IActivityTitle;
import net.mabako.steamgifts.fragments.interfaces.IHasEnterableGiveaways;
import net.mabako.steamgifts.fragments.interfaces.IHasHideableGiveaways;
import net.mabako.steamgifts.fragments.util.GiveawayListFragmentStack;
import net.mabako.steamgifts.tasks.EnterLeaveGiveawayTask;
import net.mabako.steamgifts.tasks.LoadGiveawayListTask;
import net.mabako.steamgifts.tasks.UpdateGiveawayFilterTask;

import java.util.List;

/**
 * List of all giveaways.
 */
public class GiveawayListFragment extends SearchableListFragment<GiveawayAdapter> implements IHasEnterableGiveaways, IHasHideableGiveaways, IActivityTitle {
    private static final String TAG = GiveawayListFragment.class.getSimpleName();

    private EnterLeaveGiveawayTask enterLeaveTask;

    /**
     * Type of items to show.
     */
    private Type type = Type.ALL;

    public static GiveawayListFragment newInstance(Type type, String query) {
        GiveawayListFragment g = new GiveawayListFragment();
        g.type = type;
        g.searchQuery = query;
        return g;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        GiveawayListFragmentStack.addFragment(this);
        return view;
    }

    @Override
    public void onDestroyView() {
        GiveawayListFragmentStack.removeFragment(this);
        super.onDestroyView();

        if (enterLeaveTask != null)
            enterLeaveTask.cancel(true);
    }

    @Override
    protected GiveawayAdapter createAdapter() {
        return new GiveawayAdapter(getActivity(), new EndlessAdapter.OnLoadListener() {
            @Override
            public void onLoad(int page) {
                fetchItems(page);
            }
        }, this, 50);
    }

    @Override
    protected AsyncTask<Void, Void, ?> getFetchItemsTask(int page) {
        return new LoadGiveawayListTask(this, page, type, searchQuery);
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
        return searchQuery;
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

        enterLeaveTask = new EnterLeaveGiveawayTask(this, giveawayId, xsrfToken, what);
        enterLeaveTask.execute();
    }

    @Override
    public void onEnterLeaveResult(String giveawayId, String what, Boolean success, boolean propagate) {
        if (success == Boolean.TRUE) {
            Giveaway giveaway = adapter.findItem(giveawayId);
            if (giveaway != null) {
                giveaway.setEntered(GiveawayDetailFragment.ENTRY_INSERT.equals(what));
                adapter.notifyItemChanged(giveaway);
            }
        } else {
            Log.e(TAG, "Probably an error catching the result...");
        }

        if (propagate)
            GiveawayListFragmentStack.onEnterLeaveResult(giveawayId, what, success);
    }

    public void requestHideGame(int internalGameId) {
        new UpdateGiveawayFilterTask<>(this, adapter.getXsrfToken(), UpdateGiveawayFilterTask.HIDE, internalGameId).execute();
    }

    @Override
    public void onHideGame(int internalGameId, boolean propagate) {
        adapter.removeHiddenGame(internalGameId);

        if (propagate)
            GiveawayListFragmentStack.onHideGame(internalGameId);
    }

    @Override
    public Fragment newSearchingInstance(String query) {
        return newInstance(type, query);
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
         * New giveaways.
         */
        NEW(R.string.navigation_giveaways_new, R.string.navigation_giveaways_new_title);

        private final int titleResource;
        private final int navbarResource;

        Type(int navbarResource, int titleResource) {
            this.navbarResource = navbarResource;
            this.titleResource = titleResource;
        }

        public static Type find(int identifier) {
            for (Type t : values())
                if (identifier == t.getNavbarResource())
                    return t;

            throw new IllegalStateException();
        }

        public int getTitleResource() {
            return titleResource;
        }

        public int getNavbarResource() {
            return navbarResource;
        }
    }
}
