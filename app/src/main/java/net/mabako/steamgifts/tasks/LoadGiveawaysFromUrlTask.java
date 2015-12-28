package net.mabako.steamgifts.tasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import net.mabako.steamgifts.activities.MainActivity;
import net.mabako.steamgifts.data.Giveaway;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mabako on 28.12.2015.
 */
public class LoadGiveawaysFromUrlTask extends AsyncTask<Void, Void, List<Giveaway>> {
    private static final String TAG = LoadGiveawaysFromUrlTask.class.getSimpleName();

    private MainActivity activity;
    private int page;

    public LoadGiveawaysFromUrlTask(MainActivity activity, int page) {
        this.activity = activity;
        this.page = page;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected List<Giveaway> doInBackground(Void... params) {
        Log.d(TAG, "Fetching giveaways for page " + page);

        try {
            Document document = Jsoup.connect("http://www.steamgifts.com/giveaways/search?page=" + page).get();
            Log.d(TAG, document.title());

            Elements giveaways = document.select(".giveaway__row-inner-wrap");
            Log.d(TAG, "Found inner " + giveaways.size() + " elements");

            List<Giveaway> giveawayList = new ArrayList<Giveaway>();
            for(Element element : giveaways)
            {
                Element link = element.select("h2 a").first();
                Element icon = element.select("h2 a").last();

                String title = link.text();
                String giveawayLink = link.attr("href").substring(10, 15);
                int gameId = Integer.parseInt(icon.attr("href").split("/")[4]);

                Elements links = element.select(".giveaway__links a span");
                int entries = Integer.parseInt(links.first().text().split(" ")[0].replace(",", ""));
                int comments = Integer.parseInt(links.last().text().split(" ")[0].replace(",", ""));

                String creator = element.select(".giveaway__username").text();

                Elements hints = element.select(".giveaway__heading__thin");
                String copiesT = hints.first().text();
                String pointsT = hints.last().text();
                int copies = hints.size() == 1 ? 1 : Integer.parseInt(copiesT.replace("(", "").replace(" Copies)", ""));
                int points = Integer.parseInt(pointsT.replace("(", "").replace("P)", ""));

                Log.d(TAG, "GIVEAWAY for " + title + ", " + giveawayLink + ", " + gameId);

                giveawayList.add(new Giveaway(title, giveawayLink, gameId, creator, entries, comments, copies, points));
            }

            return giveawayList;
        } catch (Exception e) {
            Log.d(TAG, "Error fetching URL", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<Giveaway> result) {
        super.onPostExecute(result);
        activity.addGiveaways(result, page == 1);
    }
}
