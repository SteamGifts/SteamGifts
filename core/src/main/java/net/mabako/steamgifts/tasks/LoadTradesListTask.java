package net.mabako.steamgifts.tasks;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import net.mabako.Constants;
import net.mabako.steamgifts.data.Trade;
import net.mabako.steamgifts.fragments.TradeListFragment;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LoadTradesListTask extends AsyncTask<Void, Void, List<Trade>> {
    private static final String TAG = LoadTradesListTask.class.getSimpleName();

    private final TradeListFragment fragment;
    private final int page;
    private final TradeListFragment.Type type;
    private final String searchQuery;

    public LoadTradesListTask(TradeListFragment fragment, int page, TradeListFragment.Type type, String searchQuery) {
        this.fragment = fragment;
        this.page = page;
        this.type = type;
        this.searchQuery = searchQuery;
    }

    @Override
    protected List<Trade> doInBackground(Void... params) {
        try {
            // Fetch the Giveaway page
            String segment = "";
            if (type != TradeListFragment.Type.ALL)
                segment = type.name().replace("_", "-").toLowerCase(Locale.ENGLISH) + "/";
            String url = "https://www.steamgifts.com/trades/" + segment + "search";

            Log.d(TAG, "Fetching trades for page " + page + " and URL " + url);

            Connection jsoup = Jsoup.connect(url)
                    .userAgent(Constants.JSOUP_USER_AGENT)
                    .timeout(Constants.JSOUP_TIMEOUT);
            jsoup.data("page", Integer.toString(page));

            if (searchQuery != null)
                jsoup.data("q", searchQuery);

            if (type == TradeListFragment.Type.CREATED)
                jsoup.followRedirects(false);

            if (SteamGiftsUserData.getCurrent(fragment.getContext()).isLoggedIn())
                jsoup.cookie("PHPSESSID", SteamGiftsUserData.getCurrent(fragment.getContext()).getSessionId());
            Document document = jsoup.get();

            SteamGiftsUserData.extract(fragment.getContext(), document);

            Elements trades = document.select(".table__row-inner-wrap");
            Log.d(TAG, "Found inner " + trades.size() + " elements");

            List<Trade> tradeList = new ArrayList<>();
            for (Element element : trades) {
                Element link = element.select("h3 a").first();

                // Basic information
                Uri uri = Uri.parse(link.attr("href"));
                String tradeId = uri.getPathSegments().get(1);
                String tradeName = uri.getPathSegments().get(2);

                Trade trade = new Trade(tradeId);
                trade.setTitle(link.text());
                trade.setName(tradeName);

                Element p = element.select(".table__column--width-fill p").first();
                trade.setCreatedTime(p.select("span").first().attr("title"));
                trade.setCreator(p.select("a").first().text());
                trade.setCreatorScorePositive(Utils.parseInt(p.select(".trade-feedback--positive").first().text()));
                trade.setCreatorScoreNegative(-Utils.parseInt(p.select(".trade-feedback--negative").first().text()));

                // The creator's avatar
                Element avatarNode = element.select(".global__image-inner-wrap").first();
                if (avatarNode != null)
                    trade.setCreatorAvatar(Utils.extractAvatar(avatarNode.attr("style")));

                trade.setLocked(element.hasClass("is-faded"));
                tradeList.add(trade);
            }

            return tradeList;
        } catch (Exception e) {
            Log.e(TAG, "Error fetching URL", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<Trade> result) {
        super.onPostExecute(result);
        fragment.addItems(result, page == 1);
    }
}
