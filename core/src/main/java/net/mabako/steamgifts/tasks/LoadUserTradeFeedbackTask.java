package net.mabako.steamgifts.tasks;

import android.os.AsyncTask;
import android.util.Log;

import net.mabako.Constants;
import net.mabako.steamgifts.data.Comment;
import net.mabako.steamgifts.data.ICommentHolder;
import net.mabako.steamgifts.data.User;
import net.mabako.steamgifts.fragments.UserDetailFragment;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;

public class LoadUserTradeFeedbackTask extends AsyncTask<Void, Void, List<Comment>> {
    private static final String TAG = "LoadUserTradeFeedbackTa";

    private final UserDetailFragment.UserTradeFeedbackListFragment fragment;
    private final long steamID64;
    private final String rating;
    private final int page;
    private final User user;

    public LoadUserTradeFeedbackTask(UserDetailFragment.UserTradeFeedbackListFragment fragment, long steamID64, String rating, int page, User user) {
        this.fragment = fragment;
        this.steamID64 = steamID64;
        this.rating = rating;
        this.page = page;
        this.user = user;
    }


    @Override
    protected List<Comment> doInBackground(Void... params) {
        Log.d(TAG, "Fetching giveaways for user " + steamID64 + " (" + rating + ") on page " + page);

        try {
            // Fetch the Giveaway page
            Connection connection = Jsoup.connect("https://www.steamtrades.com/user/" + steamID64 + "/search")
                    .userAgent(Constants.JSOUP_USER_AGENT)
                    .timeout(Constants.JSOUP_TIMEOUT);
            connection.data("page", Integer.toString(page));
            connection.data("rating", rating);

            /* FIXME broken with the split of steamtrades & steamgifts
            if (SteamGiftsUserData.getCurrent(fragment.getContext()).isLoggedIn()) {
                connection.cookie("PHPSESSID", SteamGiftsUserData.getCurrent(fragment.getContext()).getSessionId());
                connection.followRedirects(false);
            }
            */

            Connection.Response response = connection.execute();
            Document document = response.parse();

            if (response.statusCode() == 200) {
                // FIXME SteamGiftsUserData.extract(fragment.getContext(), document);

                //if (!user.isLoaded())
                    //foundXsrfToken = Utils.loadUserProfile(user, document);

                user.setPositiveFeedback(Utils.parseInt(document.select(".increment_positive_review_count").text()));
                user.setNegativeFeedback(Utils.parseInt(document.select(".increment_negative_review_count").text()));


                Element rootCommentNode = document.select(".reviews").first();
                if (rootCommentNode != null) {
                    // Parse all rows of giveaways
                    ICommentHolder holder = new ICommentHolder() {
                        private List<Comment> list = new ArrayList<>();

                        @Override
                        public List<Comment> getComments() {
                            return list;
                        }

                        @Override
                        public void addComment(Comment comment) {
                            list.add(comment);
                        }
                    };
                    Utils.loadComments(rootCommentNode, holder, 0, false, true, Comment.Type.TRADE_FEEDBACK);
                    return holder.getComments();
                } else
                    return new ArrayList<>();
            } else {
                Log.w(TAG, "Got status code " + response.statusCode());
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching URL", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<Comment> result) {
        super.onPostExecute(result);

        if (!user.isFeedbackLoaded() && result != null) {
            user.setFeedbackLoaded(true);
            fragment.onUserUpdated(user);
        }

        fragment.addItems(result, page == 1, null);
    }
}
