package net.mabako.steamgifts.tasks;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import net.mabako.steamgifts.data.Game;
import net.mabako.steamgifts.fragments.HiddenGamesFragment;
import net.mabako.steamgifts.web.SteamGiftsUserData;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Loads all games you have currently filtered.
 */
public class LoadHiddenGamesTask extends AsyncTask<Void, Void, List<Game>> {
    private static final String TAG = LoadGiveawayListTask.class.getSimpleName();

    private final HiddenGamesFragment fragment;
    private final int page;
    private final String searchQuery;
    private String foundXsrfToken;

    public LoadHiddenGamesTask(HiddenGamesFragment fragment, int page, String searchQuery) {
        this.fragment = fragment;
        this.page = page;
        this.searchQuery = searchQuery;
    }

    @Override
    protected List<Game> doInBackground(Void... params) {
        try {
            // Fetch the Giveaway page

            Connection jsoup = Jsoup.connect("http://www.steamgifts.com/account/settings/giveaways/filters/search");
            jsoup.data("page", Integer.toString(page));

            if (searchQuery != null)
                jsoup.data("q", searchQuery);

            jsoup.cookie("PHPSESSID", SteamGiftsUserData.getCurrent().getSessionId());

            Document document = jsoup.get();

            SteamGiftsUserData.extract(document);

            // Fetch the xsrf token
            Element xsrfToken = document.select("input[name=xsrf_token]").first();
            if (xsrfToken != null)
                foundXsrfToken = xsrfToken.attr("value");

            // Do away with pinned giveaways.
            document.select(".pinned-giveaways__outer-wrap").html("");

            // Parse all rows of giveaways
            return loadGames(document);
        } catch (Exception e) {
            Log.e(TAG, "Error fetching URL", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<Game> result) {
        super.onPostExecute(result);
        fragment.addItems(result, page == 1, foundXsrfToken);
    }

    private List<Game> loadGames(Document document) {
        Elements games = document.select(".table__row-inner-wrap");
        List<Game> gameList = new ArrayList<>();

        for (Element element : games) {
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

            gameList.add(game);
        }
        return gameList;
    }
}
