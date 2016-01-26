package net.mabako.steamgifts.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;

import net.mabako.steamgifts.adapters.DiscussionAdapter;
import net.mabako.steamgifts.adapters.EndlessAdapter;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.fragments.interfaces.IActivityTitle;
import net.mabako.steamgifts.tasks.LoadDiscussionListTask;

/**
 * List of all discussions.
 */
public class DiscussionListFragment extends SearchableListFragment<DiscussionAdapter> implements IActivityTitle {
    private static final String SAVED_TYPE = "type";

    /**
     * Type of items to show.
     */
    private Type type = Type.ALL;

    public static Fragment newInstance(Type type, String query) {
        DiscussionListFragment f = new DiscussionListFragment();

        Bundle args = new Bundle();
        args.putSerializable(SAVED_TYPE, type);
        args.putSerializable(SAVED_QUERY, query);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            type = (Type) getArguments().getSerializable(SAVED_TYPE);
        } else {
            type = (Type) savedInstanceState.getSerializable(SAVED_TYPE);
        }

        adapter.setFragmentValues(this, getActivity());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SAVED_TYPE, type);
    }

    @Override
    protected DiscussionAdapter createAdapter() {
        return new DiscussionAdapter();
    }

    @Override
    protected AsyncTask<Void, Void, ?> getFetchItemsTask(int page) {
        return new LoadDiscussionListTask(this, page, type, getSearchQuery());
    }

    @Override
    public int getTitleResource() {
        return type.getTitleResource();
    }

    @Override
    public String getExtraTitle() {
        return getSearchQuery();
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Fragment newSearchingInstance(String query) {
        return newInstance(type, query);
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
