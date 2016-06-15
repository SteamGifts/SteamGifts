package net.mabako.steamgifts.fragments.profile;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import net.mabako.steamgifts.activities.DetailActivity;
import net.mabako.steamgifts.activities.UrlHandlingActivity;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.adapters.MessageAdapter;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.Comment;
import net.mabako.steamgifts.fragments.ListFragment;
import net.mabako.steamgifts.fragments.UserDetailFragment;
import net.mabako.steamgifts.fragments.interfaces.IActivityTitle;
import net.mabako.steamgifts.fragments.interfaces.ICommentableFragment;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;
import net.mabako.steamgifts.receivers.CheckForNewMessages;
import net.mabako.steamgifts.tasks.LoadMessagesTask;
import net.mabako.steamgifts.tasks.MarkMessagesReadTask;

import java.io.Serializable;
import java.util.List;

public class MessageListFragment extends ListFragment<MessageAdapter> implements IActivityTitle, ICommentableFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter.setFragmentValues(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        setHasOptionsMenu(true);

        return view;
    }

    @NonNull
    @Override
    protected MessageAdapter createAdapter() {
        return new MessageAdapter();
    }

    @Override
    protected AsyncTask<Void, Void, ?> getFetchItemsTask(int page) {
        return new LoadMessagesTask(this, getContext(), page);
    }

    @Override
    protected Serializable getType() {
        return null;
    }

    @Override
    public int getTitleResource() {
        return R.string.user_tab_notifications;
    }

    @Override
    public String getExtraTitle() {
        return null;
    }

    @Override
    public void showProfile(String user) {
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.putExtra(UserDetailFragment.ARG_USER, user);
        getActivity().startActivity(intent);
    }

    @Override
    public void requestComment(Comment parentComment) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteComment(Comment comment) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canPostOrModifyComments() {
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.messages_menu, menu);

        menu.findItem(R.id.mark_read).setVisible(adapter.getXsrfToken() != null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.user) {
            showProfile(SteamGiftsUserData.getCurrent(getContext()).getName());
            return true;
        } else if (itemId == R.id.mark_read) {
            new MarkMessagesReadTask(this, adapter.getXsrfToken()).execute();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void addItems(List<? extends IEndlessAdaptable> items, boolean clearExistingItems, String foundXsrfToken) {
        super.addItems(items, clearExistingItems, foundXsrfToken);

        if (items != null && clearExistingItems) {
            // The top message for the first page (clearExistingItems == true) will be marked as the last we've actually looked at/dismissed and will not be shown in a notification again.
            for (IEndlessAdaptable item : items)
                if (item instanceof Comment) {
                    CheckForNewMessages.setLastDismissedCommentId(getContext(), ((Comment) item).getPermalinkId());
                    break;
                }
        }

        FragmentActivity activity = getActivity();
        if (activity != null)
            activity.supportInvalidateOptionsMenu();
    }

    public void onMarkedMessagesRead() {
        for (int i = 0, size = adapter.getItemCount(); i < size; ++i) {
            IEndlessAdaptable element = adapter.getItem(i);
            if (!(element instanceof Comment))
                continue;

            Comment comment = (Comment) element;

            if (comment.isHighlighted()) {
                comment.setHighlighted(false);
                adapter.notifyItemChanged(i);
            }
        }

        adapter.setXsrfToken(null);

        FragmentActivity activity = getActivity();
        if (activity != null)
            activity.supportInvalidateOptionsMenu();

        // We no longer have any notifications
        SteamGiftsUserData.getCurrent(getContext()).setMessageNotification(0);
    }

    public void navigateTo(@NonNull Comment comment) {
        getActivity().startActivity(UrlHandlingActivity.getPermalinkUri(getContext(), comment));
    }
}
