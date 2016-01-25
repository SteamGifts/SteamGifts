package net.mabako.steamgifts.fragments;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;

import net.mabako.steamgifts.adapters.EndlessAdapter;
import net.mabako.steamgifts.adapters.GiveawayGroupAdapter;
import net.mabako.steamgifts.fragments.interfaces.IActivityTitle;
import net.mabako.steamgifts.tasks.LoadGiveawayGroupsTask;

import java.io.Serializable;

public class GiveawayGroupListFragment extends ListFragment<GiveawayGroupAdapter> implements IActivityTitle {
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
        g.path = path;
        g.title = title;
        return g;
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
