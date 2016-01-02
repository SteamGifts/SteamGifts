package net.mabako.steamgifts.tasks;

import android.os.AsyncTask;
import android.util.Log;

import net.mabako.steamgifts.data.Discussion;
import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.fragments.DiscussionListFragment;
import net.mabako.steamgifts.web.WebUserData;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Fetch a list of all discussions.
 */
public class LoadDiscussionListTask extends AsyncTask<Void, Void, List<Discussion>> {
    private static final String TAG = LoadGiveawayListTask.class.getSimpleName();

    private final DiscussionListFragment fragment;
    private final int page;
    private final DiscussionListFragment.Type type;
    private final String searchQuery;

    public LoadDiscussionListTask(DiscussionListFragment fragment, int page, DiscussionListFragment.Type type, String searchQuery) {
        this.fragment = fragment;
        this.page = page;
        this.type = type;
        this.searchQuery = searchQuery;
    }

    @Override
    protected List<Discussion> doInBackground(Void... params) {
        try {
            // Fetch the Giveaway page
            String segment = "";
            if (type != DiscussionListFragment.Type.ALL)
                segment = type.name().replace("_", "-").toLowerCase(Locale.ENGLISH) + "/";
            String url = "http://www.steamgifts.com/discussions/" + segment + "search";

            Log.d(TAG, "Fetching discussions for page " + page + " and URL " + url);

            Connection jsoup = Jsoup.connect(url);
            jsoup.data("page", Integer.toString(page));

            if (searchQuery != null)
                jsoup.data("q", searchQuery);

            if (WebUserData.getCurrent().isLoggedIn())
                jsoup.cookie("PHPSESSID", WebUserData.getCurrent().getSessionId());
            Document document = jsoup.get();

            WebUserData.extract(document);

            // Parse all rows of giveaways
            Elements discussions = document.select(".table__row-inner-wrap");
            Log.d(TAG, "Found inner " + discussions.size() + " elements");

            List<Discussion> discussionList = new ArrayList<>();
            for (Element element : discussions) {
                Element link = element.select("h3 a").first();

                // Basic information
                String title = link.text();
                String discussionId = link.attr("href").substring(12, 17);
                String discussionName = link.attr("href").substring(18);

                Element p = element.select(".table__column--width-fill p").first();
                String timeAgo = p.select("span").first().text();
                String creator = p.select("a").last().text();

                // The creator's avatar
                String avatar = null;
                Element avatarNode = element.select(".global__image-inner-wrap").first();
                if(avatarNode != null)
                    avatar = Utils.extractAvatar(avatarNode.attr("style"));


                Discussion discussion = new Discussion(discussionId, title, creator, timeAgo, avatar);
                discussion.setLocked(element.hasClass("is-faded"));
                discussionList.add(discussion);
            }
            return discussionList;
        } catch (Exception e) {
            Log.e(TAG, "Error fetching URL", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<Discussion> result) {
        super.onPostExecute(result);
        fragment.addItems(result, page == 1);
    }
}
