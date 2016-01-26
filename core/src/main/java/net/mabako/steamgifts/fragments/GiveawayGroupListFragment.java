package net.mabako.steamgifts.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;

import net.mabako.steamgifts.adapters.EndlessAdapter;
import net.mabako.steamgifts.adapters.GiveawayGroupAdapter;
import net.mabako.steamgifts.fragments.interfaces.IActivityTitle;
import net.mabako.steamgifts.tasks.LoadGiveawayGroupsTask;

import java.io.Serializable;

public class GiveawayGroupListFragment extends ListFragment<GiveawayGroupAdapter> implements IActivityTitle {
    private static final String SAVED_PATH = "path";
    private static final String SAVED_TITLE = "title";

    /**
     * Path to the giveaway
     */
    private String path;

    /**
     * Title of the giveaway
     */
    private String title;

    public static Fragment newInstance(String title, String path) {
        GiveawayGroupListFragment g = new GiveawayGroupListFragment();

        Bundle args = new Bundle();
        args.putString(SAVED_TITLE, title);
        args.putString(SAVED_PATH, path);
        g.setArguments(args);

        return g;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            title = getArguments().getString(SAVED_TITLE);
            path = getArguments().getString(SAVED_PATH);
        } else {
            title = savedInstanceState.getString(SAVED_TITLE);
            path = savedInstanceState.getString(SAVED_PATH);
        }
    }

    @Override
    protected GiveawayGroupAdapter createAdapter() {
        return new GiveawayGroupAdapter(getActivity(), new EndlessAdapter.OnLoadListener() {
            @Override
            public void onLoad(int page) {
                fetchItems(page);
            }
        });
    }

    @Override
    protected AsyncTask<Void, Void, ?> getFetchItemsTask(int page) {
        return new LoadGiveawayGroupsTask(this, page, path);
    }

    @Override
    protected Serializable getType() {
        return null;
    }

    @Override
    public int getTitleResource() {
        return 0;
    }

    @Override
    public String getExtraTitle() {
        return title;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // do nothing
    }
}
