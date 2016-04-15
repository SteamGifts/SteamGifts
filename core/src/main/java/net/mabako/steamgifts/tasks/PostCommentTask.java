package net.mabako.steamgifts.tasks;

import android.app.Activity;

import org.jsoup.Connection;

public abstract class PostCommentTask extends AjaxTask<Activity> {
    private final String description;
    private final long parentId;

    public PostCommentTask(Activity activity, String path, String xsrfToken, String description, long parentId) {
        super(activity, activity, xsrfToken, "comment_new");

        setUrl("https://www.steamgifts.com/" + path);
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
        if (response != null && response.statusCode() == 301) {
            onSuccess();
        } else
            onFail();
    }

    protected abstract void onSuccess();

    protected abstract void onFail();
}
