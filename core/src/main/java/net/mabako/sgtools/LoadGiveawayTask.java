package net.mabako.sgtools;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import net.mabako.Constants;
import net.mabako.steamgifts.data.Game;
import net.mabako.steamgifts.persistentdata.SGToolsUserData;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

class LoadGiveawayTask extends AsyncTask<Void, Void, Giveaway> {
    private static final String TAG = LoadGiveawayTask.class.getSimpleName();

    private final UUID uuid;
    private final SGToolsDetailFragment fragment;
    private boolean needsLogin = false;
    private String giveawayUrl;

    public LoadGiveawayTask(SGToolsDetailFragment fragment, UUID uuid) {
        this.fragment = fragment;
        this.uuid = uuid;
    }

    @Override
    protected Giveaway doInBackground(Void... params) {
        String url = "http://www.sgtools.info/giveaways/" + uuid.toString().toLowerCase(Locale.ENGLISH);

        try {
            Log.v(TAG, "Connecting to " + url);
            Connection connection = Jsoup
                    .connect(url)
                    .userAgent(Constants.JSOUP_USER_AGENT)
                    .timeout(Constants.JSOUP_TIMEOUT)
                    .followRedirects(false);

            String sessionId = SGToolsUserData.getCurrent().getSessionId();
            // We'll be redirected if it is null anyway...
            if (sessionId != null) {
                connection.cookie("PHPSESSID", sessionId);
            }

            Connection.Response response = connection.method(Connection.Method.GET).execute();

            Log.v(TAG, url + " returned Status Code " + response.statusCode() + " (" + response.statusMessage() + ")");

            if (response.statusCode() == 200) {
                Document document = response.parse();

                Giveaway giveaway = new Giveaway();
                giveaway.setName(document.select(".featured__heading__medium").first().text());

                // TODO if sgtools.info ever allows you to create a giveaway for a game not on the store (tested HiB #3), check something more here.
                // Right now, it's only a 500 while creating such a giveaway, thus impossible.
                Uri steamUri = Uri.parse(document.select("a.global__image-outer-wrap--game-large").first().attr("href"));
                if (steamUri != null) {
                    List<String> pathSegments = steamUri.getPathSegments();
                    if (pathSegments.size() >= 2)
                        giveaway.setGameId(Integer.parseInt(pathSegments.get(1)));
                    giveaway.setType("app".equals(pathSegments.get(0)) ? Game.Type.APP : Game.Type.SUB);
                }

                // add all rules
                for (Element rule : document.select(".rules ul li"))
                    giveaway.addRule(rule.text());

                Element gaUrl = document.select(".gaurl").first();
                if (gaUrl != null)
                    giveawayUrl = gaUrl.text();

                return giveaway;
            }

            needsLogin = true;
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error fetching URL", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(Giveaway giveaway) {
        super.onPostExecute(giveaway);
        if (giveawayUrl != null) {
            fragment.onCheckSuccessful(giveawayUrl);
        } else if (giveaway != null) {
            fragment.onGiveawayLoaded(giveaway);
        } else if (needsLogin) {
            fragment.requestLogin();
        } else {
            Snackbar.make(fragment.getView(), "Unable to fetch Giveaway", Snackbar.LENGTH_INDEFINITE).setAction("Dismiss", new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            }).show();
        }
    }
}
