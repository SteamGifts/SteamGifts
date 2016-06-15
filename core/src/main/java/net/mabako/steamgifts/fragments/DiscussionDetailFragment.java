package net.mabako.steamgifts.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
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
import android.widget.Toast;

import net.mabako.steamgifts.activities.DetailActivity;
import net.mabako.steamgifts.adapters.EndlessAdapter;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.BasicDiscussion;
import net.mabako.steamgifts.data.Discussion;
import net.mabako.steamgifts.data.DiscussionExtras;
import net.mabako.steamgifts.data.Poll;
import net.mabako.steamgifts.fragments.interfaces.IHasPoll;
import net.mabako.steamgifts.fragments.util.DiscussionDetailsCard;
import net.mabako.steamgifts.persistentdata.SavedDiscussions;
import net.mabako.steamgifts.tasks.EnterLeavePollTask;
import net.mabako.steamgifts.tasks.LoadDiscussionDetailsTask;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DiscussionDetailFragment extends DetailFragment implements IHasPoll {
    public static final String ARG_DISCUSSION = "discussion";

    private static final String TAG = DiscussionDetailFragment.class.getSimpleName();

    private static final String SAVED_DISCUSSION = ARG_DISCUSSION;
    private static final String SAVED_CARD = "discussion-card";

    private SavedDiscussions savedDiscussions;

    /**
     * Content to show for the discussion details.
     */
    private BasicDiscussion discussion;
    private DiscussionDetailsCard discussionCard;

    private EnterLeavePollTask enterLeavePollTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            discussion = (BasicDiscussion) getArguments().getSerializable(SAVED_DISCUSSION);
            discussionCard = new DiscussionDetailsCard();
        } else {
            discussion = (BasicDiscussion) savedInstanceState.getSerializable(SAVED_DISCUSSION);
            discussionCard = (DiscussionDetailsCard) savedInstanceState.getSerializable(SAVED_CARD);
        }

        adapter.setFragmentValues(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SAVED_DISCUSSION, discussion);
        outState.putSerializable(SAVED_CARD, discussionCard);
    }

    public static Fragment newInstance(@NonNull BasicDiscussion discussion, @Nullable CommentContextInfo context) {
        DiscussionDetailFragment d = new DiscussionDetailFragment();

        Bundle args = new Bundle();
        args.putSerializable(SAVED_DISCUSSION, discussion);
        args.putSerializable(SAVED_COMMENT_CONTEXT, context);
        d.setArguments(args);

        return d;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = super.onCreateView(inflater, container, savedInstanceState);

        if (discussion instanceof Discussion) {
            onPostDiscussionLoaded((Discussion) discussion, true);
        } else {
            Log.d(TAG, "Loading activity for basic discussion " + discussion.getDiscussionId());
        }

        // Add the cardview for the Giveaway details
        adapter.setStickyItem(discussionCard);

        // To reverse or not to reverse?
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (prefs.getBoolean("preference_discussion_comments_reversed", false) && getCommentContext() == null) {
            adapter.setViewInReverse();
            fetchItems(EndlessAdapter.LAST_PAGE);
        } else {
            fetchItems(EndlessAdapter.FIRST_PAGE);
        }
        setHasOptionsMenu(true);

        return layout;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        savedDiscussions = new SavedDiscussions(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (savedDiscussions != null) {
            savedDiscussions.close();
            savedDiscussions = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (enterLeavePollTask != null) {
            enterLeavePollTask.cancel(true);
            enterLeavePollTask = null;
        }
    }

    @Override
    protected AsyncTask<Void, Void, ?> getFetchItemsTaskEx(int page) {
        String url = discussion.getDiscussionId();
        if (discussion instanceof Discussion)
            url += "/" + ((Discussion) discussion).getName();
        else if (getCommentContext() != null)
            url += "/" + getCommentContext().getDetailName();
        else
            url += "/sgforandroid";

        return new LoadDiscussionDetailsTask(this, url, page, !(discussion instanceof Discussion));
    }

    public void onPostDiscussionLoaded(Discussion discussion, boolean ignoreExisting) {
        // Called this twice, eh...
        if (this.discussion instanceof Discussion && !ignoreExisting)
            return;

        this.discussion = discussion;
        discussionCard.setDiscussion(discussion);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(getTitle());
            }
            activity.supportInvalidateOptionsMenu();
        }
    }

    public void onPostDiscussionLoaded(Discussion discussion) {
        onPostDiscussionLoaded(discussion, false);
    }

    public void addItems(DiscussionExtras extras, int page, boolean lastPage) {
        if (extras == null)
            return;

        if (!(discussion instanceof Discussion))
            throw new IllegalStateException("#onPostDiscussionLoaded was probably not called");
        ((Discussion) discussion).setPoll(extras.hasPoll());

        discussionCard.setExtras(extras);

        if (extras.hasPoll() && getCommentContext() == null) {
            List<IEndlessAdaptable> pollItems = new ArrayList<>();
            pollItems.add(discussionCard);

            pollItems.add(extras.getPoll().getHeader());
            pollItems.addAll(extras.getPoll().getAnswers());
            pollItems.add(new Poll.CommentSeparator());
            adapter.setStickyItems(pollItems);
        } else {
            adapter.setStickyItem(discussionCard);
        }

        adapter.notifyPage(getCommentContext() != null ? 1 : page, lastPage);
        addItems(extras.getComments(), false, extras.getXsrfToken());

        if (getActivity() != null)
            getActivity().supportInvalidateOptionsMenu();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.discussion_menu, menu);

        MenuItem commentMenu = menu.findItem(R.id.comment);
        if (discussion instanceof Discussion) {
            if (!((Discussion) discussion).isLocked()) {
                commentMenu.setVisible(true);
                commentMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        requestComment(null);

                        return true;
                    }
                });
            } else {
                MenuItem lockedMenu = menu.findItem(R.id.locked);

                lockedMenu.setVisible(true);
                lockedMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Toast.makeText(getContext(), R.string.discussion_locked, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
            }


            if (savedDiscussions != null) {
                boolean isSaved = savedDiscussions.exists(discussion.getDiscussionId());
                menu.findItem(R.id.add_saved_element).setVisible(!isSaved);
                menu.findItem(R.id.remove_saved_element).setVisible(isSaved);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.add_saved_element) {
            if (discussion instanceof Discussion && savedDiscussions.add((Discussion) discussion, discussion.getDiscussionId())) {
                getActivity().supportInvalidateOptionsMenu();
                Toast.makeText(getContext(), R.string.added_saved_discussion, Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (itemId == R.id.remove_saved_element) {
            if (discussion instanceof Discussion && savedDiscussions.remove(discussion.getDiscussionId())) {
                getActivity().supportInvalidateOptionsMenu();
                Toast.makeText(getContext(), R.string.removed_saved_discussion, Toast.LENGTH_SHORT).show();
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void showProfile(String user) {
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.putExtra(UserDetailFragment.ARG_USER, user);
        getActivity().startActivity(intent);
    }

    @NonNull
    @Override
    protected Serializable getDetailObject() {
        return discussion;
    }

    @Nullable
    @Override
    protected String getDetailPath() {
        if (discussion instanceof Discussion)
            return "discussion/" + discussion.getDiscussionId() + "/" + ((Discussion) discussion).getName();

        return null;
    }

    @Override
    protected String getTitle() {
        return discussion instanceof Discussion ? ((Discussion) discussion).getTitle() : null;
    }

    @Override
    public void selectPollAnswer(@NonNull Poll.Answer answer) {
        Poll poll = getPoll();
        if (poll == null)
            return;

        if (enterLeavePollTask != null)
            enterLeavePollTask.cancel(true);

        int currentAnswerId = poll.getSelectedAnswerId();
        Log.d(TAG, "entering poll " + currentAnswerId + ", " + answer.getId());

        // If we're selecting the same answer, remove the vote. If it's a different answer, insert the vote.
        enterLeavePollTask = new EnterLeavePollTask(this, getContext(), adapter.getXsrfToken(), currentAnswerId == answer.getId() ? EnterLeavePollTask.REMOVE_ANSWER : EnterLeavePollTask.SELECT_ANSWER, answer.getId());
        enterLeavePollTask.execute();
    }

    @Override
    public void onPollAnswerSelected(int answerId) {
        Poll poll = getPoll();
        if (poll == null)
            return;

        int currentAnswerId = poll.getSelectedAnswerId();
        Log.d(TAG, "poll answer selected -- " + currentAnswerId + ", " + answerId);

        // Update the currently selected answer.
        poll.setSelectedAnswerId(answerId);

        int totalVotes = poll.getTotalVotes();

        if (currentAnswerId != 0) {
            Poll.Answer answer = adapter.findPollAnswer(currentAnswerId);
            answer.setVoteCount(answer.getVoteCount() - 1);

            --totalVotes;
        }

        if (answerId != 0) {
            Poll.Answer answer = adapter.findPollAnswer(answerId);
            answer.setVoteCount(answer.getVoteCount() + 1);

            ++totalVotes;
        }

        poll.setTotalVotes(totalVotes);

        // Since we would probably have changed the total, recalculate the percentages.
        adapter.notifyItemRangeChanged(2, poll.getAnswers().size());
    }

    private Poll getPoll() {
        DiscussionExtras extras = discussionCard.getExtras();
        if (extras == null) {
            Log.d(TAG, "selectPollAnswer without extras");
            return null;
        }

        Poll poll = extras.getPoll();
        if (poll == null) {
            Log.d(TAG, "selectPollAnswer without poll");
            return null;
        }

        return poll;
    }
}