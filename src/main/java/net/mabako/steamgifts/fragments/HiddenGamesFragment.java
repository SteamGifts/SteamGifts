package net.mabako.steamgifts.fragments;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.View;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.adapters.EndlessAdapter;
import net.mabako.steamgifts.adapters.HiddenGamesAdapter;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.data.Game;
import net.mabako.steamgifts.fragments.interfaces.IActivityTitle;
import net.mabako.steamgifts.tasks.LoadGameListTask;
import net.mabako.steamgifts.tasks.UpdateGiveawayFilterTask;

import org.jsoup.nodes.Element;

import java.io.Serializable;
import java.util.List;

public class HiddenGamesFragment extends SearchableListFragment<HiddenGamesAdapter> implements IActivityTitle {
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
    protected HiddenGamesAdapter createAdapter() {
        return new HiddenGamesAdapter(this, new EndlessAdapter.OnLoadListener() {
            @Override
            public void onLoad(int page) {
                fetchItems(page);
            }
        });
    }

    @Override
    protected AsyncTask<Void, Void, ?> getFetchItemsTask(int page) {
        return new LoadGameListTask(this, "account/settings/giveaways/filters", page, searchQuery) {
            @Override
            protected Game load(Element element) {
                Game game = new Game();
                game.setName(element.select(".table__column__heading").text());
                game.setInternalGameId(Integer.parseInt(element.select("input[name=game_id]").first().attr("value")));

                Element link = element.select(".table__column--width-fill .table__column__secondary-link").first();
                if (link != null) {
                    Uri steamUri = Uri.parse(link.attr("href"));

                    // Steam link
                    if (steamUri != null) {
                        List<String> pathSegments = steamUri.getPathSegments();
                        if (pathSegments.size() >= 2)
                            game.setGameId(Integer.parseInt(pathSegments.get(1)));
                        game.setType("app".equals(pathSegments.get(0)) ? Game.Type.APP : Game.Type.SUB);
                    }
                }

                return game;
            }
        };
    }

    @Override
    public Fragment newSearchingInstance(String query) {
        return newInstance(query);
    }

    public void requestShowGame(int internalGameId, String title) {
        new UpdateGiveawayFilterTask<>(this, adapter.getXsrfToken(), UpdateGiveawayFilterTask.UNHIDE, internalGameId, title).execute();
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

    @Override
    public void addItems(List<? extends IEndlessAdaptable> result, boolean clearExistingItems, String xsrfToken) {
        super.addItems(result, clearExistingItems, xsrfToken);

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
