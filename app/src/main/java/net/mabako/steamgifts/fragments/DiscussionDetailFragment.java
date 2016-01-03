package net.mabako.steamgifts.fragments;

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
import net.mabako.steamgifts.adapters.CommentAdapter;
import net.mabako.steamgifts.adapters.EndlessAdapter;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.data.Discussion;
import net.mabako.steamgifts.data.DiscussionExtras;
import net.mabako.steamgifts.fragments.util.DiscussionDetailsCard;
import net.mabako.steamgifts.tasks.LoadDiscussionDetailsTask;

import java.util.ArrayList;

public class DiscussionDetailFragment extends Fragment {
    public static final String ARG_DISCUSSION = "discussion";
    private static final String TAG = DiscussionDetailFragment.class.getSimpleName();
    /**
     * Content to show for the giveaway details.
     */
    private Discussion discussion;
    private DiscussionDetailsCard discussionCard;
    private LoadDiscussionDetailsTask task;
    private RecyclerView listView;
    private CommentAdapter<DiscussionDetailFragment> adapter;

    public static Fragment newInstance(Discussion discussion) {
        DiscussionDetailFragment d = new DiscussionDetailFragment();
        d.discussion = discussion;
        return d;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_discussion_detail, container, false);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(discussion.getTitle());

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
        discussionCard = new DiscussionDetailsCard(discussion);
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

        task = new LoadDiscussionDetailsTask(this, discussion.getDiscussionId() + "/" + discussion.getName(), page);
        task.execute();
    }

    public void addDetails(DiscussionExtras extras, int page) {
        if(extras == null)
            return;

        discussionCard.setExtras(extras);
        adapter.setStickyItem(discussionCard);

        if (page == 1)
            adapter.clear();
        adapter.finishLoading(new ArrayList<IEndlessAdaptable>(extras.getComments()));
    }
}