package net.mabako.steamgifts.tasks;

import android.os.AsyncTask;
import android.util.Log;

import net.mabako.steamgifts.data.DiscussionExtras;
import net.mabako.steamgifts.data.GiveawayExtras;
import net.mabako.steamgifts.fragments.DiscussionDetailFragment;
import net.mabako.steamgifts.web.WebUserData;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class LoadDiscussionDetailsTask extends AsyncTask<Void, Void, DiscussionExtras> {
    private static final String TAG = LoadDiscussionDetailsTask.class.getSimpleName();

    private final DiscussionDetailFragment fragment;
    private final String discussionId;
    private final int page;

    public LoadDiscussionDetailsTask(DiscussionDetailFragment fragment, String discussionId, int page) {
        this.fragment = fragment;
        this.discussionId = discussionId;
        this.page = page;

    }

    @Override
    protected DiscussionExtras doInBackground(Void... params) {
        String url = "http://www.steamgifts.com/discussion/" + discussionId + "/search?page=" + page;
        Log.d(TAG, "Fetching discussion details for " + url);

        try {
            Connection jsoup = Jsoup.connect(url);
            if (WebUserData.getCurrent().isLoggedIn())
                jsoup.cookie("PHPSESSID", WebUserData.getCurrent().getSessionId());
            Document document = jsoup.get();

            // Update user details
            WebUserData.extract(document);

            DiscussionExtras extras = new DiscussionExtras();

            // Load the description
            Element description = document.select(".comment__display-state .markdown").first();
            if (description != null) // This will be null if no description is given.
                extras.setDescription(description.html());

            // Load comments
            Elements commentsNode = document.select(".comments");
            if (commentsNode.size() > 1) {
                Element rootCommentNode = commentsNode.last();
                if (rootCommentNode != null)
                    Utils.loadComments(rootCommentNode, extras);
            }

            return extras;
        } catch (IOException e) {
            Log.e(TAG, "Error fetching URL", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(DiscussionExtras discussionExtras) {
        super.onPostExecute(discussionExtras);
        fragment.addDetails(discussionExtras, page);
    }
}
