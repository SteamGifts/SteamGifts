package net.mabako.steamgifts.tasks;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import net.mabako.steamgifts.data.Discussion;
import net.mabako.steamgifts.data.DiscussionExtras;
import net.mabako.steamgifts.fragments.DiscussionDetailFragment;
import net.mabako.steamgifts.web.WebUserData;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URISyntaxException;

public class LoadDiscussionDetailsTask extends AsyncTask<Void, Void, DiscussionExtras> {
    private static final String TAG = LoadDiscussionDetailsTask.class.getSimpleName();

    private final DiscussionDetailFragment fragment;
    private final String discussionId;
    private final int page;
    private final boolean loadDetails;
    private Discussion loadedDetails = null;

    public LoadDiscussionDetailsTask(DiscussionDetailFragment fragment, String discussionId, int page, boolean loadDetails) {
        this.fragment = fragment;
        this.discussionId = discussionId;
        this.page = page;
        this.loadDetails = loadDetails;

    }

    @Override
    protected DiscussionExtras doInBackground(Void... params) {
        String url = "http://www.steamgifts.com/discussion/" + discussionId + "/search?page=" + page;
        Log.d(TAG, "Fetching discussion details for " + url);

        try {
            Connection connection = Jsoup.connect(url);
            if (WebUserData.getCurrent().isLoggedIn())
                connection.cookie("PHPSESSID", WebUserData.getCurrent().getSessionId());

            Connection.Response response = connection.execute();
            Document document = response.parse();

            // Update user details
            WebUserData.extract(document);

            DiscussionExtras extras = loadExtras(document);
            if (loadDetails) {
                try {
                    loadedDetails = loadDiscussion(document, Uri.parse(response.url().toURI().toString()));
                } catch (URISyntaxException e) {
                    Log.w(TAG, "say what - invalid url???", e);
                }
            }

            return extras;
        } catch (Exception e) {
            Log.e(TAG, "Error fetching URL", e);
            return null;
        }
    }

    private Discussion loadDiscussion(Document document, Uri linkUri) {
        Element element = document.select(".comments").first();

        // Basic information
        String discussionLink = linkUri.getPathSegments().get(1);
        String discussionName = linkUri.getPathSegments().get(2);

        Discussion discussion = new Discussion(discussionLink);
        discussion.setName(discussionName);
        discussion.setTitle(document.title()); // TODO is this "good enough"?

        discussion.setCreator(element.select(".comment__username a").first().text());
        discussion.setTimeCreated(element.select(".comment__actions > div span").first().text());

        return discussion;
    }

    @NonNull
    private DiscussionExtras loadExtras(Document document) {
        DiscussionExtras extras = new DiscussionExtras();

        // Load the description
        Element description = document.select(".comment__display-state .markdown").first();
        if (description != null) // This will be null if no description is given.
            extras.setDescription(description.html());

        // Can we send a comment?
        Element xsrf = document.select(".comment--submit form input[name=xsrf_token]").first();
        if (xsrf != null)
            extras.setXsrfToken(xsrf.attr("value"));


        // Load comments
        Elements commentsNode = document.select(".comments");
        if (commentsNode.size() > 1) {
            Element rootCommentNode = commentsNode.last();
            if (rootCommentNode != null)
                Utils.loadComments(rootCommentNode, extras);
        }
        return extras;
    }

    @Override
    protected void onPostExecute(DiscussionExtras discussionExtras) {
        super.onPostExecute(discussionExtras);

        if (discussionExtras != null || !loadDetails) {
            if (loadDetails)
                fragment.onPostDiscussionLoaded(loadedDetails);

            fragment.addDetails(discussionExtras, page);
        } else {
            Toast.makeText(fragment.getContext(), "Discussion does not exist or could not be loaded", Toast.LENGTH_LONG).show();
            fragment.getActivity().finish();
        }
    }
}
