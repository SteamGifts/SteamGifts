package net.mabako.steamgifts.tasks;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import net.mabako.steamgifts.activities.WriteCommentActivity;
import net.mabako.steamgifts.data.Comment;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public abstract class EditCommentTask extends AjaxTask<Activity> {
    private static final String TAG = EditCommentTask.class.getSimpleName();

    private final Comment comment;
    private final String newText;

    public EditCommentTask(Activity activity, String xsrfToken, String newText, Comment comment) {
        super(activity, activity, xsrfToken, "comment_edit");

        this.newText = newText;
        this.comment = comment;
    }

    @Override
    protected void addExtraParameters(Connection connection) {
        connection.data("allow_replies", "1")
                .data("comment_id", String.valueOf(comment.getId()))
                .data("description", newText);
    }

    @Override
    protected void onPostExecute(Connection.Response response) {
        Activity activity = getFragment();
        if (response != null && response.statusCode() == 200) {
            try {
                Log.v(TAG, "Response to JSON request: " + response.body());
                JSONObject root = new JSONObject(response.body());

                boolean success = "success".equals(root.getString("type"));
                if (success) {
                    Document commentHtml = Jsoup.parse(root.getString("comment"));

                    // Save the content of the edit state for a bit & remove the edit state from being rendered.
                    Element editState = commentHtml.select(".comment__edit-state.is-hidden textarea[name=description]").first();
                    commentHtml.select(".comment__edit-state").html("");
                    Element desc = commentHtml.select(".comment__description").first();

                    if (editState == null)
                        Log.d(TAG, "edit state is null?");
                    comment.setEditableContent(editState == null ? null : editState.text());
                    comment.setContent(desc.html());

                    Intent data = new Intent();
                    data.putExtra("edited-comment", comment);
                    activity.setResult(WriteCommentActivity.COMMENT_EDIT_SENT, data);
                    activity.finish();

                    return;
                }
            } catch (JSONException e) {
                Log.e(TAG, "Failed to parse JSON object", e);
            }
        }
        onFail();
    }

    protected abstract void onFail();
}
