package net.mabako.steamgifts.tasks;

import android.app.Activity;
import android.util.Log;

import net.mabako.steamgifts.fragments.WriteCommentFragment;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.util.Map;

public class PostCommentTask extends AjaxTask<Activity> {
    private String description;

    public PostCommentTask(Activity fragment, String giveawayId, String xsrfToken, String description) {
        super(fragment, xsrfToken, "comment_new");

        setUrl("http://www.steamgifts.com/giveaway/" + giveawayId);
        this.description = description;
    }

    @Override
    public void addExtraParameters(Connection connection) {
        connection.data("parent_id", "");
        connection.data("description", description);
    }

    @Override
    protected void onPostExecute(Connection.Response response) {
        Log.d("Status", "" + response.statusCode());
        for(Map.Entry<String, String> header : response.headers().entrySet()) {
            Log.d("Status", header.getKey() + "=" + header.getValue());
        }
    }
}
