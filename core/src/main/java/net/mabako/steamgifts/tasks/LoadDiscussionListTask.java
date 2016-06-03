package net.mabako.steamgifts.tasks;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import net.mabako.Constants;
import net.mabako.steamgifts.data.Discussion;
import net.mabako.steamgifts.fragments.DiscussionListFragment;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

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
            String url = "https://www.steamgifts.com/discussions/" + segment + "search";

            Log.d(TAG, "Fetching discussions for page " + page + " and URL " + url);

            Connection jsoup = Jsoup.connect(url)
                    .userAgent(Constants.JSOUP_USER_AGENT)
                    .timeout(Constants.JSOUP_TIMEOUT);
            jsoup.data("page", Integer.toString(page));

            if (searchQuery != null)
                jsoup.data("q", searchQuery);

            // We do not want to follow redirects here, because SteamGifts redirects to the main (giveaways) page if we're not logged in.
            // For all other pages however, if we're not logged in, we're redirected once as well?
            if (type == DiscussionListFragment.Type.CREATED)
                jsoup.followRedirects(false);

            if (SteamGiftsUserData.getCurrent(fragment.getContext()).isLoggedIn())
                jsoup.cookie("PHPSESSID", SteamGiftsUserData.getCurrent(fragment.getContext()).getSessionId());
            Document document = jsoup.get();

            SteamGiftsUserData.extract(fragment.getContext(), document);

            // Parse all rows of discussions
            Elements discussions = document.select(".table__row-inner-wrap");
            Log.d(TAG, "Found inner " + discussions.size() + " elements");

            List<Discussion> discussionList = new ArrayList<>();
            for (Element element : discussions) {
                Element link = element.select("h3 a").first();

                // Basic information
                Uri uri = Uri.parse(link.attr("href"));
                String discussionId = uri.getPathSegments().get(1);
                String discussionName = uri.getPathSegments().get(2);

                Discussion discussion = new Discussion(discussionId);
                discussion.setTitle(link.text());
                discussion.setName(discussionName);

                Element p = element.select(".table__column--width-fill p").first();
                discussion.setCreatedTime(p.select("span").first().attr("title"));
                discussion.setCreator(p.select("a").last().text());

                // The creator's avatar
                Element avatarNode = element.select(".global__image-inner-wrap").first();
                if (avatarNode != null)
                    discussion.setCreatorAvatar(Utils.extractAvatar(avatarNode.attr("style")));

                discussion.setLocked(element.hasClass("is-faded"));
                discussion.setPoll(!element.select("h3 i.fa-align-left").isEmpty());
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
