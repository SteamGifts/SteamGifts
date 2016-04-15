package net.mabako.steamgifts.tasks;

import android.os.AsyncTask;
import android.util.Log;

import net.mabako.Constants;
import net.mabako.steamgifts.data.BasicUser;
import net.mabako.steamgifts.fragments.WhitelistBlacklistFragment;
import net.mabako.steamgifts.fragments.interfaces.IHasWhitelistAndBlacklist;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LoadWhitelistBlacklistTask extends AsyncTask<Void, Void, List<BasicUser>> {
    private static final String TAG = "LoadWhitelistBlacklistT";

    private final WhitelistBlacklistFragment fragment;
    private final int page;
    private final IHasWhitelistAndBlacklist.What what;
    private final String searchQuery;

    private String foundXsrfToken;

    public LoadWhitelistBlacklistTask(WhitelistBlacklistFragment fragment, IHasWhitelistAndBlacklist.What what, int page, String searchQuery) {
        this.fragment = fragment;
        this.page = page;
        this.what = what;
        this.searchQuery = searchQuery;
    }

    @Override
    protected List<BasicUser> doInBackground(Void... params) {
        try {
            // Fetch the Giveaway page
            String url = "https://www.steamgifts.com/account/manage/" + what.name().toLowerCase(Locale.ENGLISH) + "/search";
            Log.d(TAG, "Fetching URL " + url);

            Connection jsoup = Jsoup.connect(url)
                    .userAgent(Constants.JSOUP_USER_AGENT)
                    .timeout(Constants.JSOUP_TIMEOUT)
                    .followRedirects(false);
            jsoup.data("page", Integer.toString(page));

            if (searchQuery != null)
                jsoup.data("q", searchQuery);

            jsoup.cookie("PHPSESSID", SteamGiftsUserData.getCurrent(fragment.getContext()).getSessionId());

            Document document = jsoup.get();

            SteamGiftsUserData.extract(fragment.getContext(), document);

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
    protected void onPostExecute(List<BasicUser> users) {
        super.onPostExecute(users);
        fragment.addItems(users, page == 1, foundXsrfToken);
    }

    private List<BasicUser> loadAll(Document document) {
        Elements users = document.select(".table__row-inner-wrap");
        List<BasicUser> userList = new ArrayList<>();

        for (Element element : users) {
            userList.add(load(element));
        }
        return userList;
    }

    private BasicUser load(Element element) {
        BasicUser user = new BasicUser();

        user.setName(element.select(".table__column__heading").text());
        user.setAvatar(Utils.extractAvatar(element.select(".global__image-inner-wrap").attr("style")));
        user.setId(Integer.parseInt(element.select("input[name=child_user_id]").first().attr("value")));

        return user;
    }
}
