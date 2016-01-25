package net.mabako.steamgifts.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.activities.DetailActivity;
import net.mabako.steamgifts.activities.WriteCommentActivity;
import net.mabako.steamgifts.adapters.CommentAdapter;
import net.mabako.steamgifts.adapters.EndlessAdapter;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.data.BasicDiscussion;
import net.mabako.steamgifts.data.Comment;
import net.mabako.steamgifts.data.Discussion;
import net.mabako.steamgifts.data.DiscussionExtras;
import net.mabako.steamgifts.fragments.interfaces.ICommentableFragment;
import net.mabako.steamgifts.fragments.util.DiscussionDetailsCard;
import net.mabako.steamgifts.tasks.LoadDiscussionDetailsTask;

import java.io.Serializable;
import java.util.List;

public class DiscussionDetailFragment extends ListFragment<CommentAdapter> implements ICommentableFragment {
    public static final String ARG_DISCUSSION = "discussion";
    private static final String TAG = DiscussionDetailFragment.class.getSimpleName();
    /**
     * Content to show for the giveaway details.
     */
    private BasicDiscussion discussion;
    private DiscussionDetailsCard discussionCard;

    public static Fragment newInstance(BasicDiscussion discussion) {
        DiscussionDetailFragment d = new DiscussionDetailFragment();
        d.discussion = discussion;
        return d;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = super.onCreateView(inflater, container, savedInstanceState);

        discussionCard = new DiscussionDetailsCard();
        if (discussion instanceof Discussion) {
            onPostDiscussionLoaded((Discussion) discussion, true);
        } else {
            Log.d(TAG, "Loading activity for basic discussion " + discussion.getDiscussionId());
        }

        // Add the cardview for the Giveaway details
        adapter.setStickyItem(discussionCard);

        // To reverse or not to reverse?
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (prefs.getBoolean("preference_discussion_comments_reversed", false)) {
            adapter.setViewInReverse();
            fetchItems(EndlessAdapter.LAST_PAGE);
        } else {
            fetchItems(EndlessAdapter.FIRST_PAGE);
        }
        setHasOptionsMenu(true);

        return layout;
    }

    @Override
    protected CommentAdapter createAdapter() {
        return new CommentAdapter<>(this, new EndlessAdapter.OnLoadListener() {
            @Override
            public void onLoad(int page) {
                fetchItems(page);
            }
        });
    }

    @Override
    protected AsyncTask<Void, Void, ?> getFetchItemsTask(int page) {
        String url = discussion.getDiscussionId();
        if (discussion instanceof Discussion)
            url += "/" + ((Discussion) discussion).getName();
        else
            url += "/sgforandroid";

        return new LoadDiscussionDetailsTask(this, url, page, !(discussion instanceof Discussion));
    }

    @Override
    protected Serializable getType() {
        return null;
    }

    public void onPostDiscussionLoaded(Discussion discussion, boolean ignoreExisting) {
        // Called this twice, eh...
        if (this.discussion instanceof Discussion && !ignoreExisting)
            return;

        this.discussion = discussion;
        discussionCard.setDiscussion(discussion);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.supportInvalidateOptionsMenu();

            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(discussion.getTitle());
            }
        }
    }

    public void onPostDiscussionLoaded(Discussion discussion) {
        onPostDiscussionLoaded(discussion, false);
    }

    public void addItems(DiscussionExtras extras, int page, boolean lastPage) {
        if (extras == null)
            return;

        discussionCard.setExtras(extras);
        adapter.setStickyItem(discussionCard);

        adapter.notifyPage(page, lastPage);
        super.addItems(extras.getComments(), false);
    }

    @Override
    public void addItems(List<? extends IEndlessAdaptable> items, boolean clearExistingItems) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addItems(List<? extends IEndlessAdaptable> items, boolean clearExistingItems, String xsrfToken) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.discussion_menu, menu);

        MenuItem commentMenu = menu.findItem(R.id.comment);
        commentMenu.setVisible(discussion instanceof Discussion);
        commentMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                requestComment(null);
                return true;
            }
        });
    }

    @Override
    public void showProfile(String user) {
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.putExtra(UserDetailFragment.ARG_USER, user);
        getActivity().startActivity(intent);
    }

    @Override
    public void requestComment(Comment parentComment) {
        if (discussion instanceof Discussion) {
            Intent intent = new Intent(getActivity(), WriteCommentActivity.class);
            intent.putExtra(WriteCommentActivity.XSRF_TOKEN, discussionCard.getExtras().getXsrfToken());
            intent.putExtra(WriteCommentActivity.PATH, "discussion/" + discussion.getDiscussionId() + "/" + ((Discussion) discussion).getName());
            intent.putExtra(WriteCommentActivity.PARENT, parentComment);
            intent.putExtra(WriteCommentActivity.TITLE, ((Discussion) discussion).getTitle());
            getActivity().startActivityForResult(intent, WriteCommentActivity.REQUEST_COMMENT);
        } else
            throw new IllegalStateException("Commenting on a not fully loaded Giveaway");
    }
}