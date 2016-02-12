package net.mabako.steamgifts.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.View;

import net.mabako.steamgifts.activities.DetailActivity;
import net.mabako.steamgifts.adapters.EndlessAdapter;
import net.mabako.steamgifts.adapters.UserAdapter;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.BasicUser;
import net.mabako.steamgifts.fragments.interfaces.IActivityTitle;
import net.mabako.steamgifts.fragments.interfaces.IHasWhitelistAndBlacklist;
import net.mabako.steamgifts.tasks.LoadWhitelistBlacklistTask;
import net.mabako.steamgifts.tasks.UpdateWhitelistBlacklistTask;

public class WhitelistBlacklistFragment extends SearchableListFragment<UserAdapter> implements IHasWhitelistAndBlacklist, IActivityTitle {
    public static final String ARG_TYPE = "whitelist-blacklist-type";

    private static final String SAVED_TYPE = "type";
    private static final String SAVED_LAST_REMOVED = "last-removed";

    private What type = What.WHITELIST;

    private UpdateWhitelistBlacklistTask updateWhitelistBlacklistTask;

    private EndlessAdapter.RemovedElement lastRemovedUser;

    public static WhitelistBlacklistFragment newInstance(What what, String query) {
        WhitelistBlacklistFragment fragment = new WhitelistBlacklistFragment();

        Bundle args = new Bundle();
        args.putSerializable(SAVED_TYPE, what);
        args.putString(SAVED_QUERY, query);
        fragment.setArguments(args);

        fragment.type = what;

        return fragment;
    }

    @Override
    public Fragment newSearchingInstance(String query) {
        return newInstance(type, query);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            type = (What) getArguments().getSerializable(SAVED_TYPE);
            lastRemovedUser = null;
        } else {
            type = (What) savedInstanceState.getSerializable(SAVED_TYPE);
            lastRemovedUser = (EndlessAdapter.RemovedElement) savedInstanceState.getSerializable(SAVED_LAST_REMOVED);
        }

        adapter.setFragmentValues(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SAVED_TYPE, type);
        outState.putSerializable(SAVED_LAST_REMOVED, lastRemovedUser);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (updateWhitelistBlacklistTask != null) {
            updateWhitelistBlacklistTask.cancel(true);
            updateWhitelistBlacklistTask = null;
        }
    }

    @NonNull
    @Override
    protected UserAdapter createAdapter() {
        return new UserAdapter();
    }

    @Override
    protected AsyncTask<Void, Void, ?> getFetchItemsTask(int page) {
        return new LoadWhitelistBlacklistTask(this, type, page, getSearchQuery());
    }

    @Override
    public What getType() {
        return type;
    }

    @Override
    public void requestUserListed(BasicUser user, What what, boolean adding) {
        if (updateWhitelistBlacklistTask != null)
            updateWhitelistBlacklistTask.cancel(true);

        updateWhitelistBlacklistTask = new UpdateWhitelistBlacklistTask(this, getContext(), adapter.getXsrfToken(), what, user, adding);
        updateWhitelistBlacklistTask.execute();
    }

    @Override
    public void onUserWhitelistOrBlacklistUpdated(BasicUser user, final What what, boolean added) {
        if (added) {
            if (lastRemovedUser != null && lastRemovedUser.getElement() instanceof BasicUser && user.getId() == ((BasicUser) lastRemovedUser.getElement()).getId()) {
                adapter.restore(lastRemovedUser);
                lastRemovedUser = null;
            }
        } else {
            // We removed someone!
            lastRemovedUser = adapter.removeUser(user.getId());
            if (lastRemovedUser != null) {
                Snackbar.make(swipeContainer, String.format(getString(R.string.user_whiteblacklist_removed), user.getName()), Snackbar.LENGTH_LONG).setAction(R.string.undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requestUserListed((BasicUser) lastRemovedUser.getElement(), what, true);
                    }
                }).show();
            }
        }
    }

    @Override
    public int getTitleResource() {
        return type == What.WHITELIST ? R.string.preference_sg_whitelist : R.string.preference_sg_blacklist;
    }

    @Override
    public String getExtraTitle() {
        return null;
    }
}
