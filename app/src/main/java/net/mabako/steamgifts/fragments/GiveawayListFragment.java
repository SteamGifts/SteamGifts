package net.mabako.steamgifts.fragments;

import android.support.v7.widget.RecyclerView;
import android.util.Log;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.adapters.EndlessAdapter;
import net.mabako.steamgifts.adapters.GiveawayAdapter;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.tasks.EnterLeaveGiveawayTask;
import net.mabako.steamgifts.tasks.LoadGiveawayListTask;
import net.mabako.steamgifts.tasks.UpdateGiveawayFilterTask;

import java.util.List;

/**
 * List of all giveaways.
 */
public class GiveawayListFragment extends ListFragment<GiveawayAdapter> implements IHasEnterableGiveaways, IFragmentNotifications {
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
    public void onDestroyView() {
        super.onDestroyView();

        if (enterLeaveTask != null)
            enterLeaveTask.cancel(true);
    }

    @Override
    protected GiveawayAdapter createAdapter(RecyclerView listView) {
        return new GiveawayAdapter(getActivity(), listView, new EndlessAdapter.OnLoadListener() {
            @Override
            public void onLoad(int page) {
                fetchItems(page);
            }
        }, this, 50);
    }

    @Override
    protected void fetchItems(int page) {
        new LoadGiveawayListTask(this, page, type, searchQuery).execute();
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
    public void onEnterLeaveResult(String giveawayId, String what, Boolean success) {
        if (success == Boolean.TRUE) {
            Giveaway giveaway = adapter.findItem(giveawayId);
            if (giveaway != null) {
                giveaway.setEntered(GiveawayDetailFragment.ENTRY_INSERT.equals(what));
                adapter.notifyItemChanged(giveaway);
            }
        } else {
            Log.e(TAG, "Probably an error catching the result...");
        }
    }

    public void addItems(List<? extends IEndlessAdaptable> items, boolean clearExistingItems, String xsrfToken) {
        adapter.setXsrfToken(xsrfToken);
        addItems(items, clearExistingItems);
    }

    public void requestHideGame(int internalGameId) {
        new UpdateGiveawayFilterTask<>(this, adapter.getXsrfToken(), UpdateGiveawayFilterTask.HIDE, internalGameId).execute();
    }

    public void onHideGame(int internalGameId) {
        adapter.removeHiddenGame(internalGameId);
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
