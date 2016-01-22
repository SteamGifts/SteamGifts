package net.mabako.steamgifts.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.activities.settings.ViewHiddenGamesActivity;
import net.mabako.steamgifts.adapters.EndlessAdapter;
import net.mabako.steamgifts.adapters.HiddenGamesAdapter;
import net.mabako.steamgifts.data.Game;
import net.mabako.steamgifts.fragments.interfaces.IActivityTitle;
import net.mabako.steamgifts.tasks.LoadHiddenGamesTask;
import net.mabako.steamgifts.tasks.UpdateGiveawayFilterTask;

import java.io.Serializable;
import java.util.List;

public class HiddenGamesFragment extends ListFragment<HiddenGamesAdapter> implements IActivityTitle {
    /**
     * Snack is only shown if the app is restarted.
     */
    private static boolean showSnack = true;

    public static HiddenGamesFragment newInstance(String query) {
        HiddenGamesFragment fragment = new HiddenGamesFragment();
        fragment.searchQuery = query;
        return fragment;
    }

    @Override
    protected HiddenGamesAdapter createAdapter(RecyclerView listView) {
        return new HiddenGamesAdapter(listView, this, new EndlessAdapter.OnLoadListener() {
            @Override
            public void onLoad(int page) {
                fetchItems(page);
            }
        });
    }

    @Override
    protected AsyncTask<Void, Void, ?> getFetchItemsTask(int page) {
        return new LoadHiddenGamesTask(this, page, searchQuery);
    }

    @Override
    protected void startActivityForQuery(String query) {

        Intent intent = new Intent(getActivity(), ViewHiddenGamesActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(ViewHiddenGamesActivity.ARG_QUERY, query);
        intent.putExtras(bundle);

        getActivity().startActivity(intent);
        if (searchQuery != null && !searchQuery.isEmpty())
            getActivity().finish();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        return view;
    }

    public void requestShowGame(int internalGameId) {
        new UpdateGiveawayFilterTask<>(this, adapter.getXsrfToken(), UpdateGiveawayFilterTask.UNHIDE, internalGameId).execute();
    }

    public void onShowGame(int internalGameId) {
        adapter.removeShownGame(internalGameId);
    }

    @Override
    protected Serializable getType() {
        return null;
    }

    @Override
    public int getTitleResource() {
        return R.string.preference_sg_hidden_games;
    }

    @Override
    public String getExtraTitle() {
        return searchQuery;
    }

    public void addItems(List<Game> result, boolean clearExistingItems, String xsrfToken) {
        addItems(result, clearExistingItems);
        adapter.setXsrfToken(xsrfToken);

        if (showSnack) {
            showSnack = false;
            Snackbar.make(getView().findViewById(R.id.list), R.string.hidden_games_snack, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.hidden_games_snack_dismiss, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // we don't really need anything here for it to be dismissable.
                        }
                    }).show();
        }
    }
}
