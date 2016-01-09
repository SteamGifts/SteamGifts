package net.mabako.steamgifts.fragments;

import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.adapters.DiscussionAdapter;
import net.mabako.steamgifts.adapters.EndlessAdapter;
import net.mabako.steamgifts.adapters.GiveawayGroupAdapter;
import net.mabako.steamgifts.tasks.LoadGiveawayGroupsTask;

import java.io.Serializable;

public class GiveawayGroupListFragment extends ListFragment<GiveawayGroupAdapter> implements IFragmentNotifications {
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
    protected GiveawayGroupAdapter createAdapter(RecyclerView listView) {
        return new GiveawayGroupAdapter(getActivity(), listView, new EndlessAdapter.OnLoadListener() {
            @Override
            public void onLoad(int page) {
                fetchItems(page);
            }
        });
    }

    @Override
    protected void fetchItems(int page) {
        new LoadGiveawayGroupsTask(this, page, path).execute();
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
