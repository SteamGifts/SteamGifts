package net.mabako.steamgifts.tasks;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import net.mabako.steamgifts.activities.WriteCommentActivity;

import org.jsoup.Connection;

import java.util.Map;

public class PostCommentTask extends AjaxTask<Activity> {
    private final String description;
    private final int parentId;

    public PostCommentTask(Activity fragment, String path, String xsrfToken, String description, int parentId) {
        super(fragment, xsrfToken, "comment_new");

        setUrl("http://www.steamgifts.com/" + path);
        this.description = description;
        this.parentId = parentId;
    }

    @Override
    public void addExtraParameters(Connection connection) {
        connection.data("parent_id", parentId == 0 ? "" : String.valueOf(parentId));
        connection.data("description", description);
    }

    @Override
    protected void onPostExecute(Connection.Response response) {
        Activity activity = getFragment();
        if (response.statusCode() == 301) {
            Intent data = new Intent();
            data.putExtra("parent", parentId);
            activity.setResult(WriteCommentActivity.COMMENT_SENT, data);
        } else {
            activity.setResult(WriteCommentActivity.COMMENT_NOT_SENT);
            Log.d("Status", "" + response.statusCode());
            for (Map.Entry<String, String> header : response.headers().entrySet()) {
                Log.d("Status", header.getKey() + "=" + header.getValue());
            }
        }
        activity.finish();
    }
}
