package net.mabako.steamgifts.tasks;

import android.os.AsyncTask;
import android.util.Log;

import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.fragments.GiveawayListFragment;
import net.mabako.steamgifts.web.SteamGiftsUserData;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;

public class LoadGiveawayListTask extends AsyncTask<Void, Void, List<Giveaway>> {
    private static final String TAG = LoadGiveawayListTask.class.getSimpleName();

    private final GiveawayListFragment fragment;
    private final int page;
    private final GiveawayListFragment.Type type;
    private final String searchQuery;
    private final boolean showPinnedGiveaways;

    private String foundXsrfToken = null;

    public LoadGiveawayListTask(GiveawayListFragment activity, int page, GiveawayListFragment.Type type, String searchQuery, boolean showPinnedGiveaways) {
        this.fragment = activity;
        this.page = page;
        this.type = type;
        this.searchQuery = searchQuery;
        this.showPinnedGiveaways = showPinnedGiveaways && type == GiveawayListFragment.Type.ALL;
    }

    @Override
    protected List<Giveaway> doInBackground(Void... params) {
        Log.d(TAG, "Fetching giveaways for page " + page);

        try {
            // Fetch the Giveaway page

            Connection jsoup = Jsoup.connect("http://www.steamgifts.com/giveaways/search");
            jsoup.data("page", Integer.toString(page));

            if (searchQuery != null)
                jsoup.data("q", searchQuery);

            if (type != GiveawayListFragment.Type.ALL)
                jsoup.data("type", type.name().toLowerCase());

            if (SteamGiftsUserData.getCurrent().isLoggedIn())
                jsoup.cookie("PHPSESSID", SteamGiftsUserData.getCurrent().getSessionId());
            Document document = jsoup.get();

            SteamGiftsUserData.extract(document);

            // Fetch the xsrf token
            Element xsrfToken = document.select("input[name=xsrf_token]").first();
            if (xsrfToken != null)
                foundXsrfToken = xsrfToken.attr("value");

            // Do away with pinned giveaways.
            if (!showPinnedGiveaways)
                document.select(".pinned-giveaways__outer-wrap").html("");

            // Parse all rows of giveaways
            return Utils.loadGiveawaysFromList(document);
        } catch (Exception e) {
            Log.e(TAG, "Error fetching URL", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<Giveaway> result) {
        super.onPostExecute(result);
        fragment.addItems(result, page == 1, foundXsrfToken);
    }
}
