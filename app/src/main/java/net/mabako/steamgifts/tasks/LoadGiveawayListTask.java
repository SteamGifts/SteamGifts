package net.mabako.steamgifts.tasks;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.fragments.GiveawayListFragment;
import net.mabako.steamgifts.web.WebUserData;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class LoadGiveawayListTask extends AsyncTask<Void, Void, List<Giveaway>> {
    private static final String TAG = LoadGiveawayListTask.class.getSimpleName();

    private final GiveawayListFragment fragment;
    private final int page;
    private final GiveawayListFragment.Type type;
    private final String searchQuery;

    private String foundXsrfToken = null;

    public LoadGiveawayListTask(GiveawayListFragment activity, int page, GiveawayListFragment.Type type, String searchQuery) {
        this.fragment = activity;
        this.page = page;
        this.type = type;
        this.searchQuery = searchQuery;
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

            if (WebUserData.getCurrent().isLoggedIn())
                jsoup.cookie("PHPSESSID", WebUserData.getCurrent().getSessionId());
            Document document = jsoup.get();

            WebUserData.extract(document);

            // Fetch the xsrf token
            Element xsrfToken = document.select("input[name=xsrf_token]").first();
            if (xsrfToken != null)
                foundXsrfToken = xsrfToken.attr("value");

            // Do away with pinned giveaways.
            document.select(".pinned-giveaways__outer-wrap").html("");

            // Parse all rows of giveaways
            Elements giveaways = document.select(".giveaway__row-inner-wrap");
            Log.d(TAG, "Found inner " + giveaways.size() + " elements");

            List<Giveaway> giveawayList = new ArrayList<>();
            for (Element element : giveaways) {
                // Basic information
                Element link = element.select("h2 a").first();
                Uri linkUri = Uri.parse(link.attr("href"));
                String giveawayLink = linkUri.getPathSegments().get(1);
                String giveawayName = linkUri.getPathSegments().get(2);

                Giveaway giveaway = new Giveaway(giveawayLink);
                giveaway.setTitle(link.text());
                giveaway.setName(giveawayName);

                giveaway.setCreator(element.select(".giveaway__username").text());

                // Entries, would usually have comment count too... but we don't display that anywhere.
                Elements links = element.select(".giveaway__links a span");
                giveaway.setEntries(Integer.parseInt(links.first().text().split(" ")[0].replace(",", "")));

                giveaway.setEntered(element.hasClass("is-faded"));

                // More details
                Element icon = element.select("h2 a").last();
                Uri uriIcon = icon == link ? null : Uri.parse(icon.attr("href"));

                Utils.loadGiveaway(giveaway, element, "giveaway", "giveaway__heading__thin", uriIcon);
                giveawayList.add(giveaway);
            }

            return giveawayList;
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
