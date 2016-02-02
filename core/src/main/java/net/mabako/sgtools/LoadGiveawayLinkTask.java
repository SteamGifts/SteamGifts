package net.mabako.sgtools;

import android.os.AsyncTask;
import android.util.Log;

import net.mabako.Constants;
import net.mabako.steamgifts.persistentdata.SGToolsUserData;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

/**
 * Execute the sgtools.info check and, if successful, return the link
 */
public class LoadGiveawayLinkTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = LoadGiveawayLinkTask.class.getSimpleName();

    private final SGToolsDetailFragment fragment;
    private final UUID uuid;

    private String url;
    private String error;


    public LoadGiveawayLinkTask(SGToolsDetailFragment fragment, UUID uuid) {
        this.fragment = fragment;
        this.uuid = uuid;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            // Call /check
            JSONObject check = fetchJson("check");
            if (check == null) {
                error = "Unknown error";
                return null;
            }

            // Can we enter the giveaway?
            boolean success = "true".equals(check.getString("success"));
            if (success) {
                // Yes!
                JSONObject link = fetchJson("getLink");
                if (link == null) {
                    error = "Unknown error";
                } else {
                    url = link.getString("url");
                }
            } else {
                // No, requirements not passed.
                error = check.getString("error");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching URL", e);
            error = "Caught Exception while loading";
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (url != null)
            fragment.onCheckSuccessful(url);
        else
            fragment.onCheckFailed(error);
    }

    private JSONObject fetchJson(String pathSegment) throws JSONException, IOException {
        String url = "http://www.sgtools.info/giveaways/" + uuid.toString().toLowerCase(Locale.ENGLISH) + "/" + pathSegment;

        Connection connection = Jsoup
                .connect(url)
                .userAgent(Constants.JSOUP_USER_AGENT)
                .timeout(Constants.JSOUP_TIMEOUT)
                .cookie("PHPSESSID", SGToolsUserData.getCurrent().getSessionId())
                .followRedirects(false)
                .ignoreContentType(true);

        Log.v(TAG, "Connecting to " + url);
        Connection.Response response = connection.method(Connection.Method.GET).execute();

        if (response != null && response.statusCode() == 200) {
            Log.v(TAG, "Result: " + response.body());
            return new JSONObject(response.body());
        } else {
            Log.w(TAG, "Got status code " + (response != null ? response.statusCode() : -1));
        }

        return null;
    }
}
