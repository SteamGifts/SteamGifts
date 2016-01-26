package net.mabako.steamgifts.tasks;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import net.mabako.steamgifts.activities.UrlHandlingActivity;
import net.mabako.steamgifts.activities.WebViewActivity;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.data.MessageHeader;
import net.mabako.steamgifts.fragments.profile.MessageListFragment;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class LoadMessagesTask extends AsyncTask<Void, Void, List<IEndlessAdaptable>> {
    private final static String TAG = LoadMessagesTask.class.getSimpleName();

    private final MessageListFragment fragment;
    private final int page;

    private String foundXsrfToken = null;

    public LoadMessagesTask(MessageListFragment fragment, int page) {
        this.fragment = fragment;
        this.page = page;
    }

    @Override
    protected List<IEndlessAdaptable> doInBackground(Void... params) {
        try {
            // Fetch the Giveaway page

            Connection jsoup = Jsoup.connect("http://www.steamgifts.com/messages/search");
            jsoup.data("page", Integer.toString(page));
            jsoup.cookie("PHPSESSID", SteamGiftsUserData.getCurrent().getSessionId());

            Document document = jsoup.get();

            SteamGiftsUserData.extract(document);

            // Fetch the xsrf token
            Element xsrfToken = document.select("input[name=xsrf_token]").first();
            if (xsrfToken != null)
                foundXsrfToken = xsrfToken.attr("value");

            // Parse all rows of giveaways
            return loadMessages(document);
        } catch (Exception e) {
            Log.e(TAG, "Error fetching URL", e);
            return null;
        }
    }

    private List<IEndlessAdaptable> loadMessages(Document document) {
        List<IEndlessAdaptable> list = new ArrayList<>();
        Elements children = document.select(".comments__entity");
        for (Element element : children) {
            Element link = element.select(".comments__entity__name a").first();
            if (link != null) {
                // Click action for the header
                String linkText = "http://www.steamgifts.com" + link.attr("href");
                MessageHeader message = new MessageHeader(link.text());

                Element commentElement = element.nextElementSibling();
                if (commentElement != null)
                    Utils.loadComments(commentElement, message);

                // add the message & all associated comments.
                list.add(message);
                list.addAll(message.getComments());
            }
        }

        return list;
    }

    @Override
    protected void onPostExecute(List<IEndlessAdaptable> iEndlessAdaptables) {
        super.onPostExecute(iEndlessAdaptables);
        fragment.addItems(iEndlessAdaptables, page == 1, foundXsrfToken);
    }
}
