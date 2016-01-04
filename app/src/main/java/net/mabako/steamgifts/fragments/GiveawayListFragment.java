package net.mabako.steamgifts.fragments;

import android.support.v7.widget.RecyclerView;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.adapters.EndlessAdapter;
import net.mabako.steamgifts.adapters.GiveawayAdapter;
import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.tasks.LoadGiveawayListTask;

/**
 * List of all giveaways.
 */
public class GiveawayListFragment extends ListFragment<GiveawayAdapter> implements IGiveawayUpdateNotification {
    private static final String TAG = GiveawayListFragment.class.getSimpleName();

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
    protected GiveawayAdapter createAdapter(RecyclerView listView) {
        return new GiveawayAdapter(getActivity(), listView, new EndlessAdapter.OnLoadListener() {
            @Override
            public void onLoad(int page) {
                fetchItems(page);
            }
        });
    }

    @Override
    protected void fetchItems(int page) {
        new LoadGiveawayListTask(this, page, type, searchQuery).execute();
    }

    /**
     * Callback for a giveaway's status being updated.
     *
     * @param giveawayId ID of the giveaway
     * @param entered    whether or not the giveaway is now entered.
     */
    @Override
    public void onUpdateGiveawayStatus(String giveawayId, boolean entered) {
        Giveaway giveaway = adapter.findItem(giveawayId);
        if (giveaway != null) {
            giveaway.setEntered(entered);
            adapter.notifyItemChanged(giveaway);
        }
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
    protected int getLayoutResource() {
        return R.layout.fragment_giveaway_list;
    }

    @Override
    public Type getType() {
        return type;
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
