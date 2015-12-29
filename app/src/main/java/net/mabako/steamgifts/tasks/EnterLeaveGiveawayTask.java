package net.mabako.steamgifts.tasks;

import android.os.AsyncTask;
import android.util.Log;

import net.mabako.steamgifts.fragments.GiveawayDetailFragment;
import net.mabako.steamgifts.web.WebUserData;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * Task to enter or leave giveaways.
 */
public class EnterLeaveGiveawayTask extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = EnterLeaveGiveawayTask.class.getSimpleName();

    private String giveawayId;
    private String xsrfToken;
    private String what;

    private GiveawayDetailFragment fragment;

    public EnterLeaveGiveawayTask(GiveawayDetailFragment fragment, String giveawayId, String xsrfToken, String what) {
        this.fragment = fragment;
        this.giveawayId = giveawayId;
        this.xsrfToken = xsrfToken;
        this.what = what;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        Log.i(TAG, what + " -> " + giveawayId);
        try {
            Document document = Jsoup
                    .connect("http://www.steamgifts.com/ajax.php")
                    .data("xsrf_token", xsrfToken)
                    .data("do", what)
                    .data("code", giveawayId)
                    .cookie("PHPSESSID", WebUserData.getCurrent().getSessionId())
                    .post();

            Log.d(TAG, "POST result: " + document.text());
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error fetching URL", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        fragment.onEnterLeaveResult(what, aBoolean);
    }
}
