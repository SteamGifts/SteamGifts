package net.mabako.steamgifts.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.activities.DetailActivity;
import net.mabako.steamgifts.activities.WriteCommentActivity;
import net.mabako.steamgifts.adapters.CommentAdapter;
import net.mabako.steamgifts.adapters.EndlessAdapter;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.data.BasicDiscussion;
import net.mabako.steamgifts.data.Comment;
import net.mabako.steamgifts.data.Discussion;
import net.mabako.steamgifts.data.DiscussionExtras;
import net.mabako.steamgifts.fragments.util.DiscussionDetailsCard;
import net.mabako.steamgifts.tasks.LoadDiscussionDetailsTask;

import java.util.ArrayList;

public class DiscussionDetailFragment extends Fragment implements ICommentableFragment {
    public static final String ARG_DISCUSSION = "discussion";
    private static final String TAG = DiscussionDetailFragment.class.getSimpleName();
    /**
     * Content to show for the giveaway details.
     */
    private BasicDiscussion discussion;
    private DiscussionDetailsCard discussionCard;
    private LoadDiscussionDetailsTask task;
    private RecyclerView listView;
    private CommentAdapter<DiscussionDetailFragment> adapter;

    public static Fragment newInstance(BasicDiscussion discussion) {
        DiscussionDetailFragment d = new DiscussionDetailFragment();
        d.discussion = discussion;
        return d;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_discussion_detail, container, false);

        discussionCard = new DiscussionDetailsCard();
        if (discussion instanceof Discussion) {
            onPostDiscussionLoaded((Discussion) discussion, true);
        } else {
            Log.d(TAG, "Loading activity for basic discussion " + discussion.getDiscussionId());
        }

        listView = (RecyclerView) layout.findViewById(R.id.list);
        listView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CommentAdapter<>(this, listView, new EndlessAdapter.OnLoadListener() {
            @Override
            public void onLoad(int page) {
                fetchItems(page);
            }
        });
        listView.setAdapter(adapter);

        // Add the cardview for the Giveaway details
        adapter.setStickyItem(discussionCard);

        fetchItems(1);
        setHasOptionsMenu(true);

        return layout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        task.cancel(true);
    }

    private void fetchItems(int page) {
        Log.d(TAG, "Fetching discussions on page " + page + " for discussion " + discussion.getDiscussionId());

        if (task != null)
            task.cancel(true);

        String url = discussion.getDiscussionId();
        if (discussion instanceof Discussion)
            url += "/" + ((Discussion) discussion).getName();

        task = new LoadDiscussionDetailsTask(this, url, page, !(discussion instanceof Discussion));
        task.execute();
    }

    public void onPostDiscussionLoaded(Discussion discussion, boolean ignoreExisting) {
        // Called this twice, eh...
        if (this.discussion instanceof Discussion && !ignoreExisting)
            return;

        this.discussion = discussion;
        discussionCard.setDiscussion(discussion);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(discussion.getTitle());
    }

    public void onPostDiscussionLoaded(Discussion discussion) {
        onPostDiscussionLoaded(discussion, false);
    }

    public void addDetails(DiscussionExtras extras, int page) {
        if (extras == null)
            return;

        discussionCard.setExtras(extras);
        adapter.setStickyItem(discussionCard);

        if (page == 1)
            adapter.clear();
        adapter.finishLoading(new ArrayList<IEndlessAdaptable>(extras.getComments()));
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