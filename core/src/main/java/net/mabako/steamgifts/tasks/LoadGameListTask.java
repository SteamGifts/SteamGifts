package net.mabako.steamgifts.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import net.mabako.Constants;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.fragments.interfaces.ILoadItemsListener;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

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
public abstract class LoadGameListTask extends AsyncTask<Void, Void, List<IEndlessAdaptable>> {
    private static final String TAG = LoadGiveawayListTask.class.getSimpleName();

    private final ILoadItemsListener listener;
    private final Context context;
    private final int page;
    private final String searchQuery;
    private final String pathSegment;
    private String foundXsrfToken;

    public LoadGameListTask(ILoadItemsListener listener, Context context, String pathSegment, int page, String searchQuery) {
        this.listener = listener;
        this.context = context;
        this.pathSegment = pathSegment;
        this.page = page;
        this.searchQuery = searchQuery;
    }

    @Override
    protected List<IEndlessAdaptable> doInBackground(Void... params) {
        try {
            // Fetch the Giveaway page

            Connection jsoup = Jsoup.connect("https://www.steamgifts.com/" + pathSegment + "/search")
                    .userAgent(Constants.JSOUP_USER_AGENT)
                    .timeout(Constants.JSOUP_TIMEOUT);
            jsoup.data("page", Integer.toString(page));

            if (searchQuery != null)
                jsoup.data("q", searchQuery);

            jsoup.cookie("PHPSESSID", SteamGiftsUserData.getCurrent(context).getSessionId());

            Document document = jsoup.get();

            SteamGiftsUserData.extract(context, document);

            // Fetch the xsrf token
            Element xsrfToken = document.select("input[name=xsrf_token]").first();
            if (xsrfToken != null)
                foundXsrfToken = xsrfToken.attr("value");

            // Do away with pinned giveaways.
            document.select(".pinned-giveaways__outer-wrap").html("");

            // Parse all rows of giveaways
            return loadAll(document);
        } catch (Exception e) {
            Log.e(TAG, "Error fetching URL", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<IEndlessAdaptable> result) {
        super.onPostExecute(result);
        listener.addItems(result, page == 1, foundXsrfToken);
    }

    private List<IEndlessAdaptable> loadAll(Document document) {
        Elements games = document.select(".table__row-inner-wrap");
        List<IEndlessAdaptable> gameList = new ArrayList<>();

        for (Element element : games) {
            gameList.add(load(element));
        }
        return gameList;
    }

    protected abstract IEndlessAdaptable load(Element element);

    public int getPage() {
        return page;
    }
}
