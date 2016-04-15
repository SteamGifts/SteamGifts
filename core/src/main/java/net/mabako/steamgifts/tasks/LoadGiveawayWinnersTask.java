package net.mabako.steamgifts.tasks;

import android.os.AsyncTask;
import android.util.Log;

import net.mabako.Constants;
import net.mabako.steamgifts.data.Winner;
import net.mabako.steamgifts.fragments.GiveawayWinnerListFragment;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class LoadGiveawayWinnersTask extends AsyncTask<Void, Void, List<Winner>> {
    private static final String TAG = LoadGiveawayGroupsTask.class.getSimpleName();

    private final GiveawayWinnerListFragment fragment;
    private final int page;
    private final String path;

    public LoadGiveawayWinnersTask(GiveawayWinnerListFragment fragment, int page, String path) {
        this.fragment = fragment;
        this.page = page;
        this.path = path;
    }

    @Override
    protected List<Winner> doInBackground(Void... params) {
        Log.d(TAG, "Fetching giveaways for page " + page);

        try {
            // Fetch the Giveaway page

            Connection jsoup = Jsoup.connect("https://www.steamgifts.com/giveaway/" + path + "/winners/search")
                    .userAgent(Constants.JSOUP_USER_AGENT)
                    .timeout(Constants.JSOUP_TIMEOUT);
            jsoup.data("page", Integer.toString(page));

            if (SteamGiftsUserData.getCurrent(fragment.getContext()).isLoggedIn())
                jsoup.cookie("PHPSESSID", SteamGiftsUserData.getCurrent(fragment.getContext()).getSessionId());
            Document document = jsoup.get();

            SteamGiftsUserData.extract(fragment.getContext(), document);

            return loadAll(document);
        } catch (IOException e) {
            Log.e(TAG, "Error fetching URL", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<Winner> result) {
        super.onPostExecute(result);
        fragment.addItems(result, page == 1);
    }

    private List<Winner> loadAll(Document document) {
        Elements users = document.select(".table__row-inner-wrap");
        List<Winner> userList = new ArrayList<>();

        for (Element element : users) {
            userList.add(load(element));
        }
        return userList;
    }

    private Winner load(Element element) {
        Winner user = new Winner();

        user.setName(element.select(".table__column__heading").text());
        user.setAvatar(Utils.extractAvatar(element.select(".global__image-inner-wrap").attr("style")));
        user.setStatus(element.select(".table__column--width-small.text-center").last().text());

        return user;
    }
}
