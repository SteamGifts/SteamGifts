package net.mabako.steamgifts.tasks;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import net.mabako.steamgifts.activities.WriteCommentActivity;
import net.mabako.steamgifts.core.R;

import org.jsoup.Connection;

public class PostCommentTask extends AjaxTask<Activity> {
    private final String description;
    private final int parentId;

    public PostCommentTask(Activity activity, String path, String xsrfToken, String description, int parentId) {
        super(activity, activity, xsrfToken, "comment_new");

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
        if (response != null && response.statusCode() == 301) {
            Intent data = new Intent();
            data.putExtra("parent", parentId);
            activity.setResult(WriteCommentActivity.COMMENT_SENT, data);
            activity.finish();
        } else {
            Toast.makeText(activity, R.string.comment_not_sent, Toast.LENGTH_SHORT).show();
        }
    }
}
