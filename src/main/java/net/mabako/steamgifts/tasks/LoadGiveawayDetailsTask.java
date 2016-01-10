package net.mabako.steamgifts.tasks;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.data.GiveawayExtras;
import net.mabako.steamgifts.fragments.GiveawayDetailFragment;
import net.mabako.steamgifts.web.SteamGiftsUserData;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URISyntaxException;

public class LoadGiveawayDetailsTask extends AsyncTask<Void, Void, GiveawayExtras> {
    private static final String TAG = LoadGiveawayDetailsTask.class.getSimpleName();

    private final GiveawayDetailFragment fragment;
    private final String giveawayId;
    private final int page;

    private final boolean loadDetails;
    private Giveaway loadedDetails = null;
    private String error;

    public LoadGiveawayDetailsTask(GiveawayDetailFragment fragment, String giveawayId, int page, boolean loadDetails) {
        this.fragment = fragment;
        this.giveawayId = giveawayId;
        this.page = page;
        this.loadDetails = loadDetails;
    }

    @Override
    protected GiveawayExtras doInBackground(Void... params) {
        String url = "http://www.steamgifts.com/giveaway/" + giveawayId + "/search?page=" + page;
        Log.d(TAG, "Fetching giveaway details for " + url);

        try {
            Connection connection = Jsoup.connect(url);
            if (SteamGiftsUserData.getCurrent().isLoggedIn())
                connection.cookie("PHPSESSID", SteamGiftsUserData.getCurrent().getSessionId());

            Connection.Response response = connection.execute();
            Document document = response.parse();

            // Update user details
            SteamGiftsUserData.extract(document);

            // Check if we have an error page showing...
            Element breadcrumbs = document.select(".page__heading__breadcrumbs").first();
            if (breadcrumbs != null && "Error".equals(breadcrumbs.text())) {
                Log.d(TAG, "Error loading Giveaway");
                Element errorElem = document.select(".table__column--width-fill").last();
                if (errorElem != null) {
                    String error = errorElem.text().replace("You do not have permission to view this giveaway, since ", "");
                    this.error = error.substring(0, 1).toUpperCase() + error.substring(1);
                }
                return null;
            } else {
                GiveawayExtras extras = loadExtras(document);
                if (loadDetails) {
                    try {
                        loadedDetails = loadGiveaway(document, Uri.parse(response.url().toURI().toString()));
                    } catch (URISyntaxException e) {
                        Log.w(TAG, "say what - invalid url???", e);
                    }
                }

                return extras;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching URL", e);
            error = "Giveaway does not exist or could not be loaded.";
            return null;
        }
    }

    private Giveaway loadGiveaway(Document document, Uri linkUri) {
        Element element = document.select(".featured__inner-wrap").first();

        // Basic information
        String giveawayLink = linkUri.getPathSegments().get(1);
        String giveawayName = linkUri.getPathSegments().get(2);

        Giveaway giveaway = new Giveaway(giveawayLink);
        giveaway.setTitle(element.select(".featured__heading__medium").text());
        giveaway.setName(giveawayName);

        giveaway.setCreator(element.select(".featured__columns > div a").text());

        // Entries, would usually have comment count too... but we don't display that anywhere.
        giveaway.setEntries(-12345678);

        // this is overwritten by loadExtras()
        giveaway.setEntered(false);

        // More details
        Element icon = element.select(".global__image-outer-wrap--game-large").first();
        Uri uriIcon = icon.hasClass("global__image-outer-wrap--missing-image") ? null : Uri.parse(icon.attr("href"));

        Utils.loadGiveaway(giveaway, element, "featured", "featured__heading__small", uriIcon);
        return giveaway;
    }

    @NonNull
    private GiveawayExtras loadExtras(Document document) {
        GiveawayExtras extras = new GiveawayExtras();

        // Load the description
        Element description = document.select(".page__description__display-state .markdown").first();
        if (description != null) // This will be null if no description is given.
            extras.setDescription(description.html());

        // Load the xsrf token regardless of whether or not you can enter.
        Element xsrfToken = document.select("input[name=xsrf_token]").first();
        if (xsrfToken != null)
            extras.setXsrfToken(xsrfToken.attr("value"));

        // Enter/Leave giveaway
        Element enterLeaveForm = document.select(".sidebar form").first();
        if (enterLeaveForm != null) {
            extras.setEntered(enterLeaveForm.select(".sidebar__entry-insert").hasClass("is-hidden"));

            if (enterLeaveForm == document.select(".sidebar > form").first()) {
                extras.setEnterable(true);
            } else {
                extras.setErrorMessage("N/A");
            }
        } else {
            Element error = document.select(".sidebar .sidebar__error").first();
            if (error != null)
                extras.setErrorMessage(error.text().trim());
        }

        // Time left
        Element time = document.select("div.featured__columns div.featured__column > span").first();
        if (time != null)
            extras.setTimeRemaining(time.text().trim());

        // Load comments
        Element rootCommentNode = document.select(".comments").first();
        if (rootCommentNode != null)
            Utils.loadComments(rootCommentNode, extras);
        return extras;
    }

    @Override
    protected void onPostExecute(GiveawayExtras giveawayDetails) {
        super.onPostExecute(giveawayDetails);

        if (giveawayDetails != null || (!loadDetails && error == null)) {
            if (loadDetails)
                fragment.onPostGiveawayLoaded(loadedDetails);

            fragment.addDetails(giveawayDetails, page);
        } else {
            Toast.makeText(fragment.getContext(), error, Toast.LENGTH_LONG).show();
            fragment.getActivity().finish();
        }
    }
}
