package net.mabako.steamgifts.tasks;

import android.os.AsyncTask;
import android.util.Log;

import net.mabako.steamgifts.data.GiveawayExtras;
import net.mabako.steamgifts.fragments.GiveawayDetailFragment;
import net.mabako.steamgifts.web.WebUserData;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

public class LoadGiveawayDetailsTask extends AsyncTask<Void, Void, GiveawayExtras> {
    private static final String TAG = LoadGiveawayDetailsTask.class.getSimpleName();

    private final GiveawayDetailFragment fragment;
    private final String giveawayId;
    private final int page;

    public LoadGiveawayDetailsTask(GiveawayDetailFragment fragment, String giveawayId, int page) {
        this.fragment = fragment;
        this.giveawayId = giveawayId;
        this.page = page;
    }


    @Override
    protected GiveawayExtras doInBackground(Void... params) {
        String url = "http://www.steamgifts.com/giveaway/" + giveawayId + "/search?page=" + page;
        Log.d(TAG, "Fetching giveaway details for " + url);

        try {
            Connection jsoup = Jsoup.connect(url);
            if (WebUserData.getCurrent().isLoggedIn())
                jsoup = jsoup.cookie("PHPSESSID", WebUserData.getCurrent().getSessionId());
            Document document = jsoup.get();

            // Update user details
            WebUserData.extract(document);

            GiveawayExtras extras = new GiveawayExtras();

            // Load the description
            Element description = document.select(".page__description__display-state").first();
            if(description != null) // This will be null if no description is given.
                extras.setDescription(description.html());

            // Enter/Leave giveaway
            Element enterLeaveForm = document.select(".sidebar form").first();
            if(enterLeaveForm != null) {
                extras.setEntered(enterLeaveForm.select(".sidebar__entry-insert").hasClass("is-hidden"));
                extras.setXsrfToken(enterLeaveForm.select("input[name=xsrf_token]").attr("value"));
            } else {
                Element error = document.select(".sidebar .sidebar__error").first();
                if(error != null)
                    extras.setErrorMessage(error.text().trim());
            }

            // Load comments
            Element rootCommentNode = document.select(".comments").first();
            if(rootCommentNode != null)
                Utils.loadComments(rootCommentNode, extras);

            return extras;
        } catch (IOException e) {
            Log.e(TAG, "Error fetching URL", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(GiveawayExtras giveawayDetails) {
        super.onPostExecute(giveawayDetails);
        fragment.addDetails(giveawayDetails, page);
    }
}
