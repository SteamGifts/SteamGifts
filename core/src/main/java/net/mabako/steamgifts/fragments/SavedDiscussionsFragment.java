package net.mabako.steamgifts.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;

import net.mabako.steamgifts.adapters.DiscussionAdapter;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.fragments.interfaces.IActivityTitle;
import net.mabako.steamgifts.persistentdata.SavedDiscussions;

import java.io.Serializable;

public class SavedDiscussionsFragment extends ListFragment<DiscussionAdapter> implements IActivityTitle {
    private SavedDiscussions savedDiscussions;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter.setFragmentValues(this, getActivity());
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

    @NonNull
    @Override
    protected DiscussionAdapter createAdapter() {
        return new DiscussionAdapter();
    }

    @Override
    protected AsyncTask<Void, Void, ?> getFetchItemsTask(int page) {
        return null;
    }

    @Override
    protected Serializable getType() {
        return null;
    }

    @Override
    protected void fetchItems(int page) {
        if (page != 1)
            return;

        super.addItems(savedDiscussions.all(), true);
        adapter.reachedTheEnd();
    }

    @Override
    public int getTitleResource() {
        return R.string.saved_discussions_title;
    }

    @Override
    public String getExtraTitle() {
        return null;
    }
}
