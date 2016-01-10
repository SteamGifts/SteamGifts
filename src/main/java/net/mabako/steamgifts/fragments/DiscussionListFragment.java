package net.mabako.steamgifts.fragments;

import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.adapters.DiscussionAdapter;
import net.mabako.steamgifts.adapters.EndlessAdapter;
import net.mabako.steamgifts.tasks.LoadDiscussionListTask;

/**
 * List of all discussions.
 */
public class DiscussionListFragment extends ListFragment<DiscussionAdapter> implements IFragmentNotifications {
    private static final String TAG = DiscussionListFragment.class.getSimpleName();

    /**
     * Type of items to show.
     */
    private Type type = Type.ALL;

    /**
     * What are we searching for?
     */
    private String searchQuery = null;

    public static Fragment newInstance(Type type, String query) {
        DiscussionListFragment f = new DiscussionListFragment();
        f.type = type;
        f.searchQuery = query;
        return f;
    }


    @Override
    protected DiscussionAdapter createAdapter(RecyclerView listView) {
        return new DiscussionAdapter(getActivity(), listView, new EndlessAdapter.OnLoadListener() {
            @Override
            public void onLoad(int page) {
                fetchItems(page);
            }
        });
    }

    @Override
    protected void fetchItems(int page) {
        new LoadDiscussionListTask(this, page, type, searchQuery).execute();
    }

    @Override
    public int getTitleResource() {
        return type.getTitleResource();
    }

    @Override
    public String getExtraTitle() {
        return searchQuery;
    }

    @Override
    public Type getType() {
        return type;
    }

    /**
     * Different types of discussion lists.
     */
    public enum Type {
        ALL(R.string.navigation_discussions_all, R.string.navigation_discussions_all_title, FontAwesome.Icon.faw_comments),
        ANNOUNCEMENTS(R.string.navigation_discussions_announcements, R.string.navigation_discussions_announcements_title, FontAwesome.Icon.faw_bullhorn),
        // BUGS_SUGGESTIONS(R.string.navigation_discussions_bugs_suggestions, R.string.navigation_discussions_bugs_suggestions_title),
        DEALS(R.string.navigation_discussions_deals, R.string.navigation_discussions_deals_title, FontAwesome.Icon.faw_usd),
        GENERAL(R.string.navigation_discussions_general, R.string.navigation_discussions_general_title, FontAwesome.Icon.faw_comments),
        GROUP_RECRUITMENT(R.string.navigation_discussions_group_recruitment, R.string.navigation_discussions_group_recruitment_title, FontAwesome.Icon.faw_users),
        LETS_PLAY_TOGETHER(R.string.navigation_discussions_lets_play_together, R.string.navigation_discussions_lets_play_together_title, FontAwesome.Icon.faw_play_circle),
        OFF_TOPIC(R.string.navigation_discussions_off_topic, R.string.navigation_discussions_off_topic_title, FontAwesome.Icon.faw_comments),
        PUZZLES(R.string.navigation_discussions_puzzles, R.string.navigation_discussions_puzzles_title, FontAwesome.Icon.faw_puzzle_piece);

        private final int titleResource;
        private final int navbarResource;
        private final FontAwesome.Icon icon;

        Type(int navbarResource, int titleResource, FontAwesome.Icon icon) {
            this.navbarResource = navbarResource;
            this.titleResource = titleResource;
            this.icon = icon;
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

        public FontAwesome.Icon getIcon() {
            return icon;
        }
    }
}
