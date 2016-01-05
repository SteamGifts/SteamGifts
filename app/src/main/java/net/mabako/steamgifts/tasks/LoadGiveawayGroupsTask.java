package net.mabako.steamgifts.tasks;

import android.os.AsyncTask;
import android.util.Log;

import net.mabako.steamgifts.data.GiveawayGroup;
import net.mabako.steamgifts.fragments.GiveawayGroupListFragment;
import net.mabako.steamgifts.web.WebUserData;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class LoadGiveawayGroupsTask extends AsyncTask<Void, Void, List<GiveawayGroup>> {
    private static final String TAG = LoadGiveawayGroupsTask.class.getSimpleName();

    private final GiveawayGroupListFragment fragment;
    private final int page;
    private final String path;

    public LoadGiveawayGroupsTask(GiveawayGroupListFragment fragment, int page, String path) {
        this.fragment = fragment;
        this.page = page;
        this.path = path;
    }

    @Override
    protected List<GiveawayGroup> doInBackground(Void... params) {
        Log.d(TAG, "Fetching giveaways for page " + page);

        try {
            // Fetch the Giveaway page

            Connection jsoup = Jsoup.connect("http://www.steamgifts.com/giveaway/" + path + "/groups/search");
            jsoup.data("page", Integer.toString(page));

            if (WebUserData.getCurrent().isLoggedIn())
                jsoup.cookie("PHPSESSID", WebUserData.getCurrent().getSessionId());
            Document document = jsoup.get();

            WebUserData.extract(document);

            // Parse all rows of groups
            Elements groups = document.select(".table__row-inner-wrap");
            Log.d(TAG, "Found inner " + groups.size() + " elements");

            List<GiveawayGroup> groupList = new ArrayList<>();
            for (Element element : groups) {
                Element link = element.select(".table__column__heading").first();

                // Basic information
                String title = link.text();
                String id = link.attr("href").substring(7, 12);

                Log.e(TAG, "group " + title + ", " + id);

                String avatar = null;
                Element avatarNode = element.select(".global__image-inner-wrap").first();
                if (avatarNode != null)
                    avatar = Utils.extractAvatar(avatarNode.attr("style"));

                GiveawayGroup group = new GiveawayGroup(id, title, avatar);
                groupList.add(group);
            }

            return groupList;
        } catch (IOException e) {
            Log.e(TAG, "Error fetching URL", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<GiveawayGroup> result) {
        super.onPostExecute(result);
        fragment.addItems(result, page == 1);
    }
}
