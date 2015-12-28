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
            Connection jsoup = Jsoup.connect("http://www.steamgifts.com/giveaway/" + giveawayId + "/");
            if (WebUserData.getCurrent().isLoggedIn())
                jsoup = jsoup.cookie("PHPSESSID", WebUserData.getCurrent().getSessionId());
            Document document = jsoup.get();

            WebUserData.extract(document);

            GiveawayExtras extras = new GiveawayExtras();

            Element description = document.select(".page__description__display-state").first();
            if(description != null)
                extras.setDescription(description.html());

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
