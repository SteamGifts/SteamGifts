package net.mabako.steamgifts.tasks;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import net.mabako.Constants;
import net.mabako.steamgifts.data.Comment;
import net.mabako.steamgifts.data.Trade;
import net.mabako.steamgifts.data.TradeExtras;
import net.mabako.steamgifts.fragments.TradeDetailFragment;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class LoadTradeDetailsTask extends AsyncTask<Void, Void, TradeExtras> {
    private static final String TAG = LoadTradeDetailsTask.class.getSimpleName();

    private final TradeDetailFragment fragment;
    private String tradeId;
    private int page;
    private final boolean loadDetails;

    private Trade loadedDetails = null;
    private boolean lastPage = false;

    public LoadTradeDetailsTask(TradeDetailFragment fragment, String tradeId, int page, boolean loadDetails) {
        this.fragment = fragment;
        this.tradeId = tradeId;
        this.page = page;
        this.loadDetails = loadDetails;
    }

    @Override
    protected TradeExtras doInBackground(Void... params) {
        try {
            Connection.Response response = connect();
            if (response.statusCode() == 200) {
                Uri uri = Uri.parse(response.url().toURI().toString());
                Log.v(TAG, "Current URI -> " + uri);
                if (uri.getPathSegments().size() < 2)
                    throw new Exception("Could actually not find the discussion, we're at URI " + uri.toString());

                // are we expecting to be on this page? this can be most easily figured out if we check for the last path segment to be "search"
                if (!"search".equals(uri.getLastPathSegment())) {
                    // Let's just try again.
                    tradeId = uri.getPathSegments().get(1) + "/" + uri.getPathSegments().get(2);
                    response = connect();
                }


                Document document = response.parse();

                // Update user details
                SteamGiftsUserData.extract(fragment.getContext(), document);

                TradeExtras extras = loadExtras(document);
                if (loadDetails) {
                    loadedDetails = loadTrade(document, uri);
                }

                // Do we have a page?
                Element pagination = document.select(".pagination__navigation a").last();
                if (pagination != null) {
                    lastPage = !"Last".equalsIgnoreCase(pagination.text());
                    if (lastPage)
                        page = Integer.parseInt(pagination.attr("data-page-number"));

                } else {
                    // no pagination
                    lastPage = true;
                    page = 1;
                }

                return extras;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching URL", e);
        }
        return null;
    }

    private Connection.Response connect() throws IOException {
        String url = "https://www.steamgifts.com/trade/" + tradeId + "/search?page=" + page;
        Log.v(TAG, "Fetching trade details for " + url);
        Connection connection = Jsoup.connect(url)
                .userAgent(Constants.JSOUP_USER_AGENT)
                .timeout(Constants.JSOUP_TIMEOUT)
                .followRedirects(true);

        if (SteamGiftsUserData.getCurrent(fragment.getContext()).isLoggedIn())
            connection.cookie("PHPSESSID", SteamGiftsUserData.getCurrent(fragment.getContext()).getSessionId());

        return connection.execute();
    }

    private Trade loadTrade(Document document, Uri linkUri) {
        Element element = document.select(".comments").first();

        // Basic information
        String tradeLink = linkUri.getPathSegments().get(1);
        String tradeName = linkUri.getPathSegments().get(2);

        Trade trade = new Trade(tradeLink);
        trade.setName(tradeName);
        trade.setTitle(Utils.getPageTitle(document));

        trade.setCreator(element.select(".comment__username a").first().text());
        trade.setCreatedTime(element.select(".comment__actions > div span").first().attr("title"));
        trade.setCreatorScorePositive(Utils.parseInt(element.select(".trade-feedback--positive").first().text()));
        trade.setCreatorScoreNegative(-Utils.parseInt(element.select(".trade-feedback--negative").first().text()));

        Element headerButton = document.select(".page__heading__button").first();
        if (headerButton != null) {
            // remove the dropdown menu.
            headerButton.select(".page__heading__relative-dropdown").html("");

            // Is this button saying 'Closed'?
            trade.setLocked("Closed".equals(headerButton.text().trim()));
        }

        return trade;
    }

    @NonNull
    private TradeExtras loadExtras(Document document) {
        TradeExtras extras = new TradeExtras();

        // Load the description
        Element description = document.select(".comment__display-state .markdown").first();
        if (description != null) {
            // This will be null if no description is given.
            description.select("blockquote").tagName("custom_quote");
            description.select("div.want").tagName("trade_want");
            description.select("div.have").tagName("trade_have");
            extras.setDescription(Utils.loadAttachedImages(extras, description));
        }

        // Can we send a comment?
        Element xsrf = document.select(".comment--submit form input[name=xsrf_token]").first();
        if (xsrf != null)
            extras.setXsrfToken(xsrf.attr("value"));


        // Load comments
        Elements commentsNode = document.select(".comments");
        if (commentsNode.size() > 1) {
            Element rootCommentNode = commentsNode.last();
            if (rootCommentNode != null)
                Utils.loadComments(rootCommentNode, extras, 0, fragment.getAdapter().isViewInReverse(), true, Comment.Type.COMMENT);
        }

        return extras;
    }

    @Override
    protected void onPostExecute(TradeExtras tradeExtras) {
        super.onPostExecute(tradeExtras);

        if (tradeExtras != null || !loadDetails) {
            if (loadDetails)
                fragment.onPostTradeLoaded(loadedDetails);

            fragment.addItems(tradeExtras, page, lastPage);
        } else {
            Toast.makeText(fragment.getContext(), "Trade does not exist or could not be loaded", Toast.LENGTH_LONG).show();
            fragment.getActivity().finish();
        }
    }
}
