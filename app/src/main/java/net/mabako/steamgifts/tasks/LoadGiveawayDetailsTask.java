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

    private GiveawayDetailFragment fragment;
    private String giveawayId;

    public LoadGiveawayDetailsTask(GiveawayDetailFragment fragment, String giveawayId) {
        this.fragment = fragment;
        this.giveawayId = giveawayId;
    }


    @Override
    protected GiveawayExtras doInBackground(Void... params) {
        Log.d(TAG, "Fetching giveaway details for " + giveawayId);

        try {
            // Fetch the giveaway page
            Connection jsoup = Jsoup.connect("http://www.steamgifts.com/giveaway/" + giveawayId + "/");
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
            }

            return extras;
        } catch (IOException e) {
            Log.e(TAG, "Error fetching URL", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(GiveawayExtras giveawayDetails) {
        super.onPostExecute(giveawayDetails);
        fragment.setExtras(giveawayDetails);
    }
}
