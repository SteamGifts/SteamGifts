package net.mabako.steamgifts.tasks;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import net.mabako.steamgifts.data.Discussion;
import net.mabako.steamgifts.data.DiscussionExtras;
import net.mabako.steamgifts.fragments.DiscussionDetailFragment;
import net.mabako.steamgifts.web.SteamGiftsUserData;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class LoadDiscussionDetailsTask extends AsyncTask<Void, Void, DiscussionExtras> {
    private static final String TAG = LoadDiscussionDetailsTask.class.getSimpleName();

    private final DiscussionDetailFragment fragment;
    private String discussionId;
    private int page;
    private final boolean loadDetails;
    private Discussion loadedDetails = null;
    private boolean lastPage = false;

    public LoadDiscussionDetailsTask(DiscussionDetailFragment fragment, String discussionId, int page, boolean loadDetails) {
        this.fragment = fragment;
        this.discussionId = discussionId;
        this.page = page;
        this.loadDetails = loadDetails;

    }

    @Override
    protected DiscussionExtras doInBackground(Void... params) {

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
                    discussionId = uri.getPathSegments().get(1) + "/" + uri.getPathSegments().get(2);
                    response = connect();
                }


                Document document = response.parse();

                // Update user details
                SteamGiftsUserData.extract(document);

                DiscussionExtras extras = loadExtras(document);
                if (loadDetails) {
                    loadedDetails = loadDiscussion(document, uri);
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
        String url = "http://www.steamgifts.com/discussion/" + discussionId + "/search?page=" + page;
        Log.v(TAG, "Fetching discussion details for " + url);
        Connection connection = Jsoup.connect(url)
                .followRedirects(true);

        if (SteamGiftsUserData.getCurrent().isLoggedIn())
            connection.cookie("PHPSESSID", SteamGiftsUserData.getCurrent().getSessionId());

        Connection.Response response = connection.execute();
        return response;
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
                Utils.loadComments(rootCommentNode, extras, 0, fragment.getAdapter().isViewInReverse());
        }
        return extras;
    }

    @Override
    protected void onPostExecute(DiscussionExtras discussionExtras) {
        super.onPostExecute(discussionExtras);

        if (discussionExtras != null || !loadDetails) {
            if (loadDetails)
                fragment.onPostDiscussionLoaded(loadedDetails);

            fragment.addDetails(discussionExtras, page, lastPage);
        } else {
            Toast.makeText(fragment.getContext(), "Discussion does not exist or could not be loaded", Toast.LENGTH_LONG).show();
            fragment.getActivity().finish();
        }
    }
}
